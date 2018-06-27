package uk.gov.pay.directdebit.payments.services;

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.pay.directdebit.mandate.fixtures.MandateFixture;
import uk.gov.pay.directdebit.mandate.model.ConfirmationDetails;
import uk.gov.pay.directdebit.mandate.model.MandateType;
import uk.gov.pay.directdebit.mandate.services.MandateConfirmService;
import uk.gov.pay.directdebit.mandate.services.MandateService;
import uk.gov.pay.directdebit.payers.api.BankAccountValidationResponse;
import uk.gov.pay.directdebit.payers.services.PayerService;
import uk.gov.pay.directdebit.payments.fixtures.GatewayAccountFixture;
import uk.gov.pay.directdebit.payments.fixtures.TransactionFixture;

import java.time.LocalDate;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.pay.directdebit.mandate.fixtures.MandateFixture.aMandateFixture;
import static uk.gov.pay.directdebit.payments.fixtures.ConfirmationDetailsFixture.confirmationDetails;
import static uk.gov.pay.directdebit.payments.fixtures.GatewayAccountFixture.aGatewayAccountFixture;

@RunWith(MockitoJUnitRunner.class)
public class SandboxServiceTest {

    @Mock
    private PayerService mockedPayerService;
    @Mock
    private MandateConfirmService mockedMandateConfirmService;
    @Mock
    private TransactionService mockedTransactionService;
    @Mock
    private MandateService mockedMandateService;

    private SandboxService service;

    private GatewayAccountFixture gatewayAccountFixture = aGatewayAccountFixture();
    private MandateFixture mandateFixture = aMandateFixture().withGatewayAccountFixture(gatewayAccountFixture);

    @Before
    public void setUp() {
        service = new SandboxService(mockedPayerService, mockedMandateConfirmService, mockedTransactionService, mockedMandateService);
    }

    @Test
    public void createPayer_shouldCreatePayerWhenReceivingPayerRequest() {
        Map<String, String> createPayerRequest = ImmutableMap.of();
        service.createPayer(mandateFixture.getExternalId(), gatewayAccountFixture.toEntity(), createPayerRequest);
        verify(mockedPayerService).createOrUpdatePayer(mandateFixture.getExternalId(), gatewayAccountFixture.toEntity(), createPayerRequest);
    }

    @Test
    public void confirm_shouldRegisterAPaymentSubmittedToProviderEventWhenSuccessfullyConfirmingOneOffMandate() {
        MandateFixture mandateFixture = aMandateFixture().withMandateType(MandateType.ONE_OFF).withGatewayAccountFixture(gatewayAccountFixture);
        TransactionFixture transactionFixture = TransactionFixture.aTransactionFixture().withMandateFixture(mandateFixture);
        ConfirmationDetails confirmationDetails = confirmationDetails()
                .withMandateFixture(mandateFixture)
                .withTransactionFixture(transactionFixture)
                .build();
        Map<String, String> details = ImmutableMap.of("sort_code", "123456", "account_number", "12345678", "transaction_external_id", transactionFixture.getExternalId());
        when(mockedMandateConfirmService
                .confirm(mandateFixture.getExternalId(), details))
                .thenReturn(confirmationDetails);

        service.confirm(mandateFixture.getExternalId(), gatewayAccountFixture.toEntity(), details);
        verify(mockedTransactionService).oneOffPaymentSubmittedToProviderFor(transactionFixture.toEntity(), LocalDate.now().plusDays(4));
    }

    @Test
    public void confirm_shouldNotRegisterAPaymentSubmittedToProviderEventWhenSuccessfullyConfirmingOnDemandMandate() {
        MandateFixture mandateFixture = aMandateFixture().withMandateType(MandateType.ON_DEMAND).withGatewayAccountFixture(gatewayAccountFixture);
        ConfirmationDetails confirmationDetails = confirmationDetails()
                .withMandateFixture(mandateFixture)
                .build();
        Map<String, String> details = ImmutableMap.of("sort_code", "123456", "account_number", "12345678");
        when(mockedMandateConfirmService
                .confirm(mandateFixture.getExternalId(), details))
                .thenReturn(confirmationDetails);

        service.confirm(mandateFixture.getExternalId(), gatewayAccountFixture.toEntity(), details);
        verifyNoMoreInteractions(mockedTransactionService);
    }

    @Test
    public void collect_shouldRegisterAPaymentSubmittedToProviderEvent() {
        MandateFixture mandateFixture = aMandateFixture().withMandateType(MandateType.ON_DEMAND).withGatewayAccountFixture(gatewayAccountFixture);
        TransactionFixture transactionFixture = TransactionFixture.aTransactionFixture().withMandateFixture(mandateFixture);
        Map<String, String> details = ImmutableMap.of(
                "amount", "123456",
                "reference", "a reference",
                "description", "a description",
                "agreement_id", mandateFixture.getExternalId());
        when(mockedMandateService
                .findByExternalId(mandateFixture.getExternalId()))
                .thenReturn(mandateFixture.toEntity());
        when(mockedTransactionService.createTransaction(details, mandateFixture.toEntity(), gatewayAccountFixture.getExternalId()))
                .thenReturn(transactionFixture.toEntity());
        service.collect(gatewayAccountFixture.toEntity(), details);
        verify(mockedTransactionService).onDemandPaymentSubmittedToProviderFor(transactionFixture.toEntity(), LocalDate.now().plusDays(4));
    }
    
    @Test
    public void shouldValidateBankAccountDetails_ifSortCodeAndAccountNumberAreValid() {
        Map<String, String> bankAccountDetails = ImmutableMap.of(
                "account_number", "12345678",
                "sort_code", "123456",
                "transaction_external_id", "12asdasd3456" 
        );

        BankAccountValidationResponse response = service.validate(mandateFixture.getExternalId(), bankAccountDetails);
        assertThat(response.isValid(), is(true));
        assertThat(response.getBankName(), is("Sandbox Bank"));
    }

    @Test
    public void shouldValidateBankAccountDetails_ifSortCodeIsInvalid() {
        Map<String, String> bankAccountDetails = ImmutableMap.of(
                "account_number", "12345678",
                "sort_code", "12345s",
                "transaction_external_id", "fsdfsdf343"
        );

        BankAccountValidationResponse response = service.validate(mandateFixture.getExternalId(), bankAccountDetails);
        assertThat(response.isValid(), is(false));
        assertThat(response.getBankName(), is(nullValue()));
    }

    @Test
    public void shouldValidateBankAccountDetails_ifAccountNumberIsInvalid() {
        Map<String, String> bankAccountDetails = ImmutableMap.of(
                "account_number", "1234567r",
                "sort_code", "123456",
                "transaction_external_id", "fsdfsdf343"
        );

        BankAccountValidationResponse response = service.validate(mandateFixture.getExternalId(), bankAccountDetails);
        assertThat(response.isValid(), is(false));
        assertThat(response.getBankName(), is(nullValue()));
    }
    
}
