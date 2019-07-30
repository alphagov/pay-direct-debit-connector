package uk.gov.pay.directdebit.payments.services;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.pay.directdebit.gatewayaccounts.model.GoCardlessOrganisationId;
import uk.gov.pay.directdebit.mandate.fixtures.MandateFixture;
import uk.gov.pay.directdebit.payments.dao.PaymentDao;
import uk.gov.pay.directdebit.payments.exception.PaymentNotFoundException;
import uk.gov.pay.directdebit.payments.fixtures.GatewayAccountFixture;
import uk.gov.pay.directdebit.payments.fixtures.PaymentFixture;
import uk.gov.pay.directdebit.payments.model.GoCardlessPaymentId;
import uk.gov.pay.directdebit.payments.model.Payment;
import uk.gov.pay.directdebit.payments.model.SandboxPaymentId;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.ignoreStubs;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.pay.directdebit.gatewayaccounts.model.PaymentProvider.GOCARDLESS;
import static uk.gov.pay.directdebit.gatewayaccounts.model.PaymentProvider.SANDBOX;

@RunWith(MockitoJUnitRunner.class)
public class PaymentQueryServiceTest {

    private static final SandboxPaymentId SANDBOX_PAYMENT_ID = SandboxPaymentId.valueOf("Sandy");
    private static final GoCardlessPaymentId GOCARDLESS_PAYMENT_ID = GoCardlessPaymentId.valueOf("PM123");
    private static final GoCardlessOrganisationId GOCARDLESS_ORGANISATION_ID = GoCardlessOrganisationId.valueOf("OR123");

    @Mock
    private PaymentDao mockPaymentDao;

    @Mock
    private Payment mockPayment;

    @InjectMocks
    private PaymentQueryService paymentQueryService;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private GatewayAccountFixture gatewayAccountFixture = GatewayAccountFixture.aGatewayAccountFixture();
    private MandateFixture mandateFixture = MandateFixture.aMandateFixture().withGatewayAccountFixture(gatewayAccountFixture);
    private PaymentFixture paymentFixture = PaymentFixture.aPaymentFixture().withMandateFixture(mandateFixture);

    @Test
    public void findByProviderIdWithSandboxPaymentIdReturnsPayment() {
        given(mockPaymentDao.findPaymentByProviderId(SANDBOX, SANDBOX_PAYMENT_ID)).willReturn(Optional.of(mockPayment));

        Optional<Payment> result = paymentQueryService.findBySandboxPaymentId(SANDBOX_PAYMENT_ID);

        assertThat(result, is(Optional.of(mockPayment)));

        verifyZeroInteractions(ignoreStubs(mockPaymentDao));
    }

    @Test
    public void findByProviderIdWithSandboxPaymentIdForNonExistentPaymentReturnsEmpty() {
        given(mockPaymentDao.findPaymentByProviderId(SANDBOX, SANDBOX_PAYMENT_ID)).willReturn(Optional.empty());

        Optional<Payment> result = paymentQueryService.findBySandboxPaymentId(SANDBOX_PAYMENT_ID);

        assertThat(result, is(Optional.empty()));
    }

    @Test
    public void findByProviderIdWithGoCardlessPaymentIdAndOrganisationIdReturnsPayment() {
        given(mockPaymentDao.findPaymentByProviderIdAndOrganisationId(GOCARDLESS, GOCARDLESS_PAYMENT_ID, GOCARDLESS_ORGANISATION_ID))
                .willReturn(Optional.of(mockPayment));

        Optional<Payment> result = paymentQueryService.findByGoCardlessPaymentIdAndOrganisationId(GOCARDLESS_PAYMENT_ID, GOCARDLESS_ORGANISATION_ID);

        assertThat(result, is(Optional.of(mockPayment)));

        verifyZeroInteractions(ignoreStubs(mockPaymentDao));
    }

    @Test
    public void findByProviderIdWithGoCardlessPaymentIdAndOrganisationIdForNonExistentPaymentReturnsEmpty() {
        given(mockPaymentDao.findPaymentByProviderIdAndOrganisationId(GOCARDLESS, GOCARDLESS_PAYMENT_ID, GOCARDLESS_ORGANISATION_ID))
                .willReturn(Optional.empty());

        Optional<Payment> result = paymentQueryService.findByGoCardlessPaymentIdAndOrganisationId(GOCARDLESS_PAYMENT_ID, GOCARDLESS_ORGANISATION_ID);

        assertThat(result, is(Optional.empty()));
    }

    @Test
    public void findByTransactionExternalIdAndAccountId_shouldFindATransaction() {
        when(mockPaymentDao.findByExternalId(paymentFixture.getExternalId()))
                .thenReturn(Optional.of(paymentFixture.toEntity()));
        Payment foundPayment = paymentQueryService.findPaymentForExternalId(paymentFixture.getExternalId());
        assertThat(foundPayment.getId(), is(notNullValue()));
        assertThat(foundPayment.getExternalId(), is(paymentFixture.getExternalId()));
        assertThat(foundPayment.getMandate(), is(mandateFixture.toEntity()));
        assertThat(foundPayment.getState(), is(paymentFixture.getState()));
        assertThat(foundPayment.getAmount(), is(paymentFixture.getAmount()));
        assertThat(foundPayment.getDescription(), is(paymentFixture.getDescription()));
        assertThat(foundPayment.getReference(), is(paymentFixture.getReference()));
        assertThat(foundPayment.getCreatedDate(), is(paymentFixture.getCreatedDate()));
    }

    @Test
    public void findChargeForExternalIdAndGatewayAccountId_shouldThrow_ifNoTransactionExistsWithExternalId() {
        thrown.expect(PaymentNotFoundException.class);
        thrown.expectMessage("No payment found with external id not-existing");
        paymentQueryService.findPaymentForExternalId("not-existing");
    }

}
