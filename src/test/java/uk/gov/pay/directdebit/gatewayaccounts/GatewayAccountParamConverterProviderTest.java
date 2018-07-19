package uk.gov.pay.directdebit.gatewayaccounts;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.pay.directdebit.gatewayaccounts.dao.GatewayAccountSelectDao;
import uk.gov.pay.directdebit.gatewayaccounts.exception.GatewayAccountNotFoundException;
import uk.gov.pay.directdebit.gatewayaccounts.model.GatewayAccount;
import uk.gov.pay.directdebit.payments.fixtures.GatewayAccountFixture;

import java.util.Optional;

import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class GatewayAccountParamConverterProviderTest {

    @Mock
    GatewayAccountSelectDao mockedGatewayAccountSelectDao;

    GatewayAccountParamConverterProvider.GatewayAccountConverter paramConverter;
    GatewayAccount gatewayAccount = GatewayAccountFixture.aGatewayAccountFixture().toEntity();

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setUp() {
        paramConverter = new GatewayAccountParamConverterProvider(mockedGatewayAccountSelectDao).createGatewayAccountConverter();
    }

    @Test
    public void shouldRetrieveTheCorrectGatewayAccountForAValidId() {
        when(mockedGatewayAccountSelectDao.findByExternalId(gatewayAccount.getExternalId())).thenReturn(Optional.of(gatewayAccount));
        GatewayAccount convertedGatewayAccount = paramConverter.fromString(gatewayAccount.getExternalId());
        Assert.assertThat(convertedGatewayAccount, is(gatewayAccount));
    }

    @Test
    public void shouldThrow_ifGatewayAccountDoesNotExist() {
        when(mockedGatewayAccountSelectDao.findByExternalId("not-existing")).thenReturn(Optional.empty());
        thrown.expect(GatewayAccountNotFoundException.class);
        thrown.expectMessage("Unknown gateway account: not-existing");
        thrown.reportMissingExceptionWithMessage("GatewayAccountNotFoundException expected");
        paramConverter.fromString("not-existing");
    }
}
