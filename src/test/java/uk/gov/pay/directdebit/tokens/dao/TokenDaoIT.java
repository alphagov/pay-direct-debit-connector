package uk.gov.pay.directdebit.tokens.dao;

import java.util.Map;
import java.util.Optional;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import uk.gov.pay.directdebit.DirectDebitConnectorApp;
import uk.gov.pay.directdebit.junit.DropwizardConfig;
import uk.gov.pay.directdebit.junit.DropwizardJUnitRunner;
import uk.gov.pay.directdebit.junit.DropwizardTestContext;
import uk.gov.pay.directdebit.junit.TestContext;
import uk.gov.pay.directdebit.mandate.fixtures.MandateFixture;
import uk.gov.pay.directdebit.payments.fixtures.GatewayAccountFixture;
import uk.gov.pay.directdebit.payments.fixtures.TransactionFixture;
import uk.gov.pay.directdebit.payments.model.Token;
import uk.gov.pay.directdebit.tokens.fixtures.TokenFixture;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static uk.gov.pay.directdebit.tokens.fixtures.TokenFixture.aTokenFixture;

@RunWith(DropwizardJUnitRunner.class)
@DropwizardConfig(app = DirectDebitConnectorApp.class, config = "config/test-it-config.yaml")
public class TokenDaoIT {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    private TokenDao tokenDao;

    private TransactionFixture testTransaction;
    private TokenFixture testToken;
    private GatewayAccountFixture gatewayAccountFixture;
    private MandateFixture mandateFixture;
    @DropwizardTestContext
    private TestContext testContext;

    @Before
    public void setup() {
        tokenDao = testContext.getJdbi().onDemand(TokenDao.class);
        gatewayAccountFixture = GatewayAccountFixture.aGatewayAccountFixture().insert(testContext.getJdbi());
        mandateFixture = MandateFixture.aMandateFixture().withGatewayAccountFixture(gatewayAccountFixture).insert(testContext.getJdbi());
        testTransaction = TransactionFixture.aTransactionFixture()
                .withMandateFixture(mandateFixture)
                .insert(testContext.getJdbi());
        testToken = aTokenFixture()
                .withMandateId(mandateFixture.getId())
                .insert(testContext.getJdbi());
    }


    @Test
    public void shouldInsertAToken() {
        Long id = tokenDao.insert(testToken.toEntity());
        Map<String, Object> foundToken = testContext.getDatabaseTestHelper().getTokenByMandateId(mandateFixture.getId());
        assertThat(foundToken.get("id"), is(id));
        assertThat(foundToken.get("mandate_id"), is(testToken.getMandateId()));
        assertThat(foundToken.get("secure_redirect_token"), is(testToken.getToken()));
    }

    @Test
    public void findByTokenId_shouldFindToken() {
        Token token = tokenDao.findByTokenId(testToken.getToken()).get();
        assertThat(token.getId(), is(notNullValue()));
        assertThat(token.getMandateId(), is(mandateFixture.getId()));
        assertThat(token.getToken(), is(testToken.getToken()));
    }

    @Test
    public void findByTokenId_shouldNotFindToken() {
        String tokenId = "non_existing_tokenId";
        assertThat(tokenDao.findByTokenId(tokenId), is(Optional.empty()));
    }

    @Test
    public void deleteToken_shouldDeleteTokenAndReturnNumberOfAffectedRows() {
        int numOfDeletedToken = tokenDao.deleteToken(testToken.getToken());
        Optional<Token> maybeTokenAfterDeletion = tokenDao.findByTokenId(testToken.getToken());
        assertThat(numOfDeletedToken, is(1));
        assertThat(maybeTokenAfterDeletion.isPresent(), is(false));
    }

    @Test
    public void deleteToken_shouldNotDeleteAnythingIfTokenDoesNotExist() {
        int numOfDeletedToken = tokenDao.deleteToken("not_existing");
        assertThat(numOfDeletedToken, is(0));
    }
}
