package uk.gov.pay.directdebit.mandate.services;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.pay.directdebit.mandate.dao.MandateSearchDao;

import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static uk.gov.pay.directdebit.mandate.fixtures.MandateFixture.aMandateFixture;
import static uk.gov.pay.directdebit.mandate.params.MandateSearchParams.MandateSearchParamsBuilder.aMandateSearchParams;


@RunWith(MockitoJUnitRunner.class)
public class MandateSearchServiceTest {

    @Mock
    private MandateSearchDao mockMandateSearchDao;

    private MandateSearchService mandateSearchService;

    @Before
    public void setUp() {
        mandateSearchService = new MandateSearchService(mockMandateSearchDao);
    }

    @Test
    public void shouldReturnOneMandateForPageAndTotalOfTwoMatchingMandates() {
        var params = aMandateSearchParams()
                .withName("aName")
                .build();

        var expectedMandatesForRequestedPage = List.of(
                aMandateFixture().withServiceReference("expectedReference").toEntity()
        );

        String gatewayAccountExternalId = "expectedGatewayId";

        given(mockMandateSearchDao.countTotalMatchingMandates(params, gatewayAccountExternalId)).willReturn(2);
        given(mockMandateSearchDao.search(params, gatewayAccountExternalId)).willReturn(expectedMandatesForRequestedPage);

        var searchResults = mandateSearchService.search(params, gatewayAccountExternalId);

        assertThat(searchResults.getTotalMatchingMandates(), is(2));
        assertThat(searchResults.getMandatesForRequestedPage().size(), is(1));
        assertThat(searchResults.getMandatesForRequestedPage().get(0).getServiceReference(), is("expectedReference"));
    }

    @Test
    public void shouldReturnNoMandatesWhenThereAreNone() {
        var params = aMandateSearchParams()
                .withName("aName")
                .build();

        String gatewayAccountExternalId = "expectedGatewayId";

        given(mockMandateSearchDao.countTotalMatchingMandates(params, gatewayAccountExternalId)).willReturn(0);

        var searchResults = mandateSearchService.search(params, gatewayAccountExternalId);

        assertThat(searchResults.getTotalMatchingMandates(), is(0));
        assertThat(searchResults.getMandatesForRequestedPage().size(), is(0));
    }

}
