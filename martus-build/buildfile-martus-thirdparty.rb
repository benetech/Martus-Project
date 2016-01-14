name = "martus-thirdparty"

def jar_file(project_name, directory, jar_name)
	return file(_(project_name, "#{directory}/bin/#{jar_name}"))
end

def license_file(project_name, directory, license_name)
	return file(_(project_name, "#{directory}/license/#{license_name}"))
end

def source_file(project_name, directory, source_name)
	return file(_(project_name, "#{directory}/source/#{source_name}"))
end

def dictionary_file(project_name, directory, dictionary_name)
  return file(_(project_name, "#{directory}/bin/dictionaries/#{dictionary_name}"))
end

define name, :layout=>create_layout_with_source_as_source(name) do
  project.group = 'org.martus'
  project.version = $BUILD_NUMBER

	install do
		puts "Installing martus-thirdparty"

	#libext
	install artifact(BCPROV_SPEC).from(jar_file(name, 'libext/BouncyCastle', 'bcprov-jdk15on-148.jar'))
	install artifact(BCPROV_SOURCE_SPEC).from(source_file(name, 'libext/BouncyCastle', 'bcprov-jdk15on-148.zip'))
	install artifact(BCPROV_LICENSE_SPEC).from(license_file(name, 'libext/BouncyCastle', 'LICENSE.html'))
	install artifact(JUNIT_SPEC).from(jar_file(name, 'libext/JUnit', 'junit-4.11.jar'))
	install artifact(JUNIT_SOURCE_SPEC).from(source_file(name, 'libext/JUnit', 'junit-4.11-sources.zip'))
	install artifact(JUNIT_LICENSE_SPEC).from(license_file(name, 'libext/JUnit', 'LICENSE.txt'))
	
	puts "************************************************************************************"

	#common
	install artifact(PERSIANCALENDAR_SPEC).from(jar_file(name, 'common/PersianCalendar', 'persiancalendar.jar'))
	install artifact(PERSIANCALENDAR_SOURCE_SPEC).from(source_file(name, 'common/PersianCalendar', 'PersianCalendar_2_1.zip'))
	install artifact(PERSIANCALENDAR_LICENSE_SPEC).from(license_file(name, 'common/PersianCalendar', 'gpl.txt'))
	install artifact(LOGI_LICENSE_SPEC).from(license_file(name, 'common/Logi', 'license.html'))

	install artifact(VELOCITY_LICENSE_SPEC).from(license_file(name, 'common/Velocity', 'LICENSE.txt'))
	install artifact(VELOCITY_SOURCE_SPEC).from(source_file(name, 'common/Velocity', 'velocity-1.4-rc1.zip'))
	install artifact(VELOCITY_DEP_LICENSE_SPEC).from(license_file(name, 'common/Velocity', 'LICENSE.txt'))
# TODO: Find velocity-dep source code
#	install artifact(VELOCITY_DEP_SOURCE_SPEC).from(source_file(name, 'common/Velocity', ''))
	install artifact(XMLRPC_COMMON_SPEC).from(jar_file(name, 'common/XMLRPC', 'xmlrpc-common-3.1.3.jar'))
	install artifact(XMLRPC_SERVER_SPEC).from(jar_file(name, 'common/XMLRPC', 'xmlrpc-server-3.1.3.jar'))
	install artifact(XMLRPC_CLIENT_SPEC).from(jar_file(name, 'common/XMLRPC', 'xmlrpc-client-3.1.3.jar'))
	install artifact(XMLRPC_COMMONS_LOGGING_SPEC).from(jar_file(name, 'common/XMLRPC', 'commons-logging-1.1.jar'))
	install artifact(XMLRPC_WS_COMMONS_UTIL_SPEC).from(jar_file(name, 'common/XMLRPC', 'ws-commons-util-1.0.2.jar'))
	install artifact(XMLRPC_SOURCE_SPEC).from(source_file(name, 'common/XMLRPC', 'apache-xmlrpc-3.1.3-src.zip'))
	install artifact(XMLRPC_LICENSE_SPEC).from(license_file(name, 'common/XMLRPC', 'LICENSE.txt'))
