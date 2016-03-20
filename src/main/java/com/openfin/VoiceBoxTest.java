package com.openfin;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Test VoiceBox app
 *
 * Created by wche on 3/11/16.
 */
public class VoiceBoxTest {


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
        WebDriver driver = null;

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

        boolean keepRunning = true;
        int count = 0;

        while (keepRunning) {
            try {
                System.out.println("Creating remote driver " + remoteDriverURL);
                driver = new RemoteWebDriver(new URL(remoteDriverURL), capabilities);
                System.out.println("Got the driver " + driver.getCurrentUrl());
                driver.manage().timeouts().setScriptTimeout(3, TimeUnit.SECONDS);

                List<String> list = new ArrayList<String>();
                list.add("https://test.web.tradervoicebox.com/login");
                list.add("https://test.web.tradervoicebox.com/dashboard");
                if (switchWindowByUrl(driver, list)) {
                    sleep(1);
                }
                if (driver.getCurrentUrl().equals(list.get(0))) {
                    LoginPage loginPage = PageFactory.initElements(driver, LoginPage.class);
                    loginPage.login();

                    List<String> list2 = new ArrayList<String>();
                    list2.add("https://test.web.tradervoicebox.com/dashboard");
                    if (switchWindowByUrl(driver, list2)) {
                        sleep(1);
                    }
                }

                DashBoardPage dashBoardPage = PageFactory.initElements(driver, DashBoardPage.class);
                dashBoardPage.toggleInviteScreen();
                dashBoardPage.logOff();

                executeJavascript(driver, "fin.desktop.System.exit();");  // ask OpenFin Runtime to exit
                sleep(2);

            } catch (Exception e) {
                keepRunning = false;
                e.printStackTrace();
            } finally {
                if (driver != null) {
                    driver.quit();
                    driver = null;
                }
            }
            count++;
            System.out.println(String.format("Finished loop %d", count));
            sleep(2);
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


    private static boolean switchWindowByUrl(WebDriver webDriver, List<String> windowUrls) throws Exception {
        boolean found = false;
        long start = System.currentTimeMillis();
        while (!found) {
            for (String name : webDriver.getWindowHandles()) {
                try {
                    webDriver.switchTo().window(name);
                    if (windowUrls.contains(webDriver.getCurrentUrl())) {
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
            System.out.println("window not found");
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
        @FindBy(css="input[type=\"username\"]")
        private WebElement usernameElement;
        @FindBy(css="input[type=\"password\"]")
        private WebElement passwordElement;
        @FindBy(css="button[type=\"submit\"]")
        private WebElement loginButton;

        public LoginPage(WebDriver driver) {
            this.driver = driver;
        }

        public void login() {
            System.out.println("logging in...");
            this.usernameElement.sendKeys("openfin");
            this.passwordElement.sendKeys("openfin");
            this.loginButton.click();
        }
    }

    public static class DashBoardPage {
        private WebDriver driver;
        @FindBy(xpath="//*[@id=\"header\"]/nav/div[1]/div/ul/li[5]/invite/button")
        private WebElement inviteButton;
        @FindBy(xpath = "//*[@id=\"ngdialog3\"]/div[2]/div[1]/div[1]/button")
        private WebElement cancelInvite;
        @FindBy(xpath="//*[@id=\"header\"]/nav/div[1]/div/ul/li[1]")
        private WebElement menuDropdownButton;
        @FindBy(xpath = "//*[@id=\"header\"]/nav/div[1]/div/ul/li[1]/ul/li[14]/a")
        private WebElement logoutButton;
        @FindBy(xpath = "//*[@id=\"confirm-dialog\"]/div[3]/button[1]")
        private WebElement logoutConfirm;

        public DashBoardPage(WebDriver driver) {
            this.driver = driver;
        }
        public void toggleInviteScreen() {
            System.out.println("toggling invite...");
            inviteButton.click();
            sleep(2);
            cancelInvite = driver.findElement(By.cssSelector ("#ngdialog1 button[class=\"close\"]"));
            if (cancelInvite != null) {
                cancelInvite.click();
                sleep(1);
            } else {
                System.out.printf("cancelInvite not found");
            }
        }
        public void logOff() {
            menuDropdownButton = driver.findElement(By.xpath("//*[@id=\"header\"]/nav/div[1]/div/ul/li[1]/a"));
            menuDropdownButton.click();
            sleep(1);
            logoutButton.click();
            sleep(1);
            logoutConfirm.click();
            sleep(1);
        }
    }

}
