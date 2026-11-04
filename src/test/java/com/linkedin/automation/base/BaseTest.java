package com.linkedin.automation.base;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.AfterMethod;

public class BaseTest {

    protected WebDriver driver;

    // Open browser before each test
    @BeforeMethod
    public void setUp() {

        driver = new ChromeDriver();

        driver.manage().window().maximize();

        System.out.println("Chrome Browser Opened Successfully!");
    }

    // Close browser after each test
    @AfterMethod
    public void tearDown() {

        if (driver != null) {

            driver.quit();

            System.out.println("Chrome Browser Closed Successfully!");
        }
    }
}
