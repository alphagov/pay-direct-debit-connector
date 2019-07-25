package uk.gov.pay.directdebit.webhook.gocardless.services;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.pay.directdebit.events.model.SandboxEvent;
import uk.gov.pay.directdebit.mandate.model.SandboxMandateId;
import uk.gov.pay.directdebit.payments.model.Payment;
import uk.gov.pay.directdebit.payments.model.SandboxPaymentId;
import uk.gov.pay.directdebit.payments.services.PaymentQueryService;
import uk.gov.pay.directdebit.payments.services.PaymentStateUpdater;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.pay.directdebit.events.model.SandboxEvent.SandboxEventBuilder.aSandboxEvent;

@RunWith(MockitoJUnitRunner.class)
public class WebhookSandboxServiceTest {
    
    @Mock
    private PaymentStateUpdater mockPaymentStateUpdater;
    
    @Mock
    private PaymentQueryService paymentQueryService;

    @InjectMocks
    private WebhookSandboxService webhookSandboxService;

    @Test
    public void shouldUpdateStatesForPaymentsAffectedByEvents() {
        var sandboxPaymentId1 = SandboxPaymentId.valueOf("Sandbox Payment 1");
        var sandboxPaymentId2 = SandboxPaymentId.valueOf("Sandbox Payment 2");
        var sandboxMandateId1 = SandboxMandateId.valueOf("Sandbox Mandate 1");

        SandboxEvent sandboxPayment1Event = aSandboxEvent()
                .withPaymentId(sandboxPaymentId1)
                .build();

        SandboxEvent anotherSandboxPayment1Event = aSandboxEvent()
                .withPaymentId(sandboxPaymentId1)
                .build();

        SandboxEvent sandboxPayment2Event = aSandboxEvent()
                .withPaymentId(sandboxPaymentId2)
                .build();

        SandboxEvent sandboxMandate1Event = aSandboxEvent()
                .withMandateId(sandboxMandateId1)
                .build();
        
        Payment payment1 = mock(Payment.class);
        Payment payment2 = mock(Payment.class);
        
        when(paymentQueryService.findBySandboxPaymentId(sandboxPaymentId1)).thenReturn(Optional.of(payment1));
        when(paymentQueryService.findBySandboxPaymentId(sandboxPaymentId2)).thenReturn(Optional.of(payment2));
        
        webhookSandboxService.updateStateOfPaymentsAffectedByEvents(List.of(
                sandboxPayment1Event,
                anotherSandboxPayment1Event,
                sandboxPayment2Event,
                sandboxMandate1Event
        ));

        verify(mockPaymentStateUpdater).updateStateIfNecessary(payment1);
        verify(mockPaymentStateUpdater).updateStateIfNecessary(payment2);
    }

}
