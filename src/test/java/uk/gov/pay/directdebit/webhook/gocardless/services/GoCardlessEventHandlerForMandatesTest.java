package uk.gov.pay.directdebit.webhook.gocardless.services;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.pay.directdebit.events.exception.GoCardlessEventHasNoMandateIdException;
import uk.gov.pay.directdebit.events.model.GoCardlessEvent;
import uk.gov.pay.directdebit.events.model.GoCardlessResourceType;
import uk.gov.pay.directdebit.gatewayaccounts.model.GoCardlessOrganisationId;
import uk.gov.pay.directdebit.mandate.fixtures.MandateFixture;
import uk.gov.pay.directdebit.mandate.services.MandateQueryService;
import uk.gov.pay.directdebit.notifications.services.UserNotificationService;
import uk.gov.pay.directdebit.payers.fixtures.PayerFixture;
import uk.gov.pay.directdebit.payments.fixtures.GatewayAccountFixture;
import uk.gov.pay.directdebit.payments.fixtures.GoCardlessEventFixture;
import uk.gov.pay.directdebit.payments.services.PaymentQueryService;
import uk.gov.pay.directdebit.webhook.gocardless.services.handlers.GoCardlessEventHandler;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class GoCardlessEventHandlerForMandatesTest {

    @Mock
    private PaymentQueryService mockPaymentQueryService;
    
    @Mock
    private MandateQueryService mockMandateQueryService;

    @Mock
    private UserNotificationService userNotificationService;

    @InjectMocks
    private GoCardlessEventHandler goCardlessEventHandler;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private PayerFixture payerFixture = PayerFixture.aPayerFixture();
    private GoCardlessOrganisationId organisationIdentifier = GoCardlessOrganisationId.valueOf("test_organisation");

    private GatewayAccountFixture gatewayAccountFixture = GatewayAccountFixture.aGatewayAccountFixture()
            .withOrganisation(organisationIdentifier);

    private MandateFixture mandateFixture = MandateFixture.aMandateFixture()
            .withGatewayAccountFixture(gatewayAccountFixture)
            .withPayerFixture(payerFixture);

    private GoCardlessEventFixture goCardlessEventFixture = GoCardlessEventFixture.aGoCardlessEventFixture()
            .withResourceType(GoCardlessResourceType.MANDATES)
            .withLinksOrganisation(organisationIdentifier);

    @Test
    public void handle_onAFailedMandateGoCardlessEvent_shouldRegisterAPayEventAsMandateFailed() {
        GoCardlessEvent goCardlessEvent = goCardlessEventFixture.withAction("failed").toEntity();

        when(mockMandateQueryService.findByGoCardlessMandateIdAndOrganisationId(
                goCardlessEvent.getLinksMandate().get(), goCardlessEventFixture.getLinksOrganisation()))
                .thenReturn(mandateFixture.toEntity());

        goCardlessEventHandler.handle(List.of(goCardlessEvent));

        verify(userNotificationService).sendMandateFailedEmailFor(mandateFixture.toEntity());
        verify(mockMandateQueryService).findByGoCardlessMandateIdAndOrganisationId(
                goCardlessEvent.getLinksMandate().get(), goCardlessEventFixture.getLinksOrganisation());
        verify(mockPaymentQueryService, never()).findByGoCardlessPaymentIdAndOrganisationId(any(), any());
    }

    @Test
    public void handle_onACancelledMandateGoCardlessEvent_shouldRegisterAPayEventAsMandateCancelled() {
        GoCardlessEvent goCardlessEvent = goCardlessEventFixture.withAction("cancelled").toEntity();

        when(mockMandateQueryService.findByGoCardlessMandateIdAndOrganisationId(
                goCardlessEvent.getLinksMandate().get(), goCardlessEventFixture.getLinksOrganisation()))
                .thenReturn(mandateFixture.toEntity());

        goCardlessEventHandler.handle(List.of(goCardlessEvent));

        verify(userNotificationService).sendMandateCancelledEmailFor(mandateFixture.toEntity());
        verify(mockMandateQueryService).findByGoCardlessMandateIdAndOrganisationId(
                goCardlessEvent.getLinksMandate().get(), goCardlessEventFixture.getLinksOrganisation());
        verify(mockPaymentQueryService, never()).findByGoCardlessPaymentIdAndOrganisationId(any(), any());
    }

    @Test
    public void handle_onCreateMandateGoCardlessEvent_shouldThrowExceptionWhenEventHasNoLinkedMandate() {
        GoCardlessEvent goCardlessEvent = goCardlessEventFixture
                .withLinksOrganisation(GoCardlessOrganisationId.valueOf("does_not_exist"))
                .withAction("cancelled")
                .withLinksMandate(null)
                .toEntity();

        thrown.expect(GoCardlessEventHasNoMandateIdException.class);
        goCardlessEventHandler.handle(List.of(goCardlessEvent));
        verify(mockPaymentQueryService, never()).findByGoCardlessPaymentIdAndOrganisationId(any(), any());
        verify(mockMandateQueryService, never()).findByGoCardlessMandateIdAndOrganisationId(any(), any());
    }
}
