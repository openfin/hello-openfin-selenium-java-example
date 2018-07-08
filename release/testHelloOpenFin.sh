#!/bin/bash

java -DRemoteDriverURL=http://localhost:9515 -DExecPath=./RunOpenFin.sh -DExecArgs="--config=./openfin_selenium.json" -DSecurityRealm=realm1 -jar hello-openfin-selenium-jar-with-dependencies.jar com.openfin.HelloOpenFinTest
