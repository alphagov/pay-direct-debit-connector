#!/usr/bin/env bash

set -eu
RUN_MIGRATION_VALUE=${RUN_MIGRATION:-}
RUN_APP_VALUE=${RUN_APP:-}

java -jar *-allinone.jar waitOnDependencies *.yaml

if [ -z "$RUN_MIGRATION_VALUE" && -z "$RUN_APP_VALUE" ]; then
  java -jar *-allinone.jar server *.yaml
else
  if [ "$RUN_MIGRATION_VALUE" == "true" ]; then
    java -jar *-allinone.jar db migrate *.yaml
  fi

  if [ "$RUN_APP_VALUE" == "true" ]; then
    java -jar *-allinone.jar server *.yaml
  fi
fi

exit 0
