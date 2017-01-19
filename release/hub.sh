#!/bin/sh
# Example script to start hub for Selenium Grid

echo "check status on http://localhost:$PORT/grid/console"

PORT=8818
java -jar selenium-server-standalone-3.0.1.jar -role hub -port $PORT


