package uk.gov.pay.directdebit.payers.dao;

import liquibase.exception.LiquibaseException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import uk.gov.pay.directdebit.DirectDebitConnectorApp;
import uk.gov.pay.directdebit.junit.DropwizardConfig;
import uk.gov.pay.directdebit.junit.DropwizardJUnitRunner;
import uk.gov.pay.directdebit.junit.DropwizardTestContext;
import uk.gov.pay.directdebit.junit.TestContext;
import uk.gov.pay.directdebit.payers.fixtures.PayerFixture;
import uk.gov.pay.directdebit.payers.model.Payer;
import uk.gov.pay.directdebit.payments.fixtures.PaymentRequestFixture;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.Map;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static uk.gov.pay.directdebit.payments.fixtures.PaymentRequestFixture.aPaymentRequestFixture;
import static uk.gov.pay.directdebit.util.ZonedDateTimeTimestampMatcher.isDate;

@RunWith(DropwizardJUnitRunner.class)
@DropwizardConfig(app = DirectDebitConnectorApp.class, config = "config/test-it-config.yaml")
public class PayerDaoIT {
    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @DropwizardTestContext
    private TestContext testContext;

    private PayerDao payerDao;

    private PaymentRequestFixture testPaymentRequest;
    private PayerFixture testPayer;

    @Before
    public void setup() throws IOException, LiquibaseException {
        payerDao = testContext.getJdbi().onDemand(PayerDao.class);
        this.testPaymentRequest = aPaymentRequestFixture()
                .insert(testContext.getJdbi());
        this.testPayer = PayerFixture.aPayerFixture()
                .withPaymentRequestId(testPaymentRequest.getId())
                .insert(testContext.getJdbi());
    }

    @Test
    public void shouldInsertAPayer() {
        Payer aPayer = PayerFixture.aPayerFixture().insert(testContext.getJdbi()).toEntity();
        Long id = payerDao.insert(aPayer);
        Map<String, Object> foundPayer = testContext.getDatabaseTestHelper().getPayerById(id);
        assertThat(foundPayer.get("id"), is(id));
        assertThat(foundPayer.get("payment_request_id"), is(aPayer.getPaymentRequestId()));
        assertThat(foundPayer.get("external_id"), is(aPayer.getExternalId()));
        assertThat(foundPayer.get("name"), is(aPayer.getName()));
        assertThat(foundPayer.get("email"), is(aPayer.getEmail()));
        assertThat(foundPayer.get("bank_account_number_last_two_digits"), is(aPayer.getEmail()));
        assertThat(foundPayer.get("bank_account_requires_authorisation"), is(aPayer.getEmail()));
        assertThat(foundPayer.get("bank_account_number"), is(aPayer.getEmail()));
        assertThat(foundPayer.get("bank_account_sort_code"), is(aPayer.getEmail()));
        assertThat(foundPayer.get("address_line1"), is(aPayer.getEmail()));
        assertThat(foundPayer.get("address_line2"), is(aPayer.getEmail()));
        assertThat(foundPayer.get("address_postcode"), is(aPayer.getEmail()));
        assertThat(foundPayer.get("address_city"), is(aPayer.getEmail()));
        assertThat(foundPayer.get("address_country"), is(aPayer.getEmail()));
        assertThat((Timestamp) foundPayer.get("created_date"), isDate(aPayer.getCreatedDate()));

    }
    @Test
    public void shouldGetAPayerById() {
        Payer payer = payerDao.findById(testPayer.getId()).get();
        assertThat(payer.getId(), is(testPayer.getId()));
        assertThat(payer.getPaymentRequestId(), is(testPayer.getPaymentRequestId()));
        assertThat(payer.getExternalId(), is(testPayer.getExternalId()));
        assertThat(payer.getName(), is(testPayer.getName()));
        assertThat(payer.getEmail(), is(testPayer.getEmail()));
        assertThat(payer.getAccountRequiresAuthorisation(), is(testPayer.getAccountRequiresAuthorisation()));
        assertThat(payer.getAccountNumber(), is(testPayer.getAccountNumber()));
        assertThat(payer.getSortCode(), is(testPayer.getSortCode()));
        assertThat(payer.getAddressLine1(), is(testPayer.getAddressLine1()));
        assertThat(payer.getAddressLine2(), is(testPayer.getAddressLine2()));
        assertThat(payer.getAddressPostcode(), is(testPayer.getAddressPostcode()));
        assertThat(payer.getAddressCity(), is(testPayer.getAddressCity()));
        assertThat(payer.getAddressCountry(), is(testPayer.getAddressCountry()));
        assertThat(payer.getCreatedDate(), is(testPayer.getCreatedDate()));
    }
    @Test
    public void shouldGetAPayerByExternalId() {
        Payer payer = payerDao.findByExternalId(testPayer.getExternalId()).get();
        assertThat(payer.getId(), is(testPayer.getId()));
        assertThat(payer.getPaymentRequestId(), is(testPayer.getPaymentRequestId()));
        assertThat(payer.getExternalId(), is(testPayer.getExternalId()));
        assertThat(payer.getName(), is(testPayer.getName()));
        assertThat(payer.getEmail(), is(testPayer.getEmail()));
        assertThat(payer.getAccountRequiresAuthorisation(), is(testPayer.getAccountRequiresAuthorisation()));
        assertThat(payer.getAccountNumber(), is(testPayer.getAccountNumber()));
        assertThat(payer.getSortCode(), is(testPayer.getSortCode()));
        assertThat(payer.getAddressLine1(), is(testPayer.getAddressLine1()));
        assertThat(payer.getAddressLine2(), is(testPayer.getAddressLine2()));
        assertThat(payer.getAddressPostcode(), is(testPayer.getAddressPostcode()));
        assertThat(payer.getAddressCity(), is(testPayer.getAddressCity()));
        assertThat(payer.getAddressCountry(), is(testPayer.getAddressCountry()));
        assertThat(payer.getCreatedDate(), is(testPayer.getCreatedDate()));
    }
    @Test
    public void shouldGetAPayerByPaymentRequestId() {
        Payer payer = payerDao.findByPaymentRequestId(testPaymentRequest.getId()).get();
        assertThat(payer.getId(), is(testPayer.getId()));
        assertThat(payer.getPaymentRequestId(), is(testPayer.getPaymentRequestId()));
        assertThat(payer.getExternalId(), is(testPayer.getExternalId()));
        assertThat(payer.getName(), is(testPayer.getName()));
        assertThat(payer.getEmail(), is(testPayer.getEmail()));
        assertThat(payer.getAccountRequiresAuthorisation(), is(testPayer.getAccountRequiresAuthorisation()));
        assertThat(payer.getAccountNumber(), is(testPayer.getAccountNumber()));
        assertThat(payer.getSortCode(), is(testPayer.getSortCode()));
        assertThat(payer.getAddressLine1(), is(testPayer.getAddressLine1()));
        assertThat(payer.getAddressLine2(), is(testPayer.getAddressLine2()));
        assertThat(payer.getAddressPostcode(), is(testPayer.getAddressPostcode()));
        assertThat(payer.getAddressCity(), is(testPayer.getAddressCity()));
        assertThat(payer.getAddressCountry(), is(testPayer.getAddressCountry()));
        assertThat(payer.getCreatedDate(), is(testPayer.getCreatedDate()));
    }
}
