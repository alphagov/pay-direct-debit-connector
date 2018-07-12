package uk.gov.pay.directdebit.events.api;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import uk.gov.pay.directdebit.payments.links.PaginationLink;
import uk.gov.pay.directdebit.payments.params.DirectDebitEventSearchParams;

import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DirectDebitEventsPaginationTest {
    
    @Mock
    private UriInfo mockUriInfo;
    private final String testUrl = "http://test.test";

    @Before
    public void setup() {
        when(mockUriInfo.getBaseUriBuilder()).thenAnswer((Answer<UriBuilder>) invocation -> UriBuilder.fromUri(testUrl));
        when(mockUriInfo.getPath()).thenReturn("/test/");
    }

    @Test
    public void testWithAllSearchParameters() {
        DirectDebitEventSearchParams searchParams = DirectDebitEventSearchParams.builder()
                .page(2)
                .afterDate(ZonedDateTime.of(2018, 6, 29, 8, 0, 0, 0, ZoneId.of("UTC")))
                .beforeDate(ZonedDateTime.of(2018, 6, 29, 9, 0, 0, 0, ZoneId.of("UTC")))
                .mandateExternalId("1234L")
                .transactionExternalId("5678L")
                .pageSize(10)
                .build();
        DirectDebitEventsPagination pagination = new DirectDebitEventsPagination(searchParams, 100, mockUriInfo);
        
        assertEquals(PaginationLink.ofValue(testUrl + "/test/?before=2018-06-29T09%3A00%3A00Z&after=2018-06-29T08%3A00%3A00Z&mandate_external_id=1234L&transaction_external_id=5678L&page=2&page_size=10")
                , pagination.getSelfLink());
        assertEquals(PaginationLink.ofValue(testUrl + "/test/?before=2018-06-29T09%3A00%3A00Z&after=2018-06-29T08%3A00%3A00Z&mandate_external_id=1234L&transaction_external_id=5678L&page=3&page_size=10")
                , pagination.getNextLink());
        assertEquals(PaginationLink.ofValue(testUrl + "/test/?before=2018-06-29T09%3A00%3A00Z&after=2018-06-29T08%3A00%3A00Z&mandate_external_id=1234L&transaction_external_id=5678L&page=1&page_size=10")
                , pagination.getFirstLink());
        assertEquals(PaginationLink.ofValue(testUrl + "/test/?before=2018-06-29T09%3A00%3A00Z&after=2018-06-29T08%3A00%3A00Z&mandate_external_id=1234L&transaction_external_id=5678L&page=10&page_size=10")
                , pagination.getLastLink());
        assertEquals(PaginationLink.ofValue(testUrl + "/test/?before=2018-06-29T09%3A00%3A00Z&after=2018-06-29T08%3A00%3A00Z&mandate_external_id=1234L&transaction_external_id=5678L&page=1&page_size=10")
                , pagination.getPrevLink());
    }

    @Test
    public void testWithSomeSearchParameters() {
        DirectDebitEventSearchParams searchParams = DirectDebitEventSearchParams.builder()
                .page(3)
                .mandateExternalId("1234L")
                .transactionExternalId("5678L")
                .pageSize(10)
                .build();
        DirectDebitEventsPagination pagination = new DirectDebitEventsPagination(searchParams, 100, mockUriInfo);

        assertEquals(pagination.getSelfLink(), PaginationLink.ofValue(testUrl + "/test/?mandate_external_id=1234L&transaction_external_id=5678L&page=3&page_size=10"));
        assertEquals(PaginationLink.ofValue(testUrl + "/test/?mandate_external_id=1234L&transaction_external_id=5678L&page=4&page_size=10")
                , pagination.getNextLink());
        assertEquals(PaginationLink.ofValue(testUrl + "/test/?mandate_external_id=1234L&transaction_external_id=5678L&page=1&page_size=10")
                , pagination.getFirstLink());
        assertEquals(PaginationLink.ofValue(testUrl + "/test/?mandate_external_id=1234L&transaction_external_id=5678L&page=10&page_size=10")
                , pagination.getLastLink());
        assertEquals(PaginationLink.ofValue(testUrl + "/test/?mandate_external_id=1234L&transaction_external_id=5678L&page=2&page_size=10")
                , pagination.getPrevLink());
    }

    @Test
    public void testWithNoSearchParameters() {
        DirectDebitEventSearchParams searchParams = DirectDebitEventSearchParams.builder().build();
        DirectDebitEventsPagination pagination = new DirectDebitEventsPagination(searchParams, 5000, mockUriInfo);

        assertEquals(PaginationLink.ofValue(testUrl + "/test/?page=1&page_size=500"), pagination.getSelfLink());
        assertEquals(PaginationLink.ofValue(testUrl + "/test/?page=2&page_size=500"), pagination.getNextLink());
        assertEquals(PaginationLink.ofValue(testUrl + "/test/?page=1&page_size=500"), pagination.getFirstLink());
        assertEquals(PaginationLink.ofValue(testUrl + "/test/?page=10&page_size=500"), pagination.getLastLink());
        assertNull(pagination.getPrevLink());
    }
}
