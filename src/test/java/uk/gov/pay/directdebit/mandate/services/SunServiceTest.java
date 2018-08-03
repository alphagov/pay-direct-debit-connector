package uk.gov.pay.directdebit.mandate.services;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.pay.directdebit.common.model.subtype.SunName;
import uk.gov.pay.directdebit.common.services.SunService;
import uk.gov.pay.directdebit.gatewayaccounts.model.PaymentProvider;
import uk.gov.pay.directdebit.mandate.fixtures.MandateFixture;
import uk.gov.pay.directdebit.mandate.model.Mandate;
import uk.gov.pay.directdebit.payments.fixtures.GatewayAccountFixture;
import uk.gov.pay.directdebit.payments.model.DirectDebitPaymentProviderCommandService;
import uk.gov.pay.directdebit.payments.model.PaymentProviderFactory;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SunServiceTest {

    @Mock
    private PaymentProviderFactory mockPaymentProviderFactory;
    @Mock
    private DirectDebitPaymentProviderCommandService mockDirectDebitPaymentProviderCommandService;
    private SunService sunService;

    @Before
    public void setUp() {
        sunService = new SunService(mockPaymentProviderFactory);
    }

    @Test
    public void shouldReturnSunNameForGoCardless() {
        GatewayAccountFixture gatewayAccountFixture =
                GatewayAccountFixture.aGatewayAccountFixture().withPaymentProvider(PaymentProvider.GOCARDLESS);
        Mandate mandate = MandateFixture.aMandateFixture().withGatewayAccountFixture(gatewayAccountFixture).toEntity();
        SunName sunName = SunName.of("BACS Test SUN Name");
        when(mockPaymentProviderFactory.getCommandServiceFor(mandate.getGatewayAccount().getPaymentProvider()))
                .thenReturn(mockDirectDebitPaymentProviderCommandService);
        when(mockDirectDebitPaymentProviderCommandService.getSunName(mandate)).thenReturn(Optional.of(sunName));
        assertThat(sunService.getSunNameFor(mandate), is(Optional.of(sunName)));
    }

    @Test
    public void shouldReturnSunNameForSandbox() {
        GatewayAccountFixture gatewayAccountFixture =
                GatewayAccountFixture.aGatewayAccountFixture().withPaymentProvider(PaymentProvider.SANDBOX);
        Mandate mandate = MandateFixture.aMandateFixture().withGatewayAccountFixture(gatewayAccountFixture).toEntity();
        SunName sunName = SunName.of("Sandbox SUN Name");
        when(mockPaymentProviderFactory.getCommandServiceFor(mandate.getGatewayAccount().getPaymentProvider()))
                .thenReturn(mockDirectDebitPaymentProviderCommandService);
        when(mockDirectDebitPaymentProviderCommandService.getSunName(mandate)).thenReturn(Optional.of(sunName));
        assertThat(sunService.getSunNameFor(mandate), is(Optional.of(sunName)));
    }
}
