name = "martus-js-xml-generator"

define name, :layout=>create_layout_with_source_as_source(name) do
	project.group = 'org.martus'
  project.version = $BUILD_NUMBER

	compile.options.source = $JAVAC_VERSION
	compile.options.target = compile.options.source
	compile.with(
		JUNIT_SPEC,
		RHINO_SPEC,
		project('martus-utils').packages.first,
		project('martus-common').packages.first
	)

	main_source_dir = _('source', 'main', 'java')
	main_target_dir = _('target', 'main', 'classes')

	build do
		filter(main_source_dir).include('**/*.csv').into(main_target_dir).run
		filter(main_source_dir).include('**/*.js').into(main_target_dir).run
		filter(main_source_dir).include('**/*.xml').into(main_target_dir).run
	end

	#TODO: Test Failure: This test fails due to a hard-coded Windows filename in
	# /martus-js-xml-generator/source/org/martus/martusjsxmlgenerator/text_finalResultWithAttachments.xml
	test.exclude('org.martus.martusjsxmlgenerator.TestImportCSV')

	
	package :jar
	package :sources
end
