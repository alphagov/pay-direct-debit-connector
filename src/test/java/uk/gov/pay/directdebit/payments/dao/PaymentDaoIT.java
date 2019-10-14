package uk.gov.pay.directdebit.payments.dao;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import uk.gov.pay.directdebit.DirectDebitConnectorApp;
import uk.gov.pay.directdebit.gatewayaccounts.model.GoCardlessOrganisationId;
import uk.gov.pay.directdebit.gatewayaccounts.model.PaymentProvider;
import uk.gov.pay.directdebit.junit.DropwizardConfig;
import uk.gov.pay.directdebit.junit.DropwizardJUnitRunner;
import uk.gov.pay.directdebit.junit.DropwizardTestContext;
import uk.gov.pay.directdebit.junit.TestContext;
import uk.gov.pay.directdebit.mandate.fixtures.MandateFixture;
import uk.gov.pay.directdebit.payments.fixtures.GatewayAccountFixture;
import uk.gov.pay.directdebit.payments.fixtures.PaymentFixture;
import uk.gov.pay.directdebit.payments.model.GoCardlessPaymentId;
import uk.gov.pay.directdebit.payments.model.Payment;
import uk.gov.pay.directdebit.payments.model.PaymentState;
import uk.gov.pay.directdebit.payments.model.SandboxPaymentId;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static java.time.Month.JULY;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static uk.gov.pay.directdebit.gatewayaccounts.model.PaymentProvider.GOCARDLESS;
import static uk.gov.pay.directdebit.gatewayaccounts.model.PaymentProvider.SANDBOX;
import static uk.gov.pay.directdebit.payments.fixtures.GatewayAccountFixture.aGatewayAccountFixture;
import static uk.gov.pay.directdebit.payments.fixtures.PaymentFixture.aPaymentFixture;
import static uk.gov.pay.directdebit.payments.model.Payment.PaymentBuilder.fromPayment;
import static uk.gov.pay.directdebit.payments.model.PaymentState.CREATED;
import static uk.gov.pay.directdebit.util.NumberMatcher.isNumber;
import static uk.gov.pay.directdebit.util.ZonedDateTimeTimestampMatcher.isDate;

@RunWith(DropwizardJUnitRunner.class)
@DropwizardConfig(app = DirectDebitConnectorApp.class, config = "config/test-it-config.yaml")
public class PaymentDaoIT {

    private static final PaymentState STATE = CREATED;
    private static final long AMOUNT = 10L;

    @DropwizardTestContext
    private TestContext testContext;
    private PaymentDao paymentDao;

    private GatewayAccountFixture testGatewayAccount;
    private PaymentFixture testPayment;
    private MandateFixture testMandate;

    @Before
    public void setup() {
        paymentDao = testContext.getJdbi().onDemand(PaymentDao.class);
        testGatewayAccount = aGatewayAccountFixture().insert(testContext.getJdbi());
        testMandate = MandateFixture.aMandateFixture().withGatewayAccountFixture(testGatewayAccount).withStateDetails("state details").insert(testContext.getJdbi());
        testPayment = generateNewPaymentFixture(testMandate, STATE, AMOUNT);
    }

    @Test
    public void shouldInsertAPayment() {
        LocalDate chargeDate = LocalDate.of(1969, 7, 16);

        Payment payment = testPayment
                .withPaymentProviderId(SandboxPaymentId.valueOf("expectedPaymentProviderId"))
                .withChargeDate(chargeDate)
                .toEntity();
        Long id = paymentDao.insert(payment);

        Map<String, Object> foundPayment = testContext.getDatabaseTestHelper().getPaymentById(id);
        assertThat(foundPayment.get("id"), is(id));
        assertThat(foundPayment.get("mandate_id"), is(testMandate.getId()));
        assertThat((Long) foundPayment.get("amount"), isNumber(AMOUNT));
        assertThat(PaymentState.valueOf((String) foundPayment.get("state")), is(STATE));
        assertThat(foundPayment.get("payment_provider_id"), is("expectedPaymentProviderId"));
        assertThat(((Date) foundPayment.get("charge_date")).toLocalDate(), is(chargeDate));
    }

