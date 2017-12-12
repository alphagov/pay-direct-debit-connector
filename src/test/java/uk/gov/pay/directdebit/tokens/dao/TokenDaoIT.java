package uk.gov.pay.directdebit.tokens.dao;

import liquibase.exception.LiquibaseException;
import org.apache.commons.lang3.RandomUtils;
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
import uk.gov.pay.directdebit.payments.fixtures.PaymentRequestFixture;
import uk.gov.pay.directdebit.payments.model.Token;
import uk.gov.pay.directdebit.tokens.fixtures.TokenFixture;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static uk.gov.pay.directdebit.payments.fixtures.PaymentRequestFixture.aPaymentRequestFixture;
import static uk.gov.pay.directdebit.tokens.fixtures.TokenFixture.aTokenFixture;

@RunWith(DropwizardJUnitRunner.class)
@DropwizardConfig(app = DirectDebitConnectorApp.class, config = "config/test-it-config.yaml")
public class TokenDaoIT {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    private TokenDao tokenDao;

    private PaymentRequestFixture testPaymentRequest;
    private TokenFixture testToken;

    @DropwizardTestContext
    private TestContext testContext;

    @Before
    public void setup() throws IOException, LiquibaseException {
        tokenDao = testContext.getJdbi().onDemand(TokenDao.class);
        this.testPaymentRequest = aPaymentRequestFixture()
                .withGatewayAccountId(RandomUtils.nextLong(1, 99999))
                .insert(testContext.getJdbi());
        this.testToken = aTokenFixture()
                .withPaymentRequestId(testPaymentRequest.getId())
                .insert(testContext.getJdbi());
    }


    @Test
    public void shouldInsertAToken() {
        Long id = tokenDao.insert(testToken.toEntity());
        Map<String, Object> foundToken = testContext.getDatabaseTestHelper().getTokenByPaymentRequestId(testPaymentRequest.getId());
        assertThat(foundToken.get("id"), is(id));
        assertThat(foundToken.get("payment_request_id"), is(testToken.getPaymentRequestId()));
        assertThat(foundToken.get("secure_redirect_token"), is(testToken.getToken()));
    }

    @Test
    public void findByChargeId_shouldFindToken() {
        Token token = tokenDao.findByPaymentId(testPaymentRequest.getId()).get();
        assertThat(token.getId(), is(notNullValue()));
        assertThat(token.getToken(), is(testToken.getToken()));
        assertThat(token.getPaymentRequestId(), is(testPaymentRequest.getId()));
    }

    @Test
    public void findByChargeId_shouldNotFindToken() {
        Long noExistingChargeId = 9876512L;
        assertThat(tokenDao.findByPaymentId(noExistingChargeId).isPresent(), is(false));
    }

    @Test
    public void findByTokenId_shouldFindToken() {
        Token token = tokenDao.findByTokenId(testToken.getToken()).get();
        assertThat(token.getId(), is(notNullValue()));
        assertThat(token.getPaymentRequestId(), is(testPaymentRequest.getId()));
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
