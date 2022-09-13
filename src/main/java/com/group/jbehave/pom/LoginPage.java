package com.group.jbehave.pom;

import com.codeborne.selenide.*;
import com.group.bdd.framework.LogUtil;
import com.group.bdd.framework.web.BrowserDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.pagefactory.AjaxElementLocatorFactory;

import static com.codeborne.selenide.Selenide.$;
import static com.group.bdd.framework.Asserts.assertThat;
import static com.group.bdd.framework.ConfigLoader.config;

public class LoginPage {

    public LoginPage() {
        PageFactory.initElements(new AjaxElementLocatorFactory(BrowserDriver.getDriver(), 5), this);
    }

    String ENV = config().getString("test.environment");

    @FindBy(id = "user-name")
    public WebElement userId;

    @FindBy(id = "password")
    public WebElement pwd;

    @FindBy(id = "login-button")
    public WebElement loginButton;

    @FindBy(xpath = "//span[contains(text(), 'Products')]")
    public WebElement productsList;

    @FindBy(id = "react-burger-menu-btn")
    public WebElement openMenu;

    @FindBy(id = "logout_sidebar_link")
    public WebElement logOutButton;

    public void openUrl() {
        try {
            String url = config().getString(ENV + ".URL");
            WebDriverRunner.setWebDriver(BrowserDriver.getDriver());
            BrowserDriver.open(url);
            LogUtil.takeScreenshot("After Open");
        } catch (Exception e) {
            assertThat("Exception: " + e.getLocalizedMessage(), false);
        }
    }

    public void enterLogindetails(String userid, String password) {
        try {
            userId.sendKeys(userid);
            pwd.sendKeys(password);
            LogUtil.takeScreenshot("Before clicking on Login");
            $(loginButton).click();
            LogUtil.takeScreenshot("After clicking on Login");
        } catch (Exception e) {
            assertThat("Exception: " + e.getLocalizedMessage(), false);
        }
    }

    public void clickLogout() {
        try {
            $(openMenu).click();
            $(logOutButton).click();
            $(loginButton).shouldBe(Condition.visible);
            LogUtil.takeScreenshot("Logged out of application");
        } catch (Exception e) {
            assertThat("Exception: " + e.getLocalizedMessage(), false);
        }
    }
}
