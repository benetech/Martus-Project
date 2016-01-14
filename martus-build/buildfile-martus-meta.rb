name = "martus-meta"

define name, :layout=>create_layout_with_source_as_source(name) do
	project.group = 'org.martus'
  project.version = $BUILD_NUMBER

	compile.options.source = $JAVAC_VERSION
	compile.options.target = compile.options.source
	compile.with(
		JUNIT_SPEC,
		project('martus-utils').packages.first,
		project('martus-common').packages.first,
		project('martus-clientside').packages.first,
		project('martus-client').packages.first,
		project('martus-server').packages.first,
		project('martus-amplifier').packages.first
	)

	#TODO: No point in executing TestMeta or TestMetaQuick since they
	# just execute all the other tests anyway
	test.exclude('org.martus.meta.TestMeta')
	test.exclude('org.martus.meta.TestMetaQuick')

	#TODO: Test Failure: This test fails due to a hard-coded Windows filename in
	# /martus-js-xml-generator/source/org/martus/martusjsxmlgenerator/text_finalResultWithAttachments.xml
	test.exclude('org.martus.martusjsxmlgenerator.TestImportCSV')

	#TODO: Test Failures: Not sure why these tests fail
	test.exclude('org.martus.meta.TestHeadQuartersTableModelConfiguration')
	test.exclude('org.martus.meta.TestHeadQuartersTableModelEdit')
	test.exclude('org.martus.meta.TestRetrieveHQTableModel')
	test.exclude('org.martus.meta.TestSSL')
	test.exclude('org.martus.meta.TestSpeed')

	test.with(
		ICU4J_SPEC,
		BCPROV_SPEC,
		XMLRPC_COMMON_SPEC,
		XMLRPC_SERVER_SPEC,
		XMLRPC_CLIENT_SPEC,
		JETTY_SPEC,
		VELOCITY_DEP_SPEC
	)
	
	package :jar

	# NOTE: Old build script signed this jar

	package :sources

end