    @Test
    public void shouldInsertAPaymentWithoutProviderId() {
        Payment payment = testPayment
                .withPaymentProviderId(null)
                .toEntity();
        Long id = paymentDao.insert(payment);

        Map<String, Object> foundPayment = testContext.getDatabaseTestHelper().getPaymentById(id);
        assertThat(foundPayment.get("id"), is(id));
        assertThat(foundPayment.get("mandate_id"), is(testMandate.getId()));
        assertThat((Long) foundPayment.get("amount"), isNumber(AMOUNT));
        assertThat(PaymentState.valueOf((String) foundPayment.get("state")), is(STATE));
        assertThat(foundPayment.get("payment_provider_id"), is(nullValue()));
    }

    @Test
    public void shouldGetAPaymentById() {
        SandboxPaymentId providerId = SandboxPaymentId.valueOf("expectedPaymentProviderId");
        LocalDate chargeDate = LocalDate.of(1969, 7, 16);

        testPayment
                .withPaymentProviderId(providerId)
                .withChargeDate(chargeDate)
                .insert(testContext.getJdbi());

        Payment payment = paymentDao.findById(testPayment.getId()).get();

        assertThat(payment.getId(), is(testPayment.getId()));
        assertThat(payment.getMandate(), is(testMandate.toEntity()));
        assertThat(payment.getExternalId(), is(testPayment.getExternalId()));
        assertThat(payment.getDescription(), is(testPayment.getDescription()));
        assertThat(payment.getReference(), is(testPayment.getReference()));
        assertThat(payment.getAmount(), is(AMOUNT));
        assertThat(payment.getState(), is(STATE));
        assertThat(payment.getCreatedDate(), is(testPayment.getCreatedDate()));
        assertThat(payment.getProviderId().get(), is(providerId));
        assertThat(payment.getChargeDate().get(), is(chargeDate));
    }

    @Test
    public void shouldGetAPaymentByExternalId() {
        testPayment.insert(testContext.getJdbi());
        Payment payment = paymentDao.findByExternalId(testPayment.getExternalId()).get();
        assertThat(payment.getId(), is(testPayment.getId()));
        assertThat(payment.getMandate(), is(testMandate.toEntity()));
        assertThat(payment.getExternalId(), is(testPayment.getExternalId()));
        assertThat(payment.getDescription(), is(testPayment.getDescription()));
        assertThat(payment.getReference(), is(testPayment.getReference()));
        assertThat(payment.getAmount(), is(AMOUNT));
        assertThat(payment.getState(), is(STATE));
        assertThat(payment.getCreatedDate(), is(testPayment.getCreatedDate()));
    }

    @Test
    public void shouldGetAPaymentByProviderIdAndOrganisationId() {
        var goCardlessOrganisationId = GoCardlessOrganisationId.valueOf("orgId");
        var expectedProviderId = GoCardlessPaymentId.valueOf("aProviderId");

        testGatewayAccount = aGatewayAccountFixture()
                .withPaymentProvider(GOCARDLESS)
                .withOrganisation(goCardlessOrganisationId)
                .insert(testContext.getJdbi());

        testMandate = MandateFixture.aMandateFixture()
                .withGatewayAccountFixture(testGatewayAccount)
                .insert(testContext.getJdbi());

        testPayment
                .withMandateFixture(testMandate)
                .withPaymentProviderId(expectedProviderId)
                .insert(testContext.getJdbi());

        Payment payment = paymentDao.findPaymentByProviderIdAndOrganisationId(GOCARDLESS, expectedProviderId, goCardlessOrganisationId).get();

        assertThat(payment.getId(), is(testPayment.getId()));
        assertThat(payment.getMandate(), is(testMandate.toEntity()));
        assertThat(payment.getExternalId(), is(testPayment.getExternalId()));
        assertThat(payment.getProviderId().get(), is(expectedProviderId));
        assertThat(payment.getDescription(), is(testPayment.getDescription()));
        assertThat(payment.getReference(), is(testPayment.getReference()));
        assertThat(payment.getAmount(), is(AMOUNT));
        assertThat(payment.getState(), is(STATE));
        assertThat(payment.getCreatedDate(), is(testPayment.getCreatedDate()));
    }

