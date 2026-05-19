package org.example.pages;

import org.example.base.BasePage;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class AmazonHomePage extends BasePage {

    @FindBy(id = "twotabsearchtextbox")
    private WebElement searchBox;

    public AmazonHomePage(WebDriver driver) {
        // super() calls PageFactory.initElements() which activates @FindBy above
        super(driver);
    }

    // Navigate to Amazon
    public void navigateToAmazon(String url) {
        driver.get(url);
    }

    // Wait for homepage to load
    public void waitForHomePageLoad() {
        waitForVisibility(searchBox);
    }

    // Search for a product
    public void searchProduct(String searchText) {
        sendKeys(searchBox, searchText);
        searchBox.sendKeys(Keys.ENTER);
    }
}
