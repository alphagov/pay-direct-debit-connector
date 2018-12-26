#!/bin/bash

set -e

mvn -DskipITs -DskipTests clean package
docker build -t govukpay/directdebit-connector:local .
