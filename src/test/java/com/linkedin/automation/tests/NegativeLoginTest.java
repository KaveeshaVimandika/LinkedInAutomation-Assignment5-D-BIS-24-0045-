
package com.linkedin.automation.tests;

import com.linkedin.automation.base.BaseTest;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.time.Duration;

public class NegativeLoginTest extends BaseTest {

    @Test
    public void verifyInvalidLogin() {

        driver.get("https://www.linkedin.com/login");

        WebDriverWait wait = new WebDriverWait(
                driver, Duration.ofSeconds(20)
        );

        String email = System.getProperty("linkedin.email");

        Assert.assertNotNull(
                email, "LinkedIn email is not configured"
        );

        WebElement username = wait.until(d ->
                d.findElements(By.cssSelector("input[type='email']"))
                        .stream()
                        .filter(WebElement::isDisplayed)
                        .findFirst()
                        .orElse(null)
        );

        WebElement password = wait.until(d ->
                d.findElements(By.cssSelector("input[type='password']"))
                        .stream()
                        .filter(WebElement::isDisplayed)
                        .findFirst()
                        .orElse(null)
        );

        username.sendKeys(email);

        // Intentionally incorrect password
        password.sendKeys("InvalidPassword_For_Test_123!");

        WebElement signInButton = wait.until(d ->
                d.findElements(By.xpath(
                                "//button[.//span[normalize-space()='Sign in']]"
                        ))
                        .stream()
                        .filter(WebElement::isDisplayed)
                        .findFirst()
                        .orElse(null)
        );

        signInButton.click();

        System.out.println("Sign In button clicked!");

        String result = wait.until(d -> {

            String pageText = d.findElement(
                    By.tagName("body")
            ).getText().toLowerCase();

            // Detect security verification
            if (pageText.contains("quick security check") ||
                    pageText.contains("security verification") ||
                    pageText.contains("verify your identity")) {

                return "SECURITY_CHECK";
            }

            // Detect invalid login message
            for (WebElement element : d.findElements(
                    By.cssSelector(
                            "[role='alert'], #error-for-password, .form__label--error"
                    ))) {

                if (element.isDisplayed()) {

                    String message = element.getText().toLowerCase();

                    if (message.contains("incorrect") ||
                            message.contains("wrong") ||
                            message.contains("doesn't match") ||
                            message.contains("not correct")) {

                        return "INVALID_LOGIN: " + message;
                    }
                }
            }

            return null;
        });

        if (result.equals("SECURITY_CHECK")) {

            Assert.fail(
                    "Test blocked by LinkedIn security verification. " +
                            "Security verification was not bypassed."
            );
        }

        Assert.assertTrue(
                result.startsWith("INVALID_LOGIN"),
                "Expected invalid login error."
        );

        System.out.println("Invalid Login Test Passed!");
        System.out.println(result);
    }
}
