package uk.gov.pay.directdebit.webhook.gocardless.services.handlers;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import uk.gov.pay.directdebit.events.model.GoCardlessEvent;
import uk.gov.pay.directdebit.events.model.GoCardlessResourceType;
import uk.gov.pay.directdebit.notifications.services.UserNotificationService;

import java.util.List;

import static org.mockito.Mockito.verifyZeroInteractions;
import static uk.gov.pay.directdebit.payments.fixtures.GoCardlessEventFixture.aGoCardlessEventFixture;

@RunWith(JUnitParamsRunner.class)
public class SendEmailsForGoCardlessEventsHandlerTest {

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();
    
    @Mock
    private UserNotificationService userNotificationService;
    
    @InjectMocks
    private SendEmailsForGoCardlessEventsHandler goCardlessMandateHandler;

    @Test
    @Parameters({"REFUNDS", "SUBSCRIPTIONS"})
    public void shouldNotSendEmailForEventsWithIrrelevantResource(String resourceType) {
        GoCardlessEvent goCardlessEvent = aGoCardlessEventFixture()
                .withResourceType(GoCardlessResourceType.valueOf(resourceType))
                .withAction("created")
                .toEntity();
        goCardlessMandateHandler.sendEmails(List.of(goCardlessEvent));
        verifyZeroInteractions(userNotificationService);
        verifyZeroInteractions(userNotificationService);
    }

    @Test
    @Parameters({"PAYMENTS", "MANDATES", "PAYOUTS"})
    public void shouldNotSendEmailForEventsWithIrrevelantActions(String resourceType) {
        GoCardlessEvent goCardlessEvent = aGoCardlessEventFixture()
                .withResourceType(GoCardlessResourceType.valueOf(resourceType))
                .withAction("not_handled")
                .toEntity();
        goCardlessMandateHandler.sendEmails(List.of(goCardlessEvent));
        verifyZeroInteractions(userNotificationService);
        verifyZeroInteractions(userNotificationService);
    }
}
