tag="Client-5.0.1"
echo "Pulling $tag From Google"

hg --repository ../martus-build pull google --rev $tag
hg --repository ../martus-client pull google --rev $tag
hg --repository ../martus-clientside pull google --rev $tag
hg --repository ../martus-common pull google --rev $tag
hg --repository ../martus-hrdag pull google --rev $tag
hg --repository ../martus-jar-verifier pull google --rev $tag
hg --repository ../martus-js-xml-generator pull google --rev $tag
hg --repository ../martus-logi pull google --rev $tag
hg --repository ../martus-swing pull google --rev $tag
hg --repository ../martus-thirdparty pull google --rev $tag
hg --repository ../martus-utils pull google --rev $tag
