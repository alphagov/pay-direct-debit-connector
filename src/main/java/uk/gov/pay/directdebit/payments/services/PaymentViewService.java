package uk.gov.pay.directdebit.payments.services;

import uk.gov.pay.directdebit.gatewayaccounts.dao.GatewayAccountDao;
import uk.gov.pay.directdebit.gatewayaccounts.exception.GatewayAccountNotFoundException;
import uk.gov.pay.directdebit.payments.api.PaymentViewResponse;
import uk.gov.pay.directdebit.payments.api.PaymentViewResultResponse;
import uk.gov.pay.directdebit.payments.api.PaymentViewValidator;
import uk.gov.pay.directdebit.payments.dao.PaymentViewDao;
import uk.gov.pay.directdebit.payments.links.Link;
import uk.gov.pay.directdebit.payments.model.PaymentView;
import uk.gov.pay.directdebit.payments.model.ViewPaginationBuilder;
import uk.gov.pay.directdebit.payments.params.PaymentViewSearchParams;

import javax.inject.Inject;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class PaymentViewService {

    private final PaymentViewDao paymentViewDao;
    private final GatewayAccountDao gatewayAccountDao;
    private final PaymentViewValidator paymentViewValidator = new PaymentViewValidator();
    private UriInfo uriInfo;
    private UriBuilder uriBuilder;

    @Inject
    public PaymentViewService(PaymentViewDao paymentViewDao, GatewayAccountDao gatewayAccountDao) {
        this.paymentViewDao = paymentViewDao;
        this.gatewayAccountDao = gatewayAccountDao;
    }

    public PaymentViewResponse getPaymentViewResponse(PaymentViewSearchParams searchParams) {
        return gatewayAccountDao.findByExternalId(searchParams.getGatewayExternalId())
                .map(gatewayAccount -> {
                    PaymentViewSearchParams validatedSearchParams = paymentViewValidator.validateParams(searchParams);
                    List<PaymentViewResultResponse> viewListResponse = Collections.emptyList();
                    Long total = getTotal(validatedSearchParams);
                    if (total > 0) {
                        viewListResponse =
                                getPaymentViewResultResponse(validatedSearchParams, gatewayAccount.getExternalId());
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
    
    public PaymentViewService withUriInfo(UriInfo uriInfo) {
        this.uriInfo = uriInfo;
        return this;
    }

    private Long getTotal(PaymentViewSearchParams searchParams) {
        return paymentViewDao.getPaymentViewCount(searchParams);
    }

    private List<PaymentViewResultResponse> getPaymentViewResultResponse(PaymentViewSearchParams searchParams, String gatewayAccountId) {
        return paymentViewDao.searchPaymentView(searchParams)
                .stream()
                .map(paymentView -> decorateWithSelfLink(populateResponseWith(paymentView), gatewayAccountId))
                .collect(Collectors.toList());
    }

    private PaymentViewResultResponse populateResponseWith(PaymentView paymentView) {
        return new PaymentViewResultResponse(
                paymentView.getTransactionExternalId(),
                paymentView.getAmount(),
                paymentView.getReference(),
                paymentView.getDescription(),
                paymentView.getCreatedDate().toString(),
                paymentView.getName(),
                paymentView.getEmail(),
                paymentView.getState().toExternal(),
                paymentView.getMandateExternalId());
    }
    
    private PaymentViewResultResponse decorateWithSelfLink(PaymentViewResultResponse listResponse, String gatewayAccountId) {
        if (this.uriBuilder == null) {
            this.uriBuilder = UriBuilder.fromUri(uriInfo.getBaseUri())
            .path("/v1/api/accounts/{accountId}/charges/{transactionExternalId}");
        }
        String href = uriBuilder.build(gatewayAccountId, listResponse.getTransactionId()).toString();
        
        Link link = Link.ofValue(href, "GET", "self");
        return listResponse.withLink(link);
    }
}