package org.example.ExtentReport;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ExtentReportManager {

    private static ExtentReports extentReports;
    private static final Object reportLock = new Object();

    // Initialize ExtentReports (thread-safe)
    public static void initializeReport() {
        if (extentReports == null) {
            synchronized (reportLock) {
                if (extentReports == null) {
                    try {
                        // Create reports directory if not exists
                        Path reportsDir = Paths.get("reports");
                        Files.createDirectories(reportsDir);

                        ExtentSparkReporter sparkReporter = new ExtentSparkReporter("reports/ExtentReport.html");
                        sparkReporter.config().setReportName("Mini Project Report");
                        sparkReporter.config().setDocumentTitle("Amazon Search Automation");

                        extentReports = new ExtentReports();
                        extentReports.setSystemInfo("Test Executor", "Apoorv Jadhav");
                        extentReports.attachReporter(sparkReporter);

                        System.out.println("ExtentReport initialized successfully");
                    } catch (Exception e) {
                        System.out.println("Error initializing ExtentReport: " + e.getMessage());
                    }
                }
            }
        }
    }

    // Create a test
    public static ExtentTest createTest(String testName) {
        if (extentReports == null) {
            initializeReport();
        }
        return extentReports.createTest(testName);
    }

    // Flush and close report
    public static void flushReport() {
        if (extentReports != null) {
            synchronized (reportLock) {
                try {
                    extentReports.flush();
                    extentReports = null;
                    System.out.println("ExtentReport flushed successfully");
                } catch (Exception e) {
                    System.out.println("Error flushing report: " + e.getMessage());
                }
            }
        }
    }

    // Get ExtentReports instance
    public static ExtentReports getReporter() {
        if (extentReports == null) {
            initializeReport();
        }
        return extentReports;
    }
}

