package com.openfin;

import org.apache.commons.io.FileUtils;
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
 * Chromedriver test Example for Sample Platform app.
 *
 * Chromedriver must be running before the test
 *
 * Runtime argument
 *
 *   -DDriverPort=9515
 *
 *    -DRemoteDriverURL=http://localhost:8818/wd/hub
 *      URL to access Selenium server or chromedriver.
 *
 *    -DExecPath=RunOpenFin.bat
 *      Full path of batch file to start OpenFin RVM.
 *
 *    -DExecArgs=--config="https://4k4o3.csb.app/platform-messaging/app.json"
 *      command argument passed to OpenFin RVM, which should be URL of a remote app config
 *
 *
 * @author johnman
 * @since 2020/04/03
 */
public class OpenFinPlatformMessagingTest {

    /**
     * main method
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        String execPath = System.getProperty("ExecPath");   // path to OpenFinRVM.exe
        String execArgs = System.getProperty("ExecArgs");   // command arguments for RunOpenFin.bat, such as --config="app.json"
        String remoteDriverURL = System.getProperty("RemoteDriverURL");  // URL to Selenium server or chromedriver
        String debuggerAddress = System.getProperty("DebuggerAddress");

        WebDriver driver;

        ChromeOptions options = new ChromeOptions();
        options.setBinary(execPath);
        options.addArguments(execArgs);
        if (debuggerAddress != null) {
            // if devtools_port is set in app.json and ChromeDriver needs to communicate on that port,  set the following property
            // to be the same as devtools_port
            // if debuggerAddress is set,  ChromeDriver does NOT start "binary" and assumes it is already running,
            // it needs to start separately
            options.setExperimentalOption("debuggerAddress", debuggerAddress);
            launchOpenfinApp(execPath, execArgs);
        }
        DesiredCapabilities capabilities = DesiredCapabilities.chrome();
        capabilities.setCapability(ChromeOptions.CAPABILITY,  options);
        System.out.println("OpenFinPlatformMessagingTest starting...");
        System.out.println("Creating remote driver " + remoteDriverURL);
        driver = new RemoteWebDriver(new URL(remoteDriverURL), capabilities);

        System.out.println("Got the driver " + driver.getCurrentUrl());
        driver.manage().timeouts().setScriptTimeout(10, TimeUnit.SECONDS);

        try {
            if (switchWindow(driver, "Main Window")) {  // select main window
                findRunimeVersion(driver);
                PlatformMainPage platformMainPage = PageFactory.initElements(driver, PlatformMainPage.class);
                platformMainPage.openView();
                sleep(2);
                platformMainPage.openWindow();
                platformMainPage.sendMessage();
            }

            if (switchWindow(driver, "View One")) {  // select window with view one view
                sleep(2);
                ViewOnePage viewOnePage = PageFactory.initElements(driver, ViewOnePage.class);

                // Selenium API for ScreenShot supported by 8.0 of Runtime
                File screenshotFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
                FileUtils.copyFile(screenshotFile, new File("viewOne.png"));

                viewOnePage.logPassedMessage();
            }

            switchWindow(driver, "Main Window");  // switch back to main window

            executeJavascript(driver, "fin.Platform.getCurrentSync().quit();");  // ask OpenFin Runtime to exit
            sleep(2);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            driver.quit();
        }

    }

    /**
     * Launch OpenFin app with installer or RVM
     *
     * @param path path to script that launches OpenFin RVM/Runtime
     * @param args arguments passed to the script
     * @throws Exception
     */
    private static void launchOpenfinApp(String path, String args) throws Exception{
        System.out.println(String.format("Starting %s %s", path, args));
        List<String> list = new ArrayList<String>();
        list.add("cmd.exe");
        list.add("/C");
        list.add(path);
        list.add(args);
        ProcessBuilder pb = new ProcessBuilder(list);
        pb.inheritIO();
        pb.start();
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
        System.out.println("Searching for window with title: " + windowTitle);
        while (!found) {
            for (String name : webDriver.getWindowHandles()) {
                try {

                    webDriver.switchTo().window(name);
                    String currentTitle = webDriver.getTitle();
                    System.out.println("Current Window Title: " + currentTitle);
                    if (currentTitle.equals(windowTitle)) {
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
     * target a window by name
     *
     * @param webDriver instance of WebDriver
     * @param windowName name to match
     * @return true if successful
     * @throws Exception
     */
    private static boolean switchWindowByName(WebDriver webDriver, String windowName) throws Exception {
        boolean found = false;
        long start = System.currentTimeMillis();
        while (!found) {
            for (String handle : webDriver.getWindowHandles()) {
                try {
                    webDriver.switchTo().window(handle);
                    String url = webDriver.getCurrentUrl();
                    System.out.printf(String.format("checking URL: %s", url));
                    if (url.startsWith("http")) {
                        Object response = executeAsyncJavascript(webDriver,
                                "var callback = arguments[arguments.length - 1];" +
                                        "if (fin && fin.me) { callback(fin.me.name);} else { callback('');};");
                        System.out.println(String.format("window name %s", response.toString()));
                        if (response != null && response.toString().equals(windowName)) {
                            found = true;
                            break;
                        }
                    }
                } catch (NoSuchWindowException wexp) {
                    // some windows may get closed during Runtime startup
                    // so may get this exception depending on timing
                    System.out.println("Ignoring NoSuchWindowException " + handle);
                }
            }
            Thread.sleep(1000);
            if ((System.currentTimeMillis() - start) > 5*1000) {
                break;
            }
        }

        if (!found) {
            System.out.println(windowName + " not found");
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
                        "fin.System.getVersion().then(v => callback(v)).catch(err => console.log(err));");
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

    /**
     * Main page of Hello OpenFin app 
     */
    public static class PlatformMainPage {
        private WebDriver driver;
        @FindBy(id="launch-window-one")
        private WebElement launchWindowElement;
        @FindBy(id="launch-view-one")
        private WebElement launchViewElement;
        @FindBy(id="send-message")
        private WebElement sendMessageElement;

        public PlatformMainPage(WebDriver driver) {
            this.driver = driver;
        }

        /**
         * Click Open Window Link
         */
        public void openWindow() {
            System.out.println("Opening a window...");
            this.launchWindowElement.click();
        }

        /**
         * Click Open View Link
         */
        public void openView() {
            System.out.println("Opening a view...");
            this.launchViewElement.click();
        }

        /**
         * Click Send Message Button
         */
        public void sendMessage() {
            System.out.println("Sending a message to a launched window/view");
            this.sendMessageElement.click();
        }
    }

    /**
     *  View One of Platform Messaging Sample App
     */
    public static class ViewOnePage {
        private WebDriver driver;
        @FindBy(id="passed-message")
        private WebElement passedMessageElement;

        public ViewOnePage(WebDriver driver) {
            this.driver = driver;
        }

        /**
         * Log Out the passed message
         */
        public void logPassedMessage() {
            String passedMessage = this.passedMessageElement.getText();
            System.out.println("View One was passed the following message: " + passedMessage);
        }
    }
}