    @Test
    public void shouldReturnEmptyWhenProviderIdMatchesButPaymentProviderServiceIdDoesNotMatch() {
        var goCardlessOrganisationId = GoCardlessOrganisationId.valueOf("orgId");
        var expectedProviderId = GoCardlessPaymentId.valueOf("aProviderId");

        testGatewayAccount = aGatewayAccountFixture()
                .withPaymentProvider(GOCARDLESS)
                .withOrganisation(goCardlessOrganisationId)
                .insert(testContext.getJdbi());

        testMandate = MandateFixture.aMandateFixture()
                .withGatewayAccountFixture(testGatewayAccount)
                .insert(testContext.getJdbi());

        testPayment
                .withMandateFixture(testMandate)
                .withPaymentProviderId(expectedProviderId)
                .insert(testContext.getJdbi());

        Optional<Payment> payment = paymentDao.findPaymentByProviderIdAndOrganisationId(GOCARDLESS, expectedProviderId, GoCardlessOrganisationId.valueOf("differentOrg"));

        assertThat(payment, is(Optional.empty()));
    }

    @Test
    public void shouldReturnEmptyWhenProviderIdMatchesButProviderDoesNotMatch() {
        var goCardlessOrganisationId = GoCardlessOrganisationId.valueOf("orgId");
        var expectedProviderId = GoCardlessPaymentId.valueOf("aProviderId");

        testGatewayAccount = aGatewayAccountFixture()
                .withPaymentProvider(GOCARDLESS)
                .withOrganisation(goCardlessOrganisationId)
                .insert(testContext.getJdbi());

        testMandate = MandateFixture.aMandateFixture()
                .withGatewayAccountFixture(testGatewayAccount)
                .insert(testContext.getJdbi());

        testPayment
                .withMandateFixture(testMandate)
                .withPaymentProviderId(expectedProviderId)
                .insert(testContext.getJdbi());

        Optional<Payment> payment = paymentDao.findPaymentByProviderIdAndOrganisationId(SANDBOX, expectedProviderId, goCardlessOrganisationId);

        assertThat(payment, is(Optional.empty()));
    }

    @Test
    public void shouldGetAPaymentByProviderIdAndNoOrganisationId() {
        var expectedProviderId = SandboxPaymentId.valueOf("aProviderId");

        testGatewayAccount = aGatewayAccountFixture()
                .withPaymentProvider(SANDBOX)
                .withOrganisation(null)
                .insert(testContext.getJdbi());

        testMandate = MandateFixture.aMandateFixture()
                .withGatewayAccountFixture(testGatewayAccount)
                .insert(testContext.getJdbi());

        testPayment
                .withMandateFixture(testMandate)
                .withPaymentProviderId(expectedProviderId)
                .insert(testContext.getJdbi());

        Payment payment = paymentDao.findPaymentByProviderId(SANDBOX, expectedProviderId).get();

        assertThat(payment.getId(), is(testPayment.getId()));
        assertThat(payment.getExternalId(), is(testPayment.getExternalId()));
        assertThat(payment.getProviderId().get(), is(expectedProviderId));
    }
    
    @Test
    public void shouldNotGetAPaymentByProviderIdAndNoOrganisationIdIfPaymentProviderIdDoesNotMatch() {
        var expectedProviderId = SandboxPaymentId.valueOf("aProviderId");

        testGatewayAccount = aGatewayAccountFixture()
                .withPaymentProvider(SANDBOX)
                .withOrganisation(null)
                .insert(testContext.getJdbi());

        testMandate = MandateFixture.aMandateFixture()
                .withGatewayAccountFixture(testGatewayAccount)
                .insert(testContext.getJdbi());

        testPayment
                .withMandateFixture(testMandate)
                .withPaymentProviderId(expectedProviderId)
                .insert(testContext.getJdbi());

        Optional<Payment> payment = paymentDao.findPaymentByProviderId(SANDBOX, SandboxPaymentId.valueOf("differentPaymentId"));

        assertThat(payment, is(Optional.empty()));
    }

