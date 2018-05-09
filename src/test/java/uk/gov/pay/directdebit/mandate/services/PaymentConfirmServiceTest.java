package uk.gov.pay.directdebit.mandate.services;

import com.google.common.collect.ImmutableMap;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.pay.directdebit.common.exception.validation.MissingMandatoryFieldsException;
import uk.gov.pay.directdebit.mandate.dao.MandateDao;
import uk.gov.pay.directdebit.mandate.exception.PayerConflictException;
import uk.gov.pay.directdebit.mandate.model.ConfirmationDetails;
import uk.gov.pay.directdebit.mandate.model.Mandate;
import uk.gov.pay.directdebit.mandate.model.MandateState;
import uk.gov.pay.directdebit.payers.api.CreatePayerValidator;
import uk.gov.pay.directdebit.payers.dao.PayerDao;
import uk.gov.pay.directdebit.payers.fixtures.PayerFixture;
import uk.gov.pay.directdebit.payments.model.Transaction;
import uk.gov.pay.directdebit.payments.services.TransactionService;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.pay.directdebit.payments.fixtures.TransactionFixture.aTransactionFixture;

@RunWith(MockitoJUnitRunner.class)
public class PaymentConfirmServiceTest {

    private PaymentConfirmService service;

    @Mock
    private MandateDao mockMandateDao;
    @Mock
    private TransactionService mockTransactionService;
    @Mock
    private PayerDao mockPayerDao;

    private Map<String, String> details = ImmutableMap
            .of("sort_code", "123456", "account_number", "12345678");

    @Rule
    public ExpectedException thrown = ExpectedException.none();
    
    @Before
    public void setup() {
        service = new PaymentConfirmService(mockTransactionService, mockPayerDao, mockMandateDao);
    }
    

    @Test
    public void confirm_shouldConfirmAPaymentByCreatingAMandateAndRegisteringExpectedEvents() {
        String paymentRequestExternalId = "test-payment-ext-id";
        long paymentRequestId = 1L;
        Long payerId = 2L;
        String accountExternalId = "account-external-id";
        Long mandateId = 3L;

        Transaction transaction = aTransactionFixture().withPaymentRequestId(paymentRequestId).toEntity();
        when(mockTransactionService.confirmedDirectDebitDetailsFor(accountExternalId, paymentRequestExternalId))
                .thenReturn(transaction);

        when(mockPayerDao.findByPaymentRequestId(paymentRequestId))
                .thenReturn(Optional.of(PayerFixture.aPayerFixture()
                        .withId(payerId).toEntity()));

        when(mockMandateDao.insert(any(Mandate.class))).thenReturn(mandateId);


        ConfirmationDetails confirmationDetails = service.confirm(accountExternalId, paymentRequestExternalId, details);
        ArgumentCaptor<Mandate> maCaptor = forClass(Mandate.class);
        verify(mockMandateDao).insert(maCaptor.capture());

        Mandate mandate = maCaptor.getValue();
        assertThat(mandate.getId(), is(mandateId));
        assertThat(mandate.getExternalId(), is(notNullValue()));
        assertThat(mandate.getPayerId(), is(payerId));
        assertThat(mandate.getReference(), is("TEMP_REFERENCE"));
        assertThat(mandate.getState(), is(MandateState.PENDING));

        assertThat(confirmationDetails.getMandate(), is(mandate));
        assertThat(confirmationDetails.getTransaction(), is(transaction));
        assertThat(confirmationDetails.getSortCode(), is("123456"));
        assertThat(confirmationDetails.getAccountNumber(), is("12345678"));
    }
    

    @Test
    public void confirm_shouldFail_whenPaymentDoesNotHaveAPayer() {

        String paymentRequestExternalId = "test-payment-ext-id";
        long paymentRequestId = 1L;
        String accountExternalId = "account-external-id";

        Transaction transaction = aTransactionFixture().withPaymentRequestId(paymentRequestId).toEntity();
        when(mockTransactionService.confirmedDirectDebitDetailsFor(accountExternalId, paymentRequestExternalId))
                .thenReturn(transaction);

        when(mockPayerDao.findByPaymentRequestId(paymentRequestId))
                .thenReturn(Optional.empty());

        try {
            service.confirm(accountExternalId, paymentRequestExternalId, details);
            fail("Expected PayerConflictException to be thrown");
        } catch (PayerConflictException e) {
            verify(mockMandateDao, never()).insert(any(Mandate.class));
        }
    }
}
