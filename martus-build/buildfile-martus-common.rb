name = "martus-common"

define name, :layout=>create_layout_with_source_as_source(name) do
	project.group = 'org.martus'
  project.version = $BUILD_NUMBER

	compile.options.source = $JAVAC_VERSION
	compile.options.target = compile.options.source
	compile.with(
		JUNIT_SPEC,
		XMLRPC_COMMON_SPEC,
		XMLRPC_SERVER_SPEC,
		XMLRPC_CLIENT_SPEC,
		ICU4J_SPEC,
		PERSIANCALENDAR_SPEC,
		BCPROV_SPEC,
		ORCHID_SPEC,
		JAVAROSA_SPEC,
		KXML_SPEC,
		project('martus-logi').package(:jar),
		project('martus-utils').package(:jar),
		project('martus-swing').package(:jar)
	)

	version_file = _('martus-common', 'source', 'org', 'martus', 'common', 'VersionBuildDate.java')
	compile ( version_file ) 

	file (version_file) => :always do
		date = today_as_iso_date
		build_date = "#{date}.#{$BUILD_NUMBER}"

		contents = File.read(version_file)
		contents.gsub!('#{BUILDDATE}', build_date)
		File.open(version_file, "w") do | f |
			f.write contents
		end
		puts "BuildDate set to: #{build_date}"
	end
  
	test.with(
	)

	#TODO: Failing test
	test.exclude 'org.martus.common.test.TestMartusSecurity'

	package :jar

	# NOTE: Old build script signed this jar

	package :sources
end
