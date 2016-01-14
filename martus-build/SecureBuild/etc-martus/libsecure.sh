#!/bin/bash

# The Martus(tm) free, social justice documentation and
# monitoring software. Copyright (C) 2004-2010, Beneficent
# Technology, Inc. (Benetech).
# 
# Martus is free software; you can redistribute it and/or
# modify it under the terms of the GNU General Public License
# as published by the Free Software Foundation; either
# version 2 of the License, or (at your option) any later
# version with the additions and exceptions described in the
# accompanying Martus license file entitled "license.txt".
# 
# It is distributed WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
# IMPLIED, including warranties of fitness of purpose or
# merchantability.  See the accompanying Martus License and
# GPL license for more details on the required license terms
# for this software.
# 
# You should have received a copy of the GNU General Public
# License along with this program; if not, write to the Free
# Software Foundation, Inc., 59 Temple Place - Suite 330,
# Boston, MA 02111-1307, USA.

# ----------------------------------------------------------------------------
# handle odd environments
# ----------------------------------------------------------------------------

[[ ${_did_source_prologue_sh-} ]] ||
   { echo "$0: first source prologue.sh" >&2; exit 1; }

if ! have_cmd caps
   then for __cmd__ in /usr/local/*/sbin/caps
	    do	[[ -x $__cmd__ ]] || continue
		PATH=$PATH:${__cmd__%/*}
		break
	done
	unset __cmd__
fi

# ----------------------------------------------------------------------------
# functions to manipulate firewall and (IP addresses)
# ----------------------------------------------------------------------------

# clean contents of arguement, e.g. account names, host names, IP addresses
function set_cleaned_list()
{
	local args="$@"

	set -f
	set -- ${args//[+;,|\'\"]/ }
	set +f

	cleaned_list=$*

	[[ $cleaned_list ]]
	return $?
}

# ----------------------------------------------------------------------------

function set_IPs()
{
	IPs=$*

	set_cleaned_list $IPs
	IPs=$cleaned_list

	[[ $IPs ]]
	return $?
}

# ----------------------------------------------------------------------------

# between $1 (chain) and final IP addrs, can pass e.g. --foo=1.2.3.4
#   to load opts (and this function will replace all '=' with SPACE)
function _set_chain__opts__IPs()
{
	chain=$1; shift

	while [[ ${1-} == -* ]]
	   do	add_words opts $1
		shift
	done
	opts=$(echo $opts | sed 's/=/ /g')

	local    raw_IPs=$*
	set_IPs $raw_IPs
	return $?
}

# ----------------------------------------------------------------------------

# the caller can change this
[[ $is_pre_2_6_kernel ]] && FW_cmd=ipchains || FW_cmd=iptables

# WARNING: this sets the global variable 'status' if there's an error
function FW()
{
	$Run $FW_cmd "$@" && return 0

	status=$?			# caller can declare this local
	warn "$FW_cmd $@ => $status"
	return $status
}

# ----------------------------------------------------------------------------

# between $1 (--src/--dst) and final IP addrs, can pass e.g. --foo=1.2.3.4
#   (and this function will replace all '=' with SPACE)
# WARNING: this calls FW which sets global variable status
function FW_allow()
{
	local which_endpoint=$1; shift
	local chain  opts  IPs
	 _set_chain__opts__IPs $* || return 0

	for IP in $IPs
	    do	local CIDR_mask=
		case $IP in
		   0.0.0.0 ) CIDR_mask=/0  ;;
		   *.0.0.0 ) CIDR_mask=/8  ;;
		   *.*.0.0 ) CIDR_mask=/16 ;;
		   *.*.*.0 ) CIDR_mask=/24 ;;
		esac
		FW -A $chain $opts $which_endpoint $IP$CIDR_mask --jump ACCEPT
	done

	return ${status-0}
}

# ------------------------------------------------------------------

function FW_allow_originate() { FW_allow --destination "$@"; }
function FW_allow_answer   () { FW_allow --source      "$@"; }

# ----------------------------------------------------------------------------

# insert rules at beginning of chain, in case special logging at end
# WARNING: this calls FW which sets global variable status
function load_FW_chains_for_TCP_originate()
{
	local chain  opts  IPs
	 _set_chain__opts__IPs $* || return 0

	for IP in $IPs
	    do	FW -I ${chain}_out 1 -d $IP -p tcp         -j ACCEPT
		FW -I ${chain}_in  1 -s $IP -p tcp ! --syn -j ACCEPT
	done

	return ${status-0}
}

# ----------------------------------------------------------------------------

# if chain is 'ssh', we also allow full ICMP to IP addresses
# WARNING: this calls FW which sets global variable status
function load_FW_chains_for_TCP_answer()
{
	local chain  opts  IPs
	 _set_chain__opts__IPs $* || return 0

	for IP in $IPs
	    do	FW -I ${chain}_in  1  -s $IP -p tcp         -j ACCEPT
		FW -I ${chain}_out 1  -d $IP -p tcp ! --syn -j ACCEPT

		[[ $chain == ssh ]] &&
		FW -I icmp 1 --bidirectional -s $IP -p icmp -j ACCEPT
	done

	return ${status-0}
}

# ----------------------------------------------------------------------------

function set_interface_IP_addrs()
{
	local network_interfaces=$*

	interface_IP_addrs=

	local interface
	for interface in $network_interfaces
	    do	set -- $(ifconfig $interface 2>/dev/null |
		 sed -n 's/.*[ 	]inet addr: *\([.0-9]*\)[ 	].*/\1/p')
		[[ ${1-} ]] || continue
		if [[ ${interface_IP_addrs-} ]]
		   then interface_IP_addrs=$interface_IP_addrs,$1
		   else interface_IP_addrs=$1
		fi
	done

	[[ ${interface_IP_addrs-} ]]
	return $?
}

