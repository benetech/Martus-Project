export NSIS_HOME="$HOME/.wine/drive_c/Program Files/NSIS/"
export ATTIC_DIR="/var/lib/jenkins/martus-client/builds/$BUILD_NUMBER/"
sh $WORKSPACE/martus-build/checkout.sh
hg log -l 1 -R martus-common
buildr -f $WORKSPACE/martus-build/buildfile clean martus-client:build_unsigned test=no
