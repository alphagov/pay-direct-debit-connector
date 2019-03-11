#!/usr/bin/env bash

set -eu

RUN_MIGRATION=${RUN_MIGRATION:-false}
RUN_APP=${RUN_APP:-true}
JAVA_OPTS=${JAVA_OPTS:-}

# Make a copy of the truststore to modify. We only need to do this as we have a
# readonly Docker volume mounted on /etc/ssl/certs. Once that's removed, this
# complexity goes away.
truststore=$(mktemp)
echo "Setting up temporary truststore $truststore"
cat /etc/ssl/certs/java/cacerts > "$truststore"

if [ -n "${CERTS_PATH:-}" ]; then
  i=0
  truststore_pass=changeit
  for cert in "$CERTS_PATH"/*; do
    echo "Adding $cert to $truststore"
    [ ! -f "$cert" ] || keytool -importcert -noprompt -keystore "$truststore" -storepass "$truststore_pass" -file "$cert" -alias custom$((i++))
  done
fi

java "-Djavax.net.ssl.trustStore=$truststore" $JAVA_OPTS -jar *-allinone.jar waitOnDependencies *.yaml

if [ "$RUN_MIGRATION" == "true" ]; then
  java "-Djavax.net.ssl.trustStore=$truststore" $JAVA_OPTS -jar *-allinone.jar db migrate *.yaml
fi

if [ "$RUN_APP" == "true" ]; then
  java "-Djavax.net.ssl.trustStore=$truststore" $JAVA_OPTS -jar *-allinone.jar server *.yaml
fi

exit 0
