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
        PaymentViewSearchParams searchParams = new PaymentViewSearchParams(gatewayAccountExternalId)
                .withPage(1L)
                .withDisplaySize(500L);
        LinksForSearchResult builder = new LinksForSearchResult(searchParams, mockedUriInfo, 120L);
        assertThat(builder.getPrevLink(), is(nullValue()));
        assertThat(builder.getFirstLink().getHref().contains("?page=1&display_size=500"), is(true));
        assertThat(builder.getLastLink().getHref().contains("?page=1&display_size=500"), is(true));
        assertThat(builder.getSelfLink().getHref().contains("?page=1&display_size=500"), is(true));
        assertThat(builder.getNextLink(), is(nullValue()));
    }

    @Test
    public void shouldShowPrevLinkAndFirstLinkAndLastLinkAsPage1_whenCurrentPageIsBiggerThanLastPage() {
        PaymentViewSearchParams searchParams = new PaymentViewSearchParams(gatewayAccountExternalId)
                .withPage(777L)
                .withDisplaySize(500L);
        LinksForSearchResult builder = new LinksForSearchResult(searchParams, mockedUriInfo, 120L);
        assertThat(builder.getPrevLink().getHref().contains("?page=1&display_size=500"), is(true));
        assertThat(builder.getFirstLink().getHref().contains("?page=1&display_size=500"), is(true));
        assertThat(builder.getLastLink().getHref().contains("?page=1&display_size=500"), is(true));
        assertThat(builder.getSelfLink().getHref().contains("?page=777&display_size=500"), is(true));
    }

    @Test
    public void shouldShowPrevLinkAndFirstLinkAsEqualsAndLastLinkAndNextLinkAsEquals() {
        PaymentViewSearchParams searchParams = new PaymentViewSearchParams(gatewayAccountExternalId)
                .withPage(2L)
                .withDisplaySize(50L);
        LinksForSearchResult builder = new LinksForSearchResult(searchParams, mockedUriInfo, 120L);
        assertThat(builder.getPrevLink().getHref().contains("?page=1&display_size=50"), is(true));
        assertThat(builder.getFirstLink().getHref().contains("?page=1&display_size=50"), is(true));
        assertThat(builder.getLastLink().getHref().contains("?page=3&display_size=50"), is(true));
        assertThat(builder.getNextLink().getHref().contains("?page=3&display_size=50"), is(true));
        assertThat(builder.getSelfLink().getHref().contains("?page=2&display_size=50"), is(true));
    }

    @Test
    public void shouldShowAllLinksCorrectly_whenMultiplePagesExists() {
        PaymentViewSearchParams searchParams = new PaymentViewSearchParams(gatewayAccountExternalId)
                .withPage(3L)
                .withDisplaySize(10L);
        LinksForSearchResult builder = new LinksForSearchResult(searchParams, mockedUriInfo, 120L);
        assertThat(builder.getFirstLink().getHref().contains("?page=1&display_size=10"), is(true));
        assertThat(builder.getLastLink().getHref().contains("?page=12&display_size=10"), is(true));
        assertThat(builder.getPrevLink().getHref().contains("?page=2&display_size=10"), is(true));
        assertThat(builder.getNextLink().getHref().contains("?page=4&display_size=10"), is(true));
        assertThat(builder.getSelfLink().getHref().contains("?page=3&display_size=10"), is(true));
    }
}
