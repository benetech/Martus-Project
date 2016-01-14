if ARGV.size < 1
	puts "Usage: push-client-google <branch-name>"
	exit 1
end

branch_name = ARGV[0]
puts "Pushing client branch #{branch_name} to google"

projects = [
'martus-build',
'martus-client',
'martus-clientside',
'martus-common',
'martus-docs',
'martus-hrdag',
'martus-jar-verifier',
'martus-js-xml-generator',
'martus-logi',
'martus-meta',
'martus-swing',
'martus-thirdparty',
'martus-utils',
]

this_script = File.absolute_path(__FILE__)
this_dir = File.dirname(this_script)
build_dir = File.dirname(this_dir)
umbrella_dir = File.dirname(build_dir)
puts umbrella_dir

projects.each do | project_name |
	dir = "#{umbrella_dir}/#{project_name}"
	cmd = "hg outgoing --repository #{dir} --branch #{branch_name} google"
	puts cmd
	`#{cmd}`
	result = $?
	puts result
	if !result.success?
		next
	end
	
	cmd = "hg push --repository #{dir} --branch #{branch_name} --new-branch google"
	puts cmd
	`#{cmd}`
	result = $?
	if result != 0
		puts "Error: #{result}"
		exit 1
	end
end

