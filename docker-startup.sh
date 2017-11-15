#!/usr/bin/env bash

set -eu

if [ -n "${AWS_CONTAINER_CREDENTIALS_RELATIVE_URI:-}" ]; then
  # Looks like we're in ECS and we've got access to credentials.
  # Use Chamber so we can use them to get secrets from Parameter Store
  AWS_REGION=${ECS_AWS_REGION} bin/chamber exec ${ECS_SERVICE} -- java -jar *-allinone.jar server *.yaml
else
  # Do a normal startup
  java -jar *-allinone.jar server *.yaml
fi
