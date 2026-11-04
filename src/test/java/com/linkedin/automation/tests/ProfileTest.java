
package com.linkedin.automation.tests;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class ProfileTest {

    private WebDriver driver;

    private static final String EMAIL = System.getProperty("linkedin.email", "");
    private static final String PASSWORD = System.getProperty("linkedin.password", "");
    private static final String SEARCH_KEYWORD = System.getProperty("linkedin.search", "Software Engineer");

    // Dedicated Chrome profile
    private static final String CHROME_PROFILE_DIR =
            System.getProperty("user.home") + File.separator + "selenium-linkedin-profile";

    // Collects every input/button in the document AND inside open shadow roots
    private static final String COLLECT_JS =
            "function collect(root, out){" +
                    "  root.querySelectorAll('input, button').forEach(function(e){ out.push(e); });" +
                    "  root.querySelectorAll('*').forEach(function(n){ if (n.shadowRoot) collect(n.shadowRoot, out); });" +
                    "  return out;" +
                    "}" +
                    "return collect(document, []);";

    private static final String SET_VALUE_JS =
            "const el = arguments[0], val = arguments[1];" +
                    "el.focus();" +
                    "const setter = Object.getOwnPropertyDescriptor(window.HTMLInputElement.prototype, 'value').set;" +
                    "setter.call(el, val);" +
                    "el.dispatchEvent(new Event('input', {bubbles:true}));" +
                    "el.dispatchEvent(new Event('change', {bubbles:true}));";

    // Returns all profile hrefs found on the page
    private static final String PROFILE_LINKS_JS =
            "return Array.from(document.querySelectorAll('a[href*=\"/in/\"]'))" +
                    ".map(function(a){ return a.href; })" +
                    ".filter(function(h){ return h.indexOf('linkedin.com/in/') > -1; });";

    @BeforeMethod
    public void setUp() {

        ChromeOptions options = new ChromeOptions();

        options.addArguments("--user-data-dir=" + CHROME_PROFILE_DIR);
        options.addArguments("--start-maximized");
        options.addArguments("--remote-allowed-origins=*");
        options.addArguments("--disable-notifications");
        options.addArguments("--lang=en-US");
        options.addArguments("--no-first-run");
        options.addArguments("--no-default-browser-check");

        options.setExperimentalOption(
                "excludeSwitches",
                List.of("enable-automation")
        );

        options.setExperimentalOption("useAutomationExtension", false);

        driver = new ChromeDriver(options);

        driver.manage().timeouts().implicitlyWait(Duration.ZERO);
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(60));

        System.out.println("Chrome Browser Opened Successfully!");
    }

    @Test
    public void loginSearchAndOpenProfileTest() {

        try {

            Assert.assertFalse(
                    EMAIL.isEmpty() || PASSWORD.isEmpty(),
                    "Set -Dlinkedin.email and -Dlinkedin.password in VM options"
            );

            driver.get("https://www.linkedin.com/login");

            waitForPageLoad();

            System.out.println(
                    "LinkedIn login page opened! URL: " + safeUrl()
            );

            autoLogin();

            searchAndOpenFirstProfile();

            System.out.println(
                    "PROFILE TEST PASSED! Opened: " + safeUrl()
            );

        } catch (Throwable e) {

            System.out.println("--------------------------------");
            System.out.println("PROFILE TEST FAILED!");
            System.out.println("Reason: " + e.getMessage());
            System.out.println("Current URL: " + safeUrl());
            System.out.println("--------------------------------");

            takeScreenshot("failure");
            dumpPage("failure");

            Assert.fail(
                    "Profile test failed: " + e.getMessage(),
                    e
            );
        }
    }

    // ================= AUTO LOGIN =================

    // UPDATED METHOD ONLY
    private void autoLogin() {

        System.out.println("Logging out from previous LinkedIn session...");

        // Log out from the previously saved LinkedIn session
        driver.get("https://www.linkedin.com/m/logout/");

        waitForPageLoad();

        // Clear saved browser cookies to prevent automatic session reuse
        driver.manage().deleteAllCookies();

        System.out.println("Previous session cleared!");

        // Open LinkedIn login page again
        driver.get("https://www.linkedin.com/login");

        waitForPageLoad();

        System.out.println("LinkedIn login page opened for fresh login!");

        long end = System.currentTimeMillis() + 60_000;
        boolean submitted = false;

        while (System.currentTimeMillis() < end) {

            String url = safeUrl();

            if (isCheckpointUrl(url)) {

                System.out.println(
                        "Checkpoint/captcha detected. Complete it manually..."
                );

                new WebDriverWait(driver, Duration.ofMinutes(3))
                        .until(d -> isLoggedInUrl(d.getCurrentUrl()));

                return;
            }

            if (!submitted && tryFillAndSubmit()) {

                submitted = true;

                System.out.println(
                        "Credentials submitted, waiting for LinkedIn..."
                );
            }

            if (submitted && isLoggedInUrl(url)) {

                System.out.println(
                        "LinkedIn login successful!"
                );

                return;
            }

            sleep(800);
        }

        throw new TimeoutException(
                submitted
                        ? "Credentials submitted but LinkedIn did not reach the feed. URL: " + safeUrl()
                        : "Login form not found / could not be filled. URL: " + safeUrl()
        );
    }

    /** Tries main document first, then every iframe. */
    private boolean tryFillAndSubmit() {

        try {

            driver.switchTo().defaultContent();

            if (fillInCurrentContext()) {
                return true;
            }

            int frameCount =
                    driver.findElements(By.tagName("iframe")).size();

            for (int i = 0; i < frameCount; i++) {

                try {

                    driver.switchTo().defaultContent();
                    driver.switchTo().frame(i);

                    if (fillInCurrentContext()) {

                        driver.switchTo().defaultContent();

                        return true;
                    }

                } catch (Exception ignored) {
                }
            }

        } catch (Exception e) {

            System.out.println(
                    "tryFillAndSubmit error: " + e.getMessage()
            );

        } finally {

            try {
                driver.switchTo().defaultContent();
            } catch (Exception ignored) {
            }
        }

        return false;
    }

    private boolean fillInCurrentContext() {

        List<WebElement> all = collectElements();

        WebElement passField = null;
        WebElement userField = null;

        for (WebElement el : all) {

            if (!"input".equalsIgnoreCase(el.getTagName())) {
                continue;
            }

            String type = attr(el, "type").toLowerCase();

            if (type.equals("password")) {

                if (passField == null && isShown(el)) {
                    passField = el;
                }

            } else if (
                    type.isEmpty()
                            || type.equals("text")
                            || type.equals("email")
                            || type.equals("tel")
            ) {

                if (userField == null && isShown(el)) {
                    userField = el;
                }
            }
        }

        if (passField == null) {
            return false;
        }

        if (userField != null) {
            setValue(userField, EMAIL);
        }

        sleep(300);

        setValue(passField, PASSWORD);

        sleep(300);

        try {

            passField.sendKeys(Keys.ENTER);

        } catch (Exception e) {

            clickSignInButton(all);
        }

        return true;
    }

    private void clickSignInButton(List<WebElement> all) {

        for (WebElement el : all) {

            try {

                if ("button".equalsIgnoreCase(el.getTagName())
                        && el.getText().trim().equalsIgnoreCase("Sign in")
                        && isShown(el)) {

                    jsClick(el);

                    return;
                }

            } catch (Exception ignored) {
            }
        }
    }

    // ================= SEARCH + PROFILE =================

    private void searchAndOpenFirstProfile() {

        String encoded = SEARCH_KEYWORD.replace(" ", "%20");

        driver.get(
                "https://www.linkedin.com/search/results/people/?keywords=" + encoded
        );

        waitForPageLoad();

        // Wait until at least one profile link exists
        String profileUrl = new WebDriverWait(
                driver,
                Duration.ofSeconds(30)
        ).until(d -> pickProfileUrl());

        System.out.println("Opening profile: " + profileUrl);

        // Open profile
        driver.get(profileUrl);

        waitForPageLoad();

        // Verify profile page
        new WebDriverWait(driver, Duration.ofSeconds(30))
                .until(ExpectedConditions.urlContains("/in/"));

        new WebDriverWait(driver, Duration.ofSeconds(30))
                .until(d -> {

                    String t = d.getTitle();

                    return t != null && !t.trim().isEmpty();
                });

        Assert.assertTrue(
                safeUrl().contains("/in/"),
                "Profile URL not opened: " + safeUrl()
        );

        // Read profile name
        String name = readProfileName();

        System.out.println("Profile name: " + name);
    }

    /** Returns first valid public-profile URL from the search page. */
    @SuppressWarnings("unchecked")
    private String pickProfileUrl() {

        try {

            Object res = ((JavascriptExecutor) driver)
                    .executeScript(PROFILE_LINKS_JS);

            if (res instanceof List) {

                for (Object o : (List<Object>) res) {

                    String href = String.valueOf(o);

                    String lower = href.toLowerCase();

                    if (lower.contains("/in/me")
                            || lower.contains("/in/edit")) {
                        continue;
                    }

                    int idx = href.indexOf("?");

                    return idx > -1
                            ? href.substring(0, idx)
                            : href;
                }
            }

        } catch (Exception ignored) {
        }

        return null;
    }

    /** Tries h1, h2, then page title. */
    private String readProfileName() {

        String[] selectors = {
                "h1",
                "main h2",
                "h2",
                "main [role='heading']"
        };

        for (String css : selectors) {

            try {

                List<WebElement> els =
                        driver.findElements(By.cssSelector(css));

                for (WebElement el : els) {

                    String text = el.getText();

                    if (text != null && !text.trim().isEmpty()) {
                        return text.trim();
                    }
                }

            } catch (Exception ignored) {
            }
        }

        // Fallback to page title
        try {

            String title = driver.getTitle();

            if (title != null && !title.trim().isEmpty()) {

                int bar = title.indexOf("|");

                return (bar > 0
                        ? title.substring(0, bar)
                        : title).trim();
            }

        } catch (Exception ignored) {
        }

        System.out.println(
                "WARNING: Could not read profile name (test still passes)."
        );

        return "N/A";
    }

    // ================= HELPERS =================

    @SuppressWarnings("unchecked")
    private List<WebElement> collectElements() {

        try {

            Object res = ((JavascriptExecutor) driver)
                    .executeScript(COLLECT_JS);

            if (res instanceof List) {

                List<WebElement> out = new ArrayList<>();

                for (Object o : (List<Object>) res) {

                    if (o instanceof WebElement) {
                        out.add((WebElement) o);
                    }
                }

                return out;
            }

        } catch (Exception e) {

            System.out.println(
                    "collectElements error: " + e.getMessage()
            );
        }

        return new ArrayList<>();
    }

    private String attr(WebElement el, String name) {

        try {

            String v = el.getAttribute(name);

            return v == null ? "" : v;

        } catch (Exception e) {

            return "";
        }
    }

    private boolean isShown(WebElement el) {

        try {

            return el.isDisplayed();

        } catch (Exception e) {

            return false;
        }
    }

    private void setValue(WebElement el, String value) {

        try {

            el.click();
            el.clear();
            el.sendKeys(value);

            if (value.equals(el.getAttribute("value"))) {
                return;
            }

        } catch (Exception ignored) {
        }

        ((JavascriptExecutor) driver)
                .executeScript(SET_VALUE_JS, el, value);
    }

    private void jsClick(WebElement el) {

        try {

            el.click();

        } catch (Exception e) {

            ((JavascriptExecutor) driver)
                    .executeScript("arguments[0].click();", el);
        }
    }

    private void waitForPageLoad() {

        try {

            new WebDriverWait(driver, Duration.ofSeconds(30))
                    .until(d ->
                            "complete".equals(
                                    ((JavascriptExecutor) d)
                                            .executeScript("return document.readyState")
                            )
                    );

        } catch (Exception ignored) {
        }

        sleep(1500);
    }

    private boolean isLoggedInUrl(String url) {

        return url.contains("linkedin.com/feed")
                || url.contains("linkedin.com/mynetwork")
                || url.contains("linkedin.com/in/")
                || url.contains("linkedin.com/search");
    }

    private boolean isCheckpointUrl(String url) {

        return url.contains("checkpoint")
                || url.contains("challenge")
                || url.contains("captcha");
    }

    private String safeUrl() {

        try {

            String u = driver.getCurrentUrl();

            return u == null ? "" : u;

        } catch (Exception e) {

            return "";
        }
    }

    private void sleep(long ms) {

        try {

            Thread.sleep(ms);

        } catch (InterruptedException ie) {

            Thread.currentThread().interrupt();
        }
    }

    private void takeScreenshot(String name) {

        try {

            if (driver == null) {
                return;
            }

            File src = ((TakesScreenshot) driver)
                    .getScreenshotAs(OutputType.FILE);

            Path dir = Paths.get("screenshots");

            Files.createDirectories(dir);

            Path dest = dir.resolve(
                    name + "_" + System.currentTimeMillis() + ".png"
            );

            Files.copy(
                    src.toPath(),
                    dest,
                    StandardCopyOption.REPLACE_EXISTING
            );

            System.out.println(
                    "Screenshot saved -> " + dest.toAbsolutePath()
            );

        } catch (Exception e) {

            System.out.println(
                    "Screenshot error: " + e.getMessage()
            );
        }
    }

    private void dumpPage(String name) {

        try {

            if (driver == null) {
                return;
            }

            Path dir = Paths.get("page-dump");

            Files.createDirectories(dir);

            Path dest = dir.resolve(
                    name + "_" + System.currentTimeMillis() + ".html"
            );

            Files.writeString(
                    dest,
                    driver.getPageSource(),
                    StandardCharsets.UTF_8
            );

            System.out.println(
                    "Page HTML saved -> " + dest.toAbsolutePath()
            );

        } catch (Exception e) {

            System.out.println(
                    "Dump error: " + e.getMessage()
            );
        }
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown() {

        if (driver != null) {

            driver.quit();

            System.out.println(
                    "Chrome Browser Closed Successfully!"
            );
        }
    }
}
