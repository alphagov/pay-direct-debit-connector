package uk.gov.pay.directdebit.payers.dao;

import liquibase.exception.LiquibaseException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import uk.gov.pay.directdebit.DirectDebitConnectorApp;
import uk.gov.pay.directdebit.junit.DropwizardConfig;
import uk.gov.pay.directdebit.junit.DropwizardJUnitRunner;
import uk.gov.pay.directdebit.junit.DropwizardTestContext;
import uk.gov.pay.directdebit.junit.TestContext;
import uk.gov.pay.directdebit.payers.fixtures.GoCardlessCustomerFixture;
import uk.gov.pay.directdebit.payers.fixtures.PayerFixture;
import uk.gov.pay.directdebit.payers.model.GoCardlessCustomer;
import uk.gov.pay.directdebit.payments.fixtures.PaymentRequestFixture;

import java.io.IOException;
import java.util.Map;

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
    private PaymentRequestFixture paymentRequestFixture;
    private PayerFixture payerFixture;

    @Before
    public void setup() throws IOException, LiquibaseException {
        paymentRequestFixture = PaymentRequestFixture.aPaymentRequestFixture().insert(testContext.getJdbi());
        payerFixture = PayerFixture.aPayerFixture().withPaymentRequestId(paymentRequestFixture.getId()).insert(testContext.getJdbi());
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
    public void shouldGetAGoCardlessCustomerById() {
        goCardlessCustomerFixture.insert(testContext.getJdbi());
        GoCardlessCustomer goCardlessCustomer = goCardlessCustomerDao.findById(goCardlessCustomerFixture.getId()).get();
        assertThat(goCardlessCustomer.getId(), is(goCardlessCustomerFixture.getId()));
        assertThat(goCardlessCustomer.getPayerId(), is(payerFixture.getId()));
        assertThat(goCardlessCustomer.getCustomerId(), is(CUSTOMER_ID));
        assertThat(goCardlessCustomer.getCustomerBankAccountId(), is(CUSTOMER_BANK_ACCOUNT_ID));
    }

    @Test
    public void shouldNotFindAPayerById_ifIdIsInvalid() {
        assertThat(goCardlessCustomerDao.findById(9812L).isPresent(), is(false));
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
