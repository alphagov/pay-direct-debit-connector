package uk.gov.pay.directdebit.mandate.services;

import org.apache.commons.lang.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.pay.directdebit.app.config.DirectDebitConfig;
import uk.gov.pay.directdebit.app.config.LinksConfig;
import uk.gov.pay.directdebit.events.services.GovUkPayEventService;
import uk.gov.pay.directdebit.gatewayaccounts.dao.GatewayAccountDao;
import uk.gov.pay.directdebit.gatewayaccounts.model.GatewayAccount;
import uk.gov.pay.directdebit.mandate.api.ConfirmMandateRequest;
import uk.gov.pay.directdebit.mandate.api.CreateMandateRequest;
import uk.gov.pay.directdebit.mandate.dao.MandateDao;
import uk.gov.pay.directdebit.mandate.exception.PayerNotFoundException;
import uk.gov.pay.directdebit.mandate.model.Mandate;
import uk.gov.pay.directdebit.mandate.model.MandateBankStatementReference;
import uk.gov.pay.directdebit.mandate.model.PaymentProviderMandateIdAndBankReference;
import uk.gov.pay.directdebit.mandate.model.SandboxMandateId;
import uk.gov.pay.directdebit.notifications.services.UserNotificationService;
import uk.gov.pay.directdebit.payers.model.BankAccountDetails;
import uk.gov.pay.directdebit.payments.exception.CreateCustomerBankAccountFailedException;
import uk.gov.pay.directdebit.payments.exception.CreateCustomerFailedException;
import uk.gov.pay.directdebit.payments.exception.CreateMandateFailedException;
import uk.gov.pay.directdebit.payments.model.PaymentProviderFactory;
import uk.gov.pay.directdebit.payments.services.PaymentQueryService;
import uk.gov.pay.directdebit.payments.services.SandboxService;
import uk.gov.pay.directdebit.tokens.services.TokenService;

import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.pay.directdebit.events.model.GovUkPayEventType.MANDATE_CREATED;
import static uk.gov.pay.directdebit.events.model.GovUkPayEventType.MANDATE_ERROR_SUBMITTING_TO_PROVIDER;
import static uk.gov.pay.directdebit.events.model.GovUkPayEventType.MANDATE_SUBMITTED_TO_PROVIDER;
import static uk.gov.pay.directdebit.events.model.GovUkPayEventType.MANDATE_UNEXPECTED_ERROR;
import static uk.gov.pay.directdebit.gatewayaccounts.model.PaymentProvider.SANDBOX;
import static uk.gov.pay.directdebit.mandate.model.Mandate.MandateBuilder.fromMandate;
import static uk.gov.pay.directdebit.mandate.model.MandateState.CREATED;
import static uk.gov.pay.directdebit.payments.fixtures.GatewayAccountFixture.aGatewayAccountFixture;

@RunWith(MockitoJUnitRunner.class)
public class MandateServiceConfirmTest {

    @Mock
    private MandateDao mockMandateDao;

    @Mock
    private GatewayAccountDao mockGatewayAccountDao;

    @Mock
    private TokenService mockTokenService;

    @Mock
    private PaymentQueryService mockPaymentQueryService;

    @Mock
    private GovUkPayEventService mockGovUkPayEventService;

    @Mock
    private UserNotificationService mockUserNotificationService;

    @Mock
    private DirectDebitConfig mockDirectDebitConfig;

    @Mock
    private LinksConfig mockLinksConfig;

    @Mock
    private UriInfo mockUriInfo;

    @Mock
    private UriBuilder mockUriBuilder;

    @Mock
    private PaymentProviderFactory mockPaymentProviderFactory;

    @Mock
    private SandboxService mockSandboxService;

    private final GatewayAccount gatewayAccount = aGatewayAccountFixture().withPaymentProvider(SANDBOX).toEntity();
    private final Map<String, String> confirmMandateRequest = Map.of("sort_code", "123456", "account_number", "12345678");
    private final ConfirmMandateRequest mandateConfirmationRequest = ConfirmMandateRequest.of(confirmMandateRequest);
    private final BankAccountDetails bankAccountDetails = BankAccountDetails.of(confirmMandateRequest);

    private MandateService service;

    @Before
    public void setUp() throws URISyntaxException {
        when(mockPaymentProviderFactory.getCommandServiceFor(SANDBOX)).thenReturn(mockSandboxService);
        when(mockDirectDebitConfig.getLinks()).thenReturn(mockLinksConfig);

        service = new MandateService(
                mockDirectDebitConfig,
                mockMandateDao,
                mockGatewayAccountDao,
                mockTokenService,
                mockPaymentProviderFactory,
                mockUserNotificationService,
                mockGovUkPayEventService,
                mockPaymentQueryService);
    }

    @Test
    public void confirmRecordsSubmittedToProviderEvent() {
        Mandate mandate = getMandateForProvider(gatewayAccount);

        var confirmMandateResponse = new PaymentProviderMandateIdAndBankReference(
                SandboxMandateId.valueOf(mandate.getExternalId().toString()),
                MandateBankStatementReference.valueOf(RandomStringUtils.randomAlphanumeric(5)));

        when(mockSandboxService.confirmMandate(mandate, bankAccountDetails)).thenReturn(confirmMandateResponse);

        service.confirm(gatewayAccount, mandate, mandateConfirmationRequest);

        var expectedUpdatedMandate = fromMandate(mandate)
                .withState(CREATED)
                .withPaymentProviderId(confirmMandateResponse.getPaymentProviderMandateId())
                .withMandateBankStatementReference(confirmMandateResponse.getMandateBankStatementReference())
                .build();

        verify(mockUserNotificationService).sendMandateCreatedEmailFor(expectedUpdatedMandate);
        verify(mockMandateDao).updateReferenceAndPaymentProviderId(expectedUpdatedMandate);
        verify(mockGovUkPayEventService).storeEventAndUpdateStateForMandate(expectedUpdatedMandate, MANDATE_SUBMITTED_TO_PROVIDER);
    }

