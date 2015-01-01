hello-openfin-selenium-java-exampl
====================================

Example of Java test code with Chrome Driver on OpenFin Runtime

We have modified the latest Chrome driver to work with OpenFin runtime.  A copy of the chromedriver.exe is included in this project.

## Source Code

HelloOpenFinTest.java has sample code for testing HTML5 components and OpenFin javascript adapter in Hello OpenFin demo application.

## Guidelines

Since all HTML5 applications in OpenFin environment need to be started with OpenFin API, chromeDriver.get(URL) is not supported.  Test code needs to provides
OpenFinRVM and HTML5 app configuration to Chromedriver so it can start OpenFin Runtime and the html5 application before tests can run.  HelloOpenFinTest.main
accepts arguments for location of OpenFinRVM and URL of configuration file for Hello OpenFin app.

Since there are always multiple applications/windows active in OpenFin Runtime, any test needs to first select the window that is being targeted.  HelloOpenFinTest.switchWindow
method selects the window by matching its title.

Since OpenFin Runtime is started by OpenFinRVM, Chromedriver does not have direct control of OpenFin Runtime.  Chromedriver must be started before any test runs.
Once test is complete, it needs to shut down OpenFin Runtime by running javascript code "fin.desktop.System.exit();".  driver.quit() does not shut down OpenFin Runtime since
it does not have access.   We will improve how Chromedriver controls OpenFin Runtime in the future release.

## Run the example

All binaries required to run HelloOpenFinTest are in release directory:

1. Install Hello OpenFin app from http://www.openfin.co/app-gallery.html

2. start chromedriver.exe

3. run testHelloOpenFin.bat

## Building from Source

To build one jar that includes all dependencies, use the command `mvn assembly:assembly -DdescriptorId=jar-with-dependencies`

## Getting help

Please contact support@openfin.co