package uk.gov.pay.directdebit.payments.services;

import uk.gov.pay.directdebit.common.model.SearchResponse;
import uk.gov.pay.directdebit.gatewayaccounts.dao.GatewayAccountDao;
import uk.gov.pay.directdebit.gatewayaccounts.exception.GatewayAccountNotFoundException;
import uk.gov.pay.directdebit.payments.api.PaymentResponse;
import uk.gov.pay.directdebit.payments.api.PaymentViewValidator;
import uk.gov.pay.directdebit.payments.dao.PaymentViewDao;
import uk.gov.pay.directdebit.payments.model.LinksForSearchResult;
import uk.gov.pay.directdebit.payments.params.PaymentViewSearchParams;

import javax.inject.Inject;
import javax.ws.rs.core.UriInfo;
import java.util.Collections;
import java.util.List;

public class PaymentSearchService {

    private final PaymentViewDao paymentViewDao;
    private final GatewayAccountDao gatewayAccountDao;
    private final PaymentViewValidator paymentViewValidator = new PaymentViewValidator();
    private UriInfo uriInfo;

    @Inject
    PaymentSearchService(PaymentViewDao paymentViewDao, GatewayAccountDao gatewayAccountDao) {
        this.paymentViewDao = paymentViewDao;
        this.gatewayAccountDao = gatewayAccountDao;
    }

    public SearchResponse<PaymentResponse> getPaymentSearchResponse(PaymentViewSearchParams searchParams) {
        gatewayAccountDao.findByExternalId(searchParams.getGatewayExternalId())
                .orElseThrow(() -> new GatewayAccountNotFoundException(searchParams.getGatewayExternalId()));

        PaymentViewSearchParams validatedSearchParams = paymentViewValidator.validateParams(searchParams);
        Long total = getTotal(validatedSearchParams);
        List<PaymentResponse> foundPayments = total > 0 ? getPaymentViewResultResponse(validatedSearchParams) : Collections.emptyList();
        LinksForSearchResult linksForSearchResult = new LinksForSearchResult(validatedSearchParams, uriInfo, total);
        
        return new SearchResponse<>(validatedSearchParams.getGatewayExternalId(),
                total,
                validatedSearchParams.getPage(),
                foundPayments,
                linksForSearchResult);
    }

    public PaymentSearchService withUriInfo(UriInfo uriInfo) {
        this.uriInfo = uriInfo;
        return this;
    }

    private Long getTotal(PaymentViewSearchParams searchParams) {
        return paymentViewDao.getPaymentViewCount(searchParams);
    }

    private List<PaymentResponse> getPaymentViewResultResponse(PaymentViewSearchParams searchParams) {
        return paymentViewDao.searchPaymentView(searchParams);
    }

}
