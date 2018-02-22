package uk.gov.pay.directdebit.gatewayaccounts;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.pay.directdebit.gatewayaccounts.dao.GatewayAccountDao;
import uk.gov.pay.directdebit.gatewayaccounts.exception.GatewayAccountNotFoundException;
import uk.gov.pay.directdebit.gatewayaccounts.exception.InvalidGatewayAccountException;
import uk.gov.pay.directdebit.gatewayaccounts.model.GatewayAccount;
import uk.gov.pay.directdebit.payments.fixtures.GatewayAccountFixture;

import java.io.IOException;
import java.util.Optional;

import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class GatewayAccountParamConverterProviderTest {

    @Mock
    GatewayAccountDao mockedGatewayAccountDao;

    GatewayAccountParamConverterProvider.GatewayAccountConverter paramConverter;
    GatewayAccount gatewayAccount = GatewayAccountFixture.aGatewayAccountFixture().toEntity();

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setUp() {
        paramConverter = new GatewayAccountParamConverterProvider(mockedGatewayAccountDao).createGatewayAccountConverter();
    }

    @Test
    public void shouldRetrieveTheCorrectGatewayAccountForAValidId() throws IOException {
        when(mockedGatewayAccountDao.findById(gatewayAccount.getId())).thenReturn(Optional.of(gatewayAccount));
        GatewayAccount convertedGatewayAccount = paramConverter.fromString(this.gatewayAccount.getId().toString());
        Assert.assertThat(convertedGatewayAccount, is(gatewayAccount));
    }

    @Test
    public void shouldThrow_ifGatewayAccountDoesNotExist() throws IOException {
        when(mockedGatewayAccountDao.findById(10L)).thenReturn(Optional.empty());
        thrown.expect(GatewayAccountNotFoundException.class);
        thrown.expectMessage("Unknown gateway account: 10");
        thrown.reportMissingExceptionWithMessage("GatewayAccountNotFoundException expected");
        paramConverter.fromString("10");
    }

    @Test
    public void shouldThrow_ifGatewayAccountIdIsNotValid() throws IOException {
        thrown.expect(InvalidGatewayAccountException.class);
        thrown.expectMessage("Unsupported gateway account: invalid id");
        thrown.reportMissingExceptionWithMessage("InvalidGatewayAccountException expected");
        paramConverter.fromString("notvalid");
    }
}
