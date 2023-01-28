package com.openfin;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Simple test with Chrome browser and official version of ChromeDriver without patches by OpenFin
 *
 * Created by wche on 3/16/16.
 */
public class ChromeTest {

    public static void main(String[] args) throws Exception {
        String remoteDriverURL = System.getProperty("RemoteDriverURL");  // URL to Selenium server or chromedriver
        WebDriver driver;

        ChromeOptions options = new ChromeOptions();
        options.setExperimentalOption("forceDevToolsScreenshot", Boolean.TRUE); // required for saving screenshot

        DesiredCapabilities capabilities = new DesiredCapabilities();
        capabilities.setCapability(ChromeOptions.CAPABILITY,  options);

        System.out.println("Creating remote driver " + remoteDriverURL);
        driver = new RemoteWebDriver(new URL(remoteDriverURL), capabilities);

        System.out.println("Got the driver " + driver.getCurrentUrl());
        driver.manage().timeouts().setScriptTimeout(3, TimeUnit.SECONDS);

        try {
            driver.get("http://localhost:8080/examples/demo.html");
            Thread.sleep(2000);
            WebElement body = driver.findElement(By.tagName("body"));
            List<WebElement> list = body.findElements(By.cssSelector("div.ui-select-container"));
            System.out.println(String.format("populating input of %d elements", list.size()));
            for (WebElement container: list) {

                WebElement focusser = container.findElement(By.cssSelector("input.ui-select-focusser"));

                focusser.sendKeys("aa\n");

//                WebElement focusser = container.findElement(By.cssSelector(".ui-select-match"));

//                focusser.click();
                WebElement input = container.findElement(By.cssSelector("input.ui-select-search"));
                input.clear();
                input.sendKeys("aa");
                input.sendKeys(Keys.ENTER);
                Actions action = new Actions(driver);
                action.sendKeys(Keys.ARROW_DOWN);

            }
            Thread.sleep(5000);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            driver.quit();
        }
    }


}
