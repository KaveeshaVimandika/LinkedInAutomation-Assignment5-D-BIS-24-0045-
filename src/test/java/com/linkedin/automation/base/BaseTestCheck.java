package com.linkedin.automation.base;

import org.testng.annotations.Test;
import org.testng.Assert;

public class BaseTestCheck extends BaseTest {

    @Test
    public void verifyBrowserLaunch() throws InterruptedException {

        driver.get("https://www.selenium.dev");

        String pageTitle = driver.getTitle();

        System.out.println("Page Title: " + pageTitle);

        Assert.assertTrue(
                pageTitle.toLowerCase().contains("selenium")
        );

        Thread.sleep(5000);
    }
}

