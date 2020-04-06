#!/bin/bash

java -DRemoteDriverURL=http://localhost:9515 -DExecPath=./RunOpenFin.sh -DExecArgs="--config=./openfin_selenium_platform.json" -DSecurityRealm=realm1 -cp hello-openfin-selenium-jar-with-dependencies.jar com.openfin.OpenFinPlatformMessagingTest