    @Test
    public void confirmRecordsUnexpectedErrorWhenPayerNotFoundException() {
        Mandate mandate = getMandateForProvider(gatewayAccount);

        when(mockPaymentProviderFactory.getCommandServiceFor(SANDBOX)).thenReturn(mockSandboxService);
        when(mockSandboxService.confirmMandate(mandate, bankAccountDetails)).thenThrow(PayerNotFoundException.class);

        try {
            service.confirm(gatewayAccount, mandate, mandateConfirmationRequest);
            fail();
        } catch (PayerNotFoundException e) {
            var expectedUpdatedMandate = fromMandate(mandate).withState(CREATED).build();
            verify(mockUserNotificationService, never()).sendMandateCreatedEmailFor(any(Mandate.class));
            verify(mockGovUkPayEventService).storeEventAndUpdateStateForMandate(expectedUpdatedMandate, MANDATE_UNEXPECTED_ERROR);
        }
    }

    @Test
    public void confirmRecordsErrorSubmittingToProviderWhenCreateCustomerFailedException() {
        Mandate mandate = getMandateForProvider(gatewayAccount);

        when(mockPaymentProviderFactory.getCommandServiceFor(SANDBOX)).thenReturn(mockSandboxService);
        when(mockSandboxService.confirmMandate(mandate, bankAccountDetails)).thenThrow(CreateCustomerFailedException.class);

        try {
            service.confirm(gatewayAccount, mandate, mandateConfirmationRequest);
            fail();
        } catch (CreateCustomerFailedException e) {
            var expectedUpdatedMandate = fromMandate(mandate).withState(CREATED).build();
            verify(mockUserNotificationService, never()).sendMandateCreatedEmailFor(any(Mandate.class));
            verify(mockGovUkPayEventService).storeEventAndUpdateStateForMandate(expectedUpdatedMandate, MANDATE_ERROR_SUBMITTING_TO_PROVIDER);
        }
    }

    @Test
    public void confirmRecordsErrorSubmittingToProviderWhenCreateCustomerBankAccountFailedException() {
        Mandate mandate = getMandateForProvider(gatewayAccount);

        when(mockPaymentProviderFactory.getCommandServiceFor(SANDBOX)).thenReturn(mockSandboxService);
        when(mockSandboxService.confirmMandate(mandate, bankAccountDetails)).thenThrow(CreateCustomerBankAccountFailedException.class);

        try {
            service.confirm(gatewayAccount, mandate, mandateConfirmationRequest);
            fail();
        } catch (CreateCustomerBankAccountFailedException e) {
            var expectedUpdatedMandate = fromMandate(mandate).withState(CREATED).build();
            verify(mockUserNotificationService, never()).sendMandateCreatedEmailFor(any(Mandate.class));
            verify(mockGovUkPayEventService).storeEventAndUpdateStateForMandate(expectedUpdatedMandate, MANDATE_ERROR_SUBMITTING_TO_PROVIDER);
        }
    }

    @Test
    public void confirmRecordsErrorSubmittingToProviderWhenCreateMandateFailedException() {
        Mandate mandate = getMandateForProvider(gatewayAccount);

        when(mockPaymentProviderFactory.getCommandServiceFor(SANDBOX)).thenReturn(mockSandboxService);
        when(mockSandboxService.confirmMandate(mandate, bankAccountDetails)).thenThrow(CreateMandateFailedException.class);

        try {
            service.confirm(gatewayAccount, mandate, mandateConfirmationRequest);
            fail();
        } catch (CreateMandateFailedException e) {
            var expectedUpdatedMandate = fromMandate(mandate).withState(CREATED).build();
            verify(mockUserNotificationService, never()).sendMandateCreatedEmailFor(any(Mandate.class));
            verify(mockGovUkPayEventService).storeEventAndUpdateStateForMandate(expectedUpdatedMandate, MANDATE_ERROR_SUBMITTING_TO_PROVIDER);
        }
    }

    private Mandate getMandateForProvider(GatewayAccount gatewayAccount) {
        when(mockGatewayAccountDao.findByExternalId(anyString())).thenReturn(Optional.of(gatewayAccount));
        when(mockMandateDao.insert(any(Mandate.class))).thenReturn(1L);

        when(mockGovUkPayEventService.storeEventAndUpdateStateForMandate(any(Mandate.class), eq(MANDATE_CREATED)))
                .then(invocationOnMock -> {
                    Mandate insertedMandate = invocationOnMock.getArgument(0, Mandate.class);
                    return fromMandate(insertedMandate).withState(CREATED).build();
                });

        var createMandateRequest = new CreateMandateRequest("https://example.com/return", "some-service-reference");
        Mandate mandate = service.createMandate(createMandateRequest, gatewayAccount.getExternalId());

        assertThat(mandate.getState(), is(CREATED));

        return mandate;
    }

}
