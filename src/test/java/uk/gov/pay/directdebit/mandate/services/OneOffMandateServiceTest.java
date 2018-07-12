package uk.gov.pay.directdebit.mandate.services;

import com.google.common.collect.ImmutableMap;
import java.time.LocalDate;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.pay.directdebit.gatewayaccounts.model.PaymentProvider;
import uk.gov.pay.directdebit.mandate.api.ConfirmMandateRequest;
import uk.gov.pay.directdebit.mandate.fixtures.MandateFixture;
import uk.gov.pay.directdebit.mandate.model.Mandate;
import uk.gov.pay.directdebit.mandate.model.MandateState;
import uk.gov.pay.directdebit.mandate.model.MandateType;
import uk.gov.pay.directdebit.mandate.model.OneOffConfirmationDetails;
import uk.gov.pay.directdebit.payers.model.BankAccountDetails;
import uk.gov.pay.directdebit.payments.api.CreatePaymentRequest;
import uk.gov.pay.directdebit.payments.fixtures.GatewayAccountFixture;
import uk.gov.pay.directdebit.payments.fixtures.TransactionFixture;
import uk.gov.pay.directdebit.payments.model.PaymentProviderFactory;
import uk.gov.pay.directdebit.payments.model.Transaction;
import uk.gov.pay.directdebit.payments.services.SandboxService;
import uk.gov.pay.directdebit.payments.services.TransactionService;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class OneOffMandateServiceTest {
    @Mock
    private SandboxService mockedSandboxService;
    @Mock
    private TransactionService mockedTransactionService;
    @Mock
    private MandateService mockedMandateService;
    @Mock
    private MandateStateUpdateService mockedMandateStateUpdateService;
    @Mock
    private PaymentProviderFactory mockedPaymentProviderFactory;
    @Mock
    private MandateServiceFactory mockedMandateServiceFactory;


    private GatewayAccountFixture gatewayAccountFixture = GatewayAccountFixture
            .aGatewayAccountFixture().withPaymentProvider(PaymentProvider.SANDBOX);
    private MandateFixture mandateFixture = MandateFixture.aMandateFixture()
            .withMandateType(MandateType.ON_DEMAND)
            .withState(MandateState.AWAITING_DIRECT_DEBIT_DETAILS)
            .withGatewayAccountFixture(gatewayAccountFixture);
    private TransactionFixture transactionFixture = TransactionFixture.aTransactionFixture().withMandateFixture(mandateFixture);
    private ImmutableMap<String, String> createPaymentRequest = ImmutableMap
            .of("return_url", "https://blabla.test", "amount", "10", "reference", "ref", "description", "desc");
    private ImmutableMap<String, String> confirmMandateRequest = ImmutableMap
            .of("sort_code", "123456", "account_number", "12345678");

    private OneOffMandateService service;
    @Before
    public void setUp() {
        service = new OneOffMandateService(mockedPaymentProviderFactory, mockedTransactionService, mockedMandateStateUpdateService, mockedMandateService);
        when(mockedPaymentProviderFactory.getCommandServiceFor(gatewayAccountFixture.getPaymentProvider())).thenReturn(mockedSandboxService);
    }
    
    @Test
    public void create_shouldDelegateToTheMandateServiceToCreateOneOffMandateAndTransactionServiceForPayment() {
        CreatePaymentRequest paymentCreationRequest = CreatePaymentRequest.of(createPaymentRequest);

        when(mockedMandateService.createMandate(paymentCreationRequest, gatewayAccountFixture.getExternalId())).thenReturn(mandateFixture.toEntity());
        service.create(gatewayAccountFixture.toEntity(), paymentCreationRequest);
        
        verify(mockedTransactionService).createTransaction(paymentCreationRequest, mandateFixture.toEntity(), gatewayAccountFixture.getExternalId());
    }

    @Test
    public void confirm_shouldConfirmOneOffMandateAndSendEmail() {
        Mandate mandate = mandateFixture.toEntity();
        Transaction transaction = transactionFixture.toEntity();
        ConfirmMandateRequest mandateConfirmationRequest = ConfirmMandateRequest
                .of(confirmMandateRequest);
        OneOffConfirmationDetails oneOffConfirmationDetails = new OneOffConfirmationDetails(mandate, LocalDate.parse("1987-11-16"));
        BankAccountDetails bankAccountDetails = BankAccountDetails.of(confirmMandateRequest);

        when(mockedTransactionService.findTransactionForExternalId(
                mandateConfirmationRequest.getTransactionExternalId())).thenReturn(transaction);
        when(mockedSandboxService.confirmOneOffMandate(mandate, bankAccountDetails, transaction))
                .thenReturn(oneOffConfirmationDetails);
        service.confirm(gatewayAccountFixture.toEntity(), mandate, mandateConfirmationRequest);

        verify(mockedMandateStateUpdateService).confirmedDirectDebitDetailsFor(mandate);
        verify(mockedTransactionService)
                .oneOffPaymentSubmittedToProviderFor(transaction, oneOffConfirmationDetails.getChargeDate());
    }
}
