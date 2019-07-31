package uk.gov.pay.directdebit.webhook.gocardless.services.handlers;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.pay.directdebit.events.model.GoCardlessEvent;
import uk.gov.pay.directdebit.mandate.services.MandateQueryService;
import uk.gov.pay.directdebit.payments.services.PaymentQueryService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static uk.gov.pay.directdebit.events.model.GoCardlessResourceType.MANDATES;
import static uk.gov.pay.directdebit.events.model.GoCardlessResourceType.PAYMENTS;
import static uk.gov.pay.directdebit.events.model.GoCardlessResourceType.REFUNDS;
import static uk.gov.pay.directdebit.events.model.GoCardlessResourceType.SUBSCRIPTIONS;
import static uk.gov.pay.directdebit.payments.fixtures.GoCardlessEventFixture.aGoCardlessEventFixture;

@RunWith(MockitoJUnitRunner.class)
public class GoCardlessEventHandlerTest {

    @Mock
    private MandateQueryService mockedMandateQueryService;

    @Mock
    private PaymentQueryService mockPaymentQueryService;

    @InjectMocks
    private GoCardlessEventHandler goCardlessMandateHandler;

    @Test
    public void shouldNotHandleEventsWithRefundsResource() {
        GoCardlessEvent goCardlessEvent = aGoCardlessEventFixture().withResourceType(REFUNDS).withAction("created").toEntity();
        goCardlessMandateHandler.handle(List.of(goCardlessEvent));
        verify(mockedMandateQueryService, never()).findByGoCardlessMandateIdAndOrganisationId(any(), any());
        verify(mockPaymentQueryService, never()).findByGoCardlessPaymentIdAndOrganisationId(any(), any());
    }

    @Test
    public void shouldNotHandleEventsWithSubscriptionsResource() {
        GoCardlessEvent goCardlessEvent = aGoCardlessEventFixture().withResourceType(SUBSCRIPTIONS).withAction("created").toEntity();
        goCardlessMandateHandler.handle(List.of(goCardlessEvent));
        verify(mockedMandateQueryService, never()).findByGoCardlessMandateIdAndOrganisationId(any(), any());
        verify(mockPaymentQueryService, never()).findByGoCardlessPaymentIdAndOrganisationId(any(), any());
    }

    @Test
    public void shouldNotHandlePaymentEventsWithAnUnhandledAction() {
        GoCardlessEvent goCardlessEvent = aGoCardlessEventFixture().withResourceType(PAYMENTS).withAction("not_handled").toEntity();
        goCardlessMandateHandler.handle(List.of(goCardlessEvent));
        verify(mockedMandateQueryService, never()).findByGoCardlessMandateIdAndOrganisationId(any(), any());
        verify(mockPaymentQueryService, never()).findByGoCardlessPaymentIdAndOrganisationId(any(), any());
    }

    @Test
    public void shouldNotHandleMandateEventsWithAnUnhandledAction() {
        GoCardlessEvent goCardlessEvent = aGoCardlessEventFixture().withResourceType(MANDATES).withAction("not_handled_again").toEntity();
        goCardlessMandateHandler.handle(List.of(goCardlessEvent));
        verify(mockedMandateQueryService, never()).findByGoCardlessMandateIdAndOrganisationId(any(), any());
        verify(mockPaymentQueryService, never()).findByGoCardlessPaymentIdAndOrganisationId(any(), any());
    }
}
