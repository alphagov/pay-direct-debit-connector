package uk.gov.pay.directdebit.payments.services;

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.pay.directdebit.gatewayaccounts.model.GatewayAccount;
import uk.gov.pay.directdebit.mandate.model.ConfirmationDetails;
import uk.gov.pay.directdebit.mandate.services.PaymentConfirmService;
import uk.gov.pay.directdebit.payers.services.PayerService;

import java.util.Map;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.pay.directdebit.mandate.fixtures.MandateFixture.aMandateFixture;
import static uk.gov.pay.directdebit.payments.fixtures.ConfirmationDetailsFixture.confirmationDetails;
import static uk.gov.pay.directdebit.payments.fixtures.GatewayAccountFixture.aGatewayAccountFixture;
import static uk.gov.pay.directdebit.payments.fixtures.TransactionFixture.aTransactionFixture;

@RunWith(MockitoJUnitRunner.class)
public class SandboxServiceTest {

    @Mock
    private PayerService mockedPayerService;
    @Mock
    private PaymentConfirmService mockedPaymentConfirmService;
    @Mock
    private TransactionService mockedTransactionService;

    private SandboxService service;
    private String paymentRequestExternalId = "sdkfhsdkjfhjdks";

    private GatewayAccount gatewayAccount = aGatewayAccountFixture().toEntity();
    @Before
    public void setUp() {
        service = new SandboxService(mockedPayerService, mockedPaymentConfirmService, mockedTransactionService);
    }

    @Test
    public void createPayer_shouldCreatePayerWhenReceivingPayerRequest() {
        Map<String, String> createPayerRequest = ImmutableMap.of();
        service.createPayer(paymentRequestExternalId, gatewayAccount, createPayerRequest);
        verify(mockedPayerService).create(paymentRequestExternalId, gatewayAccount.getId(), createPayerRequest);
    }

    @Test
    public void confirm_shouldRegisterAPaymentCreatedEventWhenSuccessfullyConfirmed() {

        ConfirmationDetails confirmationDetails = confirmationDetails()
                .withTransaction(aTransactionFixture())
                .withMandate(aMandateFixture())
                .build();

        when(mockedPaymentConfirmService.confirm(gatewayAccount.getId(), paymentRequestExternalId))
                .thenReturn(confirmationDetails);

        service.confirm(paymentRequestExternalId, gatewayAccount);

        verify(mockedTransactionService).paymentCreatedFor(confirmationDetails.getTransaction());
    }
}
