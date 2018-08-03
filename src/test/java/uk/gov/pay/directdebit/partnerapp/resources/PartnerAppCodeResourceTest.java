package uk.gov.pay.directdebit.partnerapp.resources;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.pay.directdebit.common.exception.BadRequestException;
import uk.gov.pay.directdebit.partnerapp.services.PartnerAppTokenService;

import java.util.HashMap;
import java.util.Map;

@RunWith(MockitoJUnitRunner.class)
public class PartnerAppCodeResourceTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Mock
    private PartnerAppTokenService mockedTokenService;

    private PartnerAppCodeResource resource;
    private static final String ACCESS_CODE = "some-access-code";
    private static final String PARTNER_STATE = "some-partner-state";
    private static final Map<String, String> REQUEST_MAP = new HashMap<>();

    @Before
    public void SetUp() {
        REQUEST_MAP.put("access_code", ACCESS_CODE);
        REQUEST_MAP.put("partner_state", PARTNER_STATE);
        resource = new PartnerAppCodeResource(mockedTokenService);
    }

    @Test
    public void shouldReturnBadRequest_whenMissingAccessCode() {
        thrown.expect(BadRequestException.class);
        thrown.expectMessage("Field(s) missing: [access_code]");
        thrown.reportMissingExceptionWithMessage("BadRequestException expected");
        REQUEST_MAP.remove("access_code");
        resource.createGoCardlessPartnerAppConnectTokenState(REQUEST_MAP);
    }

    @Test
    public void shouldReturnBadRequest_whenMissingPartnerState() {
        thrown.expect(BadRequestException.class);
        thrown.expectMessage("Field(s) missing: [partner_state]");
        thrown.reportMissingExceptionWithMessage("BadRequestException expected");
        REQUEST_MAP.remove("partner_state");
        resource.createGoCardlessPartnerAppConnectTokenState(REQUEST_MAP);
    }
}
