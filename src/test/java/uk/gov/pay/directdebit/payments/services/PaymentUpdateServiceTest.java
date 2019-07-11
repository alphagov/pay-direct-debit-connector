package uk.gov.pay.directdebit.payments.services;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.pay.directdebit.gatewayaccounts.model.GoCardlessOrganisationId;
import uk.gov.pay.directdebit.payments.dao.PaymentDao;
import uk.gov.pay.directdebit.payments.model.GoCardlessPaymentId;
import uk.gov.pay.directdebit.payments.model.GoCardlessPaymentIdAndOrganisationId;
import uk.gov.pay.directdebit.payments.model.PaymentLookupKey;
import uk.gov.pay.directdebit.payments.model.SandboxPaymentId;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.ignoreStubs;
import static org.mockito.Mockito.verifyZeroInteractions;
import static uk.gov.pay.directdebit.gatewayaccounts.model.PaymentProvider.GOCARDLESS;
import static uk.gov.pay.directdebit.gatewayaccounts.model.PaymentProvider.SANDBOX;
import static uk.gov.pay.directdebit.payments.model.PaymentState.PENDING;

@RunWith(MockitoJUnitRunner.class)
public class PaymentUpdateServiceTest {

    private static final SandboxPaymentId SANDBOX_MANDATE_ID = SandboxPaymentId.valueOf("Sandy");
    private static final GoCardlessPaymentId GOCARDLESS_PAYMENT_ID = GoCardlessPaymentId.valueOf("PM123");
    private static final GoCardlessOrganisationId GOCARDLESS_ORGANISATION_ID = GoCardlessOrganisationId.valueOf("OR123");
    private static final GoCardlessPaymentIdAndOrganisationId GOCARDLESS_MANDATE_ID_AND_ORGANISATION_ID =
            new GoCardlessPaymentIdAndOrganisationId(GOCARDLESS_PAYMENT_ID, GOCARDLESS_ORGANISATION_ID);

    @Mock
    private PaymentDao mockPaymentDao;

    private PaymentUpdateService paymentUpdateService;

    @Before
    public void setUp() {
        paymentUpdateService = new PaymentUpdateService(mockPaymentDao);
    }

    @Test
    public void updateStateByPaymentProviderMandateIdWithSandboxMandateIdReturnsUpdateCount() {
        given(mockPaymentDao.updateStateByProviderId(SANDBOX, SANDBOX_MANDATE_ID, PENDING)).willReturn(1);

        int updated = paymentUpdateService.updateStateByProviderId(SANDBOX, SANDBOX_MANDATE_ID, PENDING);

        assertThat(updated, is(1));

        verifyZeroInteractions(ignoreStubs(mockPaymentDao));
    }

    @Test
    public void updateStateByPaymentProviderMandateIdWithGoCardlessMandateIdAndOrganisationIdReturnsUpdateCount() {
        given(mockPaymentDao.updateStateByProviderIdAndOrganisationId(GOCARDLESS, GOCARDLESS_ORGANISATION_ID, GOCARDLESS_PAYMENT_ID, PENDING)).willReturn(1);

        int updated = paymentUpdateService.updateStateByProviderId(GOCARDLESS, GOCARDLESS_MANDATE_ID_AND_ORGANISATION_ID, PENDING);

        assertThat(updated, is(1));

        verifyZeroInteractions(ignoreStubs(mockPaymentDao));
    }

    @Test(expected = IllegalArgumentException.class)
    public void updateStateByPaymentProviderMandateIdWithUnrecognisedTypeThrowsException() {

        paymentUpdateService.updateStateByProviderId(GOCARDLESS, new UnrecognisedPaymentLookupKeyImplementation(), PENDING);
    }

    private static class UnrecognisedPaymentLookupKeyImplementation implements PaymentLookupKey {

    }

}
