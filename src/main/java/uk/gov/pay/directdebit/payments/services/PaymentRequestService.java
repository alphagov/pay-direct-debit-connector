package uk.gov.pay.directdebit.payments.services;

import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import uk.gov.pay.directdebit.app.config.DirectDebitConfig;
import uk.gov.pay.directdebit.app.config.LinksConfig;
import uk.gov.pay.directdebit.app.logger.PayLoggerFactory;
import uk.gov.pay.directdebit.payments.api.PaymentRequestResponse;
import uk.gov.pay.directdebit.payments.dao.PaymentRequestDao;
import uk.gov.pay.directdebit.payments.exception.PaymentRequestNotFoundException;
import uk.gov.pay.directdebit.payments.model.PaymentRequest;
import uk.gov.pay.directdebit.payments.model.Token;
import uk.gov.pay.directdebit.payments.model.Transaction;
import uk.gov.pay.directdebit.tokens.services.TokenService;

import javax.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static javax.ws.rs.HttpMethod.GET;
import static javax.ws.rs.HttpMethod.POST;
import static javax.ws.rs.core.MediaType.APPLICATION_FORM_URLENCODED;
import static uk.gov.pay.directdebit.common.util.URIBuilder.*;
import static uk.gov.pay.directdebit.common.util.URIBuilder.nextUrl;
import static uk.gov.pay.directdebit.payments.resources.PaymentRequestResource.CHARGE_API_PATH;

public class PaymentRequestService {
    private static final Logger LOGGER = PayLoggerFactory.getLogger(PaymentRequestService.class);

    private final LinksConfig linksConfig;
    private final PaymentRequestDao paymentRequestDao;
    private final TokenService tokenService;
    private final TransactionService transactionService;

    public PaymentRequestService(DirectDebitConfig config, PaymentRequestDao paymentRequestDao, TokenService tokenService, TransactionService transactionService) {
        this.paymentRequestDao = paymentRequestDao;
        this.tokenService = tokenService;
        this.transactionService = transactionService;
        this.linksConfig = config.getLinks();
    }

    private PaymentRequestResponse populateResponseWith(PaymentRequest paymentRequest, Transaction charge, UriInfo uriInfo) {
        String paymentRequestExternalId = paymentRequest.getExternalId();
        List<Map<String, Object>> dataLinks = new ArrayList<>();

        dataLinks.add(createLink("self", GET, selfUriFor(uriInfo, CHARGE_API_PATH, String.valueOf(paymentRequest.getGatewayAccountId()), paymentRequestExternalId)));

        if (!charge.getState().toExternal().isFinished()) {
            Token token = tokenService.generateNewTokenFor(paymentRequest);
            dataLinks.add(createLink("next_url",
                    GET,
                    nextUrl(linksConfig.getFrontendUrl(), "secure", token.getToken())));
            dataLinks.add(createLink("next_url_post",
                    POST,
                    nextUrl(linksConfig.getFrontendUrl(), "secure"),
                    APPLICATION_FORM_URLENCODED,
                    ImmutableMap.of("token", token.getToken())));
        }
        return new PaymentRequestResponse(paymentRequestExternalId,
                paymentRequest.getAmount(),
                paymentRequest.getReturnUrl(),
                paymentRequest.getDescription(),
                paymentRequest.getReference(),
                paymentRequest.getCreatedDate().toString(),
                dataLinks);
    }

    public PaymentRequestResponse createCharge(Map<String, String> paymentRequestMap, Long accountId, UriInfo uriInfo) {
        //todo when we check the account id, return  notFoundResponse("Unknown gateway account: " + accountId)) if not found
        PaymentRequest paymentRequest = new PaymentRequest(new Long(paymentRequestMap.get("amount")),
                paymentRequestMap.get("return_url"),
                accountId,
                paymentRequestMap.get("description"),
                paymentRequestMap.get("reference"));
        LOGGER.info("Creating payment request with external id {}", paymentRequest.getExternalId());
        Long id = paymentRequestDao.insert(paymentRequest);
        paymentRequest.setId(id);
        Transaction createdTransaction = transactionService.createChargeFor(paymentRequest);
        return populateResponseWith(paymentRequest, createdTransaction, uriInfo);
    }


    //todo refactor this to get the transaction immediately and put the gateway account id there
    public PaymentRequestResponse getPaymentWithExternalId(String paymentExternalId, UriInfo uriInfo) {
        return paymentRequestDao
                .findByExternalId(paymentExternalId)
                .map(paymentRequest ->  {
                    Transaction transaction = transactionService.findChargeForExternalId(paymentExternalId);
                    return populateResponseWith(paymentRequest, transaction, uriInfo);
                })
                .orElseThrow(() -> new PaymentRequestNotFoundException(paymentExternalId));
    }

}
