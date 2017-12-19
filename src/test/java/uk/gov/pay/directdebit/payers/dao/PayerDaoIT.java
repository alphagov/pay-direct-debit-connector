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
import java.time.ZonedDateTime;
import java.util.Map;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static uk.gov.pay.directdebit.payments.fixtures.PaymentRequestFixture.aPaymentRequestFixture;
import static uk.gov.pay.directdebit.util.ZonedDateTimeTimestampMatcher.isDate;

@RunWith(DropwizardJUnitRunner.class)
@DropwizardConfig(app = DirectDebitConnectorApp.class, config = "config/test-it-config.yaml")
public class PayerDaoIT {
    private static final String PAYER_NAME = "payer name";
    private static final String ACCOUNT_NUMBER = "12345678";
    private static final String SORT_CODE = "654321";
    private static final String ACCOUNT_NUMBER_LAST_TWO_DIGITS = "78";
    private static final boolean ACCOUNT_REQUIRES_AUTHORISATION = false;
    private static final String ADDRESS_LINE_1 = "address line 1";
    private static final String ADDRESS_LINE_2 = "address line 2";
    private static final String ADDRESS_CITY = "london";
    private static final String ADDRESS_POSTCODE = "a postcode";
    private static final String ADDRESS_COUNTRY = "italy";
    private static final ZonedDateTime CREATED_DATE = ZonedDateTime.parse("2017-12-30T12:30:40Z[UTC]");
    private static final String EMAIL = "aaa@bb.com";
    private static final String EXTERNAL_ID = "ablijfslkj234";

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
                .withEmail(EMAIL)
                .withExternalId(EXTERNAL_ID)
                .withName(PAYER_NAME)
                .withAccountNumber(ACCOUNT_NUMBER)
                .withSortCode(SORT_CODE)
                .withAccountNumberLastTwoDigits(ACCOUNT_NUMBER_LAST_TWO_DIGITS)
                .withAccountRequiresAuthorisation(ACCOUNT_REQUIRES_AUTHORISATION)
                .withAddressLine1(ADDRESS_LINE_1)
                .withAddressLine2(ADDRESS_LINE_2)
                .withAddressCity(ADDRESS_CITY)
                .withAddressPostcode(ADDRESS_POSTCODE)
                .withAddressCountry(ADDRESS_COUNTRY)
                .withCreatedDate(CREATED_DATE);
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
        assertThat(foundPayer.get("bank_account_number_last_two_digits"), is(ACCOUNT_NUMBER_LAST_TWO_DIGITS));
        assertThat(foundPayer.get("bank_account_requires_authorisation"), is(ACCOUNT_REQUIRES_AUTHORISATION));
        assertThat(foundPayer.get("bank_account_number"), is(ACCOUNT_NUMBER));
        assertThat(foundPayer.get("bank_account_sort_code"), is(SORT_CODE));
        assertThat(foundPayer.get("address_line1"), is(ADDRESS_LINE_1));
        assertThat(foundPayer.get("address_line2"), is(ADDRESS_LINE_2));
        assertThat(foundPayer.get("address_postcode"), is(ADDRESS_POSTCODE));
        assertThat(foundPayer.get("address_city"), is(ADDRESS_CITY));
        assertThat(foundPayer.get("address_country"), is(ADDRESS_COUNTRY));
        assertThat((Timestamp) foundPayer.get("created_date"), isDate(CREATED_DATE));
    }


    @Test
    public void shouldGetAPayerById() {
        testPayer.insert(testContext.getJdbi());
        Payer payer = payerDao.findById(testPayer.getId()).get();
        assertThat(payer.getId(), is(testPayer.getId()));
        assertThat(payer.getPaymentRequestId(), is(testPaymentRequest.getId()));
        assertThat(payer.getExternalId(), is(EXTERNAL_ID));
        assertThat(payer.getName(), is(PAYER_NAME));
        assertThat(payer.getEmail(), is(EMAIL));
        assertThat(payer.getAccountNumberLastTwoDigits(), is(ACCOUNT_NUMBER_LAST_TWO_DIGITS));
        assertThat(payer.getAccountRequiresAuthorisation(), is(ACCOUNT_REQUIRES_AUTHORISATION));
        assertThat(payer.getAccountNumber(), is(ACCOUNT_NUMBER));
        assertThat(payer.getSortCode(), is(SORT_CODE));
        assertThat(payer.getAddressLine1(), is(ADDRESS_LINE_1));
        assertThat(payer.getAddressLine2(), is(ADDRESS_LINE_2));
        assertThat(payer.getAddressPostcode(), is(ADDRESS_POSTCODE));
        assertThat(payer.getAddressCity(), is(ADDRESS_CITY));
        assertThat(payer.getAddressCountry(), is(ADDRESS_COUNTRY));
        assertThat(payer.getCreatedDate(), is(CREATED_DATE));
    }

    @Test
    public void shouldNotFindAPayerById_ifIdIsInvalid() {
        assertThat(payerDao.findById(9876512L).isPresent(), is(false));
    }

    @Test
    public void shouldGetAPayerByExternalId() {
        testPayer.insert(testContext.getJdbi());
        Payer payer = payerDao.findByExternalId(testPayer.getExternalId()).get();
        assertThat(payer.getId(), is(testPayer.getId()));
        assertThat(payer.getPaymentRequestId(), is(testPaymentRequest.getId()));
        assertThat(payer.getExternalId(), is(EXTERNAL_ID));
        assertThat(payer.getName(), is(PAYER_NAME));
        assertThat(payer.getEmail(), is(EMAIL));
        assertThat(payer.getAccountNumberLastTwoDigits(), is(ACCOUNT_NUMBER_LAST_TWO_DIGITS));
        assertThat(payer.getAccountRequiresAuthorisation(), is(ACCOUNT_REQUIRES_AUTHORISATION));
        assertThat(payer.getAccountNumber(), is(ACCOUNT_NUMBER));
        assertThat(payer.getSortCode(), is(SORT_CODE));
        assertThat(payer.getAddressLine1(), is(ADDRESS_LINE_1));
        assertThat(payer.getAddressLine2(), is(ADDRESS_LINE_2));
        assertThat(payer.getAddressPostcode(), is(ADDRESS_POSTCODE));
        assertThat(payer.getAddressCity(), is(ADDRESS_CITY));
        assertThat(payer.getAddressCountry(), is(ADDRESS_COUNTRY));
        assertThat(payer.getCreatedDate(), is(CREATED_DATE));
    }

    @Test
    public void shouldNotFindAPayerByExternalId_ifIdIsInvalid() {
        assertThat(payerDao.findByExternalId("not-existing").isPresent(), is(false));
    }

    @Test
    public void shouldGetAPayerByPaymentRequestId() {
        testPayer.insert(testContext.getJdbi());
        Payer payer = payerDao.findByPaymentRequestId(testPaymentRequest.getId()).get();
        assertThat(payer.getId(), is(testPayer.getId()));
        assertThat(payer.getPaymentRequestId(), is(testPaymentRequest.getId()));
        assertThat(payer.getExternalId(), is(EXTERNAL_ID));
        assertThat(payer.getName(), is(PAYER_NAME));
        assertThat(payer.getEmail(), is(EMAIL));
        assertThat(payer.getAccountNumberLastTwoDigits(), is(ACCOUNT_NUMBER_LAST_TWO_DIGITS));
        assertThat(payer.getAccountRequiresAuthorisation(), is(ACCOUNT_REQUIRES_AUTHORISATION));
        assertThat(payer.getAccountNumber(), is(ACCOUNT_NUMBER));
        assertThat(payer.getSortCode(), is(SORT_CODE));
        assertThat(payer.getAddressLine1(), is(ADDRESS_LINE_1));
        assertThat(payer.getAddressLine2(), is(ADDRESS_LINE_2));
        assertThat(payer.getAddressPostcode(), is(ADDRESS_POSTCODE));
        assertThat(payer.getAddressCity(), is(ADDRESS_CITY));
        assertThat(payer.getAddressCountry(), is(ADDRESS_COUNTRY));
        assertThat(payer.getCreatedDate(), is(CREATED_DATE));
    }

    @Test
    public void shouldNotFindAPayerByRequestId_ifIdIsInvalid() {
        assertThat(payerDao.findByPaymentRequestId(154L).isPresent(), is(false));
    }
}
