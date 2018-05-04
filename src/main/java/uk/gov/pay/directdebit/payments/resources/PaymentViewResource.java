package uk.gov.pay.directdebit.payments.resources;

import uk.gov.pay.directdebit.payments.dao.PaymentViewSearchParams;
import uk.gov.pay.directdebit.payments.services.PaymentViewService;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Path("/")
public class PaymentViewResource {

    private static final String PAGE = "page";
    private static final String DISPLAY_SIZE = "display_size";
    private final PaymentViewService paymentViewService;

    @Inject
    public PaymentViewResource(PaymentViewService paymentViewService) {
        this.paymentViewService = paymentViewService;
    }

    @GET
    @Path("/v1/api/accounts/{accountId}/view")
    @Produces(APPLICATION_JSON)
    public Response getPaymentView(
            @PathParam("accountId") String accountExternalId,
            @QueryParam(PAGE) Long pageNumber,
            @QueryParam(DISPLAY_SIZE) Long displaySize) {
        PaymentViewSearchParams searchParams = new PaymentViewSearchParams(
                accountExternalId,
                pageNumber,
                displaySize
        );
        return Response.ok().entity(paymentViewService.getPaymentViewResponse(searchParams)).build();
    }
}
