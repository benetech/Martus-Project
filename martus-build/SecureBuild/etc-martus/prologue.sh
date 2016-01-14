#!/bin/bash

# The Martus(tm) free, social justice documentation and
# monitoring software. Copyright (C) 2002-2014, Beneficent
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

#############################################################################
#############################################################################
### this file creates a standard environment and basic shell functions
###
### source this file at the beginning of all bash scripts
### also, can _run_ this file from a shell to create secure $tmpdir (see below)
#############################################################################
#############################################################################

##############################################################################
# setup the environment
# errors in this sections are problems with prologue.sh, not calling script
##############################################################################

# to announce errors in this script (these functions will be replaced later)
 warn() { echo -e "\n$0: source /etc/*/prologue.sh: $*\n" >&2; return 1; }
error() { warn "$*"; exit 1; }

# ----------------------------------------------------------------------------
# make sure the shell has needed features
# errors here are problems/limitations with interpreter
# ----------------------------------------------------------------------------

[ "${RANDOM-}" != "${RANDOM-}" -o "${RANDOM-}" != "${RANDOM-}" ] ||
   error "need bash or ksh"

[[ 1 == 1 ]] || error "need bash-2.02 or later, or ksh-88 or later"

# ----------------------------------------------------------------------------
# provide a directory for temporary files that's safe from symlink attacks
# ----------------------------------------------------------------------------

# daemons like RootHelper might not have a PATH ... ENV.sh will fix it up later
if [[ ${PATH-} == */[ub]* ]]
   then  PATH=$PATH:/usr/bin:/bin
   else export PATH=/usr/bin:/bin
fi

tmpdir=${tmpdir:-/tmp/$(id -nu)}	# caller is allowed to change tmpdir
[[ -w ${TMP-}    ]] && tmpdir=$TMP
[[ -w ${TMPDIR-} ]] && tmpdir=$TMPDIR

# the root filesystem is read-only while booting, don't get into infinite loop!
# GNU mkdir will fail if $tmpdir is a symlink
until [[ ! -w /tmp ]] || mkdir -m 0700 -p $tmpdir
   do	warn "deleting $(ls -ld $tmpdir)"
	rm -f $tmpdir
done

export TMP=$tmpdir TMPDIR=$tmpdir	# caller can change these

umask 022				# caller can change it

# ----------------------------------------------------------------------------
# setup global variables for calling script
# ----------------------------------------------------------------------------

type -t readonly > /dev/null 2>&1 || readonly() { true; }

our_path=${0#-}
[[ $our_path == */* ]] || our_path=$(type -p $our_path)
[[ $our_path == ./* ]] && our_path=${0#./}
[[ $our_path ==  /* ]] || our_path=$PWD/$our_path	; readonly our_path

# we might have been run as a script to create $tmpdir (see above)
[[ $our_path == /etc/*/prologue.sh ]] && exit 0

# basename of calling script; if already non-blank, we don't change it
our_name=${our_name:-${0##*/}}		# user can change

# legacy names
Cmdpath=${Cmdpath:-$our_path}			; readonly Cmdpath
Cmdname=${Cmdname:-$our_name}		# user can change

# put $Run in front of key commands, so -d means: debug only, don't do anything
: ${Run=}

true=t True=t					; readonly true  True
false= False=					; readonly false False

_chr_='[a-zA-Z0-9]'
rsync_temp_file_suffix="$_chr_$_chr_$_chr_$_chr_$_chr_$_chr_"; unset _chr_
					  readonly rsync_temp_file_suffix

# ----------------------------------------------------------------------------
# identify the application sourcing us, and setup its environment variables
# ----------------------------------------------------------------------------

_did_source_prologue_sh=$true	      # so don't recursively source ENV.sh/etc

if [[ -s /etc/redhat-release ]]
   then is_redhat=$true
   else is_redhat=$false
fi						; readonly is_redhat

if { ps -C X || ps -C Xorg; } &> /dev/null
   then is_workstation=$true
   else is_workstation=$false
fi						; readonly is_workstation

if [[ ! $is_workstation &&
      ( -e /etc/martus/firewall.sh ||
	-e /etc/martus/firewall.sh.sample ) ]]
   then is_martus_appliance=$true
   else is_martus_appliance=$false
fi						; readonly is_martus_appliance

if grep --silent ' / .*\bro\b' /proc/mounts
   then is_root_RO=$true
   else is_root_RO=$false
fi						; readonly is_root_RO

set__our_prologue_()
{
	set -- /etc/*/prologue.sh
	while true
	    do	_our_prologue_=$1; shift
		local _dir=${_our_prologue_%/*}
		[[ $# != 0 && -L $_dir  ]] && continue
		[[ -s  $_dir/libpath.sh ]] || continue
		source $_dir/libpath.sh || error "couldn't source libpath.sh"
		[[ -s  $_dir/ENV.sh ]] ||continue; [[ $our_name == ENV.sh ]] ||
		source $_dir/ENV.sh    || error "couldn't source ENV.sh"
		return
	done
}
set__our_prologue_
#
_our_app_=${_our_prologue_%/*}
_our_app_=${_our_app_##*/}			; readonly _our_app_
# app-specific lib is optional; and it can't be sourced until end of this file
__lib__=${_dir_:-/etc/$_our_app_}/lib$_our_app_.sh
[[ -s $__lib__ ]] || __lib__=
unset _dir_

_chown_app_owner_() {

	local _owner=$_our_app_
	[[ $_our_app_ == martus ]] && _owner=sysadmin
	chgrp $_owner "$@"
}

# ----------------------------------------------------------------------------
# setup global flags that describe run-time environment
# ----------------------------------------------------------------------------

is_pre_3_1_bash=$false is_pre_3_0_bash=$false
case $BASH_VERSION in
       3.0* )	is_pre_3_1_bash=$true ;;
   [0-2].*  )	is_pre_3_1_bash=$true is_pre_3_0_bash=$true ;;
esac; readonly	is_pre_3_1_bash       is_pre_3_0_bash

kernel_release=$(uname -r)		; readonly kernel_release
is_pre_2_6_kernel=$false is_pre_2_4_kernel=$false 
case $kernel_release in
   2.[4-5].* ) is_pre_2_6_kernel=$true ;;
   2.[0-3].* ) is_pre_2_6_kernel=$true is_pre_2_4_kernel=$true ;;
