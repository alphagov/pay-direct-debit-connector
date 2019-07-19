package uk.gov.pay.directdebit.mandate.services.gocardless;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import junitparams.converters.Param;
import org.apache.commons.lang3.builder.ToStringExclude;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.MockitoRule;
import uk.gov.pay.directdebit.common.model.DirectDebitStateWithDetails;
import uk.gov.pay.directdebit.events.dao.GoCardlessEventDao;
import uk.gov.pay.directdebit.events.dao.GovUkPayEventDao;
import uk.gov.pay.directdebit.events.model.GovUkPayEvent;
import uk.gov.pay.directdebit.gatewayaccounts.exception.GatewayAccountMissingOrganisationIdException;
import uk.gov.pay.directdebit.gatewayaccounts.model.GoCardlessOrganisationId;
import uk.gov.pay.directdebit.mandate.exception.UnexpectedGoCardlessEventActionException;
import uk.gov.pay.directdebit.mandate.exception.UnexpectedGovUkPayEventTypeException;
import uk.gov.pay.directdebit.mandate.model.GoCardlessMandateId;
import uk.gov.pay.directdebit.mandate.model.Mandate;
import uk.gov.pay.directdebit.mandate.model.MandateState;
import uk.gov.pay.directdebit.payments.fixtures.GatewayAccountFixture;
import uk.gov.pay.directdebit.events.model.GoCardlessEvent;

import java.time.ZonedDateTime;
import java.util.Optional;

import static java.time.ZoneOffset.UTC;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static uk.gov.pay.directdebit.events.model.GovUkPayEvent.GovUkPayEventType.MANDATE_CANCELLED_BY_USER;
import static uk.gov.pay.directdebit.events.model.GovUkPayEvent.GovUkPayEventType.PAYMENT_SUBMITTED;
import static uk.gov.pay.directdebit.gatewayaccounts.model.PaymentProvider.GOCARDLESS;
import static uk.gov.pay.directdebit.mandate.fixtures.MandateFixture.aMandateFixture;
import static uk.gov.pay.directdebit.mandate.services.gocardless.GoCardlessMandateStateCalculator.GOCARDLESS_ACTIONS_THAT_CHANGE_STATE;
import static uk.gov.pay.directdebit.mandate.services.gocardless.GoCardlessMandateStateCalculator.GOV_UK_PAY_EVENT_TYPES_THAT_CHANGE_STATE;
import static uk.gov.pay.directdebit.payments.fixtures.GatewayAccountFixture.aGatewayAccountFixture;
import static uk.gov.pay.directdebit.payments.fixtures.GoCardlessEventFixture.aGoCardlessEventFixture;
import static uk.gov.pay.directdebit.payments.fixtures.GovUkPayEventFixture.aGovUkPayEventFixture;

@RunWith(JUnitParamsRunner.class)
public class GoCardlessMandateStateCalculatorTest {

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Mock
    private GoCardlessEventDao mockGoCardlessEventDao;

    @Mock
    private GovUkPayEventDao mockGovUkPayEventDao;

    @InjectMocks
    private GoCardlessMandateStateCalculator goCardlessMandateStateCalculator;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private GoCardlessMandateId goCardlessMandateId = GoCardlessMandateId.valueOf("a-mandate-id");

    private GoCardlessOrganisationId goCardlessOrganisationId = GoCardlessOrganisationId.valueOf("an-organisation-id");

    private Mandate mandate;

    @Before
    public void setUp() {
        GatewayAccountFixture gatewayAccountFixture = aGatewayAccountFixture()
                .withPaymentProvider(GOCARDLESS)
                .withOrganisation(goCardlessOrganisationId);

        mandate = aMandateFixture()
                .withPaymentProviderId(goCardlessMandateId)
                .withGatewayAccountFixture(gatewayAccountFixture)
                .toEntity();
    }

    @Test
    @Parameters({
            "created, CREATED",
            "submitted, SUBMITTED",
            "active, ACTIVE",
            "cancelled, CANCELLED",
            "failed, FAILED"
    })

