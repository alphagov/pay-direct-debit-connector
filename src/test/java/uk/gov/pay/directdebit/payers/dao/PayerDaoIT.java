package uk.gov.pay.directdebit.payers.dao;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static uk.gov.pay.directdebit.payments.fixtures.PaymentRequestFixture.aPaymentRequestFixture;
import static uk.gov.pay.directdebit.util.ZonedDateTimeTimestampMatcher.isDate;

import java.sql.Timestamp;
import java.time.ZonedDateTime;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import uk.gov.pay.directdebit.DirectDebitConnectorApp;
import uk.gov.pay.directdebit.junit.DropwizardConfig;
import uk.gov.pay.directdebit.junit.DropwizardJUnitRunner;
import uk.gov.pay.directdebit.junit.DropwizardTestContext;
import uk.gov.pay.directdebit.junit.TestContext;
import uk.gov.pay.directdebit.payers.fixtures.PayerFixture;
import uk.gov.pay.directdebit.payers.model.Payer;
import uk.gov.pay.directdebit.payments.fixtures.GatewayAccountFixture;
import uk.gov.pay.directdebit.payments.fixtures.PaymentRequestFixture;

@RunWith(DropwizardJUnitRunner.class)
@DropwizardConfig(app = DirectDebitConnectorApp.class, config = "config/test-it-config.yaml")
public class PayerDaoIT {
    private static final String PAYER_NAME = "payer name";
    private static final String ACCOUNT_NUMBER = "12345678";
    private static final String BANK_NAME = "pay bank";
    private static final String SORT_CODE = "654321";
    private static final String ACCOUNT_NUMBER_LAST_TWO_DIGITS = "78";
    private static final boolean ACCOUNT_REQUIRES_AUTHORISATION = false;
    private static final ZonedDateTime CREATED_DATE = ZonedDateTime.parse("2017-12-30T12:30:40Z");
    private static final String EMAIL = "aaa@bb.test";
    private static final String EXTERNAL_ID = "ablijfslkj234";

    @DropwizardTestContext
    private TestContext testContext;

    private PayerDao payerDao;

    private PaymentRequestFixture testPaymentRequest;
    private PayerFixture testPayer;

    @Before
    public void setup() {
        payerDao = testContext.getJdbi().onDemand(PayerDao.class);
        GatewayAccountFixture gatewayAccountFixture = GatewayAccountFixture.aGatewayAccountFixture()
                .insert(testContext.getJdbi());
        this.testPayer = PayerFixture.aPayerFixture()
                .withEmail(EMAIL)
                .withExternalId(EXTERNAL_ID)
                .withName(PAYER_NAME)
                .withAccountNumber(ACCOUNT_NUMBER)
                .withSortCode(SORT_CODE)
                .withBankName(BANK_NAME)
                .withAccountNumberLastTwoDigits(ACCOUNT_NUMBER_LAST_TWO_DIGITS)
                .withAccountRequiresAuthorisation(ACCOUNT_REQUIRES_AUTHORISATION)
                .withCreatedDate(CREATED_DATE);
        this.testPaymentRequest = aPaymentRequestFixture()
                .withGatewayAccountId(gatewayAccountFixture.getId())
                .withPayerFixture(testPayer)
                .insert(testContext.getJdbi());
    }

    @Test
    public void shouldInsertAPayer() {
        Long id = payerDao.insert(testPayer.toEntity());
        Map<String, Object> foundPayer = testContext.getDatabaseTestHelper().getPayerById(id);
        assertThat(foundPayer.get("id"), is(id));
        assertThat(foundPayer.get("payment_request_id"), is(testPaymentRequest.getId()));
        assertThat(foundPayer.get("external_id"), is(EXTERNAL_ID));
        assertThat(foundPayer.get("name"), is(PAYER_NAME));
        assertThat(foundPayer.get("email"), is(EMAIL));
        assertThat(foundPayer.get("bank_name"), is(BANK_NAME));
        assertThat(foundPayer.get("bank_account_number_last_two_digits"), is(ACCOUNT_NUMBER_LAST_TWO_DIGITS));
        assertThat(foundPayer.get("bank_account_requires_authorisation"), is(ACCOUNT_REQUIRES_AUTHORISATION));
        assertThat(foundPayer.get("bank_account_number"), is(ACCOUNT_NUMBER));
        assertThat(foundPayer.get("bank_account_sort_code"), is(SORT_CODE));
        assertThat((Timestamp) foundPayer.get("created_date"), isDate(CREATED_DATE));
    }


    @Test
    public void shouldGetAPayerById() {
        Payer payer = payerDao.findById(testPayer.getId()).get();
        assertThat(payer.getId(), is(testPayer.getId()));
        assertThat(payer.getPaymentRequestId(), is(testPaymentRequest.getId()));
        assertThat(payer.getExternalId(), is(EXTERNAL_ID));
        assertThat(payer.getName(), is(PAYER_NAME));
        assertThat(payer.getBankName(), is(BANK_NAME));
        assertThat(payer.getEmail(), is(EMAIL));
        assertThat(payer.getAccountNumberLastTwoDigits(), is(ACCOUNT_NUMBER_LAST_TWO_DIGITS));
        assertThat(payer.getAccountRequiresAuthorisation(), is(ACCOUNT_REQUIRES_AUTHORISATION));
        assertThat(payer.getAccountNumber(), is(ACCOUNT_NUMBER));
        assertThat(payer.getSortCode(), is(SORT_CODE));
        assertThat(payer.getCreatedDate(), is(CREATED_DATE));
    }

