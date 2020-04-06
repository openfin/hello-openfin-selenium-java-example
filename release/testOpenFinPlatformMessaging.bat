@ECHO OFF
REM start tests with Selenium Grid running on http://10.37.129.2:8818/wd/hub
REM java -DRemoteDriverURL=http://10.37.129.2:8818/wd/hub -DExecPath=RunOpenFin.bat -DExecArgs=--config="https://4k4o3.csb.app/platform-messaging/app.json" -cp hello-openfin-selenium-jar-with-dependencies.jar com.openfin.OpenFinPlatformMessagingTest

REM start tests with standalone ChromeDrive.exe running on localhost:9515
java -DRemoteDriverURL=http://localhost:9515 -DExecPath=RunOpenFin.bat -DExecArgs="--config=%~dp0\openfin_selenium_platform.json" -cp hello-openfin-selenium-jar-with-dependencies.jar com.openfin.OpenFinPlatformMessagingTest
