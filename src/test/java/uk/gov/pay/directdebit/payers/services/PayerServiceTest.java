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
import uk.gov.pay.directdebit.mandate.exception.PayerNotFoundException;
import uk.gov.pay.directdebit.mandate.fixtures.MandateFixture;
import uk.gov.pay.directdebit.mandate.model.Mandate;
import uk.gov.pay.directdebit.mandate.services.MandateQueryService;
import uk.gov.pay.directdebit.mandate.model.subtype.MandateExternalId;
import uk.gov.pay.directdebit.mandate.services.MandateService;
import uk.gov.pay.directdebit.mandate.services.MandateServiceFactory;
import uk.gov.pay.directdebit.mandate.services.MandateStateUpdateService;
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

@RunWith(MockitoJUnitRunner.class)
public class PayerServiceTest {

    @Mock
    private
    PayerDao mockedPayerDao;
    @Mock
    private MandateServiceFactory mockedMandateServiceFactory;
    @Mock
    private MandateQueryService mockedMandateQueryService;
    @Mock
    private MandateStateUpdateService mockedMandateStateUpdateService;
    @Mock
    private
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
    private MandateExternalId mandateExternalId = MandateExternalId.of("sdkfhsdkjfhjdks");

    private Payer payer = PayerFixture.aPayerFixture()
            .withName("mr payment").toEntity();
    private MandateFixture mandateFixture = MandateFixture.aMandateFixture().withExternalId(mandateExternalId);

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setUp() {
        service = new PayerService(mockedPayerDao, mockedMandateServiceFactory, mockedPayerParser);
        when(mockedMandateServiceFactory.getMandateQueryService()).thenReturn(mockedMandateQueryService);
        when(mockedMandateServiceFactory.getMandateStateUpdateService()).thenReturn(mockedMandateStateUpdateService);
        when(mockedMandateQueryService.findByExternalId(mandateExternalId)).thenReturn(mandateFixture.toEntity());
        when(mockedMandateStateUpdateService.receiveDirectDebitDetailsFor(mandateFixture.toEntity())).thenReturn(mandateFixture.toEntity());
        when(mockedPayerParser.parse(createPayerRequest, mandateFixture.getId())).thenReturn(payer);
    }

    @Test
    public void shouldCreateAndStoreAPayerWhenReceivingCreatePayerRequest() {
        service.createOrUpdatePayer(mandateExternalId, createPayerRequest);
        Mandate mandate = mandateFixture.toEntity();
        verify(mockedPayerDao).insert(payer);
        verify(mockedMandateStateUpdateService).payerCreatedFor(mandate);
        verify(mockedMandateStateUpdateService, never()).payerEditedFor(mandate);
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

        service.createOrUpdatePayer(mandateExternalId, createPayerRequest);
        verify(mockedPayerDao).updatePayerDetails(originalPayer.getId(), editedPayer);
        verify(mockedMandateStateUpdateService).payerEditedFor(mandate);
        verify(mockedMandateStateUpdateService, never()).payerCreatedFor(mandate);
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
