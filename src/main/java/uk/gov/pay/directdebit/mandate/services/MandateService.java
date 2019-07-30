package uk.gov.pay.directdebit.mandate.services;

import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.directdebit.app.config.DirectDebitConfig;
import uk.gov.pay.directdebit.app.config.LinksConfig;
import uk.gov.pay.directdebit.common.exception.UnlinkedGCMerchantAccountException;
import uk.gov.pay.directdebit.common.util.RandomIdGenerator;
import uk.gov.pay.directdebit.events.services.GovUkPayEventService;
import uk.gov.pay.directdebit.gatewayaccounts.dao.GatewayAccountDao;
import uk.gov.pay.directdebit.gatewayaccounts.exception.GatewayAccountNotFoundException;
import uk.gov.pay.directdebit.gatewayaccounts.model.GatewayAccount;
import uk.gov.pay.directdebit.mandate.api.ConfirmMandateRequest;
import uk.gov.pay.directdebit.mandate.api.CreateMandateRequest;
import uk.gov.pay.directdebit.mandate.api.DirectDebitInfoFrontendResponse;
import uk.gov.pay.directdebit.mandate.api.ExternalMandateState;
import uk.gov.pay.directdebit.mandate.api.MandateResponse;
import uk.gov.pay.directdebit.mandate.dao.MandateDao;
import uk.gov.pay.directdebit.mandate.exception.MandateNotFoundException;
import uk.gov.pay.directdebit.mandate.model.Mandate;
import uk.gov.pay.directdebit.mandate.model.MandateState;
import uk.gov.pay.directdebit.mandate.model.PaymentProviderMandateIdAndBankReference;
import uk.gov.pay.directdebit.mandate.model.subtype.MandateExternalId;
import uk.gov.pay.directdebit.notifications.services.UserNotificationService;
import uk.gov.pay.directdebit.payers.model.BankAccountDetails;
import uk.gov.pay.directdebit.payments.model.Payment;
import uk.gov.pay.directdebit.payments.model.PaymentProviderFactory;
import uk.gov.pay.directdebit.payments.model.Token;
import uk.gov.pay.directdebit.payments.services.PaymentQueryService;
import uk.gov.pay.directdebit.tokens.exception.TokenNotFoundException;
import uk.gov.pay.directdebit.tokens.model.TokenExchangeDetails;
import uk.gov.pay.directdebit.tokens.services.TokenService;

import javax.inject.Inject;
import javax.ws.rs.core.UriInfo;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static javax.ws.rs.HttpMethod.GET;
import static javax.ws.rs.HttpMethod.POST;
import static javax.ws.rs.core.MediaType.APPLICATION_FORM_URLENCODED;
import static uk.gov.pay.directdebit.common.util.URIBuilder.createLink;
import static uk.gov.pay.directdebit.common.util.URIBuilder.nextUrl;
import static uk.gov.pay.directdebit.common.util.URIBuilder.selfUriFor;
import static uk.gov.pay.directdebit.events.model.GovUkPayEventType.MANDATE_CREATED;
import static uk.gov.pay.directdebit.events.model.GovUkPayEventType.MANDATE_SUBMITTED_TO_PROVIDER;
import static uk.gov.pay.directdebit.events.model.GovUkPayEventType.MANDATE_TOKEN_EXCHANGED;
import static uk.gov.pay.directdebit.events.model.GovUkPayEventType.MANDATE_USER_SETUP_CANCELLED;
import static uk.gov.pay.directdebit.events.model.GovUkPayEventType.MANDATE_USER_SETUP_CANCELLED_NOT_ELIGIBLE;
import static uk.gov.pay.directdebit.gatewayaccounts.model.PaymentProvider.GOCARDLESS;
import static uk.gov.pay.directdebit.mandate.api.ExternalMandateState.EXTERNAL_CREATED;
import static uk.gov.pay.directdebit.mandate.model.Mandate.MandateBuilder.aMandate;
import static uk.gov.pay.directdebit.mandate.model.Mandate.MandateBuilder.fromMandate;

