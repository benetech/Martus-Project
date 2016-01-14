/*

Martus(TM) is a trademark of Beneficent Technology, Inc. 
This software is (c) Copyright 2001-2015, Beneficent Technology, Inc.

Martus is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either
version 2 of the License, or (at your option) any later
version with the additions and exceptions described in the
accompanying Martus license file entitled "license.txt".

It is distributed WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, including warranties of fitness of purpose or
merchantability.  See the accompanying Martus License and
GPL license for more details on the required license terms
for this software.

You should have received a copy of the GNU General Public
License along with this program; if not, write to the Free
Software Foundation, Inc., 59 Temple Place - Suite 330,
Boston, MA 02111-1307, USA.

*/
package org.martus.client.core;

public interface TestBulletinFromXFormsLoaderConstants
{
	
	public static final  String XFORMS_MODEL_WITH_GROUP_WITHOUT_LABEL = 
		"<h:html xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns=\"http://www.w3.org/2002/xforms\" xmlns:jr=\"http://openrosa.org/javarosa\" xmlns:h=\"http://www.w3.org/1999/xhtml\" xmlns:ev=\"http://www.w3.org/2001/xml-events\" >" +
			    "<h:head>" +
			        "<h:title>Sample XForms for Testing</h:title>" +
			        "<model>" +
			            "<instance>" +
			            		"<nm id=\"SampleForUnitTesting\" >" +
			    					"<name/>" +
			    				"</nm>" +
			            "</instance>" +
			            "<bind nodeset=\"/nm/name\" type=\"string\" />" +
			        "</model>" +
			    "</h:head>" +
			    "<h:body>" +
			        "<group appearance=\"field-list\" >" +
			        "<input ref=\"name\" >" +
						"<label>Some Random Label</label>" +
						"</input>" +
			        "</group>" +
			    "</h:body>" +
			"</h:html>";
	
	public static final String XFORMS_INSTANCE_WITH_SINGLE_INPUT = "<xforms_instance>" +
			   "<nm id=\"SampleForUnitTesting\">" +
			      "<name>Some Randome value</name>" +
			   "</nm>" +
			"</xforms_instance>";
	
