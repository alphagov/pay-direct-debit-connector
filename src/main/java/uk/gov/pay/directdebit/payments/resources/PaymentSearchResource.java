package uk.gov.pay.directdebit.payments.resources;

import com.codahale.metrics.annotation.Timed;
import uk.gov.pay.directdebit.common.model.SearchResponse;
import uk.gov.pay.directdebit.gatewayaccounts.model.GatewayAccount;
import uk.gov.pay.directdebit.payments.api.PaymentResponse;
import uk.gov.pay.directdebit.payments.params.PaymentViewSearchParams;
import uk.gov.pay.directdebit.payments.services.PaymentSearchService;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Path("/")
public class PaymentSearchResource {

    private final PaymentSearchService paymentSearchService;

    @Inject
    public PaymentSearchResource(PaymentSearchService paymentSearchService) {
        this.paymentSearchService = paymentSearchService;
    }

    @GET
    @Path("/v1/api/accounts/{accountId}/payments")
    @Produces(APPLICATION_JSON)
    @Timed
    public Response searchPayments(
            @PathParam("accountId") GatewayAccount gatewayAccount,
            @Valid @BeanParam PaymentViewSearchParams paymentViewSearchParams,
            @Context UriInfo uriInfo) {

        SearchResponse<PaymentResponse> searchResponse = paymentSearchService.withUriInfo(uriInfo)
                .getPaymentSearchResponse(paymentViewSearchParams, gatewayAccount.getExternalId());
        
        return Response.ok().entity(searchResponse).build();
    }
}
