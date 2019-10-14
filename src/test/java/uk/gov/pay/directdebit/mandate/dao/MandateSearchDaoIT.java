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
import uk.gov.pay.directdebit.mandate.model.MandateState;
import uk.gov.pay.directdebit.payers.fixtures.PayerFixture;
import uk.gov.pay.directdebit.payments.fixtures.GatewayAccountFixture;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.LongStream;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static uk.gov.pay.directdebit.mandate.fixtures.MandateFixture.aMandateFixture;
import static uk.gov.pay.directdebit.mandate.params.MandateSearchParams.MandateSearchParamsBuilder.aMandateSearchParams;
import static uk.gov.pay.directdebit.payers.fixtures.PayerFixture.aPayerFixture;
import static uk.gov.pay.directdebit.payments.fixtures.GatewayAccountFixture.aGatewayAccountFixture;

@RunWith(DropwizardJUnitRunner.class)
@DropwizardConfig(app = DirectDebitConnectorApp.class, config = "config/test-it-config.yaml")
public class MandateSearchDaoIT {

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
            .withState(MandateState.SUBMITTED_TO_BANK)
            .withCreatedDate(now());
    private MandateFixture mandate2 = aMandateFixture()
            .withGatewayAccountFixture(gatewayAccountFixture)
            .withMandateBankStatementReference(MandateBankStatementReference.valueOf("STATEMENT123"))
            .withState(MandateState.SUBMITTED_TO_PROVIDER)
            .withCreatedDate(now().minusHours(6));
    
    private static ZonedDateTime now() {
        return ZonedDateTime.now(ZoneOffset.UTC);
    }
    
    @Before
    public void setUp() {
        mandateSearchDao = new MandateSearchDao(testContext.getJdbi());

        gatewayAccountFixture.insert(testContext.getJdbi());
        mandate1.insert(testContext.getJdbi());
        mandate2.insert(testContext.getJdbi());
    }
    
    @Test
    @Parameters({"REF1234", "ref1234", "f12"})
    public void searchByReference(String searchString) {
        var searchParams = aMandateSearchParams().withServiceReference(searchString).build();
        var total = mandateSearchDao.countTotalMatchingMandates(searchParams, gatewayAccountFixture.getExternalId());
        assertThat(total).isEqualTo(1);
        assertThat(mandateSearchDao.search(searchParams, gatewayAccountFixture.getExternalId())).containsExactly(mandate1.toEntity());
    }
    
    @Test
    @Parameters({"STATEMENT123", "statement123", "ment"})
    public void searchByBankStatementReference(String searchString) {
        var searchParams = aMandateSearchParams()
                .withMandateBankStatementReference(MandateBankStatementReference.valueOf(searchString)).build();
        var total = mandateSearchDao.countTotalMatchingMandates(searchParams, gatewayAccountFixture.getExternalId());
        assertThat(total).isEqualTo(1);
        assertThat(mandateSearchDao.search(searchParams, gatewayAccountFixture.getExternalId())).containsExactly(mandate2.toEntity());
    }
    
    @Test
    @Parameters({"joe.bloggs@example.com", "joe.bloggs@EXAMPLE.com", "joe.bloggs"})
    public void searchByEmail(String searchString) {
        var searchParams = aMandateSearchParams().withEmail(searchString).build();
        var total = mandateSearchDao.countTotalMatchingMandates(searchParams, gatewayAccountFixture.getExternalId());
        assertThat(total).isEqualTo(1);
        assertThat(mandateSearchDao.search(searchParams, gatewayAccountFixture.getExternalId())).containsExactly(mandate1.toEntity());
    }

    @Test
    @Parameters({"JOe Bloggs", "joe bloggs", "bloggs"})
    public void searchByName(String searchString) {
        var searchParams = aMandateSearchParams().withName(searchString).build();
        var total = mandateSearchDao.countTotalMatchingMandates(searchParams, gatewayAccountFixture.getExternalId());
        assertThat(total).isEqualTo(1);
        assertThat(mandateSearchDao.search(searchParams, gatewayAccountFixture.getExternalId())).containsExactly(mandate1.toEntity());
    }
    
    @Test
    public void searchByFromDate() {
        var searchParams = aMandateSearchParams()
                .withFromDate(String.valueOf(now().minusHours(1)))
                .build();
        assertThat(mandateSearchDao.search(searchParams, gatewayAccountFixture.getExternalId())).containsExactly(mandate1.toEntity());
        var total = mandateSearchDao.countTotalMatchingMandates(searchParams, gatewayAccountFixture.getExternalId());
        assertThat(total).isEqualTo(1);

        searchParams = aMandateSearchParams()
                .withFromDate(String.valueOf(now().minusHours(7))).build();
        assertThat(mandateSearchDao.search(searchParams, gatewayAccountFixture.getExternalId())).containsExactlyInAnyOrder(mandate1.toEntity(), mandate2.toEntity());
        total = mandateSearchDao.countTotalMatchingMandates(searchParams, gatewayAccountFixture.getExternalId());
        assertThat(total).isEqualTo(2);
    }
    