	public static final String COMPLETE_XFORMS_MODEL = 
															"<h:html xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns=\"http://www.w3.org/2002/xforms\" xmlns:jr=\"http://openrosa.org/javarosa\" xmlns:h=\"http://www.w3.org/1999/xhtml\" xmlns:ev=\"http://www.w3.org/2001/xml-events\" >" + 
															"    <h:head>" + 
															"        <h:title>secureApp Prototype</h:title>" + 
															"        <model>" + 
															"            <instance>" + 
															"                <nm id=\"VitalVoices\" >" + 
															"                    <name>John Doe</name>" + 
															"                    <nationality>China</nationality>" + 
															"                    <date></date>" + 
															"                    <age></age>" + 
															"                    <sourceOfRecordInformation></sourceOfRecordInformation>" + 
															"                    <eventLocation></eventLocation>" + 
															"                    <anonymous></anonymous>" + 
															"                    <additionalInfo></additionalInfo>" + 
															"                    <testify></testify>" + 
															"                    <victim_information>" + 
															"                        <victimFirstName></victimFirstName>" + 
															"                        <victimLastName></victimLastName>" + 
															"                        <sex></sex>" + 
															"                    </victim_information>" + 
															"                </nm>" + 
															"            </instance>" + 
															"            <bind nodeset=\"/nm/name\" type=\"string\" required=\"true()\" ></bind>" + 
															"            <bind nodeset=\"/nm/nationality\" type=\"string\" required=\"true()\" ></bind>" + 
															"            <bind jr:constraintMsg=\"No dates before 2000-01-01 allowed\" nodeset=\"/nm/date\" constraint=\". >= date('2000-01-01')\" type=\"date\" ></bind>" + 
															"            <bind nodeset=\"/nm/age\" type=\"integer\" ></bind>" + 
															"            <bind nodeset=\"/nm/sourceOfRecordInformation\" type=\"select1\" ></bind>" + 
															"            <bind nodeset=\"/nm/victim_information/victimFirstName\" type=\"string\" ></bind>" + 
															"            <bind nodeset=\"/nm/victim_information/victimLastName\" type=\"string\" ></bind>" + 
															"            <bind nodeset=\"/nm/victim_information/sex\" type=\"select1\" ></bind>" + 
															"            <bind nodeset=\"/nm/eventLocation\" type=\"select1\" ></bind>" + 
															"            <bind nodeset=\"/nm/anonymous\" type=\"select\" ></bind>" + 
															"            <bind nodeset=\"/nm/additionalInfo\" type=\"select\" ></bind>" + 
															"            <bind nodeset=\"/nm/testify\" type=\"select\" ></bind>" + 
															"        </model>" + 
															"    </h:head>" + 
															"    <h:body>" + 
															"        <group appearance=\"field-list\" >" + 
															"            <label>Section 1 (Text fields)</label>" + 
															"            <input ref=\"name\" >" + 
															"                <label>What is your name:</label>" + 
															"                <hint>(required)</hint>" + 
															"            </input>" + 
															"            <input ref=\"nationality\" >" + 
															"                <label>What is your country of origin:</label>" + 
															"                <hint>(required)</hint>" + 
															"            </input>" + 
															"            <input ref=\"age\" >" + 
															"                <label>What is your age:</label>" + 
															"            </input>" + 
															"        </group>" + 
															"        <group appearance=\"field-list\" >" + 
															"            <label>Section 2 (Date field)</label>" + 
															"            <input ref=\"date\" >" + 
															"                <label>Date of incident</label>" + 
															"                <hint>(No dates before 2000-01-01 allowed)</hint>" + 
															"            </input>" + 
															"        </group>" + 
															"        <group appearance=\"field-list\" >" + 
															"            <label>Section 3 (Drop down lists)</label>" + 
															"            <select1 ref=\"sourceOfRecordInformation\" appearance=\"minimal\" >" + 
															"                <label>Source of record information</label>" + 
															"                <item>" + 
															"                    <label>Media/Press</label>" + 
															"                    <value>mediaPress</value>" + 
															"                </item>" + 
															"                <item>" + 
															"                    <label>Legal Report</label>" + 
															"                    <value>legalReport</value>" + 
															"                </item>" + 
															"                <item>" + 
															"                    <label>Personal Interview</label>" + 
															"                    <value>personalInterview</value>" + 
															"                </item>" + 
															"                <item>" + 
															"                    <label>Other</label>" + 
															"                    <value>other</value>" + 
															"                </item>" + 
															"            </select1>" + 
															"            <select1 ref=\"eventLocation\" appearance=\"minimal\" >" + 
															"                <label>Event Location</label>" + 
															"                <item>" + 
															"                    <label>Region 1</label>" + 
															"                    <value>r1</value>" + 
															"                </item>" + 
															"                <item>" + 
															"                    <label>Region 2</label>" + 
															"                    <value>r2</value>" + 
															"                </item>" + 
															"                <item>" + 
															"                    <label>Region 3</label>" + 
															"                    <value>r3</value>" + 
															"                </item>" + 
															"            </select1>" + 
															"        </group>" + 
															"        <group appearance=\"field-list\" >" + 
															"            <label>Section 4 (Check boxes)</label>" + 
															"            <select ref=\"anonymous\" >" + 
															"                <label>Does interviewee wish to remain anonymous?</label>" + 
															"                <item>" + 
															"                    <label></label>" + 
															"                    <value>1</value>" + 
															"                </item>" + 
															"            </select>" + 
															"            <select ref=\"additionalInfo\" >" + 
															"                <label>Is interviewee willing to give additional information if needed?</label>" + 
															"                <item>" + 
															"                    <label></label>" + 
															"                    <value>1</value>" + 
															"                </item>" + 
															"            </select>" + 
															"            <select ref=\"testify\" >" + 
															"                <label>Is interviewee willing to testify?</label>" + 
															"                <item>" + 
															"                    <label></label>" + 
															"                    <value>1</value>" + 
															"                </item>" + 
															"            </select>" + 
															"        </group>" + 
															"        <group>" + 
															"            <label>Section 5 (Repeating group of fields)</label>" + 
															"            <repeat nodeset=\"/nm/victim_information\" >" + 
															"                <input ref=\"victimFirstName\" >" + 
															"                    <label>Victim first name</label>" + 
															"                </input>" + 
															"                <input ref=\"victimLastName\" >" + 
															"                    <label>Victim last name</label>" + 
															"                </input>" + 
															"                <select1 ref=\"sex\" appearance=\"minimal\" >" + 
															"                    <label>Victim Sex</label>" + 
															"                    <item>" + 
															"                        <label>Female</label>" + 
															"                        <value>female</value>" + 
															"                    </item>" + 
															"                    <item>" + 
															"                        <label>Male</label>" + 
															"                        <value>male</value>" + 
															"                    </item>" + 
															"                    <item>" + 
															"                        <label>Other</label>" + 
															"                        <value>other</value>" + 
															"                    </item>" + 
															"                </select1>" + 
															"            </repeat>" + 
															"        </group>" + 
															"    </h:body>" + 
															"</h:html>";
		
		
	public static final String COMPLETE_XFORMS_INSTANCE = 		"<nm id=\"VitalVoices\" >" + 
																"<name>John Doe</name>" + 
																"<nationality>China</nationality>" + 
																"<date>2015-03-27</date>" + 
																"<age>99</age>" + 
																"<sourceOfRecordInformation>legalReport</sourceOfRecordInformation>" + 
																"<eventLocation>r1</eventLocation>" + 
																"<anonymous>1</anonymous>" + 
																"<additionalInfo></additionalInfo>" + 
																"<testify>1</testify>" + 
																"<victim_information>" + 
																"	<victimFirstName>Vic 1 jon</victimFirstName>" + 
																"	<victimLastName>Vic 1 doe</victimLastName>" + 
																"	<sex>male</sex>" + 
																"</victim_information>" + 
																"<victim_information>" + 
																"	<victimFirstName>Vic 2 jill</victimFirstName>" + 
																"	<victimLastName>Vic 2 doe</victimLastName>" + 
																"	<sex>male</sex>" + 
																"</victim_information>" + 
																"<victim_information>" + 
																"	<victimFirstName>Vic 3 abe</victimFirstName>" + 
																"	<victimLastName>Vic 3 abel</victimLastName>" + 
																"	<sex>other</sex>" + 
																"</victim_information>" + 
																"</nm>";

