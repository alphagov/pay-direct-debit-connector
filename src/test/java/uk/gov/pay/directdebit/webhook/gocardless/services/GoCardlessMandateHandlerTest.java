package uk.gov.pay.directdebit.webhook.gocardless.services;

import com.google.common.collect.ImmutableList;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.pay.directdebit.events.exception.GoCardlessEventHasNoMandateIdException;
import uk.gov.pay.directdebit.events.model.GoCardlessEvent;
import uk.gov.pay.directdebit.events.services.GoCardlessEventService;
import uk.gov.pay.directdebit.gatewayaccounts.model.GoCardlessOrganisationId;
import uk.gov.pay.directdebit.mandate.fixtures.MandateFixture;
import uk.gov.pay.directdebit.mandate.services.MandateQueryService;
import uk.gov.pay.directdebit.mandate.services.MandateStateUpdateService;
import uk.gov.pay.directdebit.payers.fixtures.PayerFixture;
import uk.gov.pay.directdebit.payments.fixtures.DirectDebitEventFixture;
import uk.gov.pay.directdebit.payments.fixtures.GatewayAccountFixture;
import uk.gov.pay.directdebit.payments.fixtures.GoCardlessEventFixture;
import uk.gov.pay.directdebit.payments.fixtures.PaymentFixture;
import uk.gov.pay.directdebit.payments.model.DirectDebitEvent;
import uk.gov.pay.directdebit.payments.services.PaymentService;
import uk.gov.pay.directdebit.webhook.gocardless.services.handlers.GoCardlessMandateHandler;

import java.util.Optional;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class GoCardlessMandateHandlerTest {

    @Mock
    private PaymentService mockPaymentService;
    
    @Mock
    private MandateQueryService mockMandateQueryService;
    
    @Mock
    private MandateStateUpdateService mockMandateStateUpdateService;

    @Mock
    private GoCardlessEventService mockGoCardlessService;
    
    @InjectMocks
    private GoCardlessMandateHandler goCardlessMandateHandler;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private DirectDebitEvent directDebitEvent = DirectDebitEventFixture.aDirectDebitEventFixture().toEntity();
    private PayerFixture payerFixture = PayerFixture.aPayerFixture();
    private GoCardlessOrganisationId organisationIdentifier = GoCardlessOrganisationId.valueOf("test_organisation");

    private GatewayAccountFixture gatewayAccountFixture = GatewayAccountFixture.aGatewayAccountFixture()
            .withOrganisation(organisationIdentifier);
    private MandateFixture mandateFixture = MandateFixture.aMandateFixture()
            .withGatewayAccountFixture(gatewayAccountFixture)
            .withPayerFixture(payerFixture);
    private PaymentFixture paymentFixture = PaymentFixture.aPaymentFixture()
            .withMandateFixture(mandateFixture);
    private GoCardlessEventFixture goCardlessEventFixture = GoCardlessEventFixture.aGoCardlessEventFixture()
            .withLinksOrganisation(organisationIdentifier);

    @Before
    public void setUp() {
        when(mockPaymentService.findPaymentsForMandate(mandateFixture.getExternalId())).thenReturn(ImmutableList
                .of(paymentFixture.toEntity()));
    }

    @Test
    public void handle_onAFailedMandateGoCardlessEvent_shouldRegisterAPayEventAsMandateFailed() {
        GoCardlessEvent goCardlessEvent = spy(goCardlessEventFixture.withAction("failed").toEntity());

        when(mockMandateQueryService.findByGoCardlessMandateIdAndOrganisationId(
                goCardlessEvent.getLinksMandate().get(), goCardlessEventFixture.getLinksOrganisation()))
                .thenReturn(mandateFixture.toEntity());
        when(mockPaymentService.paymentFailedWithoutEmailFor(paymentFixture.toEntity())).thenReturn(
                directDebitEvent);

        goCardlessMandateHandler.handle(goCardlessEvent);

        verify(mockPaymentService).paymentFailedWithoutEmailFor(paymentFixture.toEntity());
        verify(mockMandateStateUpdateService).mandateFailedFor(mandateFixture.toEntity());
    }

    @Test
    public void handle_onACancelledMandateGoCardlessEvent_shouldRegisterAPayEventAsMandateCancelled_AndFailPaymentIfNotSubmittedToGC() {
        GoCardlessEvent goCardlessEvent = spy(goCardlessEventFixture.withAction("cancelled").toEntity());

        when(mockMandateQueryService.findByGoCardlessMandateIdAndOrganisationId(
                goCardlessEvent.getLinksMandate().get(), goCardlessEventFixture.getLinksOrganisation()))
                .thenReturn(mandateFixture.toEntity());
        when(mockPaymentService.findPaymentSubmittedEventFor(paymentFixture.toEntity())).thenReturn(Optional.empty());
        when(mockPaymentService.paymentFailedWithoutEmailFor(paymentFixture.toEntity())).thenReturn(
                directDebitEvent);

        goCardlessMandateHandler.handle(goCardlessEvent);

        verify(mockPaymentService).paymentFailedWithoutEmailFor(paymentFixture.toEntity());
        verify(mockMandateStateUpdateService).mandateCancelledFor(mandateFixture.toEntity());
    }

    @Test
    public void handle_onACancelledMandateGoCardlessEvent_shouldRegisterAPayEventAsMandateCancelled_withoutFailingPaymentIfSubmittedToGC() {
        GoCardlessEvent goCardlessEvent = spy(goCardlessEventFixture.withAction("cancelled").toEntity());

        when(mockMandateQueryService.findByGoCardlessMandateIdAndOrganisationId(
                goCardlessEvent.getLinksMandate().get(), goCardlessEventFixture.getLinksOrganisation()))
                .thenReturn(mandateFixture.toEntity());
        when(mockPaymentService.findPaymentSubmittedEventFor(paymentFixture.toEntity())).thenReturn(Optional.of(
                directDebitEvent));

        goCardlessMandateHandler.handle(goCardlessEvent);

        verify(mockPaymentService, never()).paymentFailedWithoutEmailFor(paymentFixture.toEntity());
        verify(mockMandateStateUpdateService).mandateCancelledFor(mandateFixture.toEntity());
    }

    @Test
    public void handle_onCreateMandateGoCardlessEvent_shouldThrowExceptionWhenEventHasNoLinkedMandate() {
        GoCardlessEvent goCardlessEvent = spy(goCardlessEventFixture
                .withLinksOrganisation(GoCardlessOrganisationId.valueOf("does_not_exist"))
                .withAction("cancelled")
                .withLinksMandate(null)
                .toEntity());

        thrown.expect(GoCardlessEventHasNoMandateIdException.class);
        goCardlessMandateHandler.handle(goCardlessEvent);
    }
}
