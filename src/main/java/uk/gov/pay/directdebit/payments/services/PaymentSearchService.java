package uk.gov.pay.directdebit.payments.services;

import uk.gov.pay.directdebit.gatewayaccounts.dao.GatewayAccountDao;
import uk.gov.pay.directdebit.gatewayaccounts.exception.GatewayAccountNotFoundException;
import uk.gov.pay.directdebit.payments.api.PaymentResponse;
import uk.gov.pay.directdebit.payments.api.PaymentViewResponse;
import uk.gov.pay.directdebit.payments.api.PaymentViewValidator;
import uk.gov.pay.directdebit.payments.dao.PaymentViewDao;
import uk.gov.pay.directdebit.payments.model.ViewPaginationBuilder;
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
    public PaymentSearchService(PaymentViewDao paymentViewDao, GatewayAccountDao gatewayAccountDao) {
        this.paymentViewDao = paymentViewDao;
        this.gatewayAccountDao = gatewayAccountDao;
    }

    public PaymentViewResponse getPaymentSearchResponse(PaymentViewSearchParams searchParams) {
        return gatewayAccountDao.findByExternalId(searchParams.getGatewayExternalId())
                .map(gatewayAccount -> {
                    PaymentViewSearchParams validatedSearchParams = paymentViewValidator.validateParams(searchParams);
                    List<PaymentResponse> viewListResponse = Collections.emptyList();
                    Long total = getTotal(validatedSearchParams);
                    if (total > 0) {
                        viewListResponse = getPaymentViewResultResponse(validatedSearchParams);
                    }
                    ViewPaginationBuilder paginationBuilder = new ViewPaginationBuilder(validatedSearchParams, uriInfo);
                    return new PaymentViewResponse(validatedSearchParams.getGatewayExternalId(),
                            total,
                            validatedSearchParams.getPage(),
                            viewListResponse)
                            .withPaginationBuilder(paginationBuilder
                                    .withTotalCount(total)
                                    .buildResponse());

                })
                .orElseThrow(() -> new GatewayAccountNotFoundException(searchParams.getGatewayExternalId()));
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
