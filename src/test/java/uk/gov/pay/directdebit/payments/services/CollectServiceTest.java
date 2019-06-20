package uk.gov.pay.directdebit.payments.services;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.pay.directdebit.gatewayaccounts.model.GatewayAccount;
import uk.gov.pay.directdebit.mandate.exception.MandateNotFoundException;
import uk.gov.pay.directdebit.mandate.fixtures.MandateFixture;
import uk.gov.pay.directdebit.mandate.model.Mandate;
import uk.gov.pay.directdebit.mandate.model.subtype.MandateExternalId;
import uk.gov.pay.directdebit.mandate.services.MandateQueryService;
import uk.gov.pay.directdebit.payments.api.CollectPaymentRequest;
import uk.gov.pay.directdebit.payments.fixtures.GatewayAccountFixture;
import uk.gov.pay.directdebit.payments.model.Payment;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.BDDMockito.given;

@RunWith(MockitoJUnitRunner.class)
public class CollectServiceTest {

    private static final String GATEWAY_ACCOUNT_EXTERNAL_ID = "xyzzy";
    private static final MandateExternalId MANDATE_EXTERNAL_ID = MandateExternalId.valueOf("mandy");
    private static final String DESCRIPTION = "The best payment of all time";
    private static final String REFERENCE = "ref";
    private static final long AMOUNT = 12345L;

    @Mock
    private MandateQueryService mockMandateQueryService;

    @Mock
    private PaymentService mockPaymentService;

    @Mock
    private Payment mockCreatedPayment, mockPendingPayment;

    private GatewayAccount gatewayAccount = GatewayAccountFixture.aGatewayAccountFixture().withExternalId(GATEWAY_ACCOUNT_EXTERNAL_ID).toEntity();

    private Mandate mandate = MandateFixture.aMandateFixture().withExternalId(MANDATE_EXTERNAL_ID).toEntity();
    
    private CollectPaymentRequest collectPaymentRequest = new CollectPaymentRequest(MANDATE_EXTERNAL_ID, AMOUNT, DESCRIPTION, REFERENCE);
    
    private CollectService collectService;

    @Before
    public void setUp() {
        this.collectService = new CollectService(mockMandateQueryService, mockPaymentService);
    }

    @Test
    public void collectCreatesAndCollectsPaymentReturningPendingPayment() {
        given(mockMandateQueryService.findByExternalIdAndGatewayAccountExternalId(MANDATE_EXTERNAL_ID, GATEWAY_ACCOUNT_EXTERNAL_ID)).willReturn(mandate);
        given(mockPaymentService.createPayment(AMOUNT, DESCRIPTION, REFERENCE, mandate)).willReturn(mockCreatedPayment);
        given(mockPaymentService.submitPaymentToProvider(mockCreatedPayment)).willReturn(mockPendingPayment);

        Payment payment = collectService.collect(gatewayAccount, collectPaymentRequest);
        
        assertThat(payment, is(mockPendingPayment));
    }

    @Test(expected = MandateNotFoundException.class)
    public void collectThrowsExceptionIfMandateNotFound() {
        given(mockMandateQueryService.findByExternalIdAndGatewayAccountExternalId(MANDATE_EXTERNAL_ID, GATEWAY_ACCOUNT_EXTERNAL_ID))
                .willThrow(MandateNotFoundException.class);

        collectService.collect(gatewayAccount, collectPaymentRequest);
    }

}
