package uk.gov.pay.directdebit.app.config;

import org.eclipse.persistence.config.SessionCustomizer;
import org.eclipse.persistence.sessions.DatabaseLogin;
import org.eclipse.persistence.sessions.Session;

public class DirectDebitSessionCustomiser implements SessionCustomizer {

    private static final int QUERY_RETRY_ATTEMPT_COUNT_ZERO_BASED_INDEX = 0;
    private static final int DELAY_BETWEEN_CONNECTION_ATTEMPTS_MILLIS = 2000;

    @Override
    public void customize(Session session) throws Exception {
        DatabaseLogin datasourceLogin = (DatabaseLogin) session.getDatasourceLogin();
        datasourceLogin.setQueryRetryAttemptCount(QUERY_RETRY_ATTEMPT_COUNT_ZERO_BASED_INDEX);
        datasourceLogin.setDelayBetweenConnectionAttempts(DELAY_BETWEEN_CONNECTION_ATTEMPTS_MILLIS);
    }
}
