@ECHO OFF
REM Example script to start test node for Selenium Grid
REM http://10.37.129.2:8818/grid/register should be updated for correct hub

java -Dwebdriver.chrome.driver="chromedriver.exe" -jar selenium-server-standalone-3.0.1.jar -role node -hub http://10.37.129.2:8818/grid/register -browser "browserName=chrome,version=49,maxInstances=1,platform=WIN8_1"
