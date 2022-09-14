package com.group.jbehave.utilities;

import com.group.bdd.framework.LogUtil;
import org.apache.commons.collections4.list.TreeList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.util.*;

import static com.group.bdd.framework.Asserts.assertThat;

public class ExcelUtils {

    private static final Logger LOG = LogManager.getLogger(ExcelUtils.class);

    private static final String COL_DELIM = "|";
    private static final String CSV_COL_DELIM = "\t,";
    private static final int ROW_MAX_LENGTH = 100;

    public static Map<String, String> readDataSheet(String SheetName, String keyMatch) {
        String fileLocations = new File("src/main/resources").getAbsoluteFile().toString();

        Map<String, String> dataMap = new LinkedHashMap<String, String>();
        String value;
        try {
            File file = new File(fileLocations + "/input_data/" + SheetName + ".xlsx");
            FileInputStream inputStream = new FileInputStream(file);
            Workbook workbook = new XSSFWorkbook(inputStream);
            Sheet sheet = workbook.getSheet("data");

            for (int row = 1; row <= sheet.getLastRowNum(); row++) {

                String getKeyName = sheet.getRow(row).getCell(0).toString();
                if (getKeyName.equals(keyMatch)) {
                    for (int col = 0; col < sheet.getRow(0).getLastCellNum(); col++) {
                        String key = sheet.getRow(0).getCell(col).toString();
                        try {
                            value = sheet.getRow(row).getCell(col).toString();
                        } catch (Exception e) {
                            value = "";
                        }
                        dataMap.put(key, value);
                    }
                    break;
                }
            }
        } catch (Exception e) {
            LOG.info(e.getLocalizedMessage().toString());
            assertThat("Error: " + e, false);
            return null;
        }

        return dataMap;

    }

    public static String updateTheExcelCol(Map<String, String> data) {
        StringBuilder res = new StringBuilder();
        int finalLen = 0;
        for (Map.Entry m : data.entrySet()) {
            int len = m.getKey().toString().length();
            if (len > finalLen) {
                finalLen = len;
            }

            len = m.getValue().toString().length();
            if (len > finalLen) {
                finalLen = len;
            }
        }

        for (Map.Entry m : data.entrySet()) {
            res.append(COL_DELIM);
            res.append(m.getKey().toString());
            res.append(getSpaces(finalLen - m.getKey().toString().length()));
            res.append(COL_DELIM);
            res.append(m.getValue().toString());
            res.append(getSpaces(finalLen - m.getValue().toString().length()));
            res.append(COL_DELIM);
            res.append("\n");
        }

        return res.toString();
    }

    private static String getSpaces(int len) {
        StringBuilder spaces = new StringBuilder();
        for (int i = 0; i <= len; i++) {
            spaces.append(" ");
        }
        return spaces.toString();
    }

    public static List<HashMap<String, String>> readMultiLinesInDataSheet(String SheetName, String keyMatch) {
        LOG.info("Inside reading multiple line data");
        List<HashMap<String, String>> getDataList = new TreeList<HashMap<String, String>>();
        List<HashMap<String, String>> finalList = new TreeList<HashMap<String, String>>();
        ArrayList<TreeMap<String, String>> data = new ArrayList<TreeMap<String, String>>();

        String fileLocations = new File("src/main/resources").getAbsoluteFile().toString();

        String value;
        int startRow = 1;
        int test = 0;
        int dataMapCounter = 0;
        try {
            File file = new File(fileLocations + "/input_data/" + SheetName + ".xlsx");
            LOG.info(fileLocations + "/input_data/" + SheetName + ".xlsx");
            FileInputStream inputStream = new FileInputStream(file);
            Workbook workbook = new XSSFWorkbook(inputStream);
            Sheet sheet = workbook.getSheet("data");

            for (int row = 1; row <= sheet.getLastRowNum(); row++) {
                HashMap<String, String> dataMap = new HashMap<String, String>();
                TreeMap<String, String> dataMap1 = new TreeMap<String, String>();

                String getKeyName = sheet.getRow(row).getCell(0).toString();
                if (getKeyName.equals(keyMatch)) {
                    for (int col = 0; col < sheet.getRow(0).getLastCellNum(); col++) {
                        String key = sheet.getRow(0).getCell(col).toString();
                        try {
                            Cell cell = sheet.getRow(row).getCell(col);
                            cell.setCellType(Cell.CELL_TYPE_STRING);
                            value = sheet.getRow(row).getCell(col).toString().replaceAll("\n", "").trim();
                            value = Util.replaceSpaceAndTabs(value);
                        } catch (Exception e) {
                            value = "";
                        }

                        dataMap.put(key, value);
                    }

                    getDataList.add(dataMapCounter, dataMap);
                    startRow = row + 1;
                    data.add(dataMapCounter++, dataMap1);
                    test = test + 1;
                    break;
                }
            }

            finalList.add(getDataList.get(0));
            for (int row = startRow; row <= sheet.getLastRowNum(); row++) {
                HashMap<String, String> dataMap = new HashMap<String, String>();

                String getKeyName = sheet.getRow(row).getCell(0).toString();
                if (Objects.equals(getKeyName, "")) {
                    for (int col = 0; col < sheet.getRow(0).getLastCellNum(); col++) {
                        String key = sheet.getRow(0).getCell(col).toString();
                        try {
                            Cell cell = sheet.getRow(row).getCell(col);
                            cell.setCellType(Cell.CELL_TYPE_STRING);
                            value = sheet.getRow(row).getCell(col).toString().trim();
                            value = Util.replaceSpaceAndTabs(value);
                        } catch (Exception e) {
                            value = "";
                        }

                        dataMap.put(key, value);

                    }
                    getDataList.add(dataMapCounter, dataMap);
                    test = test + 1;
                } else {
                    break;
                }
            }
            getDataList.remove(0);
            Collections.reverse(getDataList);

            finalList.addAll(getDataList);

        } catch (Exception e) {
            assertThat("Error: " + e, false);
            return null;
        }

        return finalList;
    }

