package uk.gov.pay.directdebit.tokens.services;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.pay.directdebit.mandate.fixtures.MandateFixture;
import uk.gov.pay.directdebit.payments.model.Token;
import uk.gov.pay.directdebit.tokens.dao.TokenDao;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class TokenServiceTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();
    @Mock
    private TokenDao mockedTokenDao;
    @Mock
    private TokenService service;

    @Before
    public void setUp() {
        service = new TokenService(mockedTokenDao);
    }

    @Test
    public void shouldGenerateANewToken() {
        MandateFixture mandateFixture = MandateFixture.aMandateFixture();
        Token token = service.generateNewTokenFor(mandateFixture.toEntity());
        assertThat(token.getId(), is(notNullValue()));
        assertThat(token.getToken(), is(notNullValue()));
        assertThat(token.getMandateId(), is(mandateFixture.getId()));
    }
}
