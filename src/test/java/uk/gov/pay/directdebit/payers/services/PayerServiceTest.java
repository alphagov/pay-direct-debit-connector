package uk.gov.pay.directdebit.payers.services;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Optional;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.pay.directdebit.gatewayaccounts.model.GatewayAccount;
import uk.gov.pay.directdebit.mandate.exception.PayerNotFoundException;
import uk.gov.pay.directdebit.mandate.fixtures.MandateFixture;
import uk.gov.pay.directdebit.mandate.model.Mandate;
import uk.gov.pay.directdebit.mandate.services.MandateService;
import uk.gov.pay.directdebit.payers.api.PayerParser;
import uk.gov.pay.directdebit.payers.dao.PayerDao;
import uk.gov.pay.directdebit.payers.fixtures.PayerFixture;
import uk.gov.pay.directdebit.payers.model.Payer;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.pay.directdebit.payments.fixtures.GatewayAccountFixture.aGatewayAccountFixture;

@RunWith(MockitoJUnitRunner.class)
public class PayerServiceTest {

    @Mock
    PayerDao mockedPayerDao;
    @Mock
    MandateService mockedMandateService;
    @Mock
    PayerParser mockedPayerParser;

    private PayerService service;

    private final String SORT_CODE = "123456";
    private final String ACCOUNT_NUMBER = "12345678";
    private final String BANK_NAME = "bank name";
    private final Map<String, String> createPayerRequest = ImmutableMap.of(
            "sort_code", SORT_CODE,
            "account_number", ACCOUNT_NUMBER,
            "bank_name", BANK_NAME
    );
    private String mandateExternalId = "sdkfhsdkjfhjdks";

    private Payer payer = PayerFixture.aPayerFixture()
            .withName("mr payment").toEntity();
    private GatewayAccount gatewayAccount = aGatewayAccountFixture().toEntity();
    private MandateFixture mandateFixture = MandateFixture.aMandateFixture().withExternalId(mandateExternalId);
    
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setUp() {
        service = new PayerService(mockedPayerDao, mockedMandateService, mockedPayerParser);

        when(mockedMandateService.receiveDirectDebitDetailsFor(mandateExternalId)).thenReturn(mandateFixture.toEntity());
        when(mockedPayerParser.parse(createPayerRequest, mandateFixture.getId())).thenReturn(payer);
    }

    @Test
    public void shouldCreateAndStoreAPayerWhenReceivingCreatePayerRequest() {
        service.createOrUpdatePayer(mandateExternalId, gatewayAccount, createPayerRequest);
        Mandate mandate = mandateFixture.toEntity();
        verify(mockedPayerDao).insert(payer);
        verify(mockedMandateService).payerCreatedFor(mandate);
        verify(mockedMandateService, never()).payerEditedFor(mandate);
    }
    @Test
    public void shouldUpdateAndStoreAPayerWhenReceivingCreatePayerRequest_ifAPayerAlreadyExists() {
        Payer originalPayer = PayerFixture.aPayerFixture()
                .withName("mr payment").toEntity();
        Mandate mandate = mandateFixture.toEntity();

        when(mockedPayerDao.findByMandateId(mandate.getId()))
                .thenReturn(Optional.of(originalPayer));

        Payer editedPayer = mock(Payer.class);
        when(mockedPayerParser.parse(createPayerRequest, mandateFixture.getId())).thenReturn(editedPayer);

        service.createOrUpdatePayer(mandateExternalId, gatewayAccount, createPayerRequest);
        verify(mockedPayerDao).updatePayerDetails(originalPayer.getId(), editedPayer);
        verify(mockedMandateService).payerEditedFor(mandate);
        verify(mockedMandateService, never()).payerCreatedFor(mandate);
    }

    @Test
    public void shouldReturnPayerForTransactionIfItExists() {
        when(mockedPayerDao.findByMandateId(mandateFixture.getId())).thenReturn(Optional.of(payer));
        Payer payer = service.getPayerFor(mandateFixture.toEntity());
        assertThat(payer.getId(), is(payer.getId()));
        assertThat(payer.getExternalId(), is(payer.getExternalId()));
    }

    @Test
    public void shouldThrowIfGatewayAccountDoesNotExist() {
        when(mockedPayerDao.findByMandateId(mandateFixture.getId())).thenReturn(Optional.empty());
        thrown.expect(PayerNotFoundException.class);
        thrown.expectMessage("Couldn't find payer for mandate with external id: sdkfhsdkjfhjdks");
        thrown.reportMissingExceptionWithMessage("PayerNotFoundException expected");
        service.getPayerFor(mandateFixture.toEntity());
    }
}
