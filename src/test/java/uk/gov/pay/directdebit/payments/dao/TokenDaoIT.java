package uk.gov.pay.directdebit.payments.dao;

import liquibase.exception.LiquibaseException;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import uk.gov.pay.directdebit.infra.DaoITestBase;
import uk.gov.pay.directdebit.payments.fixtures.PaymentRequestFixture;
import uk.gov.pay.directdebit.payments.fixtures.TokenFixture;
import uk.gov.pay.directdebit.payments.model.Token;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static uk.gov.pay.directdebit.payments.fixtures.PaymentRequestFixture.paymentRequestFixture;
import static uk.gov.pay.directdebit.payments.fixtures.TokenFixture.tokenFixture;


public class TokenDaoIT extends DaoITestBase {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    private TokenDao tokenDao;

    private PaymentRequestFixture testPaymentRequest;
    private TokenFixture testToken;

    @Before
    public void setup() throws IOException, LiquibaseException {
        tokenDao = jdbi.onDemand(TokenDao.class);
        this.testPaymentRequest = paymentRequestFixture(jdbi)
                .withGatewayAccountId(RandomUtils.nextLong(1, 99999))
                .insert();
       this.testToken = tokenFixture(jdbi)
                .withPaymentRequestId(testPaymentRequest.getId())
                .insert();
    }


    @Test
    public void shouldInsertAToken() {
        Long id = tokenDao.insert(testToken.toEntity());
        Map<String, Object> foundToken = databaseTestHelper.getTokenByPaymentRequestId(testPaymentRequest.getId());
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
}
