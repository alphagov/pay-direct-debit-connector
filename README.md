# pay-direct-debit-connector

The [GOV.UK Pay](https://www.payments.service.gov.uk/) Direct Debit Connector

## Environment Variables

| NAME                    | DESCRIPTION                                                                    |
| ----------------------- | ------------------------------------------------------------------------------ |
| `ADMIN_PORT`            | The port number to listen for Dropwizard admin requests on. Defaults to `8081`. |
| `CERTS_PATH`            | If set, add all certificates in this directory to the default Java truststore. |
| `RUN_APP`               | Set to `true` to run the application. Defaults to `true`. |
| `PORT`                  | The port number to listen for requests on. Defaults to `8080`. |
| `JAVA_OPTS`             | Commandline arguments to pass to the java runtime. Optional. |
| `JAVA_HOME`             | The location of the JRE. Set to `/opt/java/openjdk` in the `Dockerfile`. |
| `DB_HOST`               | The hostname of the database server. |
| `DB_PASSWORD`           | The password for the `DB_USER` user. |
| `DB_SSL_OPTION`         | To turn TLS on this value must be set as `ssl=true`. Otherwise must be empty. |
| `DB_NAME`               | The name of the database to use. Defaults to `-directdebit_connector`. |
| `DB_USER`               | The username to log into the database as. |
| `RUN_MIGRATION`         | Set to `true` to run a database migration. Defaults to `false`. |
| `FRONTEND_URL`          | The URL of the [pay-frontend](https://github.com/alphagov/pay-frontend) microservice. Defaults to the empty string. |
| `ADMINUSERS_URL`        | The URL of the [pay-adminusers](https://github.com/alphagov/pay-adminusers) microservice. Defaults to the empty string. |
| `METRICS_HOST`          | The hostname to send graphite metrics to. Defaults to `localhost`. |
| `METRICS_PORT`          | The port number to send graphite metrics to. Defaults to `8092`. |

### GoCardless settings

| NAME                                                  | DESCRIPTION                                                                    |
| ----------------------------------------------------- | ------------------------------------------------------------------------------ |
| `GOCARDLESS_TEST_OAUTH_BASE_URL`                      | The base URL to use for linking a service's GoCardless account in the GoCardless sandbox environment to GOV.UK Pay's platform account with OAuth. Defaults to the empty string. |
| `GOCARDLESS_LIVE_OAUTH_BASE_URL`                      | The base URL to use for linking a service's GoCardless account in the GoCardless live environment to GOV.UK Pay's platform account with OAuth for live transactions. Defaults to the empty string. |
| `GOCARDLESS_TEST_CLIENT_ID`                           | The client ID to use when obtaining an OAuth access token for GoCardless's sandbox environment in order to link accounts. Defaults to the empty string. |
| `GOCARDLESS_TEST_CLIENT_SECRET`                       | The client secret to use when obtaining an OAuth access token for GoCardless's sandbox environment in order to link accounts. Defaults to the empty string. |
| `GOCARDLESS_LIVE_CLIENT_ID`                           | The client ID to use when obtaining an OAuth access token for GoCardless's live environment in order to link accounts. Defaults to the empty string. |
| `GOCARDLESS_LIVE_CLIENT_SECRET`                       | The client secret to use when obtaining an OAuth access token for GoCardless's live environment in order to link accounts. Defaults to the empty string. |
| `GDS_DIRECTDEBIT_CONNECTOR_GOCARDLESS_ACCESS_TOKEN`   | The access token to use for GoCardless API calls. Defaults to the empty string. |
| `GDS_DIRECTDEBIT_CONNECTOR_GOCARDLESS_URL`            | The URL to the GoCardless API. Defaults to the empty string. |
| `GDS_DIRECTDEBIT_CONNECTOR_GOCARDLESS_WEBHOOK_SECRET` | The shared secret to use for verifying webhook calls from GoCardless. Defaults to `change-me`. |
| `GDS_DIRECTDEBIT_CONNECTOR_GOCARDLESS_ENVIRONMENT`    | The GoCardless environment to use. Defaults to `sandbox`. |

## API Specification


## Wiki

- [Wiki page](https://github.com/alphagov/pay-direct-debit/wiki)

## Licence

[MIT License](LICENCE)

## Responsible Disclosure

GOV.UK Pay aims to stay secure for everyone. If you are a security researcher and have discovered a security vulnerability in this code, we appreciate your help in disclosing it to us in a responsible manner. We will give appropriate credit to those reporting confirmed issues. Please e-mail gds-team-pay-security@digital.cabinet-office.gov.uk with details of any issue you find, we aim to reply quickly.

