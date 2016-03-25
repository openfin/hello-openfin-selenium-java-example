package com.openfin;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by richard on 3/24/16.
 */
public class TpForward {
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
            if (switchWindow(driver, "Tullett Prebon - Electronic Broking")) {
                LoginPage loginPage = PageFactory.initElements(driver, LoginPage.class);
                loginPage.login();
                sleep(3);

                DashboardPage dashBoardPage = PageFactory.initElements(driver, DashboardPage.class);
                dashBoardPage.goToFpForward();
            }
            if (switchWindow(driver, "tpForwardDeal-tile")) {
                TilePage tilePage = PageFactory.initElements(driver, TilePage.class);
                tilePage.startEdit();
                tilePage.updateTenor();
            }

//            executeJavascript(driver, "fin.desktop.System.exit();");  // ask OpenFin Runtime to exit
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
            if ((System.currentTimeMillis() - start) > 10*1000) {
                break;
            }
        }

        if (!found) {
            System.out.println(windowTitle + " not found");
        }
        return found;
    }

    /**
     *
     * retrieve version of OpenFin Runtime on currently targeted Window
     *
     * @param driver instance of WebDriver
     *
     */
    private static void findRunimeVersion(WebDriver driver) {
        Object response = executeAsyncJavascript(driver,
                "var callback = arguments[arguments.length - 1];" +
                        "fin.desktop.System.getVersion(function(v) { callback(v); } );");
        System.out.println("OpenFin Runtime version " + response);
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
     * Execute an asynchronous piece of JavaScript in the context of the currently selected frame or
     * window.
     *
     * @param driver instance of WebDriver
     * @param script javascript to run
     * @return result of execution
     */
    private static Object executeAsyncJavascript(WebDriver driver, String script) {
        System.out.println("Executing Async javascript: " + script);
        return ((JavascriptExecutor) driver).executeAsyncScript(script);
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

    public static class LoginPage {
        private WebDriver driver;
        @FindBy(xpath="/html/body/div[1]/div[3]/form/input[3]")
        private WebElement username;
        @FindBy(xpath="/html/body/div[1]/div[3]/form/input[4]")
        private WebElement password;
        @FindBy(id="control-login")
        private WebElement signIn;

        public LoginPage(WebDriver driver) {
            this.driver = driver;
        }

        public void login() {
            String un = System.getProperty("username");
            String pw = System.getProperty("password");
            username.sendKeys(un);
            password.sendKeys(pw);
            this.signIn.click();
        }
    }

    public static class DashboardPage {
        private WebDriver driver;
        @FindBy(xpath="/html/body/div[1]/div[3]/ul/li[2]/a")
        private WebElement tpForwardDeal;

        public DashboardPage(WebDriver driver) {
            this.driver = driver;
        }

        public void goToFpForward() {
            this.tpForwardDeal.click();
        }
    }

    public static class TilePage {
        private WebDriver driver;
        @FindBy(className="add-edit-grid-row")
        private WebElement editButton;
        @FindBy(id = "grid-table")
        private WebElement gridTable;
        public TilePage(WebDriver driver) {
            this.driver = driver;
        }

        public void startEdit() {
            this.editButton.click();
        }

        public void updateTenor() {
            List<WebElement> list = gridTable.findElements(By.className("tenor"));
            sendKeyToTenor(list.get(0), "FIX");
            sendKeyToTenor(list.get(1), "2Y");
        }

        private void sendKeyToTenor(WebElement tenor, String text) {
            tenor.click();
            WebElement container = tenor.findElement(By.cssSelector("div.ui-select-container"));
            WebElement focusser = container.findElement(By.cssSelector(".ui-select-match"));
            focusser.click();
            WebElement input = tenor.findElement(By.cssSelector("input.ui-select-search"));
            input.clear();
            input.sendKeys(text);
            input.sendKeys(Keys.ENTER);
        }
    }

}
