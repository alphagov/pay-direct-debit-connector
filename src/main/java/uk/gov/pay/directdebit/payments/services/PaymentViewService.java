package uk.gov.pay.directdebit.payments.services;

import uk.gov.pay.directdebit.payments.api.PaymentViewResponse;
import uk.gov.pay.directdebit.payments.dao.PaymentViewDao;
import uk.gov.pay.directdebit.payments.model.PaymentView;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

public class PaymentViewService {

    private final PaymentViewDao paymentViewDao;
    
    @Inject
    public PaymentViewService(PaymentViewDao paymentViewDao) {
        this.paymentViewDao = paymentViewDao;
    }
    
    public List<PaymentViewResponse> getPaymentViewResponse(String gatewayAccountId, Long offset, Long pageSize) {
        return paymentViewDao.searchPaymentView(gatewayAccountId, offset, pageSize)
                .stream()
                .map(paymentView -> populateResponseWith(paymentView))
                .collect(Collectors.toList());
    }
    
    private PaymentViewResponse populateResponseWith(PaymentView paymentView) {
        return new PaymentViewResponse(
                paymentView.getGatewayExternalId(),
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
