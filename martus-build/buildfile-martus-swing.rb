name = "martus-swing"


define name, :layout=>create_layout_with_source_as_source(name) do
	project.group = 'org.martus'
  project.version = $BUILD_NUMBER

	main_source_dir = _('source', 'main', 'java')
	main_target_dir = _('target', 'main', 'classes')

	compile.options.source = $JAVAC_VERSION
	compile.options.target = compile.options.source
	compile.with(
		JUNIT_SPEC,
		LAYOUTS_SPEC,
		project('martus-utils').packages.first
	)
 
 	build do
		filter(main_source_dir).include('org/martus/swing/MartusLogo.png').into(main_target_dir).run
	end
 	
   
	package :jar
	package :sources
end
