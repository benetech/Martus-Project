tag="default"
echo "Pulling $tag (from the default repo)"

hg --repository ../martus-amplifier pull --update --rev $tag
hg --repository ../martus-build pull --update --rev $tag
hg --repository ../martus-client pull --update --rev $tag
hg --repository ../martus-clientside pull --update --rev $tag
hg --repository ../martus-common pull --update --rev $tag
hg --repository ../martus-hrdag pull --update --rev $tag
hg --repository ../martus-jar-verifier pull --update --rev $tag
hg --repository ../martus-js-xml-generator pull --update --rev $tag
hg --repository ../martus-logi pull --update --rev $tag
hg --repository ../martus-meta pull --update --rev $tag
hg --repository ../martus-mspa pull --update --rev $tag
hg --repository ../martus-server pull --update --rev $tag
hg --repository ../martus-swing pull --update --rev $tag
hg --repository ../martus-thirdparty pull --update --rev $tag
hg --repository ../martus-utils pull --update --rev $tag
