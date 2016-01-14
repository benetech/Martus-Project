tag="Client-5.0.2"
echo "Outgoing $tag to Google"

hg --repository ../martus-build outgoing google --rev $tag
hg --repository ../martus-client outgoing google --rev $tag
hg --repository ../martus-clientside outgoing google --rev $tag
hg --repository ../martus-common outgoing google --rev $tag
hg --repository ../martus-hrdag outgoing google --rev $tag
hg --repository ../martus-jar-verifier outgoing google --rev $tag
hg --repository ../martus-js-xml-generator outgoing google --rev $tag
hg --repository ../martus-logi outgoing google --rev $tag
hg --repository ../martus-swing outgoing google --rev $tag
hg --repository ../martus-thirdparty outgoing google --rev $tag
hg --repository ../martus-utils outgoing google --rev $tag
