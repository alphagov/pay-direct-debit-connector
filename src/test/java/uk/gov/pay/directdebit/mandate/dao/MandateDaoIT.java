package uk.gov.pay.directdebit.mandate.dao;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import uk.gov.pay.directdebit.DirectDebitConnectorApp;
import uk.gov.pay.directdebit.common.util.RandomIdGenerator;
import uk.gov.pay.directdebit.junit.DropwizardConfig;
import uk.gov.pay.directdebit.junit.DropwizardJUnitRunner;
import uk.gov.pay.directdebit.junit.DropwizardTestContext;
import uk.gov.pay.directdebit.junit.TestContext;
import uk.gov.pay.directdebit.mandate.fixtures.MandateFixture;
import uk.gov.pay.directdebit.mandate.model.Mandate;
import uk.gov.pay.directdebit.mandate.model.MandateState;
import uk.gov.pay.directdebit.mandate.model.MandateStatesGraph;
import uk.gov.pay.directdebit.mandate.model.MandateType;
import uk.gov.pay.directdebit.mandate.model.ServiceMandateReference;
import uk.gov.pay.directdebit.mandate.model.subtype.MandateExternalId;
import uk.gov.pay.directdebit.payments.fixtures.GatewayAccountFixture;
import uk.gov.pay.directdebit.tokens.fixtures.TokenFixture;

import java.sql.Timestamp;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static java.time.ZonedDateTime.now;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static uk.gov.pay.directdebit.tokens.fixtures.TokenFixture.aTokenFixture;
import static uk.gov.pay.directdebit.util.ZonedDateTimeTimestampMatcher.isDate;

@RunWith(DropwizardJUnitRunner.class)
@DropwizardConfig(app = DirectDebitConnectorApp.class, config = "config/test-it-config.yaml")
public class MandateDaoIT {

    @DropwizardTestContext
    private TestContext testContext;

    private MandateDao mandateDao;
    private GatewayAccountFixture gatewayAccountFixture = GatewayAccountFixture.aGatewayAccountFixture();

    @Before
    public void setup() {
        gatewayAccountFixture.insert(testContext.getJdbi());
        mandateDao = testContext.getJdbi().onDemand(MandateDao.class);
    }

    @Test
    public void shouldInsertAMandateWithoutServiceReference() {
        ZonedDateTime createdDate = now();
        Long id = mandateDao.insert(
                new Mandate(
                        null,
                        gatewayAccountFixture.toEntity(),
                        MandateType.ONE_OFF,
                        MandateExternalId.valueOf(RandomIdGenerator.newId()),
                        "test-reference",
                        null,
                        MandateState.PENDING,
                        "https://www.example.com/return_url",
                        createdDate,
                        null
                )
        );
        Map<String, Object> mandate = testContext.getDatabaseTestHelper().getMandateById(id);
        assertThat(mandate.get("id"), is(id));
        assertThat(mandate.get("external_id"), is(notNullValue()));
        assertThat(mandate.get("mandate_reference"), is("test-reference"));
        assertThat(mandate.get("service_reference"), is(nullValue()));
        assertThat(mandate.get("return_url"), is("https://www.example.com/return_url"));
        assertThat(mandate.get("type"), is("ONE_OFF"));
        assertThat(mandate.get("state"), is("PENDING"));
        assertThat((Timestamp) mandate.get("created_date"), isDate(createdDate));
    }

    @Test
    public void shouldInsertAMandateWithServiceReference() {
        ZonedDateTime createdDate = now();
        Long id = mandateDao.insert(
                new Mandate(
                        null,
                        gatewayAccountFixture.toEntity(),
                        MandateType.ONE_OFF,
                        MandateExternalId.valueOf(RandomIdGenerator.newId()),
                        "test-reference",
                        ServiceMandateReference.valueOf("test-service-reference"),
                        MandateState.PENDING,
                        "https://www.example.com/return_url",
                        createdDate,
                        null
                )
        );
        Map<String, Object> mandate = testContext.getDatabaseTestHelper().getMandateById(id);
        assertThat(mandate.get("id"), is(id));
        assertThat(mandate.get("external_id"), is(notNullValue()));
        assertThat(mandate.get("mandate_reference"), is("test-reference"));
        assertThat(mandate.get("service_reference"), is("test-service-reference"));
        assertThat(mandate.get("return_url"), is("https://www.example.com/return_url"));
        assertThat(mandate.get("type"), is("ONE_OFF"));
        assertThat(mandate.get("state"), is("PENDING"));
        assertThat((Timestamp) mandate.get("created_date"), isDate(createdDate));
    }

