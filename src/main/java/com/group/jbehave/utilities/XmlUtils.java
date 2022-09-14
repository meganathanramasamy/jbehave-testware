package com.group.jbehave.utilities;

import java.io.*;

import java.util.ArrayList;
import java.util.Map;
import java.util.regex.Pattern;

import com.group.bdd.framework.LogUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.*;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import static com.group.bdd.framework.Asserts.assertThat;

public class XmlUtils {

    private static final Logger LOG = LogManager.getLogger(XmlUtils.class);

    public static final ThreadLocal<Map<String, String>> currentXLRow = new ThreadLocal<>();
    private static final String COL_DELIM = ",";

    public String generateXML(String sheetName, String XMLName, String xmldata) {
        String generateXML = "";
        String fileLocations = new File("src/main/resources").getAbsoluteFile().toString();
        File file = new File(fileLocations + "/input_files/" + XMLName + ".xml");
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file.toString()));
            String line;
            while ((line = reader.readLine()) != null) {
                generateXML = generateXML + line + "\r\n";
            }
            reader.close();

            currentXLRow.set(ExcelUtils.readDataSheet(sheetName, xmldata));

            LogUtil.logAttachment(xmldata, ExcelUtils.updateTheExcelCol(currentXLRow.get()));

            generateXML = Util.findAndReplaceText(generateXML, currentXLRow.get());

        } catch (IOException e) {
            assertThat("Error: " + e, false);
        } finally {
            try {
                reader.close();
            } catch (Exception e) {
                assertThat("Error: " + e.getMessage(), false);
            }
        }

        LOG.info("generateXML: " + "\r\n" + generateXML);
        return generateXML;
    }

    private String createRow(Map<String, String> elements) {
        StringBuilder res = new StringBuilder();
        for (String ele : elements.keySet()) {
            res.append(ele);
            res.append(COL_DELIM);
        }
        res.append("\n");
        for (String ele : elements.keySet()) {
            String val = elements.get(ele);
            res.append(val);
            res.append(COL_DELIM);
        }

        res.append("\n");
        return res.toString();
    }

    public static String formatXML(String xml) {
        String prettyXml = xml.replaceAll("\\s*[\\r\\n]+\\s*", "").trim();
        prettyXml = prettyXml.replaceAll(">\\s*<", "><");
        return prettyXml;
    }

    public static boolean checkIfXMLIsWellFormed(String aXml) {
        boolean isValid = true;
        try {
            File file = new File(aXml);
            if (file.exists()) {
                XMLReader reader = null;
                try {
                    reader = XMLReaderFactory.createXMLReader();
                } catch (SAXException e) {
                    e.printStackTrace();
                }
                reader.parse(aXml);
            } else {
                isValid = false;
            }
        } catch (SAXException e) {
            isValid = false;
        } catch (IOException io) {
            isValid = false;
        }
        return isValid;
    }

    public static String removeEmptyXMLTags(String xml) {
        Pattern emptyValueTag = Pattern.compile("\\s*<\\w+/>");
        Pattern emptyTagMultiLine = Pattern.compile("\\s*<\\w+>\n*\\s*</\\w+>");
        Pattern emptyValueTagWithColon = Pattern.compile("<\\s*[\\w:]+?\\s*\\/>");
        Pattern emptyTagMultiLineWithColon = Pattern.compile("<\\s*[\\w:]+>(<\\/?\\w+>)*<\\/\\s*[\\w:]+>");
        xml = emptyValueTag.matcher(xml).replaceAll("");
        while (xml.length() != (xml = emptyTagMultiLine.matcher(xml).replaceAll("")).length()) {
        }
        xml = emptyValueTagWithColon.matcher(xml).replaceAll("");
        while (xml.length() != (xml = emptyTagMultiLineWithColon.matcher(xml).replaceAll("")).length()) {
        }
        return xml;
    }

    public static String removeEmptyXMLTagsWithNamespace(String xml) {
        //Pattern emptyValueTag = Pattern.compile("\\s*<\\w+/>");
        //Pattern emptyTagMultiLine = Pattern.compile("\\s*<\\w+>\n*\\s*</\\w+>");
        Pattern emptyValueTag = Pattern.compile("<\\s*[\\w:]+?\\s*\\/>");
        Pattern emptyTagMultiLine = Pattern.compile("<\\s*[\\w:]+>(<\\/?\\w+>)*<\\/\\s*[\\w:]+>");
        xml = emptyValueTag.matcher(xml).replaceAll("");
        while (xml.length() != (xml = emptyTagMultiLine.matcher(xml).replaceAll("")).length()) {
        }
        return xml;
    }

    public static String replaceEmptyXMLTagsWithSelfClosingTag(String xml) {
        Pattern emptyTagMultiLine = Pattern.compile("\\s*(<\\w+)>\n*\\s*</\\w+>");
        return emptyTagMultiLine.matcher(xml).replaceAll("$1" + "/>");
    }

    public static String removeNameSpace(String xml) {
        try {
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            transformer.setOutputProperty(OutputKeys.INDENT, "false");
            LOG.info("before xml = " + xml);
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            InputSource inputSource = new InputSource(new StringReader(xml));
            Document xmlDoc = builder.parse(inputSource);
            Node root = xmlDoc.getDocumentElement();
            NodeList rootchildren = root.getChildNodes();
            Element newroot = xmlDoc.createElement(root.getNodeName());
            for (int i = 0; i < rootchildren.getLength(); i++) {
                newroot.appendChild(rootchildren.item(i).cloneNode(true));
            }
            xmlDoc.replaceChild(newroot, root);
            DOMSource requestXMLSource = new DOMSource(xmlDoc.getDocumentElement());
            StringWriter requestXMLStringWriter = new StringWriter();
            StreamResult requestXMLStreamResult = new StreamResult(requestXMLStringWriter);
            transformer.transform(requestXMLSource, requestXMLStreamResult);
            String modifiedRequestXML = requestXMLStringWriter.toString();

            return modifiedRequestXML;
        } catch (Exception e) {
            assertThat("Could not parse message as xml: " + e.getMessage(), false);
        }
        return "";
    }

    public static String removeXmlStringNamespaceAndPreamble(String xmlString) {
        return xmlString.replaceAll("(<\\?[^<]*\\?>)?", ""). /* remove preamble */
                replaceAll("xmlns.*?(\"|\').*?(\"|\')", "") /* remove xmlns declaration */
                .replaceAll("(<)(\\w+:)(.*?>)", "$1$3") /* remove opening tag prefix */
                .replaceAll("(</)(\\w+:)(.*?>)", "$1$3"); /* remove closing tags prefix */
    }

    public static String unescapeXMLForValidation(String expXML) {
        expXML = expXML.replaceAll("<!\\[CDATA\\[", "");
        expXML = expXML.replaceAll("]]>", "");

        expXML = expXML.replaceAll("&gt;", ">");
        expXML = expXML.replaceAll("&quot;", "\"");
        expXML = expXML.replaceAll("&apos;", "\'");
        expXML = expXML.replaceAll("&lt;", "<");
        expXML = expXML.replaceAll("&amp;", "&");

        return expXML;
    }

    public static String xmlEscapeText(String t) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < t.length(); i++) {
            char c = t.charAt(i);
            switch (c) {
                case '<':
                    sb.append("&lt;");
                    break;
                case '>':
                    sb.append("&gt;");
                    break;
                case '\"':
                    sb.append("&quot;");
                    break;
                case '&':
                    sb.append("&amp;");
                    break;
                case '\'':
                    sb.append("&apos;");
                    break;
                default:
                    if (c > 0x7e) {
                        sb.append("&#" + ((int) c) + ";");
                    } else
                        sb.append(c);
            }
        }
        return sb.toString();
    }

    public static String findFieldAtNthOccurrence(String putXMLtextAll, String field, int occurrence) {
        String value = "";
        try {
            int startposition = StringUtils.ordinalIndexOf(putXMLtextAll, "<" + field + ">", occurrence);
            int endposition = StringUtils.ordinalIndexOf(putXMLtextAll, "</" + field + ">", occurrence) + 3
                    + field.length();
            StringBuffer buf = new StringBuffer(putXMLtextAll);
            value = buf.substring(startposition, endposition);
            int startTagLength = ("<" + field + ">").length();
            int endTagLength = value.indexOf("</" + field + ">");
            value = value.substring(startTagLength, endTagLength);
        } catch (StringIndexOutOfBoundsException e) {
            value = "";
        }
        return value;
    }

    public static String getXMLNodeValue(String messageText, String nodePath) {

        String getXMLNodeValue = "";
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setCoalescing(true);
        DocumentBuilder builder;
        try {
            builder = factory.newDocumentBuilder();
            Document document = builder.parse(new InputSource(new StringReader(messageText)));

            XPath xPath = XPathFactory.newInstance().newXPath();

            NodeList nodes = (NodeList) xPath.evaluate(nodePath + "/text()",
                    document.getDocumentElement(), XPathConstants.NODESET);
            for (int i = 0; i < nodes.getLength(); ++i) {
                if (nodes.item(i) instanceof Text) {
                    getXMLNodeValue = ((Text) nodes.item(i)).getTextContent();
                } else if (nodes.item(i) instanceof Element) {
                    Element e = (Element) nodes.item(i);
                    getXMLNodeValue = e.getNodeValue();
                }
            }
        } catch (Exception e) {
            assertThat("Error: " + e, false);
        }

        return getXMLNodeValue;
    }

    public static String getXMLNodeAttributeValue(String messageText, String nodePath, String attribute) {

        String getXMLNodeAttrValue = "";
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;

        try {
            builder = factory.newDocumentBuilder();
            Document document = builder.parse(new InputSource(new StringReader(messageText)));

            XPathFactory xpf = XPathFactory.newInstance();
            XPath xpath = xpf.newXPath();
            Element userElement = (Element) xpath.evaluate(nodePath, document,
                    XPathConstants.NODE);
            getXMLNodeAttrValue = userElement.getAttribute(attribute);
        } catch (NullPointerException e) {
            getXMLNodeAttrValue = "";

        } catch (Exception e) {
            assertThat("Error occurred in Reading XML Attribute", false);

        }
        return getXMLNodeAttrValue;
    }

    public static String checkXMLNodeExists(String messageText, String nodePath) {

        String getXMLNodeValue = "";
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
        try {
            builder = factory.newDocumentBuilder();
            Document document = builder.parse(new InputSource(new StringReader(messageText)));

            XPath xPath = XPathFactory.newInstance().newXPath();
            NodeList nodes = (NodeList) xPath.evaluate(nodePath + "/text()",
                    document.getDocumentElement(), XPathConstants.NODESET);
            if (nodes.getLength() == 0) {
                getXMLNodeValue = "Field is not present";
                LogUtil.log("Field is not present" + ": " + nodePath);
            } else {
                for (int i = 0; i < nodes.getLength(); ++i) {
                    if (nodes.item(i) instanceof Text) {
                        getXMLNodeValue = ((Text) nodes.item(i)).getTextContent();
                    } else if (nodes.item(i) instanceof Element) {
                        Element e = (Element) nodes.item(i);
                        getXMLNodeValue = e.getNodeValue();
                    }
                }
            }
        } catch (Exception e) {
            assertThat("Error: " + e, false);
        }
        return getXMLNodeValue;
    }

    public static String removeFieldAtNthOccurrence(String fileData, String field, int occurrence) {
        int startposition = StringUtils.ordinalIndexOf(fileData, "<" + field + ">", occurrence);
        int endposition = StringUtils.ordinalIndexOf(fileData, "</" + field + ">", occurrence) + 3 + field.length();
        StringBuffer buf = new StringBuffer(fileData).replace(startposition, endposition, "");
        return buf.toString();
    }

    public static String readXMLBlockAtNthOccurrence(String fileData, String field, int occurrence) {
        int startposition = StringUtils.ordinalIndexOf(fileData, "<" + field + ">", occurrence);
        int endposition = StringUtils.ordinalIndexOf(fileData, "</" + field + ">", occurrence) + 3 + field.length();
        StringBuffer buf = new StringBuffer(fileData).replace(endposition, fileData.length(), "");
        buf.replace(0, startposition, "");
        return buf.toString();
    }

    public static String readXMLBlockAtNthOccurrenceWithoutEndTag(String fileData, String field, int occurrence) {
        StringBuffer buf = new StringBuffer();
        try {
            int startposition = StringUtils.ordinalIndexOf(fileData, "<" + field, occurrence);
            int endposition = StringUtils.ordinalIndexOf(fileData, "</" + field, occurrence) + 3 + field.length();
            buf = new StringBuffer(fileData).replace(endposition, fileData.length(), "");
            buf.replace(0, startposition, "");
            return buf.toString();
        } catch (Exception ex) {
            assertThat("Exception occurred in readXMLBlockAtNthOccurrenceWithoutEndTag method." + ex.getMessage(), false);
        }
        return buf.toString();
    }

    public static String getXMLPart(String content, String tagName) {
        String startTag = "<" + tagName + ">";
        String endTag = "</" + tagName + ">";
        int startposition = content.indexOf(startTag);
        int endposition = content.indexOf(endTag, startposition);
        if (startposition == -1) return "";
        startposition += startTag.length();
        if (endposition == -1) return "";
        return content.substring(startposition, endposition);
    }

    public static String getXPathFromNode(Node root) {
        Node current = root;
        String output = "";
        while (current.getParentNode() != null) {
            Node parent = current.getParentNode();
            if (parent != null && parent.getChildNodes().getLength() > 1) {
                int nthChild = 1;
                Node siblingSearch = current;
                while ((siblingSearch = siblingSearch.getPreviousSibling()) != null) {
                    // only count siblings of same type
                    if (siblingSearch.getNodeName().equals(current.getNodeName())) {
                        nthChild++;
                    }
                }
                output = "/" + current.getNodeName() + "[" + nthChild + "]" + output;
            } else {
                output = "/" + current.getNodeName() + output;
            }
            current = current.getParentNode();
        }
        return output;
    }

    public static String getChildNodeValuesWithDelimiter(String messageText, String nodePath, String delimiter) {
        String getXMLNodeValue = "";
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
        try {
            if (!messageText.trim().equals("")) {
                builder = factory.newDocumentBuilder();
                Document document = builder.parse(new InputSource(new StringReader(messageText)));

                XPath xPath = XPathFactory.newInstance().newXPath();
                NodeList nodes = (NodeList) xPath.evaluate(nodePath + "/text()",
                        document.getDocumentElement(), XPathConstants.NODESET);

                ArrayList<String> elementPaths = new ArrayList<>();

                for (int i = 0; i < nodes.getLength(); ++i) {
                    if (!(nodes.item(i)).getTextContent().equals(""))
                        elementPaths.add(getXPathFromNode(nodes.item(i)));
                }

                for (String xpath : elementPaths) {
                    String value = getXMLNodeValue(messageText, xpath.replaceAll("#text", ""));
                    if (getXMLNodeValue.equals("")) {
                        getXMLNodeValue = value;
                    } else {
                        getXMLNodeValue = getXMLNodeValue + delimiter + value;
                    }
                }
            }

        } catch (Exception e) {
            assertThat("Exception in method - getChildNodeValuesWithDelimiter : " + e.getLocalizedMessage(), false);
        }
        return getXMLNodeValue;
    }

    public static int xmlTagCount(String xml, String tag) {
        NodeList list;
        int count = 0;
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = docFactory.newDocumentBuilder();
            Document document = builder.parse(new InputSource(new StringReader(xml)));

            list = document.getElementsByTagName(tag);
            count = list.getLength();
        } catch (Exception ex) {
            assertThat("Exception in tagCount Method." + ex.getMessage(), false);
        }
        return count;
    }

    public static String indentXMLString(String xml) {
        String temp = formatXML(xml).replaceAll("\"xmlns", "\" xmlns");
        String formatting = "";
        int prevIndex = 0;
        int position = 0;
        int indentLevel = 0;

        while (temp.indexOf("><", prevIndex) != -1) {
            position = temp.indexOf("><", prevIndex);
            if (temp.charAt(position + 2) == '/') {
                indentLevel--;
            } else if (!(temp.charAt(temp.lastIndexOf("<", position - 1) + 1) == '/')) {
                indentLevel++;
            }
            formatting = "\r\n";
            for (int i = 1; i <= indentLevel; i++) {
                formatting = formatting + "    ";
            }

            temp = temp.replaceFirst("><", ">" + formatting + "<");
            prevIndex = position + 3 + indentLevel;
        }
        return temp;
    }

    public String validateXMLSchemaWithText(String xsdPath, String xmlText) {

        String returnText = "";
        String file = new File("src/main/resources").getAbsoluteFile().toString();
        xsdPath = file + "/schemas/" + xsdPath;

        try {
            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = schemaFactory.newSchema(new File(xsdPath));
            Validator validator = schema.newValidator();
            validator.validate(new StreamSource(new StringReader(xmlText)));
        } catch (IOException e) {
            returnText = "XSD VALIDATION: Exception: " + e.getMessage();
            return returnText;
        } catch (SAXException e1) {
            returnText = "XSD VALIDATION: SAX Exception: " + e1.getMessage();
            return returnText;
        }

        return returnText;
    }

    public boolean validateXMLSchema(String xsdPath, String xmlPath) {

        String file = new File("src/main/resources").getAbsoluteFile().toString();

        xsdPath = file + "/schemas/" + xsdPath;
        xmlPath = file + "/output_xml/" + xmlPath + ".xml";

        try {
            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = factory.newSchema(new File(xsdPath));
            Validator validator = schema.newValidator();
            validator.validate(new StreamSource(new File(xmlPath)));
        } catch (IOException | SAXException e) {
            return false;
        }

        return true;
    }

}
