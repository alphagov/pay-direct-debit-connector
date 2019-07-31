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
import uk.gov.pay.directdebit.events.model.GoCardlessResourceType;
import uk.gov.pay.directdebit.gatewayaccounts.model.GoCardlessOrganisationId;
import uk.gov.pay.directdebit.mandate.fixtures.MandateFixture;
import uk.gov.pay.directdebit.notifications.services.UserNotificationService;
import uk.gov.pay.directdebit.payments.fixtures.GatewayAccountFixture;
import uk.gov.pay.directdebit.payments.fixtures.GoCardlessEventFixture;
import uk.gov.pay.directdebit.payments.fixtures.PaymentFixture;
import uk.gov.pay.directdebit.payments.model.Payment;
import uk.gov.pay.directdebit.payments.services.PaymentQueryService;
import uk.gov.pay.directdebit.webhook.gocardless.services.handlers.SendEmailsForGoCardlessEventsHandler;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SendEmailsForGoCardlessPaymentEventsHandlerTest {

    @Mock
    private UserNotificationService mockUserNotificationService;

    @Mock
    private PaymentQueryService mockPaymentQueryService;
    
    @InjectMocks
    private SendEmailsForGoCardlessEventsHandler sendEmailsForGoCardlessEventsHandler;

    private GoCardlessOrganisationId organisationIdentifier = GoCardlessOrganisationId.valueOf("test_org_id");
    private GatewayAccountFixture gatewayAccountFixture = GatewayAccountFixture.aGatewayAccountFixture().withOrganisation(organisationIdentifier);
    private MandateFixture mandateFixture = MandateFixture.aMandateFixture().withGatewayAccountFixture(gatewayAccountFixture);
    private Payment payment = PaymentFixture.aPaymentFixture().withMandateFixture(mandateFixture).toEntity();
    private GoCardlessEventFixture goCardlessEventFixture = GoCardlessEventFixture.aGoCardlessEventFixture()
            .withResourceType(GoCardlessResourceType.PAYMENTS)
            .withLinksOrganisation(organisationIdentifier);

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void assertEmailIsSentForFailedPaymentGoCardlessEvent() {
        GoCardlessEvent goCardlessEvent = goCardlessEventFixture.withAction("failed").toEntity();

        when(mockPaymentQueryService.findByGoCardlessPaymentIdAndOrganisationId(goCardlessEvent.getLinksPayment().get(),
                goCardlessEvent.getLinksOrganisation()))
                .thenReturn(Optional.of(payment));

        sendEmailsForGoCardlessEventsHandler.handle(List.of(goCardlessEvent));

        verify(mockUserNotificationService).sendPaymentFailedEmailFor(payment);
    }

    @Test
    public void assertEmailIsSentForCreatePaymentGoCardlessEvent_shouldThrowExceptionWhenEventHasNoLinkedPayment() {
        GoCardlessEvent goCardlessEvent = goCardlessEventFixture
                .withLinksOrganisation(GoCardlessOrganisationId.valueOf("does_not_exist"))
                .withAction("created")
                .withLinksPayment(null)
                .toEntity();

        thrown.expect(GoCardlessEventHasNoPaymentIdException.class);
        sendEmailsForGoCardlessEventsHandler.handle(List.of(goCardlessEvent));
        verify(mockUserNotificationService, never()).sendPaymentFailedEmailFor(any());
    }

}