    @Test
    public void shouldFindAMandateById() {
        MandateFixture mandateFixture = MandateFixture.aMandateFixture()
                .withMandateReference("test-reference")
                .withServiceReference(ServiceMandateReference.valueOf("test-service-reference"))
                .withGatewayAccountFixture(gatewayAccountFixture)
                .insert(testContext.getJdbi()
                );
        Mandate mandate = mandateDao.findById(mandateFixture.getId()).get();
        assertThat(mandate.getId(), is(mandateFixture.getId()));
        assertThat(mandate.getExternalId(), is(notNullValue()));
        assertThat(mandate.getMandateReference(), is("test-reference"));
        assertThat(mandate.getServiceReference(), is(ServiceMandateReference.valueOf("test-service-reference")));
        assertThat(mandate.getState(), is(MandateState.CREATED));
        assertThat(mandate.getType(), is(mandateFixture.getMandateType()));
    }

    @Test
    public void shouldNotFindAMandateById_ifIdIsInvalid() {
        Long invalidId = 29L;
        assertThat(mandateDao.findById(invalidId), is(Optional.empty()));
    }

    @Test
    public void shouldFindAMandateByTokenId() {
        MandateFixture mandateFixture = MandateFixture.aMandateFixture()
                .withMandateReference("test-reference")
                .withServiceReference(ServiceMandateReference.valueOf("test-service-reference"))
                .withGatewayAccountFixture(gatewayAccountFixture)
                .insert(testContext.getJdbi());

        TokenFixture token = aTokenFixture()
                .withMandateId(mandateFixture.getId())
                .insert(testContext.getJdbi());

        Mandate mandate = mandateDao.findByTokenId(token.getToken()).get();
        assertThat(mandate.getId(), is(mandateFixture.getId()));
        assertThat(mandate.getExternalId(), is(mandateFixture.getExternalId()));
        assertThat(mandate.getMandateReference(), is("test-reference"));
        assertThat(mandate.getServiceReference(), is(ServiceMandateReference.valueOf("test-service-reference")));
        assertThat(mandate.getState(), is(MandateState.CREATED));
        assertThat(mandate.getType(), is(mandateFixture.getMandateType()));
    }

    @Test
    public void shouldNotFindATransactionByTokenId_ifTokenIdIsInvalid() {
        String tokenId = "non_existing_tokenId";
        assertThat(mandateDao.findByTokenId(tokenId), is(Optional.empty()));
    }

    @Test
    public void shouldFindAMandateByExternalId() {
        MandateFixture mandateFixture = MandateFixture.aMandateFixture()
                .withMandateReference("test-reference")
                .withServiceReference(ServiceMandateReference.valueOf("test-service-reference"))
                .withGatewayAccountFixture(gatewayAccountFixture)
                .insert(testContext.getJdbi());

        Mandate mandate = mandateDao.findByExternalId(mandateFixture.getExternalId()).get();
        assertThat(mandate.getId(), is(mandateFixture.getId()));
        assertThat(mandate.getExternalId(), is(notNullValue()));
        assertThat(mandate.getMandateReference(), is("test-reference"));
        assertThat(mandate.getServiceReference(), is(ServiceMandateReference.valueOf("test-service-reference")));
        assertThat(mandate.getState(), is(MandateState.CREATED));
        assertThat(mandate.getType(), is(mandateFixture.getMandateType()));
    }

    @Test
    public void shouldNotFindAMandateByExternalId_ifExternalIdIsInvalid() {
        MandateExternalId invalidMandateId = MandateExternalId.valueOf("invalid1d");
        assertThat(mandateDao.findByExternalId(invalidMandateId), is(Optional.empty()));
    }

    @Test
    public void shouldUpdateStateAndReturnNumberOfAffectedRows() {
        Mandate testMandate = MandateFixture.aMandateFixture().withGatewayAccountFixture(gatewayAccountFixture).insert(testContext.getJdbi()).toEntity();
        MandateState newState = MandateState.FAILED;
        int numOfUpdatedMandates = mandateDao.updateState(testMandate.getId(), newState);

        Map<String, Object> mandateAfterUpdate = testContext.getDatabaseTestHelper().getMandateById(testMandate.getId());
        assertThat(numOfUpdatedMandates, is(1));
        assertThat(mandateAfterUpdate.get("id"), is(testMandate.getId()));
        assertThat(mandateAfterUpdate.get("external_id"), is(testMandate.getExternalId().toString()));
        assertThat(mandateAfterUpdate.get("mandate_reference"), is(testMandate.getMandateReference()));
        assertThat(mandateAfterUpdate.get("service_reference"), is(testMandate.getServiceReference().toString()));
        assertThat(mandateAfterUpdate.get("state"), is(newState.toString()));
        assertThat(mandateAfterUpdate.get("type"), is(testMandate.getType().toString()));
    }

