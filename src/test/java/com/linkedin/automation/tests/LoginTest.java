
package com.linkedin.automation.tests;

import com.linkedin.automation.base.BaseTest;
import com.linkedin.automation.pages.LoginPage;

import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.time.Duration;

public class LoginTest extends BaseTest {

    @Test
    public void verifySuccessfulLogin() {

        // Get credentials from VM options
        String email = System.getProperty("linkedin.email");
        String passwordValue =
                System.getProperty("linkedin.password");

        Assert.assertNotNull(
                email, "LinkedIn email is not configured"
        );

        Assert.assertNotNull(
                passwordValue, "LinkedIn password is not configured"
        );

        // Create LoginPage object
        LoginPage loginPage = new LoginPage(driver);

        // Open LinkedIn login page
        loginPage.openLoginPage();

        // Enter credentials and click Sign In
        loginPage.login(email, passwordValue);

        System.out.println("Sign In button clicked!");
        System.out.println("Waiting for LinkedIn verification...");

        // Wait for manual security verification
        boolean loginSuccessful = new WebDriverWait(
                driver, Duration.ofSeconds(300)
        ).until(d -> {

            String currentUrl = d.getCurrentUrl();

            System.out.println("Current URL: " + currentUrl);

            return currentUrl.contains("/feed");
        });

        // Verify login
        Assert.assertTrue(
                loginSuccessful,
                "Login was not successful!"
        );

        System.out.println("Login Successful!");
        System.out.println("LinkedIn Feed Page Opened!");
    }
}
