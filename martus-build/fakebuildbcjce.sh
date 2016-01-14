export RELEASE_IDENTIFIER=pre-4.0
export INPUT_BUILD_NUMBER=TEST
export BUILD_NUMBER=NNN
export ATTIC_DIR=/var/lib/hudson/martus-client/builds/$INPUT_BUILD_NUMBER
export WORKSPACE=/home/kevins/temp/martus

cd /home/kevins/temp/martus
pwd

sh martus-build/buildbcjce.sh
