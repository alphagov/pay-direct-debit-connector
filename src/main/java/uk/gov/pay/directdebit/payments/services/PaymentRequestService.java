package uk.gov.pay.directdebit.payments.services;

import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import uk.gov.pay.directdebit.app.config.DirectDebitConfig;
import uk.gov.pay.directdebit.app.config.LinksConfig;
import uk.gov.pay.directdebit.app.logger.PayLoggerFactory;
import uk.gov.pay.directdebit.gatewayaccounts.dao.GatewayAccountDao;
import uk.gov.pay.directdebit.gatewayaccounts.exception.GatewayAccountNotFoundException;
import uk.gov.pay.directdebit.payments.api.PaymentRequestFrontendResponse;
import uk.gov.pay.directdebit.payments.api.PaymentRequestResponse;
import uk.gov.pay.directdebit.payments.dao.PaymentRequestDao;
import uk.gov.pay.directdebit.payments.exception.PaymentRequestNotFoundException;
import uk.gov.pay.directdebit.payments.model.PaymentRequest;
import uk.gov.pay.directdebit.payments.model.Token;
import uk.gov.pay.directdebit.payments.model.Transaction;
import uk.gov.pay.directdebit.tokens.services.TokenService;

import javax.inject.Inject;
import javax.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static javax.ws.rs.HttpMethod.GET;
import static javax.ws.rs.HttpMethod.POST;
import static javax.ws.rs.core.MediaType.APPLICATION_FORM_URLENCODED;
import static uk.gov.pay.directdebit.common.util.URIBuilder.createLink;
import static uk.gov.pay.directdebit.common.util.URIBuilder.nextUrl;
import static uk.gov.pay.directdebit.common.util.URIBuilder.selfUriFor;
import static uk.gov.pay.directdebit.payments.resources.PaymentRequestResource.CHARGE_API_PATH;

public class PaymentRequestService {
    private static final Logger LOGGER = PayLoggerFactory.getLogger(PaymentRequestService.class);

    private final LinksConfig linksConfig;
    private final PaymentRequestDao paymentRequestDao;
    private final TokenService tokenService;
    private final TransactionService transactionService;
    private final GatewayAccountDao gatewayAccountDao;

    @Inject
    public PaymentRequestService(DirectDebitConfig config, PaymentRequestDao paymentRequestDao, TokenService tokenService, TransactionService transactionService, GatewayAccountDao gatewayAccountDao) {
        this.paymentRequestDao = paymentRequestDao;
        this.tokenService = tokenService;
        this.transactionService = transactionService;
        this.gatewayAccountDao = gatewayAccountDao;
        this.linksConfig = config.getLinks();
    }

    private PaymentRequestResponse populateResponseWith(PaymentRequest paymentRequest, String accountExternalId, Transaction transaction, UriInfo uriInfo) {
        String paymentRequestExternalId = paymentRequest.getExternalId();
        List<Map<String, Object>> dataLinks = new ArrayList<>();

        dataLinks.add(createLink("self", GET, selfUriFor(uriInfo, CHARGE_API_PATH, accountExternalId, paymentRequestExternalId)));

        if (!transaction.getState().toExternal().isFinished()) {
            Token token = tokenService.generateNewTokenFor(paymentRequest);
            dataLinks.add(createLink("next_url",
                    GET,
                    nextUrl(linksConfig.getFrontendUrl(), "secure", token.getToken())));
            dataLinks.add(createLink("next_url_post",
                    POST,
                    nextUrl(linksConfig.getFrontendUrl(), "secure"),
                    APPLICATION_FORM_URLENCODED,
                    ImmutableMap.of("chargeTokenId", token.getToken())));
        }
        return new PaymentRequestResponse(paymentRequestExternalId,
                transaction.getState().toExternal(),
                paymentRequest.getAmount(),
                paymentRequest.getReturnUrl(),
                paymentRequest.getDescription(),
                paymentRequest.getReference(),
                paymentRequest.getCreatedDate().toString(),
                dataLinks);
    }

    public PaymentRequestResponse createTransaction(Map<String, String> paymentRequestMap, String accountExternalId, UriInfo uriInfo) {
        return gatewayAccountDao.findByExternalId(accountExternalId)
                .map(gatewayAccount -> {
                    PaymentRequest paymentRequest = new PaymentRequest(new Long(paymentRequestMap.get("amount")),
                            paymentRequestMap.get("return_url"),
                            gatewayAccount.getId(),
                            paymentRequestMap.get("description"),
                            paymentRequestMap.get("reference"));
                    LOGGER.info("Creating payment request with external id {}", paymentRequest.getExternalId());
                    Long id = paymentRequestDao.insert(paymentRequest);
                    paymentRequest.setId(id);
                    Transaction createdTransaction = transactionService.createChargeFor(paymentRequest, gatewayAccount);
                    return populateResponseWith(paymentRequest, accountExternalId, createdTransaction, uriInfo);
                })
                .orElseThrow(() -> {
                    LOGGER.error("Gateway account with id {} not found", accountExternalId);
                    return new GatewayAccountNotFoundException(accountExternalId);
                });
    }

    public void cancelTransaction(String accountExternalId, String paymentRequestExternalId) {
        Transaction transaction = transactionService.findTransactionForExternalIdAndGatewayAccountExternalId(paymentRequestExternalId, accountExternalId);
        transactionService.paymentCancelledFor(transaction);
    }

    public PaymentRequestResponse getPaymentWithExternalId(String accountExternalId, String paymentExternalId, UriInfo uriInfo) {
        return paymentRequestDao
                .findByExternalIdAndAccountExternalId(paymentExternalId, accountExternalId)
                .map(paymentRequest ->  {
                    Transaction transaction = transactionService.findTransactionForExternalIdAndGatewayAccountExternalId(paymentExternalId, accountExternalId);
                    return populateResponseWith(paymentRequest, accountExternalId, transaction, uriInfo);
                })
                .orElseThrow(() -> new PaymentRequestNotFoundException(paymentExternalId, accountExternalId));
    }

    public PaymentRequestFrontendResponse getPaymentWithExternalId(String accountExternalId, String paymentRequestExternalId) {
        return paymentRequestDao
                .findByExternalIdAndAccountExternalId(paymentRequestExternalId, accountExternalId)
                .map(paymentRequest ->  {
                    Transaction transaction = transactionService.findTransactionForExternalIdAndGatewayAccountExternalId(paymentRequestExternalId, accountExternalId);
                    // payer will be populated in the next PR
                    return new PaymentRequestFrontendResponse(
                            paymentRequest.getExternalId(),
                            paymentRequest.getGatewayAccountId(),
                            accountExternalId,
                            transaction.getState().toExternal(),
                            paymentRequest.getAmount(),
                            paymentRequest.getReturnUrl(),
                            paymentRequest.getDescription(),
                            paymentRequest.getReference(),
                            paymentRequest.getCreatedDate().toString(),
                            null);
                })
                .orElseThrow(() -> new PaymentRequestNotFoundException(paymentRequestExternalId, accountExternalId));
    }

    public void changePaymentMethod(String accountExternalId, String paymentRequestExternalId) {
        Transaction transaction = transactionService.findTransactionForExternalIdAndGatewayAccountExternalId(paymentRequestExternalId, accountExternalId);
        transactionService.paymentMethodChangedFor(transaction);
    }

}
