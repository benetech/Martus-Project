export NSIS_HOME="$HOME/.wine/drive_c/Program Files/NSIS/"
export ATTIC_DIR="/var/lib/jenkins/martus-client/builds/$BUILD_NUMBER/"

# Must force Java 7 for Android toolchain compatibility
export JAVA_HOME="/etc/alternatives/java_sdk_openjdk"

sh $WORKSPACE/martus-build/checkout.sh
hg log -l 1 -R martus-common
buildr -f $WORKSPACE/martus-build/buildfile clean martus-api:package test=no