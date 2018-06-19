package uk.gov.pay.directdebit.gatewayaccounts.services;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.pay.directdebit.gatewayaccounts.dao.GatewayAccountDao;
import uk.gov.pay.directdebit.gatewayaccounts.exception.GatewayAccountNotFoundException;
import uk.gov.pay.directdebit.gatewayaccounts.model.GatewayAccount;
import uk.gov.pay.directdebit.gatewayaccounts.model.PaymentProvider;
import uk.gov.pay.directdebit.mandate.fixtures.MandateFixture;
import uk.gov.pay.directdebit.payments.fixtures.GatewayAccountFixture;
import uk.gov.pay.directdebit.payments.fixtures.TransactionFixture;
import uk.gov.pay.directdebit.payments.model.Transaction;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.pay.directdebit.payments.fixtures.GatewayAccountFixture.aGatewayAccountFixture;

@RunWith(MockitoJUnitRunner.class)
public class GatewayAccountServiceTest {
    private static final PaymentProvider PAYMENT_PROVIDER = PaymentProvider.SANDBOX;
    private static final String SERVICE_NAME = "alex";
    private static final String DESCRIPTION = "is awesome";
    private static final String ANALYTICS_ID = "DD_234098_BBBLA";
    private static final GatewayAccount.Type TYPE = GatewayAccount.Type.TEST;

    private GatewayAccountFixture gatewayAccountFixture = aGatewayAccountFixture()
                .withPaymentProvider(PAYMENT_PROVIDER)
                        .withServiceName(SERVICE_NAME)
                        .withDescription(DESCRIPTION)
                        .withType(TYPE)
                        .withAnalyticsId(ANALYTICS_ID);

    private MandateFixture mandateFixture = MandateFixture.aMandateFixture()
            .withGatewayAccountFixture(gatewayAccountFixture);
    @Mock
    private GatewayAccountDao mockedGatewayAccountDao;

    @Mock
    private GatewayAccountParser mockedGatewayAccountParser;

    private GatewayAccountService service;

    private Map<String, String> createTransactionRequest = new HashMap<>();
    private Transaction transaction = TransactionFixture.aTransactionFixture().withMandateFixture(mandateFixture).toEntity();
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setUp() {
        service = new GatewayAccountService(mockedGatewayAccountDao, mockedGatewayAccountParser);
    }

    @Test
    public void shouldReturnGatewayAccountIfItExists() {
        String accountId = "10sadsadsadL";
        when(mockedGatewayAccountDao.findByExternalId(accountId)).thenReturn(Optional.of(gatewayAccountFixture.toEntity()));
        GatewayAccount gatewayAccount = service.getGatewayAccountForId(accountId);
        assertThat(gatewayAccount.getId(), is(gatewayAccountFixture.getId()));
        assertThat(gatewayAccount.getPaymentProvider(), is(PAYMENT_PROVIDER));
        assertThat(gatewayAccount.getAnalyticsId(), is(ANALYTICS_ID));
        assertThat(gatewayAccount.getDescription(), is(DESCRIPTION));
        assertThat(gatewayAccount.getServiceName(), is(SERVICE_NAME));
        assertThat(gatewayAccount.getType(), is(TYPE));
    }

    @Test
    public void shouldThrowIfGatewayAccountDoesNotExist() {
        String accountId = "10sadsadsadL";
        when(mockedGatewayAccountDao.findByExternalId(accountId)).thenReturn(Optional.empty());
        thrown.expect(GatewayAccountNotFoundException.class);
        thrown.expectMessage("Unknown gateway account: 10");
        thrown.reportMissingExceptionWithMessage("GatewayAccountNotFoundException expected");
        service.getGatewayAccountForId(accountId);
    }

    @Test
    public void shouldReturnGatewayAccountForTransactionIfItExists() {
        when(mockedGatewayAccountDao.findById(gatewayAccountFixture.getId()))
                .thenReturn(Optional.of(gatewayAccountFixture.toEntity()));
        GatewayAccount gatewayAccount = service.getGatewayAccountFor(transaction);
        assertThat(gatewayAccount.getId(), is(gatewayAccountFixture.getId()));
        assertThat(gatewayAccount.getPaymentProvider(), is(PAYMENT_PROVIDER));
        assertThat(gatewayAccount.getAnalyticsId(), is(ANALYTICS_ID));
        assertThat(gatewayAccount.getDescription(), is(DESCRIPTION));
        assertThat(gatewayAccount.getServiceName(), is(SERVICE_NAME));
        assertThat(gatewayAccount.getType(), is(TYPE));
    }

    @Test
    public void shouldThrowIfGatewayAccountForTransactionDoesNotExist() {
        when(mockedGatewayAccountDao.findById(gatewayAccountFixture.getId()))
                .thenReturn(Optional.empty());
        thrown.expect(GatewayAccountNotFoundException.class);
        thrown.expectMessage("Unknown gateway account: " + gatewayAccountFixture.getId());
        thrown.reportMissingExceptionWithMessage("GatewayAccountNotFoundException expected");
        service.getGatewayAccountFor(transaction);
    }

    @Test
    public void shouldReturnAListOfGatewayAccounts() {
        when(mockedGatewayAccountDao.findAll())
                .thenReturn(Arrays.asList(gatewayAccountFixture.toEntity(),
                        gatewayAccountFixture.toEntity(),
                        gatewayAccountFixture.toEntity()));

        List<GatewayAccount> gatewayAccounts = service.getAllGatewayAccounts();

        assertThat(gatewayAccounts.size(), is(3));
    }

    @Test
    public void shouldStoreAGatewayAccount() {
        GatewayAccount parsedGatewayAccount = GatewayAccountFixture.aGatewayAccountFixture().toEntity();
        when(mockedGatewayAccountParser.parse(createTransactionRequest)).thenReturn(parsedGatewayAccount);
        service.create(createTransactionRequest);
        verify(mockedGatewayAccountDao).insert(parsedGatewayAccount);
    }

}
