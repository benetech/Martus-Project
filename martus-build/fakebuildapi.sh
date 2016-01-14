export RELEASE_IDENTIFIER=pre-4.0
export INPUT_BUILD_NUMBER=TEST
export BUILD_NUMBER=NNN
export ATTIC_DIR=/var/lib/hudson/martus-client/builds/$INPUT_BUILD_NUMBER
export WORKSPACE=/home/kevins/temp/martus

hg log -l 1 -R martus-common
buildr -f $WORKSPACE/martus-build/buildfile clean martus-api:package test=no
