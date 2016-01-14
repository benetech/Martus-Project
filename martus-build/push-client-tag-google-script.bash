tag="Client-5.0.2"
echo "Pushing $tag to Google"

hg --repository ../martus-build push --new-branch google --rev $tag
hg --repository ../martus-client push --new-branch google --rev $tag
hg --repository ../martus-clientside push --new-branch google --rev $tag
hg --repository ../martus-common push --new-branch google --rev $tag
hg --repository ../martus-hrdag push --new-branch google --rev $tag
hg --repository ../martus-jar-verifier push --new-branch google --rev $tag
hg --repository ../martus-js-xml-generator push --new-branch google --rev $tag
hg --repository ../martus-logi push --new-branch google --rev $tag
hg --repository ../martus-swing push --new-branch google --rev $tag
hg --repository ../martus-thirdparty push --new-branch google --rev $tag
hg --repository ../martus-utils push --new-branch google --rev $tag
