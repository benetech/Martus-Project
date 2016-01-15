#!/bin/sh
export DEST=/Users/charlesl/GitRepo/Martus-Project;
export SRC=/Users/charlesl/EclipseMartus/martus-build;
rm -rf $DEST/martus-amplifier
rm -rf $DEST/martus-android
rm -rf $DEST/martus-bc-jce
rm -rf $DEST/martus-build
rm -rf $DEST/martus-client
rm -rf $DEST/martus-clientside
rm -rf $DEST/martus-common
rm -rf $DEST/martus-hrdag
rm -rf $DEST/martus-jar-verifier
rm -rf $DEST/martus-js-xml-generator
rm -rf $DEST/martus-logi
rm -rf $DEST/martus-meta
rm -rf $DEST/martus-mspa
rm -rf $DEST/martus-server
rm -rf $DEST/martus-swing
rm -rf $DEST/martus-thirdparty
rm -rf $DEST/martus-utils

cp -r ../martus-amplifier $DEST/martus-amplifier
cp -r ../martus-android $DEST/martus-android
cp -r ../martus-bc-jce $DEST/martus-bc-jce
cp -r ../martus-build $DEST/martus-build
cp -r ../martus-client $DEST/martus-client
cp -r ../martus-clientside $DEST/martus-clientside
cp -r ../martus-common $DEST/martus-common
cp -r ../martus-hrdag $DEST/martus-hrdag
cp -r ../martus-jar-verifier $DEST/martus-jar-verifier
cp -r ../martus-js-xml-generator $DEST/martus-js-xml-generator
cp -r ../martus-logi $DEST/martus-logi
cp -r ../martus-meta $DEST/martus-meta
cp -r ../martus-mspa $DEST/martus-mspa
cp -r ../martus-server $DEST/martus-server
cp -r ../martus-swing $DEST/martus-swing
cp -r ../martus-thirdparty $DEST/martus-thirdparty
cp -r ../martus-utils $DEST/martus-utils

rm -rf $DEST/martus-amplifier/.hg
rm -rf $DEST/martus-amplifier/.hg*
rm -rf $DEST/martus-amplifier/.DS_Store
rm -rf $DEST/martus-amplifier/classes
rm -rf $DEST/martus-amplifier/bin

rm -rf $DEST/martus-android/.hg
rm -rf $DEST/martus-android/.hg*
rm -rf $DEST/martus-android/.DS_Store
rm -rf $DEST/martus-android/.gradle
rm -rf $DEST/martus-android/.idea
rm -rf $DEST/martus-android/build
rm -rf $DEST/martus-android/bin


rm -rf $DEST/martus-bc-jce/.hg
rm -rf $DEST/martus-bc-jce/.hg*
rm -rf $DEST/martus-bc-jce/.DS_Store
rm -rf $DEST/martus-bc-jce/bin

rm -rf $DEST/martus-build/.hg
rm -rf $DEST/martus-build/.hg*
rm -rf $DEST/martus-build/.DS_Store
rm -rf $DEST/martus-build/bin

rm -rf $DEST/martus-client/.hg
rm -rf $DEST/martus-client/.hg*
rm -rf $DEST/martus-client/.DS_Store
rm -rf $DEST/martus-client/bin

rm -rf $DEST/martus-clientside/.hg
rm -rf $DEST/martus-clientside/.hg*
rm -rf $DEST/martus-clientside/.DS_Store
rm -rf $DEST/martus-clientside/bin

rm -rf $DEST/martus-common/.hg
rm -rf $DEST/martus-common/.hg*
rm -rf $DEST/martus-common/.DS_Store
rm -rf $DEST/martus-common/bin

rm -rf $DEST/martus-hrdag/.hg
rm -rf $DEST/martus-hrdag/.hg*
rm -rf $DEST/martus-hrdag/.DS_Store
rm -rf $DEST/martus-hrdag/bin

rm -rf $DEST/martus-jar-verifier/.hg
rm -rf $DEST/martus-jar-verifier/.hg*
rm -rf $DEST/martus-jar-verifier/.DS_Store
rm -rf $DEST/martus-jar-verifier/bin

rm -rf $DEST/martus-js-xml-generator/.hg
rm -rf $DEST/martus-js-xml-generator/.hg*
rm -rf $DEST/martus-js-xml-generator/.DS_Store
rm -rf $DEST/martus-js-xml-generator/bin

rm -rf $DEST/martus-logi/.hg
rm -rf $DEST/martus-logi/.hg*
rm -rf $DEST/martus-logi/.DS_Store
rm -rf $DEST/martus-logi/bin

rm -rf $DEST/martus-meta/.hg
rm -rf $DEST/martus-meta/.hg*
rm -rf $DEST/martus-meta/.DS_Store
rm -rf $DEST/martus-meta/bin

rm -rf $DEST/martus-mspa/.hg
rm -rf $DEST/martus-mspa/.hg*
rm -rf $DEST/martus-mspa/.DS_Store
rm -rf $DEST/martus-mspa/bin

rm -rf $DEST/martus-server/.hg
rm -rf $DEST/martus-server/.hg*
rm -rf $DEST/martus-server/.DS_Store
rm -rf $DEST/martus-server/bin

rm -rf $DEST/martus-swing/.hg
rm -rf $DEST/martus-swing/.hg*
rm -rf $DEST/martus-swing/.DS_Store
rm -rf $DEST/martus-swing/bin

rm -rf $DEST/martus-thirdparty/.hg
rm -rf $DEST/martus-thirdparty/.hg*
rm -rf $DEST/martus-thirdparty/.DS_Store
rm -rf $DEST/martus-thirdparty/bin

rm -rf $DEST/martus-utils/.hg
rm -rf $DEST/martus-utils/.hg*
rm -rf $DEST/martus-utils/.DS_Store
rm -rf $DEST/martus-utils/bin



echo
echo "Now sync $DEST with Github then Tag this Release"
echo


