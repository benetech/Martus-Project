#!/bin/bash

oPATH=$PATH
source /etc/martus/prologue.sh  || exit 1 # our libc equivalent
source /etc/martus/libsecure.sh || error libsecure.sh
PATH=$PATH:$oPATH
_our_name=$our_name			# our_name is reset later
_our_path=$our_path

export   ANDROID_HOME=/opt/android-sdk-linux
export       ANT_HOME=/opt/apache-ant
export PATH=$ANT_HOME/bin:$PATH

create_SVN_repo_usage="[-D] user group repo_home repo(s)"
Usage="
Usage: $our_name [options] action [args]
   Current actions are:
     mlp [-C] [-r rev]: clone (unless -C), then build desktop .mlp files
     apk [-C] [-r rev] [type]: clone (unless -C), build type (default release)
     zipalign [type]: run zipalign on type file (default release-signed)
     mkusr [-i id] username gecos [group]: setup a user account
     users: show recent ssh logins
     hg repo(s): create new hg repo(s)
     fix-hg-perms: fix group-write perms and SELinux labels in all repos
     svn $create_SVN_repo_usage: make SVN repo w/ tags/ trunk/ (! -D)
     cvs2svn department project(s): import each CVS project into SVN repository
     svnprop: add SVN properties to files based on their extension
     svnci: find all scott svn checkins, for status report/timesheet
     upd-android-sdk: update Android SDK
     fix-android-sdk: fix Android SDK mode values: readable; exe bit
   Common options:
	-n: pass -n (--dry-run) to rsync (for actions that use 'rsv')
	-v: pass -v (--verbose) to rsync and maybe other commands (maybe)
	-q: pass -q (--quiet) to rsync and maybe other commands   (maybe)

	-d: Debug shell script (don't run commands, just show them)
	-t: Trace shell script (show commands as they execute)"

Run= Trace= debug_opt= trace_opt= d2_rsync_opts=
while getopts "nvq dth" arg
    do  case $arg in
	   ( n ) add_words d2_rsync_opts --dry-run ;;
	   ( v ) add_words d2_rsync_opts --verbose ;;
	   ( q ) add_words d2_rsync_opts --quiet ;; # cancels --verbose

	   ( d ) Run=echo       debug_opt=-d ;; # put $Run before 'active' cmds
	   ( t ) Trace="set -x" trace_opt=-t ;;
	   ( h ) echo  "$Usage" ; exit 0 ;;
	   ( * ) error "$Usage" ;;
	esac
done
let OPTIND=$OPTIND-1
shift $OPTIND
unset arg

[[ -n $Trace && -n $Run ]] && Run=:

trap '' HUP TERM
trap 'set +x; rm -f $tmp $tmp2 $tmp3 $tmp4 $tmp5 $ts; trap EXIT' EXIT

 tmp=$tmpdir/$our_name-1-$$
  ts=$tmpdir/$our_name-ts-$$
tmp1=$tmp
tmp2=$tmpdir/$our_name-2-$$
tmp3=$tmpdir/$our_name-3-$$
tmp4=$tmpdir/$our_name-4-$$
tmp5=$tmpdir/$our_name-5-$$

shopt -s extglob

cutp() { cut -d\| "$@"; }

# ----------------------------------------------------------------------------
# functions
# ----------------------------------------------------------------------------

filter_rsync() {
   egrep -v "^(sent|wrote) .*bytes/sec$|^total size is|^(building|receiving) file list \.\.\. done$|^(sending|receiving) incremental file list$|^[[:space:]]*$"
}

# ---------------------------------

# the caller can augment rsync_opts
rsync_opts="--exclude=.svn --exclude=*.[0-9][0-9][0-9][0-9][0-9][0-9]
			   --exclude=.#*"
_rsv() {
	$Run rsync "$@" | filter_rsync
	[[ ${PIPESTATUS[0]} == 0 ]]
}

# ----------------------

rsv() {
	[[ -o xtrace ]] && { set +x; local xtrace="set -x"; } || local xtrace=
	_rsv  -aH   -Sv $rsync_opts $d2_rsync_opts "$@"
	local status=$?
	$xtrace
	return $status
}

# ----------------------

rsvr() {
	[[ -o xtrace ]] && { set +x; local xtrace="set -x"; } || local xtrace=
	_rsv -rltH -Sv $rsync_opts $d2_rsync_opts "$@"
	local status=$?
	$xtrace
	return $status
}

# ----------------------

rsv_fat() { _rsv -rlpt  -Sv $rsync_opts $d2_rsync_opts "$@"; }

# ----------------------

ersv () { eval rsv "$@"; }

# ----------------------

ersvr() { eval rsvr "$@"; }

# ----------------------------------------------------------------------------

#          RSA1 public key is xxx B default,  xxx B for 2048 bit
# ssh-rsa: RSA  public key is 200 B default,  372 B for 2048 bit
# ssh-dsa: DSS  public key is 580 B default, 1092 B for 2048 bit
setup_user_ssh() {
	local username=$1 group=${2-users}

	local home
	eval "home=~$username"
	[[ $home != "~"* ]] || warn "couldn't find $username in /etc/passwd" ||
	   return 1
	[[ $home == /home/* ]] || warn "$username homedir must be in /home" ||
	   return 1

	local ssh_dir=$home/.ssh	# was once a global variable
	$Run mkdir --parents $ssh_dir || error mkdir

	local new_auth_file=$tmpdir/$username.auth
	local     auth_file=$ssh_dir/authorized_keys
	[[ $Run ]] && auth_file=/dev/stdout
	local ssh_cmd_msg="echo This account can only be used for svn."
	$Trace
	[[ -s $new_auth_file &&
	   ( ! -s $auth_file || ! $auth_file -nt $new_auth_file ) ]] &&
	$Run sed "/^ *ssh-/s/^/command=\"$ssh_cmd_msg\" /" $new_auth_file \
							     > $auth_file &&
	ls -l $auth_file

	$Run touch $auth_file &&
	$Run chmod -R go-rwx $ssh_dir &&
	$Run chmod a-wx $ssh_dir/* || error ".ssh perms problems"

	$Run restorecon -R -v $home
	if $Run chown -R $username:$group $ssh_dir
	   then local status=0
	   else $Run chown -R $username $ssh_dir &&
		$Run chgrp -R users $ssh_dir
		local status=$?
	fi
	local sshd_config_file=/etc/ssh/sshd_config
	grep --silent "^AllowUsers .* $username\b" $sshd_config_file &&
	  return $status
	if [[ $status == 0 ]]
	   then error "group, iptables, AllowUsers, /etc.xen-6/aliases"
	   else error "error(s)"
	fi
}

# ----------------------------------------------------------------------------

domain2IP() {
	local name=$1
	set -- $(hostx $name | sed -n 's/.*\tA\t//p')
	echo $1
}

# ----------------------------------------------------------------------------

set_id___from_file_name() {
	local file=$1 name=$2

	id=$(egrep "^$name:" $file | cut --delimiter=':' --field=3)
	[[ $id ]] || error "can't find '$name' in $file"
}

# ----------------------------------------------------------------------------

set_users___from_group() {
	local group=$1 file=/etc/group

	users=$(egrep "^$group:" $file | cut --delimiter=':' --field=4)
	[[ $users ]] || error "can't find '$group' in $file"
	users=${users//, /}
}

# ----------------------------------------------------------------------------

set_next_id() {
	[[ ${1-} == -i ]] && { declare -i id=$2; shift 2; } || declare -i id=0
	[[ $# == 1 || $# == 2 ]] || error "set_next_id config_file [regex]"
	local config_file=$1 name_regex=${2-}

	if [[ $id -eq 0 ]]
	   then local temp1=$tmpdir/d2-set_next_id-$$
		if [[ ! $name_regex ]]
		   then local records=$config_file
		   else local records=$temp1
			egrep "$name_regex" $config_file > $records ||
			  error "didn't find '$name_regex' in $config_file"
		fi
		declare -i id=$(egrep --invert '^(nobody|nogroup):' $records |
				  cut --delimiter=':' --field=3 |
				  sort --numeric-sort | tail --lines=1)
		rm --force $temp1
	fi

	id+=1
	while true
	   do	local file
		for file in /etc/passwd /etc/group
		    do	if fgrep --silent ":$id:" $file
			   then id+=1
				continue 2
			fi
		done
		break
	done
	next_id=$id
}

# ----------------------------------------------------------------------------

mkusr() {
	[[ ${1-} == -i ]] && { local id_opt="$1 $2";shift 2; } || local id_opt=
	[[ $# == 2 || $# == 3 ]] || error "mkusr [-i id] user gecos [group]"
	local user=$1 gecos=$2 group=${3:-users}

	grep --silent "^$user:" /etc/passwd && error "user $user exists"

	case $user in
	   svn* ) local user_regex='^svn'	;;
	   *    ) local user_regex='kevins:'	;;
	esac
	set_next_id $id_opt /etc/passwd "$user_regex"

	grep --silent "^$group:" /etc/group ||
	$Run groupadd -g $next_id    $group || error $group problem
	$Run useradd  -u $next_id -g $group -p '*' -c "$gecos" -m $user &&
	setup_user_ssh $user $group || $Run error "mkusr $* => $?"

	[[ $group == svn* ]] ||
	warn "If '$user' needs svn access, add to group svn"
}

# ----------------------------------------------------------------------------

mkgroup() {
	[[ ${1-} == -i ]] && { local id_opt="$1 $2";shift 2; } || local id_opt=
	[[ $# -ge 1 ]] || error "mkgroup [-i id] group [users]"
	local group=$1; shift
	local _users=${*-} users= user home

	for user in ${_users//,/ }
	    do	eval home=\~$user
		[[ -d $home ]] || error "$user has no homedir"
		add_words users $(egrep ":$home:/s?bin/" /etc/passwd |
				    cut -d: -f1)
	done

	[[ $group != [A-Z][A-Z]* ]] || is_arg1_in_arg2 svn-dashboard $users ||
	   users="svn-dashboard $users"

	grep --silent "^$group:" /etc/group && error "group $group exists"

	false && {			# no longer have CO class
	if [[ $group == *-* ]]
	   then local group_regex='^co-ccj:'
	   else if [[ $group == [A-Z][A-Z] ]]
		   then local group_regex='^[A-Z][A-Z]:'
		   else local group_regex='^[a-zA-Z0-9]*:'
		fi
		[[ $users ]] || set_users___from_group $canonical_DP_group
	fi
	set_next_id $id_opt /etc/group "$group_regex"
	}

	[[ $users ]] ||
	 error "$group doesn't look like a data project, need to specify users"

	$Run groupadd $group &&
	$Run gpasswd -M ${users// /,} $group ||
	  error "couldn't setup $group"
	exit

	if [[ $group == [A-Z]* && ! $Run ]]
	   then echo "$group:x:$next_id:" >> /etc/group &&
		echo "$group:!::"         >> /etc/gshadow
	   else	$Run groupadd -g $next_id $group
	fi &&
	$Run gpasswd -M ${users// /,} $group ||
	  error "couldn't setup $group"
}

# ----------------------------------------------------------------------------

# call with correct umask (or change top-level dir later):
#   02: everyone can read, group can read or write
#   07: world  can't read, group can read or write
#  027: world  can't read, group can read-only
repo_home_default=~svn
create_SVN_repo() {
	local do_mkdirs=$true
	[[ $1 == -D ]] && { do_mkdirs=; shift; }
	[[ $# -ge 4 ]] ||
	   error "create_SVN_repo $create_SVN_repo_usage"
	local user=$1 group=$2 repo_home_default=$3; shift 3
	$Run egrep --silent "^$group:" /etc/group ||
	   error "Unix group doesn't exist, run: d2 mkgroup $group [user(s)]"

	if [[ $repo_home_default ]]
	   then	[[   $repo_home_default == /* ]] ||
		eval "repo_home_default=~$repo_home_default"
		[[ -d $repo_home_default ]] ||
		   error "can't find default repo home $repo_home_default"
	fi

	local CCJ_class_config_dir=~scott/projects/class-README
	if [[   -d $CCJ_class_config_dir ]]
	   then cd $CCJ_class_config_dir && $Run svn update --quiet || return 1
	fi
	if [[ $repo_home_default == */svn-class ]]
	   then local is_CCJ_class=$true
	   else local is_CCJ_class=$false
	fi

	local working_dir=$tmpdir/mkrepo
	rm -rf $working_dir && mkdir -p $working_dir || error mkdir

	local repo
	for repo
	    do	if [[ $repo == */* ]]
		   then local _repo_home=${repo%/*}; repo=${repo##*/}
		   else local _repo_home=$repo_home_default
		fi
		$Trace
		$Run gpasswd -a $user ${_repo_home##*/} &> /dev/null
		cd $_repo_home &&
		$Run svnadmin create --fs-type fsfs $repo || return 1
		# perms are controlled by changing  $repo (top-level dir) only
		find $repo/* -perm +0200 | xargs -r $Run chmod g+w
		if [[ $do_mkdirs ]]
		   then cd $working_dir && rm -rf $repo &&
			$Run svn checkout file://$_repo_home/$repo $repo &&
			   $Run cd $repo || return 1
			local dirs="branches tags trunk"
			[[ -d $CCJ_class_config_dir ]] && dirs="$dirs README"
			$Run svn mkdir $dirs
			[[   -d $CCJ_class_config_dir ]] &&
			$Run cp $CCJ_class_config_dir/*.txt README/ &&
			$Run svn add README/*.txt
			$Run chown -R $user:$group *
			$Run svn ci -m "Store your latest work in 'trunk'"
			cd $_repo_home
		fi
		$Run chown -R $user:$group $repo &&
		find $repo -type d | xargs $Run chmod g+s ||
		  return 1
	done
	return 0
}

# ----------------------------------------------------------------------------

update_hg_repo() {
	local repos=${*:-.}

	[[ ! $do_clone ]] && return

	echo \
	  "grabbing files; _must_ be silent before 'Buildfile:' or nohup.out"

	umask 02

	local repo cwd=$PWD
	for repo in $repos
	    do	cd_ $cwd/$repo
		$Run rm -rf .[^.] .[^h]? .h[^g] .???* *

		$Run hg --quiet verify &&
		# $Run hg verifysig
		$Run hg --quiet pull || error "something wrong with our clone"

		$Run hg --quiet update --clean --rev $rev &&
		$Run hg --quiet revert --all || error "hg revert returned $?"
		$Run chmod -R g+rwX,o-w *
	done
	cd_ $cwd
}

# ----------------------------------------------------------------------------

cpmod() {
	local src=$1 dst=$2

	[[ -e $src && -e $src ]] ||
	   { $Run error "$(ls -ld $src $dst)"; return; }
	local mode=$(stat $src | sed -n 's@Access: (\([0-9]*\)/.*@\1@p')
	$Run chmod $mode $dst
	set -- $(ls -ld  $src)
	cpmod_user=$3 cpmod_group=$4	# global variables
	$Run chown $cpmod_user:$cpmod_group $dst
}

# ----------------------------------------------------------------------------

hostname=${HOSTNAME%%.*}

shopt -s extglob			# we use extended pattern matching

#############################################################################
# put stuff after here
#############################################################################

[[ $# != 0 ]] || error "specify an action to perform\n$Usage"

set -u

action=$1; [[ $action != [0-9]*[0-9] ]] && shift
our_name="$our_name $action"		# for 'error' function

case $action in
   mlp ) # mlp [-C] [-r rev]: clone (unless -C), then build desktop .mlp files
	if [[ ${1-} == -C ]] 
	   then do_clone=$false; shift
	   else do_clone=$true
	fi
	if [[ ${1-} == -r ]]
	   then rev=$2; shift 2
	   else rev=default
	fi

	$Trace
	umask 2
	cd_ ~build-jar/martus-desktop-mlp
	update_hg_repo martus-build martus-client
	[[ -s nohup.out ]] && $Run mv -v nohup.out nohup.out~
	$Run ant -f martus-build/build-mlp.xml |& tee nohup.out
	[[ $Run && -e nohup.out ]] && rm nohup.out
	exit 0
	;;

 # apk [-C] [-r rev] [type]: clone (unless -C), build type (default release)
   apk )
	if [[ ${1-} == -C ]]
	   then do_clone=$false; shift
	   else do_clone=$true
	fi
	if [[ ${1-} == -r ]]
	   then rev=$2; shift 2
	   else rev=default
	fi
	type=${1:-release}

	$Trace
	umask 2
	cd_ ~build-jar/martus-android
	update_hg_repo martus-android martus-wrapper
	repo=martus-wrapper		# new way
	[[ -s $repo/build.xml ]] ||
	repo=martus-android		# old way
	apk_file=bin/Martus-release-unsigned.apk # see xen0:~sign-files/d2
	$Run rm -f martus-*/$apk_file
	cd_ $repo

	[[ -s nohup.out ]] && $Run mv -v nohup.out ~build-jar/work/apk/
	$Run ant clean $type |& tee nohup.out
	status=${PIPESTATUS[0]}
	$Run chmod -R g+rwX,o-w ~build-jar/martus-android/[am]*/* |&
	   egrep -v ': Operation not permitted$'
	if ! [[ $status == 0 && -s $apk_file ]]
	   then banner failed
		log=$PWD/nohup.out.$(date +%F.%T)
		cp -p nohup.out $log
		error "build failed, see $hostname:$log"
	fi
	[[ $Run && -e nohup.out ]] && rm nohup.out
	exit 0
	;;
 # zipalign [type]: run zipalign on type file (default release-signed)
   zipa* )
	type_=${1:-release-signed} email_addr=${2:-barbram@benetech.org}
	type_=${type_%.apk}

	unaligned_apk=Martus-${type_#Martus-}.apk
	  aligned_apk=${unaligned_apk/-unaligned.apk/.apk}
	cd_ ~build-jar/work/apk
	set -- $(ls -tr Martus-*.apk)
	while [[ $# -gt 5 ]]; do $Run rm -f $1{,.sha1}; shift; done
	true && {
	for file in $aligned_apk{,.sha1}
	    do	[[ -s $file ]] && $Run mv -v $file $file-
	done
	set -- /opt/android-sdk-linux/build-tools/*/zipalign
	PATH=$PATH:$(dirname ${!#})
	# -v shows every file, might lose error messages
	$Run zipalign 4 $unaligned_apk $aligned_apk || error "zipalign => $?"
	[[ $Run ]] ||
	sha1sum $aligned_apk > $aligned_apk.sha1    || error  "sha1sum => $?"
	}
	rsv $aligned_apk{,.sha1} secure-build@hrdag: &&
	[[ ! $Run ]] && set -x &&
	echo https://hrdag.benetech.org/martus-client/Releases/secure-build/ |
	mail -s "Mobile Martus secure build uploaded to website" $email_addr
	;;

   mkusr ) # mkusr   [-i id] username gecos [group]: setup a user account
	mkusr "$@"
	;;
   users ) # users: show recent ssh logins 
	fgrep ']: Accepted publickey for' /var/log/secure |
	egrep -v "Accepted publickey for (jenkins|root|scott|$LOGNAME) from " |
	sed 's/ port [0-9]* ssh2$//' | tail -n 2
	date | sed 's/^... //'
	;;
   yu* | yeu )
	[[ $Run ]] || set -x
	$Run exec yum --enablerepo=rpmforge-extras update mercurial ;;
   y*c*u* )				# ycu
	[[ $Run ]] || set -x
	$Run yum --enablerepo=rpmforge-extras check-update | fgrep mercurial &&
	   set +x &&
	   echo -e "\nver 2.2.3 is final, see: http://mercurial.selenic.com/wiki/WhatsNew"
	;;
   svn*load )
	[[ ${1-} == -d ]] && { dir=$1; shift 2; } || dir=~svn
	for dump in ${*:-*.dump.bz2}
	    do	bzcat $dump | $Run svnadmin load $dir
	done
	;;
    hg ) # hg repo(s): create new hg repo(s)
	[[ $# != 0 ]] || error "specify repo name(s)"
	[[ $UID != 0 ]] || error "don't be root"
	# I'm not sure what this was about, unless it really meant mvcs
	# $Run warn "need 'semanage fcontext' on mjenkins"
	cd_ ~hg
	umask 02
	for repo
	    do	[[ ! -d $repo ]] || warn "$repo exists, skipping" || continue
		[[ $repo == *[^-/a-z0-9]* || $repo != [a-z]* ]] &&
		   error "weird name, fix hgserve"
		$Run mkdir $repo
		$Run cd_ $repo
		$Run hg init
		$Run ln --verbose ~hg/.hg/hgrc .hg/
		$Run cd ..
		# $Run chgrp -R xxx $repo
	done
	[[ $repo == */* ]] ||
	echo "as root, run: restorecon -R -v /var/local/vcs/hg"
	;;
   fix*hg*perm* )
	cd_ ~hg
	find * ! -name hgrc ! -perm +020 -print0 | xargs -0 $Run chmod g+w
	cmd="restorecon -R /var/local/vcs/hg"
	if [[ $UID == 0 ]]
	   then $Run $cmd
	   else echo "as root, run: $cmd"
	fi
	;;
   svn )
	create_SVN_repo "$@"
	;;
   cvs2svn )
	[[ $# -ge 2 ]] || error "department project(s)"
	department=$1; shift
	[[ $department ]] && department=-$department
	time \
	for project
	    do	(( $# > 1 )) && header $project
		eval \
		  "cvs_repo_home=~cvs$department svn_repo_home=~svn$department"
		cd_ $cvs_repo_home || continue
		set -- $(ls -ld $project)
		create_SVN_repo -D $3 $4 $svn_repo_home $project &&
		# --no-default-eol: it disappeared about 5 years ago
		# --default-eol=binary got a key error
		# --fallback-encoding: e.g. Latin-1 catches Windows (c) char
		time $Run cvs2svn				\
		    --fallback-encoding=Latin-1			\
		    --eol-from-mime-type			\
		    --mime-types=/etc/martus/svn-mime.types	\
		    --existing-svnrepos				\
		    --svnrepo $svn_repo_home/$project		\
		    $cvs_repo_home/$project
	done
	;;
   svnprop ) # add SVN properties to files based on their extension
	[[ -d .svn ]] ||
	   error "you should be in the trunk of your SVN workspace"

	setprop() {
		local name=$1 value=$2; shift 2

		local ext names=
		for ext
		    do	names="$names -o -iname *.$ext"
		done
		names="( ${names# -o } )"

		set -- *
		set -f
		$Trace
		find $* -name .svn -prune -o $names -print0 |
		   xargs -0 --no-run-if-empty \
			$Run svn propset $name "$value" # --force
		set +x
		set +f
	}
	 text_ext="txt text notes report properties"	; readonly text_ext
	ascii_ext="histo meta log md5 sha sha1"		; readonly ascii_ext
	  bin_ext="marsh array num numpy scipy dat pyc pyo odp odt emf dta"
	  bin_ext="$bin_ext sxc sxd sxg sxi sxw gnumeric"
	  bin_ext="$bin_ext dms lha lzh so dmg o"
	  bin_ext="$bin_ext z gz bz2 tgz tb2 tar zip jar iso class"
	  bin_ext="$bin_ext cdx dbc dbf dct  dcxfpt fxp"
	  bin_ext="$bin_ext lzo tzo rz odp ods odt otp ots ott"
	  src_ext="r do ado py php inc sh java c h cpp c++ sql prg map"
	  src_ext="$src_ext Makefile config pass control flattener"
						readonly bin_ext src_ext

	keywords="Date Revision Author HeadURL Id"
	setprop svn:keywords "$keywords"	01 $text_ext $src_ext

	MT=svn:mime-type				; readonly MT
	setprop $MT text/comma-separated-values	csv dsv tsv
	setprop $MT text/plain			$text_ext $ascii_ext
	setprop $MT text/program		 $src_ext 01
	setprop $MT text/postscript		eps ps
	setprop $MT text/pdf			pdf
	setprop $MT text/png			png
	setprop $MT text/rtf			rtf
	setprop $MT text/xml			xml form webprj
	setprop $MT text/html			html htm
	setprop $MT application/msword		doc
	setprop $MT application/vnd.ms-excel	xls
	setprop $MT application/vnd.ms-powerpoint	ppt
	setprop $MT application/octet-stream	$bin_ext

	echo -e "\nsvn ci -m'Setup standard $MT and svn:keywords properties.'"

	warn "decide if should use (probably pointless): svn propset --force"
	;;
   svnci ) # find all scott svn checkins, for status report/timesheet
	$Trace
	for project in martus-server
	    do	svn log file:///var/local/svn/$project | less '+/scott'
	done
	;;
   u*a*sdk ) # upd-android-sdk: update Android SDK
	# https://wiki.echocat.org/display/ECHOCAT/2012/08/17/Configure+Android+SDK+on+CentOS+6+x86_64
	dir=${1:-/opt/android-sdk-linux}
	[[ $UID == 0 ]] || $Run error "must be root"
	cd_ $dir
	readlink $PWD
	[[ $Run ]] || set -x
	$Run chown -R $LOGNAME . &&
	$Run chmod -R u+w      . || error oops
	su $LOGNAME -c "{ while : ; do echo y; sleep 0.2; done; } | $Run $PWD/tools/android -s update sdk --no-ui" || error update
	readlink $PWD
	exec $our_path $debug_opt $debug_opt fix-android-sdk $dir
	;;
   f*a*sdk )# fix-android-sdk: fix Android SDK mode values: readable; exe bit
	dir=${1:-/opt/android-sdk-linux}
	    exe="bat bin exe py so"
	non_exe="apk ini jar java jet js mp3 mps ogg png pom ttf txt xml zip"
	fix() { local action=$1; shift
		local regex=$(echo $* | tr ' ' '|')
		find . -regextype posix-egrep -type f \
		       -regex ".*\.($regex)$" -print0 |
		   xargs -0 $Run chmod $action | cut -c 1-79; }
	[[ $UID == 0 ]] || $Run error "must be root"
	set -x
	cd_ $dir
	$Run chmod -R a+X,a+r,o-w .
	$Run chown -R root:root   .
	fix a+x     $exe
	fix a-x $non_exe
	find . -regextype posix-egrep -type f \
	       -regex ".*/(displayState|documentData|layout)$" -print0 |
	  xargs -0 $Run chmod a-x | cut -c 1-79

	readlink $PWD
	$Run exec restorecon -R $PWD  # might do nothing since $PWD is symlink
	;;
   * ) error "unknown action\n$Usage" ;;
esac

exit
