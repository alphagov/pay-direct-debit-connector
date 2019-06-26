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
import uk.gov.pay.directdebit.mandate.model.GoCardlessMandateId;
import uk.gov.pay.directdebit.mandate.model.Mandate;
import uk.gov.pay.directdebit.mandate.model.MandateBankStatementReference;
import uk.gov.pay.directdebit.mandate.model.MandateState;
import uk.gov.pay.directdebit.mandate.model.MandateStatesGraph;
import uk.gov.pay.directdebit.mandate.model.PaymentProviderMandateId;
import uk.gov.pay.directdebit.mandate.model.SandboxMandateId;
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
import static uk.gov.pay.directdebit.mandate.model.Mandate.MandateBuilder.aMandate;
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
    public void shouldInsertAMandateWithServiceReference() {
        ZonedDateTime createdDate = now();
        Long id = mandateDao.insert(aMandate()
                        .withGatewayAccount(gatewayAccountFixture.toEntity())
                        .withExternalId(MandateExternalId.valueOf(RandomIdGenerator.newId()))
                        .withMandateBankStatementReference(MandateBankStatementReference.valueOf("test-reference"))
                        .withServiceReference("test-service-reference")
                        .withState(MandateState.PENDING)
                        .withReturnUrl("https://www.example.com/return_url")
                        .withCreatedDate(createdDate)
                        .build());

        Map<String, Object> mandate = testContext.getDatabaseTestHelper().getMandateById(id);
        assertThat(mandate.get("id"), is(id));
        assertThat(mandate.get("external_id"), is(notNullValue()));
        assertThat(mandate.get("mandate_reference"), is("test-reference"));
        assertThat(mandate.get("service_reference"), is("test-service-reference"));
        assertThat(mandate.get("return_url"), is("https://www.example.com/return_url"));
        assertThat(mandate.get("state"), is("PENDING"));
        assertThat((Timestamp) mandate.get("created_date"), isDate(createdDate));
        assertThat(mandate.get("payment_provider"), is(nullValue()));
    }

    @Test
    public void shouldFindAMandateById() {
        PaymentProviderMandateId paymentProviderMandateId = SandboxMandateId.valueOf("aSandboxMandateId");
        MandateFixture mandateFixture = MandateFixture.aMandateFixture()
                .withMandateBankStatementReference(MandateBankStatementReference.valueOf("test-reference"))
                .withServiceReference("test-service-reference")
                .withGatewayAccountFixture(gatewayAccountFixture)
                .withPaymentProviderId(paymentProviderMandateId)
                .insert(testContext.getJdbi());

        Mandate mandate = mandateDao.findById(mandateFixture.getId()).get();
        assertThat(mandate.getId(), is(mandateFixture.getId()));
        assertThat(mandate.getExternalId(), is(notNullValue()));
        assertThat(mandate.getMandateBankStatementReference().get(), is(MandateBankStatementReference.valueOf("test-reference")));
        assertThat(mandate.getServiceReference(), is("test-service-reference"));
        assertThat(mandate.getState(), is(MandateState.CREATED));
        assertThat(mandate.getPaymentProviderMandateId().get(), is(paymentProviderMandateId));
    }

    @Test
    public void shouldNotFindAMandateById_ifIdIsInvalid() {
        Long invalidId = 29L;
        assertThat(mandateDao.findById(invalidId), is(Optional.empty()));
    }

    @Test
    public void shouldFindAMandateByTokenId() {
        MandateFixture mandateFixture = MandateFixture.aMandateFixture()
                .withMandateBankStatementReference(MandateBankStatementReference.valueOf("test-reference"))
                .withServiceReference("test-service-reference")
                .withGatewayAccountFixture(gatewayAccountFixture)
                .insert(testContext.getJdbi());

        TokenFixture token = aTokenFixture()
                .withMandateId(mandateFixture.getId())
                .insert(testContext.getJdbi());

        Mandate mandate = mandateDao.findByTokenId(token.getToken()).get();
        assertThat(mandate.getId(), is(mandateFixture.getId()));
        assertThat(mandate.getExternalId(), is(mandateFixture.getExternalId()));
        assertThat(mandate.getMandateBankStatementReference().get(), is(MandateBankStatementReference.valueOf("test-reference")));
        assertThat(mandate.getServiceReference(), is("test-service-reference"));
        assertThat(mandate.getState(), is(MandateState.CREATED));
    }

    @Test
    public void shouldNotFindATransactionByTokenId_ifTokenIdIsInvalid() {
        String tokenId = "non_existing_tokenId";
        assertThat(mandateDao.findByTokenId(tokenId), is(Optional.empty()));
    }

    @Test
    public void shouldFindAMandateByExternalId() {
        MandateFixture mandateFixture = MandateFixture.aMandateFixture()
                .withMandateBankStatementReference(MandateBankStatementReference.valueOf("test-reference"))
                .withServiceReference("test-service-reference")
                .withGatewayAccountFixture(gatewayAccountFixture)
                .insert(testContext.getJdbi());

        Mandate mandate = mandateDao.findByExternalId(mandateFixture.getExternalId()).get();
        assertThat(mandate.getId(), is(mandateFixture.getId()));
        assertThat(mandate.getExternalId(), is(notNullValue()));
        assertThat(mandate.getMandateBankStatementReference().get(), is(MandateBankStatementReference.valueOf("test-reference")));
        assertThat(mandate.getServiceReference(), is("test-service-reference"));
        assertThat(mandate.getState(), is(MandateState.CREATED));
    }

    @Test
    public void shouldNotFindAMandateByExternalId_ifExternalIdDoesNotExist() {
        MandateExternalId invalidMandateId = MandateExternalId.valueOf("invalid1d");
        assertThat(mandateDao.findByExternalId(invalidMandateId), is(Optional.empty()));
    }

    @Test
    public void shouldFindAMandateByExternalIdAndGatewayAccount() {
        MandateFixture mandateFixture = MandateFixture.aMandateFixture()
                .withMandateBankStatementReference(MandateBankStatementReference.valueOf("test-reference"))
                .withServiceReference("test-service-reference")
                .withGatewayAccountFixture(gatewayAccountFixture)
                .insert(testContext.getJdbi());

        Mandate mandate = mandateDao.findByExternalIdAndGatewayAccountExternalId(mandateFixture.getExternalId(), gatewayAccountFixture.getExternalId()).get();

        assertThat(mandate.getId(), is(mandateFixture.getId()));
        assertThat(mandate.getExternalId(), is(notNullValue()));
        assertThat(mandate.getMandateBankStatementReference().get(), is(MandateBankStatementReference.valueOf("test-reference")));
        assertThat(mandate.getServiceReference(), is("test-service-reference"));
        assertThat(mandate.getState(), is(MandateState.CREATED));
    }

    @Test
    public void shouldNotFindAMandateByExternalIdAndGatewayAccountId_ifGatewayAccountIdIsNotCorrect() {
        MandateFixture mandateFixture = MandateFixture.aMandateFixture()
                .withMandateBankStatementReference(MandateBankStatementReference.valueOf("test-reference"))
                .withServiceReference("test-service-reference")
                .withGatewayAccountFixture(gatewayAccountFixture)
                .insert(testContext.getJdbi());

        assertThat(mandateDao.findByExternalIdAndGatewayAccountExternalId(mandateFixture.getExternalId(), "xxxx"), is(Optional.empty()));
    }
    
    
    @Test
    public void shouldFindAMandateByPaymentProviderId() {
        GoCardlessMandateId goCardlessMandateId = GoCardlessMandateId.valueOf("expectedGoCardlessMandateId");
        MandateFixture mandateFixture = MandateFixture.aMandateFixture()
                .withGatewayAccountFixture(gatewayAccountFixture)
                .withExternalId(MandateExternalId.valueOf("expectedExternalId"))
                .withPaymentProviderId(goCardlessMandateId)
                .insert(testContext.getJdbi());

        Mandate mandate = mandateDao.findByPaymentProviderMandateId(goCardlessMandateId).get();
        assertThat(mandate.getId(), is(mandateFixture.getId()));
        assertThat(mandate.getExternalId().toString(), is("expectedExternalId"));
        assertThat(mandate.getPaymentProviderMandateId().get().toString(), is("expectedGoCardlessMandateId"));
        assertThat(mandate.getState(), is(MandateState.CREATED));
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
        assertThat(mandateAfterUpdate.get("mandate_reference"), is(testMandate.getMandateBankStatementReference().get().toString()));
        assertThat(mandateAfterUpdate.get("service_reference"), is(testMandate.getServiceReference()));
        assertThat(mandateAfterUpdate.get("state"), is(newState.toString()));
    }

    @Test
    public void shouldNotUpdateAnythingIfTransactionDoesNotExist() {
        int numOfUpdatedMandates = mandateDao.updateState(34L, MandateState.FAILED);
        assertThat(numOfUpdatedMandates, is(0));
    }

    @Test
    public void shouldUpdateReferenceAndPaymentProviderId() {
        var bankStatementReference = MandateBankStatementReference.valueOf("newReference");
        var paymentProviderId = GoCardlessMandateId.valueOf("aPaymentProviderId");

        Mandate testMandate = MandateFixture.aMandateFixture()
                .withGatewayAccountFixture(gatewayAccountFixture)
                .withMandateBankStatementReference(MandateBankStatementReference.valueOf("old-reference"))
                .insert(testContext.getJdbi())
                .toEntity();

        Mandate confirmedTestMandate = Mandate.MandateBuilder.fromMandate(testMandate)
                .withMandateBankStatementReference(bankStatementReference)
                .withPaymentProviderId(paymentProviderId)
                .build();

        int numOfUpdatedMandates = mandateDao.updateReferenceAndPaymentProviderId(confirmedTestMandate);

        Map<String, Object> mandateAfterUpdate = testContext.getDatabaseTestHelper().getMandateById(testMandate.getId());
        assertThat(numOfUpdatedMandates, is(1));
        assertThat(mandateAfterUpdate.get("id"), is(testMandate.getId()));
        assertThat(mandateAfterUpdate.get("external_id"), is(testMandate.getExternalId().toString()));
        assertThat(mandateAfterUpdate.get("mandate_reference"), is(bankStatementReference.toString()));
        assertThat(mandateAfterUpdate.get("state"), is(testMandate.getState().toString()));
        assertThat(mandateAfterUpdate.get("payment_provider_id"), is(paymentProviderId.toString()));
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
