package com.group.jbehave.steps;

import com.group.bdd.framework.jira.JiraEntity;
import com.group.bdd.framework.jira.JiraZephyrTestCaseManagement;
import com.group.bdd.framework.LogUtil;
import com.group.bdd.framework.StorySteps;
import com.group.jbehave.utilities.ExcelUtils;
import com.group.jbehave.utilities.FileUtils;
import com.group.jbehave.utilities.Util;
import org.jbehave.core.annotations.*;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import static com.group.bdd.framework.Asserts.assertThat;
import static com.group.bdd.framework.ConfigLoader.config;

@StorySteps
public class BaseSteps {
    private JiraZephyrTestCaseManagement jiraClient = new JiraZephyrTestCaseManagement();
    public static HashMap<String, String> loginUsers = new HashMap<>();

    @BeforeStories
    public void initSetup() {
        setJiraSystemProperties();
        manageCredentials();
    }

    @AfterStories
    public void finishSetup() {
        jiraClient.updateExecutionResults();
    }

    @BeforeScenario(uponType = ScenarioType.ANY)
    public void beforeScenario() {
    }

    @AfterScenario(uponType = ScenarioType.ANY)
    public void afterScenario() {
    }

    @Given("Read the Test case Names: $sourcePath")
    public void givenReadTheTestCaseNamesTestdatasetup(String sourcePath) {
        LogUtil.log(Thread.currentThread().getName());
        String fileLocations = new File("src/main/resources").getAbsoluteFile().toString();
        String storyPath = fileLocations + "/" + sourcePath + "/";
        LinkedList<String> list = new LinkedList();
        list = FileUtils.listFilesAndFilesSubDirectories(storyPath, list);
        LinkedList<String> ignoreList = new LinkedList();
        ignoreList.add("Common");
        LinkedList<Map<String, String>> readList = new LinkedList();
        for (String fileName : list) {
            File f = new File(fileName);
            if (!ignoreList.contains(f.getParentFile().getName())) {
                readList = Util.readTestcasesAndKeys(fileName, readList, sourcePath);
            }
        }
        LogUtil.logCSVAttachment("Testcase List", ExcelUtils.createCSVTable(readList));
    }

    private void setJiraSystemProperties() {
        System.setProperty("JIRA.UPDATE", config().getString("JIRA.UPDATE"));
        System.setProperty("JIRA_URL", config().getString("JIRA_URL"));
        System.setProperty("JIRA_USERNAME", config().getString("JIRA_USERNAME"));
        System.setProperty("JIRA_PASSWORD", config().getString("JIRA_PASSWORD"));
        System.setProperty("JIRA.INPUT_FORMAT", config().getString("JIRA.INPUT_FORMAT"));
        System.setProperty("JIRA.INPUT_LOCATION", config().getString("JIRA.INPUT_LOCATION"));
        System.setProperty("JIRA.PROJECT_NAME", config().getString("JIRA.PROJECT_NAME"));
        System.setProperty("JIRA.CYCLE_NAME", config().getString("JIRA.CYCLE_NAME"));
        System.setProperty("JIRA.FOLDER_NAME", config().getString("JIRA.FOLDER_NAME"));
        System.setProperty("JIRA.VERSION", config().getString("JIRA.VERSION"));
        System.setProperty("JIRA.REPORT_TYPE", config().getString("JIRA.REPORT_TYPE"));
        System.setProperty("JIRA.REPORTS_DIR", config().getString("JIRA.REPORTS_DIR"));
        System.setProperty("JIRA.BUILD_NUMBER", config().getString("JIRA.BUILD_NUMBER"));
        System.setProperty("JIRA.ALLURE_DIR", config().getString("JIRA.ALLURE_DIR"));
        System.setProperty("JIRA.CUCUMBER_DIR", config().getString("JIRA.CUCUMBER_DIR"));
        System.setProperty("JIRA.THREADS_COUNT", config().getString("JIRA.THREADS_COUNT"));

        setJiraEntity();
    }

    /*To be called once properties are loaded from framework*/
    private static void setJiraEntity() {
        JiraEntity.setJiraUrl(System.getProperty("JIRA_URL"));
        JiraEntity.setJiraUpdate(System.getProperty("JIRA.UPDATE"));
        JiraEntity.setJiraUsername(System.getProperty("JIRA_USERNAME"));
        JiraEntity.setJiraPassword(System.getProperty("JIRA_PASSWORD"));
        JiraEntity.setJiraInputFormat(System.getProperty("JIRA.INPUT_FORMAT"));
        JiraEntity.setJiraInputLocation(System.getProperty("JIRA.INPUT_LOCATION"));
        JiraEntity.setHeaderAo7deabf(JiraZephyrTestCaseManagement.getJiraCustomHeader("AO-7DEABF"));
        JiraEntity.setJiraProject(System.getProperty("JIRA.PROJECT_NAME"));
        JiraEntity.setJiraCycleName(System.getProperty("JIRA.CYCLE_NAME"));
        JiraEntity.setJiraFolderName(System.getProperty("JIRA.FOLDER_NAME"));
        JiraEntity.setJiraVersion(System.getProperty("JIRA.VERSION"));
        JiraEntity.setJiraReportType(System.getProperty("JIRA.REPORT_TYPE"));
        JiraEntity.setJiraReportsDir(System.getProperty("JIRA.REPORTS_DIR"));
        JiraEntity.setJiraBuildNumber(System.getProperty("JIRA.BUILD_NUMBER"));
        JiraEntity.setJiraThreadsCount(Integer.parseInt(System.getProperty("JIRA.THREADS_COUNT", "1")));
    }

    private static synchronized void manageCredentials() {
        String ENV = config().getString("test.environment");
        Iterator<String> keys = config().getKeys(ENV);
        while (keys.hasNext()) {
            String key = keys.next();
            if (key.startsWith(ENV + ".loginUser") && !key.endsWith("_password")) {
                loginUsers.put(key, "Available");
            }
        }
        LogUtil.log("Login Users" + loginUsers.toString());
    }

    public synchronized HashMap<String, String> getAvailableUser(String userType) {
        HashMap<String, String> userMap = new HashMap<>();
        LogUtil.log("Thread Name: " + Thread.currentThread().getName());
        String threadName = Thread.currentThread().getName();
        threadWaitTime(threadName);
        try {
            if (loginUsers.size() < Integer.parseInt(config().getString("bdd.thread"))) {
                assertThat("Thread's count(" + config().getString("bdd.thread") + ") is higher than available Users(" + loginUsers.size() + "). Please check the configurations!.", false);
            }
            if (userType.equals("login")) {
                for (String key : loginUsers.keySet()) {
                    if (loginUsers.get(key).equals("Available")) {
                        LogUtil.log("Available Login User:" + config().getString(key));
                        userMap.put("User", config().getString(key));
                        userMap.put("Password", config().getString(key + "_password"));
                        userMap.put("userType", "login");
                        userMap.put("propertyKey", key);
                        loginUsers.put(key, "InUse");
                        break;
                    }
                }
            }
        } catch (Exception ex) {
            assertThat("Exception - No Users Available At the Moment." + ex.getLocalizedMessage(), false);
        }

        return userMap;
    }

    public synchronized void releaseUser(String user) {
        LogUtil.log("Releasing User(" + user + "): " + config().getString(user));
        if (user.contains("loginUser")) {
            loginUsers.put(user, "Available");
        }
    }

    private void threadWaitTime(String threadName) {
        int number = Integer.parseInt(threadName.substring(threadName.length() - 1));
        if (number % 2 == 0) {
            Util.sleep(2000);
        }
    }

}
