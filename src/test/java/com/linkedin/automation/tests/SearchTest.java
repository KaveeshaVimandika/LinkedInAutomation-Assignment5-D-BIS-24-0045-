package com.linkedin.automation.tests;

import com.linkedin.automation.base.BaseTest;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.StaleElementReferenceException;

import org.openqa.selenium.support.ui.WebDriverWait;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.time.Duration;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.IOException;
import java.util.List;

public class SearchTest extends BaseTest {

    private static final int WAIT_TIME = 40;

    // Find a visible and enabled element
    private WebElement findVisibleElement(
            WebDriver driver, By... locators) {

        WebDriverWait wait =
                new WebDriverWait(
                        driver,
                        Duration.ofSeconds(WAIT_TIME)
                );

        return wait.until(d -> {

            for (By locator : locators) {

                List<WebElement> elements =
                        d.findElements(locator);

                for (WebElement element : elements) {

                    try {

                        if (element.isDisplayed()
                                && element.isEnabled()) {

                            return element;
                        }

                    } catch (StaleElementReferenceException ignored) {
                        // Retry during next wait cycle
                    }
                }
            }

            return null;
        });
    }

    // Detect LinkedIn security verification
    private boolean isSecurityCheckpoint(WebDriver driver) {

        String url = driver.getCurrentUrl().toLowerCase();

        String pageText = driver.findElement(By.tagName("body"))
                .getText()
                .toLowerCase();

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

        WebDriverWait wait =
                new WebDriverWait(
                        driver,
                        Duration.ofSeconds(WAIT_TIME)
                );

        try {

            // STEP 1: Open LinkedIn Login Page

            driver.get("https://www.linkedin.com/login");

            System.out.println(
                    "LinkedIn Login Page Opened!"
            );

            // STEP 2: Get Login Credentials

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

            // STEP 3: Enter Email

            WebElement emailBox =
                    findVisibleElement(
                            driver,
                            By.cssSelector("input#username"),
                            By.cssSelector("input[type='email']")
                    );

            emailBox.clear();
            emailBox.sendKeys(email);

            System.out.println("Email entered!");

            // STEP 4: Enter Password

            WebElement passwordBox =
                    findVisibleElement(
                            driver,
                            By.cssSelector("input#password"),
                            By.cssSelector("input[type='password']")
                    );

            passwordBox.clear();
            passwordBox.sendKeys(password);

            System.out.println("Password entered!");

            // STEP 5: Click Sign In Button

            WebElement signInButton =
                    findVisibleElement(
                            driver,
                            By.xpath("//button[.//span[normalize-space()='Sign in']]"),
                            By.cssSelector("button[type='submit']")
                    );

            signInButton.click();

            System.out.println("Sign In button clicked!");

            // STEP 6: Wait for Login or Security Check

            boolean loginOrSecurity = wait.until(d -> {

                if (isSecurityCheckpoint(d)) {
                    return true;
                }

                boolean feedOpened =
                        d.getCurrentUrl().contains("/feed");

                boolean searchVisible =
                        !d.findElements(
                                        By.cssSelector(
                                                "input[placeholder*='Search'], " +
                                                        "input[aria-label*='Search']"
                                        )
                                ).stream()
                                .filter(WebElement::isDisplayed)
                                .toList()
                                .isEmpty();

                return feedOpened || searchVisible;
            });

            Assert.assertTrue(
                    loginOrSecurity,
                    "Login status could not be determined!"
            );

            Assert.assertFalse(
                    isSecurityCheckpoint(driver),
                    "LinkedIn Quick Security Check interrupted login. " +
                            "Verification was not bypassed."
            );

            Assert.assertTrue(
                    driver.getCurrentUrl().contains("/feed")
                            || !driver.findElements(
                            By.cssSelector(
                                    "input[placeholder*='Search'], " +
                                            "input[aria-label*='Search']"
                            )
                    ).isEmpty(),
                    "LinkedIn Feed was not reached!"
            );

            System.out.println("Login Successful!");

            // STEP 7: Find Search Box

            WebElement searchBox =
                    findVisibleElement(
                            driver,
                            By.cssSelector("input[placeholder*='Search']"),
                            By.cssSelector("input[aria-label*='Search']"),
                            By.cssSelector("input[role='combobox']")
                    );

            Assert.assertNotNull(
                    searchBox,
                    "LinkedIn Search Box not found!"
            );

            // STEP 8: Enter Search Term

            searchBox.click();

            searchBox.sendKeys(
                    Keys.chord(Keys.CONTROL, "a")
            );

            searchBox.sendKeys("Software Testing");

            System.out.println(
                    "Search term entered!"
            );

            // STEP 9: Press Enter

            searchBox.sendKeys(Keys.ENTER);

            System.out.println(
                    "Enter key pressed!"
            );

            // STEP 10: Verify Search Results URL

            wait.until(d ->
                    d.getCurrentUrl().contains("/search/")
            );

            Assert.assertTrue(
                    driver.getCurrentUrl().contains("/search/"),
                    "Search results page was not opened!"
            );

            System.out.println(
                    "Search Results Page Opened!"
            );

            // STEP 11: Verify At Least One Visible Result

            By resultLocator = By.cssSelector(
                    ".reusable-search__result-container, " +
                            "main a[href*='/in/'], " +
                            "main a[href*='/company/']"
            );

            WebElement searchResult =
                    wait.until(d -> {

                        List<WebElement> results =
                                d.findElements(resultLocator);

                        for (WebElement result : results) {

                            try {

                                if (result.isDisplayed()
                                        && !result.getText().isBlank()) {

                                    return result;
                                }

                            } catch (StaleElementReferenceException ignored) {
                                // Retry
                            }
                        }

                        return null;
                    });

            Assert.assertNotNull(
                    searchResult,
                    "No visible search result found!"
            );

            System.out.println(
                    "Visible Search Result Found!"
            );

            System.out.println(
                    "Search Test Completed Successfully!"
            );

            System.out.println(
                    "Final URL: " + driver.getCurrentUrl()
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
