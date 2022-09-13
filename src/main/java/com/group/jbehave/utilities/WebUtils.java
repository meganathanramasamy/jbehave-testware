package com.group.jbehave.utilities;

import com.group.bdd.framework.LogUtil;
import com.group.bdd.framework.web.BrowserDriver;
import com.group.jbehave.steps.UiSteps;
import org.apache.log4j.Logger;
import org.openqa.selenium.*;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.Select;

import java.util.*;

import static com.group.bdd.framework.Asserts.assertThat;

public class WebUtils {

    final static Logger LOG = Logger.getLogger(WebUtils.class);

    private void scrolltoElementJavaScript(WebElement element) {
        ((JavascriptExecutor) BrowserDriver.getDriver()).executeScript("arguments[0].scrollIntoView(true);", element);
    }

    public List<Map<String, Object>> createMapFromTable(List<WebElement> tableRows, String className) {
        List<Map<String, Object>> tableMap = new ArrayList<>();
        for (WebElement row : tableRows) {
            if (!row.getAttribute("class").equals(className)) {
                Map<String, Object> tmpMap = new TreeMap<>();
                List<WebElement> dataCells = row.findElements(By.tagName("td"));
                for (WebElement data : dataCells) {
                    tmpMap.put(data.getAttribute("id"), data.getText());
                }
                tableMap.add(tmpMap);
            }
        }
        return tableMap;
    }

    public void closeBlankPageandSwitchtoNewWindow() {
        Set s = BrowserDriver.getDriver().getWindowHandles();
        Iterator itr = s.iterator();
        while (itr.hasNext()) {
            String txnWndwHandle = itr.next().toString();
            if (!txnWndwHandle.contains(UiSteps.thread_windowHndl.get())) {
                try {
                    BrowserDriver.getDriver().switchTo().window(txnWndwHandle);
                    if (BrowserDriver.getDriver().getTitle().contains("Blank")) {
                        LogUtil.log("Close Blank window -- Txn Window Handle: " + txnWndwHandle);
                        BrowserDriver.getDriver().close();
                        BrowserDriver.getDriver().switchTo().window(UiSteps.thread_windowHndl.get());
                    } else {
                        LogUtil.log("Switched focus to window with title: " + BrowserDriver.getDriver().getTitle());
                    }
                } catch (NoSuchWindowException e) {
                    s = BrowserDriver.getDriver().getWindowHandles();
                    itr = s.iterator();
                } catch (Exception E) {
                    assertThat("Exceptions: " + E.getLocalizedMessage(), false);
                }
            }
        }
    }

    public void waitforLoadingtoComplete() {
        for (int i = 0; i <= 20; i++) {
            try {
                if (BrowserDriver.getDriver().findElements(By.xpath("//div[@class='spinner']")).size() >= 1) {
                    LogUtil.log("Waiting for loading to complete");
                    Util.sleep(2000);
                }
            } catch (NoSuchElementException e) {
                break;
            }
        }
    }

    public static void closeAllWindowsExceptMainWindow() {
        try {
            for (String winHandle : BrowserDriver.getDriver().getWindowHandles()) {
                if (!UiSteps.thread_windowHndl.get().contains(winHandle)) {
                    BrowserDriver.getDriver().switchTo().window(winHandle).close();
                    BrowserDriver.getDriver().switchTo().window(UiSteps.thread_windowHndl.get());
                }
            }
        } catch (Exception e) {
            closeAllWindowsExceptMainWindow();
        }
    }

    public static void waitForExpectedWindowSize(int windowSize) {
        try {
            int i;
            int retryLimit = 20;
            int retryIntervalSecs = 2;
            for (i = 0; i < retryLimit; i++) {
                Set s = BrowserDriver.getDriver().getWindowHandles();
                LOG.info("Count " + (i + 1) + ": " + s.size());
                if (s.size() == windowSize) {
                    break;
                } else {
                    Util.sleep(retryIntervalSecs * 1000);
                }
            }
            if (i == retryLimit) {
                closeAllWindowsExceptMainWindow();
                assertThat("No of windows is not as expected after " + (retryLimit * retryIntervalSecs) + " secs", false);
            }
        } catch (Exception e) {
            closeAllWindowsExceptMainWindow();
            assertThat("No of windows are not as expected" + e, false);
        }
    }

    public static void moveAndTakeScreenshots(String message, WebElement element, String columnDiv) {
        if (BrowserDriver.hasInstance()) {
            List<WebElement> elemementsList = BrowserDriver.getDriver().findElements(By.xpath(columnDiv));
            LogUtil.attachScreenshotOnWebElement(message, element);
            for (int i = 9; i < elemementsList.size(); ) {
                Actions action = new Actions(BrowserDriver.getDriver());
                action.moveToElement(elemementsList.get(i)).perform();
                LogUtil.attachScreenshotOnWebElement(message, element);
                i = i + 6;
            }
        }
    }

    public static boolean javaScriptClick(WebElement element) {

        try {
            if (element.isEnabled() && element.isDisplayed()) {
                ((JavascriptExecutor) BrowserDriver.getDriver()).executeScript("arguments[0].click();", element);
                return true;
            } else {
                return false;
            }
        } catch (StaleElementReferenceException e) {
            return true;
        }
    }

    public void selectOptionfromDropDown(WebElement drpDownEle, String optionText) {
        if (drpDownEle.isEnabled()) {
            Select dropDown = new Select(drpDownEle);
            dropDown.selectByVisibleText(optionText);
            Util.sleep(2000);
        } else {
            assertThat("Dropdown field '" + drpDownEle.getAttribute("name") + "' is disabled", false);
        }
    }

    public static void validateselectedOptionsfromDropDown(WebElement selectElement, List<String> expectedOptions) {
        try {
            Select sel = new Select(selectElement);
            List<WebElement> selOptions = sel.getAllSelectedOptions();
            List<String> selectedValues = new ArrayList<String>();

            for (WebElement element : selOptions) {
                selectedValues.add(element.getText());
            }

            if (expectedOptions.containsAll(selectedValues) && expectedOptions.size() == selectedValues.size()) {
                for (int i = 0; i < expectedOptions.size(); i++) {
                    LogUtil.log(expectedOptions.get(i) + " is selected from the Select list.");
                }
                LogUtil.attachScreenshot("Select box Screen");
            }
        } catch (Exception e) {
            assertThat("Exception: " + e.getLocalizedMessage(), false);
        }
    }

}
