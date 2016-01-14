#!/bin/bash

#$Header$

# this is for setting the path and doing other things
# run it as a script to echo canonical script path

#
# miscellaneous environment stuff
#

# caller can run this script to see default PATH; but it needs prologue.sh
[[ ${_did_source_prologue_sh-} ]] || source /etc/bookshare/prologue.sh

export LC_COLLATE=C			# so [A-Z] doesn't include a-y
export LC_ALL=C				# server needs nothing special

export RSYNC_RSH=ssh

#
# setup PATH: see also /etc/profile.d/bookshare.sh
#

PATH=/usr/bin:/bin

prepend2path PATH /usr/local/bin
 append2path PATH /usr/local/{bookshare,martus}/bin

# root scripts get the system-related binaries first in path
# the directories are listed as highest-priority first
if [[ $UID == 0 ]]; then
 prepend2path -r PATH /usr/local/{bookshare/,martus/,}sbin /usr/sbin /sbin
else
  append2path    PATH /usr/local/{bookshare/,martus/,}sbin /usr/sbin /sbin
fi

# java stuff
append2path PATH /opt/java/tools/maven/current/bin /usr/local/maven/bin
for __dir__ in /opt/java/sdk/current /opt/java/sdk /usr/java/jdk*
    do	[[ -d $__dir__ ]] && export JAVA_HOME=$__dir__ && break
done
unset __dir__
[[ ${JAVA_HOME-} ]] && append2path PATH $JAVA_HOME/bin $JAVA_HOME/current/bin

export PATH

# can execute this file as a script to get the correct PATH
if [[ $our_name == ENV.sh ]]
   then [[ ${CLASSPATH-} ]] &&
	echo CLASSPATH=$CLASSPATH
	echo PATH=$PATH
	exit 0
fi

#
# setup LD_LIBRARY_PATH
#

[[ ${LD_LIBRARY_PATH-} ]] || LD_LIBRARY_PATH=/usr/lib

# since we prepend, put the primary-architecture lib last
if [[ $(uname -m) == x86_64 ]]
   then set -- /usr/lib/{i,x}*-linux-gnu /usr/lib{3,6}?
   else set -- /usr/lib/{x,i}*-linux-gnu /usr/lib{6,3}?
fi
prepend2path LD_LIBRARY_PATH /usr/lib $*
# since we prepend, put the primary-architecture lib last
if [[ $(uname -m) == x86_64 ]]
   then set --     /lib/{i,x}*-linux-gnu     /lib{3,6}?
   else set --     /lib/{x,i}*-linux-gnu     /lib{6,3}?
fi
prepend2path LD_LIBRARY_PATH /lib $*
prepend2path LD_LIBRARY_PATH /usr/local/lib

export LD_LIBRARY_PATH

true					# we must return 0
