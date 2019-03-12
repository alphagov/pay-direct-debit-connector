#!/bin/bash

set -e

cd "$(dirname "$0")"

mvn -DskipITs clean package
docker build -t govukpay/directdebit-connector:local .
