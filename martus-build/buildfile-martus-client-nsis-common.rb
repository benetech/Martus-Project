def get_nsis_contrib_language_directory(base)
	return File.join(base, 'BuildFiles', 'Windows', 'Win32_NSIS_Unicode', 'NSIS', 'Contrib', 'Language files')
end

def copy_files_to_nsis(from)
	nlf_files = Dir.glob(File.join(from, '*.nlf'))
	nsh_files = Dir.glob(File.join(from, '*.nsh'))
	nsis_files = [] + nlf_files + nsh_files
	nsis_language_directory = File.join($full_nsis_dir, 'Contrib', 'Language files')
	puts "Updating NSIS files, copying from #{from} to #{nsis_language_directory}"
	FileUtils.cp(nsis_files, nsis_language_directory, {:preserve => true})
end

def run_nsis_task(nsis_zip, nsi_name, exe_name)
	puts "Unzipping #{nsis_zip}..."
	previous_pwd = Dir.pwd
    
    unzipped_dir = 'FilesForWindowsInstaller'
	dest_dir = _(:target, :temp, unzipped_dir)
	FileUtils.rm_rf dest_dir
	FileUtils.mkdir_p(dest_dir)
	unzip_file(nsis_zip, dest_dir)
	
	FileUtils.chdir File.join(dest_dir, $nsis_script_dir)
	puts "dest_dir is #{dest_dir}"
	puts "Running makensis from: #{Dir.pwd}"
	nsis_cmd = "#{$nsis_command} #{nsi_name}"
	puts "Running: #{nsis_cmd}"
	STDOUT.flush
	error_output = `#{nsis_cmd}`
	status = $?
	if status.exitstatus > 0
		error = error_output.split("\n").join("\n  ")
		raise "Error running makensis #{status.exitstatus}: #{error}"
	end
	puts 'Finished makensis'
	
	FileUtils.mkdir_p _(:target)
	mv exe_name, _(:target, exe_name)
	
	# Uncomment to clean up, but leave commented out for easier debugging 
	#FileUtils.rm_rf dest_dir

	FileUtils.chdir previous_pwd
end