    @Test
    public void shouldNotGetAPaymentByProviderIdAndNoOrganisationIdIfProviderDoesNotMatch() {
        var expectedProviderId = SandboxPaymentId.valueOf("aProviderId");

        testGatewayAccount = aGatewayAccountFixture()
                .withPaymentProvider(SANDBOX)
                .withOrganisation(null)
                .insert(testContext.getJdbi());

        testMandate = MandateFixture.aMandateFixture()
                .withGatewayAccountFixture(testGatewayAccount)
                .insert(testContext.getJdbi());

        testPayment
                .withMandateFixture(testMandate)
                .withPaymentProviderId(expectedProviderId)
                .insert(testContext.getJdbi());

        Optional<Payment> payment = paymentDao.findPaymentByProviderId(GOCARDLESS, expectedProviderId);

        assertThat(payment, is(Optional.empty()));
    }

    @Test
    public void shouldNotGetAPaymentByProviderIdAndNoOrganisationIdIfOrganisationIsNotNull() {
        var expectedProviderId = SandboxPaymentId.valueOf("aProviderId");

        testGatewayAccount = aGatewayAccountFixture()
                .withPaymentProvider(SANDBOX)
                .withOrganisation(GoCardlessOrganisationId.valueOf("organisationId"))
                .insert(testContext.getJdbi());

        testMandate = MandateFixture.aMandateFixture()
                .withGatewayAccountFixture(testGatewayAccount)
                .insert(testContext.getJdbi());

        testPayment
                .withMandateFixture(testMandate)
                .withPaymentProviderId(expectedProviderId)
                .insert(testContext.getJdbi());

        Optional<Payment> payment = paymentDao.findPaymentByProviderId(SANDBOX, expectedProviderId);

        assertThat(payment, is(Optional.empty()));
    }
    
    @Test
    public void shouldFindAllPaymentsByPaymentStateAndProvider() {
        GatewayAccountFixture goCardlessGatewayAccount = aGatewayAccountFixture().withPaymentProvider(GOCARDLESS).insert(testContext.getJdbi());
        GatewayAccountFixture sandboxGatewayAccount = aGatewayAccountFixture().withPaymentProvider(SANDBOX).insert(testContext.getJdbi());

        MandateFixture sandboxMandate = MandateFixture.aMandateFixture().withGatewayAccountFixture(sandboxGatewayAccount).insert(testContext.getJdbi());
        MandateFixture goCardlessMandate = MandateFixture.aMandateFixture().withGatewayAccountFixture(goCardlessGatewayAccount).insert(testContext.getJdbi());
        PaymentFixture sandboxCharge =
                generateNewPaymentFixture(sandboxMandate, CREATED, AMOUNT);
        
        generateNewPaymentFixture(goCardlessMandate, CREATED, AMOUNT);
        sandboxCharge.insert(testContext.getJdbi());

        PaymentFixture successSandboxCharge =
                generateNewPaymentFixture(sandboxMandate, PaymentState.PAID_OUT, AMOUNT);
        successSandboxCharge.insert(testContext.getJdbi());

        PaymentFixture goCardlessSuccessCharge =
                generateNewPaymentFixture(goCardlessMandate, PaymentState.PAID_OUT, AMOUNT);
        goCardlessSuccessCharge.insert(testContext.getJdbi());

        List<Payment> successPaymentsList = paymentDao.findAllByPaymentStateAndProvider(PaymentState.PAID_OUT, SANDBOX);
        assertThat(successPaymentsList.size(), is(1));
        assertThat(successPaymentsList.get(0).getState(), is(PaymentState.PAID_OUT));
        assertThat(successPaymentsList.get(0).getMandate().getGatewayAccount().getPaymentProvider(), is(SANDBOX));
    }

    @Test
    public void shouldNotFindAnyPaymentByPaymentState_ifPaymentStateIsNotUsed() {
        PaymentFixture processingDirectDebitPaymentStatePaymentFixture =
                generateNewPaymentFixture(testMandate, CREATED, AMOUNT);
        processingDirectDebitPaymentStatePaymentFixture.insert(testContext.getJdbi());

        List<Payment> successPaymentsList = paymentDao.findAllByPaymentStateAndProvider(PaymentState.PAID_OUT, SANDBOX);
        assertThat(successPaymentsList.size(), is(0));
    }

