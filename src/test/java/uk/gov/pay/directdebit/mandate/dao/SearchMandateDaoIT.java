package uk.gov.pay.directdebit.mandate.dao;

import junitparams.Parameters;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import uk.gov.pay.directdebit.DirectDebitConnectorApp;
import uk.gov.pay.directdebit.junit.DropwizardConfig;
import uk.gov.pay.directdebit.junit.DropwizardJUnitRunner;
import uk.gov.pay.directdebit.junit.DropwizardTestContext;
import uk.gov.pay.directdebit.junit.TestContext;
import uk.gov.pay.directdebit.mandate.fixtures.MandateFixture;
import uk.gov.pay.directdebit.mandate.model.Mandate;
import uk.gov.pay.directdebit.mandate.model.MandateBankStatementReference;
import uk.gov.pay.directdebit.payers.fixtures.PayerFixture;
import uk.gov.pay.directdebit.payments.fixtures.GatewayAccountFixture;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.LongStream;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static uk.gov.pay.directdebit.mandate.fixtures.MandateFixture.aMandateFixture;
import static uk.gov.pay.directdebit.mandate.model.MandateState.FAILED;
import static uk.gov.pay.directdebit.mandate.params.MandateSearchParams.aMandateSearchParams;
import static uk.gov.pay.directdebit.payers.fixtures.PayerFixture.aPayerFixture;
import static uk.gov.pay.directdebit.payments.fixtures.GatewayAccountFixture.aGatewayAccountFixture;

@RunWith(DropwizardJUnitRunner.class)
@DropwizardConfig(app = DirectDebitConnectorApp.class, config = "config/test-it-config.yaml")
public class SearchMandateDaoIT {

    @DropwizardTestContext
    private TestContext testContext;
    
    private static final String GATEWAY_ACCOUNT_ID = "gateway-account-id";

    private MandateSearchDao mandateSearchDao;
    private GatewayAccountFixture gatewayAccountFixture = aGatewayAccountFixture().withExternalId(GATEWAY_ACCOUNT_ID);
    private PayerFixture joeBloggs = aPayerFixture().withEmail("joe.bloggs@example.com").withName("Joe Bloggs");
    private MandateFixture mandate1 = aMandateFixture()
            .withGatewayAccountFixture(gatewayAccountFixture)
            .withServiceReference("REF1234")
            .withPayerFixture(joeBloggs)
            .withCreatedDate(now());
    private MandateFixture mandate2 = aMandateFixture()
            .withGatewayAccountFixture(gatewayAccountFixture)
            .withMandateBankStatementReference(MandateBankStatementReference.valueOf("STATEMENT123"))
            .withState(FAILED)
            .withCreatedDate(now().minusHours(6));
    
    private static ZonedDateTime now() {
        return java.time.ZonedDateTime.now(ZoneOffset.UTC);
    }
    
    @Before
    public void setup() {
        mandateSearchDao = new MandateSearchDao(testContext.getJdbi());

        gatewayAccountFixture.insert(testContext.getJdbi());
        mandate1.insert(testContext.getJdbi());
        mandate2.insert(testContext.getJdbi());
    }
    
    @Test
    @Parameters({"REF1234", "ref1234", "f12"})
    public void searchByReference(String searchString) {
        var searchParams = aMandateSearchParams().withReference(searchString);
        assertThat(mandateSearchDao.search(searchParams, GATEWAY_ACCOUNT_ID)).containsExactly(mandate1.toEntity());
    }
    
    @Test
    public void searchByState() {
        var searchParams = aMandateSearchParams().withMandateState(FAILED);
        assertThat(mandateSearchDao.search(searchParams, GATEWAY_ACCOUNT_ID)).containsExactly(mandate2.toEntity());
    }
    
    @Test
    @Parameters({"STATEMENT123", "statement123", "ment"})
    public void searchByBankStatementReference(String searchString) {
        var searchParams = aMandateSearchParams().withMandateBankStatementReference(MandateBankStatementReference.valueOf(searchString));
        assertThat(mandateSearchDao.search(searchParams, GATEWAY_ACCOUNT_ID)).containsExactly(mandate2.toEntity());
    }
    
    @Test
    @Parameters({"joe.bloggs@example.com", "joe.bloggs@EXAMPLE.com", "joe.bloggs"})
    public void searchByEmail(String searchString) {
        var searchParams = aMandateSearchParams().withEmail(searchString);
        assertThat(mandateSearchDao.search(searchParams, GATEWAY_ACCOUNT_ID)).containsExactly(mandate1.toEntity());
    }

    @Test
    @Parameters({"JOe Bloggs", "joe bloggs", "bloggs"})
    public void searchByName(String searchString) {
        var searchParams = aMandateSearchParams().withName(searchString);
        assertThat(mandateSearchDao.search(searchParams, GATEWAY_ACCOUNT_ID)).containsExactly(mandate1.toEntity());
    }
    
    @Test
    public void searchByFromDate() {
        var searchParams = aMandateSearchParams().withFromDate(now().minusHours(1));
        assertThat(mandateSearchDao.search(searchParams, GATEWAY_ACCOUNT_ID)).containsExactly(mandate1.toEntity());

        searchParams = aMandateSearchParams().withFromDate(now().minusHours(7));
        assertThat(mandateSearchDao.search(searchParams, GATEWAY_ACCOUNT_ID))
                .containsExactlyInAnyOrder(mandate1.toEntity(), mandate2.toEntity());
    }
    
    @Test
    public void searchByToDate() {
        var searchParams = aMandateSearchParams().withToDate(now().minusHours(1));
        assertThat(mandateSearchDao.search(searchParams, GATEWAY_ACCOUNT_ID)).containsExactly(mandate2.toEntity());

        searchParams = aMandateSearchParams().withToDate(now());
        assertThat(mandateSearchDao.search(searchParams, GATEWAY_ACCOUNT_ID))
                .containsExactlyInAnyOrder(mandate1.toEntity(), mandate2.toEntity());
    }
    
    @Test
    public void searchByDateRange() {
        var searchParams = aMandateSearchParams().withToDate(now().minusHours(1)).withFromDate(now().minusHours(7));
        assertThat(mandateSearchDao.search(searchParams, GATEWAY_ACCOUNT_ID)).containsExactly(mandate2.toEntity());
    }
    
    @Test
    public void searchByPage() {
        LongStream.rangeClosed(101, 105).forEach(n -> {
            aMandateFixture().withGatewayAccountFixture(gatewayAccountFixture).withId(n).insert(testContext.getJdbi());
        });
        var searchParams = aMandateSearchParams()
                .withFromDate(now().minusHours(1))
                .withPage(3)
                .withDisplaySize(2);
        List<Mandate> results = mandateSearchDao.search(searchParams, GATEWAY_ACCOUNT_ID);
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getId()).isEqualTo(101L);
    }
    
    @Test
    public void searchByDisplaySize() {
        var searchParams = aMandateSearchParams()
                .withToDate(now())
                .withDisplaySize(1)
                ;
        assertThat(mandateSearchDao.search(searchParams, GATEWAY_ACCOUNT_ID)).hasSize(1);
    }
    
    @Test
    public void searchByMultipleParams() {
        var searchParams = aMandateSearchParams()
                .withReference("REF1234")
                .withEmail("joe.bloggs@example.com")
                .withName("bloggs")
                .withFromDate(now().minusHours(1))
                ;
        assertThat(mandateSearchDao.search(searchParams, GATEWAY_ACCOUNT_ID)).containsExactly(mandate1.toEntity());
    }
}
