package uk.gov.pay.directdebit.payments.services.gocardless;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import uk.gov.pay.directdebit.common.model.DirectDebitStateWithDetails;
import uk.gov.pay.directdebit.events.dao.GoCardlessEventDao;
import uk.gov.pay.directdebit.events.dao.GovUkPayEventDao;
import uk.gov.pay.directdebit.events.model.GoCardlessEvent;
import uk.gov.pay.directdebit.events.model.GovUkPayEvent;
import uk.gov.pay.directdebit.events.model.GovUkPayEventType;
import uk.gov.pay.directdebit.gatewayaccounts.exception.GatewayAccountMissingOrganisationIdException;
import uk.gov.pay.directdebit.gatewayaccounts.model.GoCardlessOrganisationId;
import uk.gov.pay.directdebit.gatewayaccounts.model.PaymentProvider;
import uk.gov.pay.directdebit.mandate.fixtures.MandateFixture;
import uk.gov.pay.directdebit.payments.fixtures.GatewayAccountFixture;
import uk.gov.pay.directdebit.payments.model.GoCardlessPaymentId;
import uk.gov.pay.directdebit.payments.model.Payment;
import uk.gov.pay.directdebit.payments.model.PaymentState;

import java.time.ZonedDateTime;
import java.util.Optional;

import static java.time.ZoneOffset.UTC;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static uk.gov.pay.directdebit.events.model.GovUkPayEventType.PAYMENT_SUBMITTED;
import static uk.gov.pay.directdebit.gatewayaccounts.model.PaymentProvider.GOCARDLESS;
import static uk.gov.pay.directdebit.mandate.fixtures.MandateFixture.aMandateFixture;
import static uk.gov.pay.directdebit.payments.fixtures.GatewayAccountFixture.aGatewayAccountFixture;
import static uk.gov.pay.directdebit.payments.fixtures.GoCardlessEventFixture.aGoCardlessEventFixture;
import static uk.gov.pay.directdebit.payments.fixtures.GovUkPayEventFixture.aGovUkPayEventFixture;
import static uk.gov.pay.directdebit.payments.fixtures.PaymentFixture.aPaymentFixture;
import static uk.gov.pay.directdebit.payments.services.gocardless.GoCardlessPaymentStateCalculator.GOCARDLESS_ACTIONS_THAT_CHANGE_STATE;
import static uk.gov.pay.directdebit.payments.services.gocardless.GoCardlessPaymentStateCalculator.GOV_UK_PAY_EVENT_TYPES_THAT_CHANGE_STATE;

@RunWith(JUnitParamsRunner.class)
public class GoCardlessPaymentStateCalculatorTest {

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Mock
    private GoCardlessEventDao mockGoCardlessEventDao;

    @Mock
    private GovUkPayEventDao mockGovUkPayEventDao;

    @InjectMocks
    private GoCardlessPaymentStateCalculator goCardlessPaymentStateCalculator;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private GoCardlessOrganisationId goCardlessOrganisationId = GoCardlessOrganisationId.valueOf("an-organisation-id");

    private GoCardlessPaymentId goCardlessPaymentId = GoCardlessPaymentId.valueOf("a-payment-id");

    private Payment payment;

    @Before
    public void setUp() {
        GatewayAccountFixture gatewayAccountFixture = aGatewayAccountFixture()
                .withPaymentProvider(PaymentProvider.GOCARDLESS)
                .withOrganisation(goCardlessOrganisationId);

        MandateFixture mandateFixture = aMandateFixture().withGatewayAccountFixture(gatewayAccountFixture);

        payment = aPaymentFixture()
                .withMandateFixture(mandateFixture)
                .withPaymentProviderId(goCardlessPaymentId)
                .toEntity();
    }

    @Test
    @Parameters({
            "failed, FAILED",
            "paid_out, PAID_OUT"
    })
    public void goCardlessEventActionMapsToState(String action, String expectedState) {
        GoCardlessEvent goCardlessEvent = aGoCardlessEventFixture().withAction(action).toEntity();
        given(mockGoCardlessEventDao.findLatestApplicableEventForPayment(goCardlessPaymentId, goCardlessOrganisationId,
                GOCARDLESS_ACTIONS_THAT_CHANGE_STATE))
                .willReturn(Optional.of(goCardlessEvent));

        Optional<DirectDebitStateWithDetails<PaymentState>> result = goCardlessPaymentStateCalculator.calculate(payment);

        assertThat(result.get().getState(), is(PaymentState.valueOf(expectedState)));
    }

    @Test
    @Parameters({
            "PAYMENT_SUBMITTED, SUBMITTED_TO_PROVIDER"
    })
    public void govUkPayEventTypeMapsToState(String eventType, String expectedState) {
        GovUkPayEventType govUkPayEventType = GovUkPayEventType.valueOf(eventType);
        GovUkPayEvent govUkPayEvent = aGovUkPayEventFixture().withEventType(govUkPayEventType).toEntity();
        given(mockGovUkPayEventDao.findLatestApplicableEventForPayment(payment.getId(), GOV_UK_PAY_EVENT_TYPES_THAT_CHANGE_STATE))
                .willReturn(Optional.of(govUkPayEvent));

        Optional<DirectDebitStateWithDetails<PaymentState>> result = goCardlessPaymentStateCalculator.calculate(payment);

        assertThat(result.get().getState(), is(PaymentState.valueOf(expectedState)));
    }

