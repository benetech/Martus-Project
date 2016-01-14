export RELEASE_IDENTIFIER=pre-4.0
export INPUT_BUILD_NUMBER=TEST
export BUILD_NUMBER=NNN
export ATTIC_DIR=/var/lib/jenkins/martus-client/builds/$INPUT_BUILD_NUMBER

# fake signing
jarsigner -signed-jar $ATTIC_DIR/martus-client-signed-$INPUT_BUILD_NUMBER.jar $ATTIC_DIR/martus-client-unsigned-$INPUT_BUILD_NUMBER.jar SSMTSJAR


cd /home/kevins/temp/martus/
sh martus-build/buildrelease.sh
