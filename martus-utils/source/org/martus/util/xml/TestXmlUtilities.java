package org.martus.util.xml;

import org.martus.util.TestCaseEnhanced;

public class TestXmlUtilities extends TestCaseEnhanced
{
	public TestXmlUtilities(String name)
	{
		super(name);
	}
	
	public void testStripXmlHeader()
	{
		verifyXmlHeaderStripped("", "");
		verifyXmlHeaderStripped("", "<?xml version=\"1.1\"?>");
		verifyXmlHeaderStripped("", "<?xml version=\"1.0\"?>");
		verifyXmlHeaderStripped("", "<?xml version=\"1.0\" ?>");
		verifyXmlHeaderStripped("", "<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		verifyXmlHeaderStripped("", "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\" ?>");
	}

	private void verifyXmlHeaderStripped(String expectedValue, String valueToStripHeaderFrom)
	{
		assertEquals("Xml header not removed?", expectedValue, XmlUtilities.stripXmlHeader(valueToStripHeaderFrom));
	}
	
	public void testStripXmlElement()
	{
		verifyStripXmlElement("", "", "");
		verifyStripXmlElement("<someElement>value</someElement>", "<someElement>value</someElement>", "");
		verifyStripXmlElement("value", "<someElement>value</someElement>", "someElement");
		verifyStripXmlElement("value", "<sOMeElement>value</SomeElement>", "SOmeElement");
		verifyStripXmlElement("value", "< someElement >value< / someElement >", "someElement");
		verifyStripXmlElement("<someElement>value</someElement>", "<someElement><nestedElement>value</nestedElement></someElement>", "nestedElement");
		verifyStripXmlElement("value", "<someElement attribute=\"value\">value</someElement>", "someElement");
	}

	private void verifyStripXmlElement(String expectedResult, String valueToStripFrom, String elementNameToStrip)
	{
		assertEquals("Xml element stripped incorrectly?", expectedResult, XmlUtilities.stripXmlStartEndElements(valueToStripFrom, elementNameToStrip));
	}
}
