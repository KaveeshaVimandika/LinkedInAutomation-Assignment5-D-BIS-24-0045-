package com.linkedin.automation.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class ProfilePage {

    private WebDriver driver;
    private WebDriverWait wait;

    private final By profileLinks =
            By.cssSelector("main a[href*='/in/']");

    private final By profileHeading =
            By.cssSelector("main h1, main h2");

    public ProfilePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(30));
    }

    // Click the first available profile from search results
    public void openFirstProfile() {

        WebElement firstProfile = wait.until(d ->
                d.findElements(profileLinks).stream()
                        .filter(WebElement::isDisplayed)
                        .filter(WebElement::isEnabled)
                        .filter(element -> {
                            String href = element.getAttribute("href");

                            return href != null
                                    && href.contains("linkedin.com/in/")
                                    && !href.contains("/in/me")
                                    && !href.contains("/in/edit");
                        })
                        .findFirst()
                        .orElse(null)
        );

        firstProfile.click();

        wait.until(ExpectedConditions.urlContains("/in/"));
    }

    // Get current profile URL
    public String getProfileUrl() {
        return driver.getCurrentUrl();
    }

    // Get profile name
    public String getProfileName() {

        try {
            WebElement heading = wait.until(
                    ExpectedConditions.visibilityOfElementLocated(profileHeading)
            );

            return heading.getText();

        } catch (Exception e) {
            return "Profile heading not found";
        }
    }

    // Verify profile page
    public boolean isProfilePageOpened() {
        return driver.getCurrentUrl().contains("/in/");
    }
}
