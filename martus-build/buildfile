# Usage:
#   To build an unsigned client jar: 
#		buildr clean checkout martus-client-unsigned:package martus-client-mac-dmg:build test=no

require 'fileutils'
require 'English'
require 'tmpdir'

main_dir = File.dirname(__FILE__)
require "#{main_dir}/buildfile-martus"
require "#{main_dir}/buildfile-martus-api"
require "#{main_dir}/buildfile-martus-thirdparty"
require "#{main_dir}/buildfile-martus-logi"
require "#{main_dir}/buildfile-martus-hrdag"
require "#{main_dir}/buildfile-martus-utils"
require "#{main_dir}/buildfile-martus-swing"
require "#{main_dir}/buildfile-martus-common"
require "#{main_dir}/buildfile-martus-js-xml-generator"
require "#{main_dir}/buildfile-martus-jar-verifier"
require "#{main_dir}/buildfile-martus-clientside"
require "#{main_dir}/buildfile-martus-mlp"
require "#{main_dir}/buildfile-martus-client"
require "#{main_dir}/buildfile-martus-mspa"
require "#{main_dir}/buildfile-martus-amplifier"
require "#{main_dir}/buildfile-martus-server"
require "#{main_dir}/buildfile-martus-meta"

require "#{main_dir}/buildfile-martus-client-linux-zip"
require "#{main_dir}/buildfile-martus-client-nsis-zip"
require "#{main_dir}/buildfile-martus-client-nsis-single"
require "#{main_dir}/buildfile-martus-client-nsis-upgrade"
require "#{main_dir}/buildfile-martus-client-nsis-cd"
require "#{main_dir}/buildfile-martus-client-nsis-pieces"
require "#{main_dir}/buildfile-martus-client-iso"
require "#{main_dir}/buildfile-martus-mspa-client-zip"
require "#{main_dir}/buildfile-martus-client-mac-dmg"

#TODO: Need to set up proper dependency chains
#TODO: Need to eliminate optional files from Java 6 runtime
#TODO: Need to make sure all built artifacts are archived
#TODO: Should create a server tarball that contains source code and licenses

#NOTE: Old script created amplifier tarball (build.xml#release) but
# Scott confirms it is not needed/used
