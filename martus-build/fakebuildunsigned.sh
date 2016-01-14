export BUILD_NUMBER="TEST"
export ATTIC_DIR="/var/lib/jenkins/martus-client/builds/$BUILD_NUMBER/"

cd /home/kevins/temp/martus
pwd

buildr --trace -f martus-build/buildfile clean martus-client:build_unsigned test=no
cp -p martus-client/target/martus-client-unsigned-$BUILD_NUMBER.jar $ATTIC_DIR
cp -p martus-thirdparty/target/martus-thirdparty-$BUILD_NUMBER.zip $ATTIC_DIR
