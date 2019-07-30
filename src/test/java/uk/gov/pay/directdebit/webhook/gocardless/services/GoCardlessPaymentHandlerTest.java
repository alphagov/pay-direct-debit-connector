package uk.gov.pay.directdebit.webhook.gocardless.services;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.pay.directdebit.events.exception.GoCardlessEventHasNoPaymentIdException;
import uk.gov.pay.directdebit.events.model.GoCardlessEvent;
import uk.gov.pay.directdebit.gatewayaccounts.model.GoCardlessOrganisationId;
import uk.gov.pay.directdebit.mandate.fixtures.MandateFixture;
import uk.gov.pay.directdebit.notifications.services.UserNotificationService;
import uk.gov.pay.directdebit.payments.fixtures.GatewayAccountFixture;
import uk.gov.pay.directdebit.payments.fixtures.GoCardlessEventFixture;
import uk.gov.pay.directdebit.payments.fixtures.PaymentFixture;
import uk.gov.pay.directdebit.payments.model.Payment;
import uk.gov.pay.directdebit.payments.services.PaymentQueryService;
import uk.gov.pay.directdebit.webhook.gocardless.services.handlers.GoCardlessPaymentHandler;

import java.util.Optional;

import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class GoCardlessPaymentHandlerTest {

    @Mock
    private UserNotificationService mockUserNotificationService;

    @Mock
    private PaymentQueryService mockPaymentQueryService;

    @InjectMocks
    private GoCardlessPaymentHandler goCardlessPaymentHandler;

    private GoCardlessOrganisationId organisationIdentifier = GoCardlessOrganisationId.valueOf("test_org_id");
    private GatewayAccountFixture gatewayAccountFixture = GatewayAccountFixture.aGatewayAccountFixture().withOrganisation(organisationIdentifier);
    private MandateFixture mandateFixture = MandateFixture.aMandateFixture().withGatewayAccountFixture(gatewayAccountFixture);
    private Payment payment = PaymentFixture.aPaymentFixture().withMandateFixture(mandateFixture).toEntity();
    private GoCardlessEventFixture goCardlessEventFixture = GoCardlessEventFixture.aGoCardlessEventFixture().withLinksOrganisation(organisationIdentifier);

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void handle_onFailedPaymentGoCardlessEvent_shouldSendEmail() {
        GoCardlessEvent goCardlessEvent = spy(goCardlessEventFixture.withAction("failed").toEntity());

        when(mockPaymentQueryService.findByGoCardlessPaymentIdAndOrganisationId(goCardlessEvent.getLinksPayment().get(),
                goCardlessEvent.getLinksOrganisation()))
                .thenReturn(Optional.of(payment));

        goCardlessPaymentHandler.handle(goCardlessEvent);

        verify(mockUserNotificationService).sendPaymentFailedEmailFor(payment);
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