    public void goCardlessEventActionMapsToState(String action, String expectedState) {
        GoCardlessEvent goCardlessEvent = aGoCardlessEventFixture().withAction(action).toEntity();
        given(mockGoCardlessEventDao.findLatestApplicableEventForMandate(goCardlessMandateId, goCardlessOrganisationId,
                GOCARDLESS_ACTIONS_THAT_CHANGE_STATE))
                .willReturn(Optional.of(goCardlessEvent));

        Optional<DirectDebitStateWithDetails<MandateState>> result = goCardlessMandateStateCalculator.calculate(mandate);

        assertThat(result.get().getState(), is(MandateState.valueOf(expectedState)));
    }

    @Test
    @Parameters({
            "MANDATE_CREATED, CREATED",
            "MANDATE_TOKEN_EXCHANGED, AWAITING_DIRECT_DEBIT_DETAILS",
            "MANDATE_SUBMITTED, SUBMITTED",
            "MANDATE_EXPIRED_BY_SYSTEM, EXPIRED",
            "MANDATE_CANCELLED_BY_USER, CANCELLED",
            "MANDATE_CANCELLED_BY_USER_NOT_ELIGIBLE, USER_CANCEL_NOT_ELIGIBLE"
    })
    public void govUkPayEventTypeMapsToState(String eventType, String expectedState) {
        GovUkPayEvent.GovUkPayEventType govUkPayEventType = GovUkPayEvent.GovUkPayEventType.valueOf(eventType);
        GovUkPayEvent govUkPayEvent = aGovUkPayEventFixture().withEventType(govUkPayEventType).toEntity();
        given(mockGovUkPayEventDao.findLatestApplicableEventForMandate(mandate.getId(), GOV_UK_PAY_EVENT_TYPES_THAT_CHANGE_STATE))
                .willReturn(Optional.of(govUkPayEvent));

        Optional<DirectDebitStateWithDetails<MandateState>> result = goCardlessMandateStateCalculator.calculate(mandate);

        assertThat(result.get().getState(), is(MandateState.valueOf(expectedState)));
    }

    @Test
    public void detailsCauseAndDescriptionReturned() {
        GoCardlessEvent goCardlessEvent = aGoCardlessEventFixture().withAction("failed")
                .withDetailsCause("details_cause")
                .withDetailsDescription("This is a description")
                .toEntity();
        given(mockGoCardlessEventDao.findLatestApplicableEventForMandate(goCardlessMandateId, goCardlessOrganisationId,
                GOCARDLESS_ACTIONS_THAT_CHANGE_STATE))
                .willReturn(Optional.of(goCardlessEvent));

        Optional<DirectDebitStateWithDetails<MandateState>> result = goCardlessMandateStateCalculator.calculate(mandate);

        assertThat(result.get().getDetails(), is(Optional.of(goCardlessEvent.getDetailsCause())));
        assertThat(result.get().getDetailsDescription(), is(Optional.of(goCardlessEvent.getDetailsDescription())));
    }

    @Test
    public void resolvesStateFromGovUkPayEventWhenIsLaterThanLatestGoCardlessEvent() {
        GoCardlessEvent goCardlessEvent = aGoCardlessEventFixture()
                .withAction("active")
                .withCreatedAt(ZonedDateTime.of(2019, 7, 22, 9, 0, 0, 0, UTC))
                .toEntity();
        given(mockGoCardlessEventDao.findLatestApplicableEventForMandate(goCardlessMandateId, goCardlessOrganisationId,
                GOCARDLESS_ACTIONS_THAT_CHANGE_STATE))
                .willReturn(Optional.of(goCardlessEvent));

        GovUkPayEvent govUkPayEvent = aGovUkPayEventFixture()
                .withEventType(MANDATE_CANCELLED_BY_USER)
                .withEventDate(ZonedDateTime.of(2019, 7, 22, 10, 0, 0, 0, UTC))
                .toEntity();
        given(mockGovUkPayEventDao.findLatestApplicableEventForMandate(mandate.getId(), GOV_UK_PAY_EVENT_TYPES_THAT_CHANGE_STATE))
                .willReturn(Optional.of(govUkPayEvent));

        Optional<DirectDebitStateWithDetails<MandateState>> result = goCardlessMandateStateCalculator.calculate(mandate);

        assertThat(result.get().getState(), is(MandateState.CANCELLED));
    }

