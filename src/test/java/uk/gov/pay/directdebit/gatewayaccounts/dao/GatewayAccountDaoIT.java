package uk.gov.pay.directdebit.gatewayaccounts.dao;

import liquibase.exception.LiquibaseException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import uk.gov.pay.directdebit.DirectDebitConnectorApp;
import uk.gov.pay.directdebit.gatewayaccounts.dao.GatewayAccountDao;
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
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static uk.gov.pay.directdebit.payments.fixtures.GatewayAccountFixture.aGatewayAccountFixture;

@RunWith(DropwizardJUnitRunner.class)
@DropwizardConfig(app = DirectDebitConnectorApp.class, config = "config/test-it-config.yaml")
public class GatewayAccountDaoIT {

    private static final String EXTERNAL_ID = "external1d";
    private static final PaymentProvider PAYMENT_PROVIDER = PaymentProvider.SANDBOX;
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
        Long id = gatewayAccountDao.insert(testGatewayAccount.toEntity());
        Map<String, Object> foundGatewayAccount = testContext.getDatabaseTestHelper().getGatewayAccountById(id);
        assertThat(foundGatewayAccount.get("id"), is(id));
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
    public void shouldFindAGatewayAccountById() {
        testGatewayAccount.insert(testContext.getJdbi());
        GatewayAccount gatewayAccount = gatewayAccountDao.findById(testGatewayAccount.getId()).get();
        assertThat(gatewayAccount.getId(), is(notNullValue()));
        assertThat(gatewayAccount.getExternalId(), is(EXTERNAL_ID));
        assertThat(gatewayAccount.getPaymentProvider(), is(PAYMENT_PROVIDER));
        assertThat(gatewayAccount.getAnalyticsId(), is(ANALYTICS_ID));
        assertThat(gatewayAccount.getDescription(), is(DESCRIPTION));
        assertThat(gatewayAccount.getType(), is(TYPE));
    }

    @Test
    public void shouldFindAGatewayAccountByExternalId() {
        testGatewayAccount.insert(testContext.getJdbi());
        GatewayAccount gatewayAccount = gatewayAccountDao.findByExternalId(EXTERNAL_ID).get();
        assertThat(gatewayAccount.getId(), is(notNullValue()));
        assertThat(gatewayAccount.getExternalId(), is(EXTERNAL_ID));
        assertThat(gatewayAccount.getPaymentProvider(), is(PAYMENT_PROVIDER));
        assertThat(gatewayAccount.getAnalyticsId(), is(ANALYTICS_ID));
        assertThat(gatewayAccount.getDescription(), is(DESCRIPTION));
        assertThat(gatewayAccount.getType(), is(TYPE));
    }
    @Test
    public void shouldNotFindAGatewayAccount_ifIdIsNotValid() {
        assertThat(gatewayAccountDao.findById(3L).isPresent(), is(false));
    }

    @Test
    public void shouldFindAllGatewayAccounts() {
        PaymentProvider paymentProvider2 = PaymentProvider.GOCARDLESS;
        String serviceName2 = "silvia";
        String externalId2 = "anotherid";
        String description2 = "can't type and is not drunk maybe";
        String analyticsId2 = "DD_234098_BBBLABLA";
        testGatewayAccount.insert(testContext.getJdbi());
        GatewayAccountFixture.aGatewayAccountFixture()
                .withExternalId(externalId2)
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
        assertThat(first.getExternalId(), is(EXTERNAL_ID));
        assertThat(first.getPaymentProvider(), is(PAYMENT_PROVIDER));
        assertThat(first.getServiceName(), is(SERVICE_NAME));
        assertThat(first.getDescription(), is(DESCRIPTION));
        assertThat(first.getAnalyticsId(), is(ANALYTICS_ID));
        assertThat(first.getType(), is(TYPE));

        assertThat(second.getId(), is(notNullValue()));
        assertThat(second.getExternalId(), is(externalId2));
        assertThat(second.getPaymentProvider(), is(paymentProvider2));
        assertThat(second.getServiceName(), is(serviceName2));
        assertThat(second.getDescription(), is(description2));
        assertThat(second.getAnalyticsId(), is(analyticsId2));
        assertThat(second.getType(), is(TYPE));
    }

