package uk.gov.pay.directdebit.mandate.services.gocardless;

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
import uk.gov.pay.directdebit.mandate.model.GoCardlessMandateId;
import uk.gov.pay.directdebit.mandate.model.Mandate;
import uk.gov.pay.directdebit.mandate.model.MandateState;
import uk.gov.pay.directdebit.payments.fixtures.GatewayAccountFixture;

import java.time.ZonedDateTime;
import java.util.Optional;

import static java.time.ZoneOffset.UTC;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static uk.gov.pay.directdebit.events.model.GovUkPayEventType.MANDATE_USER_SETUP_CANCELLED;
import static uk.gov.pay.directdebit.gatewayaccounts.model.PaymentProvider.GOCARDLESS;
import static uk.gov.pay.directdebit.mandate.fixtures.MandateFixture.aMandateFixture;
import static uk.gov.pay.directdebit.mandate.services.GovUkPayEventToMandateStateMapper.GOV_UK_PAY_EVENT_TYPES_THAT_CHANGE_MANDATE_STATE;
import static uk.gov.pay.directdebit.mandate.services.gocardless.GoCardlessEventToMandateStateMapper.GOCARDLESS_ACTIONS_THAT_CHANGE_MANDATE_STATE;
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
            "submitted, SUBMITTED_TO_PROVIDER",
            "active, ACTIVE",
            "cancelled, USER_SETUP_CANCELLED",
            "failed, FAILED",
            "reinstated, ACTIVE"
    })
    public void goCardlessEventActionMapsToState(String action, String expectedState) {
        GoCardlessEvent goCardlessEvent = aGoCardlessEventFixture().withAction(action).toEntity();
        given(mockGoCardlessEventDao.findLatestApplicableEventForMandate(goCardlessMandateId, goCardlessOrganisationId,
                GOCARDLESS_ACTIONS_THAT_CHANGE_MANDATE_STATE))
                .willReturn(Optional.of(goCardlessEvent));

        Optional<DirectDebitStateWithDetails<MandateState>> result = goCardlessMandateStateCalculator.calculate(mandate);

        assertThat(result.get().getState(), is(MandateState.valueOf(expectedState)));
    }

    @Test
    @Parameters({
            "MANDATE_CREATED, CREATED",
            "MANDATE_TOKEN_EXCHANGED, AWAITING_DIRECT_DEBIT_DETAILS",
            "MANDATE_SUBMITTED_TO_PROVIDER, SUBMITTED_TO_PROVIDER",
            "MANDATE_USER_SETUP_EXPIRED, USER_SETUP_EXPIRED",
            "MANDATE_USER_SETUP_CANCELLED, USER_SETUP_CANCELLED",
            "MANDATE_USER_SETUP_CANCELLED_NOT_ELIGIBLE, USER_SETUP_CANCELLED"
    })
    public void govUkPayEventTypeMapsToState(String eventType, String expectedState) {
        GovUkPayEventType govUkPayEventType = GovUkPayEventType.valueOf(eventType);
        GovUkPayEvent govUkPayEvent = aGovUkPayEventFixture().withEventType(govUkPayEventType).toEntity();
        given(mockGovUkPayEventDao.findLatestApplicableEventForMandate(mandate.getId(), GOV_UK_PAY_EVENT_TYPES_THAT_CHANGE_MANDATE_STATE))
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
                GOCARDLESS_ACTIONS_THAT_CHANGE_MANDATE_STATE))
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
                GOCARDLESS_ACTIONS_THAT_CHANGE_MANDATE_STATE))
                .willReturn(Optional.of(goCardlessEvent));

        GovUkPayEvent govUkPayEvent = aGovUkPayEventFixture()
                .withEventType(MANDATE_USER_SETUP_CANCELLED)
                .withEventDate(ZonedDateTime.of(2019, 7, 22, 10, 0, 0, 0, UTC))
                .toEntity();
        given(mockGovUkPayEventDao.findLatestApplicableEventForMandate(mandate.getId(), GOV_UK_PAY_EVENT_TYPES_THAT_CHANGE_MANDATE_STATE))
                .willReturn(Optional.of(govUkPayEvent));

        Optional<DirectDebitStateWithDetails<MandateState>> result = goCardlessMandateStateCalculator.calculate(mandate);

        assertThat(result.get().getState(), is(MandateState.USER_SETUP_CANCELLED));
    }

    @Test
    public void resolvesStateFromGoCardlessEventWhenIsLaterThanLatestGovUkPayEvent() {
        GoCardlessEvent goCardlessEvent = aGoCardlessEventFixture()
                .withAction("active")
                .withCreatedAt(ZonedDateTime.of(2019, 7, 22, 10, 0, 0, 0, UTC))
                .toEntity();
        given(mockGoCardlessEventDao.findLatestApplicableEventForMandate(goCardlessMandateId, goCardlessOrganisationId,
                GOCARDLESS_ACTIONS_THAT_CHANGE_MANDATE_STATE))
                .willReturn(Optional.of(goCardlessEvent));

        GovUkPayEvent govUkPayEvent = aGovUkPayEventFixture()
                .withEventType(MANDATE_USER_SETUP_CANCELLED)
                .withEventDate(ZonedDateTime.of(2019, 7, 22, 9, 0, 0, 0, UTC))
                .toEntity();
        given(mockGovUkPayEventDao.findLatestApplicableEventForMandate(mandate.getId(), GOV_UK_PAY_EVENT_TYPES_THAT_CHANGE_MANDATE_STATE))
                .willReturn(Optional.of(govUkPayEvent));

        Optional<DirectDebitStateWithDetails<MandateState>> result = goCardlessMandateStateCalculator.calculate(mandate);

        assertThat(result.get().getState(), is(MandateState.ACTIVE));
    }

    @Test
    public void noApplicableEventsMapsToNothing() {
        given(mockGoCardlessEventDao.findLatestApplicableEventForMandate(goCardlessMandateId, goCardlessOrganisationId,
                GOCARDLESS_ACTIONS_THAT_CHANGE_MANDATE_STATE))
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

        goCardlessMandateStateCalculator.calculate(mandate);
    }
}
