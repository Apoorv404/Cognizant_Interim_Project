package org.example.base;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.edge.EdgeDriver;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class BaseTest {

    protected WebDriver driver;
    protected static final Path SCREENSHOT_DIR = Paths.get("src", "screenshots");
    protected String browserName;

    // Called manually from AmazonSearchTest before the test starts
    public void setUp(String browser) throws Exception {
        Files.createDirectories(SCREENSHOT_DIR);
        driver = createDriver(browser);
        driver.manage().window().maximize();
        browserName = browser;
    }

    // Called manually from AmazonSearchTest after the test ends
    public void tearDown() {
        if (driver != null) {
            driver.quit();
            driver = null;
        }
    }

    private WebDriver createDriver(String browser) {
        if ("edge".equalsIgnoreCase(browser)) {
            return new EdgeDriver();
        }
        return new ChromeDriver();
    }

    // Take screenshot and return absolute path
    protected String captureScreenshot(String stepName) {
        try {
            String timestamp = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS").format(LocalDateTime.now());
            Path screenshotPath = SCREENSHOT_DIR.resolve(timestamp + "_" + browserName + "_" + stepName + ".png");

            Path source = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE).toPath();
            Files.copy(source, screenshotPath, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("[" + browserName + "] Screenshot saved: " + screenshotPath.getFileName());
            return screenshotPath.toAbsolutePath().toString();
        } catch (Exception e) {
            System.out.println("[" + browserName + "] Screenshot failed: " + e.getMessage());
            return "";
        }
    }
}
