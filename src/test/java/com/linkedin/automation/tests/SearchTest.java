package com.linkedin.automation.tests;

import com.linkedin.automation.base.BaseTest;
import com.linkedin.automation.pages.LoginPage;
import com.linkedin.automation.pages.SearchPage;

import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.time.Duration;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.IOException;

public class SearchTest extends BaseTest {

    private static final int WAIT_TIME = 40;

    // Detect LinkedIn security verification
    private boolean isSecurityCheckpoint(WebDriver driver) {

        String url = driver.getCurrentUrl().toLowerCase();

        String pageText = driver.findElement(
                By.tagName("body")
        ).getText().toLowerCase();

        return url.contains("checkpoint")
                || url.contains("challenge")
                || pageText.contains("quick security check")
                || pageText.contains("security verification")
                || pageText.contains("verify your identity");
    }

    // Capture screenshot when test fails
    private void captureFailureScreenshot() {

        try {

            Files.createDirectories(Paths.get("screenshots"));

            byte[] screenshot =
                    ((TakesScreenshot) driver)
                            .getScreenshotAs(OutputType.BYTES);

            Files.write(
                    Paths.get(
                            "screenshots",
                            "SearchTest_Failure.png"
                    ),
                    screenshot
            );

            System.out.println(
                    "Failure screenshot saved!"
            );

        } catch (IOException e) {

            System.out.println(
                    "Unable to save screenshot: " + e.getMessage()
            );
        }
    }

    @Test
    public void verifySearchResults() {

        try {

            // STEP 1: Get login credentials
            String email =
                    System.getProperty("linkedin.email");

            String password =
                    System.getProperty("linkedin.password");

            Assert.assertNotNull(
                    email,
                    "LinkedIn email is missing!"
            );

            Assert.assertNotNull(
                    password,
                    "LinkedIn password is missing!"
            );

            Assert.assertFalse(
                    email.isBlank(),
                    "Email cannot be empty!"
            );

            Assert.assertFalse(
                    password.isBlank(),
                    "Password cannot be empty!"
            );

            // STEP 2: Create LoginPage object
            LoginPage loginPage = new LoginPage(driver);

            // Open LinkedIn login page
            loginPage.openLoginPage();

            System.out.println(
                    "LinkedIn Login Page Opened!"
            );

            // STEP 3: Login using LoginPage
            loginPage.login(email, password);

            System.out.println(
                    "Sign In button clicked!"
            );

            System.out.println(
                    "Waiting for LinkedIn verification..."
            );

            // STEP 4: Wait for Feed or security checkpoint
            WebDriverWait loginWait = new WebDriverWait(
                    driver,
                    Duration.ofSeconds(300)
            );

            loginWait.until(d -> {

                if (isSecurityCheckpoint(d)) {
                    return true;
                }

                return d.getCurrentUrl().contains("/feed");
            });

            // Do not bypass security verification
            Assert.assertFalse(
                    isSecurityCheckpoint(driver),
                    "LinkedIn security verification interrupted login. "
                            + "Verification was not bypassed."
            );

            Assert.assertTrue(
                    driver.getCurrentUrl().contains("/feed"),
                    "LinkedIn Feed was not reached!"
            );

            System.out.println(
                    "Login Successful!"
            );

            // STEP 5: Create SearchPage object
            SearchPage searchPage = new SearchPage(driver);

            // STEP 6: Search keyword
            String searchKeyword = "Software Testing";

            searchPage.search(searchKeyword);

            System.out.println(
                    "Search term entered: " + searchKeyword
            );

            System.out.println(
                    "Search submitted!"
            );

            // STEP 7: Verify search results URL
            Assert.assertTrue(
                    searchPage.isSearchResultsPageOpened(),
                    "Search results page was not opened!"
            );

            System.out.println(
                    "Search Results Page Opened!"
            );

            // STEP 8: Verify visible search result
            Assert.assertTrue(
                    searchPage.hasVisibleSearchResult(),
                    "No visible search result found!"
            );

            System.out.println(
                    "Visible Search Result Found!"
            );

            System.out.println(
                    "Search Test Completed Successfully!"
            );

            System.out.println(
                    "Final URL: " + searchPage.getCurrentUrl()
            );

        } catch (Exception e) {

            System.out.println(
                    "Search Test Failed: " + e.getMessage()
            );

            System.out.println(
                    "Current URL: " + driver.getCurrentUrl()
            );

            captureFailureScreenshot();

            throw new AssertionError(
                    "Search Test Failed!",
                    e
            );
        }
    }
}
