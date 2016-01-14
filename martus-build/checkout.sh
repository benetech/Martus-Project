#cvs -d :ext:cvs.benetech.org:/var/local/cvs checkout martus
BRANCH=`hg --repository martus-build branch`
echo Checking out $BRANCH

function clone_or_fetch_martus {
echo clone_or_fetch_martus $1
cd $WORKSPACE
if [ -d "$1" ]; then
	hg --repository $1 pull --update --branch $BRANCH
else
	hg clone ssh://mvcs/martus/$1 --branch $BRANCH
fi
}

function clone_or_fetch {
echo clone_or_fetch $1
cd $WORKSPACE
if [ -d "$1" ]; then
	hg --repository $1 pull --update --branch $BRANCH
else
	hg clone ssh://mvcs/$1 --branch $BRANCH
fi
}

clone_or_fetch_martus martus-amplifier
clone_or_fetch_martus martus-bc-jce
clone_or_fetch_martus martus-client
clone_or_fetch_martus martus-clientside
clone_or_fetch_martus martus-common
clone_or_fetch_martus martus-docs
clone_or_fetch_martus martus-hrdag
clone_or_fetch_martus martus-jar-verifier
clone_or_fetch_martus martus-js-xml-generator
clone_or_fetch_martus martus-logi
clone_or_fetch_martus martus-meta
clone_or_fetch_martus martus-mspa
clone_or_fetch_martus martus-server
clone_or_fetch_martus martus-swing
clone_or_fetch_martus martus-thirdparty
clone_or_fetch_martus martus-utils

clone_or_fetch martus-sha