	public static final String AGE_LABEL = "AGE";
	public static final String AGE_VALUE = "30";

	public static final String XFORMS_MODEL_INTERGER_FIELD =
			"<xforms_model>" +
				"<h:html xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns=\"http://www.w3.org/2002/xforms\" xmlns:jr=\"http://openrosa.org/javarosa\" xmlns:h=\"http://www.w3.org/1999/xhtml\" xmlns:ev=\"http://www.w3.org/2001/xml-events\" >" +
					"<h:head>"+
						"<h:title>secureApp Prototype</h:title>"+
						"<model>"+
							"<instance>"+
								"<nm id=\"VitalVoices\" >"+
									"<age></age>"+
								"</nm>"+
							"</instance>"+				""+
							"<bind nodeset=\"/nm/age\" type=\"integer\" ></bind>"+
						"</model>"+
					"</h:head>"+
				"<h:body>"+
					"<group appearance=\"field-list\" >"+
						"<label>Section 1 (Text fields)</label>"+
						"<input ref=\"age\" >"+
							"<label>"+ AGE_LABEL +"</label>"+
						"</input>"+
					"</group>"+
				"</h:body>"+
			"</h:html>" +
		"</xforms_model>";

	public static final String XFORMS_INSTANCE_INTERGER_FIELD = 
			"<xforms_instance>" +
				"<nm id=\"VitalVoices\">"+
					"<age>" + AGE_VALUE + "</age>"+
				"</nm>"+
			"</xforms_instance>";

}
