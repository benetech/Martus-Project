#!/bin/bash

have_cmd readlink ||
readlink() {
	[[ -o xtrace ]] && { set +x; local xtrace="set -x"; } || local xtrace=
	local file=${1-}
	[[ $file == -* ]] && error "prologue.sh readlink can't handle options"

	local link=$(ls -ld $file 2>/dev/null | sed -n 's/.* -> //p')
	echo "$link"
	$xtrace
	[[ $link ]]
	return $?
}

# ----------------------------------------------------------------------------
# cron functions
# ----------------------------------------------------------------------------

is_cron_job() {
	typeset -i PID=$1

	(( $PID > 1 )) || return 1

	set -- $(ps --pid $PID --no-headers --format ppid,command )
	[[ $# == 0 ]] && return 1
	local parent_PID=$1 cmd=${2##*/}

	[[ $cmd == cron || $cmd == crond ]] && return 0
	[[ $parent_PID == 1 ]] && return 1

	is_cron_job $parent_PID
	return $?
}

# ----------------------------------------------------------------------------

exit_if_cron_run_to_skip() {

	local skip_file=/var/lib/skip_cron_run/$our_name

	[[ -e $skip_file ]] || return 0
	is_cron_job $$ || return 0

	typeset timestamp=$tmpdir/exit_if_cron_run_to_skip$$
	touch  $timestamp

	if [ $skip_file -ot $timestamp ]
	   then rm $skip_file
		echo "$our_name being skipped for this cron run"
	   else echo "$our_name being skipped for cron runs"
	fi

	rm $timestamp

	exit 0
}

exit_if_cron_run_to_skip

# ----------------------------------------------------------------------------

# delete rolled logs (i.e. log with appended date) that are older then -e days
expire_date_stamped_logs() {
	[[ $1 == -e ]] ||
	   error "Usage: expire_date_stamped_logs -e max_days_old log_name(s)"
	shift; local expire_days=$1; shift

	if [[ -o noglob ]]
	   then set +o noglob
		local was_noglob=$true
	   else local was_noglob=$false
	fi
	local logs=
	local log_name
	for   log_name in $*
	    do	set -- $log_name[-.][0-9][0-9][0-9][0-9][0-9][0-9]*
		[[ $# != 1 || -e $1 ]] || continue
		logs="$logs $*"
	done
	[[ $was_noglob ]] && set -o noglob

	[[ $logs ]] || return 1
	logs=$(find $logs -mtime +$expire_days)
	[[ $logs ]] || return 1

	have_cmd set_extra_caps_for_exec ||
	  source /etc/$_our_app_/libsecure.sh || error libsecure.sh
	local extra_caps_for_exec
	  set_extra_caps_for_exec immutable

	${Run-} chattr -ia $logs
	if is_append_only .
	   then ${Run-} chattr -a .
		local is_CWD_append_only=$true
	   else local is_CWD_append_only=$false
	fi
	${Run-} rm $logs
	[[ $is_CWD_append_only ]] && ${Run-} chattr +a .
}

# ----------------------------------------------------------------------------
# miscellaneous BSO-specific functions
# ----------------------------------------------------------------------------

mount_all_option_NFS() {

	[[ -o xtrace ]] && { set +x; local xtrace="set -x"; } || local xtrace=

	local option=$1; shift
	[[ $option == bg ]] && local bg=bg || local bg=

	local did_find_unmounted=$false
	local      device mount type options ignore
	while read device mount type options ignore
	   do	[[ $device == \#* ]] && continue
		[[ $type == nfs ]] || continue
		[[ ,$options, == *,$option,* ]] || continue
		fgrep --silent " $mount " /proc/mounts && continue

		did_find_unmounted=$true
		$xtrace
		eval ${Run-} mount $mount $bg ||
		  echo " ... from mount $mount" >&2
		set +x
	done < /etc/fstab

	$xtrace
	[[ $did_find_unmounted ]]
}

# ----------------------

mount_all_noauto_NFS() { mount_all_option_NFS nomount "$@"; }
mount_all_bg_NFS()     { mount_all_option_NFS   bg    "$@"; }

# legacy name, don't use it
mount_all_nfs_noauto() { mount_all_noauto_NFS "$@"; }

# ----------------------------------------------------------------------------

live_host=bookshare02

 is_dev=$false is_development=$false
  is_qa=$false is_alpha=$false
is_live=$false is_production=$false

setup_hostname_vars()
{

	local hostname=${HOSTNAME%%.*}

	case $hostname in
	   benetech03  ) is_qa=$true is_dev=$true ;;
	   $live_host  ) is_live=$true ;;
	   bookshare?? ) is_live=$true ;;
	   * ) is_qa=$true ;;		# non-$live_host defaults to QA
	esac

	# legacy names
	is_development=$is_dev
	is_alpha=$is_qa
	is_production=$is_live
}

setup_hostname_vars

# ----------------------------------------------------------------------------

check_SSH_key_agent() {
	local disable_SSH_key_agent=$*

	local user=$(id --user --name)
	eval "local home=~$user"
	if [[ $disable_SSH_key_agent ]]
	   then unset SSH_AGENT_PID SSH_AUTH_SOCK
		export HOME=$home
	   else [[ ${SSH_AGENT_PID-} || ${SSH_AUTH_SOCK-} || \
		   ( $home != $HOME && $user != root )		]] &&
		   warn "using your SSH keypair, not $user's; see -A or -a"
	fi
}

true