    @Test
    public void shouldUpdateStateAndReturnNumberOfAffectedRows() {
        PaymentState newState = CREATED;
        testPayment.insert(testContext.getJdbi());
        int numOfUpdatedPayments = paymentDao.updateState(testPayment.getId(), newState);
        Map<String, Object> transactionAfterUpdate = testContext.getDatabaseTestHelper().getPaymentById(testPayment.getId());
        assertThat(numOfUpdatedPayments, is(1));
        assertThat(transactionAfterUpdate.get("id"), is(testPayment.getId()));
        assertThat(transactionAfterUpdate.get("external_id"), is(testPayment.getExternalId()));
        assertThat(transactionAfterUpdate.get("mandate_id"), is(testMandate.getId()));
        assertThat(transactionAfterUpdate.get("description"), is(testPayment.getDescription()));
        assertThat(transactionAfterUpdate.get("reference"), is(testPayment.getReference()));
        assertThat(transactionAfterUpdate.get("amount"), is(AMOUNT));
        assertThat(transactionAfterUpdate.get("state"), is(newState.toString()));
        assertThat((Timestamp) transactionAfterUpdate.get("created_date"), isDate(testPayment.getCreatedDate()));
    }

    @Test
    public void shouldUpdateProviderIdAndChargeDateAndReturnNumberOfAffectedRows() {
        LocalDate chargeDate = LocalDate.of(1969, JULY, 16);
        SandboxPaymentId providerPaymentId = SandboxPaymentId.valueOf("expectedProviderId");
        Payment originalPayment = testPayment
                .withPaymentProviderId(null)
                .withChargeDate(null)
                .insert(testContext.getJdbi())
                .toEntity();

        Payment paymentWithProviderIdAndChargeDate = fromPayment(originalPayment)
                .withChargeDate(chargeDate)
                .withProviderId(providerPaymentId).build();

        int numOfUpdatedPayments = paymentDao.updateProviderIdAndChargeDate(paymentWithProviderIdAndChargeDate);
        
        Map<String, Object> paymentAfterUpdate = testContext.getDatabaseTestHelper().getPaymentById(originalPayment.getId());
        assertThat(numOfUpdatedPayments, is(1));
        assertThat(paymentAfterUpdate.get("id"), is(originalPayment.getId()));
        assertThat(paymentAfterUpdate.get("external_id"), is(originalPayment.getExternalId()));
        assertThat(paymentAfterUpdate.get("mandate_id"), is(originalPayment.getMandate().getId()));
        assertThat(paymentAfterUpdate.get("description"), is(originalPayment.getDescription()));
        assertThat(paymentAfterUpdate.get("reference"), is(originalPayment.getReference()));
        assertThat(paymentAfterUpdate.get("amount"), is(originalPayment.getAmount()));
        assertThat(paymentAfterUpdate.get("state"), is(originalPayment.getState().toString()));
        assertThat((Timestamp) paymentAfterUpdate.get("created_date"), isDate(originalPayment.getCreatedDate()));
        assertThat(((Date) paymentAfterUpdate.get("charge_date")).toLocalDate(), is(chargeDate));
        assertThat(paymentAfterUpdate.get("payment_provider_id"), is(providerPaymentId.toString()));
    }

    @Test
    public void shouldNotUpdateAnythingIfPaymentDoesNotExist() {
        int numOfUpdatedPayments = paymentDao.updateState(34L, CREATED);
        assertThat(numOfUpdatedPayments, is(0));
    }
    
    @Test
    public void findAllPaymentsBySetOfStatesAndCreationTime_shouldFindThreePayments() {
        aPaymentFixture().withMandateFixture(testMandate).withState(CREATED)
                .withCreatedDate(ZonedDateTime.now().minusMinutes(91L)).insert(testContext.getJdbi());
        
        aPaymentFixture().withMandateFixture(testMandate).withState(CREATED)
                .withCreatedDate(ZonedDateTime.now().minusMinutes(91L)).insert(testContext.getJdbi());
        
        aPaymentFixture().withMandateFixture(testMandate).withState(CREATED)
                .withCreatedDate(ZonedDateTime.now().minusMinutes(91L)).insert(testContext.getJdbi());
        
        Set<PaymentState> states = Set.of(CREATED);
        List<Payment> payments = paymentDao.findAllPaymentsBySetOfStatesAndCreationTime(states, ZonedDateTime.now().minusMinutes(90L));
        assertThat(payments.size(), is(3));
    }