public class MandateService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MandateService.class);
    private final MandateDao mandateDao;
    private final LinksConfig linksConfig;
    private final GatewayAccountDao gatewayAccountDao;
    private final TokenService tokenService;
    private final PaymentProviderFactory paymentProviderFactory;
    private final UserNotificationService userNotificationService;
    private final GovUkPayEventService govUkPayEventService;
    private final PaymentQueryService paymentQueryService;

    @Inject
    public MandateService(DirectDebitConfig directDebitConfig,
                          MandateDao mandateDao,
                          GatewayAccountDao gatewayAccountDao,
                          TokenService tokenService,
                          PaymentProviderFactory paymentProviderFactory,
                          UserNotificationService userNotificationService,
                          GovUkPayEventService govUkPayEventService,
                          PaymentQueryService paymentQueryService) {
        this.gatewayAccountDao = gatewayAccountDao;
        this.tokenService = tokenService;
        this.mandateDao = mandateDao;
        this.linksConfig = directDebitConfig.getLinks();
        this.paymentProviderFactory = paymentProviderFactory;
        this.userNotificationService = userNotificationService;
        this.govUkPayEventService = govUkPayEventService;
        this.paymentQueryService = paymentQueryService;
    }

    public Mandate createMandate(CreateMandateRequest createRequest, String accountExternalId) {
        return gatewayAccountDao.findByExternalId(accountExternalId)
                .map(gatewayAccount -> {
                    if (gatewayAccount.getAccessToken().isEmpty() &&
                            gatewayAccount.getPaymentProvider() == GOCARDLESS) {
                        LOGGER.error("Gateway account with id {} does not have access token", accountExternalId);
                        throw new UnlinkedGCMerchantAccountException(accountExternalId);
                    }

                    Mandate mandate = aMandate()
                            .withGatewayAccount(gatewayAccount)
                            .withExternalId(MandateExternalId.valueOf(RandomIdGenerator.newId()))
                            .withServiceReference(createRequest.getReference())
                            .withState(MandateState.CREATED)
                            .withReturnUrl(createRequest.getReturnUrl())
                            .withCreatedDate(ZonedDateTime.now(ZoneOffset.UTC))
                            .withDescription(createRequest.getDescription().orElse(null))
                            .build();

                    LOGGER.info("Creating mandate external id {}", mandate.getExternalId());
                    Long id = mandateDao.insert(mandate);

                    Mandate insertedMandate = fromMandate(mandate).withId(id).build();

                    return govUkPayEventService.storeEventAndUpdateStateForMandate(insertedMandate, MANDATE_CREATED);
                })
                .orElseThrow(() -> {
                    LOGGER.error("Gateway account with id {} not found", accountExternalId);
                    return new GatewayAccountNotFoundException(accountExternalId);
                });
    }

    public MandateResponse createMandate(CreateMandateRequest createMandateRequest, String accountExternalId, UriInfo uriInfo) {
        Mandate mandate = createMandate(createMandateRequest, accountExternalId);
        List<Map<String, Object>> dataLinks = createLinks(mandate, accountExternalId, uriInfo);
        return new MandateResponse(mandate, dataLinks);
    }

    public TokenExchangeDetails getMandateFor(String token) {
        return mandateDao
                .findByTokenId(token)
                .map(mandate -> govUkPayEventService.storeEventAndUpdateStateForMandate(mandate, MANDATE_TOKEN_EXCHANGED))
                .map(TokenExchangeDetails::new)
                .orElseThrow(TokenNotFoundException::new);
    }

    public DirectDebitInfoFrontendResponse populateGetMandateWithPaymentResponseForFrontend(String accountExternalId, String paymentExternalId) {
        Payment payment = paymentQueryService.findPaymentForExternalId(paymentExternalId);
        Mandate mandate = payment.getMandate();
        return new DirectDebitInfoFrontendResponse(mandate, accountExternalId, payment);
    }

    public DirectDebitInfoFrontendResponse populateGetMandateResponseForFrontend(String accountExternalId, MandateExternalId mandateExternalId) {
        Mandate mandate = findByExternalId(mandateExternalId);
        return new DirectDebitInfoFrontendResponse(mandate, accountExternalId, null);
    }

    public MandateResponse populateGetMandateResponse(String accountExternalId, MandateExternalId mandateExternalId, UriInfo uriInfo) {
        Mandate mandate = findByExternalId(mandateExternalId);
        List<Map<String, Object>> dataLinks = createLinks(mandate, accountExternalId, uriInfo);
        return new MandateResponse(mandate, dataLinks);
    }

    public MandateResponse populateGetMandateResponse(Mandate mandate, UriInfo uriInfo) {
        var dataLinks = createLinks(mandate, mandate.getGatewayAccount().getExternalId(), uriInfo);
        return new MandateResponse(mandate, dataLinks);
    }

    public Mandate findByExternalId(MandateExternalId externalId) {
        return mandateDao
                .findByExternalId(externalId)
                .orElseThrow(() -> new MandateNotFoundException(externalId));
    }

    public Mandate findById(Long id) {
        return mandateDao
                .findById(id)
                .orElseThrow(() -> new MandateNotFoundException(id.toString()));
    }

    public void changePaymentMethodFor(MandateExternalId mandateExternalId) {
        Mandate mandate = findByExternalId(mandateExternalId);
        govUkPayEventService.storeEventAndUpdateStateForMandate(mandate, MANDATE_USER_SETUP_CANCELLED_NOT_ELIGIBLE);
    }

    public void cancelMandateCreation(MandateExternalId mandateExternalId) {
        Mandate mandate = findByExternalId(mandateExternalId);
        govUkPayEventService.storeEventAndUpdateStateForMandate(mandate, MANDATE_USER_SETUP_CANCELLED);
    }

    public void confirm(GatewayAccount gatewayAccount, Mandate mandate, ConfirmMandateRequest confirmDetailsRequest) {
        PaymentProviderMandateIdAndBankReference paymentProviderMandateIdAndBankReference = paymentProviderFactory
                .getCommandServiceFor(gatewayAccount.getPaymentProvider())
                .confirmMandate(
                        mandate,
                        new BankAccountDetails(
                                confirmDetailsRequest.getAccountNumber(),
                                confirmDetailsRequest.getSortCode())
                );

        Mandate updatedMandate = fromMandate(mandate)
                .withMandateBankStatementReference(paymentProviderMandateIdAndBankReference.getMandateBankStatementReference())
                .withPaymentProviderId(paymentProviderMandateIdAndBankReference.getPaymentProviderMandateId())
                .build();

        mandateDao.updateReferenceAndPaymentProviderId(updatedMandate);
        userNotificationService.sendMandateCreatedEmailFor(updatedMandate);
        govUkPayEventService.storeEventAndUpdateStateForMandate(updatedMandate, MANDATE_SUBMITTED_TO_PROVIDER);
    }

    private List<Map<String, Object>> createLinks(Mandate mandate, String accountExternalId, UriInfo uriInfo) {
        List<Map<String, Object>> dataLinks = new ArrayList<>();

        dataLinks.add(createLink("self", GET, selfUriFor(uriInfo,
                "/v1/api/accounts/{accountId}/mandates/{mandateExternalId}",
                accountExternalId,
                mandate.getExternalId().toString())));

        if (mandate.getState().toExternal() == EXTERNAL_CREATED) {
            Token token = tokenService.generateNewTokenFor(mandate);
            dataLinks.add(createLink("next_url",
                    GET,
                    nextUrl(linksConfig.getFrontendUrl(), "secure", token.getToken())));
            dataLinks.add(createLink("next_url_post",
                    POST,
                    nextUrl(linksConfig.getFrontendUrl(), "secure"),
                    APPLICATION_FORM_URLENCODED,
                    ImmutableMap.of("chargeTokenId", token.getToken())));
        }
        return dataLinks;
    }
}
