package uk.gov.pay.directdebit.payments.model;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.pay.directdebit.payments.params.PaymentViewSearchParams;

import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.net.URISyntaxException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.pay.directdebit.payments.params.PaymentViewSearchParams.PaymentViewSearchParamsBuilder.aPaymentViewSearchParams;

@RunWith(MockitoJUnitRunner.class)
public class LinksForSearchResultTest {

    @Mock
    private UriInfo mockedUriInfo;

    private final String gatewayAccountExternalId = "a-gateway-account-external-id";

    @Before
    public void setUp() throws URISyntaxException {
        URI uri = new URI("http://example.org");
        when(mockedUriInfo.getBaseUriBuilder()).thenReturn(UriBuilder.fromUri(uri), UriBuilder.fromUri(uri));
        when(mockedUriInfo.getPath()).thenReturn("/v1/api/accounts/" + gatewayAccountExternalId + "/transactions/view");
    }

    @Test
    public void shouldNotShowPrevLinkAndFirstLinkIsEqualToLastLink_whenOnlyOnePage() {
        PaymentViewSearchParams searchParams = aPaymentViewSearchParams()
                .withPage(1)
                .withDisplaySize(500)
                .build();
        LinksForSearchResult builder = new LinksForSearchResult(searchParams, mockedUriInfo, 120, gatewayAccountExternalId);
        assertThat(builder.getPrevLink(), is(nullValue()));
        assertThat(builder.getFirstLink().getHref().contains("?page=1&display_size=500"), is(true));
        assertThat(builder.getLastLink().getHref().contains("?page=1&display_size=500"), is(true));
        assertThat(builder.getSelfLink().getHref().contains("?page=1&display_size=500"), is(true));
        assertThat(builder.getNextLink(), is(nullValue()));
    }

    @Test
    public void shouldShowPrevLinkAndFirstLinkAndLastLinkAsPage1_whenCurrentPageIsBiggerThanLastPage() {
        PaymentViewSearchParams searchParams = aPaymentViewSearchParams()
                .withPage(777)
                .withDisplaySize(500)
                .build();
        LinksForSearchResult builder = new LinksForSearchResult(searchParams, mockedUriInfo, 120, gatewayAccountExternalId);
        assertThat(builder.getPrevLink().getHref().contains("?page=1&display_size=500"), is(true));
        assertThat(builder.getFirstLink().getHref().contains("?page=1&display_size=500"), is(true));
        assertThat(builder.getLastLink().getHref().contains("?page=1&display_size=500"), is(true));
        assertThat(builder.getSelfLink().getHref().contains("?page=777&display_size=500"), is(true));
    }

    @Test
    public void shouldShowPrevLinkAndFirstLinkAsEqualsAndLastLinkAndNextLinkAsEquals() {
        PaymentViewSearchParams searchParams = aPaymentViewSearchParams()
                .withPage(2)
                .withDisplaySize(50)
                .build();
        LinksForSearchResult builder = new LinksForSearchResult(searchParams, mockedUriInfo, 120, gatewayAccountExternalId);
        assertThat(builder.getPrevLink().getHref().contains("?page=1&display_size=50"), is(true));
        assertThat(builder.getFirstLink().getHref().contains("?page=1&display_size=50"), is(true));
        assertThat(builder.getLastLink().getHref().contains("?page=3&display_size=50"), is(true));
        assertThat(builder.getNextLink().getHref().contains("?page=3&display_size=50"), is(true));
        assertThat(builder.getSelfLink().getHref().contains("?page=2&display_size=50"), is(true));
    }

    @Test
    public void shouldShowAllLinksCorrectly_whenMultiplePagesExists() {
        PaymentViewSearchParams searchParams = aPaymentViewSearchParams()
                .withPage(3)
                .withDisplaySize(10)
                .build();
        LinksForSearchResult builder = new LinksForSearchResult(searchParams, mockedUriInfo, 120, gatewayAccountExternalId);
        assertThat(builder.getFirstLink().getHref().contains("?page=1&display_size=10"), is(true));
        assertThat(builder.getLastLink().getHref().contains("?page=12&display_size=10"), is(true));
        assertThat(builder.getPrevLink().getHref().contains("?page=2&display_size=10"), is(true));
        assertThat(builder.getNextLink().getHref().contains("?page=4&display_size=10"), is(true));
        assertThat(builder.getSelfLink().getHref().contains("?page=3&display_size=10"), is(true));
    }
}