    @Test
    public void shouldUpdateStateByProviderIdAndOrganisationAndReturnNumberOfAffectedRows() {
        GatewayAccountFixture goCardlessGatewayAccountFixture = aGatewayAccountFixture()
                .withPaymentProvider(GOCARDLESS)
                .withOrganisation(GoCardlessOrganisationId.valueOf("Organisation ID we want"))
                .insert(testContext.getJdbi());

        GatewayAccountFixture goCardlessGatewayAccountFixtureWithWrongOrganisation = aGatewayAccountFixture()
                .withPaymentProvider(GOCARDLESS)
                .withOrganisation(GoCardlessOrganisationId.valueOf("Different organisation"))
                .insert(testContext.getJdbi());

        MandateFixture mandateFixture = MandateFixture.aMandateFixture()
                .withGatewayAccountFixture(goCardlessGatewayAccountFixture)
                .insert(testContext.getJdbi());

        MandateFixture mandateFixtureWithWrongOrganisation = MandateFixture.aMandateFixture()
                .withGatewayAccountFixture(goCardlessGatewayAccountFixtureWithWrongOrganisation)
                .insert(testContext.getJdbi());

        aPaymentFixture()
                .withMandateFixture(mandateFixture)
                .withExternalId("Payment we want")
                .withPaymentProviderId(GoCardlessPaymentId.valueOf("Payment ID we want"))
                .insert(testContext.getJdbi());

        aPaymentFixture()
                .withMandateFixture(mandateFixture)
                .withPaymentProviderId(GoCardlessPaymentId.valueOf("Different payment ID"))
                .insert(testContext.getJdbi());

        aPaymentFixture()
                .withMandateFixture(mandateFixtureWithWrongOrganisation)
                .withPaymentProviderId(GoCardlessPaymentId.valueOf("Payment ID we want"))
                .insert(testContext.getJdbi());

        int numOfUpdatedPayments = paymentDao.updateStateByProviderIdAndOrganisationId(GOCARDLESS, GoCardlessOrganisationId.valueOf("Organisation ID we want"),
                GoCardlessPaymentId.valueOf("Payment ID we want"), PaymentState.SUBMITTED_TO_PROVIDER, "state details", "state details description");

        assertThat(numOfUpdatedPayments, is(1));

        Payment payment = paymentDao.findByExternalId("Payment we want").get();
        assertThat(payment.getState(), is(PaymentState.SUBMITTED_TO_PROVIDER));
        assertThat(payment.getStateDetails(), is(Optional.of("state details")));
        assertThat(payment.getStateDetailsDescription(), is(Optional.of("state details description")));
    }

