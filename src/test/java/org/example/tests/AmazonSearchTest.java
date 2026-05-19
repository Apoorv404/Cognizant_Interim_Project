package org.example.tests;

import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.MediaEntityBuilder;
import org.example.base.BaseTest;
import org.example.ExtentReport.ExtentReportManager;
import org.example.pages.AmazonHomePage;
import org.example.pages.AmazonResultsPage;
import org.example.utils.TestData;
import org.example.utils.ExcelUtil;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import java.util.regex.Pattern;

public class AmazonSearchTest extends BaseTest {

    private ExtentTest extentTest;
    private static final Pattern RESULT_PATTERN = Pattern.compile("\\d+-\\d+ of (over )?[\\d,]+ results for", Pattern.CASE_INSENSITIVE);

    @BeforeClass(alwaysRun = true)
    public synchronized void beforeClass() throws Exception {
        ExtentReportManager.initializeReport();
        ExcelUtil.createFileIfMissing();
    }

    @Test
    @Parameters("browser")
    public void verifyAmazonSearchAndSort(@Optional("chrome") String browser) throws Exception {
        TestData data = ExcelUtil.readData(browser);

        extentTest = ExtentReportManager.createTest("Amazon Search & Sort - " + browser.toUpperCase());

        String finalStatus = "FAIL";
        String actualResultText = "No result captured";

        try {
            super.setUp(browser);
            logStep("Browser launched: " + browser);

            AmazonHomePage homePage = new AmazonHomePage(driver);
            homePage.navigateToAmazon(data.url);
            homePage.waitForHomePageLoad();
            logStepWithScreenshot("Website loaded fully", "01_website_loaded");

            homePage.searchProduct(data.searchText);
            logStepWithScreenshot("Searched for: " + data.searchText, "02_query_searched");

            AmazonResultsPage resultsPage = new AmazonResultsPage(driver);
            resultsPage.waitForResultsPageLoad();
            
            String resultValue = resultsPage.getResultText();
            actualResultText = resultValue;
            logStep("Fetched result text: " + resultValue);

            Assert.assertTrue(resultValue.toLowerCase().contains(data.searchText.toLowerCase()),
                    "Search text not present in result");
            Assert.assertTrue(RESULT_PATTERN.matcher(resultValue).find(),
                    "Result summary format not found");
            logStep("Validated result summary text");

            resultsPage.waitForSortDropdown();
            logStep("Clicked on sort listbox");

            int optionCount = resultsPage.getSortOptionsCount();
            logStep("Sort option count: " + optionCount);

            Assert.assertTrue(optionCount >= data.minSortOptionCount,
                    "Sort option count is less than expected");

            resultsPage.selectSortOption(data.expectedSortOption);
            logStepWithScreenshot("Selected Newest Arrivals - page reloaded", "04_clicked_newest_arrivals");

            String selectedOption = resultsPage.getSelectedSortOption();
            Assert.assertEquals(selectedOption, data.expectedSortOption,
                    "Sort option not selected correctly");
            logStep("Validated selected sort option: " + selectedOption);

            finalStatus = "PASS";
            extentTest.pass("Test passed successfully");
        } catch (Exception e) {
            finalStatus = "FAIL";
            actualResultText = e.getMessage() == null ? e.toString() : e.getMessage();
            extentTest.fail("Test failed: " + actualResultText);
            System.out.println("[" + browser + "] Test failed with error: " + actualResultText);
        } finally {
            ExcelUtil.writeResult(browser, finalStatus, actualResultText);
            super.tearDown();
        }
    }

    @AfterClass(alwaysRun = true)
    public synchronized void afterClass() {
        ExtentReportManager.flushReport();
    }

    private void logStep(String message) {
        System.out.println("[" + browserName + "] " + message);
        extentTest.info(message);
    }

    private void logStepWithScreenshot(String message, String stepName) {
        System.out.println("[" + browserName + "] " + message);
        String screenshotPath = captureScreenshot(stepName);
        if (screenshotPath.isEmpty()) {
            extentTest.info(message);
        } else {
            try {
                extentTest.info(message,
                        MediaEntityBuilder.createScreenCaptureFromPath(screenshotPath).build());
            } catch (Exception e) {
                extentTest.info(message);
            }
        }
    }
}