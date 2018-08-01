package uk.gov.pay.directdebit.mandate.services;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import uk.gov.pay.directdebit.app.config.DirectDebitConfig;
import uk.gov.pay.directdebit.app.config.LinksConfig;
import uk.gov.pay.directdebit.app.logger.PayLoggerFactory;
import uk.gov.pay.directdebit.common.util.RandomIdGenerator;
import uk.gov.pay.directdebit.gatewayaccounts.dao.GatewayAccountDao;
import uk.gov.pay.directdebit.gatewayaccounts.exception.GatewayAccountNotFoundException;
import uk.gov.pay.directdebit.gatewayaccounts.model.PaymentProvider;
import uk.gov.pay.directdebit.mandate.api.CreateMandateRequest;
import uk.gov.pay.directdebit.mandate.api.CreateMandateResponse;
import uk.gov.pay.directdebit.mandate.api.CreateRequest;
import uk.gov.pay.directdebit.mandate.api.DirectDebitInfoFrontendResponse;
import uk.gov.pay.directdebit.mandate.api.GetMandateResponse;
import uk.gov.pay.directdebit.mandate.dao.MandateDao;
import uk.gov.pay.directdebit.mandate.exception.MandateNotFoundException;
import uk.gov.pay.directdebit.mandate.exception.WrongNumberOfTransactionsForOneOffMandateException;
import uk.gov.pay.directdebit.mandate.model.Mandate;
import uk.gov.pay.directdebit.mandate.model.MandateState;
import uk.gov.pay.directdebit.mandate.model.MandateType;
import uk.gov.pay.directdebit.mandate.model.subtype.MandateExternalId;
import uk.gov.pay.directdebit.payments.model.DirectDebitEvent;
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

public class MandateService {

    private static final Logger LOGGER = PayLoggerFactory.getLogger(MandateService.class);
    private final MandateDao mandateDao;
    private final LinksConfig linksConfig;
    private final GatewayAccountDao gatewayAccountDao;
    private final TokenService tokenService;
    private final TransactionService transactionService;
    private final MandateStateUpdateService mandateStateUpdateService;

    @Inject
    public MandateService(
            DirectDebitConfig directDebitConfig,
            MandateDao mandateDao, GatewayAccountDao gatewayAccountDao,
            TokenService tokenService,
            TransactionService transactionService,
            MandateStateUpdateService mandateStateUpdateService) {
        this.gatewayAccountDao = gatewayAccountDao;
        this.tokenService = tokenService;
        this.transactionService = transactionService;
        this.mandateDao = mandateDao;
        this.mandateStateUpdateService = mandateStateUpdateService;
        this.linksConfig = directDebitConfig.getLinks();
    }

    public Mandate createMandate(CreateRequest createRequest, String accountExternalId) {
        return gatewayAccountDao.findByExternalId(accountExternalId)
                .map(gatewayAccount -> {
                    // TODO:
                    // when we introduce GoCardless gateway accounts to work with create mandate,
                    // then modify appropriate mandate reference values
                    String mandateReference = PaymentProvider.SANDBOX.equals(gatewayAccount.getPaymentProvider()) ?
                            RandomStringUtils.randomAlphanumeric(18) :
                            "gocardless-default";

                    Mandate mandate = new Mandate(
                            null,
                            gatewayAccount,
                            createRequest.getMandateType(),
                            MandateExternalId.of(RandomIdGenerator.newId()),
                            mandateReference,
                            createRequest.getReference(),
                            MandateState.CREATED,
                            createRequest.getReturnUrl(),
                            ZonedDateTime.now(ZoneOffset.UTC),
                            null);
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
                mandate.getType(),
                mandate.getReturnUrl(),
                mandate.getCreatedDate().toString(),
                mandate.getState().toExternal(),
                dataLinks,
                mandate.getServiceReference(),
                mandate.getMandateReference());
    }

    public TokenExchangeDetails getMandateFor(String token) {
        Mandate mandate = mandateDao
                .findByTokenId(token)
                .map(mandateStateUpdateService::tokenExchangedFor)
                .orElseThrow(TokenNotFoundException::new);

        String transactionExternalId = mandate.getType().equals(MandateType.ONE_OFF)
                ? retrieveTransactionForOneOffMandate(mandate.getExternalId()).getExternalId()
                : null;
        return new TokenExchangeDetails(mandate, transactionExternalId);

    }

    public DirectDebitInfoFrontendResponse populateGetMandateWithTransactionResponseForFrontend(String accountExternalId, String transactionExternalId) {
        Transaction transaction = transactionService
                .findTransactionForExternalId(transactionExternalId);
        Mandate mandate = transaction.getMandate();
        return new DirectDebitInfoFrontendResponse(
                mandate.getExternalId(),
                mandate.getGatewayAccount().getId(),
                accountExternalId,
                mandate.getState(),
                mandate.getReturnUrl(),
                mandate.getMandateReference(),
                mandate.getType().toString(),
                mandate.getCreatedDate().toString(),
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
                mandate.getMandateReference(),
                mandate.getType().toString(),
                mandate.getCreatedDate().toString(),
                mandate.getPayer(),
                null
        );
    }

    public GetMandateResponse populateGetMandateResponse(String accountExternalId, MandateExternalId mandateExternalId, UriInfo uriInfo) {
        Mandate mandate = findByExternalId(mandateExternalId);
        List<Map<String, Object>> dataLinks = createLinks(mandate, accountExternalId, uriInfo);

        return new GetMandateResponse(
                mandateExternalId,
                mandate.getType(),
                mandate.getReturnUrl(),
                dataLinks,
                mandate.getState().toExternal(),
                mandate.getServiceReference(),
                mandate.getMandateReference());
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

    public DirectDebitEvent changePaymentMethodFor(MandateExternalId mandateExternalId) {
        Mandate mandate = findByExternalId(mandateExternalId);
        if (MandateType.ONE_OFF.equals(mandate.getType())) {
            Transaction transaction = retrieveTransactionForOneOffMandate(mandateExternalId);
            transactionService.paymentMethodChangedFor(transaction);
        }
        return mandateStateUpdateService.changePaymentMethodFor(mandate);
    }

    public DirectDebitEvent cancelMandateCreation(MandateExternalId mandateExternalId) {
        Mandate mandate = findByExternalId(mandateExternalId);
        if (MandateType.ONE_OFF.equals(mandate.getType())) {
            Transaction transaction = retrieveTransactionForOneOffMandate(mandateExternalId);
            transactionService.paymentCancelledFor(transaction);
        }
        return mandateStateUpdateService.cancelMandateCreation(mandate);
    }

    private Transaction retrieveTransactionForOneOffMandate(MandateExternalId mandateExternalId) {
        List<Transaction> transactions = transactionService.findTransactionsForMandate(mandateExternalId);
        if (transactions.size() != 1) {
            throw new WrongNumberOfTransactionsForOneOffMandateException("Found zero or multiple transactions for one off mandate with external id " + mandateExternalId);
        }
        return transactions.get(0);
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