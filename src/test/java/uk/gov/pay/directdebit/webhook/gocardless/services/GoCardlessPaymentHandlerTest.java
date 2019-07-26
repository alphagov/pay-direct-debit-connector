package uk.gov.pay.directdebit.webhook.gocardless.services;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.pay.directdebit.events.exception.GoCardlessEventHasNoPaymentIdException;
import uk.gov.pay.directdebit.events.model.GoCardlessEvent;
import uk.gov.pay.directdebit.events.services.GoCardlessEventService;
import uk.gov.pay.directdebit.gatewayaccounts.model.GoCardlessOrganisationId;
import uk.gov.pay.directdebit.mandate.fixtures.MandateFixture;
import uk.gov.pay.directdebit.payments.fixtures.GatewayAccountFixture;
import uk.gov.pay.directdebit.payments.fixtures.GoCardlessEventFixture;
import uk.gov.pay.directdebit.payments.fixtures.PaymentFixture;
import uk.gov.pay.directdebit.payments.model.Payment;
import uk.gov.pay.directdebit.payments.services.PaymentQueryService;
import uk.gov.pay.directdebit.payments.services.PaymentService;
import uk.gov.pay.directdebit.webhook.gocardless.services.handlers.GoCardlessPaymentHandler;

import java.util.Optional;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class GoCardlessPaymentHandlerTest {

    @Mock
    private PaymentService mockedPaymentService;
    
    @Mock
    private PaymentQueryService mockedPaymentQueryService;
    
    @Mock
    private GoCardlessEventService mockedGoCardlessEventService;

    private GoCardlessPaymentHandler goCardlessPaymentHandler;
    private GoCardlessOrganisationId organisationIdentifier = GoCardlessOrganisationId.valueOf("test_org_id");
    private GatewayAccountFixture gatewayAccountFixture = GatewayAccountFixture.aGatewayAccountFixture().withOrganisation(organisationIdentifier);
    private MandateFixture mandateFixture = MandateFixture.aMandateFixture().withGatewayAccountFixture(gatewayAccountFixture);
    private Payment payment = PaymentFixture.aPaymentFixture().withMandateFixture(mandateFixture).toEntity();
    private GoCardlessEventFixture goCardlessEventFixture = GoCardlessEventFixture.aGoCardlessEventFixture().withLinksOrganisation(organisationIdentifier);

    @Rule
    public ExpectedException thrown = ExpectedException.none();
    
    @Before
    public void setUp() {
        goCardlessPaymentHandler = new GoCardlessPaymentHandler(mockedPaymentService, mockedPaymentQueryService, mockedGoCardlessEventService);
    }

    @Test
    public void handle_onFailedPaymentGoCardlessEvent_shouldSetAPayEventAsFailed() {
        GoCardlessEvent goCardlessEvent = spy(goCardlessEventFixture.withAction("failed").toEntity());

        when(mockedPaymentQueryService.findByGoCardlessPaymentIdAndOrganisationId(goCardlessEvent.getLinksPayment().get(),
                goCardlessEvent.getLinksOrganisation()))
                .thenReturn(Optional.of(payment));

        goCardlessPaymentHandler.handle(goCardlessEvent);

        verify(mockedPaymentService).paymentFailedWithEmailFor(payment);
    }

    @Test
    public void handle_doNotProcess_organisationOnEventDoesNotMatchGatewayAccount() {
        GoCardlessEvent goCardlessEvent = spy(goCardlessEventFixture
                .withLinksOrganisation(GoCardlessOrganisationId.valueOf("i_do_not_exist_id"))
                .withAction("paid")
                .toEntity());

        when(mockedPaymentQueryService.findByGoCardlessPaymentIdAndOrganisationId(goCardlessEvent.getLinksPayment().get(),
                goCardlessEvent.getLinksOrganisation()))
                .thenReturn(Optional.of(payment));

        goCardlessPaymentHandler.handle(goCardlessEvent);
        verify(mockedGoCardlessEventService, never()).storeEvent(goCardlessEvent);
    }

    @Test
    public void handle_onCreatePaymentGoCardlessEvent_shouldThrowExceptionWhenEventHasNoLinkedPayment() {
        GoCardlessEvent goCardlessEvent = goCardlessEventFixture
                .withLinksOrganisation(GoCardlessOrganisationId.valueOf("does_not_exist"))
                .withAction("created")
                .withLinksPayment(null)
                .toEntity();

        thrown.expect(GoCardlessEventHasNoPaymentIdException.class);
        goCardlessPaymentHandler.handle(goCardlessEvent);
    }

}
