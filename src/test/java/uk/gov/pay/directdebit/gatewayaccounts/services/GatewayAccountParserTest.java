package uk.gov.pay.directdebit.gatewayaccounts.services;

import com.google.common.collect.ImmutableMap;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import uk.gov.pay.directdebit.gatewayaccounts.exception.InvalidGatewayAccountTypeException;
import uk.gov.pay.directdebit.gatewayaccounts.exception.InvalidPaymentProviderException;
import uk.gov.pay.directdebit.gatewayaccounts.model.GatewayAccount;
import uk.gov.pay.directdebit.gatewayaccounts.model.PaymentProvider;

import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

public class GatewayAccountParserTest {
    private static final PaymentProvider PAYMENT_PROVIDER = PaymentProvider.SANDBOX;
    private static final String SERVICE_NAME = "alex";
    private static final GatewayAccount.Type TYPE = GatewayAccount.Type.TEST;
    private static final String DESCRIPTION = "is awesome";
    private static final String ANALYTICS_ID = "DD_234098_BBBLA";

    private GatewayAccountParser parser = new GatewayAccountParser();

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void shouldCreateGatewayAccountFromFullRequest() {
        Map<String, String> request = ImmutableMap.of(
                "payment_provider", PAYMENT_PROVIDER.toString(),
                "service_name", SERVICE_NAME,
                "type", TYPE.toString(),
                "description", DESCRIPTION,
                "analytics_id", ANALYTICS_ID
        );
        GatewayAccount gatewayAccount = parser.parse(request);
        assertThat(gatewayAccount.getPaymentProvider(), is(PAYMENT_PROVIDER));
        assertThat(gatewayAccount.getType(), is(TYPE));
        assertThat(gatewayAccount.getServiceName(), is(SERVICE_NAME));
        assertThat(gatewayAccount.getAnalyticsId(), is(ANALYTICS_ID));
        assertThat(gatewayAccount.getDescription(), is(DESCRIPTION));
    }

    @Test
    public void shouldCreateGatewayAccountFromMinimalRequest() {
        Map<String, String> request = ImmutableMap.of(
                "payment_provider", PAYMENT_PROVIDER.toString(),
                "service_name", SERVICE_NAME,
                "type", TYPE.toString()
        );
        GatewayAccount gatewayAccount = parser.parse(request);
        assertThat(gatewayAccount.getPaymentProvider(), is(PAYMENT_PROVIDER));
        assertThat(gatewayAccount.getType(), is(TYPE));
        assertThat(gatewayAccount.getServiceName(), is(SERVICE_NAME));
        assertThat(gatewayAccount.getAnalyticsId(), is(nullValue()));
        assertThat(gatewayAccount.getDescription(), is(nullValue()));
    }

    @Test
    public void shouldThrowIfTypeIsNotSupported() {
        Map<String, String> request = ImmutableMap.of(
          "payment_provider", "SANDBOX",
          "service_name", "blabla",
          "type", "something"
        );
        thrown.expect(InvalidGatewayAccountTypeException.class);
        thrown.expectMessage("Unsupported gateway account type: something");
        parser.parse(request);
    }

    @Test
    public void shouldThrowIfPaymentProviderIsNotSupported() {
        Map<String, String> request = ImmutableMap.of(
                "payment_provider", "something",
                "service_name", "blabla",
                "type", "TEST"
        );
        thrown.expect(InvalidPaymentProviderException.class);
        thrown.expectMessage("Unsupported payment provider: something");
        parser.parse(request);
    }
}
