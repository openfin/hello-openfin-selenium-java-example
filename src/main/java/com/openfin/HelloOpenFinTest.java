package com.openfin;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

import java.io.File;
import java.net.URL;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

/**
 * Chromedriver test Example for Hello OpenFin app.
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
 *    -DExecArgs=--config="https://demoappdirectory.openf.in/desktop/config/apps/OpenFin/HelloOpenFin/app.json"
 *      command argument passed to OpenFin RVM, which should be URL of a remote app config
 *
 *
 * @author wche
 * @since 11/13/14
 */
public class HelloOpenFinTest {

    /**
     * main method
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        String execPath = System.getProperty("ExecPath");   // path to OpenFinRVM.exe
        String execArgs = System.getProperty("ExecArgs");   // command arguments for RunOpenFin.bat, such as --config="app.json"
        String remoteDriverURL = System.getProperty("RemoteDriverURL");  // URL to Selenium server or chromedriver
        String securityRealm = System.getProperty("SecurityRealm");   // security realm passed to RunOpenFin.bat

        WebDriver driver;

        ChromeOptions options = new ChromeOptions();
        options.setBinary(execPath);
        options.addArguments(execArgs);
        options.setExperimentalOption("forceDevToolsScreenshot", Boolean.TRUE);
        if (securityRealm != null) {
            options.addArguments(String.format("--security-realm=%s", securityRealm));
        }
        DesiredCapabilities capabilities = DesiredCapabilities.chrome();
        capabilities.setCapability(ChromeOptions.CAPABILITY,  options);

        System.out.println("Creating remote driver " + remoteDriverURL);
        driver = new RemoteWebDriver(new URL(remoteDriverURL), capabilities);

        System.out.println("Got the driver " + driver.getCurrentUrl());
        driver.manage().timeouts().setScriptTimeout(10, TimeUnit.SECONDS);

        try {
            if (switchWindow(driver, "Hello OpenFin")) {  // select main window
                findRunimeVersion(driver);
                HelloMainPage helloMainPage = PageFactory.initElements(driver, HelloMainPage.class);
                helloMainPage.showNotification();
                sleep(2);
                helloMainPage.showCPUInfo();
            }

            if (switchWindow(driver, "Hello OpenFin CPU Info")) {  // select CPU Info window
                sleep(2);
                CPUInfoPage cpuInfoPage = PageFactory.initElements(driver, CPUInfoPage.class);

                // Selenium API for ScreenShot supported by 8.0 of Runtime
                File screenshotFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
                FileUtils.copyFile(screenshotFile, new File("showCPUInfo.png"));

                // Alternative method to take screenshot
//                String screenShotJS =     "var callback = arguments[arguments.length - 1];" +
//                                "fin.desktop.Window.getCurrent().getSnapshot(data => callback(data));";
//                String encoded64 = (String) executeAsyncJavascript(driver, screenShotJS);
//                byte[] data = Base64.getDecoder().decode(encoded64);
//                FileUtils.writeByteArrayToFile(new File("showCPUInfo.png"), data);

                cpuInfoPage.closePage();
            }

            switchWindow(driver, "Hello OpenFin");  // switch back to main window

            executeJavascript(driver, "fin.desktop.System.exit();");  // ask OpenFin Runtime to exit
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
        String[] cmd = new String[4];
        cmd[0] = "cmd.exe";
        cmd[1] = "/C";
        cmd[2] = path;
        cmd[3] = args;
        Process process = Runtime.getRuntime().exec(cmd);
        int exitVal = process.waitFor();
        System.out.println("Binary path " + path + " exit value " + exitVal);
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
                                        "if (fin && fin.desktop && fin.desktop.Window) { callback(fin.desktop.Window.getCurrent().name);} else { callback('');};");
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

    /**
     * Main page of Hello OpenFin app 
     */
    public static class HelloMainPage {
        private WebDriver driver;
        @FindBy(id="desktop-notification")
        private WebElement notificationUpElement;
        @FindBy(id="cpu-info")
        private WebElement cpuInfoElement;

        public HelloMainPage(WebDriver driver) {
            this.driver = driver;
        }

        /**
         * Click Notification button  
         */
        public void showNotification() {
            System.out.println("Showing a notification...");
            this.notificationUpElement.click();
        }

        /**
         * Click CPU Info button to show CPU info window
         */
        public void showCPUInfo() {
            System.out.println("Bring up CPU info page...");
            this.cpuInfoElement.click();
        }
    }

    /**
     *  CPU info of Hollo OpenFin app
     */
    public static class CPUInfoPage {
        private WebDriver driver;
        @FindBy(id="close-app")
        private WebElement closeElement;

        public CPUInfoPage(WebDriver driver) {
            this.driver = driver;
        }

        /**
         * Click on X button to close the window
         */
        public void closePage() {
            System.out.println("Closeing CPU Info page");
            this.closeElement.click();
        }
    }


}
