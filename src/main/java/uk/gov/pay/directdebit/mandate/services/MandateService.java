package uk.gov.pay.directdebit.mandate.services;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.directdebit.app.config.DirectDebitConfig;
import uk.gov.pay.directdebit.app.config.LinksConfig;
import uk.gov.pay.directdebit.common.exception.UnlinkedGCMerchantAccountException;
import uk.gov.pay.directdebit.common.util.RandomIdGenerator;
import uk.gov.pay.directdebit.gatewayaccounts.dao.GatewayAccountDao;
import uk.gov.pay.directdebit.gatewayaccounts.exception.GatewayAccountNotFoundException;
import uk.gov.pay.directdebit.gatewayaccounts.model.GatewayAccount;
import uk.gov.pay.directdebit.gatewayaccounts.model.PaymentProvider;
import uk.gov.pay.directdebit.mandate.api.ConfirmMandateRequest;
import uk.gov.pay.directdebit.mandate.api.CreateMandateRequest;
import uk.gov.pay.directdebit.mandate.api.CreateMandateResponse;
import uk.gov.pay.directdebit.mandate.api.DirectDebitInfoFrontendResponse;
import uk.gov.pay.directdebit.mandate.api.GetMandateResponse;
import uk.gov.pay.directdebit.mandate.dao.MandateDao;
import uk.gov.pay.directdebit.mandate.exception.MandateNotFoundException;
import uk.gov.pay.directdebit.mandate.model.PaymentProviderMandateIdAndBankReference;
import uk.gov.pay.directdebit.mandate.model.Mandate;
import uk.gov.pay.directdebit.mandate.model.MandateBankStatementReference;
import uk.gov.pay.directdebit.mandate.model.MandateState;
import uk.gov.pay.directdebit.mandate.model.subtype.MandateExternalId;
import uk.gov.pay.directdebit.payers.model.BankAccountDetails;
import uk.gov.pay.directdebit.payments.exception.InvalidStateTransitionException;
import uk.gov.pay.directdebit.payments.model.PaymentProviderFactory;
import uk.gov.pay.directdebit.payments.model.Token;
import uk.gov.pay.directdebit.payments.model.Transaction;
import uk.gov.pay.directdebit.payments.services.TransactionService;
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
import static uk.gov.pay.directdebit.mandate.model.Mandate.MandateBuilder.aMandate;
import static uk.gov.pay.directdebit.payments.model.DirectDebitEvent.SupportedEvent.DIRECT_DEBIT_DETAILS_CONFIRMED;