    @Test
    public void detailsCauseAndDescriptionReturned() {
        GoCardlessEvent goCardlessEvent = aGoCardlessEventFixture().withAction("failed")
                .withDetailsCause("details_cause")
                .withDetailsDescription("This is a description")
                .toEntity();

        given(mockGoCardlessEventDao.findLatestApplicableEventForPayment(goCardlessPaymentId, goCardlessOrganisationId,
                GOCARDLESS_ACTIONS_THAT_CHANGE_STATE))
                .willReturn(Optional.of(goCardlessEvent));

        Optional<DirectDebitStateWithDetails<PaymentState>> result = goCardlessPaymentStateCalculator.calculate(payment);

        assertThat(result.get().getDetails(), is(Optional.of(goCardlessEvent.getDetailsCause())));
        assertThat(result.get().getDetailsDescription(), is(Optional.of(goCardlessEvent.getDetailsDescription())));
    }

    @Test
    public void resolvesStateFromGovUkPayEventWhenIsLaterThanLatestGoCardlessEvent() {
        GoCardlessEvent goCardlessEvent = aGoCardlessEventFixture()
                .withAction("failed")
                .withCreatedAt(ZonedDateTime.of(2019, 7, 22, 9, 0, 0, 0, UTC))
                .toEntity();
        given(mockGoCardlessEventDao.findLatestApplicableEventForPayment(goCardlessPaymentId, goCardlessOrganisationId,
                GOCARDLESS_ACTIONS_THAT_CHANGE_STATE))
                .willReturn(Optional.of(goCardlessEvent));

        GovUkPayEvent govUkPayEvent = aGovUkPayEventFixture()
                .withEventType(PAYMENT_SUBMITTED)
                .withEventDate(ZonedDateTime.of(2019, 7, 22, 10, 0, 0, 0, UTC))
                .toEntity();
        given(mockGovUkPayEventDao.findLatestApplicableEventForPayment(payment.getId(), GOV_UK_PAY_EVENT_TYPES_THAT_CHANGE_STATE))
                .willReturn(Optional.of(govUkPayEvent));

        Optional<DirectDebitStateWithDetails<PaymentState>> result = goCardlessPaymentStateCalculator.calculate(payment);

        assertThat(result.get().getState(), is(PaymentState.SUBMITTED_TO_PROVIDER));
    }

    @Test
    public void resolvesStateFromGoCardlessEventWhenIsLaterThanLatestGovUkPayEvent() {
        GoCardlessEvent goCardlessEvent = aGoCardlessEventFixture()
                .withAction("failed")
                .withCreatedAt(ZonedDateTime.of(2019, 7, 22, 10, 0, 0, 0, UTC))
                .toEntity();
        given(mockGoCardlessEventDao.findLatestApplicableEventForPayment(goCardlessPaymentId, goCardlessOrganisationId,
                GOCARDLESS_ACTIONS_THAT_CHANGE_STATE))
                .willReturn(Optional.of(goCardlessEvent));

        GovUkPayEvent govUkPayEvent = aGovUkPayEventFixture()
                .withEventType(PAYMENT_SUBMITTED)
                .withEventDate(ZonedDateTime.of(2019, 7, 22, 9, 0, 0, 0, UTC))
                .toEntity();
        given(mockGovUkPayEventDao.findLatestApplicableEventForPayment(payment.getId(), GOV_UK_PAY_EVENT_TYPES_THAT_CHANGE_STATE))
                .willReturn(Optional.of(govUkPayEvent));

        Optional<DirectDebitStateWithDetails<PaymentState>> result = goCardlessPaymentStateCalculator.calculate(payment);

        assertThat(result.get().getState(), is(PaymentState.FAILED));
    }

    @Test
    public void noApplicableEventsMapsToNothing() {
        given(mockGoCardlessEventDao.findLatestApplicableEventForPayment(goCardlessPaymentId, goCardlessOrganisationId,
                GOCARDLESS_ACTIONS_THAT_CHANGE_STATE))
                .willReturn(Optional.empty());

        Optional<DirectDebitStateWithDetails<PaymentState>> result = goCardlessPaymentStateCalculator.calculate(payment);

        assertThat(result, is(Optional.empty()));
    }


    @Test
    public void gatewayAccountMissingOrganisationIdThrowsException() {
        GatewayAccountFixture gatewayAccountFixture = aGatewayAccountFixture()
                .withPaymentProvider(GOCARDLESS)
                .withOrganisation(null);

        var mandateFixture = aMandateFixture()
                .withGatewayAccountFixture(gatewayAccountFixture);

        Payment payment = aPaymentFixture()
                .withMandateFixture(mandateFixture)
                .withPaymentProviderId(goCardlessPaymentId)
                .toEntity();

        thrown.expect(GatewayAccountMissingOrganisationIdException.class);

        goCardlessPaymentStateCalculator.calculate(payment);
    }

}