    @Test
    public void searchByToDate() {
        var searchParams = aMandateSearchParams()
                .withToDate(String.valueOf(now().minusHours(1))).build();
        assertThat(mandateSearchDao.search(searchParams, gatewayAccountFixture.getExternalId())).containsExactly(mandate2.toEntity());
        var total = mandateSearchDao.countTotalMatchingMandates(searchParams, gatewayAccountFixture.getExternalId());
        assertThat(total).isEqualTo(1);

        searchParams = aMandateSearchParams()
                .withToDate(String.valueOf(now())).build();
        assertThat(mandateSearchDao.search(searchParams, gatewayAccountFixture.getExternalId())).containsExactlyInAnyOrder(mandate1.toEntity(), mandate2.toEntity());
        total = mandateSearchDao.countTotalMatchingMandates(searchParams, gatewayAccountFixture.getExternalId());
        assertThat(total).isEqualTo(2);
    }
    
    @Test
    public void searchByDateRange() {
        var searchParams = aMandateSearchParams()
                .withToDate(String.valueOf(now().minusHours(1)))
                .withFromDate(String.valueOf(now().minusHours(7)))
                .build();
        var total = mandateSearchDao.countTotalMatchingMandates(searchParams, gatewayAccountFixture.getExternalId());
        assertThat(total).isEqualTo(1);
        assertThat(mandateSearchDao.search(searchParams, gatewayAccountFixture.getExternalId())).containsExactly(mandate2.toEntity());
    }
    
    @Test
    public void searchByPage() {
        LongStream.rangeClosed(101, 105).forEach(n -> {
            aMandateFixture().withGatewayAccountFixture(gatewayAccountFixture).withId(n).insert(testContext.getJdbi());
        });
        var searchParams = aMandateSearchParams()
                .withFromDate(String.valueOf(now().minusHours(1)))
                .withPage(3)
                .withDisplaySize(2)
                .build();
        List<Mandate> results = mandateSearchDao.search(searchParams, gatewayAccountFixture.getExternalId());
        assertThat(results).hasSize(2);
        assertThat(results.get(0).getId()).isEqualTo(102L);
        assertThat(results.get(1).getId()).isEqualTo(101L);
        var total = mandateSearchDao.countTotalMatchingMandates(searchParams, gatewayAccountFixture.getExternalId());
        assertThat(total).isEqualTo(6);
    }
    
    @Test
    public void searchByDisplaySize() {
        var searchParams = aMandateSearchParams()
                .withToDate(String.valueOf(now()))
                .withDisplaySize(1)
                .build();
        assertThat(mandateSearchDao.search(searchParams, gatewayAccountFixture.getExternalId())).hasSize(1);
        var total = mandateSearchDao.countTotalMatchingMandates(searchParams, gatewayAccountFixture.getExternalId());
        assertThat(total).isEqualTo(2);
    }
    
    @Test
    public void searchByMultipleParams() {
        var searchParams = aMandateSearchParams()
                .withServiceReference("REF1234")
                .withEmail("joe.bloggs@example.com")
                .withName("bloggs")
                .withFromDate(String.valueOf(now().minusHours(1)))
                .build();
        assertThat(mandateSearchDao.search(searchParams, gatewayAccountFixture.getExternalId())).containsExactly(mandate1.toEntity());
        var total = mandateSearchDao.countTotalMatchingMandates(searchParams, gatewayAccountFixture.getExternalId());
        assertThat(total).isEqualTo(1);
    }

    @Test
    public void searchByExternalState() {
        aMandateFixture()
                .withGatewayAccountFixture(gatewayAccountFixture)
                .withId(999L)
                .withState(MandateState.USER_SETUP_EXPIRED)
                .insert(testContext.getJdbi());

        var searchParams = aMandateSearchParams()
                .withExternalMandateState("pending")
                .build();

        var results = mandateSearchDao.search(searchParams, gatewayAccountFixture.getExternalId());
        assertThat(results).hasSize(2);
        assertThat(results).containsExactlyInAnyOrder(mandate1.toEntity(), mandate2.toEntity());

        var total = mandateSearchDao.countTotalMatchingMandates(searchParams, gatewayAccountFixture.getExternalId());
        assertThat(total).isEqualTo(2);
    }
}
