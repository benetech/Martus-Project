name = "martus-mlp"

define name, :layout=>create_layout_with_source_as_source(name) do
	project.group = 'org.martus'
  project.version = $BUILD_NUMBER

	#TODO: Need to generate MLP files
	#TODO: Be sure Burmese MLP is included
	#TODO: Each MLP jar needs to be signed
end
