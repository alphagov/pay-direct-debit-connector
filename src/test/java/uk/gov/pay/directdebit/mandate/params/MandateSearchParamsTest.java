package uk.gov.pay.directdebit.mandate.params;


import org.junit.Test;
import uk.gov.pay.directdebit.mandate.api.ExternalMandateState;
import uk.gov.pay.directdebit.mandate.model.MandateBankStatementReference;

import java.time.LocalDate;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.pay.directdebit.mandate.api.ExternalMandateState.EXTERNAL_CREATED;
import static uk.gov.pay.directdebit.mandate.params.MandateSearchParams.MandateSearchParamsBuilder.aMandateSearchParams;

public class MandateSearchParamsTest {

    @Test
    public void shouldCreateQueryWithAllParams() {
        int page = 1;
        int displaySize = 100;
        String email = "tester@example.com";
        String name = "tester";
        String serviceReference = "aServiceReference";
        String toDate = LocalDate.now().toString();
        String fromDate = LocalDate.now().minusDays(3).toString();
        String mandateState = EXTERNAL_CREATED.getState();
        MandateBankStatementReference mandateBankStatementReference = 
                MandateBankStatementReference.valueOf("bankReference");
        
        
        var mandateSearchParams = aMandateSearchParams()
                .withFromDate(fromDate)
                .withToDate(toDate)
                .withDisplaySize(displaySize)
                .withEmail(email)
                .withName(name)
                .withServiceReference(serviceReference)
                .withMandateBankStatementReference(mandateBankStatementReference)
                .withExternalMandateState(mandateState)
                .withPage(page)
                .build();

        String expectedQuery = "page=" + page +
                "&display_size=" + displaySize +
                "&reference=" + serviceReference +
                "&state=" + mandateState +
                "&bank_statement_reference=" + mandateBankStatementReference.toString() +
                "&name=" + name +
                "&email=" + email +
                "&from_date=" + fromDate +
                "&to_date=" + toDate;

        assertThat(mandateSearchParams.buildQueryParamString(), is(expectedQuery));
    }

    @Test
    public void shouldCreateQueryWithNoParams() {
        var mandateSearchParams = aMandateSearchParams().build();

        String expectedQuery = "page=1&display_size=500";

        assertThat(mandateSearchParams.buildQueryParamString(), is(expectedQuery));
    }

}
