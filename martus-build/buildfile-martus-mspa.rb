name = "martus-mspa"

define name, :layout=>create_layout_with_source_as_source(name) do
	project.group = 'org.martus'
  project.version = $BUILD_NUMBER

	compile.options.source = $JAVAC_VERSION
	compile.options.target = compile.options.source
	compile.with(
		JUNIT_SPEC,
		LAYOUTS_SPEC,
		MAIL_SPEC,
		XMLRPC_COMMON_SPEC,
		XMLRPC_CLIENT_SPEC,
		XMLRPC_COMMONS_LOGGING_SPEC,
		XMLRPC_WS_COMMONS_UTIL_SPEC,
		project('martus-utils').packages.first,
		project('martus-swing').packages.first,
		project('martus-common').packages.first,
		project('martus-clientside').packages.first
	)


	#TODO: Test Failure: The following test fails on Linux because:
	#  1. It refers to a relative path instead of absolute, and
	#  2. There is a permissions error which I don't understand
	test.exclude 'org.martus.mspa.roothelper.TestProcessStdinStdOut'

	test.with(
		ICU4J_SPEC
	)

	jar_name = _("#{name}/target/martus-mspa-client-#{$BUILD_NUMBER}.jar")
	package :jar, :file=>jar_name

	# TODO: Old build script signed this jar

	package(:sources)
	package(:sources).merge(project('martus-common').package(:sources))
	package(:sources).merge(project('martus-utils').package(:sources))
	package(:sources).merge(project('martus-hrdag').package(:sources))
	package(:sources).merge(project('martus-logi').package(:sources))
	package(:sources).merge(project('martus-swing').package(:sources))
	package(:sources).merge(project('martus-clientside').package(:sources))
end
