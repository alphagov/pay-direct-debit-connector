package uk.gov.pay.directdebit.partnerapp.dao;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import uk.gov.pay.directdebit.DirectDebitConnectorApp;
import uk.gov.pay.directdebit.junit.DropwizardConfig;
import uk.gov.pay.directdebit.junit.DropwizardJUnitRunner;
import uk.gov.pay.directdebit.junit.DropwizardTestContext;
import uk.gov.pay.directdebit.junit.TestContext;
import uk.gov.pay.directdebit.partnerapp.fixtures.GoCardlessAppConnectAccountEntityFixture;
import uk.gov.pay.directdebit.partnerapp.model.GoCardlessAppConnectAccountEntity;
import uk.gov.pay.directdebit.payments.fixtures.GatewayAccountFixture;

import java.util.Map;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

@RunWith(DropwizardJUnitRunner.class)
@DropwizardConfig(app = DirectDebitConnectorApp.class, config = "config/test-it-config.yaml")
public class GoCardlessAppConnectAccountTokenDaoIT {

    @DropwizardTestContext
    private TestContext testContext;

    private GoCardlessAppConnectAccountTokenDao tokenDao;

    private GoCardlessAppConnectAccountEntityFixture accountEntityFixture;
    private GatewayAccountFixture gatewayAccountFixture;

    @Before
    public void setUp() {
        tokenDao = testContext.getJdbi().onDemand(GoCardlessAppConnectAccountTokenDao.class);
        gatewayAccountFixture = GatewayAccountFixture.aGatewayAccountFixture()
                .insert(testContext.getJdbi());
        accountEntityFixture = GoCardlessAppConnectAccountEntityFixture
                .aPartnerAppAccountFixture()
                .withGatewayAccountId(gatewayAccountFixture.getId());
    }

    @Test
    public void shouldInsertAGoCardlessPartnerAppToken() {
        GoCardlessAppConnectAccountEntity accountEntity = accountEntityFixture.toEntity();
        Long id = tokenDao.insert(accountEntity);
        Map<String, Object> mandate = testContext.getDatabaseTestHelper().getGoCardlessPartnerAppTokenById(id);
        assertThat(mandate.get("id"), is(id));
        assertThat(mandate.get("token"), is(accountEntity.getToken()));
        assertThat(mandate.get("active"), is(true));
        assertThat(mandate.get("gateway_account_id"), is(gatewayAccountFixture.getId()));
        assertThat(mandate.get("redirect_uri"), is(accountEntity.getRedirectUri()));
    }

    @Test
    public void shouldGetAnExistingGoCardlessPartnerAppTokenByGatewayAccountId() {
        accountEntityFixture.insert(testContext.getJdbi());
        GoCardlessAppConnectAccountEntity thatAccountEntity = accountEntityFixture.toEntity();
        GoCardlessAppConnectAccountEntity thisAccountEntity = tokenDao.findByGatewayAccountId(gatewayAccountFixture.getId()).get();
        assertThat(thisAccountEntity.getId(), is(1L));
        assertThat(thisAccountEntity.getToken(), is(thatAccountEntity.getToken()));
        assertThat(thisAccountEntity.isActive(), is(true));
        assertThat(thisAccountEntity.getGatewayAccountId(), is(thatAccountEntity.getGatewayAccountId()));
    }

    @Test
    public void shouldGetAnExistingGoCardlessPartnerAppTokenByTokenAndGatewayAccountId() {
        accountEntityFixture.insert(testContext.getJdbi());
        GoCardlessAppConnectAccountEntity thatAccountEntity = accountEntityFixture.toEntity();
        GoCardlessAppConnectAccountEntity thisAccountEntity = tokenDao.findByTokenAndGatewayAccountId(thatAccountEntity.getToken(), gatewayAccountFixture.getId()).get();
        assertThat(thisAccountEntity.getId(), is(1L));
        assertThat(thisAccountEntity.getToken(), is(thatAccountEntity.getToken()));
        assertThat(thisAccountEntity.isActive(), is(true));
        assertThat(thisAccountEntity.getGatewayAccountId(), is(thatAccountEntity.getGatewayAccountId()));
    }

    @Test
    public void shouldDisableAnExistingGoCardlessPartnerAppToken() {
        accountEntityFixture.insert(testContext.getJdbi());
        GoCardlessAppConnectAccountEntity thatAccountEntity = accountEntityFixture.toEntity();
        tokenDao.disableToken(thatAccountEntity.getToken(), thatAccountEntity.getGatewayAccountId());
        assertThat(tokenDao.findByGatewayAccountId(gatewayAccountFixture.getId()).isPresent(), is(false));
    }

    @Test
    public void shouldFindAnActiveGoCardlessPartnerAppTokenByToken() {
        accountEntityFixture.insert(testContext.getJdbi());
        GoCardlessAppConnectAccountEntity thatAccountEntity = accountEntityFixture.toEntity();
        GoCardlessAppConnectAccountEntity thisAccountEntity = tokenDao.findActiveTokenByToken(thatAccountEntity.getToken()).get();
        assertThat(thisAccountEntity.getId(), is(1L));
        assertThat(thisAccountEntity.getToken(), is(thatAccountEntity.getToken()));
        assertThat(thisAccountEntity.isActive(), is(true));
        assertThat(thisAccountEntity.getGatewayAccountId(), is(thatAccountEntity.getGatewayAccountId()));
    }
}
