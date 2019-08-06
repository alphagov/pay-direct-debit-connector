package uk.gov.pay.directdebit.payments.services;

import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.pay.directdebit.common.model.SearchResponse;
import uk.gov.pay.directdebit.common.util.RandomIdGenerator;
import uk.gov.pay.directdebit.payments.api.ExternalPaymentState;
import uk.gov.pay.directdebit.payments.api.ExternalPaymentStateWithDetails;
import uk.gov.pay.directdebit.payments.api.PaymentResponse;
import uk.gov.pay.directdebit.payments.dao.PaymentViewDao;
import uk.gov.pay.directdebit.payments.params.PaymentViewSearchParams;

import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.pay.directdebit.payments.api.PaymentResponse.PaymentResponseBuilder.aPaymentResponse;
import static uk.gov.pay.directdebit.payments.params.PaymentViewSearchParams.PaymentViewSearchParamsBuilder.aPaymentViewSearchParams;

@RunWith(MockitoJUnitRunner.class)
public class PaymentSearchServiceTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Mock
    private PaymentViewDao mockPaymentViewDao;

    @Mock
    private UriInfo mockUriInfo;

    private String gatewayAccountExternalId = "an-external-id";
    private ZonedDateTime createdDate = ZonedDateTime.now(ZoneOffset.UTC);

    private PaymentSearchService paymentSearchService;

    @Before
    public void setUp() {
        when(mockUriInfo.getBaseUriBuilder()).thenReturn(UriBuilder.fromUri("http://app.com"),
                UriBuilder.fromUri("http://app.com"));
        when(mockUriInfo.getPath()).thenReturn("/v1/api/accounts/" + gatewayAccountExternalId + "/transactions/view");

        paymentSearchService = new PaymentSearchService(mockPaymentViewDao).withUriInfo(mockUriInfo);
    }

    @Test
    public void getPaymentViewList_withGatewayAccountIdAndOffsetAndLimit_shouldPopulateResponse() {
        PaymentViewSearchParams searchParams = aPaymentViewSearchParams()
                .withPage(1)
                .withDisplaySize(100)
                .withFromDateString(createdDate.toString())
                .withToDateString(createdDate.toString())
                .build();

        List<PaymentResponse> paymentResponses = createPaymentResponses(4);

        when(mockPaymentViewDao.searchPaymentView(searchParams, gatewayAccountExternalId)).thenReturn(paymentResponses);
        when(mockPaymentViewDao.getPaymentViewCount(searchParams, gatewayAccountExternalId)).thenReturn(4);
        
        SearchResponse<PaymentResponse> response = paymentSearchService.getPaymentSearchResponse(searchParams,
                gatewayAccountExternalId);
        
        assertThat(response.getResults().get(3).getAmount(), is(1003L));
        assertThat(response.getResults().get(0).getState(),
                is(new ExternalPaymentStateWithDetails(ExternalPaymentState.EXTERNAL_PENDING, "example_details")));
        assertThat(response.getResults().get(2).getCreatedDate(), is(createdDate));
    }

    @Test
    public void shouldReturn20Records_whenPaginationSetToPage2AndDisplaySize20() {
        List<PaymentResponse> paymentResponses = createPaymentResponses(20);

        PaymentViewSearchParams searchParams = aPaymentViewSearchParams()
                .withPage(2)
                .withDisplaySize(20)
                .build();

        when(mockPaymentViewDao.getPaymentViewCount(searchParams, gatewayAccountExternalId)).thenReturn(50);
        when(mockPaymentViewDao.searchPaymentView(searchParams, gatewayAccountExternalId)).thenReturn(paymentResponses);

        SearchResponse paymentViewResponse = paymentSearchService.getPaymentSearchResponse(searchParams,
                gatewayAccountExternalId);
        
        assertThat(paymentViewResponse.getCount(), is(20));
        assertThat(paymentViewResponse.getPage(), is(2));
        assertThat(paymentViewResponse.getTotal(), is(50));
        assertThat(paymentViewResponse.getLinksForSearchResult().getFirstLink().getHref().contains("?page=1"), is(true));
        assertThat(paymentViewResponse.getLinksForSearchResult().getLastLink().getHref().contains("?page=3"), is(true));
        assertThat(paymentViewResponse.getLinksForSearchResult().getPrevLink().getHref().contains("?page=1"), is(true));
        assertThat(paymentViewResponse.getLinksForSearchResult().getNextLink().getHref().contains("?page=3"), is(true));
        assertThat(paymentViewResponse.getLinksForSearchResult().getSelfLink().getHref().contains("?page=2"), is(true));
    }

    @Test
    public void shouldReturnNoRecords_whenPaginationSetToPage2AndNoDisplaySize() {
        List<PaymentResponse> paymentViewList = new ArrayList<>();

        PaymentViewSearchParams searchParams = aPaymentViewSearchParams()
                .withPage(2)
                .build();

        when(mockPaymentViewDao.getPaymentViewCount(searchParams, gatewayAccountExternalId)).thenReturn(18);
        when(mockPaymentViewDao.searchPaymentView(searchParams, gatewayAccountExternalId)).thenReturn(paymentViewList);

        SearchResponse<PaymentResponse> paymentViewResponse = paymentSearchService.getPaymentSearchResponse(searchParams,
                gatewayAccountExternalId);

        assertThat(paymentViewResponse.getCount(), is(0));
        assertThat(paymentViewResponse.getPage(), is(2));
        assertThat(paymentViewResponse.getTotal(), is(18));
        assertThat(paymentViewResponse.getResults().size(), is(0));
        assertThat(paymentViewResponse.getLinksForSearchResult().getFirstLink().getHref().contains("?page=1&display_size=500"), is(true));
        assertThat(paymentViewResponse.getLinksForSearchResult().getLastLink().getHref().contains("?page=1&display_size=500"), is(true));
        assertThat(paymentViewResponse.getLinksForSearchResult().getPrevLink().getHref().contains("?page=1&display_size=500"), is(true));
        assertThat(paymentViewResponse.getLinksForSearchResult().getNextLink(), is(nullValue()));
        assertThat(paymentViewResponse.getLinksForSearchResult().getSelfLink().getHref().contains("?page=2&display_size=500"), is(true));
    }


    @NotNull
    private List<PaymentResponse> createPaymentResponses(int numberOfPayments) {
        List<PaymentResponse> paymentViewList = new ArrayList<>();
        for (int i = 0; i < numberOfPayments; i++) {
            PaymentResponse paymentResponse = aPaymentResponse()
                    .withCreatedDate(createdDate)
                    .withState(new ExternalPaymentStateWithDetails(ExternalPaymentState.EXTERNAL_PENDING, "example_details"))
                    .withReference("Pay reference " + i)
                    .withAmount(1000L + i)
                    .withDescription("This is a description " + i)
                    .withPaymentExternalId(RandomIdGenerator.newId())
                    .build();
            paymentViewList.add(paymentResponse);
        }
        return paymentViewList;
    }
}
