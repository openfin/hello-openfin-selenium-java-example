# Hello OpenFin Selenium Java Example

## Overview
Example of Java test code on OpenFin Runtime with Chrome Driver.

### Source Code
HelloOpenFinTest.java has sample code for testing HTML5 components and OpenFin javascript adapter in Hello OpenFin demo application.

OpenFinPlatformMessagingTest.java has sample code for testing HTML5 components and OpenFin javascript adapter in a sample OpenFin Platform based demo application.

### Guidelines
Since all HTML5 applications in the OpenFin environment need to be started with OpenFin API, chromeDriver.get(URL) is not supported.

ChromeDriver, by default, starts Chrome browser with various Chrome arguments, including remote debugging port, before running tests.  ChromeOptions.setBinary needs to be called so ChromeDriver can start OpenFin Runtime properly.  RunOpenFin.bat is an example batch file that can be set as 'binary'.

Given there can be multiple applications/windows active in OpenFin Runtime, tests must begin by selecting the targeted window. Each test script has a function that selects the window by matching it's title.

Since the OpenFin Runtime is started by OpenFinRVM, Chromedriver does not have direct control of the OpenFin Runtime. Chromedriver must be started before any test runs. Once a test is complete, it needs to shut down OpenFin Runtime by running javascript code "fin.desktop.System.exit();". driver.quit() does not shut down OpenFin Runtime since it does not have access. Moving forward, we will improve how Chromedriver controls OpenFin Runtime in the future release.

In Summary
* Tests must target specific windows
* [ChromeDriver](https://sites.google.com/a/chromium.org/chromedriver/)  must be started before tests are run
* OpenFin RunTime must be shut down after a test is completed

### Assumptions
* Version 10.66.x.x+ of Runtime is required (15.80.49.30+ if you are running an OpenFin Platform based application)

## Launch
## Run Locally
All binaries required to run HelloOpenFinTest are in release directory:

1. Install [Hello OpenFin](https://install.openfin.co/download/?config=https%3A%2F%2Fcdn.openfin.co%2Fdemos%2Fhello%2Fapp.json&fileName=HelloOpenFin&supportEmail=support%40openfin.co) App 
2. start chromedriver.exe
3. run testHelloOpenFin.bat

To run the Platform Messaging Sample we assume you have either installed the Hello OpenFin app above or you already have the OpenFin RVM installed by installing some other OpenFin Application.

1. start chromedriver.exe
2. run testOpenFinPlatformMessaging.bat

### Building from Source
To build one jar that includes all dependencies, use the command `mvn assembly:assembly -DdescriptorId=jar-with-dependencies` (ensure you have maven downloaded and setup and set in your PATH). Once the hello-openfin-selenium-jar-with-dependencies.jar is built ensure it is copied to the release folder before running the .bat file.

## Instructions for Selenium Server
Two example scripts are included in this project to demonstrate use of Selenium Server on localhost.
1. seleniumHub.bat/sh for launching Selenium Grid hub.
2. seleniumNode.bat for launching Selenium Grid node.
3. set RemoteDriverURL=http://localhost:8818/wd/hub

## Disclaimers
* This is a starter example and intended to demonstrate to app providers a sample of how to approach an implementation. There are potentially other ways to approach it and alternatives could be considered. 
* This is an open source project and all are encouraged to contribute.
* Its possible that the repo is not actively maintained.

## License
MIT

The code in this repository is covered by the included license.

However, if you run this code, it may call on the OpenFin RVM or OpenFin Runtime, which are covered by OpenFin’s Developer, Community, and Enterprise licenses. You can learn more about OpenFin licensing at the links listed below or just email us at support@openfin.co with questions.

https://openfin.co/developer-agreement/ <br/>
https://openfin.co/licensing/

## Support
Please enter an issue in the repo for any questions or problems. 
<br> Alternatively, please contact us at support@openfin.co
