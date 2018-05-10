package uk.gov.pay.directdebit.mandate.resources;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.noContent;

import java.util.Map;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.directdebit.gatewayaccounts.model.GatewayAccount;
import uk.gov.pay.directdebit.common.validation.BankAccountDetailsValidator;
import uk.gov.pay.directdebit.payments.model.DirectDebitPaymentProvider;
import uk.gov.pay.directdebit.payments.model.PaymentProviderFactory;

@Path("/")
public class ConfirmPaymentResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfirmPaymentResource.class);
    private final PaymentProviderFactory paymentProviderFactory;

    private final BankAccountDetailsValidator bankAccountDetailsValidator = new BankAccountDetailsValidator();

    @Inject
    public ConfirmPaymentResource(PaymentProviderFactory paymentProviderFactory) {
        this.paymentProviderFactory = paymentProviderFactory;
    }

    @POST
    @Path("/v1/api/accounts/{accountId}/payment-requests/{paymentRequestExternalId}/confirm")
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    public Response confirm(@PathParam("accountId") GatewayAccount gatewayAccount, @PathParam("paymentRequestExternalId") String paymentRequestExternalId, Map<String, String> confirmDetailsRequest) {
        LOGGER.info("Confirming payment for payment request with id: {}", paymentRequestExternalId);
        bankAccountDetailsValidator.validate(confirmDetailsRequest);
        DirectDebitPaymentProvider service = paymentProviderFactory.getServiceFor(gatewayAccount.getPaymentProvider());
        service.confirm(paymentRequestExternalId, gatewayAccount, confirmDetailsRequest);
        LOGGER.info("Confirmed payment for payment request with id: {}", paymentRequestExternalId);

        return noContent().build();
    }
}
