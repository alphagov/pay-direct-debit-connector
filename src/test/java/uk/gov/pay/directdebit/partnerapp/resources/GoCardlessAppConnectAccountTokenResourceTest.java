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

import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static uk.gov.pay.directdebit.partnerapp.resources.GoCardlessAppConnectAccountTokenResource.CODE_FIELD;
import static uk.gov.pay.directdebit.partnerapp.resources.GoCardlessAppConnectAccountTokenResource.STATE_FIELD;

@RunWith(MockitoJUnitRunner.class)
public class GoCardlessAppConnectAccountTokenResourceTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Mock
    private GoCardlessAppConnectAccountService mockedTokenService;

    private GoCardlessAppConnectAccountTokenResource resource;
    private static final String ACCESS_CODE = "some-access-code";
    private static final String STATE_TOKEN = "some-partner-state-example";
    private final Map<String, String> REQUEST_MAP = new HashMap<>();

    @Before
    public void setUp() {
        REQUEST_MAP.put(CODE_FIELD, ACCESS_CODE);
        REQUEST_MAP.put(STATE_FIELD, STATE_TOKEN);
        resource = new GoCardlessAppConnectAccountTokenResource(mockedTokenService);
    }

    @Test
    public void shouldReturnOK_whenExchangeAccessCodeForTokenSuccessfully() {
        when(mockedTokenService.exchangeCodeForToken(ACCESS_CODE, STATE_TOKEN)).thenReturn(Response.ok().build());
        Response response = resource.getGoCardlessConnectAccessToken(REQUEST_MAP);
        assertThat(response.getStatus(), is(200));
    }

    @Test
    public void shouldReturnBadRequest_whenMissingAccessCode() {
        thrown.expect(BadRequestException.class);
        thrown.expectMessage("Field(s) missing: [code]");
        thrown.reportMissingExceptionWithMessage("BadRequestException expected");
        REQUEST_MAP.remove(CODE_FIELD);
        resource.getGoCardlessConnectAccessToken(REQUEST_MAP);
    }

    @Test
    public void shouldReturnBadRequest_whenMissingPartnerState() {
        thrown.expect(BadRequestException.class);
        thrown.expectMessage("Field(s) missing: [state]");
        thrown.reportMissingExceptionWithMessage("BadRequestException expected");
        REQUEST_MAP.remove(STATE_FIELD);
        resource.getGoCardlessConnectAccessToken(REQUEST_MAP);
    }
}
