
package com.linkedin.automation.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.ElementClickInterceptedException;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.time.Duration;
import java.util.List;

public class SearchPage {

    private WebDriver driver;
    private WebDriverWait wait;

    // LinkedIn Search Box Locator
    private final By searchBoxLocator =
            By.cssSelector(
                    "input[placeholder*='Search'], " +
                            "input[aria-label*='Search'], " +
                            "input[role='combobox']"
            );

    // LinkedIn Dialog / Popup Locator
    private final By dialogLocator =
            By.cssSelector("dialog[data-testid='dialog']");

    // Search Results Locator
    private final By resultLocator =
            By.cssSelector(
                    ".reusable-search__result-container, " +
                            "main a[href*='/in/'], " +
                            "main a[href*='/company/']"
            );

    // Constructor
    public SearchPage(WebDriver driver) {

        this.driver = driver;

        this.wait = new WebDriverWait(
                driver,
                Duration.ofSeconds(40)
        );
    }

    // Close Blocking Dialog / Popup
    private void closeBlockingDialog() {

        try {

            List<WebElement> dialogs =
                    driver.findElements(dialogLocator);

            for (WebElement dialog : dialogs) {

                if (dialog.isDisplayed()) {

                    driver.switchTo()
                            .activeElement()
                            .sendKeys(Keys.ESCAPE);

                    wait.until(
                            ExpectedConditions.invisibilityOfElementLocated(
                                    dialogLocator
                            )
                    );

                    System.out.println(
                            "Blocking dialog closed!"
                    );

                    break;
                }
            }

        } catch (Exception e) {

            System.out.println(
                    "No blocking dialog found or dialog could not be closed."
            );
        }
    }

    // Get Visible Search Box Safely
    private WebElement getSearchBox() {

        WebDriverWait searchWait = new WebDriverWait(
                driver,
                Duration.ofSeconds(40)
        );

        searchWait.ignoring(
                StaleElementReferenceException.class
        );

        return searchWait.until(d -> {

            List<WebElement> boxes =
                    d.findElements(searchBoxLocator);

            for (WebElement box : boxes) {

                try {

                    if (box.isDisplayed() && box.isEnabled()) {
                        return box;
                    }

                } catch (StaleElementReferenceException e) {
                    // Retry with a fresh element
                }
            }

            return null;
        });
    }

    // Enter Search Keyword
    public void enterSearchKeyword(String keyword) {

        for (int attempt = 1; attempt <= 3; attempt++) {

            try {

                // Close blocking popup if available
                closeBlockingDialog();

                // Wait until search field is visible
                wait.until(
                        ExpectedConditions.visibilityOfElementLocated(
                                searchBoxLocator
                        )
                );

                // Locate a fresh search field
                WebElement searchBox = getSearchBox();

                // Wait until search field is clickable
                wait.until(
                        ExpectedConditions.elementToBeClickable(
                                searchBoxLocator
                        )
                );

                // Locate again before clicking
                searchBox = getSearchBox();

                searchBox.click();

                // Locate again before typing
                searchBox = getSearchBox();

                searchBox.sendKeys(Keys.CONTROL, "a");
                searchBox.sendKeys(keyword);

                System.out.println(
                        "Search term entered: " + keyword
                );

                return;

            } catch (StaleElementReferenceException |
                     ElementClickInterceptedException e) {

                System.out.println(
                        "Retrying search field interaction. Attempt: "
                                + attempt
                );

                closeBlockingDialog();
            }
        }

        throw new RuntimeException(
                "Unable to interact with LinkedIn search field after 3 attempts."
        );
    }

    // Submit Search
    public void submitSearch() {

        WebElement searchBox = getSearchBox();

        searchBox.sendKeys(Keys.ENTER);

        System.out.println("Search submitted!");
    }

    // Perform Complete Search
    public void search(String keyword) {

        enterSearchKeyword(keyword);

        submitSearch();

        wait.until(d ->
                d.getCurrentUrl().contains("/search/")
        );

        System.out.println(
                "Search Results Page Opened!"
        );
    }

    // Verify Search Results Page
    public boolean isSearchResultsPageOpened() {

        try {

            return wait.until(d ->
                    d.getCurrentUrl().contains("/search/")
            );

        } catch (Exception e) {

            return false;
        }
    }

    // Get First Visible Search Result
    public WebElement getFirstVisibleSearchResult() {

        WebDriverWait resultWait = new WebDriverWait(
                driver,
                Duration.ofSeconds(30)
        );

        resultWait.ignoring(
                StaleElementReferenceException.class
        );

        return resultWait.until(d -> {

            List<WebElement> results =
                    d.findElements(resultLocator);

            for (WebElement result : results) {

                try {

                    if (result.isDisplayed()) {
                        return result;
                    }

                } catch (StaleElementReferenceException e) {
                    // Retry with fresh result element
                }
            }

            return null;
        });
    }

    // Check Whether Search Result Exists
    public boolean hasVisibleSearchResult() {

        try {

            return getFirstVisibleSearchResult() != null;

        } catch (Exception e) {

            return false;
        }
    }

    // Get Current URL
    public String getCurrentUrl() {

        return driver.getCurrentUrl();
    }
}
