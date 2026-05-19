package org.example.pages;

import org.example.base.BasePage;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.Select;

public class AmazonResultsPage extends BasePage {

    @FindBy(css = ".a-size-base.a-spacing-small.a-spacing-top-small.a-text-normal")
    private WebElement resultText;

    @FindBy(id = "s-result-sort-select")
    private WebElement sortDropdown;

    public AmazonResultsPage(WebDriver driver) {
        // super() calls PageFactory.initElements() which activates @FindBy above
        super(driver);
    }

    // Wait for results page to load
    public void waitForResultsPageLoad() {
        waitForVisibility(resultText);
    }

    // Get result text displayed on screen
    public String getResultText() {
        return getText(resultText);
    }

    // Wait for sort dropdown to be clickable
    public void waitForSortDropdown() {
        waitForClickable(sortDropdown);
    }

    // Get sort dropdown options count
    public int getSortOptionsCount() {
        Select select = new Select(sortDropdown);
        return select.getOptions().size();
    }

    // Select sort option by visible text
    public void selectSortOption(String optionText) {
        Select select = new Select(sortDropdown);
        select.selectByVisibleText(optionText);
    }

    // Get currently selected sort option text
    public String getSelectedSortOption() {
        Select select = new Select(sortDropdown);
        return select.getFirstSelectedOption().getText();
    }
}
