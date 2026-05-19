package org.example.utils;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ExcelUtil {

    private static final Path EXCEL_PATH = Paths.get("src", "test", "resources", "AmazonTestData.xlsx");
    private static final String SHEET_NAME = "AmazonData";
    private static final Object excelLock = new Object();

    // Light green and light red colors
    private static final byte[] COLOR_PASS = new byte[]{(byte) 144, (byte) 238, (byte) 144};
    private static final byte[] COLOR_FAIL = new byte[]{(byte) 255, (byte) 182, (byte) 193};

    // Column indexes
    private static final int COL_BROWSER        = 0;
    private static final int COL_URL            = 1;
    private static final int COL_SEARCH_TEXT    = 2;
    private static final int COL_EXPECTED_SORT  = 3;
    private static final int COL_MIN_SORT_COUNT = 4;
    private static final int COL_FETCHED_TEXT   = 5;
    private static final int COL_STATUS         = 6;

    public static void createFileIfMissing() throws IOException {
        Files.createDirectories(EXCEL_PATH.getParent());
        if (Files.exists(EXCEL_PATH)) {
            return;
        }

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet(SHEET_NAME);

            // Header row
            Row header = sheet.createRow(0);
            header.createCell(COL_BROWSER).setCellValue("browser");
            header.createCell(COL_URL).setCellValue("url");
            header.createCell(COL_SEARCH_TEXT).setCellValue("searchText");
            header.createCell(COL_EXPECTED_SORT).setCellValue("expectedSortOption");
            header.createCell(COL_MIN_SORT_COUNT).setCellValue("minSortOptionCount");
            header.createCell(COL_FETCHED_TEXT).setCellValue("fetchedResultText");
            header.createCell(COL_STATUS).setCellValue("status");

            // Chrome test data row
            Row chromeRow = sheet.createRow(1);
            chromeRow.createCell(COL_BROWSER).setCellValue("chrome");
            chromeRow.createCell(COL_URL).setCellValue("https://www.amazon.in");
            chromeRow.createCell(COL_SEARCH_TEXT).setCellValue("mobile phone under 30000");
            chromeRow.createCell(COL_EXPECTED_SORT).setCellValue("Newest Arrivals");
            chromeRow.createCell(COL_MIN_SORT_COUNT).setCellValue(4);

            // Edge test data row
            Row edgeRow = sheet.createRow(2);
            edgeRow.createCell(COL_BROWSER).setCellValue("edge");
            edgeRow.createCell(COL_URL).setCellValue("https://www.amazon.in");
            edgeRow.createCell(COL_SEARCH_TEXT).setCellValue("mobile phone under 30000");
            edgeRow.createCell(COL_EXPECTED_SORT).setCellValue("Newest Arrivals");
            edgeRow.createCell(COL_MIN_SORT_COUNT).setCellValue(4);

            try (FileOutputStream outputStream = new FileOutputStream(EXCEL_PATH.toFile())) {
                workbook.write(outputStream);
            }
        }
    }

    // Read test input data from Excel by matching browser name
    public static TestData readData(String browser) throws IOException {
        createFileIfMissing();

        try (FileInputStream inputStream = new FileInputStream(EXCEL_PATH.toFile());
             Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheet(SHEET_NAME);
            if (sheet == null) {
                throw new IllegalStateException("Sheet not found in excel file");
            }

            DataFormatter formatter = new DataFormatter();

            // Start from row 1 (skip header)
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                String rowBrowser = formatter.formatCellValue(row.getCell(COL_BROWSER)).trim();
                if (rowBrowser.equalsIgnoreCase(browser)) {
                    TestData data = new TestData();
                    data.browser            = rowBrowser;
                    data.url                = formatter.formatCellValue(row.getCell(COL_URL)).trim();
                    data.searchText         = formatter.formatCellValue(row.getCell(COL_SEARCH_TEXT)).trim();
                    data.expectedSortOption = formatter.formatCellValue(row.getCell(COL_EXPECTED_SORT)).trim();
                    data.minSortOptionCount = Integer.parseInt(formatter.formatCellValue(row.getCell(COL_MIN_SORT_COUNT)).trim());
                    return data;
                }
            }

            throw new IllegalStateException("No test data found for browser: " + browser);
        }
    }

    // Write status and fetched result text back to the row matching the browser
    public static void writeResult(String browser, String status, String actualResult) throws IOException {
        synchronized (excelLock) {
            createFileIfMissing();

            try (FileInputStream inputStream = new FileInputStream(EXCEL_PATH.toFile());
                 XSSFWorkbook workbook = new XSSFWorkbook(inputStream)) {
                Sheet sheet = workbook.getSheet(SHEET_NAME);
                DataFormatter formatter = new DataFormatter();

                for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                    Row row = sheet.getRow(i);
                    if (row == null) continue;

                    String rowBrowser = formatter.formatCellValue(row.getCell(COL_BROWSER)).trim();
                    if (rowBrowser.equalsIgnoreCase(browser)) {
                        // Store actual result string fetched from the web page
                        row.createCell(COL_FETCHED_TEXT).setCellValue(actualResult);

                        // Color the status cell green for PASS, red for FAIL
                        XSSFCellStyle statusStyle = workbook.createCellStyle();
                        byte[] colorBytes = "PASS".equalsIgnoreCase(status) ? COLOR_PASS : COLOR_FAIL;
                        statusStyle.setFillForegroundColor(new XSSFColor(colorBytes, null));
                        statusStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

                        Cell statusCell = row.createCell(COL_STATUS);
                        statusCell.setCellValue(status);
                        statusCell.setCellStyle(statusStyle);
                        break;
                    }
                }

                try (FileOutputStream outputStream = new FileOutputStream(EXCEL_PATH.toFile())) {
                    workbook.write(outputStream);
                }
            }
        }
    }
}

