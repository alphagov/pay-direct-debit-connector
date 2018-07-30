package uk.gov.pay.directdebit.common.clients;

import com.gocardless.GoCardlessClient;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.pay.directdebit.app.config.DirectDebitConfig;
import uk.gov.pay.directdebit.gatewayaccounts.model.PaymentProviderAccessToken;

import javax.net.ssl.SSLSocketFactory;
import java.util.Optional;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class GoCardlessClientFactoryTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private DirectDebitConfig mockedDirectDebitConfig;
    @Mock
    private SSLSocketFactory mockedSSLSocketFactory;
    private GoCardlessClientFactory goCardlessClientFactory;

    @Before
    public void setUp() {
        when(mockedDirectDebitConfig.getGoCardless().getAccessToken()).thenReturn("aaa");
        when(mockedDirectDebitConfig.getProxyConfig().getHost()).thenReturn("aaa");
        when(mockedDirectDebitConfig.getProxyConfig().getPort()).thenReturn(0);
        when(mockedDirectDebitConfig.getGoCardless().getEnvironment()).thenReturn(GoCardlessClient.Environment.SANDBOX);
        goCardlessClientFactory = new GoCardlessClientFactory(mockedDirectDebitConfig, mockedSSLSocketFactory);
    }

    @Test
    public void shouldCreateOnlyOneClientPerGatewayAccount() {
        GoCardlessClientFacade firstClient = goCardlessClientFactory
                .getClientFor(Optional.of(PaymentProviderAccessToken.of("accessToken")));
        GoCardlessClientFacade secondClient = goCardlessClientFactory
                .getClientFor(Optional.of(PaymentProviderAccessToken.of("accessToken")));
        assertThat(firstClient, is(secondClient));
    }


    //backward compatibility, please remove once all gateway accounts have an access token
    @Test
    public void shouldCreateAClient_ifNoAccessTokenIsDefined() {
        GoCardlessClientFacade client = goCardlessClientFactory
                .getClientFor(Optional.empty());
        GoCardlessClientFacade clientWithConfigAccessToken = goCardlessClientFactory
                .getClientFor(Optional.of(PaymentProviderAccessToken.of("aaa")));
        assertThat(client, is(clientWithConfigAccessToken));
    }
}
