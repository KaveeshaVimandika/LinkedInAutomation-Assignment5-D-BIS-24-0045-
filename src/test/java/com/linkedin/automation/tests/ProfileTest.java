package com.linkedin.automation.tests;

import com.linkedin.automation.base.BaseTest;
import com.linkedin.automation.pages.LoginPage;
import com.linkedin.automation.pages.SearchPage;
import com.linkedin.automation.pages.ProfilePage;

import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.time.Duration;

public class ProfileTest extends BaseTest {

    @Test
    public void verifyFirstSearchResultProfile() {

        // Step 1: Get LinkedIn credentials
        String email = System.getProperty("linkedin.email");
        String password = System.getProperty("linkedin.password");

        Assert.assertNotNull(email, "LinkedIn email is not configured");
        Assert.assertNotNull(password, "LinkedIn password is not configured");

        // Step 2: Login to LinkedIn
        LoginPage loginPage = new LoginPage(driver);

        loginPage.openLoginPage();
        loginPage.login(email, password);

        System.out.println("Login button clicked!");
        System.out.println("Waiting for LinkedIn Feed...");

        // Wait until Feed page opens
        new WebDriverWait(driver, Duration.ofSeconds(300))
                .until(d -> d.getCurrentUrl().contains("/feed"));

        Assert.assertTrue(
                driver.getCurrentUrl().contains("/feed"),
                "LinkedIn Feed page did not open!"
        );

        System.out.println("Login Successful!");

        // Step 3: Perform a search
        SearchPage searchPage = new SearchPage(driver);

        String keyword = "Software Engineer";

        searchPage.search(keyword);

        Assert.assertTrue(
                searchPage.isSearchResultsPageOpened(),
                "Search results page did not open!"
        );

        Assert.assertTrue(
                searchPage.hasVisibleSearchResult(),
                "No search results were found!"
        );

        System.out.println("Search Keyword: " + keyword);
        System.out.println("Search Results Displayed Successfully!");

        // Step 4: Click the first available user profile
        ProfilePage profilePage = new ProfilePage(driver);

        profilePage.openFirstProfile();

        System.out.println("First User Profile Clicked!");

        // Step 5: Verify the correct profile page opened
        Assert.assertTrue(
                profilePage.isProfilePageOpened(),
                "The selected LinkedIn profile page did not open!"
        );

        String profileUrl = profilePage.getProfileUrl();

        Assert.assertTrue(
                profileUrl.contains("/in/"),
                "Incorrect page opened. Expected a LinkedIn user profile URL."
        );

        System.out.println("Profile URL: " + profileUrl);

        // Step 6: Get profile name
        String profileName = profilePage.getProfileName();

        System.out.println("Profile Name: " + profileName);
        System.out.println("Profile Page Verified Successfully!");
    }
}
