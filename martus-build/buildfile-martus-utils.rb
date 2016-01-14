name = "martus-utils"

define name, :layout=>create_layout_with_source_as_source(name) do
	project.group = 'org.martus'
  project.version = $BUILD_NUMBER

	compile.options.source = $JAVAC_VERSION
	compile.options.target = compile.options.source
	compile.with(
		JUNIT_SPEC,
		PERSIANCALENDAR_SPEC,
		ICU4J_SPEC
	)

	test.exclude 'org.martus.util.TestCaseEnhanced'
	
	package :jar
	package :sources
end
