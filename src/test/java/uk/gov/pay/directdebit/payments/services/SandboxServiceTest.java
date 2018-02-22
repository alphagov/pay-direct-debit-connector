package uk.gov.pay.directdebit.payments.services;

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.pay.directdebit.gatewayaccounts.model.GatewayAccount;
import uk.gov.pay.directdebit.payers.services.PayerService;

import java.util.Map;

import static org.mockito.Mockito.verify;
import static uk.gov.pay.directdebit.payments.fixtures.GatewayAccountFixture.aGatewayAccountFixture;

@RunWith(MockitoJUnitRunner.class)
public class SandboxServiceTest {

    @Mock
    PayerService mockedPayerService;

    private SandboxService service;
    private String paymentRequestExternalId = "sdkfhsdkjfhjdks";

    private GatewayAccount gatewayAccount = aGatewayAccountFixture().toEntity();
    @Before
    public void setUp() {
        service = new SandboxService(mockedPayerService);
    }

    @Test
    public void shouldDelegateToPayerServiceWhenReceivingPayerRequest() {
        Map<String, String> createPayerRequest = ImmutableMap.of();
        service.createPayer(paymentRequestExternalId, gatewayAccount, createPayerRequest);
        verify(mockedPayerService).create(paymentRequestExternalId, gatewayAccount.getId(), createPayerRequest);
    }
}