public class MandateService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MandateService.class);
    private final MandateDao mandateDao;
    private final LinksConfig linksConfig;
    private final GatewayAccountDao gatewayAccountDao;
    private final TokenService tokenService;
    private final TransactionService transactionService;
    private final MandateStateUpdateService mandateStateUpdateService;
    private final PaymentProviderFactory paymentProviderFactory;

    @Inject
    public MandateService(DirectDebitConfig directDebitConfig,
                          MandateDao mandateDao,
                          GatewayAccountDao gatewayAccountDao,
                          TokenService tokenService,
                          TransactionService transactionService,
                          MandateStateUpdateService mandateStateUpdateService,
                          PaymentProviderFactory paymentProviderFactory) {
        this.gatewayAccountDao = gatewayAccountDao;
        this.tokenService = tokenService;
        this.transactionService = transactionService;
        this.mandateDao = mandateDao;
        this.mandateStateUpdateService = mandateStateUpdateService;
        this.linksConfig = directDebitConfig.getLinks();
        this.paymentProviderFactory = paymentProviderFactory;
    }

    public Mandate createMandate(CreateMandateRequest createRequest, String accountExternalId) {
        return gatewayAccountDao.findByExternalId(accountExternalId)
                .map(gatewayAccount -> {
                    if (gatewayAccount.getAccessToken().isEmpty() &&
                            gatewayAccount.getPaymentProvider() == PaymentProvider.GOCARDLESS) {
                        LOGGER.error("Gateway account with id {} does not have access token", accountExternalId);
                        throw new UnlinkedGCMerchantAccountException(accountExternalId);
                    }

                    // TODO:
                    // when we introduce GoCardless gateway accounts to work with create mandate,
                    // then modify appropriate mandate reference values
                    MandateBankStatementReference mandateReference = MandateBankStatementReference.valueOf(
                            PaymentProvider.SANDBOX.equals(gatewayAccount.getPaymentProvider()) ?
                                    RandomStringUtils.randomAlphanumeric(18) : "gocardless-default");

                    Mandate mandate = aMandate()
                            .withGatewayAccount(gatewayAccount)
                            .withExternalId(MandateExternalId.valueOf(RandomIdGenerator.newId()))
                            .withMandateBankStatementReference(mandateReference)
                            .withServiceReference(createRequest.getReference())
                            .withState(MandateState.CREATED)
                            .withReturnUrl(createRequest.getReturnUrl())
                            .withCreatedDate(ZonedDateTime.now(ZoneOffset.UTC))
                            .build();

                    LOGGER.info("Creating mandate external id {}", mandate.getExternalId());
                    Long id = mandateDao.insert(mandate);
                    mandate.setId(id);
                    mandateStateUpdateService.mandateCreatedFor(mandate);
                    return mandate;
                })
                .orElseThrow(() -> {
                    LOGGER.error("Gateway account with id {} not found", accountExternalId);
                    return new GatewayAccountNotFoundException(accountExternalId);
                });
    }

    public CreateMandateResponse createMandate(CreateMandateRequest createMandateRequest, String accountExternalId, UriInfo uriInfo) {
        Mandate mandate = createMandate(createMandateRequest, accountExternalId);
        MandateExternalId mandateExternalId = mandate.getExternalId();
        List<Map<String, Object>> dataLinks = createLinks(mandate, accountExternalId, uriInfo);

        return new CreateMandateResponse(
                mandateExternalId,
                mandate.getReturnUrl(),
                mandate.getCreatedDate(),
                mandate.getState().toExternal(),
                dataLinks,
                mandate.getServiceReference(),
                mandate.getMandateBankStatementReference());
    }

    public TokenExchangeDetails getMandateFor(String token) {
        Mandate mandate = mandateDao
                .findByTokenId(token)
                .map(mandateStateUpdateService::tokenExchangedFor)
                .orElseThrow(TokenNotFoundException::new);

        return new TokenExchangeDetails(mandate);

    }

    public DirectDebitInfoFrontendResponse populateGetMandateWithTransactionResponseForFrontend(String accountExternalId, String transactionExternalId) {
        Transaction transaction = transactionService.findTransactionForExternalId(transactionExternalId);
        Mandate mandate = transaction.getMandate();
        return new DirectDebitInfoFrontendResponse(
                mandate.getExternalId(),
                mandate.getGatewayAccount().getId(),
                accountExternalId,
                mandate.getState(),
                mandate.getReturnUrl(),
                mandate.getMandateBankStatementReference(),
                mandate.getCreatedDate(),
                mandate.getPayer(),
                transaction
        );
    }

    public DirectDebitInfoFrontendResponse populateGetMandateResponseForFrontend(String accountExternalId, MandateExternalId mandateExternalId) {
        Mandate mandate = findByExternalId(mandateExternalId);
        return new DirectDebitInfoFrontendResponse(
                mandate.getExternalId(),
                mandate.getGatewayAccount().getId(),
                accountExternalId,
                mandate.getState(),
                mandate.getReturnUrl(),
                mandate.getMandateBankStatementReference(),
                mandate.getCreatedDate(),
                mandate.getPayer(),
                null
        );
    }

    public GetMandateResponse populateGetMandateResponse(String accountExternalId, MandateExternalId mandateExternalId, UriInfo uriInfo) {
        Mandate mandate = findByExternalId(mandateExternalId);
        List<Map<String, Object>> dataLinks = createLinks(mandate, accountExternalId, uriInfo);

        return new GetMandateResponse(
                mandateExternalId,
                mandate.getReturnUrl(),
                dataLinks,
                mandate.getState().toExternal(),
                mandate.getServiceReference(),
                mandate.getMandateBankStatementReference());
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
        mandateStateUpdateService.changePaymentMethodFor(mandate);
    }

    public void cancelMandateCreation(MandateExternalId mandateExternalId) {
        Mandate mandate = findByExternalId(mandateExternalId);
        mandateStateUpdateService.cancelMandateCreation(mandate);
    }

    public void confirm(GatewayAccount gatewayAccount, Mandate mandate, ConfirmMandateRequest confirmDetailsRequest) {

        if (mandateStateUpdateService.canUpdateStateFor(mandate, DIRECT_DEBIT_DETAILS_CONFIRMED)) {
            PaymentProviderMandateIdAndBankReference paymentProviderMandateIdAndBankReference = paymentProviderFactory
                    .getCommandServiceFor(gatewayAccount.getPaymentProvider())
                    .confirmMandate(
                            mandate,
                            new BankAccountDetails(
                                    confirmDetailsRequest.getAccountNumber(),
                                    confirmDetailsRequest.getSortCode())
                    );
            mandate.setMandateBankStatementReference(paymentProviderMandateIdAndBankReference.getMandateBankStatementReference());
            mandate.setPaymentProviderMandateId(paymentProviderMandateIdAndBankReference.getPaymentProviderMandateId());
            mandateStateUpdateService.confirmedOnDemandDirectDebitDetailsFor(mandate);
        } else {
            throw new InvalidStateTransitionException(DIRECT_DEBIT_DETAILS_CONFIRMED.toString(), mandate.getState().toString());
        }
    }

    private List<Map<String, Object>> createLinks(Mandate mandate, String accountExternalId, UriInfo uriInfo) {
        List<Map<String, Object>> dataLinks = new ArrayList<>();

        dataLinks.add(createLink("self", GET, selfUriFor(uriInfo,
                "/v1/api/accounts/{accountId}/mandates/{mandateExternalId}",
                accountExternalId,
                mandate.getExternalId().toString())));

        if (!mandate.getState().toExternal().isFinished()) {
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
