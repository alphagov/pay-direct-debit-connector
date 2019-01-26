package uk.gov.pay.directdebit.partnerapp.resources;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.pay.directdebit.common.exception.BadRequestException;
import uk.gov.pay.directdebit.partnerapp.services.GoCardlessAppConnectAccountService;

import java.util.HashMap;

@RunWith(MockitoJUnitRunner.class)
public class GoCardlessAppConnectAccountStateResourceTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Mock
    private GoCardlessAppConnectAccountService mockedTokenService;

    @Test
    public void createToken_shouldReturnBadRequest_whenMissingGatewayAccountFromRequest() {
        GoCardlessAppConnectAccountStateResource resource = new GoCardlessAppConnectAccountStateResource(mockedTokenService);
        thrown.expect(BadRequestException.class);
        thrown.expectMessage("Field(s) missing: [gateway_account_id, redirect_uri]");
        thrown.reportMissingExceptionWithMessage("BadRequestException expected");
        resource.createGoCardlessPartnerAppConnectTokenState(new HashMap<>());
    }
}