    @Test
    public void shouldNotUpdateAnythingIfTransactionDoesNotExist() {
        int numOfUpdatedMandates = mandateDao.updateState(34L, MandateState.FAILED);
        assertThat(numOfUpdatedMandates, is(0));
    }

    @Test
    public void shouldUpdateReferenceAndReturnNumberOfAffectedRows() {
        Mandate testMandate = MandateFixture.aMandateFixture().withGatewayAccountFixture(gatewayAccountFixture).withMandateReference("old-reference").insert(testContext.getJdbi()).toEntity();
        String newMandateReference = "newReference";
        int numOfUpdatedMandates = mandateDao.updateMandateReference(testMandate.getId(), newMandateReference);

        Map<String, Object> mandateAfterUpdate = testContext.getDatabaseTestHelper().getMandateById(testMandate.getId());
        assertThat(numOfUpdatedMandates, is(1));
        assertThat(mandateAfterUpdate.get("id"), is(testMandate.getId()));
        assertThat(mandateAfterUpdate.get("external_id"), is(testMandate.getExternalId().toString()));
        assertThat(mandateAfterUpdate.get("mandate_reference"), is(newMandateReference));
        assertThat(mandateAfterUpdate.get("state"), is(testMandate.getState().toString()));
        assertThat(mandateAfterUpdate.get("type"), is(testMandate.getType().toString()));
    }

    @Test
    public void shouldNotFindMandateInWrongState() {
        MandateFixture.aMandateFixture()
                .withState(MandateState.PENDING)
                .withGatewayAccountFixture(gatewayAccountFixture)
                .withCreatedDate(ZonedDateTime.now().minusMinutes(91L))
                .insert(testContext.getJdbi());

        MandateStatesGraph mandateStatesGraph = new MandateStatesGraph();
        Set<MandateState> states = mandateStatesGraph.getPriorStates(MandateState.PENDING);
        List<Mandate> transactions = mandateDao.findAllMandatesBySetOfStatesAndMaxCreationTime(states, ZonedDateTime.now().minusMinutes(90L));
        assertThat(transactions.size(), is(0));
    }

    @Test
    public void shouldNotFindMandateWrongCreationTime() {
        MandateFixture.aMandateFixture()
                .withState(MandateState.CREATED)
                .withGatewayAccountFixture(gatewayAccountFixture)
                .withCreatedDate(ZonedDateTime.now())
                .insert(testContext.getJdbi());

        MandateStatesGraph mandateStatesGraph = new MandateStatesGraph();
        Set<MandateState> states = mandateStatesGraph.getPriorStates(MandateState.PENDING);
        List<Mandate> transactions = mandateDao.findAllMandatesBySetOfStatesAndMaxCreationTime(states, ZonedDateTime.now().minusMinutes(90L));
        assertThat(transactions.size(), is(0));
    }

    @Test
    public void shouldFindThreeMandates() {
        MandateFixture.aMandateFixture()
                .withState(MandateState.AWAITING_DIRECT_DEBIT_DETAILS)
                .withGatewayAccountFixture(gatewayAccountFixture)
                .withCreatedDate(ZonedDateTime.now().minusMinutes(200L))
                .insert(testContext.getJdbi());

        MandateFixture.aMandateFixture()
                .withState(MandateState.CREATED)
                .withGatewayAccountFixture(gatewayAccountFixture)
                .withCreatedDate(ZonedDateTime.now().minusMinutes(100L))
                .insert(testContext.getJdbi());

        MandateFixture.aMandateFixture()
                .withState(MandateState.SUBMITTED)
                .withGatewayAccountFixture(gatewayAccountFixture)
                .withCreatedDate(ZonedDateTime.now().minusMinutes(91L))
                .insert(testContext.getJdbi());

        MandateStatesGraph mandateStatesGraph = new MandateStatesGraph();
        Set<MandateState> states = mandateStatesGraph.getPriorStates(MandateState.PENDING);
        List<Mandate> transactions = mandateDao.findAllMandatesBySetOfStatesAndMaxCreationTime(states, ZonedDateTime.now().minusMinutes(90L));
        assertThat(transactions.size(), is(3));
    }
}
