package uk.gov.pay.directdebit.gatewayaccounts.dao;

import liquibase.exception.LiquibaseException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import uk.gov.pay.directdebit.DirectDebitConnectorApp;
import uk.gov.pay.directdebit.gatewayaccounts.model.GatewayAccount;
import uk.gov.pay.directdebit.gatewayaccounts.model.PaymentProvider;
import uk.gov.pay.directdebit.gatewayaccounts.model.PaymentProviderAccessToken;
import uk.gov.pay.directdebit.gatewayaccounts.model.PaymentProviderOrganisationIdentifier;
import uk.gov.pay.directdebit.junit.DropwizardConfig;
import uk.gov.pay.directdebit.junit.DropwizardJUnitRunner;
import uk.gov.pay.directdebit.junit.DropwizardTestContext;
import uk.gov.pay.directdebit.junit.TestContext;
import uk.gov.pay.directdebit.payments.fixtures.GatewayAccountFixture;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.*;
import static uk.gov.pay.directdebit.payments.fixtures.GatewayAccountFixture.aGatewayAccountFixture;

@RunWith(DropwizardJUnitRunner.class)
@DropwizardConfig(app = DirectDebitConnectorApp.class, config = "config/test-it-config.yaml")
public class GatewayAccountCommandDaoTest {

    private static final String EXTERNAL_ID = "external1d";
    private static final PaymentProvider PAYMENT_PROVIDER = PaymentProvider.SANDBOX;
    private static final String SERVICE_NAME = "a service";
    private static final String DESCRIPTION = "command test service";
    private static final String ANALYTICS_ID = "DD_234098_BBBLA";
    private static final GatewayAccount.Type TYPE = GatewayAccount.Type.TEST;

    @DropwizardTestContext
    private TestContext testContext;

    private GatewayAccountCommandDao gatewayAccountCommandDao;
    private GatewayAccountFixture testGatewayAccount;

    @Before
    public void setup() throws IOException, LiquibaseException {
        gatewayAccountCommandDao = new GatewayAccountCommandDao(testContext.getJdbi());
        this.testGatewayAccount = aGatewayAccountFixture()
                .withExternalId(EXTERNAL_ID)
                .withPaymentProvider(PAYMENT_PROVIDER)
                .withType(TYPE)
                .withServiceName(SERVICE_NAME)
                .withAnalyticsId(ANALYTICS_ID)
                .withDescription(DESCRIPTION)
                .withAccessToken(null)
                .withOrganisation(null);
    }

    @Test
    public void shouldInsertAGatewayAccount() {
        Optional<Long> id = gatewayAccountCommandDao.insert(testGatewayAccount.toEntity());
        assertThat(id.isPresent(), is(true));
        Map<String, Object> foundGatewayAccount = 
                testContext.getDatabaseTestHelper().getGatewayAccountByExternalId(EXTERNAL_ID);
        assertThat(foundGatewayAccount.get("id"), is(id.get()));
        assertThat(foundGatewayAccount.get("external_id"), is(EXTERNAL_ID));
        assertThat(foundGatewayAccount.get("payment_provider"), is(PAYMENT_PROVIDER.toString()));
        assertThat(foundGatewayAccount.get("service_name"), is(SERVICE_NAME));
        assertThat(foundGatewayAccount.get("analytics_id"), is(ANALYTICS_ID));
        assertThat(foundGatewayAccount.get("type"), is(TYPE.toString()));
        assertThat(foundGatewayAccount.get("description"), is(DESCRIPTION));
        assertThat(foundGatewayAccount.get("access_token"), is(nullValue()));
        assertThat(foundGatewayAccount.get("organisation"), is(nullValue()));
    }

    @Test
    public void shouldInsertAGatewayAccount_withAccessTokenAndOrganisation() {
        testGatewayAccount
                .withAccessToken(PaymentProviderAccessToken.of("an-access-token"))
                .withOrganisation(PaymentProviderOrganisationIdentifier.of("an-organisation"));
        Optional<Long> id = gatewayAccountCommandDao.insert(testGatewayAccount.toEntity());
        assertThat(id.isPresent(), is(true));
        Map<String, Object> foundGatewayAccount =
                testContext.getDatabaseTestHelper().getGatewayAccountByExternalId(EXTERNAL_ID);
        assertThat(foundGatewayAccount.get("id"), is(id.get()));
        assertThat(foundGatewayAccount.get("external_id"), is(EXTERNAL_ID));
        assertThat(foundGatewayAccount.get("payment_provider"), is(PAYMENT_PROVIDER.toString()));
        assertThat(foundGatewayAccount.get("service_name"), is(SERVICE_NAME));
        assertThat(foundGatewayAccount.get("analytics_id"), is(ANALYTICS_ID));
        assertThat(foundGatewayAccount.get("type"), is(TYPE.toString()));
        assertThat(foundGatewayAccount.get("description"), is(DESCRIPTION));
        assertThat(foundGatewayAccount.get("access_token"), is("an-access-token"));
        assertThat(foundGatewayAccount.get("organisation"), is("an-organisation"));
    }
}
