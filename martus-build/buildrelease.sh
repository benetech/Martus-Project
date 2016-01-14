echo INPUT_BUILD_NUMBER=$INPUT_BUILD_NUMBER
buildr --trace -f martus-build/buildfile test=no \
clean \
martus-client-linux-zip:package martus-client-linux-zip:sha1 martus-client-linux-zip:sha2 \
martus-client-nsis-single:build \
martus-client-nsis-pieces:package \
martus-client-nsis-upgrade:build \
martus-client-mac-dmg:package \
martus-client-iso:build
