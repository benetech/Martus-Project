name = "martus-server"

define name, :layout=>create_layout_with_source_as_source(name) do
	project.group = 'org.martus'
  project.version = $BUILD_NUMBER
  date = today_without_dashes
  jarpath = _(:target, "martus-server-#{date}.#{project.version}.jar")

	compile.options.source = $JAVAC_VERSION
	compile.options.target = compile.options.source
	compile.with(
		JUNIT_SPEC,
		project('martus-utils').packages.first,
		project('martus-common').packages.first,
		project('martus-amplifier').packages.first,
		XMLRPC_COMMON_SPEC,
		XMLRPC_CLIENT_SPEC,
		XMLRPC_SERVER_SPEC,
		BLUEPRINT_CORE_SPEC,
		COMMONS_BEANUTILS_SPEC,
		COMMONS_BEANUTILS_CORE_SPEC,
		COMMONS_COLLECTIONS_SPEC,
		COMMONS_CONFIGURATION_SPEC,
		COMMONS_DIGESTER_SPEC,
		COMMONS_LANG_SPEC,
		CONCURRENT_LINKED_HASHMAP_LRU_SPEC,
		JNA_SPEC,
		JNA_PLATFORM_SPEC,
		ORIENT_COMMONS_SPEC,
		ORIENTDB_CORE_SPEC,
		ORIENTDB_GRAPHDB_SPEC,
		ORIENTDB_NATIVEOS_SPEC
		
	)

	test.with(
		BCPROV_SPEC,
		JETTY_SPEC,
		ICU4J_SPEC
	)

  package(:jar, :file => jarpath).tap do | p |
    puts "Packaging server #{p.to_s}"
    p.with :manifest=>manifest.merge('Main-Class'=>'org.martus.server.main.MartusServer')

    p.merge(project('martus-jar-verifier').package(:jar))
    p.merge(project('martus-common').package(:jar))
    p.merge(project('martus-utils').package(:jar))
    p.merge(project('martus-hrdag').package(:jar))
    p.merge(project('martus-logi').package(:jar))
    p.merge(project('martus-swing').package(:jar))
    p.merge(project('martus-amplifier').package(:jar))
    p.merge(project('martus-mspa').package(:jar)).include('**/MSPAServer.class')
    p.merge(project('martus-mspa').package(:jar)).include('**/RootHelper.class')

	thirdparty_jars = [
		BCPROV_SPEC,
		ICU4J_SPEC,
    	JAVAX_SERVLET_SPEC,
    	JUNIT_SPEC,
    	LUCENE_SPEC,
    	JETTY_SPEC,
    	PERSIANCALENDAR_SPEC,
    	VELOCITY_DEP_SPEC,
    	XMLRPC_COMMON_SPEC,
    	XMLRPC_SERVER_SPEC,
    	XMLRPC_CLIENT_SPEC,
    	XMLRPC_COMMONS_LOGGING_SPEC,
    	XMLRPC_WS_COMMONS_UTIL_SPEC,
		BLUEPRINT_CORE_SPEC,
		COMMONS_BEANUTILS_SPEC,
		COMMONS_BEANUTILS_CORE_SPEC,
		COMMONS_COLLECTIONS_SPEC,
		COMMONS_CONFIGURATION_SPEC,
		COMMONS_DIGESTER_SPEC,
		COMMONS_LANG_SPEC,
		CONCURRENT_LINKED_HASHMAP_LRU_SPEC,
		JNA_SPEC,
		JNA_PLATFORM_SPEC,
		ORIENT_COMMONS_SPEC,
		ORIENTDB_CORE_SPEC,
		ORIENTDB_GRAPHDB_SPEC,
		ORIENTDB_NATIVEOS_SPEC,
	]
	thirdparty_jars.each do | spec | 
		p.include(artifact(spec), :path=>'ThirdPartyJars')
	end
  end
  
  task 'sha1' => package(:jar) do
    sha = create_server_sha1(jarpath)
    sha_dest_dir = get_sha_dest_dir
    FileUtils::cp(sha, File.join(sha_dest_dir, File::basename(sha)))
  end

  task 'sha2' => package(:jar) do
    sha = create_sha2(jarpath)
    sha_dest_dir = get_sha_dest_dir
    FileUtils::cp(sha, File.join(sha_dest_dir, File::basename(sha)))
  end
  
  def get_sha_dest_dir
    now = today_as_iso_date
    year = now[0,4]
    month = now[5,2]
    day = now[8,2]
    year_dir = File.join(sha_root_dir, year)
    month_dir = File.join(year_dir, month)
    day_dir = File.join(month_dir, day)
    puts "Creating sha dir: #{day_dir}"
    FileUtils.mkdir_p(day_dir)
    return day_dir
  end
  
  task 'push-sha-files' => ['sha1', 'sha2'] do
    cmd = "hg -R #{sha_root_dir} add -S #{sha_root_dir}/."
    puts cmd
    result = `#{cmd} 2>&1`
    if $? != 0
      raise "Error adding new sha files to hg: #{cmd}\n#{result}"
    end

    cmd = "hg -R #{sha_root_dir} commit -m'New sha files from build #{project.version}'"
    puts cmd
    result = `#{cmd} 2>&1`
    if $? != 0
      raise "Error committing new sha files to hg: #{cmd}\n#{result}"
    end

    cmd = "hg -R #{sha_root_dir} push"
    puts cmd
    result = `#{cmd} 2>&1`
    if $? != 0
      raise "Error pushing new sha files to hg: #{cmd}\n#{result}"
    end
  end
  
  def sha_root_dir 
    return File.join(ENV['WORKSPACE'], 'martus-sha')
  end

  task 'everything' => [project('martus-thirdparty').install, package(:jar), 'push-sha-files']
  
	# NOTE: Old build script signed this jar
end