    @Test
    public void shouldFindGatewayAccounts() {
        testGatewayAccount.insert(testContext.getJdbi());

        List<GatewayAccount> gatewayAccounts;
        GatewayAccount first;
        GatewayAccount second;

        PaymentProvider paymentProvider  = PaymentProvider.GOCARDLESS;
        String serviceName               = "aservice";
        String externalId                = "single";
        String description               = "tests pls";
        String analyticsId               = "DD_234099_TOOLONGWONTREAD";

        PaymentProvider paymentProvider2                   = PaymentProvider.GOCARDLESS;
        String serviceName2                                = "aservice";
        String externalId2                                 = "multiple";
        String description2                                = "tests pls";
        String analyticsId2                                = "DD_234199_TOOLONGWONTREAD";
        PaymentProviderAccessToken accessToken             = PaymentProviderAccessToken.of("gimmeaccess");
        PaymentProviderOrganisationIdentifier organisation = PaymentProviderOrganisationIdentifier.of("organisation");

        GatewayAccountFixture
          .aGatewayAccountFixture()
          .withExternalId(externalId)
          .withServiceName(serviceName)
          .withDescription(description)
          .withPaymentProvider(paymentProvider)
          .withAnalyticsId(analyticsId)
          .insert(testContext.getJdbi());

        GatewayAccountFixture
          .aGatewayAccountFixture()
          .withExternalId(externalId2)
          .withServiceName(serviceName2)
          .withDescription(description2)
          .withPaymentProvider(paymentProvider2)
          .withAnalyticsId(analyticsId2)
          .withAccessToken(accessToken)
          .withOrganisation(organisation)          
          .insert(testContext.getJdbi());

        gatewayAccounts = gatewayAccountDao.find(
          Arrays.asList(externalId)
        );

        assertThat(gatewayAccounts.size(), is(1));

        first = gatewayAccounts.get(0);

        assertThat(first.getId(),              is(notNullValue()));
        assertThat(first.getExternalId(),      is(externalId));
        assertThat(first.getPaymentProvider(), is(paymentProvider));
        assertThat(first.getServiceName(),     is(serviceName));
        assertThat(first.getDescription(),     is(description));
        assertThat(first.getAnalyticsId(),     is(analyticsId));
        assertThat(first.getType(),            is(TYPE));

        gatewayAccounts = gatewayAccountDao.find(
          Arrays.asList(externalId, externalId2)
        );

        assertThat(gatewayAccounts.size(), is(2));

        first = gatewayAccounts.get(0);
        second = gatewayAccounts.get(1);

        assertThat(first.getId(),              is(notNullValue()));
        assertThat(first.getExternalId(),      is(externalId));
        assertThat(first.getPaymentProvider(), is(paymentProvider));
        assertThat(first.getServiceName(),     is(serviceName));
        assertThat(first.getDescription(),     is(description));
        assertThat(first.getAnalyticsId(),     is(analyticsId));
        assertThat(first.getType(),            is(TYPE));

        assertThat(second.getId(), is(notNullValue()));
        assertThat(second.getExternalId(), is(externalId2));
        assertThat(second.getPaymentProvider(), is(paymentProvider2));
        assertThat(second.getServiceName(), is(serviceName2));
        assertThat(second.getDescription(), is(description2));
        assertThat(second.getAnalyticsId(), is(analyticsId2));
        assertThat(second.getType(), is(TYPE));
        assertThat(second.getAccessToken(), is(Optional.of(accessToken)));
        assertThat(second.getOrganisation(), is(Optional.of(organisation)));
    }

    @Test
    public void shouldReturnAnEmptyListIfNoGatewayAccountsExist() {
        List<GatewayAccount> gatewayAccounts = gatewayAccountDao.findAll();
        assertThat(gatewayAccounts.isEmpty(), is(true));
    }

    @Test
    public void shouldInsertAGatewayAccount_withAccessTokenAndOrganisation() {
        testGatewayAccount
                .withAccessToken(PaymentProviderAccessToken.of("an-access-token"))
                .withOrganisation(PaymentProviderOrganisationIdentifier.of("an-organisation"));
        Long id = gatewayAccountDao.insert(testGatewayAccount.toEntity());
        Map<String, Object> foundGatewayAccount =
                testContext.getDatabaseTestHelper().getGatewayAccountById(id);
        assertThat(foundGatewayAccount.get("id"), is(id));
        assertThat(foundGatewayAccount.get("external_id"), is(EXTERNAL_ID));
        assertThat(foundGatewayAccount.get("payment_provider"), is(PAYMENT_PROVIDER.toString()));
        assertThat(foundGatewayAccount.get("service_name"), is(SERVICE_NAME));
        assertThat(foundGatewayAccount.get("analytics_id"), is(ANALYTICS_ID));
        assertThat(foundGatewayAccount.get("type"), is(TYPE.toString()));
        assertThat(foundGatewayAccount.get("description"), is(DESCRIPTION));
        assertThat(foundGatewayAccount.get("access_token"), is("an-access-token"));
        assertThat(foundGatewayAccount.get("organisation"), is("an-organisation"));
    }
    
    @Test
    public void shouldUpdateAccessTokenAndOrganisation() {
        Long id = gatewayAccountDao.insert(testGatewayAccount.toEntity());
        Map<String, Object> foundGatewayAccount = testContext.getDatabaseTestHelper().getGatewayAccountById(id);
        assertThat(foundGatewayAccount.get("access_token"), is(nullValue()));
        assertThat(foundGatewayAccount.get("organisation"), is(nullValue()));
        gatewayAccountDao.updateAccessTokenAndOrganisation(
                                    testGatewayAccount.getExternalId(),
                                    PaymentProviderAccessToken.of("an-access-token"),
                                    PaymentProviderOrganisationIdentifier.of("an-organisation"));
        foundGatewayAccount = testContext.getDatabaseTestHelper().getGatewayAccountById(id);
        assertThat(foundGatewayAccount.get("id"), is(id));
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
