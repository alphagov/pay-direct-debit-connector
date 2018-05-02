package uk.gov.pay.directdebit.payments.resources;

import com.google.common.collect.ImmutableList;
import org.apache.commons.lang3.tuple.Pair;
import uk.gov.pay.directdebit.gatewayaccounts.dao.GatewayAccountDao;
import uk.gov.pay.directdebit.payments.api.PaymentViewListResponse;
import uk.gov.pay.directdebit.payments.api.PaymentViewResponse;
import uk.gov.pay.directdebit.payments.api.PaymentViewValidator;
import uk.gov.pay.directdebit.payments.dao.PaymentViewSearchParams;
import uk.gov.pay.directdebit.payments.services.PaymentViewService;
import uk.gov.pay.directdebit.utils.ResponseUtil;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import java.util.List;

import static java.lang.String.format;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static uk.gov.pay.directdebit.utils.ResponseUtil.notFoundResponse;

@Path("/")
public class PaymentViewResource {

    private static final String PAGE = "page";
    private static final String DISPLAY_SIZE = "display_size";
    private final PaymentViewService paymentViewService;
    private final GatewayAccountDao gatewayAccountDao;
    private final PaymentViewValidator paymentViewValidator = new PaymentViewValidator();

    @Inject
    public PaymentViewResource(PaymentViewService paymentViewService, GatewayAccountDao gatewayAccountDao) {
        this.paymentViewService = paymentViewService;
        this.gatewayAccountDao = gatewayAccountDao;
    }

    @GET
    @Path("/v1/api/accounts/{accountId}/view")
    @Produces(APPLICATION_JSON)
    public Response getPaymentView(
            @PathParam("accountId") String accountExternalId,
            @QueryParam(PAGE) Long pageNumber,
            @QueryParam(DISPLAY_SIZE) Long displaySize) {
        List<Pair<String, Long>> nonNegativePairMap = ImmutableList.of(Pair.of(PAGE, pageNumber), Pair.of(DISPLAY_SIZE, displaySize));

        PaymentViewSearchParams searchParams = new PaymentViewSearchParams(
                pageNumber,
                displaySize
        );
        return paymentViewValidator.validateQueryParams(nonNegativePairMap)
                .map(ResponseUtil::badRequestResponse)
                .orElseGet(() -> gatewayAccountDao.findByExternalId(accountExternalId)
                        .map(gatewayAccount ->
                                doSearch(accountExternalId, searchParams))
                        .orElseGet(() -> notFoundResponse(format("account with id %s not found", accountExternalId)))
                );
    }

    private Response doSearch(String accountExternalId, PaymentViewSearchParams searchParams) {
        List<PaymentViewListResponse> viewList = paymentViewService.getPaymentViewListResponse(accountExternalId, searchParams);
        if (viewList.size() == 0) {
            return notFoundResponse(format("found no records with page size %s and display_size %s",
                    searchParams.getPage(),
                    searchParams.getPaginationParams().get().getRight()));
        }
        PaymentViewResponse wrapper = new PaymentViewResponse(
                accountExternalId,
                searchParams.getPage(),
                searchParams.getPaginationParams().get().getRight(),
                viewList
        );
        return Response.ok().entity(wrapper).build();
    }
}
