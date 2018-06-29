package uk.gov.pay.directdebit.payments.resources;

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

    private static final String PAGE_KEY = "page";
    private static final String DISPLAY_SIZE_KEY = "display_size";
    private static final String FROM_DATE_KEY = "from_date";
    private static final String TO_DATE_KEY = "to_date";
    private static final String EMAIL_KEY = "email";
    private static final String REFERENCE_KEY = "reference";
    private static final String AMOUNT_KEY = "amount";
    private static final String MANDATE_ID_EXTERNAL_KEY = "agreement_id";
    private static final String MANDATE_ID_BWC_EXTERNAL_KEY = "agreement";
    private final PaymentViewService paymentViewService;

    @Inject
    public PaymentViewResource(PaymentViewService paymentViewService) {
        this.paymentViewService = paymentViewService;
    }

    @GET
    @Path("/v1/api/accounts/{accountId}/transactions/view")
    @Produces(APPLICATION_JSON)
    public Response getPaymentView(
            @PathParam("accountId") String accountExternalId,
            @QueryParam(PAGE_KEY) Long pageNumber,
            @QueryParam(DISPLAY_SIZE_KEY) Long displaySize,
            @QueryParam(FROM_DATE_KEY) String fromDate,
            @QueryParam(TO_DATE_KEY) String toDate,
            @QueryParam(EMAIL_KEY) String email,
            @QueryParam(REFERENCE_KEY) String reference,
            @QueryParam(AMOUNT_KEY) Long amount,
            @QueryParam(MANDATE_ID_EXTERNAL_KEY) String mandateId,
            @QueryParam(MANDATE_ID_BWC_EXTERNAL_KEY) String mandateBWCId,
            @Context UriInfo uriInfo){
        
        String mandateIdToBeUsed = mandateBWCId == null ? mandateId : mandateBWCId;
        PaymentViewSearchParams searchParams = new PaymentViewSearchParams(accountExternalId)
                .withPage(pageNumber)
                .withDisplaySize(displaySize)
                .withFromDateString(fromDate)
                .withToDateString(toDate)
                .withEmail(email)
                .withReference(reference)
                .withAmount(amount)
                .withMandateId(mandateIdToBeUsed);
        
        return Response.ok().entity(paymentViewService
                .withUriInfo(uriInfo)
                .getPaymentViewResponse(searchParams)).build();
    }
}
