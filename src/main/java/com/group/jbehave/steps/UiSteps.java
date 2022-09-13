package com.group.jbehave.steps;

import com.codeborne.selenide.Condition;
import com.group.bdd.framework.LogUtil;
import com.group.bdd.framework.StorySteps;
import com.group.bdd.framework.web.BrowserDriver;
import com.group.jbehave.pom.LoginPage;
import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;

import java.util.HashMap;

import static com.codeborne.selenide.Selenide.$;
import static com.group.bdd.framework.Asserts.assertThat;

@StorySteps
public class UiSteps {

    private LoginPage loginPage() {
        return new LoginPage();
    }

    public static ThreadLocal<String> userName = new ThreadLocal<>();
    public static ThreadLocal<String> password = new ThreadLocal<>();
    public static ThreadLocal<String> userParameter = new ThreadLocal<>();
    public static ThreadLocal<String> thread_windowHndl = new ThreadLocal<>();

    public BaseSteps baseSteps = new BaseSteps();

    @Given("Open the Login Page")
    public void openLoginPage() {
        try {
            loginPage().openUrl();
        } catch (Exception e) {
            assertThat("Exception - Login Method failed." + e.getMessage(), false);
        }
    }

    @When("User fill details and click Login")
    public void loginToTheApplication() {
        try {
            HashMap<String, String> user = baseSteps.getAvailableUser("login");
            userName.set(user.get("User"));
            password.set(user.get("Password"));
            userParameter.set(user.get("propertyKey"));
            loginPage().enterLogindetails(userName.get(), password.get());
            thread_windowHndl.set(BrowserDriver.getDriver().getWindowHandle());
        } catch (Exception e) {
            assertThat("Exception - Login Method failed." + e.getMessage(), false);
        }
    }

    @Then("Verify Login is success")
    public void checkloginSuccess() {
        try {
            $(loginPage().productsList).shouldBe(Condition.visible);
            LogUtil.takeScreenshot("Login Success");
        } catch (Exception e) {
            assertThat("Exception - Login Method failed." + e.getMessage(), false);
        }
    }

    @Then("Log out of application")
    public void thenLogout() {
        try {
            loginPage().clickLogout();
            baseSteps.releaseUser(userParameter.get());
        } catch (Exception e) {
            assertThat("Exception: " + e.getLocalizedMessage(), false);
        }
    }
}
