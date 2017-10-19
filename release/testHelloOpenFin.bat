@ECHO OFF
REM start tests with Selenium Grid running on http://10.37.129.2:8818/wd/hub
REM java -DRemoteDriverURL=http://10.37.129.2:8818/wd/hub -DExecPath=RunOpenFin.bat -DExecArgs=--config="https://demoappdirectory.openf.in/desktop/config/apps/OpenFin/HelloOpenFin/selenium.json" -jar hello-openfin-selenium-jar-with-dependencies.jar com.openfin.HelloOpenFinTest

REM start tests with standalone ChromeDrive.exe running on localhost:9515
java -DRemoteDriverURL=http://localhost:9515 -DExecPath=RunOpenFin.bat -DExecArgs=--config="https://demoappdirectory.openf.in/desktop/config/apps/OpenFin/HelloOpenFin/selenium.json" -DSecurityRealm=realm1 -jar hello-openfin-selenium-jar-with-dependencies.jar com.openfin.HelloOpenFinTest
