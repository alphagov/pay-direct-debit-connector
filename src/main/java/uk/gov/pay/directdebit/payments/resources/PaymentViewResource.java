package uk.gov.pay.directdebit.payments.resources;

import com.codahale.metrics.annotation.Timed;
import uk.gov.pay.directdebit.payments.params.PaymentViewSearchParams;
import uk.gov.pay.directdebit.payments.services.PaymentViewService;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Path("/")
public class PaymentViewResource {

    private final PaymentViewService paymentViewService;

    @Inject
    public PaymentViewResource(PaymentViewService paymentViewService) {
        this.paymentViewService = paymentViewService;
    }

    @GET
    @Path("/v1/api/accounts/{accountId}/payments")
    @Produces(APPLICATION_JSON)
    @Timed
    public Response getPaymentView(
            @PathParam("accountId") String accountExternalId,
            @QueryParam("page") Long pageNumber,
            @QueryParam("display_size") Long displaySize,
            @QueryParam("from_date") String fromDate,
            @QueryParam("to_date") String toDate,
            @QueryParam("reference") String reference,
            @QueryParam("amount") Long amount,
            @QueryParam("mandate_id") String mandateId,
            @QueryParam("state") String state,
            @Context UriInfo uriInfo) {

        PaymentViewSearchParams searchParams = new PaymentViewSearchParams(accountExternalId)
                .withPage(pageNumber)
                .withDisplaySize(displaySize)
                .withFromDateString(fromDate)
                .withToDateString(toDate)
                .withReference(reference)
                .withAmount(amount)
                .withMandateId(mandateId)
                .withState(state);
        return Response.ok().entity(paymentViewService.withUriInfo(uriInfo).getPaymentViewResponse(searchParams)).build();
    }
}
