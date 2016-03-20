package com.openfin;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Example for testing ui-select element of AngularUI with OpenFin and ChromeDriver.
 *
 * The sample code has been tested with bootstrap theme in https://github.com/angular-ui/ui-select/blob/master/examples/demo.html
 *
 * <p>Selected: {{person.selected}}</p>
 * <ui-select ng-model="person.selected" theme="bootstrap" ng-disabled="disabled" style="min-width: 300px;" title="Choose a person">
 * <ui-select-match placeholder="Select a person in the list or search his name/age...">{{$select.selected.name}}</ui-select-match>
 * <ui-select-choices repeat="person in people | propsFilter: {name: $select.search, age: $select.search}">
 * <div ng-bind-html="person.name | highlight: $select.search"></div>
 * <small>
 * email: {{person.email}}
 * age: <span ng-bind-html="''+person.age | highlight: $select.search"></span>
 * </small>
 * </ui-select-choices>
 * </ui-select>
 *
 * The code should work for select2 theme as well.
 *
 * Created by wche on 3/8/16.
 *
 */
public class AngularUISelectTest {

    /**
     * main method
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        String execPath = System.getProperty("ExecPath");   // path to OpenFinRVM.exe
        String execArgs = System.getProperty("ExecArgs");   // command arguments for OpenFinRVM.exe, such as --config="app.json"
        String debuggerAddress = System.getProperty("DebuggerAddress");  // debugger URL for OpenFin Runtime.exe
        String remoteDriverURL = System.getProperty("RemoteDriverURL");  // URL to Selenium server or chromedriver
        WebDriver driver;

        ChromeOptions options = new ChromeOptions();
        if (execPath != null) {
            System.out.println("Binary path " + execPath);
            options.setBinary(new File(execPath));
            options.addArguments(execArgs);
        }
        options.setExperimentalOption("debuggerAddress", debuggerAddress != null ? debuggerAddress : "localhost:9090");
        options.setExperimentalOption("forceDevToolsScreenshot", Boolean.TRUE); // required for saving screenshot

        DesiredCapabilities capabilities = DesiredCapabilities.chrome();
        capabilities.setCapability(ChromeOptions.CAPABILITY,  options);

        System.out.println("Creating remote driver " + remoteDriverURL);
        driver = new RemoteWebDriver(new URL(remoteDriverURL), capabilities);

        System.out.println("Got the driver " + driver.getCurrentUrl());
        driver.manage().timeouts().setScriptTimeout(3, TimeUnit.SECONDS);

        try {
            if (switchWindow(driver, "AngularJS ui-select")) {  // select main window
                sleep(1);
            }

            WebElement body = driver.findElement(By.tagName("body"));
            List<WebElement> list = body.findElements(By.cssSelector("div.ui-select-container"));
            for (WebElement container: list) {
                WebElement focusser = container.findElement(By.cssSelector(".ui-select-match"));
                focusser.click();
                WebElement input = container.findElement(By.cssSelector("input.ui-select-search"));
                input.clear();
                input.sendKeys("Adam");
                input.sendKeys(Keys.ENTER);
            }
            Thread.sleep(10000);  // sleep here so we can see it works
            executeJavascript(driver, "fin.desktop.System.exit();");  // ask OpenFin Runtime to exit
            sleep(2);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            driver.quit();
        }
    }


    /**
     * target a window by title
     *
     * @param webDriver instance of WebDriver
     * @param windowTitle title to match
     * @return true if successful
     * @throws Exception
     */
    private static boolean switchWindow(WebDriver webDriver, String windowTitle) throws Exception {
        boolean found = false;
        long start = System.currentTimeMillis();
        while (!found) {
            for (String name : webDriver.getWindowHandles()) {
                try {
                    webDriver.switchTo().window(name);
                    if (webDriver.getTitle().equals(windowTitle)) {
                        found = true;
                        break;
                    }
                } catch (NoSuchWindowException wexp) {
                    // some windows may get closed during Runtime startup
                    // so may get this exception depending on timing
                    System.out.println("Ignoring NoSuchWindowException " + name);
                }
            }
            Thread.sleep(1000);
            if ((System.currentTimeMillis() - start) > 5*1000) {
                break;
            }
        }

        if (!found) {
            System.out.println(windowTitle + " not found");
        }
        return found;
    }

    /**
     * Executes JavaScript in the context of the currently selected window
     *
     * @param driver instance of WebDriver
     * @param script javascript to run
     * @return
     */
    private static Object executeJavascript(WebDriver driver, String script) {
        System.out.println("Executing javascript: " + script);
        return ((JavascriptExecutor) driver).executeScript(script);
    }

    /**
     * Sleep
     * @param secs
     */
    private static void sleep(long secs) {
        System.out.println("Sleeping for " + secs + " seconds");
        try {
            Thread.sleep(secs * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
