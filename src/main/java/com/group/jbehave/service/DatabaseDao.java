package com.group.jbehave.service;

import static com.group.jbehave.utilities.DBUtils.getDBHeaders;
import static java.lang.String.format;
import static com.group.bdd.framework.Asserts.assertThat;
import static com.group.bdd.framework.ConfigLoader.config;

import java.sql.*;
import java.util.List;
import java.util.Map;

import com.group.bdd.framework.DbActions;
import com.group.bdd.framework.LogUtil;
import com.group.jbehave.utilities.ExcelUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DatabaseDao {

    private static final Logger LOG = LogManager.getLogger(DatabaseDao.class);

    private static String environment = config().getString("test.environment");
    private static final String CONNECTION_STRING_TEMPLATE = "jdbc:oracle:thin:%s/%s@%s:%s:%s";

    private static DatabaseDao instance = new DatabaseDao();

    public static DatabaseDao getInstance() {
        return instance;
    }

    private String dbConnString = "";

    private String connString(String user, String pass, String host, String port, String sessionId) {
        return format(CONNECTION_STRING_TEMPLATE, config().getString(user), config().getString(pass),
                config().getString(host), config().getString(port), config().getString(sessionId));
    }

    public DatabaseDao() {
        dbConnString = connString(environment + ".testDB.user", environment + ".testDB.pass", environment + ".testDB.host", environment + "testDB.port",
                environment + ".testDB.sessionId");
    }

    private String getConnString(String database) {
        String connString = connString(environment + "." + database + ".user", environment + "." + database + ".pass", environment + "." + database + ".host",
                environment + "." + database + ".port", environment + "." + database + ".sessionId");
        return connString;
    }

    public List<Map<String, Object>> runQueryImpl(String dataSource, String sql) {
        List<Map<String, Object>> result = null;
        Connection connection = null;
        Statement stat = null;
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
            connection = DriverManager.getConnection(dataSource);
            stat = connection.createStatement();
            ResultSet rs = stat.executeQuery(sql);
            LOG.info("SQL Query : " + sql);
            result = DbActions.QueryExecutor.asMaps(rs);
        } catch (SQLRecoverableException e) {
            result = runQueryImpl(dataSource, sql);
        } catch (Exception e) {
            assertThat("Error running query: \n" + sql + "\n" + e, false);
        } finally {
            try {
                if (stat != null) {
                    stat.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (Exception e) {
                assertThat("Error: " + e.getMessage(), false);
            }
        }
        return result;
    }


    public String runAnyQuery(String database, String sqlQuery) {
        String result = "";
        String dataSource = null;

        dataSource = getConnString(database);

        Statement stat = null;
        Connection connection = null;
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
            if (config().getString(environment + "." + database + ".sessionId").equalsIgnoreCase("")) {
                dataSource = dataSource.substring(0, dataSource.length() - 1);
            }
            connection = DriverManager.getConnection(dataSource);
            stat = connection.createStatement();
            stat.executeUpdate(sqlQuery);
            result = sqlQuery + " - SUCCESS";
        } catch (SQLRecoverableException e) {
            result = runAnyQuery(database, sqlQuery);
        } catch (Exception e) {
            result = sqlQuery + ". Error: " + e.getMessage() + " - FAILED";
        } finally {
            try {
                if (stat != null) {
                    stat.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (Exception e) {
                assertThat("Error: " + e.getMessage(), false);
            }
        }
        return result;
    }

    public List<Map<String, Object>> runDBQueryImpl(String database, String sql) {
        String dataSource = getConnString(database);
        if (config().getString(environment + "." + database + ".sessionId").equalsIgnoreCase("")) {
            dataSource = dataSource.substring(0, dataSource.length() - 1);
        }
        List<Map<String, Object>> result = null;
        Connection connection = null;
        Statement stat = null;
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
            connection = DriverManager.getConnection(dataSource);
            stat = connection.createStatement();
            ResultSet rs = stat.executeQuery(sql);
            LOG.info("SQL Query : " + sql);
            result = DbActions.QueryExecutor.asMaps(rs);
        } catch (SQLRecoverableException e) {
            result = runQueryImpl(dataSource, sql);
        } catch (Exception e) {
            assertThat("Error running query: \n" + sql + "\n" + e, false);
        } finally {
            try {
                if (stat != null) {
                    stat.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (Exception e) {
                assertThat("Error: " + e.getMessage(), false);
            }
        }
        return result;
    }

    public String runBatchQueries(String database, String sqlQuery) {
        String result = "";
        String dataSource = null;

        dataSource = getConnString(database);

        Statement stat = null;
        Connection connection = null;
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
            if (config().getString(environment + "." + database + ".sessionId").equalsIgnoreCase("")) {
                dataSource = dataSource.substring(0, dataSource.length() - 1);
            }
            connection = DriverManager.getConnection(dataSource);
            stat = connection.createStatement();
            for (String query : sqlQuery.split(";")) {
                stat.addBatch(query.trim());
            }
            stat.executeBatch();
            result = sqlQuery + " - SUCCESS";
        } catch (SQLRecoverableException e) {
            result = runBatchQueries(database, sqlQuery);
        } catch (Exception e) {
            result = sqlQuery + ". Error: " + e.getMessage() + " - FAILED";
        } finally {
            try {
                if (stat != null) {
                    stat.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (Exception e) {
                assertThat("Error: " + e.getMessage(), false);
            }
        }
        return result;
    }

    public String runDBQueryImplClobType(String database, String sql, String clobColumn) {
        String dataSource = getConnString(database);
        List<Map<String, Object>> result = null;
        Connection connection = null;
        Statement stat = null;
        String clobValue = "";
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
            connection = DriverManager.getConnection(dataSource);
            stat = connection.createStatement();
            ResultSet rs = stat.executeQuery(sql);
            LOG.info("SQL Query : " + sql);
            result = DbActions.QueryExecutor.asMaps(rs);

        } catch (SQLRecoverableException e) {
            result = runQueryImpl(dataSource, sql);
        } catch (Exception e) {
            assertThat("Error running query: \n" + sql + "\n" + e, false);
        } finally {
            if (result.size() == 0) {
                assertThat("Records not found for: " + sql, false);
            }
            Clob clob = (Clob) result.get(0).get(clobColumn);
            if (clob != null) {
                try {
                    int size = (int) clob.length();
                    clobValue = clob.getSubString(1, size);
                    //LogUtil.log(clobValue);
                } catch (SQLException e) {
                    clobValue = "";
                    assertThat("Error: " + e, false);
                }
            }

            try {
                if (stat != null) {
                    stat.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (Exception e) {
                assertThat("Error: " + e.getMessage(), false);
            }
        }
        return clobValue;
    }

    public String runDBQueryForXMLType(String database, String sql) {
        String dataSource = getConnString(database);
        List<Map<String, Object>> result = null;
        Connection connection = null;
        Statement stat = null;
        String columnVal = "";
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
            connection = DriverManager.getConnection(dataSource);
            stat = connection.createStatement();
            ResultSet rs = stat.executeQuery(sql);

            while (rs.next()) {
                byte[] xmlVal = rs.getBytes(1);
                if (xmlVal == null) {
                    columnVal = "null";
                } else {
                    columnVal = new String(xmlVal);
                }
            }

            if (!columnVal.equals("null")) {
                int firstOpen = columnVal.indexOf("<");
                int firstClose = columnVal.indexOf(">");
                if (firstOpen < firstClose) {
                    int firstLineLength = columnVal.indexOf(">");
                    String firstLine = columnVal.substring(0, firstLineLength);
                    String remainLine = columnVal.substring(firstLineLength);
                    int lastIndex = firstLine.lastIndexOf("<");
                    firstLine = firstLine.substring(lastIndex);
                    columnVal = firstLine + remainLine;
                } else {
                    int startLength = columnVal.indexOf("<");
                    columnVal = columnVal.substring(startLength);
                }
                LogUtil.logAttachmentXML(sql, columnVal);
            } else {
                LogUtil.logCSVAttachment(sql, ExcelUtils.getCSVFormat(getDBHeaders(sql)));
            }

        } catch (SQLRecoverableException e) {
            result = runQueryImpl(dataSource, sql);
        } catch (Exception e) {
            assertThat("Error running query: \n" + sql + "\n" + e, false);
        } finally {
            try {
                if (stat != null) {
                    stat.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (Exception e) {
                assertThat("Error: " + e.getMessage(), false);
            }
        }
        return columnVal;
    }

}
