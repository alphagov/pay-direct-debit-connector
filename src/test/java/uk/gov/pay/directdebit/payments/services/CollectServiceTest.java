package uk.gov.pay.directdebit.payments.services;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import uk.gov.pay.directdebit.gatewayaccounts.model.GatewayAccount;
import uk.gov.pay.directdebit.mandate.exception.MandateNotFoundException;
import uk.gov.pay.directdebit.mandate.exception.MandateStateInvalidException;
import uk.gov.pay.directdebit.mandate.fixtures.MandateFixture;
import uk.gov.pay.directdebit.mandate.model.MandateState;
import uk.gov.pay.directdebit.mandate.model.PaymentProviderMandateId;
import uk.gov.pay.directdebit.mandate.model.SandboxMandateId;
import uk.gov.pay.directdebit.mandate.model.subtype.MandateExternalId;
import uk.gov.pay.directdebit.mandate.services.MandateQueryService;
import uk.gov.pay.directdebit.payments.api.CollectPaymentRequest;
import uk.gov.pay.directdebit.payments.exception.MandateNotSubmittedToProviderException;
import uk.gov.pay.directdebit.payments.model.Payment;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.BDDMockito.given;
import static uk.gov.pay.directdebit.mandate.fixtures.MandateFixture.aMandateFixture;
import static uk.gov.pay.directdebit.payments.fixtures.GatewayAccountFixture.aGatewayAccountFixture;

@RunWith(JUnitParamsRunner.class)
public class CollectServiceTest {

    private static final String GATEWAY_ACCOUNT_EXTERNAL_ID = "xyzzy";
    private static final MandateExternalId MANDATE_EXTERNAL_ID = MandateExternalId.valueOf("mandy");
    private static final PaymentProviderMandateId PAYMENT_PROVIDER_MANDATE_ID = SandboxMandateId.valueOf("provider-mandate-id");
    private static final String DESCRIPTION = "The best payment of all time";
    private static final String REFERENCE = "ref";
    private static final long AMOUNT = 12345L;

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Mock
    private MandateQueryService mockMandateQueryService;

    @Mock
    private PaymentService mockPaymentService;

    @Mock
    private Payment mockCreatedPayment, mockPendingPayment;

    private GatewayAccount gatewayAccount = aGatewayAccountFixture().withExternalId(GATEWAY_ACCOUNT_EXTERNAL_ID).toEntity();
    
    private MandateFixture mandateFixture = aMandateFixture().withExternalId(MANDATE_EXTERNAL_ID);

    private CollectPaymentRequest collectPaymentRequest = new CollectPaymentRequest(MANDATE_EXTERNAL_ID, AMOUNT, DESCRIPTION, REFERENCE);

    private CollectService collectService;

    @Before
    public void setUp() {
        this.collectService = new CollectService(mockMandateQueryService, mockPaymentService);
    }

    @Test
    @Parameters({"SUBMITTED_TO_PROVIDER", "SUBMITTED_TO_BANK", "ACTIVE"})
    public void collectInValidMandateStateCreatesAndCollectsPaymentReturningPendingPayment(String mandateState) {
        var mandate = aMandateFixture().withPaymentProviderId(PAYMENT_PROVIDER_MANDATE_ID).withState(MandateState.valueOf(mandateState)).toEntity();
    
        given(mockMandateQueryService.findByExternalIdAndGatewayAccountExternalId(MANDATE_EXTERNAL_ID, GATEWAY_ACCOUNT_EXTERNAL_ID)).willReturn(mandate);
        given(mockPaymentService.createPayment(AMOUNT, DESCRIPTION, REFERENCE, mandate)).willReturn(mockCreatedPayment);
        given(mockPaymentService.submitPaymentToProvider(mockCreatedPayment, PAYMENT_PROVIDER_MANDATE_ID)).willReturn(mockPendingPayment);

        var payment = collectService.collect(gatewayAccount, collectPaymentRequest);

        assertThat(payment, is(mockPendingPayment));
    }

    @Test(expected = MandateStateInvalidException.class)
    @Parameters({
            "CREATED", 
            "AWAITING_DIRECT_DEBIT_DETAILS", 
            "USER_SETUP_CANCELLED",
            "USER_SETUP_EXPIRED",
            "FAILED",
            "CANCELLED",
            "EXPIRED"})
    public void collectPaymentFailureForInvalidMandateStates(String mandateState) {
        var mandate = aMandateFixture().withPaymentProviderId(PAYMENT_PROVIDER_MANDATE_ID).withState(MandateState.valueOf(mandateState)).toEntity();

        given(mockMandateQueryService.findByExternalIdAndGatewayAccountExternalId(MANDATE_EXTERNAL_ID, GATEWAY_ACCOUNT_EXTERNAL_ID)).willReturn(mandate);
        
        collectService.collect(gatewayAccount, collectPaymentRequest);
    }

    @Test(expected = MandateNotFoundException.class)
    public void collectThrowsExceptionIfMandateNotFound() {
        given(mockMandateQueryService.findByExternalIdAndGatewayAccountExternalId(MANDATE_EXTERNAL_ID, GATEWAY_ACCOUNT_EXTERNAL_ID))
                .willThrow(MandateNotFoundException.class);

        collectService.collect(gatewayAccount, collectPaymentRequest);
    }

    @Test(expected = MandateNotSubmittedToProviderException.class)
    public void collectThrowsExceptionIfMandateHasNoProviderId() {
        var mandate = mandateFixture.withState(MandateState.SUBMITTED_TO_PROVIDER).toEntity();

        given(mockMandateQueryService.findByExternalIdAndGatewayAccountExternalId(MANDATE_EXTERNAL_ID, GATEWAY_ACCOUNT_EXTERNAL_ID)).willReturn(mandate);

        collectService.collect(gatewayAccount, collectPaymentRequest);
    }

}