    @Test
    public void shouldUpdateStateByProviderIdAndOrganisationWithNoDetailsAndDescriptionAndReturnNumberOfAffectedRows() {
        GatewayAccountFixture goCardlessGatewayAccountFixture = aGatewayAccountFixture()
                .withPaymentProvider(GOCARDLESS)
                .withOrganisation(GoCardlessOrganisationId.valueOf("Organisation ID we want"))
                .insert(testContext.getJdbi());

        GatewayAccountFixture goCardlessGatewayAccountFixtureWithWrongOrganisation = aGatewayAccountFixture()
                .withPaymentProvider(GOCARDLESS)
                .withOrganisation(GoCardlessOrganisationId.valueOf("Different organisation"))
                .insert(testContext.getJdbi());

        MandateFixture mandateFixture = MandateFixture.aMandateFixture()
                .withGatewayAccountFixture(goCardlessGatewayAccountFixture)
                .insert(testContext.getJdbi());

        MandateFixture mandateFixtureWithWrongOrganisation = MandateFixture.aMandateFixture()
                .withGatewayAccountFixture(goCardlessGatewayAccountFixtureWithWrongOrganisation)
                .insert(testContext.getJdbi());

        aPaymentFixture()
                .withMandateFixture(mandateFixture)
                .withExternalId("Payment we want")
                .withStateDetails("state details before update")
                .withStateDetailsDescription("state details description before update")
                .withPaymentProviderId(GoCardlessPaymentId.valueOf("Payment ID we want"))
                .insert(testContext.getJdbi());

        aPaymentFixture()
                .withMandateFixture(mandateFixture)
                .withPaymentProviderId(GoCardlessPaymentId.valueOf("Different payment ID"))
                .insert(testContext.getJdbi());

        aPaymentFixture()
                .withMandateFixture(mandateFixtureWithWrongOrganisation)
                .withPaymentProviderId(GoCardlessPaymentId.valueOf("Payment ID we want"))
                .insert(testContext.getJdbi());

        int numOfUpdatedPayments = paymentDao.updateStateByProviderIdAndOrganisationId(GOCARDLESS, GoCardlessOrganisationId.valueOf("Organisation ID we want"),
                GoCardlessPaymentId.valueOf("Payment ID we want"), PaymentState.SUBMITTED_TO_PROVIDER, null, null);

        assertThat(numOfUpdatedPayments, is(1));

        Payment payment = paymentDao.findByExternalId("Payment we want").get();
        assertThat(payment.getState(), is(PaymentState.SUBMITTED_TO_PROVIDER));
        assertThat(payment.getStateDetails(), is(Optional.empty()));
        assertThat(payment.getStateDetailsDescription(), is(Optional.empty()));
    }
    
    @Test
    public void shouldUpdateStateByProviderIdAndNoOrganisationAndReturnNumberOfAffectedRows() {
        GatewayAccountFixture gatewayAccountFixtureWithNoOrganisation = aGatewayAccountFixture()
                .withPaymentProvider(SANDBOX)
                .withOrganisation(null)
                .insert(testContext.getJdbi());

        GatewayAccountFixture gatewayAccountFixtureWithWrongOrganisation = aGatewayAccountFixture()
                .withPaymentProvider(SANDBOX)
                .withOrganisation(GoCardlessOrganisationId.valueOf("Different organisation"))
                .insert(testContext.getJdbi());
        
        MandateFixture mandateFixtureWithNoOrganisation = MandateFixture.aMandateFixture()
                .withGatewayAccountFixture(gatewayAccountFixtureWithNoOrganisation)
                .insert(testContext.getJdbi());

        MandateFixture mandateFixtureWithWrongOrganisation = MandateFixture.aMandateFixture()
                .withGatewayAccountFixture(gatewayAccountFixtureWithWrongOrganisation)
                .insert(testContext.getJdbi());

        aPaymentFixture()
                .withMandateFixture(mandateFixtureWithNoOrganisation)
                .withExternalId("Payment we want")
                .withPaymentProviderId(SandboxPaymentId.valueOf("Payment ID we want"))
                .insert(testContext.getJdbi());

        aPaymentFixture()
                .withMandateFixture(mandateFixtureWithNoOrganisation)
                .withPaymentProviderId(SandboxPaymentId.valueOf("Different payment ID"))
                .insert(testContext.getJdbi());

        aPaymentFixture()
                .withMandateFixture(mandateFixtureWithWrongOrganisation)
                .withPaymentProviderId(SandboxPaymentId.valueOf("Payment ID we want"))
                .insert(testContext.getJdbi());

        int numOfUpdatedPayments = paymentDao.updateStateByProviderId(SANDBOX,
                SandboxPaymentId.valueOf("Payment ID we want"),
                PaymentState.SUBMITTED_TO_PROVIDER,
                "state details",
                "state details description");

        assertThat(numOfUpdatedPayments, is(1));

        Payment payment = paymentDao.findByExternalId("Payment we want").get();
        assertThat(payment.getState(), is(PaymentState.SUBMITTED_TO_PROVIDER));
        assertThat(payment.getStateDetails(), is(Optional.of("state details")));
        assertThat(payment.getStateDetailsDescription(), is(Optional.of("state details description")));
    }

