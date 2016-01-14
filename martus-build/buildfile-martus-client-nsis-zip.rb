name = 'martus-client-nsis-zip'

define name, :layout=>create_layout_with_source_as_source('.') do
  project.group = 'org.martus'
  project.version = ENV['RELEASE_IDENTIFIER'] || 'MISSING_PROJECT_VERSION'
  input_build_number = ENV['INPUT_BUILD_NUMBER']
  release_build_number = $BUILD_NUMBER


  combined_license_file = _(:target, :temp, 'combined-license.txt')
  file combined_license_file do
    FileUtils.mkdir_p File.dirname(combined_license_file)
    martus_license = File.readlines(_('martus-build', 'BuildFiles', 'Documents', 'license.txt')).join
    gpl = File.readlines(_('martus-build', 'BuildFiles', 'Documents', 'gpl.txt')).join
    File.open(combined_license_file, "w") do | out |
      out.write(martus_license)
      out.write("\n\n\t**********************************\n\n")
      out.write(gpl)
    end
  end

	attic_dir = $attic_dir
	signed_jar = File.join(attic_dir, "martus-client-signed-#{input_build_number}.jar")
	source_zip = File.join(attic_dir, "martus-client-sources-#{input_build_number}.zip")
    jre_tree = _(:target, :temp, 'jre8')
	
	
	package(:zip).include(signed_jar, :as=>"martus.jar")
	package(:zip).include(source_zip, :path=>'BuildFiles/SourceFiles')
	  
	package(:zip).include(_('martus-build', 'BuildFiles', '*.txt'), :path=>'BuildFiles')
	package(:zip).include(_('martus-jar-verifier/*.txt'), :path=>'BuildFiles/Verifier')
	package(:zip).include(_('martus-jar-verifier/*.bat'), :path=>'BuildFiles/Verifier')
	package(:zip).include(_('martus-jar-verifier/source'), :path=>'BuildFiles/Verifier')
	#TODO: Need to include MartusWin32SetupLauncher?
	package(:zip).include(_('martus-build', 'BuildFiles', 'ProgramFiles'), :path=>'BuildFiles')
	package(:zip).include(_('martus-build', 'BuildFiles', 'SampleDir'), :path=>'BuildFiles')
	package(:zip).include(combined_license_file, :path=>'BuildFiles')
	package(:zip).include(_('martus-build', 'BuildFiles', 'Windows', $nsis_script_dir))
	package(:zip).include(_('martus-build', 'BuildFiles', 'Fonts'), :path=>'BuildFiles')
	package(:zip).include(_('martus-build', 'BuildFiles', 'Fonts', '*.ttf'), :path=>'BuildFiles/jre8/jre/lib/fonts/fallback')

	package(:zip).tap do | zip |
		puts "Adding files to #{zip}"
		include_artifacts(zip, third_party_client_source, 'BuildFiles/SourceFiles') 
	    
	    #TODO: Need to include MartusSetupLauncher?
	  
	    include_artifacts(zip, third_party_client_jars, 'BuildFiles/Jars')
	    include_artifacts(zip, [_('martus-build', 'BuildFiles', 'Documents')], 'BuildFiles')
	    include_artifacts(zip, third_party_client_licenses, 'BuildFiles/Documents/Licenses')
	    zip.include(jre_tree, :path=>'BuildFiles')
	end
	
	file jre_tree do
	    jre_zip = _('martus-build', 'BuildFiles', 'JavaRedistributables', 'Win32', 'jre8u40.zip')
	    FileUtils.rm_rf(jre_tree)
	    FileUtils.mkdir_p(jre_tree)
	    unzip_file(jre_zip, jre_tree)
	end
end
