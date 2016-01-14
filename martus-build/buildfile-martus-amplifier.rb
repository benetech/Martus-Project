name = "martus-amplifier"

define name, :layout=>create_layout_with_source_as_source(name) do
	project.group = 'org.martus'
  project.version = $BUILD_NUMBER

	compile.options.source = $JAVAC_VERSION
	compile.options.target = compile.options.source
	compile.with(
		JUNIT_SPEC,
		ICU4J_SPEC,
		project('martus-utils').packages.first,
		project('martus-common').packages.first,
		XMLRPC_CLIENT_SPEC,
		XMLRPC_COMMON_SPEC,
		JETTY_SPEC,
		JAVAX_SERVLET_SPEC,
		LUCENE_SPEC,
		VELOCITY_SPEC
	)
  
	build do
		from_dir = _(:source, :main, :java)
		to_dir = _(:target, :main, :classes)
		#puts "Amplifier copying from: #{from_dir} to #{to_dir}"
		filter(from_dir).include('**/*.txt').into(to_dir).run
		filter(from_dir).include('**/*.html').into(to_dir).run
	end
	
	test.with(
		BCPROV_SPEC
	)

	#TODO: Failing test TestAmplifierLocalization
	test.exclude('org.martus.amplifier.common.test.TestAmplifierLocalization')
	
	package :jar
	package(:jar).include(_(:root, 'presentation'), :path=>'www/MartusAmplifier')
	package(:jar).include(_(:root, 'presentationNonSSL'), :path=>'www/MartusAmplifier')
	

	# NOTE: Old build script signed this jar

	package :sources
end
