package org.martus.client.bulletinstore.converters;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TimeZone;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class BulletinsAsXmlToCsvConverter
{
	private static String DELIMITER = ",";
	private static String DATE_DELIMETER = "#";
	private static final String EOLN = System.getProperty("line.separator");

	private static final Object DATE_TYPE = "DATE";
	private static final String DATE_PREFIX = "Date" + DATE_DELIMETER;
	private static final Object RANGE_TYPE = "DATERANGE";
	private static final String RANGE_PREFIX = "DateRangeStart" + DATE_DELIMETER;

	private InputSource xmlInputSource;
	private String outFileName;
	private Map<String, String> metaHeaderMap = new LinkedHashMap();
	private Map<String, String> headerMap = new LinkedHashMap();
	private Map<String, ArrayList<String>> gridFieldNames = new LinkedHashMap();
	private Map<String, ArrayList<String>> gridDataMap = new LinkedHashMap();

	private String csvOutput = "";

	public BulletinsAsXmlToCsvConverter(InputSource inputSourceToUse, String anOutFileName)
	{
		xmlInputSource = inputSourceToUse;
		this.outFileName = anOutFileName;
	}

	public String getCsvAsStringOutput() 
	{
		return this.csvOutput;
	}

	public Map<String, String> getMetaHeaders() 
	{ 
		return this.metaHeaderMap; 
	}

	public Map<String, String> getHeaders() 
	{
		return this.headerMap;
	}

	public String getOutFileName() 
	{ 
		return this.outFileName; 
	}

	public String parseAndTranslateFile()
	{
		String retStr = "";
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = null;
		Document doc = null;
		try 
		{
			db = dbf.newDocumentBuilder();
		}
		catch (ParserConfigurationException e1) {
			e1.printStackTrace();
			return "Text Translation Fail Builder";
		}
		try
		{
			doc = db.parse(getInputSource());
		} 
		catch (SAXException e) 
		{
			e.printStackTrace();
			return "Text Translation Fail SAX exception";
		}
		catch (IOException e) 
		{
			e.printStackTrace();
			return "Text Translation Fail IO Error";
		}

		if (doc != null) {
			Node baseNode = doc.getFirstChild();

			if (baseNode.getNodeName().equalsIgnoreCase("MartusBulletins")) 
			{
				retStr = readHeaders(doc);
				addHeadersToOutput();
				this.csvOutput += EOLN;

				NodeList bulletins = doc.getElementsByTagName("MartusBulletin");
				for (int n = 0; n < bulletins.getLength(); n++) 
				{
					Element bulletin = (Element)bulletins.item(n);
					addMetaDataToOutput(bulletin);
					addFieldsToOutput(bulletin);
					this.csvOutput += EOLN;
				}
			}
			else 
			{
				retStr = "Not Martus XML file";
			}
		} 
		else 
		{
			retStr = "Could not parse file";
		}

		return retStr;
	}

	private String readHeaders(Document doc)
	{
		readMetaHeaders(doc);
		NodeList specList = doc.getElementsByTagName("MainFieldSpecs");
		int len = specList.getLength();

		for (int spec = 0; spec < len; spec++) 
		{
			Node fields = specList.item(spec);
			NodeList fieldList = fields.getChildNodes();
			int numFields = fieldList.getLength();
			for (int fieldNum = 0; fieldNum < numFields; fieldNum++) 
			{
				Node subNode = fieldList.item(fieldNum);
				if (subNode.getNodeName().equals("Field"))
				{
					NamedNodeMap map = subNode.getAttributes();
					String type = null;
					NodeList children = subNode.getChildNodes();
					ArrayList<Node> stripped = stripTextItems(children);
					String tag = stripped.get(0).getTextContent();
					String label = stripped.get(1).getTextContent();
					if ((map != null) && (map.getNamedItem("type") != null))
						type = map.getNamedItem("type").getNodeValue();
					if (!type.equals("MESSAGE"))
					{

						if (type.equals("GRID")) 
						{
							readGridColumns((Element)subNode, tag);
						}
						else 
						{
							tag = modifyTagsForDates(tag, type);
							this.headerMap.put(tag, label);
						} 
					}
				}
			} 
		}
		return "";
	}

	private ArrayList<Node> stripTextItems(NodeList children)
	{
		ArrayList<Node> retVal = new ArrayList();
		for (int n = 0; n < children.getLength(); n++) 
		{
			Node item = children.item(n);
			if (item.getNodeType() != 3) 
			{
				retVal.add(item);
			}
		}
		return retVal;
	}


	private void readMetaHeaders(Document doc)
	{
		NodeList metaList = doc.getElementsByTagName("BulletinMetaData");
		Node metaData = getLatestVersionMetaHeaders(metaList);
		NodeList itemList = metaData.getChildNodes();
		for (int n = 0; n < itemList.getLength(); n++) 
		{
			Node item = itemList.item(n);
			String itemName = item.getNodeName();
			if ((item.getNodeType() != 3) && 
					(!itemName.equals("AuthorAccountId")) && 
					(!itemName.equals("BulletinStatus")) && 
					(!itemName.equals("LocalizedBulletinStatus")) && 
					(!itemName.equals("LocalizedBulletinLastSavedDateTime")) && 
					(!itemName.equals("AllPrivate")) && 
					(!itemName.equals("ExtendedHistory")))
			{

				this.metaHeaderMap.put(itemName, itemName);
			}
		}

		this.metaHeaderMap.put("FileName", "FileName");
	}


	private void addHeadersToOutput()
	{
		for (String key : this.metaHeaderMap.keySet()) 
		{
			if (key.equals("BulletinLastSavedDateTime"))
				key = "BulletinLastSavedDateGMT";
		
			this.csvOutput = (this.csvOutput + key + getDelimeter());
		}
		
		for (String key : this.headerMap.keySet()) 
		{
			key = modifyForDates(key);
			this.csvOutput = (this.csvOutput + key + getDelimeter());
		}
	}

	private void addMetaDataToOutput(Element bulletin)
	{
		Element metaData = (Element)bulletin.getElementsByTagName("BulletinMetaData").item(0);

		for (String header : this.metaHeaderMap.keySet()) 
		{
			NodeList itemList = metaData.getElementsByTagName(header);
			String metaValue; 
			if (itemList.getLength() != 0) 
			{
				metaValue = itemList.item(0).getTextContent();
				if (header.equals("BulletinLastSavedDateTime"))
					metaValue = msecsToGMTStr(metaValue);
			} 
			else 
			{ 
				if (header.equals("FileName")) 
				{
					metaValue = getOutFileName();
				} 
				else 
				{
					metaValue = "";
				}
			}
			this.csvOutput = (this.csvOutput + encodeRawValue(metaValue) + getDelimeter());
		}
	}

	private void addFieldsToOutput(Element bulletin)
	{
		Element fieldValues = (Element)bulletin.getElementsByTagName("FieldValues").item(0);
		NodeList fields = fieldValues.getElementsByTagName("Field");
		LinkedHashMap<String, Node> map = BuildFieldMap(fields);

		for (String key : this.headerMap.keySet())
		{
			String header = stripDateData(key);
			String value; if (map.containsKey(header)) {
				Element fieldNode = (Element)map.get(header);
				NodeList valueList = fieldNode.getElementsByTagName("Value");
				value = valueList.item(0).getTextContent();
				value = encodeRawValue(value);
				value = modifyValueForDates(key, value);
			} 
			else {
				value = "";
			}

			this.csvOutput = (this.csvOutput + value + getDelimeter());
		}

		String gridLocalID = getLocalID(bulletin);
		extractGridColumnData(gridLocalID, map);
	}

	private String getLocalID(Element bulletin)
	{
		NodeList singleItemList = bulletin.getElementsByTagName("BulletinLocalId");
		String retVal = singleItemList.item(0).getTextContent();

		return retVal;
	}

	LinkedHashMap<String, Node> BuildFieldMap(NodeList fields)
	{
		LinkedHashMap<String, Node> map = new LinkedHashMap();
		for (int i = 0; i < fields.getLength(); i++) 
		{
			map.put(fields.item(i).getAttributes().item(0).getTextContent(), fields.item(i));
		}

		return map;
	}

	private void readGridColumns(Element gridDescNode, String tag)
	{
		ArrayList<String> colList = this.gridFieldNames.get(tag);
		if (colList == null) 
		{
			colList = new ArrayList();
			colList.add("LocalId");
			NodeList colNodeList = gridDescNode.getElementsByTagName("Column");
			int numCols = colNodeList.getLength();
			for (int colNum = 0; colNum < numCols; colNum++) 
			{
				Node subNode = colNodeList.item(colNum);
				String type = subNode.getAttributes().item(0).getTextContent();
				NodeList children = subNode.getChildNodes();
				ArrayList<Node> stripped = stripTextItems(children);
				String label = stripped.get(1).getTextContent();
				label = modifyTagsForDates(label, type);
				colList.add(label);
			}
			this.gridFieldNames.put(tag, colList);
		}
	}

	private void extractGridColumnData(String gridLocalID, LinkedHashMap<String, Node> fieldMap)
	{
		for (String gridFieldName : this.gridFieldNames.keySet())
		{
			Element field = (Element)fieldMap.get(gridFieldName);
			if (field != null) 
			{
				String gridName = field.getAttribute("tag");
				NodeList rowList = field.getElementsByTagName("Row");
				ArrayList<String> rowArray = this.gridDataMap.get(gridName);
				if (rowArray == null)
					rowArray = new ArrayList();

				for (int rowIndex = 0; rowIndex < rowList.getLength(); rowIndex++) 
				{
					String rowOutput = "";

					Element row = (Element)rowList.item(rowIndex);
					NodeList colList = row.getElementsByTagName("Column");
					rowOutput = rowOutput + gridLocalID + getDelimeter();
					Boolean rowHasValues = Boolean.valueOf(false);
					for (int colIndex = 0; colIndex < colList.getLength(); colIndex++) 
					{
						Element col = (Element)colList.item(colIndex);
						String value = col.getTextContent();
						value = encodeRawValue(value);

						if (value.contains("Simple:"))
							value = value.replace("Simple:", "");

						if (value.contains("Range:")) 
						{
							value = value.replace("Range:", "");
							value = value.replace(",", getDelimeter());
						}

						value = value.replace("0001-01-01", "");

						if (!value.isEmpty())
							rowHasValues = Boolean.valueOf(true);

						rowOutput = rowOutput + value + getDelimeter();
					}
					if (rowHasValues.booleanValue())
						rowArray.add(rowOutput);
				}
				this.gridDataMap.put(gridName, rowArray);
			}
		}
	}



	public HashMap<String, String> getAllGridInfo()
	{
		HashMap<String, String> retMap = new HashMap();
		for (String gridName : this.gridFieldNames.keySet()) 
		{
			String gridStr = "";

			ArrayList<String> col = this.gridFieldNames.get(gridName);
			for (int i = 0; i < col.size(); i++) 
			{
				String aStr = modifyForDates(col.get(i));
				gridStr = gridStr + aStr + getDelimeter();
			}
			gridStr = gridStr + EOLN;

			ArrayList<String> rowList = this.gridDataMap.get(gridName);

			for (int row = 0; row < rowList.size(); row++) 
			{
				gridStr = gridStr + rowList.get(row) + EOLN;
			}
			retMap.put(gridName, gridStr);
		}

		return retMap;
	}

	private Node getLatestVersionMetaHeaders(NodeList metaList)
	{
		Element retNode = (Element)metaList.item(0);

		Node origVer = retNode.getElementsByTagName("BulletinVersion").item(0);
		int curVer = Integer.parseInt(origVer.getTextContent());

		for (int i = 1; i < metaList.getLength(); i++) 
		{
			Element temp = (Element)metaList.item(i);
			NodeList nodes = temp.getElementsByTagName("BulletinVersion");
			int ver = Integer.parseInt(nodes.item(0).getTextContent());
			if (ver > curVer) 
			{
				curVer = ver;
				retNode = temp;
			}
		}

		return retNode;
	}

	private String encodeRawValue(String valueToEncode)
	{
		valueToEncode = CsvEncoder.encodeValue(valueToEncode, getDelimeter());
		valueToEncode = valueToEncode.replace(EOLN, " ");
		valueToEncode = valueToEncode.replace("\r", " ");
		valueToEncode = valueToEncode.replace("\n", " ");

		return valueToEncode;
	}

	private String msecsToGMTStr(String milliSecs)
	{
		Long ms = Long.valueOf(Long.parseLong(milliSecs));
		Date date = new Date(ms.longValue());

		SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
		sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
		
		return sdf.format(date);
	}
	
	private String modifyForDates(String key)
	{
		if (key.startsWith(DATE_PREFIX)) 
		{
			key = key.split(DATE_DELIMETER)[1];
		} 
		else if (key.startsWith(RANGE_PREFIX)) 
		{
			String[] split = key.split(DATE_DELIMETER,-1);
			String tempKey = split[1];
			String newKey = tempKey + "Start" + getDelimeter() + tempKey + "End";
			return newKey;
		}

		return key;
	}
	
	private String modifyTagsForDates(String tag, String type)
	{
		if (type.equals(DATE_TYPE)) 
		{
			String newTag = DATE_PREFIX + tag;
			return newTag;
		} 
		else if (type.equals(RANGE_TYPE)) 
		{
			String newTag = RANGE_PREFIX + tag + DATE_DELIMETER + tag;
			return newTag;
		}
		return tag;
	}
	
	private String stripDateData(String key)
	{
		if ((key.startsWith(DATE_PREFIX)) || (key.startsWith(RANGE_PREFIX))) 
		{
			String[] split = key.split(DATE_DELIMETER);
			String newKey = split[1];
			return newKey;
		}

		return key;
	}

	private String modifyValueForDates(String key, String value)
	{
		if (key.startsWith(DATE_PREFIX)) 
		{
			value = value.replace("Simple:", "");
		} 
		else if (key.startsWith(RANGE_PREFIX)) 
		{
			value = value.replace("Range:", "");
			value = CsvEncoder.decodeValue(value);
		}

		value = value.replace("0001-01-01", "");
		return value;
	}
	
	private InputSource getInputSource()
	{
		return xmlInputSource;
	}
	
	public String getDelimeter()
	{
		return DELIMITER;
	}
}
