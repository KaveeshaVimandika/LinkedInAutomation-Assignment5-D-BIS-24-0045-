
package com.linkedin.automation.tests;

import com.linkedin.automation.base.BaseTest;
import com.linkedin.automation.pages.LoginPage;

import org.testng.Assert;
import org.testng.annotations.Test;

public class NegativeLoginTest extends BaseTest {

    @Test
    public void verifyInvalidLogin() {

        // Get email from VM options
        String email = System.getProperty("linkedin.email");

        Assert.assertNotNull(
                email,
                "LinkedIn email is not configured"
        );

        // Create LoginPage object
        LoginPage loginPage = new LoginPage(driver);

        // Open LinkedIn login page
        loginPage.openLoginPage();

        // Intentionally incorrect password
        String invalidPassword =
                "InvalidPassword_For_Test_123!";

        // Perform invalid login
        loginPage.login(email, invalidPassword);

        System.out.println("Sign In button clicked!");
        System.out.println("Checking login response...");

        // Get login result
        String result = loginPage.getLoginResult();

        // Security verification handling
        if (result.equals("SECURITY_CHECK")) {

            Assert.fail(
                    "Test blocked by LinkedIn security verification. "
                            + "Security verification was not bypassed."
            );
        }

        // Verify incorrect password response
        Assert.assertTrue(
                result.startsWith("INVALID_LOGIN"),
                "Expected incorrect password error."
        );

        System.out.println("Invalid Login Test Passed!");
        System.out.println(result);
    }
}