# ----------------------------------------------------------------------------
# generic functions to identify processes, useful with caps --pid
# ----------------------------------------------------------------------------

# the 'ps' command uses /proc, which is more secure than utmp or wtmp
function set_tty___from_PID()
{
	local PID=$1

	set -f
	set -- $(ps --no-header --format=tty --pid=$PID)
	set +f

	tty=${1-}
	[[ $# == 1 ]] || return 1

	[[ $tty == '?' ]] && return 1

	[[ $tty == /dev/* ]] || tty=/dev/$tty

	return 0
}

# ----------------------------------------------------------------------------

# ps uses /proc, which is more secure than utmp or wtmp
function set_login_user__login_PID()
{
	local tty=$1

	[[ $tty == /dev/* ]] || tty=/dev/$tty

	set -- $(ps -H --no-header --format=user,pid --tty=$tty)
	[[ $# != 0 ]] || return 1

	login_user=$1 login_PID=$2

	return 0
}

# ----------------------------------------------------------------------------

set_parent_PID()
{
	local PID=$1

	set -- $(ps --no-headers --format=ppid --pid=$PID)
	parent_PID=${1-}
}

# ----------------------------------------------------------------------------

# exe_path will be set to null of process disappears
function set_exe_path__script_path()
{
	local PID=$1

	local symlink=/proc/$PID/exe

	exe_path=$(readlink $symlink) script_path=

	if [[ ! $exe_path ]]
	   then [[ -d /proc/$PID ]] || return 1
		[[ $UID == 0 ]] ||
		   error "set_exe_path__script_path $PID: must be root"
	fi

	case $exe_path in
	   /bin/bash | /usr/bin/perl | /usr/bin/python ) ;;
	   * ) return 0 ;;
	esac

	local cmdline=/proc/$PID/cmdline

	# if script disappeared, we still have exe_path
	[[ -e $cmdline ]] || return 0 

	set -f
	set -- $(tr '\0' ' ' < $cmdline)
	set +f
	[[ $# == 0 ]] && return 0

	until [[ $# == 1 || $1 == /* ]]
	   do	shift;
	done

	script_path=$1

	return 0
}

# ----------------------------------------------------------------------------

function do_PIDs_have_exe_path()
{
	local cmd_path=$1; shift; local PIDs=$*

	local PID
	for PID in $PIDs
	    do	local exe_path script_path
		set_exe_path__script_path $PID
		[[ $exe_path == $cmd_path ]] || return 1
	done

	return 0
}

# ----------------------------------------------------------------------------

function do_PIDs_have_path()
{
	local cmd_path=$1; shift; local PIDs=$*

	local PID
	for PID in $PIDs
	    do	local exe_path script_path
		set_exe_path__script_path $PID
		[[    $exe_path == $cmd_path ]] && continue
		[[ $script_path == $cmd_path ]] && continue
		return 1
	done

	return 0
}

# ----------------------------------------------------------------------------

function is_child_of_sshd()
{
	local PID=$1

	local parent_PID
	set_parent_PID $PID

	local tty
	set_tty___from_PID $parent_PID
	[[ $tty == '?' ]] || return 1

	do_PIDs_have_exe_path /usr/sbin/sshd $parent_PID
	return $?
}

# ----------------------------------------------------------------------------

function set_PID___from_lock_file()
{
	local _lock_file=$1

	local lock_dir=/var/lock
	if [[ ! -f $_lock_file && $_lock_file != */* ]]
	   then [[ $_lock_file == LCK..* || -f $lock_dir/$_lock_file ]] ||
		    _lock_file=LCK..$_lock_file
		[[ -f $_lock_file ]] || _lock_file=$lock_dir/$_lock_file
	fi

	if true
	   then set -- $(< $_lock_file)
	fi 2>/dev/null
	PID=$*

	[[ $PID ]]
	return $?
}

# -------------------------------------------------------

function lock_PID()
{
	local _lock_file=$1

	if      [[ $_lock_file != */* ]]
	   then [[ $_lock_file == LCK..* ]] || _lock_file=LCK..$_lock_file
		    _lock_file=/var/lock/$_lock_file
	fi

	$Run lockpid $_lock_file && return 0
	[[ -f $_lock_file ]] || return 1

	local  PID SIDs
	if set_PID___from_lock_file $_lock_file
	   then if set_SIDs___from_ps $PID
		   then ps -f w -H --sid $SIDs
		   else ps -f w $PID
		fi
	   else PID="<unknown>"
	fi
	$Run warn "PID $PID has locked '${_lock_file##*/LCK..}'"
}

# ----------------------------------------------------------------------------

function set_PIDs___from_ps()
{
	set -- $(ps --no-headers --format=pid "$@")

	PIDs=$*

	[[ $PIDs ]]
	return $?
}

# ----------------------------------------------------------------------------

function set_SIDs___from_ps()
{
	set -- $(ps --no-headers --format=sid "$@")

	SIDs=$*

	[[ $SIDs ]]
	return $?
}

# ----------------------------------------------------------------------------

function set_PIDs___for_cmd()
{

	local cmd all_PIDs=
	for cmd
	    do	set_PIDs___from_ps  -C $cmd && add_words all_PIDs $PIDs
	done

	PIDs=$all_PIDs

	[[ $PIDs ]]
	return $?
}

# ----------------------------------------------------------------------------

function set_script_path()
{
	local PID=$1

	script_path=

	set -- /bin/bash /usr/bin/perl /usr/bin/python /usr/bin/ruby
	local interpreter_paths=$*

	local exe_path=$(readlink /proc/$PID/exe)
	is_arg1_in_arg2 $exe_path $interpreter_paths || return 1

	set -f
	set -- $(tr '\0' ' ' < /proc/$PID/cmdline)
	set +f

	shift			# toss interpreter
	while [[ $# != 0 && $1 == -* ]]; do shift; done # toss args

	[[ $# == 0 ]] && return 1

	script_path=$1
	return 0
}
		
# ----------------------------------------------------------------------------

function set_PIDs___for_trusted_cmd()
{
	local cmds=$*

	set --  /*bin /usr/*bin /etc/init.d /usr/local/martus/*bin \
		/usr/local/j*re*/bin /usr/local/sbin /usr/local/bookshare/*bin
	local trusted_dirs=$*

	set_PIDs___for_cmd $cmds || return 1

	local PID trusted_PIDs=
	for PID in $PIDs
	    do	local  script_path
		if set_script_path $PID
		   then local cmd_path=$script_path
		   else local cmd_path=$(readlink /proc/$PID/exe)
		fi

		if [[ ! $cmd_path ]]
		   then [[ $UID != 0 && -d /proc/$PID ]] &&
			   add_words trusted_PIDs $PID
			continue
		fi

		local cmd_dir=${cmd_path%/*}
		if ! is_arg1_in_arg2 $cmd_dir $trusted_dirs
		   then # since this function typically used for cmds that can
			#   retain caps, silently ignore cmds in chroot jail
			[[ $cmd_dir == /chroot/* ]] ||
			   warn "$cmd_path is untrusted"
			continue
		fi
		add_words trusted_PIDs $PID
	done

	PIDs=$trusted_PIDs

	[[ $PIDs ]]
	return $?
}

# ----------------------------------------------------------------------------

function set_PID___from_PID_file()
{
	local file=$1

	PID=

	local ignore
	[[ -s $file ]] && read PID ignore < $file && [[ $PID ]]
	return $?
}

# ----------------------------------------------------------------------------
# functions to manage capabilities
# ----------------------------------------------------------------------------

# ipchains needs net_raw
sysadmin_caps="chown dac_override dac_read_search fowner fsetid kill
	setgid setuid setpcap linux_immutable net_bind_service
	net_admin net_raw ipc_lock sys_chroot
	sys_admin sys_boot sys_nice sys_resource sys_time mknod"
# if 2.6 kernel, sys_ptrace needed to read /proc/PID/root, audit_write for sshd
[[ $is_pre_2_6_kernel ]] ||
   sysadmin_caps="$sysadmin_caps sys_ptrace audit_write"
case $HOSTNAME in
   martustst | martusp4 )
	add_words sysadmin_caps sys_ptrace ;; # for strace
esac						; readonly sysadmin_caps
min_sysadmin_caps=$sysadmin_caps		; readonly min_sysadmin_caps

[[ $UID == 0 ]] && caps_path=$(type -p caps)
# make it safe to use $caps_path no matter what
[[ ${caps_path-} ]] || caps_path=:		; readonly caps_path

# ----------------------------------------------------------------------------

function can_modify_caps
{
	[[ $caps_path ]] || return 1

	[[ -o xtrace ]] && { set +x; local xtrace="set -x"; } || local xtrace=
	$caps_path --no-messages --is-set rawio && return 0

	$caps_path --pid=$$ --no-messages --is-inheritable --is-set rawio
	local status=$?

	$xtrace
	return $status
}

# ----------------------------------------------------------------------------

# see is_first_lockdown= in /usr/local/martus/sbin/lockdown
is_martus_server()
{

	egrep --quiet ' /usr .*nosuid' /proc/mounts || return 1

	set -- /etc/{init.d,sysconfig}/{firewall,iptables,ipchains,sshd}
	set -- $(egrep --no-messages --files-with-matches Martus $*)
	[[ $# -ge 2 && -x /etc/init.d/martus && ! $(caps --list) ]] ||
	   return 1

	return 0
}

# ----------------------------------------------------------------------------

[[ $is_pre_2_6_kernel ]] && number_normal_caps=28 || number_normal_caps=30
[[ $caps_path ]] && number_normal_caps=$($caps_path --number-normal-caps)
is_martus_server && number_normal_caps=0 # now using inheritable caps only
						  readonly number_normal_caps

function is_normal_caps
{

	[[ $caps_path ]] || return 0

	set -- $($caps_path --list)
	[[ $# == $number_normal_caps ]]
	return $?
}

# ---------------------------------------

function is_locked_caps
{

	[[ $caps_path ]] || return 0

	# if no rawio, we won't be able to list caps
	set -- $($caps_path --list)
	[[ $# == 0 ]]
	return $?
}

# ----------------------------------------------------------------------------

# don't currently have a way to reduce (inheritable) caps for exec'ed child
#   without permanently reducing them for parent (since rawio has been dropped)
# BUT: setup_min_caps_for_exec can set $$ inheritable caps to min_caps (first
#   saving original inheritable caps, see earlier versions); then
#   restore_from_min_caps_for_exec can restore $$ inheritable caps
[[ ${LOGNAME-} == scott && $HOSTNAME == martusU.benetech.org && \
			$our_name == martus ]] &&
warn  "setup_min_caps_for_exec is currently a stub"
       setup_min_caps_for_exec() { return; }
restore_from_min_caps_for_exec() { return; }

# ----------------------------------------------------------------------------

# sets global variable used by clr_extra_caps_for_exec
function set_extra_caps_for_exec()
{
	[[ -o xtrace ]] && { set +x; local xtrace="set -x"; } || local xtrace=
	local caps=$*

	: ${extra_caps_for_exec=}

	# this function not needed with modern security model
	[[ $is_pre_2_6_kernel ]] || { $xtrace; return 0; }

	can_modify_caps || { $xtrace; return 1; }

	[[ ! $extra_caps_for_exec ]] ||
    error "make extra_caps_for_exec a local before: set_extra_caps_for_exec $*"

	local missing=$($caps_path --is-for-exec --pid=$$ --is-set $caps)
	extra_caps_for_exec=${missing##*:}

	if [[ $extra_caps_for_exec ]]
	   then $caps_path --is-for-exe --pid=$$ --set $extra_caps_for_exec ||
		   error "set_extra_caps_for_exec $* => $?"
		$xtrace
		return 0
	   else $xtrace
		return 1
	fi
}

# ----------------------------------------------------------------------------

function clr_extra_caps_for_exec
{

	[[ $extra_caps_for_exec ]] || return 0

	[[ -o xtrace ]] && { set +x; local xtrace="set -x"; } || local xtrace=

	$caps_path --is-inheritable --pid=$$ --clear $extra_caps_for_exec ||
	   error "clr_extra_caps_for_exec $extra_caps_for_exec => $?"

	extra_caps_for_exec=
	$xtrace
	return 0
}

# ----------------------------------------------------------------------------
# functions dealing with filing systems read-only/read-write mount status
# ----------------------------------------------------------------------------

proc_mounts=/proc/mounts				; readonly proc_mounts

function set_mount_point()
{
	local path=${1-}

	path=${path#./}
	[[ $path == ?*/ ]] && path=${path%/}
	[[ $path == /* ]] || path=$PWD/$path
	[[ $path == .  ]] && path=$PWD

	[[ $path == */./* || $path == */../* ]] &&
	   error "set_mount_point $*: can't have /./ or /../ in path"

	mount_point=$path

	while [[ $mount_point == *[^-._a-zA-Z0-9/]* ]]
	    do	mount_point=${mount_point%/*}
	done

	until egrep --silent "^[^[:space:]]+ $mount_point "  $proc_mounts
	    do	mount_point=${mount_point%/*}
		[[ $mount_point ]] || mount_point=/
		# test late: it's OK if no $path, but its parent must exist
		[[ -e $mount_point ]] || return 1
	done

	return 0
}

# ---------------------------------

function is_mounted_rw()
{
	local path=$1

	set_mount_point $path || return 1

	# ignore rootfs record, it doesn't reflect current mount status of /
	egrep --silent "^[^r][^[:space:]]+ $mount_point [^ ]+ rw" $proc_mounts
	return $?
}

# ---------------------------------

function is_mounted_ro()
{
	local path=$1

	set_mount_point $path || return 1

	# ignore rootfs record, it doesn't reflect current mount status of /
	egrep --silent "^[^r][^[:space:]]+ $mount_point [^ ]+ ro" $proc_mounts
	return $?
}

# ------------------------------------------------------------

# with 2.6 kernel: Xen dom0 owns/manages most filesystems containg code (i.e.
#   / /boot /usr /chroot /chroot/jvm /chroot/jvm_mspa), sysadmin manages rest
# MSPAServer has to write /chroot/etc
if [[ $is_pre_2_6_kernel ]]
   then _extra_RO_="/proc/bus/usb /proc /boot" # Xen guest has no /boot FS
   else _extra_RO_="/proc/sys/fs/binfmt_misc" # new to 2.6; but ...
	_extra_RO_=			# ... we let sysadmin manage /proc
fi
FS_not_RW_when_locked=" / /usr  /usrlocal
			/chroot /chroot/jvm /chroot/jvm_mspa $_extra_RO_"
unset _extra_RO_			; readonly FS_not_RW_when_locked

function are_critical_FS_mounted_rw
{

	[[ $HOSTNAME == *.benetech.org ]] &&
	   warn "$FUNCNAME $*: should probably use are_critical_FS_mounted_ro"

	local FS
	for   FS in $FS_not_RW_when_locked
	   do	case $FS in
		  /chroot | /chroot/jvm | /chroot/jvm_mspa ) # always read-only
			continue
		esac
		is_mounted_ro $FS && return 1
	done

	return 0
}

# --------------------------------------------

function are_critical_FS_mounted_ro
{

	local FS
	for   FS in $FS_not_RW_when_locked
	   do	is_mounted_rw $FS && return 1
	done

	return 0
}

# ----------------------------------------------------------------------------

function set_PIDs___from_lsof_write
{
	local file=$1

	PIDs=$(sed -n 's/.* \([0-9][0-9]*\) .*[0-9][wu]  *REG .*/\1/p' $file |
	       sort -u)

	[[ $PIDs ]]
	return $?
}

# --------------------

function set_PIDs___from_lsof_TYPE
{
	local file=$1 type=$2

	PIDs=$(sed -n "s/.* \([0-9][0-9]*\) .* $type .*/\1/p" $file | sort -u)

	[[ $PIDs ]]
	return $?
}

# --------------------

function set_PIDs___no_family
{
	PIDs=

	local PID
	for   PID in $*
	    do	[[ $PID == $$ || $PID == $PPID ]] && continue
		add_words PIDs $PID
	done

	[[ $PIDs ]]
	return $?
}

# --------------------

function set_PIDs___no_self
{
	PIDs=

	local PID
	for   PID in $*
	    do	[[ $PID == $$ ]] && continue
		add_words PIDs $PID
	done

	[[ $PIDs ]]
	return $?
}

# ------------------------------------------

function set_PIDs___from_lsof_output_file
{
	              local do_include_parent=$false
	[[ $1 == -p ]] && { do_include_parent=$true; shift; }
	local uncut_file=${1-$lsof_output_file}

	local file=${cut_lsof_output_file-}
	[[ $file ]] || error "setup cut_lsof_output_file"

	cut -c1-36 $uncut_file > $file || return 1

	set_PIDs___from_lsof_write $file &&
	   return 0

	set_PIDs___from_lsof_TYPE  $file REG && set_PIDs___no_family $PIDs &&
	   return 0

	[[ $do_include_parent ]] &&
	set_PIDs___from_lsof_TYPE  $file REG && set_PIDs___no_self   $PIDs &&
	   return 0

	set_PIDs___from_lsof_TYPE  $file DIR && set_PIDs___no_family $PIDs &&
	   return 0

	[[ $do_include_parent ]] &&
	set_PIDs___from_lsof_TYPE  $file DIR && set_PIDs___no_self   $PIDs &&
	   return 0

	return 1
}

# ----------------------------------------------------------------------------

function _is_time_for_remount_kill
{
	local mount_point=$1
	[[ ${loops_remaining_before_kill-} ]] ||
	   error "need to setup loops_remaining_before_kill as integer"

	if (( --loops_remaining_before_kill > 0 ))
	   then usleep 300123
		echo -e ".\c" > /dev/tty
		return 1
	fi

	[[ $loops_remaining_before_kill != 0 ]] && return 0

	# remember, our output is being appended to a log

	local msg="Killing some processes to enable security lock"
	echo -e "\n\t$msg \c" > /dev/tty

	[[ $our_name == martus ]] || return 0

	log "PID=$$ remount_with_retry kill"
	lsof $mount_point

	return 0
}
		
# ----------------------------------------------------------------------------

function _mount_loop_with_kill()
{
	local mount_args=$*

	local     lsof_output_file=$tmpdir/lsof.1.$$
	local cut_lsof_output_file=$tmpdir/lsof.2.$$

	local msg="Retrying security lock"
	echo -e "\n\t$msg \c" > /dev/tty

	local status=0

	declare -i loops_remaining_before_kill=10 loops_remaining_for_kill=20 

	until mount $mount_args
	   do	_is_time_for_remount_kill $mount_point || continue

		killall -q -9 fixtime rdate-a chattrvar

		lsof $mount_point > $lsof_output_file
		cat $lsof_output_file

		# this routine not called for /, so don't worry about daemons
		if set_PIDs___from_lsof_output_file -p $lsof_output_file
		   then echo "kill $PIDs"
			      kill $PIDs
		fi
		echo -e ".\c" > /dev/tty
		sleep 2

		(( --loops_remaining_for_kill )) && continue

		status=1
		break
	done

	echo -e "\n" > /dev/tty

	return $status
}

# ----------------------------------------------------------------------------

# need dac_override to delete mount's /etc/mtab~ lock file
mount_caps="dac_override sys_admin"			; readonly mount_caps

# if can't remount ro, keep killing processes until mount succeeds
# this is needed because kernel *sometimes* can't remount /usrlocal ro
#    due to perl opening the following odd file when running martusadmin:
#    REG    9,4 55630 47079 /usrlocal/martus/bin/.martusadmin2.6iCl9e (deleted)
# caller must have raised 'kill' process cap
function remount_with_retry()
{
	local log; [[ $1 == -l ]] && { log=$2; shift 2; } || log=/dev/null
	local option=$1 path=$2 bad=${3-}
	[[ $path && ! $bad ]] || error "Usage: $FUNCNAME [-l log] option path"

	[[ $UID == 0 ]] || return 1

	local mount_point
	set_mount_point $path ||
	   warn "remount_with_retry: $path doesn't exist" || return 1

	setup_min_caps_for_exec $mount_caps

	# /usr/bin/passwd is setuid, but it's been moved to /bin (on /)
	[[ $mount_point != / ]] && is_martus_server && option=$option,nosuid
	option=$option,remount
	local mount_args="-o $option $mount_point"

	mount $mount_args
	local status=$?

	# can't kill processes with open files on /, too many system processes
	# ditto with /usr; this usually happens after a software install
	if [[ $status == 0 || $option == rw* ||
	      $mount_point == / || $mount_point == /usr ]]
	   then restore_from_min_caps_for_exec
		[[ $status == 0 || $option == rw* ]] ||
		   echo "Can't remount $mount_point, need to kill processes else reboot." >&2
		return $status
	fi

	_mount_loop_with_kill $mount_args >> $log 2>&1
	status=$?

	restore_from_min_caps_for_exec

	return $status
}

# ----------------------------------------------------------------------------
# functions to see if system is fully locked, or is fully unlocked
# NOTE: both these functions will be false if the system is half-locked
# ----------------------------------------------------------------------------

function is_locked
{

	are_critical_FS_mounted_ro && is_locked_caps
	return $?
}

# ---------------------------------

function is_unlocked
{

	is_locked && return 1 || return 0

	# doesn't work well with Xen guest, OS FSs always RO
	are_critical_FS_mounted_rw && is_normal_caps
	return $?
}

# ----------------------------------------------------------------------------
# miscellaneous functions
# ----------------------------------------------------------------------------

# if don't want the key (from agent) of the sysadmin who's running this command
use_root_SSH_key()
{
	unset SSH_AGENT_PID SSH_AUTH_SOCK
	export HOME=~root
}

# ----------------------------------------------------------------------------

# for each file in $1 that looks like foo.martus, append it to file foo
# TODO: add an option to delete the .martus file if append is successful
append_dot_martus_files() {
	local root_dir=$1 is_upgrade=${2-}

	local cwd=$PWD
	local append_file
	cd_ $root_dir &&
	find . -name '*.martus' |
	while read append_file
	   do	append_file=${append_file#./}
		 local file=${append_file%.martus}
		[[    -f $file ]] || warn "creating missing $root_dir/$file"
		if [[ -f $file && $is_upgrade ]]
		   then :		# handled specially below
		   else write_file_from_stdin -a $file < $append_file
			continue
		fi

		local record
		egrep '^[a-zA-Z0-9]' $append_file |
		while read record
		   do	local first_word=$(echo "$record" | awk '{print $1'})
			egrep --silent "^$first_word\b" $file && continue
			echo "$record" | write_file_from_stdin -a $file
		done
	done
	cd_ $cwd
}

true
