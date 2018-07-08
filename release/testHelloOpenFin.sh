#!/bin/bash

java -DRemoteDriverURL=http://localhost:9515 -DExecPath=/Users/anthony/Documents/openfin/projects/hello-openfin-selenium-java-example/release/RunOpenFin.sh -DExecArgs="--config=/Users/anthony/Documents/openfin/projects/hello-openfin-selenium-java-example/release/openfin_selenium.json" -DSecurityRealm=realm1 -jar hello-openfin-selenium-jar-with-dependencies.jar com.openfin.HelloOpenFinTest
