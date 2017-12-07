package uk.gov.pay.directdebit.payments.services;

import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.directdebit.app.config.DirectDebitConfig;
import uk.gov.pay.directdebit.app.config.LinksConfig;
import uk.gov.pay.directdebit.app.logger.PayLoggerFactory;
import uk.gov.pay.directdebit.payments.api.PaymentRequestResponse;
import uk.gov.pay.directdebit.payments.dao.PaymentRequestDao;
import uk.gov.pay.directdebit.payments.dao.PaymentRequestEventDao;
import uk.gov.pay.directdebit.payments.dao.TokenDao;
import uk.gov.pay.directdebit.payments.dao.TransactionDao;
import uk.gov.pay.directdebit.payments.exception.ChargeNotFoundException;
import uk.gov.pay.directdebit.payments.exception.PaymentRequestNotFoundException;
import uk.gov.pay.directdebit.payments.model.*;

import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static javax.ws.rs.HttpMethod.GET;
import static javax.ws.rs.HttpMethod.POST;
import static javax.ws.rs.core.MediaType.APPLICATION_FORM_URLENCODED;
import static uk.gov.pay.directdebit.payments.resources.PaymentRequestResource.CHARGE_API_PATH;

public class PaymentRequestService {
    private static final Logger logger = PayLoggerFactory.getLogger(PaymentRequestService.class);

    private final LinksConfig linksConfig;
    private final TokenDao tokenDao;
    private final PaymentRequestDao paymentRequestDao;
    private final PaymentRequestEventDao paymentRequestEventDao;
    private final TransactionDao transactionDao;

    public PaymentRequestService(DirectDebitConfig config, PaymentRequestDao paymentRequestDao, TokenDao tokenDao, PaymentRequestEventDao paymentRequestEventDao, TransactionDao transactionDao) {
        this.paymentRequestDao = paymentRequestDao;
        this.paymentRequestEventDao = paymentRequestEventDao;
        this.tokenDao = tokenDao;
        this.transactionDao = transactionDao;
        this.linksConfig = config.getLinks();
    }
    private Map<String, Object> createLink(String rel, String method, URI href) {
        return ImmutableMap.of(
                "rel", rel,
                "method", method,
                "href", href
        );
    }

    private Map<String, Object> createLink(String rel, String method, URI href, String type, Map<String, Object> params) {
        return ImmutableMap.of(
                "rel", rel,
                "method", method,
                "href", href,
                "type", type,
                "params", params
        );
    }

    public PaymentRequestResponse populateResponseWith(PaymentRequest paymentRequest, Transaction charge, UriInfo uriInfo) {
        String paymentRequestExternalId = paymentRequest.getExternalId();
        List<Map<String, Object>> dataLinks = new ArrayList<>();
        dataLinks.add(createLink("self", GET, selfUriFor(uriInfo, paymentRequest.getGatewayAccountId(), paymentRequestExternalId)));
        if (!charge.getState().toExternal().isFinished()) {
            Token token = Token.generateNewTokenFor(paymentRequest.getId());
            tokenDao.insert(token);

            dataLinks.add(createLink("next_url", GET, nextUrl(token.getToken())));
            dataLinks.add(createLink("next_url_post", POST, nextUrl(), APPLICATION_FORM_URLENCODED, new HashMap<String, Object>() {{
                        put("chargeTokenId", token.getToken());
                    }})
            );
        }
        return new PaymentRequestResponse(
                paymentRequestExternalId,
                paymentRequest.getAmount(),
                paymentRequest.getReturnUrl(),
                paymentRequest.getDescription(),
                paymentRequest.getReference(),
                paymentRequest.getCreatedDate().toString(),
                dataLinks);
    }

    public PaymentRequestResponse create(Map<String, String> paymentRequestMap, Long accountId, UriInfo uriInfo) {
        //todo when we check the account id, return  notFoundResponse("Unknown gateway account: " + accountId)) if not found
        PaymentRequest paymentRequest = new PaymentRequest(
                new Long(paymentRequestMap.get("amount")),
                paymentRequestMap.get("return_url"),
                accountId,
                paymentRequestMap.get("description"),
                paymentRequestMap.get("reference"));
        Long id = paymentRequestDao.insert(paymentRequest);
        paymentRequest.setId(id);
        insertCreatedEventFor(paymentRequest);
        Transaction createdTransaction = insertCreatedTransactionFor(paymentRequest);
        return populateResponseWith(paymentRequest, createdTransaction, uriInfo);
    }
    private URI selfUriFor(UriInfo uriInfo, Long accountId, String chargeId) {
        return uriInfo.getBaseUriBuilder()
                .path(CHARGE_API_PATH)
                .build(accountId, chargeId);
    }

    private URI nextUrl(String tokenId) {
        return UriBuilder.fromUri(linksConfig.getFrontendUrl())
                .path("secure")
                .path(tokenId)
                .build();
    }

    private URI nextUrl() {
        return UriBuilder.fromUri(linksConfig.getFrontendUrl())
                .path("secure")
                .build();
    }

    public PaymentRequestResponse getPaymentWithExternalId(String paymentExternalId, UriInfo uriInfo) {
        return paymentRequestDao
                .findByExternalId(paymentExternalId)
                .map(paymentRequest ->  {
                    Transaction transaction = transactionDao.findByPaymentRequestId(paymentRequest.getId())
                            .orElseThrow(() -> new ChargeNotFoundException(paymentExternalId));
                    logger.info("Found charge for payment request with id: {}", paymentRequest.getExternalId());
                    return populateResponseWith(paymentRequest, transaction, uriInfo);
                })
                .orElseThrow(() -> new PaymentRequestNotFoundException(paymentExternalId));
    }


    private void insertCreatedEventFor(PaymentRequest paymentRequest) {
        PaymentRequestEvent paymentRequestEvent = new PaymentRequestEvent(
                paymentRequest.getId(),
                PaymentRequestEvent.Type.CHARGE,
                PaymentRequestEvent.SupportedEvent.CHARGE_CREATED,
                ZonedDateTime.now());
        logger.info("Created event for payment request {}", paymentRequest.getExternalId());
        paymentRequestEventDao.insert(paymentRequestEvent);
    }
    private Transaction insertCreatedTransactionFor(PaymentRequest paymentRequest) {
        Transaction transaction = new Transaction(
                paymentRequest.getId(),
                paymentRequest.getAmount(),
                Transaction.Type.CHARGE,
                PaymentStatesGraph.initialState());
        logger.info("Created transaction for payment request {}", paymentRequest.getExternalId());
        transactionDao.insert(transaction);
        return transaction;
    }
}