    public static String getCSVFormat(List<Map<String, Object>> result) {
        List<Map<String, String>> res = toReverseOrderMap(result);
        return createCSVTable(res);
    }

    private static List<Map<String, String>> toReverseOrderMap(List<Map<String, Object>> list) {
        List<Map<String, String>> res = new ArrayList<Map<String, String>>();

        for (Map<String, Object> row : list) {
            Map<String, String> copy = new LinkedHashMap<String, String>();
            for (Map.Entry m1 : row.entrySet()) {
                String key = m1.getKey().toString();
                String value = "";
                if (key.startsWith("XMLCONTENT") && m1.getValue() != null) {
                    value = XmlUtils.formatXML(m1.getValue().toString());
                } else if (m1.getValue() != null) {
                    value = m1.getValue().toString();
                } else {
                    value = "";
                }
                copy.put(key, value);
            }
            res.add(copy);
        }
        return res;
    }

    public static String createCSVTable(List<Map<String, String>> table) {
        if (table.isEmpty() || table.get(0).isEmpty()) {
            return "";
        }
        List<Integer> maxLengths = findLength(table);
        String tableHeaders = createRow(maxLengths, new ArrayList<Object>(table.get(0).keySet()), CSV_COL_DELIM);
        StringBuilder sb = new StringBuilder();
        sb.append(tableHeaders);
        for (Map<String, String> row : table) {
            sb.append(createRow(maxLengths, new ArrayList<Object>(row.values()), CSV_COL_DELIM));
        }
        return sb.toString();
    }

    private static String createRow(List<Integer> colMaxLengths, List<Object> elements, String delim) {
        StringBuilder res = new StringBuilder();
        for (int i = 0; i < elements.size(); i++) {
            String val = elements.get(i).toString();
            if (val.contains("xmlns")) {
                val = XmlUtils.formatXML(val);
            }
            res.append(val);
            res.append(getSpaces(colMaxLengths.get(i) - val.length()));
            res.append(delim);
        }
        res.append("\n");
        return res.toString();
    }

    private static List<Integer> findLength(List<Map<String, String>> table) {
        List<Integer> maxLengths = new ArrayList<Integer>();
        for (String key : table.get(0).keySet()) {
            int len = key.length();
            if (len > ROW_MAX_LENGTH) {
                len = ROW_MAX_LENGTH;
            }
            maxLengths.add(len);
        }
        int cnt = 0;
        for (Map<String, String> row : table) {
            cnt = 0;
            for (String value : row.values()) {
                Integer curr = maxLengths.get(cnt);
                if (curr == ROW_MAX_LENGTH) {
                    continue;
                }
                int len = value.length();
                if (len > ROW_MAX_LENGTH) {
                    maxLengths.set(cnt, ROW_MAX_LENGTH);
                } else if (len > curr) {
                    maxLengths.set(cnt, len);
                }
                cnt++;
            }
        }
        return maxLengths;
    }

    public static void printInputSheet(String testDataType, List<HashMap<String, String>> getDataList) {
        String printInputData = "";
        int rowNum = 1;
        for (Map<String, String> actualMap : getDataList) {

            printInputData = printInputData + "------------------Row:" + rowNum + "------------------\n";
            for (Map.Entry m : actualMap.entrySet()) {
                String columnName = String.format("%1$-" + 40 + "s", m.getKey());
                printInputData = printInputData + "|" + columnName + " | " + m.getValue().toString() + "\n";
            }
            printInputData = printInputData + "\n\n";
            rowNum++;
        }
        LogUtil.logAttachment(testDataType, printInputData);
    }

    public static HashMap<Integer, String> readKeysInDataSheet(String SheetName) {
        HashMap<Integer, String> keysList = new HashMap<>();

        String fileLocations = new File("src/main/resources").getAbsoluteFile().toString();
        try {
            File file = new File(fileLocations + "/input_data/" + SheetName + ".xlsx");
            FileInputStream inputStream = new FileInputStream(file);
            Workbook workbook = new XSSFWorkbook(inputStream);
            Sheet sheet = workbook.getSheet("data");

            for (int row = 1; row <= sheet.getLastRowNum(); row++) {
                keysList.put(row, sheet.getRow(row).getCell(0).toString());
            }

        } catch (Exception e) {
        }

        return keysList;
    }


}