# TODO: Find ICU4J source code
#	install artifact(ICU4J_SOURCE_SPEC).from(source_file(name, 'common/PersianCalendar', 'icu4j_3_2_license.html'))
	install artifact(ICU4J_LICENSE_SPEC).from(license_file(name, 'common/PersianCalendar', 'icu4j_3_2_license.html'))

	install artifact(ORCHID_SPEC).from(jar_file(name, 'common/orchid', "orchid-#{$ORCHID_VERSION}.25f1ae1.jar"))
	install artifact(ORCHID_SOURCE_SPEC).from(source_file(name, 'common/orchid', "orchid-#{$ORCHID_VERSION}.25f1ae1-src.zip"))
	install artifact(ORCHID_LICENSE_SPEC).from(license_file(name, 'common/orchid', 'LICENSE'))
	
	install artifact(JAVAROSA_SPEC).from(jar_file(name, 'common/JavaRosa', "javarosa-libraries.jar"))
	install artifact(JAVAROSA_SOURCE_SPEC).from(source_file(name, 'common/JavaRosa', "src.zip"))
	install artifact(JAVAROSA_LICENSE_SPEC).from(license_file(name, 'common/JavaRosa', 'LICENSE'))
	install artifact(KXML_SPEC).from(jar_file(name, 'common/JavaRosa', 'kxml2-2.3.0.jar'))

	#client
	install artifact(LAYOUTS_SPEC).from(jar_file(name, 'client/jhlabs', 'layouts.jar'))
	install artifact(LAYOUTS_SOURCE_SPEC).from(source_file(name, 'client/jhlabs', 'layouts.zip'))
	install artifact(LAYOUTS_LICENSE_SPEC).from(license_file(name, 'client/jhlabs', 'LICENSE.TXT'))
	install artifact(RHINO_SPEC).from(jar_file(name, 'client/RhinoJavaScript', 'js.jar'))
	install artifact(RHINO_SOURCE_SPEC).from(source_file(name, 'client/RhinoJavaScript', 'Rhino-src.zip'))
	install artifact(RHINO_LICENSE_SPEC).from(license_file(name, 'client/RhinoJavaScript', 'license.txt'))
	install artifact(JORTHO_SPEC).from(jar_file(name, 'client/jortho', 'jortho-0.5.jar'))
  install artifact(JORTHO_SOURCE_SPEC).from(source_file(name, 'client/jortho', 'JOrtho_0.5.zip'))
  install artifact(JORTHO_LICENSE_SPEC).from(license_file(name, 'client/jortho', 'license-jortho.txt'))
  install artifact(JORTHO_ENGLISH_SPEC).from(dictionary_file(name, 'client/jortho', 'dictionary_en.ortho'))
  install artifact(JORTHO_SPANISH_SPEC).from(dictionary_file(name, 'client/jortho', 'dictionary_es.ortho'))
  install artifact(JFREECHART_SPEC).from(jar_file(name, 'client/JFreeChart', 'jfreechart-1.0.14.jar'))
  install artifact(JFREECHART_SOURCE_SPEC).from(source_file(name, 'client/JFreeChart', 'jfreechart-1.0.14.zip'))
  install artifact(JFREECHART_LICENSE_SPEC).from(license_file(name, 'client/JFreeChart', 'License-JFreeChart.txt'))
  install artifact(JCOMMON_SPEC).from(jar_file(name, 'client/JFreeChart', 'jcommon-1.0.17.jar'))
  install artifact(JCOMMON_SOURCE_SPEC).from(source_file(name, 'client/JFreeChart', 'jcommon-1.0.17.zip'))
  install artifact(JCOMMON_LICENSE_SPEC).from(license_file(name, 'client/JFreeChart', 'License-JCommon.txt'))
	#NOTE: Would like to include license for khmer fonts, but there are no license files
  #NOTE: Would like to include license for Armenian fonts, but there are no license files
	#NOTE: Would like to include license for NSIS installer, but don't see any
	#TODO: Need to include client license files for Sun Java (after upgrading to Java 6)

	#server
	install artifact(JETTY_SOURCE_SPEC).from(source_file(name, 'server/Jetty', 'jetty-4.2.24-all.tar.gz'))
	license_task = extract_artifact_entry_task(JETTY_SPEC, 'org/mortbay/LICENSE.html')
	install artifact(JETTY_LICENSE_SPEC).from(license_task)
	install artifact(LUCENE_SOURCE_SPEC).from(source_file(name, 'server/Lucene', 'lucene-1.3-rc1-src.zip'))
	license_task = extract_artifact_entry_task(LUCENE_SOURCE_SPEC, 'lucene-1.3-rc1-src/LICENSE.txt')
	install artifact(LUCENE_LICENSE_SPEC).from(license_task)
	# TODO: Should include source/license for javax.servlet.jar
	# TODO: Should include source/license for javax.mail.jar
	install artifact(BLUEPRINT_CORE_SPEC).from(jar_file(name, 'server/OrientDB', 'blueprints-core-2.5.0.jar'))
	install artifact(COMMONS_BEANUTILS_SPEC).from(jar_file(name, 'server/OrientDB', 'commons-beanutils-1.7.0.jar'))
	install artifact(COMMONS_BEANUTILS_CORE_SPEC).from(jar_file(name, 'server/OrientDB', 'commons-beanutils-core-1.8.0.jar'))
	install artifact(COMMONS_COLLECTIONS_SPEC).from(jar_file(name, 'server/OrientDB', 'commons-collections-3.2.1.jar'))
	install artifact(COMMONS_CONFIGURATION_SPEC).from(jar_file(name, 'server/OrientDB', 'commons-configuration-1.6.jar'))
	install artifact(COMMONS_DIGESTER_SPEC).from(jar_file(name, 'server/OrientDB', 'commons-digester-1.8.jar'))
	install artifact(COMMONS_LANG_SPEC).from(jar_file(name, 'server/OrientDB', 'commons-lang-2.4.jar'))
	install artifact(CONCURRENT_LINKED_HASHMAP_LRU_SPEC).from(jar_file(name, 'server/OrientDB', 'concurrentlinkedhashmap-lru-1.4.jar'))
	install artifact(JNA_SPEC).from(jar_file(name, 'server/OrientDB', 'jna-4.0.0.jar'))
	install artifact(JNA_PLATFORM_SPEC).from(jar_file(name, 'server/OrientDB', 'jna-platform-4.0.0.jar'))
	install artifact(ORIENT_COMMONS_SPEC).from(jar_file(name, 'server/OrientDB', 'orient-commons-1.7.4.jar'))
	install artifact(ORIENTDB_CORE_SPEC).from(jar_file(name, 'server/OrientDB', 'orientdb-core-1.7.4.jar'))
	install artifact(ORIENTDB_GRAPHDB_SPEC).from(jar_file(name, 'server/OrientDB', 'orientdb-graphdb-1.7.4.jar'))
	install artifact(ORIENTDB_NATIVEOS_SPEC).from(jar_file(name, 'server/OrientDB', 'orientdb-nativeos-1.7.4.jar'))
	
	install artifact(ORIENTDB_SOURCE_SPEC).from(source_file(name, 'server/OrientDB', 'orientdb-1.7.4-source.zip'))
	install artifact(ORIENTDB_LICENSE_SPEC).from(license_file(name, 'server/OrientDB', 'license.txt'))
	end
	
  package(:zip, :file => _('target', "martus-thirdparty-#{project.version}.zip")).tap do | p |
    p.include(artifact(JUNIT_SPEC), :path=>'ThirdParty')
    p.include(artifact(BCPROV_SPEC), :path=>'ThirdParty')
    p.include(artifact(PERSIANCALENDAR_SPEC), :path=>'ThirdParty')
    p.include(artifact(VELOCITY_DEP_SPEC), :path=>'ThirdParty')
    p.include(artifact(XMLRPC_COMMON_SPEC), :path=>'ThirdParty')
    p.include(artifact(XMLRPC_CLIENT_SPEC), :path=>'ThirdParty')
    p.include(artifact(XMLRPC_SERVER_SPEC), :path=>'ThirdParty')
    p.include(artifact(XMLRPC_COMMONS_LOGGING_SPEC), :path=>'ThirdParty')
    p.include(artifact(XMLRPC_WS_COMMONS_UTIL_SPEC), :path=>'ThirdParty')
    p.include(artifact(ICU4J_SPEC), :path=>'ThirdParty')
    p.include(artifact(LAYOUTS_SPEC), :path=>'ThirdParty')
    p.include(artifact(RHINO_SPEC), :path=>'ThirdParty')
    p.include(artifact(JCOMMON_SPEC), :path=>'ThirdParty')
    p.include(artifact(JFREECHART_SPEC), :path=>'ThirdParty')
    p.include(artifact(ORCHID_SPEC), :path=>'ThirdParty')
    p.include(artifact(JAVAROSA_SPEC), :path=>'ThirdParty')
    p.include(artifact(KXML_SPEC), :path=>'ThirdParty')
  end
end
