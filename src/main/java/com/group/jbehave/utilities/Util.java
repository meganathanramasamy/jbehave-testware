package com.group.jbehave.utilities;

import com.group.bdd.framework.LogUtil;
import oracle.sql.CLOB;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.jbehave.core.model.ExamplesTable;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.sql.Clob;
import java.sql.SQLException;
import java.text.*;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.*;

import static com.group.bdd.framework.Asserts.assertThat;
import static com.group.bdd.framework.DBUtils.getDBTableReportFormatCol;


public class Util extends Thread {

    final static Logger LOG = Logger.getLogger(Util.class);

    public static String findAndReplaceText(String textMessage, Map<String, String> dataValues) {
        for (Map.Entry m : dataValues.entrySet()) {
            String replaceValue = (String) m.getValue().toString();
            int position = replaceValue.indexOf("$");
            while (position >= 0) {
                StringBuilder str = new StringBuilder(replaceValue);
                str.insert(position, '\\');
                replaceValue = str.toString();
                position = replaceValue.indexOf("$", position + 2);
            }
            replaceValue = replaceSpaceAndTabs(replaceValue);
            String key = m.getKey().toString();
            textMessage = textMessage.replaceAll("\\$-\\{" + key + "\\}", replaceValue);
        }

        return textMessage;
    }

    public static String findAndReplaceTextWithoutTrim(String textMessage, Map<String, String> dataValues) {
        for (Map.Entry m : dataValues.entrySet()) {
            String replaceValue = (String) m.getValue().toString();
            if (replaceValue.contains("$")) {
                int position = replaceValue.indexOf("$");
                StringBuilder str = new StringBuilder(replaceValue);
                str.insert(position, '\\');
                replaceValue = str.toString();
            }
            replaceValue = replaceSpaceAndTabs(replaceValue);
            String key = m.getKey().toString();
            textMessage = textMessage.replaceAll("\\$-\\{" + key + "\\}", replaceValue);
        }

        return textMessage;
    }

    public static String replaceSpaceAndTabs(String inString) {
        int takeNum;
        for (int j = 0; j < inString.length(); j++) {
            if (inString.contains("SPACE>") || (inString.contains("TAB>"))) {
                int spacePosition = inString.indexOf("SPACE>");
                int tabPosition = inString.indexOf("TAB>");
                if (spacePosition < 0) {
                    spacePosition = tabPosition + tabPosition;
                } else if (tabPosition < 0) {
                    tabPosition = spacePosition + spacePosition;
                }
                if (spacePosition < tabPosition) {
                    String shortString = inString.substring(0, spacePosition);
                    String startString = shortString.substring(shortString.lastIndexOf("<")).replaceFirst("<", "");
                    takeNum = Integer.parseInt(startString);
                    String replaceSpace = "<" + takeNum + "SPACE>";
                    String spaces = String.format("%1$-" + takeNum + "s", "");
                    inString = inString.replaceAll(replaceSpace, spaces);
                } else {
                    String shortString = inString.substring(0, tabPosition);
                    String startString = shortString.substring(shortString.lastIndexOf("<")).replaceFirst("<", "");
                    takeNum = Integer.parseInt(startString);
                    String replaceTab = "<" + takeNum + "TAB>";
                    String tabs = "";
                    for (int num = 1; num <= takeNum; num++) {
                        tabs = tabs + "\t";
                    }
                    inString = inString.replaceAll(replaceTab, tabs);
                }
            } else {
                break;
            }
        }

        inString = inString.replaceAll("<CARRIAGERETURN>", "\r");
        inString = inString.replaceAll("<NEWLINE>", "\n");
        return inString;
    }

    public synchronized static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public static String generateRandom12DigitChar() {
        int randomNumSixDigit = ThreadLocalRandom.current().nextInt(100000, 999999 + 1);
        int randomNumThreeDigit = ThreadLocalRandom.current().nextInt(100, 999 + 1);
        DateFormat format = new SimpleDateFormat("ss");
        DateFormat formatSSS = new SimpleDateFormat("SSS");
        String getSecs = format.format(new Date());
        String getMilliSecs = formatSSS.format(new Date());
        String randNum = getMilliSecs + randomNumThreeDigit + randomNumSixDigit;
        return randNum;
    }

