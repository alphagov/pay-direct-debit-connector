package uk.gov.pay.directdebit.partnerapp.resources;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.pay.directdebit.common.exception.BadRequestException;
import uk.gov.pay.directdebit.partnerapp.services.GoCardlessAppConnectAccountService;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.pay.directdebit.partnerapp.resources.GoCardlessAppConnectAccountStateResource.GATEWAY_ACCOUNT_ID_FIELD;
import static uk.gov.pay.directdebit.partnerapp.resources.GoCardlessAppConnectAccountStateResource.REDIRECT_URI_FIELD;

@RunWith(MockitoJUnitRunner.class)
public class GoCardlessAppConnectAccountStateResourceTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Mock
    private GoCardlessAppConnectAccountService mockedTokenService;

    private GoCardlessAppConnectAccountStateResource resource;
    private static final String GATEWAY_EXTERNAL_ID = "some-test-test-external-id";
    private static final String REDIRECT_URI = "https://example.com/oauth/complete";
    private final Map<String, String> REQUEST_MAP = new HashMap<>();

    @Before
    public void setUp() {
        REQUEST_MAP.put(GATEWAY_ACCOUNT_ID_FIELD, GATEWAY_EXTERNAL_ID);
        REQUEST_MAP.put(REDIRECT_URI_FIELD, REDIRECT_URI);
        resource = new GoCardlessAppConnectAccountStateResource(mockedTokenService);
    }

    @Test
    public void createToken_shouldReturnBadRequest_whenMissingGatewayAccountFromRequest() {
        thrown.expect(BadRequestException.class);
        thrown.expectMessage("Field(s) missing: [gateway_account_id, redirect_uri]");
        thrown.reportMissingExceptionWithMessage("BadRequestException expected");
        resource.createGoCardlessPartnerAppConnectTokenState(new HashMap<>());
    }
}
