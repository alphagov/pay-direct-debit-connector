package uk.gov.pay.directdebit.mandate.params;


import org.junit.Test;
import uk.gov.pay.directdebit.mandate.model.MandateBankStatementReference;
import uk.gov.pay.directdebit.mandate.model.MandateState;

import java.time.LocalDate;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.pay.directdebit.mandate.params.MandateSearchParams.MandateSearchParamsBuilder.aMandateSearchParams;

public class MandateSearchParamsTest {

    private final String gatewayAccountExternalId = "expectedGatewayAccountExternalId";
    private final String toDate = LocalDate.now().toString();
    private final String fromDate = LocalDate.now().minusDays(3).toString();
    private final String email = "tester@example.com";
    private final int displaySize = 100;
    private final String name = "tester";
    private final String serviceReference = "aServiceReference";
    private final MandateBankStatementReference mandateBankStatementReference = MandateBankStatementReference.valueOf("bankReference");
    private final int page = 1;
    private final MandateState mandateState = MandateState.PENDING;


    @Test
    public void shouldCreateQueryWithAllParams() {
        var mandateSearchParams = aMandateSearchParams(gatewayAccountExternalId)
                .withFromDate(fromDate)
                .withToDate(toDate)
                .withDisplaySize(displaySize)
                .withEmail(email)
                .withName(name)
                .withServiceReference(serviceReference)
                .withMandateBankStatementReference(mandateBankStatementReference)
                .withMandateState(mandateState)
                .withPage(page)
                .build();

        String expectedQuery = "page=1" +
                "&display_size=100" +
                "&reference=aServiceReference" +
                "&state=PENDING" +
                "&bank_statement_reference=bankReference" +
                "&name=tester" +
                "&email=tester@example.com" +
                "&from_date=2019-07-08" +
                "&to_date=2019-07-11";

        assertThat(mandateSearchParams.buildQueryParamString(), is(expectedQuery));
    }

    @Test
    public void shouldCreateQueryWithNoParams() {
        var mandateSearchParams = aMandateSearchParams(gatewayAccountExternalId).build();

        String expectedQuery = "page=1&display_size=500";

        assertThat(mandateSearchParams.buildQueryParamString(), is(expectedQuery));
    }

}
