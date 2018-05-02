package uk.gov.pay.directdebit.payments.services;

import org.apache.commons.lang3.tuple.Pair;
import uk.gov.pay.directdebit.payments.api.PaymentViewListResponse;
import uk.gov.pay.directdebit.payments.dao.PaymentViewDao;
import uk.gov.pay.directdebit.payments.dao.PaymentViewSearchParams;
import uk.gov.pay.directdebit.payments.model.PaymentView;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

import static uk.gov.pay.directdebit.payments.api.PaymentViewValidator.validatePagination;

public class PaymentViewService {

    private final PaymentViewDao paymentViewDao;

    @Inject
    public PaymentViewService(PaymentViewDao paymentViewDao) {
        this.paymentViewDao = paymentViewDao;
    }

    public List<PaymentViewListResponse> getPaymentViewListResponse(String gatewayAccountId, PaymentViewSearchParams paymentViewSearchParams) {
        Pair<Long, Long> pagination = validatePagination(paymentViewSearchParams.getPaginationParams());
        return paymentViewDao.searchPaymentView(gatewayAccountId, pagination.getLeft(), pagination.getRight())
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
