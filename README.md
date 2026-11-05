LinkedIn Automation Testing Project

A Java-based web UI automation project for testing selected LinkedIn
login, search, and profile-navigation functionalities using Selenium
WebDriver, TestNG, and Maven.

Project Overview

This project automates the following functional test scenarios:

  
   Test Case                           Description
  
  1. LoginTest                          Verifies that a user can log in
                                      with valid LinkedIn credentials and
                                      reach the Feed page.

  2. NegativeLoginTest                  Verifies that an invalid password
                                      is rejected and an appropriate
                                      login error is detected.

  3. SearchTest                         Searches for a keyword (for
                                      example, 'Software Testing') and
                                      verifies that search results are
                                      displayed.

  4. ProfileTest                        Logs in, searches for a keyword,
                                      opens the first available profile,
                                      and verifies the profile page URL.
  

The tests are configured in 'testng.xml' and run sequentially.

Technologies Used

-   Java JDK 21
-   Maven
-   Selenium WebDriver 4.51.0
-   TestNG 7.10.2
-   Google Chrome
-   IntelliJ IDEA

Project Structure

 text
LinkedInAutomation/
├── src/
│   └── test/
│       └── java/
│           └── com/linkedin/automation/
│               ├── base/
│               │   ├── BaseTest.java
│               │   └── BaseTestCheck.java
│               ├── listeners/
│               ├── pages/
│               │   ├── LoginPage.java
│               │   ├── SearchPage.java
│               │   └── ProfilePage.java
│               ├── tests/
│               │   ├── LoginTest.java
│               │   ├── NegativeLoginTest.java
│               │   ├── SearchTest.java
│               │   └── ProfileTest.java
│               └── utils/
├── screenshots/
├── page-dump/
├── pom.xml
└── testng.xml
```

> The exact contents of folders such as `listeners` and `utils` may vary
> depending on the current project implementation.

Design Approach

The project uses the Page Object Model (POM) design pattern:

BaseTest: Sets up and closes the WebDriver for each test method.
LoginPage: Contains LinkedIn login-page locators and login
    actions.
SearchPage: Contains search-page locators and search actions.
ProfilePage: Contains actions and checks for opening a profile.
Test classes:Contain test scenarios and assertions.

This separation makes the test code easier to maintain and reuse.

Prerequisites

Before running the tests;

1.  Java JDK 21 installed.
2.  Maven installed, or Maven support enabled in IntelliJ IDEA.
3.  Google Chrome installed.
4.  The project dependencies downloaded through Maven.
5.  A LinkedIn account that you are authorised to use for testing.

Selenium Manager is used by Selenium to manage the browser driver in
supported environments. Keep Chrome and Selenium versions reasonably up
to date.

Configuration: LinkedIn Credentials

The tests read credentials from Java system properties. Do not hardcode
your LinkedIn email address or password in the source code, and do not
commit credentials to GitHub.

In IntelliJ IDEA:

1.  Open Run → Edit Configurations.
2.  Select the TestNG suite configuration for `testng.xml`.
3.  Find VM options.
4.  Add the following, replacing the placeholders with your own
    credentials:

``` text
-ea -Dlinkedin.email=YOUR_EMAIL -Dlinkedin.password=YOUR_PASSWORD
```

5.  Click Apply, then OK.

For a negative-login test run by itself, the email property is required;
the test uses an intentionally invalid password defined in its test
code.

If the project is run from a terminal, pass the same Java system
properties to the JVM/test runner configuration. Do not paste real
credentials into shared terminal logs, screenshots, or documentation.

 Running the Tests

 Run the complete TestNG suite in IntelliJ IDEA

1.  Open 'testng.xml' in the project root.
2.  Right-click inside the file.
3.  Select Run for the TestNG suite.
4.  Review the TestNG results in the Run window.

The suite uses:

 xml
<suite name="LinkedIn Automation Suite" parallel="false">


'parallel="false"' runs the configured test classes sequentially rather
than in parallel.

 Run an individual test

Open the required test class under:

'src/test/java/com/linkedin/automation/tests'

Right-click inside the class and select Run. Make sure the run
configuration contains the required VM options for that test.

Expected Test Results

When all test scenarios execute successfully, the TestNG summary should
show:

 text
Total tests run: 4
Passes: 4
Failures: 0
Skips: 0


This is an expected example, not a guarantee. Actual results depend on
LinkedIn availability, account state, browser compatibility, and
security checks.

Screenshots and Debugging

The project contains a `screenshots/` directory for test evidence. The
`page-dump/` directory may contain saved HTML page dumps from debugging.

Check the test output and the relevant screenshot/page dump when a test
fails. Do not assume automatic failure screenshots are enabled unless
the screenshot listener or capture code is configured and registered in
the current project.

Important Notes and Limitations

-   LinkedIn may display CAPTCHA, identity verification, or other
    security checkpoints. Complete verification manually only when
    LinkedIn permits it. Do not attempt to bypass CAPTCHA or security
    controls.
-   If a security checkpoint cannot be completed, stop the test and
    document the scenario as blocked by the platform's security
    verification.
-   LinkedIn frequently changes its page structure and element
    attributes. Locators may need maintenance when the website changes.
-   Search results and the first available profile can vary depending on
    the account, query, and LinkedIn's ranking.
-   Automated tests depend on network connectivity and the availability
    of LinkedIn.
-   Use only your own account or an account for which you have explicit
    permission to perform testing.


