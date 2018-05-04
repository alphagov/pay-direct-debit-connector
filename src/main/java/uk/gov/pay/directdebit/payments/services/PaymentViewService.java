package uk.gov.pay.directdebit.payments.services;

import uk.gov.pay.directdebit.gatewayaccounts.dao.GatewayAccountDao;
import uk.gov.pay.directdebit.gatewayaccounts.exception.GatewayAccountNotFoundException;
import uk.gov.pay.directdebit.payments.api.PaymentViewListResponse;
import uk.gov.pay.directdebit.payments.api.PaymentViewResponse;
import uk.gov.pay.directdebit.payments.api.PaymentViewValidator;
import uk.gov.pay.directdebit.payments.dao.PaymentViewDao;
import uk.gov.pay.directdebit.payments.dao.PaymentViewSearchParams;
import uk.gov.pay.directdebit.payments.exception.RecordsNotFoundException;
import uk.gov.pay.directdebit.payments.model.PaymentView;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.String.format;

public class PaymentViewService {

    private final PaymentViewDao paymentViewDao;
    private final GatewayAccountDao gatewayAccountDao;
    private final PaymentViewValidator paymentViewValidator = new PaymentViewValidator();

    @Inject
    public PaymentViewService(PaymentViewDao paymentViewDao, GatewayAccountDao gatewayAccountDao) {
        this.paymentViewDao = paymentViewDao;
        this.gatewayAccountDao = gatewayAccountDao;
    }

    public PaymentViewResponse getPaymentViewResponse(PaymentViewSearchParams searchParams) {
        return gatewayAccountDao.findByExternalId(searchParams.getGatewayExternalId())
                .map(gatewayAccount -> {
                    paymentViewValidator.validateParams(searchParams);
                    List<PaymentViewListResponse> viewListResponse = getPaymentViewListResponse(searchParams);
                    if (viewListResponse.size() == 0) {
                        throw new RecordsNotFoundException(format("Found no records with page size %s and display_size %s",
                                searchParams.getPage(),
                                searchParams.getDisplaySize()));
                    }
                    return new PaymentViewResponse(searchParams.getGatewayExternalId(),
                            searchParams.getPage(),
                            searchParams.getPaginationParams().getDisplaySize(),
                            viewListResponse);

                })
                .orElseThrow(() -> new GatewayAccountNotFoundException(searchParams.getGatewayExternalId()));
    }

    private List<PaymentViewListResponse> getPaymentViewListResponse(PaymentViewSearchParams searchParams) {
        return paymentViewDao.searchPaymentView(searchParams)
                .stream()
                .map(paymentView -> populateResponseWith(paymentView))
                .collect(Collectors.toList());
    }

    private PaymentViewListResponse populateResponseWith(PaymentView paymentView) {
        return new PaymentViewListResponse(
                paymentView.getPaymentRequestExternalId(),
                paymentView.getAmount(),
                paymentView.getReference(),
                paymentView.getDescription(),
                paymentView.getReturnUrl(),
                paymentView.getCreatedDate().toString(),
                paymentView.getName(),
                paymentView.getEmail(),
                paymentView.getState().toExternal()
        );
    }
}
