name = 'martus-client-sourcefiles-zip'

def include_artifacts_in_zip(package, artifacts, directory, extension)
  artifacts.each do | artifact |
    artifact_file = artifact.to_s
    artifact_filename = File.basename(artifact_file)
    package.include(artifact, :as=>"#{directory}/#{artifact_filename}.#{extension}")
  end
end

define name, :layout=>create_layout_with_source_as_source('.') do
	project.group = 'org.martus'
  project.version = ENV['RELEASE_IDENTIFIER']
  input_build_number = ENV['INPUT_BUILD_NUMBER']
  release_build_number = $BUILD_NUMBER
	
	zippath = _("target", "MartusClient-SourceFiles-#{project.version}-#{input_build_number}-#{release_build_number}.zip")
	package(:zip, :file => zippath).path("MartusClient-#{project.version}").tap do | p |
		puts "Defining package for SourceFiles-zip #{project.version} #{input_build_number}-#{release_build_number}"
		input_dir = $attic_dir
		signed_jar = File.join(input_dir, "martus-client-signed-#{input_build_number}.jar")
		source_zip_name = "martus-client-sources-#{input_build_number}.zip"
    		source_zip = "#{input_dir}/#{source_zip_name}"
		p.include(signed_jar, :as=>"martus.jar")
    		p.include(source_zip, :as=>"SourceFiles/martus-sources.zip")
    		p.include(_('martus-build', "BuildFiles", "Documents", "license.txt"))
    		p.include(_('martus-build', "BuildFiles", "Documents", "gpl.txt"))
    		p.include(third_party_client_jars, :path=>'ThirdParty')
    		include_artifacts_in_zip(p, third_party_client_source, "SourceFiles", "zip")
    		include_artifacts_in_zip(p, third_party_client_licenses, "ThirdParty/Licenses", "txt")
	end
	
	sha1path = "#{zippath}.sha1"
	task 'sha1' => zippath do
	  create_sha1(zippath)
	end

  sha2path = "#{zippath}.sha2"
  task 'sha2' => zippath do
    create_sha2(zippath)
  end
  
end
