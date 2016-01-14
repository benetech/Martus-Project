name = 'martus-clientside'

define name, :layout=>create_layout_with_source_as_source(name) do
	project.group = 'org.martus'
  project.version = $BUILD_NUMBER

	compile.options.source = $JAVAC_VERSION
	compile.options.target = compile.options.source
	compile.with(
		JUNIT_SPEC,
		project('martus-utils').packages.first,
		project('martus-common').packages.first,
		project('martus-swing').packages.first,
		LAYOUTS_SPEC,
		XMLRPC_CLIENT_SPEC,
		XMLRPC_COMMON_SPEC,
		project('martus-jar-verifier').packages.first
	)
  
	test.with(
		ICU4J_SPEC,
		BCPROV_SPEC
	)
	
	package :jar
	package :sources
end
