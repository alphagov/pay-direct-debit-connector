package uk.gov.pay.directdebit.mandate.services;

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.pay.directdebit.gatewayaccounts.model.PaymentProvider;
import uk.gov.pay.directdebit.mandate.api.ConfirmMandateRequest;
import uk.gov.pay.directdebit.mandate.api.CreateMandateRequest;
import uk.gov.pay.directdebit.mandate.fixtures.MandateFixture;
import uk.gov.pay.directdebit.mandate.model.Mandate;
import uk.gov.pay.directdebit.mandate.model.MandateState;
import uk.gov.pay.directdebit.mandate.model.MandateType;
import uk.gov.pay.directdebit.payers.model.BankAccountDetails;
import uk.gov.pay.directdebit.payments.api.CollectPaymentRequest;
import uk.gov.pay.directdebit.payments.exception.InvalidStateTransitionException;
import uk.gov.pay.directdebit.payments.fixtures.GatewayAccountFixture;
import uk.gov.pay.directdebit.payments.fixtures.TransactionFixture;
import uk.gov.pay.directdebit.payments.model.DirectDebitEvent;
import uk.gov.pay.directdebit.payments.model.PaymentProviderFactory;
import uk.gov.pay.directdebit.payments.model.Transaction;
import uk.gov.pay.directdebit.payments.services.SandboxService;
import uk.gov.pay.directdebit.payments.services.TransactionService;

import javax.ws.rs.core.UriInfo;
import java.time.LocalDate;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class OnDemandMandateServiceTest {

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

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

    private GatewayAccountFixture gatewayAccountFixture = GatewayAccountFixture
            .aGatewayAccountFixture().withPaymentProvider(PaymentProvider.SANDBOX);
    private MandateFixture mandateFixture = MandateFixture.aMandateFixture()
            .withMandateType(MandateType.ON_DEMAND)
            .withState(MandateState.AWAITING_DIRECT_DEBIT_DETAILS)
            .withGatewayAccountFixture(gatewayAccountFixture);
    private ImmutableMap<String, String> createMandateRequest = ImmutableMap
            .of("return_url", "https://blabla.test", "agreement_type", "ON_DEMAND", "service_reference", "ref");
    private ImmutableMap<String, String> confirmMandateRequest = ImmutableMap
            .of("sort_code", "123456", "account_number", "12345678");

    private OnDemandMandateService service;

    @Before
    public void setUp() {
        service = new OnDemandMandateService(mockedPaymentProviderFactory, mockedMandateStateUpdateService, mockedTransactionService, mockedMandateService);
        when(mockedPaymentProviderFactory.getCommandServiceFor(gatewayAccountFixture.getPaymentProvider())).thenReturn(mockedSandboxService);
    }

    @Test
    public void create_shouldDelegateToTheMandateServiceToCreateOnDemandMandate() {
        CreateMandateRequest mandateCreationRequest = CreateMandateRequest.of(createMandateRequest);
        UriInfo mockedUriInfo = mock(UriInfo.class);
        service.create(gatewayAccountFixture.toEntity(), mandateCreationRequest, mockedUriInfo);

        verify(mockedMandateService).createMandate(mandateCreationRequest, gatewayAccountFixture.getExternalId(), mockedUriInfo);
    }

    @Test
    public void confirm_shouldConfirmOnDemandMandate() {
        Mandate mandate = mandateFixture.toEntity();
        ConfirmMandateRequest mandateConfirmationRequest = ConfirmMandateRequest.of(confirmMandateRequest);
        BankAccountDetails bankAccountDetails = BankAccountDetails.of(confirmMandateRequest);

        when(mockedMandateStateUpdateService.canUpdateStateFor(mandate, DirectDebitEvent.SupportedEvent.DIRECT_DEBIT_DETAILS_CONFIRMED)).thenReturn(true);
        when(mockedSandboxService.confirmOnDemandMandate(mandate, bankAccountDetails)).thenReturn(mandate);
        service.confirm(gatewayAccountFixture.toEntity(), mandate, mandateConfirmationRequest);

        verify(mockedMandateStateUpdateService).confirmedOnDemandDirectDebitDetailsFor(mandate);
    }

    @Test
    public void confirm_shouldNotConfirmOnDemandMandateForInvalidState() {
        Mandate mandate = mandateFixture.withState(MandateState.CANCELLED).toEntity();
        ConfirmMandateRequest mandateConfirmationRequest = ConfirmMandateRequest.of(confirmMandateRequest);

        when(mockedMandateStateUpdateService.canUpdateStateFor(mandate, DirectDebitEvent.SupportedEvent.DIRECT_DEBIT_DETAILS_CONFIRMED)).thenReturn(false);
        expectedException.expect(InvalidStateTransitionException.class);
        expectedException.expectMessage("Transition DIRECT_DEBIT_DETAILS_CONFIRMED from state CANCELLED is not valid");

        service.confirm(gatewayAccountFixture.toEntity(), mandate, mandateConfirmationRequest);
    }

    @Test
    public void collect_shouldCreateATransactionAPaymentAndRegisterOnDemandPaymentSubmittedEvent() {
        Transaction transaction = TransactionFixture.aTransactionFixture().withMandateFixture(mandateFixture).toEntity();
        Mandate mandate = mandateFixture.toEntity();
        CollectPaymentRequest collectPaymentRequest = new CollectPaymentRequest(
                mandateFixture.getExternalId(),
                123456L,
                "a description",
                "a reference"
        );
        LocalDate chargeDate = LocalDate.parse("1987-11-16");
        when(mockedTransactionService
                .createTransaction(collectPaymentRequest, mandate, gatewayAccountFixture.getExternalId()))
                .thenReturn(transaction);
        when(mockedSandboxService.collect(mandate, transaction)).thenReturn(chargeDate);

        service.collect(gatewayAccountFixture.toEntity(), mandate, collectPaymentRequest);

        verify(mockedTransactionService).onDemandPaymentSubmittedToProviderFor(transaction, chargeDate);
    }

}
