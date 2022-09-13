package com.group.jbehave.utilities;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.group.bdd.framework.LogUtil;
import com.group.jbehave.service.DatabaseDao;

import static com.group.bdd.framework.Asserts.assertThat;

public class DBUtils {

    static DatabaseDao databaseDao = new DatabaseDao();

    public List<Map<String, Object>> getDBRows(String dbName, String sqlQuery) {
        return databaseDao.runDBQueryImpl(dbName, sqlQuery);
    }

    public String runUpdateQuery(String sqlQuery, String dbName) {
        return databaseDao.runAnyQuery(dbName, sqlQuery);
    }

    public String runBatchQueries(String sqlQuery, String dbName) {
        return databaseDao.runBatchQueries(dbName, sqlQuery);
    }

    public void runAnyQuery(String database, String sqlQuery, boolean isLog) {
        String result = databaseDao.runAnyQuery(database, sqlQuery);
        if (isLog) {
            LogUtil.log(sqlQuery);
        }
        if (result.contains(" - FAILED")) {
            assertThat("Error occurred in executing the query. See report for more details. ==> " + result, false);
        }
    }

    public static void runAnyQueryLog(String database, String sqlQuery, boolean isLog) {
        String result = databaseDao.runAnyQuery(database, sqlQuery);
        if (isLog) {
            LogUtil.log(sqlQuery);
        }
        if (result.contains(" - FAILED")) {
            assertThat("Error occurred in executing the query. See report for more details. ==> " + result, false);
        }
    }

    public static List<Map<String, Object>> executeTheDatabaseQuery(String sqlQuery, String database, boolean isLogEnabled) {
        List<Map<String, Object>> actualResult = databaseDao.runDBQueryImpl(database, sqlQuery);
        if (isLogEnabled) {
            if (actualResult.size() >= 1) {
                LogUtil.logCSVAttachment(sqlQuery, ExcelUtils.getCSVFormat(actualResult));
            } else {
                LogUtil.logCSVAttachment(sqlQuery, ExcelUtils.getCSVFormat(getDBHeaders(sqlQuery)));
            }
        }
        return actualResult;
    }

    public static List<Map<String, Object>> executeTheDatabaseQueryWithLoopCount(String sqlQuery, String database, boolean isLogEnabled, int loopCount) {
        List<Map<String, Object>> actualResult = new ArrayList<>();
        for (int i = 0; i < loopCount; i++) {
            actualResult = executeTheDatabaseQuery(sqlQuery, database, false);
            if (actualResult.size() >= 1) {
                break;
            } else {
                Util.sleep(1000);
            }
        }
        actualResult = executeTheDatabaseQuery(sqlQuery, database, isLogEnabled);
        return actualResult;
    }

    public static String executeTheDatabaseQueryClob(String sqlQuery, String database, boolean isLogEnabled, String clobColumn) {
        String columnValue = databaseDao.runDBQueryImplClobType(database, sqlQuery, clobColumn);
        if (isLogEnabled) {
            LogUtil.logAttachment(sqlQuery, columnValue);
        }
        return columnValue;
    }

    public static String executeTheXmlTypeDatabaseQuery(String sqlQuery, String database, boolean isLogEnabled) {
        String actualResult = databaseDao.runDBQueryForXMLType(database, sqlQuery);
        return actualResult;
    }

    public static boolean waitForDBResult(String sqlQuery1, int timeoutSecs) {
        List<Map<String, Object>> resultSet = new ArrayList<>();
        boolean resultFound = false;

        for (int loop = 1; loop <= timeoutSecs; loop++) {
            resultSet = executeTheDatabaseQuery(sqlQuery1, "testDB", false);
            if (resultSet.size() >= 1) {
                resultFound = true;
                break;
            } else {
                Util.sleep(1000);
            }
        }
        return resultFound;
    }

    public static List<Map<String, Object>> getDBHeaders(String sqlQuery) {
        List<Map<String, Object>> list = new ArrayList<>();
        Map<String, Object> map = new LinkedHashMap<>();

        String[] alias = sqlQuery.substring(sqlQuery.indexOf("FROM")).split(" ");
        sqlQuery = sqlQuery.replaceAll(alias[2] + ".", "");
        sqlQuery = sqlQuery.replaceAll("\\.GETSTRINGVAL\\(\\) AS XMLCONTENT", "").trim();
        String[] splitColumns = sqlQuery.split("FROM");
        splitColumns = splitColumns[0].trim().substring(7).split(",");
        for (String column : splitColumns) {
            map.put(column, " ");
        }
        list.add(map);
        return list;
    }

}
