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
 * Created by richard on 3/23/16.
 */
public class SimpleTest {


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
        int etagSeq = 1;

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

        while (etagSeq < 20) {
            driver = new RemoteWebDriver(new URL(remoteDriverURL), capabilities);
            System.out.println("Got the driver " + driver.getCurrentUrl());
            driver.manage().timeouts().setScriptTimeout(3, TimeUnit.SECONDS);
            try {
                if (switchWindow(driver, "ETAG check")) {  // select main window
                    sleep(1);
                }
                WebElement body = driver.findElement(By.id("etagseq"));
                String tag = body.getText();
                if (Integer.parseInt(tag) != etagSeq) {
                    System.out.println(String.format("Mismatch %d %s", etagSeq, tag));
                    break;
                }
                System.out.printf(String.format("Run %d", etagSeq));
                executeJavascript(driver, "fin.desktop.System.exit();");  // ask OpenFin Runtime to exit
                sleep(5);
                etagSeq++;
            } catch (Exception e) {
                e.printStackTrace();
                break;
            } finally {
                driver.quit();
            }
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
