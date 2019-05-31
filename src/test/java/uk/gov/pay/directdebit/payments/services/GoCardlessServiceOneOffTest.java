package uk.gov.pay.directdebit.payments.services;

import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.pay.directdebit.mandate.model.Mandate;
import uk.gov.pay.directdebit.mandate.model.OneOffConfirmationDetails;
import uk.gov.pay.directdebit.mandate.services.gocardless.GoCardlessService;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class GoCardlessServiceOneOffTest extends GoCardlessServiceTest {
    
    @Before
    public void setUp() {
        service = new GoCardlessService(mockedGoCardlessClientFactory, mockedGoCardlessCustomerDao, mockedGoCardlessPaymentDao, mockedGoCardlessMandateDao);
        when(mockedGoCardlessClientFactory.getClientFor(Optional.of(gatewayAccountFixture.getAccessToken()))).thenReturn(mockedGoCardlessClientFacade);
        when(mockedGoCardlessClientFacade.createCustomer(MANDATE_ID, payerFixture.toEntity())).thenReturn(goCardlessCustomer);
        when(mockedGoCardlessClientFacade.createCustomerBankAccount(MANDATE_ID, goCardlessCustomer, payerFixture.getName(), SORT_CODE, ACCOUNT_NUMBER)).thenReturn(goCardlessCustomer);
    }

    @Test
    public void confirm_shouldStoreAGoCardlessCustomerBankAccountMandateAndPaymentWhenReceivingConfirmTransaction() {
        Mandate mandate = mandateFixture.toEntity();
        when(mockedGoCardlessClientFacade.createMandate(mandate, goCardlessCustomer)).thenReturn(goCardlessMandate);
        when(mockedGoCardlessClientFacade.createPayment(transaction, goCardlessMandate)).thenReturn(goCardlessPayment);

        OneOffConfirmationDetails oneOffConfirmationDetails = service
                .confirmOneOffMandate(mandate, bankAccountDetails, transaction);
        verify(mockedGoCardlessCustomerDao).insert(goCardlessCustomer);
        verify(mockedGoCardlessMandateDao).insert(goCardlessMandate);
        verify(mockedGoCardlessPaymentDao).insert(goCardlessPayment);
        InOrder orderedCalls = inOrder(mockedGoCardlessClientFacade);

        orderedCalls.verify(mockedGoCardlessClientFacade).createCustomer(MANDATE_ID, payerFixture.toEntity());
        orderedCalls.verify(mockedGoCardlessClientFacade).createCustomerBankAccount(MANDATE_ID, goCardlessCustomer, payerFixture.getName(), SORT_CODE, ACCOUNT_NUMBER);
        orderedCalls.verify(mockedGoCardlessClientFacade).createMandate(mandate, goCardlessCustomer);
        orderedCalls.verify(mockedGoCardlessClientFacade).createPayment(transaction, goCardlessMandate);
       
        assertThat(oneOffConfirmationDetails.getMandate().getMandateReference(), is(goCardlessMandate.getGoCardlessReference()));
        assertThat(oneOffConfirmationDetails.getChargeDate(), is(goCardlessPayment.getChargeDate()));
    }
    
    @Test
    public void confirm_shouldThrow_ifFailingToCreateCustomerInGoCardless() {
        verifyCreateCustomerFailedException();
        
        service.confirmOneOffMandate(mandateFixture.toEntity(), bankAccountDetails, transaction);
    }

    @Test
    public void confirm_shouldThrow_ifFailingToCreateCustomerBankAccountInGoCardless() {
        verifyCreateCustomerBankAccountFailedException();
        
        service.confirmOneOffMandate(mandateFixture.toEntity(), bankAccountDetails, transaction);
    }

    @Test
    public void confirm_shouldThrow_ifFailingToCreatePaymentInGoCardless() {
        verifyCreatePaymentFailedException();

        service.confirmOneOffMandate(mandateFixture.toEntity(), bankAccountDetails, transaction);
    }

    @Test
    public void confirm_shouldThrow_ifFailingToCreateMandateInGoCardless() {
        verifyMandateFailedException();
        
        service.confirmOneOffMandate(mandateFixture.toEntity(), bankAccountDetails, transaction);
    }

    public void collect_shouldThrow() {
        service.collect(mandateFixture.toEntity(), transaction);
        
        verifyNoMoreInteractions(mockedGoCardlessMandateDao);
        verifyNoMoreInteractions(mockedGoCardlessClientFacade);
        verifyNoMoreInteractions(mockedGoCardlessPaymentDao);
    }
}