    @Test
    public void shouldNotFindAPayerById_ifIdIsInvalid() {
        assertThat(payerDao.findById(9876512L).isPresent(), is(false));
    }

    @Test
    public void shouldGetAPayerByExternalId() {
        Payer payer = payerDao.findByExternalId(testPayer.getExternalId()).get();
        assertThat(payer.getId(), is(testPayer.getId()));
        assertThat(payer.getPaymentRequestId(), is(testPaymentRequest.getId()));
        assertThat(payer.getExternalId(), is(EXTERNAL_ID));
        assertThat(payer.getName(), is(PAYER_NAME));
        assertThat(payer.getEmail(), is(EMAIL));
        assertThat(payer.getBankName(), is(BANK_NAME));
        assertThat(payer.getAccountNumberLastTwoDigits(), is(ACCOUNT_NUMBER_LAST_TWO_DIGITS));
        assertThat(payer.getAccountRequiresAuthorisation(), is(ACCOUNT_REQUIRES_AUTHORISATION));
        assertThat(payer.getAccountNumber(), is(ACCOUNT_NUMBER));
        assertThat(payer.getSortCode(), is(SORT_CODE));
        assertThat(payer.getCreatedDate(), is(CREATED_DATE));
    }

    @Test
    public void shouldNotFindAPayerByExternalId_ifIdIsInvalid() {
        assertThat(payerDao.findByExternalId("not-existing").isPresent(), is(false));
    }

    @Test
    public void shouldGetAPayerByPaymentRequestId() {
        Payer payer = payerDao.findByPaymentRequestId(testPaymentRequest.getId()).get();
        assertThat(payer.getId(), is(testPayer.getId()));
        assertThat(payer.getPaymentRequestId(), is(testPaymentRequest.getId()));
        assertThat(payer.getExternalId(), is(EXTERNAL_ID));
        assertThat(payer.getName(), is(PAYER_NAME));
        assertThat(payer.getEmail(), is(EMAIL));
        assertThat(payer.getBankName(), is(BANK_NAME));
        assertThat(payer.getAccountNumberLastTwoDigits(), is(ACCOUNT_NUMBER_LAST_TWO_DIGITS));
        assertThat(payer.getAccountRequiresAuthorisation(), is(ACCOUNT_REQUIRES_AUTHORISATION));
        assertThat(payer.getAccountNumber(), is(ACCOUNT_NUMBER));
        assertThat(payer.getSortCode(), is(SORT_CODE));
        assertThat(payer.getCreatedDate(), is(CREATED_DATE));
    }

    @Test
    public void shouldNotFindAPayerByRequestId_ifIdIsInvalid() {
        assertThat(payerDao.findByPaymentRequestId(154L).isPresent(), is(false));
    }

    @Test
    public void shouldUpdatePayerDetailsAndReturnNumberOfAffectedRows() {
        String newName = "coffee";
        String newAccountNumber = "87654321";
        String newSortCode = "123456";
        String newEmail = "aaa@bbb.test";
        String newBankName = "another bank";
        boolean newRequiresAuthorisation = true;
        Payer newPayerDetails = PayerFixture
                .aPayerFixture()
                .withName(newName)
                .withAccountNumber(newAccountNumber)
                .withSortCode(newSortCode)
                .withEmail(newEmail)
                .withBankName(newBankName)
                .withAccountRequiresAuthorisation(newRequiresAuthorisation)
                .withAccountNumberLastTwoDigits("21")
                .withExternalId("this_should_not_be_updated")
                .withCreatedDate(ZonedDateTime.now())
                .toEntity();
        int numOfUpdatedPayers = payerDao.updatePayerDetails(testPayer.getId(), newPayerDetails);
        Map<String, Object> payerAfterUpdate = testContext.getDatabaseTestHelper().getPayerById(testPayer.getId());

        assertThat(numOfUpdatedPayers, is(1));
        assertThat(payerAfterUpdate.get("name"), is(newName));
        assertThat(payerAfterUpdate.get("email"), is(newEmail));
        assertThat(payerAfterUpdate.get("bank_account_number_last_two_digits"), is("21"));
        assertThat(payerAfterUpdate.get("bank_account_requires_authorisation"), is(newRequiresAuthorisation));
        assertThat(payerAfterUpdate.get("bank_account_number"), is(newAccountNumber));
        assertThat(payerAfterUpdate.get("bank_account_sort_code"), is(newSortCode));
        assertThat(payerAfterUpdate.get("bank_name"), is(newBankName));

        // These properties should not be updated
        assertThat(payerAfterUpdate.get("id"), is(testPayer.getId()));
        assertThat(payerAfterUpdate.get("payment_request_id"), is(testPaymentRequest.getId()));
        assertThat(payerAfterUpdate.get("external_id"), is(EXTERNAL_ID));
        assertThat((Timestamp) payerAfterUpdate.get("created_date"), isDate(CREATED_DATE));

    }

    @Test
    public void shouldNotUpdateAnythingIfPayerDoesNotExist() {
        int numOfUpdatedPayers = payerDao.updatePayerDetails(34L, testPayer.toEntity());
        assertThat(numOfUpdatedPayers, is(0));
    }
}
