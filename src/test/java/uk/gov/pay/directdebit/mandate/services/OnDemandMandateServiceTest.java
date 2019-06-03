package uk.gov.pay.directdebit.mandate.services;

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.pay.directdebit.gatewayaccounts.model.PaymentProvider;
import uk.gov.pay.directdebit.mandate.api.ConfirmMandateRequest;
import uk.gov.pay.directdebit.mandate.fixtures.MandateFixture;
import uk.gov.pay.directdebit.mandate.model.Mandate;
import uk.gov.pay.directdebit.mandate.model.MandateState;
import uk.gov.pay.directdebit.payers.model.BankAccountDetails;
import uk.gov.pay.directdebit.payments.exception.InvalidStateTransitionException;
import uk.gov.pay.directdebit.payments.fixtures.GatewayAccountFixture;
import uk.gov.pay.directdebit.payments.model.DirectDebitEvent;
import uk.gov.pay.directdebit.payments.model.PaymentProviderFactory;
import uk.gov.pay.directdebit.payments.services.SandboxService;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class OnDemandMandateServiceTest {

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Mock
    private SandboxService mockedSandboxService;
    @Mock
    private MandateService mockedMandateService;
    @Mock
    private MandateStateUpdateService mockedMandateStateUpdateService;
    @Mock
    private PaymentProviderFactory mockedPaymentProviderFactory;

    private GatewayAccountFixture gatewayAccountFixture = GatewayAccountFixture
            .aGatewayAccountFixture().withPaymentProvider(PaymentProvider.SANDBOX);
    private MandateFixture mandateFixture = MandateFixture.aMandateFixture()
            .withState(MandateState.AWAITING_DIRECT_DEBIT_DETAILS)
            .withGatewayAccountFixture(gatewayAccountFixture);
    private ImmutableMap<String, String> createMandateRequest = ImmutableMap
            .of("return_url", "https://blabla.test", "agreement_type", "ON_DEMAND", "service_reference", "ref");
    private ImmutableMap<String, String> confirmMandateRequest = ImmutableMap
            .of("sort_code", "123456", "account_number", "12345678");

    private OnDemandMandateService service;

    @Before
    public void setUp() {
        service = new OnDemandMandateService(mockedPaymentProviderFactory, mockedMandateStateUpdateService);
        when(mockedPaymentProviderFactory.getCommandServiceFor(gatewayAccountFixture.getPaymentProvider())).thenReturn(mockedSandboxService);
    }
    
    @Test
    public void confirm_shouldConfirmOnDemandMandate() {
        Mandate mandate = mandateFixture.toEntity();
        ConfirmMandateRequest mandateConfirmationRequest = ConfirmMandateRequest.of(confirmMandateRequest);
        BankAccountDetails bankAccountDetails = BankAccountDetails.of(confirmMandateRequest);

        when(mockedMandateStateUpdateService.canUpdateStateFor(mandate, DirectDebitEvent.SupportedEvent.DIRECT_DEBIT_DETAILS_CONFIRMED)).thenReturn(true);
        when(mockedSandboxService.confirmOnDemandMandate(mandate, bankAccountDetails)).thenReturn(mandate);
        service.confirm(gatewayAccountFixture.toEntity(), mandate, mandateConfirmationRequest);

        verify(mockedMandateStateUpdateService).confirmedOnDemandDirectDebitDetailsFor(mandate);
    }

    @Test
    public void confirm_shouldNotConfirmOnDemandMandateForInvalidState() {
        Mandate mandate = mandateFixture.withState(MandateState.CANCELLED).toEntity();
        ConfirmMandateRequest mandateConfirmationRequest = ConfirmMandateRequest.of(confirmMandateRequest);

        when(mockedMandateStateUpdateService.canUpdateStateFor(mandate, DirectDebitEvent.SupportedEvent.DIRECT_DEBIT_DETAILS_CONFIRMED)).thenReturn(false);
        expectedException.expect(InvalidStateTransitionException.class);
        expectedException.expectMessage("Transition DIRECT_DEBIT_DETAILS_CONFIRMED from state CANCELLED is not valid");

        service.confirm(gatewayAccountFixture.toEntity(), mandate, mandateConfirmationRequest);
    }
}