    @Test
    public void shouldUpdateStateByProviderIdAndNoOrganisationWithNoDetailsAndDescriptionAndReturnNumberOfAffectedRows() {
        GatewayAccountFixture gatewayAccountFixtureWithNoOrganisation = aGatewayAccountFixture()
                .withPaymentProvider(SANDBOX)
                .withOrganisation(null)
                .insert(testContext.getJdbi());

        GatewayAccountFixture gatewayAccountFixtureWithWrongOrganisation = aGatewayAccountFixture()
                .withPaymentProvider(SANDBOX)
                .withOrganisation(GoCardlessOrganisationId.valueOf("Different organisation"))
                .insert(testContext.getJdbi());

        MandateFixture mandateFixtureWithNoOrganisation = MandateFixture.aMandateFixture()
                .withGatewayAccountFixture(gatewayAccountFixtureWithNoOrganisation)
                .insert(testContext.getJdbi());

        MandateFixture mandateFixtureWithWrongOrganisation = MandateFixture.aMandateFixture()
                .withGatewayAccountFixture(gatewayAccountFixtureWithWrongOrganisation)
                .insert(testContext.getJdbi());

        aPaymentFixture()
                .withMandateFixture(mandateFixtureWithNoOrganisation)
                .withExternalId("Payment we want")
                .withPaymentProviderId(SandboxPaymentId.valueOf("Payment ID we want"))
                .insert(testContext.getJdbi());

        aPaymentFixture()
                .withMandateFixture(mandateFixtureWithNoOrganisation)
                .withPaymentProviderId(SandboxPaymentId.valueOf("Different payment ID"))
                .insert(testContext.getJdbi());

        aPaymentFixture()
                .withMandateFixture(mandateFixtureWithWrongOrganisation)
                .withPaymentProviderId(SandboxPaymentId.valueOf("Payment ID we want"))
                .withStateDetails("state details before update")
                .withStateDetailsDescription("state details description before update")
                .insert(testContext.getJdbi());

        int numOfUpdatedPayments = paymentDao.updateStateByProviderId(SANDBOX,
                SandboxPaymentId.valueOf("Payment ID we want"),
                PaymentState.SUBMITTED_TO_PROVIDER,
                null,
                null);

        assertThat(numOfUpdatedPayments, is(1));

        Payment payment = paymentDao.findByExternalId("Payment we want").get();
        assertThat(payment.getState(), is(PaymentState.SUBMITTED_TO_PROVIDER));
        assertThat(payment.getStateDetails(), is(Optional.empty()));
        assertThat(payment.getStateDetailsDescription(), is(Optional.empty()));
    }

    @Test
    public void findAllPaymentsBySetOfStatesAndCreationTime_shouldNotFindPayment_TooEarly() {
        aPaymentFixture().withMandateFixture(testMandate).withState(CREATED)
                .withCreatedDate(ZonedDateTime.now()).insert(testContext.getJdbi());

        Set<PaymentState> states = Set.of(CREATED);
        List<Payment> payments = paymentDao.findAllPaymentsBySetOfStatesAndCreationTime(states, ZonedDateTime.now().minusMinutes(90L));
        assertThat(payments.size(), is(0));
    }

    @Test
    public void findAllPaymentsBySetOfStatesAndCreationTime_shouldNotFindPayment_WrongState() {
        aPaymentFixture().withMandateFixture(testMandate).withState(PaymentState.SUBMITTED_TO_PROVIDER)
                .withCreatedDate(ZonedDateTime.now()).insert(testContext.getJdbi());

        Set<PaymentState> states = Set.of(CREATED);
        List<Payment> payments = paymentDao.findAllPaymentsBySetOfStatesAndCreationTime(states, ZonedDateTime.now().minusMinutes(90L));
        assertThat(payments.size(), is(0));
    }
    

    private PaymentFixture generateNewPaymentFixture(MandateFixture mandateFixture,
                                                     PaymentState paymentState,
                                                     long amount) {
        return aPaymentFixture()
                .withMandateFixture(mandateFixture)
                .withAmount(amount)
                .withState(paymentState);
    }

}