    @Test
    public void resolvesStateFromGoCardlessEventWhenIsLaterThanLatestGovUkPayEvent() {
        GoCardlessEvent goCardlessEvent = aGoCardlessEventFixture()
                .withAction("active")
                .withCreatedAt(ZonedDateTime.of(2019, 7, 22, 10, 0, 0, 0, UTC))
                .toEntity();
        given(mockGoCardlessEventDao.findLatestApplicableEventForMandate(goCardlessMandateId, goCardlessOrganisationId,
                GOCARDLESS_ACTIONS_THAT_CHANGE_STATE))
                .willReturn(Optional.of(goCardlessEvent));

        GovUkPayEvent govUkPayEvent = aGovUkPayEventFixture()
                .withEventType(MANDATE_CANCELLED_BY_USER)
                .withEventDate(ZonedDateTime.of(2019, 7, 22, 9, 0, 0, 0, UTC))
                .toEntity();
        given(mockGovUkPayEventDao.findLatestApplicableEventForMandate(mandate.getId(), GOV_UK_PAY_EVENT_TYPES_THAT_CHANGE_STATE))
                .willReturn(Optional.of(govUkPayEvent));

        Optional<DirectDebitStateWithDetails<MandateState>> result = goCardlessMandateStateCalculator.calculate(mandate);

        assertThat(result.get().getState(), is(MandateState.ACTIVE));
    }

    @Test
    public void unexpectedGoCardlessEventActionThrowsException() {
        GoCardlessEvent goCardlessEvent = aGoCardlessEventFixture().withAction("eaten_by_wolves").toEntity();
        given(mockGoCardlessEventDao.findLatestApplicableEventForMandate(goCardlessMandateId, goCardlessOrganisationId,
                GOCARDLESS_ACTIONS_THAT_CHANGE_STATE))
                .willReturn(Optional.of(goCardlessEvent));

        thrown.expect(UnexpectedGoCardlessEventActionException.class);

        Optional<DirectDebitStateWithDetails<MandateState>> result = goCardlessMandateStateCalculator.calculate(mandate);

        assertThat(result, is(Optional.empty()));
    }

    @Test
    public void unexpectedGovUkPayEventTypeThrowsException() {
        GovUkPayEvent govUkPayEvent = aGovUkPayEventFixture().withEventType(PAYMENT_SUBMITTED).toEntity();
        given(mockGovUkPayEventDao.findLatestApplicableEventForMandate(mandate.getId(), GOV_UK_PAY_EVENT_TYPES_THAT_CHANGE_STATE))
                .willReturn(Optional.of(govUkPayEvent));

        thrown.expect(UnexpectedGovUkPayEventTypeException.class);

        Optional<DirectDebitStateWithDetails<MandateState>> result = goCardlessMandateStateCalculator.calculate(mandate);

        assertThat(result, is(Optional.empty()));
    }

    @Test
    public void noApplicableEventsMapsToNothing() {
        given(mockGoCardlessEventDao.findLatestApplicableEventForMandate(goCardlessMandateId, goCardlessOrganisationId,
                GOCARDLESS_ACTIONS_THAT_CHANGE_STATE))
                .willReturn(Optional.empty());

        Optional<DirectDebitStateWithDetails<MandateState>> result = goCardlessMandateStateCalculator.calculate(mandate);

        assertThat(result, is(Optional.empty()));
    }
    
    @Test
    public void gatewayAccountMissingOrganisationIdThrowsException() {
        GatewayAccountFixture gatewayAccountFixture = aGatewayAccountFixture()
                .withPaymentProvider(GOCARDLESS)
                .withOrganisation(null);

        var mandate = aMandateFixture()
                .withPaymentProviderId(goCardlessMandateId)
                .withGatewayAccountFixture(gatewayAccountFixture)
                .toEntity();
        
        thrown.expect(GatewayAccountMissingOrganisationIdException.class);

        Optional<DirectDebitStateWithDetails<MandateState>> result = goCardlessMandateStateCalculator.calculate(mandate);
    }
}
