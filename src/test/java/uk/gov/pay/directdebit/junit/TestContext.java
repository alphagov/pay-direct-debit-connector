package uk.gov.pay.directdebit.junit;

import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import uk.gov.pay.directdebit.app.config.DirectDebitConfig;
import uk.gov.pay.directdebit.events.model.GoCardlessEventIdArgumentFactory;
import uk.gov.pay.directdebit.mandate.model.GoCardlessMandateIdArgumentFactory;
import uk.gov.pay.directdebit.mandate.model.MandateBankStatementReferenceArgumentFactory;
import uk.gov.pay.directdebit.mandate.model.PaymentProviderMandateIdArgumentFactory;
import uk.gov.pay.directdebit.mandate.model.subtype.MandateExternalIdArgumentFactory;
import uk.gov.pay.directdebit.payments.model.PaymentProviderPaymentIdArgumentFactory;
import uk.gov.pay.directdebit.util.DatabaseTestHelper;

public class TestContext {

    private final DirectDebitConfig configuration;
    private final String databaseUrl;
    private final String databaseUser;
    private final String databasePassword;
    private Jdbi jdbi;
    // This should be out of the text context really (since it is a specific class for this project)
    // but is fine for now
    private DatabaseTestHelper databaseTestHelper;
    private int port;

    public TestContext(int port, DirectDebitConfig configuration) {
        databaseUrl = configuration.getDataSourceFactory().getUrl();
        databaseUser = configuration.getDataSourceFactory().getUser();
        databasePassword = configuration.getDataSourceFactory().getPassword();
        jdbi = Jdbi.create(databaseUrl, databaseUser, databasePassword);
        jdbi.installPlugin(new SqlObjectPlugin());
        jdbi.registerArgument(new MandateExternalIdArgumentFactory());
        jdbi.registerArgument(new GoCardlessMandateIdArgumentFactory());
        jdbi.registerArgument(new MandateBankStatementReferenceArgumentFactory());
        jdbi.registerArgument(new PaymentProviderMandateIdArgumentFactory());
        jdbi.registerArgument(new PaymentProviderPaymentIdArgumentFactory());
        jdbi.registerArgument(new GoCardlessEventIdArgumentFactory());
        this.databaseTestHelper = new DatabaseTestHelper(jdbi);
        this.port = port;
        this.configuration = configuration;
    }

    public Jdbi getJdbi() {
        return jdbi;
    }

    public DatabaseTestHelper getDatabaseTestHelper() {
        return databaseTestHelper;
    }

    public int getPort() {
        return port;
    }

    public String getGoCardlessWebhookSecret() {
        return configuration.getGoCardless().getWebhookSecret();
    }

    String getDatabaseUrl() {
        return databaseUrl;
    }

    String getDatabaseUser() {
        return databaseUser;
    }

    String getDatabasePassword() {
        return databasePassword;
    }

}
