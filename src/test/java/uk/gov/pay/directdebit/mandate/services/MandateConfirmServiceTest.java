package uk.gov.pay.directdebit.mandate.services;

import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.pay.directdebit.mandate.fixtures.MandateFixture;
import uk.gov.pay.directdebit.mandate.model.ConfirmationDetails;
import uk.gov.pay.directdebit.mandate.model.Mandate;
import uk.gov.pay.directdebit.payments.dao.TransactionDao;
import uk.gov.pay.directdebit.payments.exception.ChargeNotFoundException;
import uk.gov.pay.directdebit.payments.fixtures.TransactionFixture;
import uk.gov.pay.directdebit.payments.model.Transaction;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MandateConfirmServiceTest {

    private MandateConfirmService service;

    @Mock
    private MandateService mockMandateService;

    @Mock
    private TransactionDao mockTransactionDao;
    
    @Rule
    public ExpectedException thrown = ExpectedException.none();
    
    @Before
    public void setup() {
        service = new MandateConfirmService(mockMandateService, mockTransactionDao);
    }
    
    @Test
    public void confirm_shouldConfirmAPaymentByRegisteringExpectedEvents_whenNoTransactionIsSupplied() {
        ImmutableMap<String, String> confirmMandateRequest = ImmutableMap
                .of("sort_code", "123456", "account_number", "12345678");
        
        String mandateExternalId = "test-mandate-ext-id";
        Mandate mandate = MandateFixture.aMandateFixture().toEntity();
        when(mockMandateService.confirmedDirectDebitDetailsFor(mandateExternalId))
                .thenReturn(mandate);
        ConfirmationDetails confirmationDetails = service.confirm(mandateExternalId, confirmMandateRequest);
        assertThat(confirmationDetails.getMandate(), is(mandate));
        assertThat(confirmationDetails.getSortCode(), is("123456"));
        assertThat(confirmationDetails.getAccountNumber(), is("12345678"));
        assertThat(confirmationDetails.getTransaction(), is(nullValue()));
    }

    @Test
    public void confirm_shouldConfirmAPaymentByRegisteringExpectedEvents_whenATransactionIsSuppliedAndExists() {
        String transactionExternaId = "usdfhkdhfksd";
        ImmutableMap<String, String> confirmMandateRequest = ImmutableMap
                .of("sort_code", "123456", "account_number", "12345678", "transaction_external_id", transactionExternaId);

        String mandateExternalId = "test-mandate-ext-id";
        Mandate mandate = MandateFixture.aMandateFixture().toEntity();
        Transaction transaction = TransactionFixture.aTransactionFixture().toEntity();
        when(mockTransactionDao.findByExternalId(transactionExternaId)).thenReturn(Optional.of(transaction));
        when(mockMandateService.confirmedDirectDebitDetailsFor(mandateExternalId))
                .thenReturn(mandate);
        ConfirmationDetails confirmationDetails = service.confirm(mandateExternalId, confirmMandateRequest);
        assertThat(confirmationDetails.getMandate(), is(mandate));
        assertThat(confirmationDetails.getSortCode(), is("123456"));
        assertThat(confirmationDetails.getAccountNumber(), is("12345678"));
        assertThat(confirmationDetails.getTransaction(), is(transaction));
    }

    @Test
    public void confirm_shouldThrow_whenATransactionIsSuppliedButDoesNotExist() {
        String transactionExternaId = "usdfhkdhfksd";
        ImmutableMap<String, String> confirmMandateRequest = ImmutableMap
                .of("sort_code", "123456", "account_number", "12345678", "transaction_external_id", transactionExternaId);

        String mandateExternalId = "test-mandate-ext-id";
        MandateFixture mandateFixture = MandateFixture.aMandateFixture().withExternalId(mandateExternalId);
        when(mockTransactionDao.findByExternalId(transactionExternaId)).thenReturn(Optional.empty());
        when(mockMandateService.confirmedDirectDebitDetailsFor(mandateExternalId))
                .thenReturn(mandateFixture.toEntity());

        thrown.expect(ChargeNotFoundException.class);
        thrown.expectMessage("No charges found for mandate id " + mandateExternalId + ", transaction id " + transactionExternaId);
        thrown.reportMissingExceptionWithMessage("ChargeNotFoundException expected");
        service.confirm(mandateExternalId, confirmMandateRequest);
    }
    
}
