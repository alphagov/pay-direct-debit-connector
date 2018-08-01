package uk.gov.pay.directdebit.partnerapp.dao;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import uk.gov.pay.directdebit.DirectDebitConnectorApp;
import uk.gov.pay.directdebit.junit.DropwizardConfig;
import uk.gov.pay.directdebit.junit.DropwizardJUnitRunner;
import uk.gov.pay.directdebit.junit.DropwizardTestContext;
import uk.gov.pay.directdebit.junit.TestContext;
import uk.gov.pay.directdebit.partnerapp.fixtures.PartnerAppTokenEntityFixture;
import uk.gov.pay.directdebit.partnerapp.model.PartnerAppTokenEntity;
import uk.gov.pay.directdebit.payments.fixtures.GatewayAccountFixture;

import java.util.Map;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

@RunWith(DropwizardJUnitRunner.class)
@DropwizardConfig(app = DirectDebitConnectorApp.class, config = "config/test-it-config.yaml")
public class PartnerAppTokenDaoIT {

    @DropwizardTestContext
    private TestContext testContext;

    private PartnerAppTokenDao tokenDao;

    private PartnerAppTokenEntityFixture tokenEntityFixture;
    private GatewayAccountFixture gatewayAccountFixture;

    @Before
    public void setup() {
        tokenDao = testContext.getJdbi().onDemand(PartnerAppTokenDao.class);
        gatewayAccountFixture = GatewayAccountFixture.aGatewayAccountFixture()
                .insert(testContext.getJdbi());
        tokenEntityFixture = PartnerAppTokenEntityFixture
                .aPartnerAppTokenFixture()
                .withGatewayAccountId(gatewayAccountFixture.getId());
    }

    @Test
    public void shouldInsertAGoCardlessPartnerAppToken() {
        PartnerAppTokenEntity appTokenEntity = tokenEntityFixture.toEntity();
        Long id = tokenDao.insert(appTokenEntity);
        Map<String, Object> mandate = testContext.getDatabaseTestHelper().getGoCardlessPartnerAppTokenById(id);
        assertThat(mandate.get("id"), is(id));
        assertThat(mandate.get("token"), is(appTokenEntity.getToken()));
        assertThat(mandate.get("active"), is(true));
        assertThat(mandate.get("gateway_account_id"), is(gatewayAccountFixture.getId()));
    }

    @Test
    public void shouldGetAnExistingGoCardlessPartnerAppTokenByGatewayAccountId() {
        tokenEntityFixture.insert(testContext.getJdbi());
        PartnerAppTokenEntity thatTokenEntity = tokenEntityFixture.toEntity();
        PartnerAppTokenEntity thisTokenEntity = tokenDao.findByGatewayAccountId(gatewayAccountFixture.getId()).get();
        assertThat(thisTokenEntity.getId(), is(1L));
        assertThat(thisTokenEntity.getToken(), is(thatTokenEntity.getToken()));
        assertThat(thisTokenEntity.isActive(), is(true));
        assertThat(thisTokenEntity.getGatewayAccountId(), is(thatTokenEntity.getGatewayAccountId()));
    }

    @Test
    public void shouldGetAnExistingGoCardlessPartnerAppTokenByTokenAndGatewayAccountId() {
        tokenEntityFixture.insert(testContext.getJdbi());
        PartnerAppTokenEntity thatTokenEntity = tokenEntityFixture.toEntity();
        PartnerAppTokenEntity thisTokenEntity = tokenDao.findByTokenAndGatewayAccountId(thatTokenEntity.getToken(), gatewayAccountFixture.getId()).get();
        assertThat(thisTokenEntity.getId(), is(1L));
        assertThat(thisTokenEntity.getToken(), is(thatTokenEntity.getToken()));
        assertThat(thisTokenEntity.isActive(), is(true));
        assertThat(thisTokenEntity.getGatewayAccountId(), is(thatTokenEntity.getGatewayAccountId()));
    }

    @Test
    public void shouldDisableAnExistingGoCardlessPartnerAppToken() {
        tokenEntityFixture.insert(testContext.getJdbi());
        PartnerAppTokenEntity thatTokenEntity = tokenEntityFixture.toEntity();
        tokenDao.disableToken(thatTokenEntity.getToken(), thatTokenEntity.getGatewayAccountId());
        assertThat(tokenDao.findByGatewayAccountId(gatewayAccountFixture.getId()).isPresent(), is(false));
    }
}
