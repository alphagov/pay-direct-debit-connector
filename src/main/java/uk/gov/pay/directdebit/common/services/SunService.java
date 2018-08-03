package uk.gov.pay.directdebit.common.services;

import uk.gov.pay.directdebit.common.model.subtype.SunName;
import uk.gov.pay.directdebit.mandate.model.Mandate;
import uk.gov.pay.directdebit.payments.model.DirectDebitPaymentProviderCommandService;
import uk.gov.pay.directdebit.payments.model.PaymentProviderFactory;

import javax.inject.Inject;
import java.util.Optional;

public class SunService {

    private final PaymentProviderFactory paymentProviderFactory;

    @Inject
    public SunService(PaymentProviderFactory paymentProviderFactory) {
        this.paymentProviderFactory = paymentProviderFactory;
    }

    public Optional<SunName> getSunNameFor(Mandate mandate) {
        DirectDebitPaymentProviderCommandService paymentProviderCommandService =
                paymentProviderFactory.getCommandServiceFor(mandate.getGatewayAccount().getPaymentProvider());
        return paymentProviderCommandService.getSunName(mandate);
    }

}
