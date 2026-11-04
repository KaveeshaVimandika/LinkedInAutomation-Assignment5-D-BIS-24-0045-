package com.linkedin.automation.tests;

import com.linkedin.automation.base.BaseTest;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.time.Duration;

public class LoginTest extends BaseTest {

    @Test
    public void verifySuccessfulLogin() {

        // Open LinkedIn login page
        driver.get("https://www.linkedin.com/login");

        WebDriverWait wait = new WebDriverWait(
                driver, Duration.ofSeconds(30)
        );

        // Get login credentials from VM options
        String email = System.getProperty("linkedin.email");
        String passwordValue = System.getProperty("linkedin.password");

        Assert.assertNotNull(
                email, "LinkedIn email is not configured"
        );

        Assert.assertNotNull(
                passwordValue, "LinkedIn password is not configured"
        );

        // Find visible username field
        WebElement username = wait.until(d ->
                d.findElements(By.cssSelector("input[type='email']"))
                        .stream()
                        .filter(WebElement::isDisplayed)
                        .findFirst()
                        .orElse(null)
        );

        // Find visible password field
        WebElement password = wait.until(d ->
                d.findElements(By.cssSelector("input[type='password']"))
                        .stream()
                        .filter(WebElement::isDisplayed)
                        .findFirst()
                        .orElse(null)
        );

        // Enter login details
        username.clear();
        username.sendKeys(email);

        password.clear();
        password.sendKeys(passwordValue);

        // Find Sign In button
        WebElement signInButton = wait.until(d ->
                d.findElements(By.xpath(
                                "//button[.//span[normalize-space()='Sign in']]"
                        ))
                        .stream()
                        .filter(WebElement::isDisplayed)
                        .findFirst()
                        .orElse(null)
        );

        // Click Sign In
        signInButton.click();

        System.out.println("Sign In button clicked!");
        System.out.println("Waiting for LinkedIn verification...");

        // Allow time for manual security verification
        WebDriverWait loginWait = new WebDriverWait(
                driver, Duration.ofSeconds(300)
        );

        // Wait until LinkedIn Feed page opens
        boolean loginSuccessful = loginWait.until(d -> {

            String currentUrl = d.getCurrentUrl();

            System.out.println("Current URL: " + currentUrl);

            return currentUrl.contains("/feed");

        });

        // Verify successful login
        Assert.assertTrue(
                loginSuccessful,
                "Login was not successful!"
        );

        System.out.println("Login Successful!");
        System.out.println("LinkedIn Feed Page Opened!");
    }
}
