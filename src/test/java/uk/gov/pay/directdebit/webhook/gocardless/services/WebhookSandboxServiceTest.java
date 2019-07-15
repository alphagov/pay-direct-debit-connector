package uk.gov.pay.directdebit.webhook.gocardless.services;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.pay.directdebit.events.model.SandboxEvent;
import uk.gov.pay.directdebit.mandate.model.SandboxMandateId;
import uk.gov.pay.directdebit.payments.model.SandboxPaymentId;
import uk.gov.pay.directdebit.payments.services.sandbox.SandboxPaymentStateUpdater;

import java.util.List;

import static org.mockito.Mockito.verify;
import static uk.gov.pay.directdebit.events.model.SandboxEvent.SandboxEventBuilder.aSandboxEvent;

@RunWith(MockitoJUnitRunner.class)
public class WebhookSandboxServiceTest {
    
    @Mock
    private SandboxPaymentStateUpdater mockSandboxPaymentStateUpdater;

    private WebhookSandboxService webhookSandboxService;

    @Before
    public void setUp() {
        webhookSandboxService = new WebhookSandboxService(mockSandboxPaymentStateUpdater);
    }

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

        webhookSandboxService.updateStateOfPaymentsAffectedByEvents(List.of(
                sandboxPayment1Event,
                anotherSandboxPayment1Event,
                sandboxPayment2Event,
                sandboxMandate1Event
        ));

        verify(mockSandboxPaymentStateUpdater).updateState(sandboxPaymentId1);
        verify(mockSandboxPaymentStateUpdater).updateState(sandboxPaymentId2);
    }

}
