name = 'martus-api'

# FIXME: This is a crude hack. This file is loaded right after buildfile-martus
# (which sets the Java version to 8). So if we set it to 7 here, it will get used 
# by all the other buildfiles which are included after this one.
# Comment out for normal builds; uncomment for api builds
#$JAVAC_VERSION = '7'

define name, :layout=>create_layout_with_source_as_source('.') do
	project.group = 'org.martus'
	project.version =$BUILD_NUMBER
	
	jarname = _('target', "martus-api-#{project.version}.jar")
	package(:zip, :file=>jarname).tap do | p |
		p.merge(project('martus-common').package).include('org/martus/common/*.class')
		p.merge(project('martus-common').package).include('org/martus/common/bulletin/*.class')
		p.merge(project('martus-common').package).include('org/martus/common/bulletinstore/*.class')
		p.merge(project('martus-common').package).include('org/martus/common/crypto/*.class')
		p.merge(project('martus-common').package).include('org/martus/common/database/*.class')
		p.merge(project('martus-common').package).include('org/martus/common/field/*.class')
		p.merge(project('martus-common').package).include('org/martus/common/fieldspec/*.class')
		p.merge(project('martus-common').package).include('org/martus/common/network/*.class')
		p.merge(project('martus-common').package).include('org/martus/common/network/mirroring/*.class')
		p.merge(project('martus-common').package).include('org/martus/common/packet/*.class')
		p.merge(project('martus-common').package).include('org/martus/common/utilities/*.class')
		p.merge(project('martus-common').package).include('org/martus/common/xmlrpc/*.class')
		p.merge(project('martus-common').package).include('org/miradi/utils/*.class')
		p.merge(project('martus-utils').package).include('org/martus/util/*.class')
		p.merge(project('martus-utils').package).include('org/martus/util/inputstreamwithseek/*.class')
		p.merge(project('martus-utils').package).include('org/martus/util/language/*.class')
		p.merge(project('martus-utils').package).include('org/martus/util/xml/*.class')
		p.merge(project('martus-clientside').package).include('org/martus/clientside/ClientPortOverride.class')
		p.merge(project('martus-clientside').package).include('org/martus/clientside/ClientSideNetwork*.class')
	end

end
