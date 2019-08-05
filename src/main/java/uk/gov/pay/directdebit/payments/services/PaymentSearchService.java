package uk.gov.pay.directdebit.payments.services;

import uk.gov.pay.directdebit.common.model.SearchResponse;
import uk.gov.pay.directdebit.payments.api.PaymentResponse;
import uk.gov.pay.directdebit.payments.dao.PaymentViewDao;
import uk.gov.pay.directdebit.payments.model.LinksForSearchResult;
import uk.gov.pay.directdebit.payments.params.PaymentViewSearchParams;

import javax.inject.Inject;
import javax.ws.rs.core.UriInfo;
import java.util.Collections;
import java.util.List;

public class PaymentSearchService {

    private final PaymentViewDao paymentViewDao;
    private UriInfo uriInfo;

    @Inject
    PaymentSearchService(PaymentViewDao paymentViewDao) {
        this.paymentViewDao = paymentViewDao;
    }

    public SearchResponse<PaymentResponse> getPaymentSearchResponse(PaymentViewSearchParams searchParams, String gatewayAccountExternalId) {
        int totalMatchingPayments = paymentViewDao.getPaymentViewCount(searchParams, gatewayAccountExternalId);

        List<PaymentResponse> paymentsForRequestedPage = 
                totalMatchingPayments > 0 
                        ? paymentViewDao.searchPaymentView(searchParams, gatewayAccountExternalId) 
                        : Collections.emptyList();

        LinksForSearchResult linksForSearchResult = new LinksForSearchResult(
                searchParams, uriInfo, totalMatchingPayments, gatewayAccountExternalId);
        
        return new SearchResponse<>(gatewayAccountExternalId,
                totalMatchingPayments,
                searchParams.getPage(),
                paymentsForRequestedPage,
                linksForSearchResult);
    }

    public PaymentSearchService withUriInfo(UriInfo uriInfo) {
        this.uriInfo = uriInfo;
        return this;
    }
}
