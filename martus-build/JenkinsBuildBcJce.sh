export NSIS_HOME="$HOME/.wine/drive_c/Program Files/NSIS/"
export ATTIC_DIR="/var/lib/jenkins/martus-client/builds/$BUILD_NUMBER/"
sh $WORKSPACE/martus-build/checkout.sh

echo INPUT_BUILD_NUMBER=$INPUT_BUILD_NUMBER
buildr --trace -f martus-build/buildfile test=no clean martus-bc-jce:jar-with-shas 
