package uk.gov.pay.directdebit.payments.dao;

import liquibase.exception.LiquibaseException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import uk.gov.pay.directdebit.DirectDebitConnectorApp;
import uk.gov.pay.directdebit.gatewayaccounts.dao.GatewayAccountDao;
import uk.gov.pay.directdebit.gatewayaccounts.model.GatewayAccount;
import uk.gov.pay.directdebit.junit.DropwizardConfig;
import uk.gov.pay.directdebit.junit.DropwizardJUnitRunner;
import uk.gov.pay.directdebit.junit.DropwizardTestContext;
import uk.gov.pay.directdebit.junit.TestContext;
import uk.gov.pay.directdebit.payments.fixtures.GatewayAccountFixture;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static uk.gov.pay.directdebit.payments.fixtures.GatewayAccountFixture.aGatewayAccountFixture;

@RunWith(DropwizardJUnitRunner.class)
@DropwizardConfig(app = DirectDebitConnectorApp.class, config = "config/test-it-config.yaml")
public class GatewayAccountDaoIT {

    private static final String PAYMENT_PROVIDER = "sandbox";
    private static final String SERVICE_NAME = "alex";
    private static final String DESCRIPTION = "is awesome";
    private static final String ANALYTICS_ID = "DD_234098_BBBLA";
    private static final GatewayAccount.Type TYPE = GatewayAccount.Type.TEST;

    @DropwizardTestContext
    private TestContext testContext;

    private GatewayAccountDao gatewayAccountDao;
    private GatewayAccountFixture testGatewayAccount;

    @Before
    public void setup() throws IOException, LiquibaseException {
        gatewayAccountDao = testContext.getJdbi().onDemand(GatewayAccountDao.class);
        this.testGatewayAccount = aGatewayAccountFixture()
                .withPaymentProvider(PAYMENT_PROVIDER)
                .withType(TYPE)
                .withServiceName(SERVICE_NAME)
                .withAnalyticsId(ANALYTICS_ID)
                .withDescription(DESCRIPTION);
    }

    @Test
    public void shouldInsertAGatewayAccount() {
        Long id = gatewayAccountDao.insert(testGatewayAccount.toEntity());
        Map<String, Object> foundGatewayAccount = testContext.getDatabaseTestHelper().getGatewayAccountById(id);
        assertThat(foundGatewayAccount.get("id"), is(id));
        assertThat(foundGatewayAccount.get("payment_provider"), is(PAYMENT_PROVIDER));
        assertThat(foundGatewayAccount.get("service_name"), is(SERVICE_NAME));
        assertThat(foundGatewayAccount.get("analytics_id"), is(ANALYTICS_ID));
        assertThat(foundGatewayAccount.get("type"), is(TYPE.toString()));
        assertThat(foundGatewayAccount.get("description"), is(DESCRIPTION));
    }

    @Test
    public void shouldFindAGatewayAccountById() {
        testGatewayAccount.insert(testContext.getJdbi());
        GatewayAccount gatewayAccount = gatewayAccountDao.findById(testGatewayAccount.getId()).get();
        assertThat(gatewayAccount.getId(), is(notNullValue()));
        assertThat(gatewayAccount.getPaymentProvider(), is(PAYMENT_PROVIDER));
    }

    @Test
    public void shouldNotFindAGatewayAccount_ifIdIsNotValid() {
        assertThat(gatewayAccountDao.findById(3L).isPresent(), is(false));
    }

    @Test
    public void shouldFindAllGatewayAccounts() {
        String paymentProvider2 = "sandbox";
        String serviceName2 = "silvia";
        String description2 = "can't type and is not drunk maybe";
        String analyticsId2 = "DD_234098_BBBLABLA";
        testGatewayAccount.insert(testContext.getJdbi());
        GatewayAccountFixture.aGatewayAccountFixture()
                .withServiceName(serviceName2)
                .withDescription(description2)
                .withPaymentProvider(paymentProvider2)
                .withAnalyticsId(analyticsId2)
                .insert(testContext.getJdbi());

        List<GatewayAccount> gatewayAccounts = gatewayAccountDao.findAll();

        assertThat(gatewayAccounts.size(), is(2));
        GatewayAccount first = gatewayAccounts.get(0);
        GatewayAccount second = gatewayAccounts.get(1);

        assertThat(first.getId(), is(notNullValue()));
        assertThat(first.getPaymentProvider(), is(PAYMENT_PROVIDER));
        assertThat(first.getServiceName(), is(SERVICE_NAME));
        assertThat(first.getDescription(), is(DESCRIPTION));
        assertThat(first.getAnalyticsId(), is(ANALYTICS_ID));
        assertThat(first.getType(), is(TYPE));

        assertThat(second.getId(), is(notNullValue()));
        assertThat(second.getPaymentProvider(), is(paymentProvider2));
        assertThat(second.getServiceName(), is(serviceName2));
        assertThat(second.getDescription(), is(description2));
        assertThat(second.getAnalyticsId(), is(analyticsId2));
        assertThat(second.getType(), is(TYPE));
    }

    @Test
    public void shouldReturnAnEmptyListIfNoGatewayAccountsExist() {
        List<GatewayAccount> gatewayAccounts = gatewayAccountDao.findAll();
        assertThat(gatewayAccounts.isEmpty(), is(true));
    }
}
