package uk.gov.pay.directdebit.mandate.services;

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.pay.directdebit.gatewayaccounts.model.GatewayAccount;
import uk.gov.pay.directdebit.gatewayaccounts.model.PaymentProvider;
import uk.gov.pay.directdebit.mandate.dao.MandateDao;
import uk.gov.pay.directdebit.mandate.fixtures.MandateFixture;
import uk.gov.pay.directdebit.mandate.model.Mandate;
import uk.gov.pay.directdebit.mandate.model.MandateConfirmationDetails;
import uk.gov.pay.directdebit.mandate.model.MandateConfirmationRequest;
import uk.gov.pay.directdebit.mandate.model.MandateState;
import uk.gov.pay.directdebit.mandate.services.gocardless.GoCardlessCommandService;
import uk.gov.pay.directdebit.payments.exception.InvalidStateTransitionException;
import uk.gov.pay.directdebit.payments.model.PaymentProviderFactory;
import uk.gov.pay.directdebit.payments.services.DirectDebitEventService;
import uk.gov.pay.directdebit.payments.services.GoCardlessService;
import uk.gov.pay.directdebit.payments.services.SandboxService;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@RunWith(MockitoJUnitRunner.class)
public class OnDemandMandateServiceTest {
    @Mock
    private SandboxService sandboxService;
    @Mock
    private GoCardlessService goCardlessService;
    @Mock
    private GoCardlessCommandService goCardlessCommandService;
    @Mock
    private MandateDao mandateDao;
    @Mock
    private DirectDebitEventService directDebitEventService;

    private MandateStateUpdateService mandateStateUpdateService;
    private OnDemandMandateService service;
    private GatewayAccount sandboxGatewayAccount = new GatewayAccount(
            2L,
            "sandbox_gateway_account",
            PaymentProvider.fromString("SANDBOX"),
            GatewayAccount.Type.TEST,
            "My service",
            "My description",
            "analytics_id"
    );
    private GatewayAccount goCardlessGatewayAccount = new GatewayAccount(
            2L,
            "gocardless_gateway_account",
            PaymentProvider.fromString("GOCARDLESS"),
            GatewayAccount.Type.TEST,
            "My service",
            "My description",
            "analytics_id"
    );

    @Before
    public void setUp() {

        PaymentProviderFactory paymentProviderFactory = new PaymentProviderFactory(
                sandboxService,
                goCardlessService,
                goCardlessCommandService);
        
        mandateStateUpdateService = new MandateStateUpdateService(mandateDao, directDebitEventService);
        service = new OnDemandMandateService(paymentProviderFactory, mandateStateUpdateService);
    }

    @Test
    public void confirm_shouldConfirOnDemandMandateForSandboxProvider() {
        ImmutableMap<String, String> confirmMandateRequest = ImmutableMap
                .of("sort_code", "123456", "account_number", "12345678");
        MandateConfirmationRequest mandateConfirmationRequest = MandateConfirmationRequest.of(confirmMandateRequest);

        String mandateExternalId = "test-mandate-ext-id";
        Mandate mandate = MandateFixture.aMandateFixture().withState(MandateState.AWAITING_DIRECT_DEBIT_DETAILS).withExternalId(mandateExternalId).toEntity();
        MandateConfirmationDetails mandateConfirmationDetails = MandateConfirmationDetails.from(mandate, mandateConfirmationRequest);

        service.confirm(sandboxGatewayAccount, mandate, mandateConfirmationRequest);
        
        verify(sandboxService).confirmMandate(mandateConfirmationDetails);
        verify(directDebitEventService).registerDirectDebitConfirmedEventFor(mandate);
    }

    @Test
    public void confirm_shouldConfirOnDemandMandateForGoCardlessProvider() {
        ImmutableMap<String, String> confirmMandateRequest = ImmutableMap
                .of("sort_code", "123456", "account_number", "12345678");
        MandateConfirmationRequest mandateConfirmationRequest = MandateConfirmationRequest.of(confirmMandateRequest);

        String mandateExternalId = "test-mandate-ext-id";
        Mandate mandate = MandateFixture.aMandateFixture().withState(MandateState.AWAITING_DIRECT_DEBIT_DETAILS).withExternalId(mandateExternalId).toEntity();
        MandateConfirmationDetails mandateConfirmationDetails = MandateConfirmationDetails.from(mandate, mandateConfirmationRequest);

        service.confirm(goCardlessGatewayAccount, mandate, mandateConfirmationRequest);
        
        verify(goCardlessCommandService).confirmMandate(mandateConfirmationDetails);
        verify(directDebitEventService).registerDirectDebitConfirmedEventFor(mandate);
    }

    @Test(expected=InvalidStateTransitionException.class)
    public void confirm_shouldNotConfirmMandateInInvalidState() {
        ImmutableMap<String, String> confirmMandateRequest = ImmutableMap
                .of("sort_code", "123456", "account_number", "12345678");
        MandateConfirmationRequest mandateConfirmationRequest = MandateConfirmationRequest.of(confirmMandateRequest);

        String mandateExternalId = "test-mandate-ext-id";
        Mandate mandate = MandateFixture.aMandateFixture().withState(MandateState.CREATED).withExternalId(mandateExternalId).toEntity();
        
        service.confirm(goCardlessGatewayAccount, mandate, mandateConfirmationRequest);
        
        verifyNoMoreInteractions(goCardlessCommandService);
    }
}
