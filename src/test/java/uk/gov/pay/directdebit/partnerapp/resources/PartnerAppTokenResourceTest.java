package uk.gov.pay.directdebit.partnerapp.resources;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.pay.directdebit.common.exception.BadRequestException;
import uk.gov.pay.directdebit.partnerapp.api.PartnerAppTokenResponse;
import uk.gov.pay.directdebit.partnerapp.fixtures.PartnerAppTokenEntityFixture;
import uk.gov.pay.directdebit.partnerapp.model.PartnerAppTokenEntity;
import uk.gov.pay.directdebit.partnerapp.services.PartnerAppTokenService;

import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PartnerAppTokenResourceTest {

    private static final long GATEWAY_ACCOUNT_ID = 1L;
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Mock
    private PartnerAppTokenService mockedTokenService;

    private PartnerAppTokenResource resource;
    private static final String GATEWAY_EXTERNAL_ID = "some-test-id";
    private static final String REDIRECT_URI = "https://example.com/oauth/complete";
    private static final Map<String, String> REQUEST_MAP = new HashMap<>();
    private PartnerAppTokenEntity tokenEntity;

    @Before
    public void SetUp() {
        REQUEST_MAP.put("gateway_account_id", GATEWAY_EXTERNAL_ID);
        REQUEST_MAP.put("redirect_uri", REDIRECT_URI);
        resource = new PartnerAppTokenResource(mockedTokenService);
        tokenEntity = PartnerAppTokenEntityFixture.aPartnerAppTokenFixture()
                .withGatewayAccountId(GATEWAY_ACCOUNT_ID)
                .toEntity();
    }

    @Test
    public void shouldCreateAToken() {
        when(mockedTokenService.createToken(GATEWAY_EXTERNAL_ID, REDIRECT_URI)).thenReturn(Optional.of(tokenEntity));
        Response response = resource.createGoCardlessPartnerAppConnectTokenState(REQUEST_MAP);
        String selfLink = "/v1/api/gocardless/partnerapp/tokens/" + tokenEntity.getToken();

        assertThat(response.getStatus(), is(201));
        assertThat(response.getLocation(), is(URI.create(selfLink)));

        PartnerAppTokenResponse entity = (PartnerAppTokenResponse) response.getEntity();
        assertThat(entity.getToken(), is(tokenEntity.getToken()));
        assertThat(entity.isActive(), is(tokenEntity.isActive()));
    }

    @Test
    public void createToken_shouldReturnBadRequest_whenInvalidGatewayAccount() {
        when(mockedTokenService.createToken(GATEWAY_EXTERNAL_ID, REDIRECT_URI)).thenReturn(Optional.empty());
        thrown.expect(BadRequestException.class);
        thrown.expectMessage("There is no gateway account with external id [some-test-id]");
        thrown.reportMissingExceptionWithMessage("BadRequestException expected");
        resource.createGoCardlessPartnerAppConnectTokenState(REQUEST_MAP);
    }

    @Test
    public void createToken_shouldReturnBadRequest_whenMissingGatewayAccountFromRequest() {
        thrown.expect(BadRequestException.class);
        thrown.expectMessage("Field(s) missing: [gateway_account_id, redirect_uri]");
        thrown.reportMissingExceptionWithMessage("BadRequestException expected");
        resource.createGoCardlessPartnerAppConnectTokenState(new HashMap<>());
    }
}
