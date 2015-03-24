hello-openfin-selenium-java-example
====================================

Example of Java test code with Chrome Driver on OpenFin Runtime

We have modified the latest Chrome driver to work with OpenFin runtime.  A copy of the chromedriver.exe is included in this project.

## Source Code

HelloOpenFinTest.java has sample code for testing HTML5 components and OpenFin javascript adapter in Hello OpenFin demo application.

## Guidelines

Since all HTML5 applications in the OpenFin environment need to be started with OpenFin API, chromeDriver.get(URL) is not supported. Test code needs to provide the OpenFinRVM and a HTML5 app configuration to Chromedriver so it can start OpenFin Runtime and the html5 application before tests can be run. config.js has the location of OpenFinRVM and the URL for the 'Hello OpenFin' configuration file.

Given there can be multiple applications/windows active in OpenFin Runtime, tests must begin by selecting the targeted window. Each test script has a function that selects the window by matching it's title.

Since the OpenFin Runtime is started by OpenFinRVM, Chromedriver does not have direct control of the OpenFin Runtime. Chromedriver must be started before any test runs. Once a test is complete, it needs to shut down OpenFin Runtime by running javascript code "fin.desktop.System.exit();". driver.quit() does not shut down OpenFin Runtime since it does not have access. Moving forward, we will improve how Chromedriver controls OpenFin Runtime in the future release.

In Summary
* Tests must target specific windows
* Chromedriver must be started before tests are run
* OpenFin RunTime must be shut down after a test is completed

## Run the example

All binaries required to run HelloOpenFinTest are in release directory:

1. Install Hello OpenFin app from http://www.openfin.co/app-gallery.html

2. start chromedriver.exe

3. run testHelloOpenFin.bat

## Building from Source

To build one jar that includes all dependencies, use the command `mvn assembly:assembly -DdescriptorId=jar-with-dependencies`

## Getting help

Please contact support@openfin.co
