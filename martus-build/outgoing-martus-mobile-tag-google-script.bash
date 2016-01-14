tag="MartusMobile-APK-23-1.2.2"
echo "Outgoing $tag to Google"

hg --repository ../martus-android outgoing google --rev $tag
