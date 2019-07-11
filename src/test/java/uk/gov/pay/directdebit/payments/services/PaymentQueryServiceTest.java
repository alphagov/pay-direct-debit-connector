package uk.gov.pay.directdebit.payments.services;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.pay.directdebit.gatewayaccounts.model.GoCardlessOrganisationId;
import uk.gov.pay.directdebit.payments.dao.PaymentDao;
import uk.gov.pay.directdebit.payments.exception.PaymentNotFoundException;
import uk.gov.pay.directdebit.payments.model.GoCardlessPaymentId;
import uk.gov.pay.directdebit.payments.model.GoCardlessPaymentIdAndOrganisationId;
import uk.gov.pay.directdebit.payments.model.Payment;
import uk.gov.pay.directdebit.payments.model.PaymentLookupKey;
import uk.gov.pay.directdebit.payments.model.SandboxPaymentId;

import java.util.Optional;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.ignoreStubs;
import static org.mockito.Mockito.verifyZeroInteractions;
import static uk.gov.pay.directdebit.gatewayaccounts.model.PaymentProvider.GOCARDLESS;
import static uk.gov.pay.directdebit.gatewayaccounts.model.PaymentProvider.SANDBOX;

@RunWith(MockitoJUnitRunner.class)
public class PaymentQueryServiceTest {

    private static final SandboxPaymentId SANDBOX_PAYMENT_ID = SandboxPaymentId.valueOf("Sandy");
    private static final GoCardlessPaymentId GOCARDLESS_PAYMENT_ID = GoCardlessPaymentId.valueOf("PM123");
    private static final GoCardlessOrganisationId GOCARDLESS_ORGANISATION_ID = GoCardlessOrganisationId.valueOf("OR123");
    private static final GoCardlessPaymentIdAndOrganisationId GOCARDLESS_PAYMENT_ID_AND_ORGANISATION_ID =
            new GoCardlessPaymentIdAndOrganisationId(GOCARDLESS_PAYMENT_ID, GOCARDLESS_ORGANISATION_ID);

    @Mock
    private PaymentDao mockPaymentDao;

    @Mock
    private Payment mockPayment;

    private PaymentQueryService paymentQueryService;

    @Before
    public void setUp() {
        paymentQueryService = new PaymentQueryService(mockPaymentDao);
    }

    @Test
    public void findByProviderIdWithSandboxPaymentIdReturnsPayment() {
        given(mockPaymentDao.findPaymentByProviderId(SANDBOX, SANDBOX_PAYMENT_ID)).willReturn(Optional.of(mockPayment));

        Payment result = paymentQueryService.findByProviderPaymentId(SANDBOX, SANDBOX_PAYMENT_ID);

        assertThat(result, is(mockPayment));

        verifyZeroInteractions(ignoreStubs(mockPaymentDao));
    }

    @Test(expected = PaymentNotFoundException.class)
    public void findByProviderIdWithSandboxPaymentIdForNonExistentPaymentThrowsException() {
        given(mockPaymentDao.findPaymentByProviderId(SANDBOX, SANDBOX_PAYMENT_ID)).willReturn(Optional.empty());

        paymentQueryService.findByProviderPaymentId(SANDBOX, SANDBOX_PAYMENT_ID);
    }

    @Test
    public void findByProviderIdWithGoCardlessPaymentIdAndOrganisationIdReturnsPayment() {
        given(mockPaymentDao.findPaymentByProviderIdAndOrganisationId(GOCARDLESS, GOCARDLESS_PAYMENT_ID, GOCARDLESS_ORGANISATION_ID))
                .willReturn(Optional.of(mockPayment));

        Payment result = paymentQueryService.findByProviderPaymentId(GOCARDLESS, GOCARDLESS_PAYMENT_ID_AND_ORGANISATION_ID);

        assertThat(result, is(mockPayment));

        verifyZeroInteractions(ignoreStubs(mockPaymentDao));
    }

    @Test(expected = PaymentNotFoundException.class)
    public void findByProviderIdWithGoCardlessPaymentIdAndOrganisationIdForNonExistentPaymentThrowsException() {
        given(mockPaymentDao.findPaymentByProviderIdAndOrganisationId(GOCARDLESS, GOCARDLESS_PAYMENT_ID, GOCARDLESS_ORGANISATION_ID))
                .willReturn(Optional.empty());

        paymentQueryService.findByProviderPaymentId(GOCARDLESS, GOCARDLESS_PAYMENT_ID_AND_ORGANISATION_ID);
    }

    @Test(expected = IllegalArgumentException.class)
    public void findByPaymentProviderMandateIdWithUnrecognisedTypeThrowsException() {
        paymentQueryService.findByProviderPaymentId(GOCARDLESS, new UnrecognisedPaymentLookupKeyImplementation());
    }

    private static class UnrecognisedPaymentLookupKeyImplementation implements PaymentLookupKey {

    }

}
