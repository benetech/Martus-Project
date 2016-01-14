tag="MartusMobile-APK-23-1.2.2"
echo "Pushing $tag to Google"

hg --repository ../martus-android push --new-branch google --rev $tag
