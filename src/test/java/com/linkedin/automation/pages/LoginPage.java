
package com.linkedin.automation.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class LoginPage {

    private WebDriver driver;
    private WebDriverWait wait;

    private final String LOGIN_URL = "https://www.linkedin.com/login";

    private final By usernameField =
            By.cssSelector("input[type='email']");

    private final By passwordField =
            By.cssSelector("input[type='password']");

    private final By signInButton =
            By.xpath("//button[.//span[normalize-space()='Sign in']]");

    private final By errorMessages =
            By.cssSelector(
                    "[role='alert'], #error-for-password, .form__label--error"
            );

    public LoginPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(30));
    }

    // Open LinkedIn Login Page
    public void openLoginPage() {
        driver.get(LOGIN_URL);
    }

    // Enter Email
    public void enterEmail(String email) {

        WebElement username = wait.until(d ->
                d.findElements(usernameField).stream()
                        .filter(WebElement::isDisplayed)
                        .filter(WebElement::isEnabled)
                        .findFirst()
                        .orElse(null)
        );

        username.clear();
        username.sendKeys(email);
    }

    // Enter Password
    public void enterPassword(String password) {

        WebElement passwordElement = wait.until(d ->
                d.findElements(passwordField).stream()
                        .filter(WebElement::isDisplayed)
                        .filter(WebElement::isEnabled)
                        .findFirst()
                        .orElse(null)
        );

        passwordElement.clear();
        passwordElement.sendKeys(password);
    }

    // Click Sign In Button
    public void clickSignIn() {

        WebElement button = wait.until(d ->
                d.findElements(signInButton).stream()
                        .filter(WebElement::isDisplayed)
                        .filter(WebElement::isEnabled)
                        .findFirst()
                        .orElse(null)
        );

        button.click();
    }

    // Perform Login
    public void login(String email, String password) {
        enterEmail(email);
        enterPassword(password);
        clickSignIn();
    }

    // Get Login Result
    public String getLoginResult() {

        WebDriverWait resultWait =
                new WebDriverWait(driver, Duration.ofSeconds(30));

        return resultWait.until(d -> {

            String pageText = d.findElement(By.tagName("body"))
                    .getText().toLowerCase();

            // Security Verification Check
            if (pageText.contains("quick security check")
                    || pageText.contains("security verification")
                    || pageText.contains("verify your identity")) {

                return "SECURITY_CHECK";
            }

            // Invalid Login Message Check
            if (pageText.contains("wrong email or password")
                    || pageText.contains("incorrect email or password")
                    || pageText.contains("email or password is incorrect")
                    || pageText.contains("incorrect password")
                    || pageText.contains("wrong password")
                    || pageText.contains("password is incorrect")) {

                return "INVALID_LOGIN: Wrong email or password";
            }

            // Check Error Elements
            for (WebElement element : d.findElements(errorMessages)) {

                if (element.isDisplayed()) {

                    String message = element.getText().toLowerCase();

                    if (message.contains("incorrect")
                            || message.contains("wrong")
                            || message.contains("doesn't match")
                            || message.contains("not correct")) {

                        return "INVALID_LOGIN: " + message;
                    }
                }
            }

            return null;
        });
    }
}
