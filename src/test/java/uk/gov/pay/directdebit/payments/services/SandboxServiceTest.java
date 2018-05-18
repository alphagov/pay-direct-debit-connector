package uk.gov.pay.directdebit.payments.services;

import com.google.common.collect.ImmutableMap;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.pay.directdebit.gatewayaccounts.model.GatewayAccount;
import uk.gov.pay.directdebit.mandate.model.ConfirmationDetails;
import uk.gov.pay.directdebit.mandate.services.PaymentConfirmService;
import uk.gov.pay.directdebit.payers.api.BankAccountValidationResponse;
import uk.gov.pay.directdebit.payers.fixtures.PayerFixture;
import uk.gov.pay.directdebit.payers.model.Payer;
import uk.gov.pay.directdebit.payers.services.PayerService;
import uk.gov.pay.directdebit.payments.model.Transaction;

import java.time.LocalDate;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.*;
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
    private Payer payer = PayerFixture.aPayerFixture().toEntity();

    @Before
    public void setUp() {
        service = new SandboxService(mockedPayerService, mockedPaymentConfirmService, mockedTransactionService);
    }

    @Test
    public void createPayer_shouldCreatePayerWhenReceivingPayerRequest() {
        Map<String, String> createPayerRequest = ImmutableMap.of();
        service.createPayer(paymentRequestExternalId, gatewayAccount, createPayerRequest);
        verify(mockedPayerService).createOrUpdatePayer(paymentRequestExternalId, gatewayAccount, createPayerRequest);
    }

    @Test
    public void confirm_shouldRegisterAPaymentSubmittedToProviderEventWhenSuccessfullyConfirmed() {
        ConfirmationDetails confirmationDetails = confirmationDetails()
                .withTransaction(aTransactionFixture())
                .withMandate(aMandateFixture())
                .build();
        Transaction transaction = confirmationDetails.getTransaction();

        Map<String, String> details = ImmutableMap.of("sort_code", "123456", "account_number", "12345678");
        when(mockedPaymentConfirmService.confirm(gatewayAccount.getExternalId(), paymentRequestExternalId, details))
                .thenReturn(confirmationDetails);
        when(mockedPayerService.getPayerFor(transaction)).thenReturn(payer);

        service.confirm(paymentRequestExternalId, gatewayAccount, details);
        verify(mockedTransactionService).paymentSubmittedToProviderFor(transaction, payer, confirmationDetails.getMandate(), LocalDate.now().plusDays(4));
    }

    @Test
    public void shouldValidateBankAccountDetails_ifSortCodeAndAccountNumberAreValid() {
        Map<String, String> bankAccountDetails = ImmutableMap.of(
                "account_number", "12345678",
                "sort_code", "123456" 
        );

        BankAccountValidationResponse response = service.validate(paymentRequestExternalId, bankAccountDetails);
        assertThat(response.isValid(), is(true));
        assertThat(response.getBankName(), is("Sandbox Bank"));
    }

    @Test
    public void shouldValidateBankAccountDetails_ifSortCodeIsInvalid() {
        Map<String, String> bankAccountDetails = ImmutableMap.of(
                "account_number", "12345678",
                "sort_code", "12345s"
        );

        BankAccountValidationResponse response = service.validate(paymentRequestExternalId, bankAccountDetails);
        assertThat(response.isValid(), is(false));
        assertThat(response.getBankName(), is(nullValue()));
    }

    @Test
    public void shouldValidateBankAccountDetails_ifAccountNumberIsInvalid() {
        Map<String, String> bankAccountDetails = ImmutableMap.of(
                "account_number", "1234567r",
                "sort_code", "123456"
        );

        BankAccountValidationResponse response = service.validate(paymentRequestExternalId, bankAccountDetails);
        assertThat(response.isValid(), is(false));
        assertThat(response.getBankName(), is(nullValue()));
    }
}