    public static Map<String, Object> convertExamplesTableToMap(ExamplesTable examplesTable) {
        Map<String, Object> expectedMap = new HashMap<String, Object>();
        for (Map<String, String> row : examplesTable.getRows()) {
            Iterator<Entry<String, String>> it = row.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, String> pairs = (Map.Entry<String, String>) it.next();
                expectedMap.put(pairs.getKey(), pairs.getValue());
            }
        }
        return expectedMap;
    }

    public static String compareTags(String actualTagValue, String expectedTagValue, String tagName, String mismatchedField) {
        if (actualTagValue.equals(expectedTagValue)) {
            LogUtil.log("Matched Field: " + tagName + " ==> ExpectedTagValue: " + expectedTagValue
                    + ", ActualTagValue: " + actualTagValue);
        } else {
            LogUtil.log("*********Mismatched Field: " + tagName + " ==> ExpectedTagValue: " + expectedTagValue
                    + ", ActualTagValue: " + actualTagValue);
            mismatchedField = mismatchedField + ", " + tagName;
        }
        if (mismatchedField.startsWith(",")) {
            mismatchedField = mismatchedField.substring(1);
        }
        return mismatchedField;
    }

    public void compareExpectedAndActualDBRecords(List<Map<String, Object>> expectedResult,
                                                  List<Map<String, Object>> actualResult) {
        String mismatchedField = "";
        for (int i = 0; i < expectedResult.size(); i++) {
            Map<String, Object> expectedMap = expectedResult.get(i);
            Map<String, Object> actualMap = actualResult.get(i);
            mismatchedField = equalMaps(expectedMap, actualMap);
        }
        if (!mismatchedField.equals("")) {
            assertThat("Actual & Expected results are not matched. Mismatched Fields: " + mismatchedField,
                    false);
        }
    }

    public static String equalMaps(Map<String, Object> m1, Map<String, Object> m2) {
        String mismatchedField = "";

        for (String key : m1.keySet()) {
            if (m2.get(key) == null) {
                m2.put(key, "null");
            }
            int position = m2.get(key).toString().indexOf("?>");
            String actualValue = "";
            if (position >= 1) {
                actualValue = XmlUtils.formatXML(m2.get(key).toString()).trim().substring(position + 2).trim();
            } else {
                actualValue = XmlUtils.formatXML(m2.get(key).toString());
            }
            if (!m1.get(key).equals(actualValue)) {
                if (m2.get(key) == null) {
                    m2.put(key, "");
                }
                mismatchedField = mismatchedField + "," + key;
                if (m1.get(key).toString().length() >= 50 || m2.get(key).toString().length() >= 50) {
                    LogUtil.log("*********Mismatched Field: " + key + " ==> ");
                    LogUtil.log("Expected Value  : " + m1.get(key));
                    LogUtil.log("Actual Value 	 : " + actualValue);
                } else {
                    LogUtil.log("*********Mismatched Field: " + key + " ==> Expected Value  : " + m1.get(key) + ", Actual Value 	 : " + m2.get(key));
                }
            } else {
                LogUtil.log("Matched Field: " + key + " ==> Expected Value  : " + m1.get(key) + ", Actual Value 	 : " + m2.get(key));
            }
        }
        if (mismatchedField.startsWith(",")) {
            mismatchedField = mismatchedField.substring(1);
        }
        return mismatchedField;
    }

    public static String compareStringFields(String mismatchedField, String fieldName, String expValue, String actValue) {
        if (expValue.equalsIgnoreCase("null")) {
            expValue = "";
        }
        if (actValue.equals(expValue)) {
            LogUtil.log("Matched Field: " + fieldName + " ==> Expected: " + expValue + "; Actual: " + actValue);
        } else {
            LogUtil.log("******Mismatched Field: " + fieldName + " ==> Expected: " + expValue + "; Actual: " + actValue);
            mismatchedField = mismatchedField + fieldName + ",";
        }
        return mismatchedField;
    }

    public static String ensureTwoDecimalPlaces(String inStr) {
        int decimalIndex = inStr.indexOf(".");
        if (decimalIndex == -1) {
            inStr = inStr + ".00";
        } else if (decimalIndex == inStr.length() - 2) {
            inStr = inStr + "0";
        } else if (decimalIndex < inStr.length() - 3) {
            inStr = inStr.substring(0, decimalIndex + 3);
        }
        return inStr;
    }

    public static String formatAmountCommaSeparated(String amount) {
        amount = ensureTwoDecimalPlaces(amount);
        String[] splitAmountStr = amount.split("\\.");
        if (splitAmountStr[0].length() > 3) {
            int charCount = 0;
            String formattedAmt = "";
            for (int i = splitAmountStr[0].length() - 1; i >= 0; i--) {
                if (charCount == 3) {
                    formattedAmt = "," + formattedAmt;
                    charCount = 0;
                }
                char c = splitAmountStr[0].charAt(i);
                formattedAmt = c + formattedAmt;
                charCount++;
            }
            amount = formattedAmt + "." + splitAmountStr[1];
        }
        return amount;
    }

    public static Map<String, List<String>> convertExamplesTableToMapValueAsList(ExamplesTable examplesTable) {
        List<String> lst = new ArrayList<>();
        Map<String, List<String>> expectedMap = new HashMap<String, List<String>>();
        for (Map<String, String> row : examplesTable.getRows()) {
            Iterator<Entry<String, String>> it = row.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, String> pairs = (Map.Entry<String, String>) it.next();
                lst.add(pairs.getValue());
                expectedMap.put(pairs.getKey(), lst);
            }
        }
        return expectedMap;
    }

    public static String mixedCase(String input) {
        StringBuilder sb = new StringBuilder(input.toLowerCase());

        for (int i = 0; i < input.length(); i += 2) {
            sb.setCharAt(i, Character.toUpperCase(input.charAt(i)));
        }
        return sb.toString();
    }

    public static String clobToString(CLOB clob) {
        String value = "";
        if (clob != null) {
            try {
                int size = (int) clob.length();
                value = clob.getSubString(1, size);
            } catch (SQLException e) {
                value = "";
                assertThat("Error: " + e, false);
            }
        }
        return value;
    }

    public static String convertClobToStringDynamic(List<Map<String, Object>> actualResult, String columnName) {
        Clob clob = (Clob) actualResult.get(0).get(columnName);
        String value = "";
        if (clob != null) {
            try {
                int size = (int) clob.length();
                if (size <= 4000) {
                    value = clob.getSubString(1, size);
                } else {
                    try {
                        InputStream in = clob.getAsciiStream();
                        StringWriter w = new StringWriter();
                        IOUtils.copy(in, w);
                        value = w.toString();
                    } catch (Exception e) {
                        value = "";
                    }
                }

                //LogUtil.log(value);
            } catch (SQLException e) {
                value = "";
                //assertThat("Error: " + e, false);
            }
        }
        return value;
    }

    public static String convertClobMapToStringDynamic(Map<String, Object> actualResult, String columnName) {
        Clob clob = (Clob) actualResult.get(columnName);
        String value = "";
        if (clob != null) {
            try {
                int size = (int) clob.length();
                if (size <= 4000) {
                    value = clob.getSubString(1, size);
                } else {
                    try {
                        InputStream in = clob.getAsciiStream();
                        StringWriter w = new StringWriter();
                        IOUtils.copy(in, w);
                        value = w.toString();
                    } catch (Exception e) {
                        value = "***Clob Size more than 4000 bytes. Error While Converting to Readable format***";
                    }
                }
            } catch (SQLException e) {
                value = "***Clob Size more than 4000 bytes. Error While Converting to Readable format***";
            }
        }
        return value;
    }

    public static void addToTextLog(String logFileName, String identifier, String data) {
        String contentToAppend = identifier + "," + data + "\r\n";
        String pathStr = System.getProperty("user.dir") + "\\" + logFileName + ".txt";
        Path path = Paths.get(pathStr);
        try {
            if (!Files.exists(path)) {
                Files.createFile(path);
            }

            Files.write(
                    Paths.get(pathStr),
                    contentToAppend.getBytes(),
                    StandardOpenOption.APPEND);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String replaceNonStandardAlphabet(String stringToBeModified) {
        String normalized = Normalizer.normalize(stringToBeModified, Normalizer.Form.NFD);
        String accentRemoved = normalized.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        return accentRemoved;
    }

    public static LinkedList<Map<String, String>> readTestcasesAndKeys(String storyPath, LinkedList<Map<String, String>> readList,
                                                                       String sourcePath) {
        String storyFile = "";
        boolean flag = false;
        File file = new File(storyPath);

        try {
            BufferedReader reader;
            reader = new BufferedReader(new FileReader(file.toString()));
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().equals("")) {
                    if (flag == true) {
                        if (line.startsWith("@description") || line.startsWith("Given")) {
                            flag = false;
                        }
                    }
                    if (line.startsWith("Scenario:") || flag == true) {
                        storyFile = storyFile + line + "\r\n";
                        flag = true;
                    }
                }
            }
            reader.close();

            String[] stories = storyFile.split("\r\n");
            String newStoryFile = "";
            String keys = "", sce = "";
            for (String storyLine : stories) {
                Map<String, String> readMap = new LinkedHashMap<>();
                if (storyLine.startsWith("Scenario:")) {
                    newStoryFile = newStoryFile + "\n" + storyLine;
                } else {
                    newStoryFile = newStoryFile + ";" + storyLine;
                }
            }
            newStoryFile = newStoryFile.replaceAll("Meta:;", "Meta:");
            stories = newStoryFile.split("\n");
            for (String storyLine : stories) {
                if (!storyLine.trim().equals("")) {
                    Map<String, String> readMap = new LinkedHashMap<>();
                    String storyName = "..\\" + storyPath.substring(storyPath.lastIndexOf(sourcePath));
                    String tcName = storyLine.substring(0, storyLine.indexOf("Meta:")).trim();
                    String tcKey = storyLine.substring(storyLine.indexOf("Meta:")).trim().replaceAll("Meta:", "");
                    readMap.put("Story", "\"" + storyName.trim() + "\"");
                    readMap.put("TestCase", "\"" + tcName.trim().substring(0, tcName.length() - 1) + "\"");
                    readMap.put("Meta", "\"" + tcKey.trim() + "\"");
                    readList.add(readMap);
                }
            }
        } catch (IOException e) {
            assertThat("Error: " + e, false);
        }

        return readList;
    }

    public static void findReplaceSpacesInMeta(String storyPath) {
        try {
            Path path = Paths.get(storyPath);
            Charset charset = StandardCharsets.UTF_8;

            String content = new String(Files.readAllBytes(path), charset);
            String[] lines = content.split("\r\n");

            for (String line : lines) {
                if (line.matches("^(?!@description)@.* .*")) {
                    String newValue = line.replaceAll(" ", "_");
                    content = content.replaceAll(line, newValue);
                }
            }
            Files.write(path, content.getBytes(charset));
        } catch (IOException e) {
            assertThat("Error: " + e, false);
        }
    }

    public static String padCharactersAtSpecificInterval(String original, int interval, String separator) {
        String formatted = "";

        for (int i = 0; i < original.length(); i++) {
            if (i % interval == 0 && i > 0) {
                formatted += separator;
            }
            formatted += original.substring(i, i + 1);
        }

        return formatted;
    }

    public static String findEmptyAndAddCharacters(String mainElement, String newElement, String append) {
        if (!newElement.equals("")) {
            mainElement = mainElement + append + newElement;
        }
        return mainElement;
    }

    public Map<Integer, String> storeListToMapDetails(List<HashMap<String, String>> getDataList, String rowName) {
        Map<Integer, String> map = new TreeMap<Integer, String>();
        int position = 0;
        for (Map<String, String> row : getDataList) {
            if (0 <= position) {
                map.put(position, row.get(rowName).toString());
            }
            position++;
        }
        return map;
    }

    public static String getNumericString(int length) {
        String getDateTime = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        int n = 0;
        if (getDateTime.length() < length) {
            n = length - getDateTime.length();
        } else {
            n = length;
        }
        String numericString = "0123456789";
        StringBuilder sb = new StringBuilder(n);
        for (int i = 0; i < n; i++) {
            int index = (int) (numericString.length() * Math.random());
            sb.append(numericString.charAt(index));
        }
        if (length > 14) {
            sb.append(getDateTime);
        }
        return sb.toString();
    }

    public static String getUniqueNumbers(String initChar, int length) {
        String uniqueValue = "";
        length = length - initChar.length();
        uniqueValue = initChar + getNumericString(length);
        return uniqueValue;
    }

    public static String getKeyValue(Map<String, String> data, String key) {
        String value = "";
        for (Map.Entry<String, String> entry : data.entrySet()) {
            if (entry.getKey().equals(key)) {
                value = entry.getValue();
            }
        }
        return value;
    }

    public static String getListKeyValue(List<Map<String, Object>> list, String paramName) {
        String value = null;
        for (Map<String, Object> map : list) {
            if (map.get("PARAMETERNAME").equals(paramName)) {
                value = map.get("PARAMETERVALUE").toString();
                break;
            }
        }
        return value;
    }

    public static void printMultiLogs(String sqlQuery, List<Map<String, Object>> actualResult) {
        String multipleRows = "";
        if (actualResult.size() > 1) {
            for (int i = 0; i < actualResult.size(); i++) {
                multipleRows = multipleRows + actualResult.get(i) + "\n\n";
            }
            LogUtil.logAttachment(sqlQuery, multipleRows);
        } else if (actualResult.size() == 1) {
            LogUtil.logAttachment(sqlQuery, getDBTableReportFormatCol(actualResult));
        } else {
            LogUtil.log("No records found. " + sqlQuery);
        }
    }

    public List<Map<String, Object>> setExamplesTableToMap(ExamplesTable expectedTable) {
        List<Map<String, Object>> expectedResult = new ArrayList<Map<String, Object>>();
        Map<String, Object> expectedMap = new HashMap<String, Object>();

        for (Map<String, String> row : expectedTable.getRows()) {
            Iterator<Entry<String, String>> it = row.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, String> pairs = (Map.Entry<String, String>) it.next();
                expectedMap.put(pairs.getKey(), pairs.getValue());
            }
        }
        expectedResult.add(expectedMap);
        return expectedResult;
    }

    public boolean isMapFieldEqual(Map<String, Object> expectedMap, Map<String, Object> actualMap, String field) {
        if (actualMap.get(field) == null) actualMap.put(field, "null");
        return expectedMap.get(field).toString().trim().equals(actualMap.get(field).toString().trim());
    }
}