esac; readonly is_pre_2_6_kernel       is_pre_2_4_kernel

case $(uname -r) in
   *[0-9].ELxen*   ) declare -i RHEL_version=4 ;;
   *[0-9].el[1-9]* ) declare -i RHEL_version=$(uname -r |
					sed 's/.*[0-9]\.el\([0-9]*\).*/\1/') ;;
   *		   ) declare -i RHEL_version=0 ;;
esac					; readonly RHEL_version

[[ $(uname -m) == x86_64 ]] && is_64b=$true || is_64b=$false
					  readonly is_64b
is_SELinux_enabled=$false
if [[ ! -d /selinux/booleans ]]
   then is_SELinux=$false
   else is_SELinux=$true
	[[ $(getenforce 2>/dev/null) == Enforcing ]] &&
	   is_SELinux_enabled=$true
fi					;readonly is_SELinux is_SELinux_enabled

set_security_context() {
	local file=$1
	set -f
	set -- $(ls --scontext $file)
	set +f
	security_context=${1##*:}
}

# don't trash parent script's positional parameters (if old shell) with set --
_setup_xen_flags() {

	[[ -d /proc/xen ]] && is_xen=$true || is_xen=$false   ; readonly is_xen
	[[ $kernel_release == *xen && ! $is_xen ]] &&
	   warn "_setup_xen_flags in prologue.sh seems broken"

	# lockdown assumes that chroot w/host's /proc will be considered a domU
	set -- /proc/xen/xsd_*
	if [[ ! $is_xen ]]
	   then is_xen_domU=$false is_xen_dom0=$false
	elif [[ -e $1 ]]
	   then is_xen_domU=$false is_xen_dom0=$true
	   else is_xen_domU=$true  is_xen_dom0=$false
	fi				; readonly is_xen_domU is_xen_dom0
}
_setup_xen_flags
unset -f _setup_xen_flags

if [[ -s /boot/grub/grub.conf && $is_xen_domU ]]
   then is_cloud_guest=$true
   else is_cloud_guest=$false
fi					; readonly is_cloud_guest

_setup_MB_flags() {
	set -- $(grep '^MemTotal:' /proc/meminfo 2>/dev/null)
	[[ ${3-} == kB ]] || return
	let kernel_MB=$2/1024
	if [[ $is_xen_dom0 ]]
	   then [[ $UID == 0 ]] && /usr/sbin/xend status &> /dev/null || return
		set -- $(xm info | grep '^total_memory\b')
		hardware_MB=$3
	elif [[ ! $is_xen_domU ]]
	   then hardware_MB=$kernel_MB	# but grub.cfg could set kernel_MB low
	fi
}
_setup_MB_flags
unset -f _setup_MB_flags

##############################################################################
##############################################################################
## NOTE: functions return "exit status" (i.e. 0 for success, non-0 otherwise);
##     the exit status is stored in $?, or operated on by: if while || && ....
##
## there are three kinds of syntax for routines:
##    function func()	# returns status, takes arguments
##    function func	# returns status, doesn't take arguments
##    procedure()	# doesn't return status (exits on fatal error)
##
## there are two kinds of routines that set global variables:
##    set_foo		# set variable foo (from global knowledge)
##    set_foo___from_xx	# set variable foo ... using method xx on args
##############################################################################
##############################################################################

##############################################################################
# shell functions for use by calling script
##############################################################################

# use to indicate that a command is not yet ready to run
notyet() { warn "'$*' not yet available, ignoring"; }

# ----------------------------------------------------------------------------
# functions to check for needed utilities
# ----------------------------------------------------------------------------

# return true if have any of the passed commands, else silently return false
function have_cmd() {
	local _cmd
	for _cmd
	   do	type -t $_cmd && return 0
	done &> /dev/null
	return 1
}
function haveCmd() { have_cmd "$@"; }	# legacy name

# exit noisily if missing (e.g. not in PATH) any of the $* commands
need_cmds() {

	[[ -o xtrace ]] && { set +x; local xtrace="set -x"; } || local xtrace=

	local _cmd is_cmd_missing=
	for _cmd
	    do	haveCmd $_cmd && continue

		echo "$our_name: command '$_cmd' is not in current path."
		is_cmd_missing=1
	done

	[[ $is_cmd_missing ]] && exit 2
	$xtrace
}
needCmds() { need_cmds "$@"; }		# legacy name

# ----------------------------------------------------------------------------
# simple error and warning functions
# ----------------------------------------------------------------------------

log_date_format_opt="+%a %m/%d %H:%M:%S" # caller can change

: ${warn_error_log_file=}	       # if set, warn and error will log to it

_log_warning_error() {
	local _msg=$*

	[[ $warn_error_log_file ]] || return

	local log_date
	if have_cmd set_log_date      # may not have sourced this function yet
	   then set_log_date
	   else log_date=$(date "$log_date_format_opt")
	fi

	echo -e "$log_date $our_name: $_msg" >> $warn_error_log_file
}

# -------------------------------------------------------

# simple-minded error routine for caller's script; can be redefined by caller
error() {
	[[ -o xtrace ]] && { set +x; local xtrace="set -x"; } || local xtrace=
	local _msg="\n$our_name: $*\n"
	if [[ ${1-} == -t ]]
	   then shift
		_msg=${_msg/: -t/:}
		[[ -t 2 || $Run ]] || echo -e "$_msg" 2> /dev/null > /dev/tty
	fi
	if [[ ${Usage-} && "$*" == "${Usage-}" ]]
	   then echo -e "$*" >&2
	   else echo -e "$_msg" >&2
		_log_warning_error "$*"
	fi
	$xtrace

	# bash exit can fail if have trap handler, so have to delete it ...
	trap EXIT
	# ... user can provide function that contains their trap-handler code
	${callback_from_error-}

	exit 1
}

# ----------------------------------------------------------------------------

did_warn=
# sample 'warn' usage: cmd || warn "cmd failed, ignoring" || continue
function warn() {
	[[ -o xtrace ]] && { set +x; local xtrace="set -x"; } || local xtrace=
	local _msg="\n$our_name: $*\n"
	if [[ ${1-} == -t ]]
	   then shift
		_msg=${_msg/: -t/:}
		[[ -t 2 || $Run ]] || echo -e "$_msg" 2> /dev/null > /dev/tty
	fi
	echo -e "$_msg" >&2
	_log_warning_error "$*"
	$xtrace
	did_warn=$true
	return 1
}

# ----------------------------------------------------------------------------

print_or_egrep_Usage_then_exit() {
	[[ ${1-} == -[hHk] ]] && shift	# strip help or keyword-search option
	[[ $# == 0 ]] && echo -e "$Usage" && exit 0
	echo "$Usage" | grep -i "$@"
	exit 0
}

# ----------------------------------------------------------------------------
# generic logging functions (also see _log_warning_error, earlier)
# ----------------------------------------------------------------------------

syslog_timestamp_format_opt="+%b %e %X"	# caller can change

set_syslog_timestamp()
{

	syslog_timestamp=$(date "$syslog_timestamp_format_opt")
}

# ----------------------------------------------------------------------------

# set to $true to get predictable log dates for regression/acceptance tests
# set to 'increment' to have the predictable dates change on each call
do_log_fake_timestamps=$false

set_log_date() {

	[[ -o xtrace ]] && { set +x; local xtrace="set -x"; } || local xtrace=
	local _format=$log_date_format_opt

	if [[ $do_log_fake_timestamps ]]
	   then if [[ $do_log_fake_timestamps == increment ]]
		   then let _fake_log_seconds_=${_fake_log_seconds_-0}+1
			local seconds=$_fake_log_seconds_
		   else local seconds=${_fake_log_seconds_-1}
		fi
		log_date=$(date -d "1970-01-01 $seconds seconds" "$_format")
	   else log_date=$(date "$_format")
	fi
	$xtrace
}
set_log_timestamp() { set_log_date "$@"; } # legacy name

# ----------------------------------------------------------------------------

prepend_log_date() {

	local _log_date
	set_log_date
	[[ ${1-} == -n ]] && local name="$our_name: " || local name=
	sed "s@^@$_log_date $name@"
}

# ----------------------------------------------------------------------------

set__nologging_log_path_() {

	[[ ${_nologging_log_path_-} ]] && return

	local _log_base=/var/local/$_our_app_/log/$our_name

	local DoW_nr=$(date '+%w')
	[[ $DoW_nr == 0 ]] && DoW_nr=7

	_nologging_log_path_=$_log_base.$DoW_nr.$(date '+%a')
}

# ----------------------------------------------------------------------------

# echo timestamped message, unless $is_logging_fully_disabled; or echo to
# $_nologging_log_path_ only ($is_no_stdout_logging) or also ($is_full_logging)
log() {
	[[ -o xtrace ]] && { set +x; local xtrace="set -x"; } || local xtrace=
	local _msg="$*"

	[[ ${is_logging_fully_disabled-} ]] && return

	set_log_date
	local header="$log_date $our_name:"

	# is_nologging is legacy name
	[[ ${is_nologging-} ]] && is_no_stdout_logging=$true

	   #    is_no_stdout_logging has higher preference
	   [[ ${is_no_stdout_logging-} ]] && is_full_logging=$false

	if [[ ${is_no_stdout_logging-} ||  ${is_full_logging-} ]]
	   then local       is_not_first_log=${_nologging_log_path_-}
		[[ $is_not_first_log ]] || set__nologging_log_path_
		local log=$_nologging_log_path_

		local dir
		set_dir "$log"
		local log_dir=$dir
		set_dir "$log_dir"
		local log_parent_dir=$dir

		if [[ ! -d $log_dir && -w $log_parent_dir ]]
		   then mkdir $log_dir
			_chown_app_owner_   $log_dir
			chmod u+rw,go-w,o-r $log_dir
			ls -ld $log_dir
		fi
		if [[ ! -w $log && ! -w $log_dir ]]
		   then [[ $is_not_first_log ]] || warn "can't write to $log"
			echo -e "$header $_msg"
			$xtrace
			return
		fi

		if [[ ! $is_not_first_log && -f $log &&
		      "$(find $log -mtime +3)" ]] # old log (week timescale)?
		   then [[ -w $log ]] && > $log || rm -f $log
		fi

		header="${header% $our_name:}" # filename contains $our_name
		echo -e "$header $_msg" >> $log; [[ ${is_full_logging-} ]] &&
		echo -e "$header $_msg"
	   else echo -e "$header $_msg"
	fi

	$xtrace
}

# ----------------------------------------------------------------------------

# show head-style header
header() {
	[[ -o xtrace ]] && { set +x; local xtrace="set -x"; } || local xtrace=
	echo -e "\n==> $* <=="
	$xtrace
}

# ----------------------------------------------------------------------------
# miscellaneous functions
# ----------------------------------------------------------------------------

set_absolute_dir() {
	[[ -o xtrace ]] && { set +x; local xtrace="set -x"; } || local xtrace=
	[[ $# == 1 ]] || error "Usage: $FUNCNAME filename" || return 1
	local name=$1

	[[ -d "$name" ]] || name=$(dirname "$name")
	absolute_dir=$(cd "$name" && /bin/pwd)
	$xtrace
}

# -------------------------------------------------------

set_absolute_path() {
	[[ -o xtrace ]] && { set +x; local xtrace="set -x"; } || local xtrace=
	[[ $# == 1 ]] || error "Usage: $FUNCNAME filename" || return 1
	local name=$1

	local absolute_dir
	set_absolute_dir "$name"
	if [[ -d "$name" ]]
	   then absolute_path=$absolute_dir
	   else absolute_path=$absolute_dir/$(basename "$name")
	fi
	$xtrace
}

# ----------------------------------------------------------------------------

# can create a configuration file (or append with -a) using here-document;
#  if $Run is non-blank, will show the result (if -a, 'diff' against real file)
write_file_from_stdin() {
	[[ -o xtrace ]] && { set +x; local xtrace="set -x"; } || local xtrace=
	local do_append=$false
	[[ $1 == -a ]] && { do_append=$true; shift; }
	local file=$1

	if [[ $Run ]]
	   then local original_file=$file absolute_path
		set_absolute_path $original_file
		file=$tmpdir$absolute_path
		mkdir --parent $(dirname $file)
		if [[ $do_append ]]
		   then if [[ -f $original_file ]]
			   then cp $original_file $file ||
				   error "write_file_from_stdin: Run error"
			   else > $file	# in case previous debug run
				original_file=
			fi
		fi
	fi

	if [[ $do_append ]]
	   then cat >> $file
	   else cat >  $file
	fi || error "write_file_from_stdin: failed to write $file"

	if [[ $Run ]]
	   then if [[ $original_file && -f $original_file ]]
		   then diff --unified $original_file $file
		   else head --verbose --lines=50 $file
		fi
	fi
	$xtrace
}

# ----------------------------------------------------------------------------

cd_() {
	[[ -o xtrace ]] && { set +x; local xtrace="set -x"; } || local xtrace=
	(( $# <= 1 )) || error "wrong number args: cd_ $*"
	local _dir=${1-$HOME}

	cd "$_dir" || error "cd $_dir"
	# -n and -z needed here for buggy 2.04 version of bash (in RHL 7.1)
	if [[ ( -n $Run || -n ${do_show_cd-} ) && -z ${Trace-} ]]
	   then local _msg="cd $PWD"
		[[ $_dir == */.* && $_dir != /* ]] && _msg="$_msg # $_dir"
		echo "$_msg"
	fi
	$xtrace
	return 0
}

# ----------------------------------------------------------------------------

set_FS_inodes_used() {
	local _dir=$1

	# -A 1: multi-line records for long dev names (like Logical Volumes)
	set -- $(df --inodes --no-sync $_dir/. | grep -A 1 "^/")
	FS_inodes_used=$3
}

# ----------------------------------------------------------------------------

set_FS_KB_used() {
	local _dir=$1

	# -A 1: multi-line records for long dev names (like Logical Volumes)
	set -- $(df -k --no-sync $_dir/. | grep -A 1 "^/")
	FS_KB_used=$3
}

# ----------------------------------------------------------------------------

# sometimes test reports false when process _is_ alive, so try a few times
function is_process_alive() {

	local PID
	for PID
	    do	local did_find_process=$false
		local try
		for try in 1 2 3 4 5
		    do	if kill -0 $PID
			   then did_find_process=$true
				break
			   else [[ $UID == 0 ]] || # 'kill' works if we're root
				case $(kill -0 $PID 2>&1) in
				   ( *"Operation not permitted"* )
					did_find_process=$true
					break ;;
				   ( *"No such process"* )
					;;
				   ( * )
					if [[ -d /proc/$PID ]]
					   then did_find_process=$true
						break
					fi ;;
				esac
			fi &> /dev/null
			[[ $is_xen_domU ]] && break # domU can't do usleep
			usleep 123123
		done
		[[ $did_find_process ]] || return 1
	done
	return 0
}

# ----------------------------------------------------------------------------

function is_a_tty {
	[[ -t 0 || -t 1 || -t 2 ]]
	return $?
}

# ----------------------------------------------------------------------------

# in variable named $1, append the subsequent args (with white space)
function add_words() {
	[[ -o xtrace ]] && { set +x; local xtrace="set -x"; } || local xtrace=
	local variable_name=$1; shift

	[[ $# == 0 ]] && $xtrace && return 0 # maybe no words to add

	# it's too hard to detect "unbound variable" in this case
	local unbound_variable_msg="
	  $FUNCNAME $variable_name $*: $variable_name is unset"
	local value=${!variable_name?"$unbound_variable_msg"}

	if [[ $value ]]
	   then eval "$variable_name=\"\$value \$*\""
	   else eval "$variable_name=\$*"
	fi

	$xtrace
	return 0
}

# ----------------------------------------------------------------------------

function confirm() {
	[[ -o xtrace ]] && { set +x; local xtrace="set -x"; } || local xtrace=
	[[ $1 == -n  ]] && { echo; shift; }
	local _prompt=$1 default=${2-}

	local y_n status
	case $default in
	   [yY]* ) y_n="Y/n" status=0 ;;
	   [nN]* ) y_n="y/N" status=1 ;;
	   *     ) y_n="y/n" status=  ;;
	esac

	add_words _prompt "($y_n)? "

	local key
	while read -n 1 -p "$_prompt" key
	   do	$xtrace
		case $key in
		   [yY]* ) status=0 && break ;;
		   [nN]* ) status=1 && break ;;
		   *     ) [[ $status ]] && return $status ;;
		esac
		set +x
	done
	echo

	[[ $status ]] || error "confirm $*: read failure"
	$xtrace
	return $status
}

# ----------------------------------------------------------------------------

# does 1st argument match any of the space-separated words in rest of arguments
function is_arg1_in_arg2() {
	[[ -o xtrace ]] && { set +x; local xtrace="set -x"; } || local xtrace=
	local arg1=$1; shift; local arg2=$*
	[[ $arg1 && $arg2 ]] || return 1

	[[ " $arg2 " == *" $arg1 "* ]]
	local status=$?
	$xtrace
	return $status
}

# ----------------------------------------------------------------------------

# globbed pattern can only contain subsequent args
assert_glob() {
	[[ -o xtrace ]] && { set +x; local xtrace="set -x"; } || local xtrace=
	local check_basename_only=$false
	[[ ${1-} == -b ]] && { local check_basename_only=$true; shift; }
	local pattern=$1; shift; local allowed=$*

	set -- $pattern
	[[ "$*" == "$pattern" ]] && { $xtrace; return; } # no matches is OK

	local path
	for path
	    do	[[ $check_basename_only ]] && path=${path##*/}
		is_arg1_in_arg2 "$path" "$allowed" && continue
		error "pattern '$pattern' globbed to '$path' in $PWD"
	done
	$xtrace
}

# ----------------------------------------------------------------------------

set_dir() {
	local path=$1

	if [[ $path == */* ]]
	   then dir=${path%/*}
		[[ $dir ]] || dir=/
	   else dir=.
	fi
}

# ----------------------

set_basename() {
	local path=$1

	basename=${path##*/}
}

# ----------------------------------------------------------------------------

assert_accessible() {
	[[ -o xtrace ]] && { set +x; local xtrace="set -x"; } || local xtrace=
	local tests=
	while [[ $1 == -* ]] ; do tests="$tests $1"; shift; done

	local file
	for file
	   do	[[ -e $file ]] || error "'$file' doesn't exist"

		local test
		for test in $tests
		    do	eval "[[ $test '$file' ]]" ||
			   error "'$file' fails test $test"
		done
	done
	$xtrace
}

# -------------------

function assert_readable()       { assert_accessible -r "$@"; }
function assert_writable()       { assert_accessible -w "$@"; }
function assert_executable()     { assert_accessible -x "$@"; }

function assert_writable_dirs()  { assert_writable -d -x "$@"; }
function assert_writable_files() { assert_writable -f    "$@"; }

# ----------------------------------------------------------------------------

# file $1 is modified in-place (with optional backup) by subsequent command
modify_file() {
	[[ -o xtrace ]] && { set +x; local xtrace="set -x"; } || local xtrace=
	local backup_ext=
	[[ $1 == -b* ]] && { backup_ext=$1; shift; }
	[[ $# -ge 2 ]] || error "Usage: modify_file [-b[ext]] file command"
	local file=$1; shift

	local dir
	set_dir "$file"

	assert_writable_files "$file"
	assert_writable_dirs  "$dir"

	if [[ $backup_ext ]]
	   then backup_ext=${backup_ext#-b}
		local backup=$file${backup_ext:-'~'}
		ln -f "$file" "$backup" || error "can't backup '$file'"
	fi

	# we use cp -p just to copy the file metadata (uid, gid, mode)
	cp -p "$file"   "$file+" &&
	 "$@" "$file" > "$file+" &&
	  mv  "$file+"  "$file"  ||
	   error "modify_file $file $* => $?"
	$xtrace
}

# ----------------------------------------------------------------------------

function set_martus_version
{
	local is_previous=${1-}

	local  _version_file=/usr/local/martus/etc/tarball-version.txt
	[[ $is_previous ]] && set -- $_version_file*~ && _version_file=${!#}
	[[ -s $_version_file ]] &&
	martus_version=$(sed "s@.*/martus-server-\([.0-9]*\)\.[a-zA-Z].*@\1@" \
				$_version_file) || martus_version=
	[[ $martus_version ]]
}

# ----------------------------------------------------------------------------

backup_files_before_upgrade() {
	[[ $1 == -p ]] && { local is_previous=$1; shift; } ||local is_previous=

	local martus_version file
	  set_martus_version $is_previous
	for file
	    do	local  orig_content_file=$file 
		[[ -e $orig_content_file ]] || orig_content_file=/dev/null
		if [[ $martus_version ]]
		   then local backup_file=$file-$martus_version
			[[ -e $backup_file ]] ||
			   $Run cp -p $orig_content_file $backup_file
		   else [[ $(find $file~ -mtime -7 2>/dev/null) ]] ||
			   $Run cp -p $orig_content_file $file~
		fi
	done
}

# ----------------------------------------------------------------------------

function is_immutable() {
	[[ $(lsattr -d $1) == *i*" "* ]]
	return $?
}

# ------------------------------------------------------------------

function is_append_only() {
	[[ $(lsattr -d $1) == *a*" "* ]]
	return $?
}

# ------------------------------------------------------------------

function roll_log() {
	local args=$*

	if [[ ${1-} == -r ]]
	   then shift
		local is_world_readable=$true
	   else local is_world_readable=$false
	fi
	if [[ ${1-} == -h ]]
	   then shift
		local is_hourly=$true
	   else local is_hourly=$false
	fi
	local log=$1
	[[ $log == *.bz2 || $log == *.gz ]] && error "bad: roll_log $args"

	[[ -s $log ]] || return 1

	local rolled=$log.$(date '+%y%m%d')
	set -- $log.[0-9][0-9][0-9][0-9][0-9][0-9]
	if [[ $is_hourly ]]
	   then rolled=${rolled}_$(date '+%H')
		set -- ${1}_[0-9][0-9]
	fi
	[[ -e $1 ]] && local old_rolled=$* || local old_rolled=

	[[ -s $rolled.bz2 || -s $rolled ]] && return 1

	# watch out for symlink attacks
	local file
	for   file in $log+ $rolled $rolled.bz2
	    do	[[ -L $file ]] || continue
		warn "possible symlink attack from cracker:\n  $(ls -ld $file)"
		return 1
	done

	$Run touch $log+ && $Run chattr -ai $log+ &&
	$Run chmod ug+rw,o-rw $log+ ||
	   warn "can't create new $log correctly" || return 1

	have_cmd set_extra_caps_for_exec ||
	  source /etc/$_our_app_/libsecure.sh || error libsecure.sh
	local extra_caps_for_exec
	  set_extra_caps_for_exec immutable

	if is_append_only .
	   then $Run chattr -a .
		local is_CWD_append_only=$true
	   else local is_CWD_append_only=$false
	fi

	$Run chattr -ai $log &&
	$Run ln $log $rolled &&
	$Run mv $log+  $log  &&
	$Run chattr +a $log $rolled	# daemon could still have $rolled open

	if [[ $old_rolled ]]
	   then [[ $is_world_readable ]] &&
		   local read_mode=a+r || local read_mode=o-r
		$Run chattr -ai           $old_rolled $rolled
		$Run chown root           $old_rolled $rolled
		$Run chmod a-w,$read_mode $old_rolled $rolled
		$Run chattr +a                        $rolled
		$Run nice -2 bzip2 -9 $old_rolled
		for file in $old_rolled
		    do	$Run chattr +i $file.bz2
		done
	fi
	[[ $is_CWD_append_only ]] && $Run chattr +a .

	clr_extra_caps_for_exec
	return 0
}

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
	    do	set -- $log_name.[0-9][0-9][0-9][0-9][0-9][0-9]*
		[[ $# != 1 || -e $1 ]] || continue
		logs="$logs $*"
	done
	[[ $was_noglob ]] && set -o noglob

	[[ $logs ]] || return 1
	logs=$(find $logs -mtime +$expire_days)
	[[ $logs ]] || return 1

	local extra_caps_for_exec
	  set_extra_caps_for_exec immutable

	$Run chattr -ia $logs
	if is_append_only .
	   then $Run chattr -a .
		local is_CWD_append_only=$true
	   else local is_CWD_append_only=$false
	fi
	$Run rm $logs
	[[ $is_CWD_append_only ]] && $Run chattr +a .
}

# ----------------------------------------------------------------------------

assert_sha1sum()
{
	local sha1sum=$1 file=${2-}

	set --  $(sha1sum $file)
	[[ $1 == $sha1sum ]] && return 0
	error    "sha1sum($file) != $sha1sum"
}

# --------------------------------------------

assert_jar_OK()
{
	local jar=$1

	unzip -q -t $jar | egrep --invert "^No errors detected "
	[[ ${PIPESTATUS[0]} == 0 && $? == 1 ]] ||
	   error "jar $jar seems corrupt"
}

# ----------------------------------------------------------------------------

# to test (top-level) functions by passing names as _args_ to $our_name script
# TODO: add getopts, then support -a: Abort if any function returns non-0
run_functions()
{
	local is_procedure=$false	# assume 'warn' if function "fails"
	[[ $1 == -p ]] && { is_procedure=$true; shift; }
	local functions=$*

	local status=0

	local function
	for   function in $functions
	    do	have_cmd  $function ||
		    error "function '$function' doesn't exist"
		$function && continue
		status=$?
		[[ $is_procedure ]] ||
		   warn "function '$function' returned $status"
	done

	return $status
}

# ----------------------

run_procedures() { run_functions -p "$@"; }

# ----------------------------------------------------------------------------

set_next_emacs_backup() {
	local path=$1

	path=${path%.~[0-9]*~}
	set -- $path.~[0-9]*~
	[[ -d  $path ]] && error "$path is a directory"
	declare -i max_backup_num=0
	local backup
	for backup
	    do	[[ -e $backup ]] || continue
		local _num=${backup##*.~}
		declare -i num=${_num%\~}
		(( $num > $max_backup_num )) && max_backup_num=num
	done
	max_backup_num=max_backup_num+1
	next_emacs_backup="$path.~$max_backup_num~"
}

# ----------------------------------------------------------------------------

pegrep() { grep --perl-regexp "$@"; }

# ----------------------------------------------------------------------------

set_vmstat_opts()
{

	# -S M on RHEL-6 causes si & so to always be 0 (bi is still KBps)
	  if vmstat -h 2>&1 | fgrep --silent -- -S &&
	     [[ ! $is_redhat || $(type -p vmstat) == *local/* ]]
	  then vmstat_opts='-a -S M'
	elif vmstat -h 2>&1 | fgrep --silent -- -a
	  then vmstat_opts=-a
	  else vmstat_opts=
	fi
}

# ----------------------------------------------------------------------------

does_file_end_in_newline()
{
	local file
	for file
	    do	[[ -f $file && -s $file ]] || return 1
		[[ $(tail -c 1 $file) ]] && return 1
	done
	return 0
}

# ----------------------------------------------------------------------------
# this section is not shared with /etc/martus/prologue.sh
# ----------------------------------------------------------------------------

set_FAQ_file() {
	case $our_name in
	   w*faq* | faq-w* )
		FAQ_file=~/gnulinuxfaq.txt ;;
	   m*faq* | faq-m* )
		FAQ_file=/usr/local/martus/doc/MartusServerFAQ.txt ;;
	   o*faq* | faq-o* )
		FAQ_file=/usr/local/bookshare/doc/bookshare-old.faq ;;
	   * )	FAQ_file=/usr/local/bookshare/doc/bookshare.faq ;;
	esac
}

# ----------------------------------------------------------------------------

if [[ $HOSTNAME == *.managed.contegix.com ]]
   then is_contegix=$true
   else is_contegix=$false
fi

# ----------------------------------------------------------------------------

default_systems_email_addr=root

[[ -d /etc/httpd ]] && is_apache_2=$true || is_apache_2=$false
					  readonly is_apache_2

is_contegix() { [[ $HOSTNAME == *.managed.contegix.com ]]; }

# ----------------------------------------------------------------------------

_echo_mail_header_OPTARG() { echo "$1: $OPTARG"; }

mail_from() {
	local reply_to_opt=
	{
	local OPTIND opt
	[[ $* == *-F* ]] || set -- -F $default_systems_email_addr "$@"
	while getopts "F:s:r:c:b:" opt
	   do	case $opt in
		   ( F ) _echo_mail_header_OPTARG From ;;
		   ( s ) _echo_mail_header_OPTARG Subject ;;
		   ( r ) _echo_mail_header_OPTARG Reply-To ;;
		   ( c ) _echo_mail_header_OPTARG Cc ;;
		   ( b ) _echo_mail_header_OPTARG Bcc ;;
		esac
	done
	let OPTIND=$OPTIND-1
	shift $OPTIND

	[[ $# != 0 ]] || error "$FUNCNAME $args: need recipients"
	echo -n "To: "
	if [[ $* == *' '* && $* != *,* ]]
	   then echo "$*" | sed 's/  */, /g'
	   else echo "$*"
	fi
	echo				# separate header from body
	cat				# cat stdin as message body
	} | $Run sendmail -odq -oi $reply_to_opt -t
}

# ----------------------------------------------------------------------------

is_NFS_mount() {

	local dir
	for dir
	    do	[[ -d $dir ]] && dir=$dir/. # in case $dir is a symlink
		[[ $(/bin/df $dir) == *:* ]] && return 0
	done
	return 1
}

# ----------------------------------------------------------------------------

have_cmd usleep ||
usleep() {
	local usecs=$1

	if (( ${#usecs} > 6 ))
	   then local secs=${usecs%??????}
	   else	local secs
		printf -v secs ".%06d" $usecs
	fi
	sleep $secs
}

# ----------------------------------------------------------------------------
# _our_app_ might have custom generic-library; source last, may need the above
# ----------------------------------------------------------------------------

[[ ! $__lib__ ]] || source $__lib__ || error "couldn't source $__lib__"
unset __lib__

true					# we must return 0
