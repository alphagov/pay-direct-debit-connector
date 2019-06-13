package uk.gov.pay.directdebit.payers.dao;

import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import uk.gov.pay.directdebit.DirectDebitConnectorApp;
import uk.gov.pay.directdebit.junit.DropwizardConfig;
import uk.gov.pay.directdebit.junit.DropwizardJUnitRunner;
import uk.gov.pay.directdebit.junit.DropwizardTestContext;
import uk.gov.pay.directdebit.junit.TestContext;
import uk.gov.pay.directdebit.mandate.fixtures.MandateFixture;
import uk.gov.pay.directdebit.payers.fixtures.GoCardlessCustomerFixture;
import uk.gov.pay.directdebit.payers.fixtures.PayerFixture;
import uk.gov.pay.directdebit.payments.fixtures.GatewayAccountFixture;
import uk.gov.pay.directdebit.payments.fixtures.PaymentFixture;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static uk.gov.pay.directdebit.payers.fixtures.GoCardlessCustomerFixture.aGoCardlessCustomerFixture;

@RunWith(DropwizardJUnitRunner.class)
@DropwizardConfig(app = DirectDebitConnectorApp.class, config = "config/test-it-config.yaml")
public class GoCardlessCustomerDaoIT {
    private static final String CUSTOMER_ID = "CA2982HHJ9000GG";
    private static final String CUSTOMER_BANK_ACCOUNT_ID = "BA938902CXGG";

    @DropwizardTestContext
    private TestContext testContext;

    private GoCardlessCustomerDao goCardlessCustomerDao;

    private GoCardlessCustomerFixture goCardlessCustomerFixture;
    private PaymentFixture paymentFixture;
    private PayerFixture payerFixture;
    private MandateFixture mandateFixture;
    private GatewayAccountFixture gatewayAccountFixture;

    @Before
    public void setup() {
        gatewayAccountFixture = GatewayAccountFixture.aGatewayAccountFixture().insert(testContext.getJdbi());
        mandateFixture = MandateFixture.aMandateFixture().withGatewayAccountFixture(gatewayAccountFixture).insert(testContext.getJdbi());
        paymentFixture = PaymentFixture.aPaymentFixture()
                .withMandateFixture(mandateFixture)
                .insert(testContext.getJdbi());
        payerFixture = PayerFixture.aPayerFixture().withMandateId(mandateFixture.getId()).insert(testContext.getJdbi());
        goCardlessCustomerDao = testContext.getJdbi().onDemand(GoCardlessCustomerDao.class);
        goCardlessCustomerFixture = aGoCardlessCustomerFixture()
                .withPayerId(payerFixture.getId())
                .withCustomerId(CUSTOMER_ID)
                .withCustomerBankAccountId(CUSTOMER_BANK_ACCOUNT_ID);
    }

    @Test
    public void shouldInsertAGoCardlessCustomer() {
        Long id = goCardlessCustomerDao.insert(goCardlessCustomerFixture.toEntity());
        Map<String, Object> foundGoCardlessCustomer = testContext.getDatabaseTestHelper().getGoCardlessCustomerById(id);
        assertThat(foundGoCardlessCustomer.get("id"), is(id));
        assertThat(foundGoCardlessCustomer.get("payer_id"), is(payerFixture.getId()));
        assertThat(foundGoCardlessCustomer.get("customer_id"), is(CUSTOMER_ID));
        assertThat(foundGoCardlessCustomer.get("customer_bank_account_id"), is(CUSTOMER_BANK_ACCOUNT_ID));
    }

    @Test
    public void shouldUpdateAGoCardlessCustomerWithBankAccountId() {
        goCardlessCustomerFixture.insert(testContext.getJdbi());
        int affectedRows = goCardlessCustomerDao.updateBankAccountId(goCardlessCustomerFixture.getId(), "newBankAccountId");
        Map<String, Object> goCardlessCustomerAfterUpdate = testContext.getDatabaseTestHelper().getGoCardlessCustomerById(goCardlessCustomerFixture.getId());

        assertThat(affectedRows, is(1));
        assertThat(goCardlessCustomerAfterUpdate.get("id"), is(goCardlessCustomerFixture.getId()));
        assertThat(goCardlessCustomerAfterUpdate.get("payer_id"), is(payerFixture.getId()));
        assertThat(goCardlessCustomerAfterUpdate.get("customer_id"), is(CUSTOMER_ID));
        assertThat(goCardlessCustomerAfterUpdate.get("customer_bank_account_id"), is("newBankAccountId"));
    }
}
