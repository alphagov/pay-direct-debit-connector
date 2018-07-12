package uk.gov.pay.directdebit.payments.services;

import java.time.LocalDate;
import java.util.Optional;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.pay.directdebit.mandate.model.Mandate;
import uk.gov.pay.directdebit.mandate.model.MandateType;
import uk.gov.pay.directdebit.mandate.services.gocardless.GoCardlessService;
import uk.gov.pay.directdebit.payments.exception.CreateCustomerBankAccountFailedException;
import uk.gov.pay.directdebit.payments.exception.GoCardlessMandateNotFoundException;

import static java.lang.String.format;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class GoCardlessServiceOnDemandTest extends GoCardlessServiceTest {

    @Before
    public void setUp() {
        mandateFixture.withMandateType(MandateType.ON_DEMAND);
        service = new GoCardlessService(mockedGoCardlessClientFacade, mockedGoCardlessCustomerDao, mockedGoCardlessPaymentDao, mockedGoCardlessMandateDao);

        when(mockedGoCardlessClientFacade.createCustomer(MANDATE_ID, payerFixture.toEntity())).thenReturn(goCardlessCustomer);
        when(mockedGoCardlessClientFacade.createCustomerBankAccount(MANDATE_ID, goCardlessCustomer, payerFixture.getName(), SORT_CODE, ACCOUNT_NUMBER)).thenReturn(goCardlessCustomer);
    }

    @Test
    public void confirm_shouldStoreAGoCardlessCustomerBankAccountMandateAndPaymentWhenReceivingConfirmTransaction() {
        Mandate mandate = mandateFixture.toEntity();

        when(mockedGoCardlessClientFacade.createMandate(mandate, goCardlessCustomer)).thenReturn(goCardlessMandate);

        Mandate confirmedMandate = service.confirmOnDemandMandate(mandate, bankAccountDetails);
        verify(mockedGoCardlessCustomerDao).insert(goCardlessCustomer);
        verify(mockedGoCardlessMandateDao).insert(goCardlessMandate);
        InOrder orderedCalls = inOrder(mockedGoCardlessClientFacade);

        orderedCalls.verify(mockedGoCardlessClientFacade).createCustomer(MANDATE_ID, payerFixture.toEntity());
        orderedCalls.verify(mockedGoCardlessClientFacade).createCustomerBankAccount(MANDATE_ID, goCardlessCustomer, payerFixture.getName(), SORT_CODE, ACCOUNT_NUMBER);
        orderedCalls.verify(mockedGoCardlessClientFacade).createMandate(mandate, goCardlessCustomer);

        Assert.assertThat(confirmedMandate.getMandateReference(), is(goCardlessMandate.getGoCardlessReference()));
    }

    @Test
    public void confirm_shouldThrow_ifFailingToCreateCustomerInGoCardless() {
        verifyCreateCustomerFailedException();

        service.confirmOnDemandMandate(mandateFixture.toEntity(), bankAccountDetails);
    }

    @Test
    public void confirm_shouldThrow_ifFailingToCreateCustomerBankAccountInGoCardless() {
        verifyCreateCustomerBankAccountFailedException();

        service.confirmOnDemandMandate(mandateFixture.toEntity(), bankAccountDetails);
    }

    @Test
    public void confirm_shouldThrow_ifFailingToCreateMandateInGoCardless() {
        verifyMandateFailedException();

        service.confirmOnDemandMandate(mandateFixture.toEntity(), bankAccountDetails);
    }

    @Test
    public void collect_shouldCreateAndStoreAPaymentForAValidGoCardlessMandate() {
        Mandate mandate = mandateFixture.toEntity();

        when(mockedGoCardlessMandateDao.findByMandateId(goCardlessMandate.getMandateId()))
                .thenReturn(Optional.of(goCardlessMandate));
        when(mockedGoCardlessClientFacade.createPayment(transaction, goCardlessMandate))
                .thenReturn(goCardlessPayment);

        LocalDate chargeDate = service.collect(mandate, transaction);
        verify(mockedGoCardlessPaymentDao).insert(goCardlessPayment);

        Assert.assertThat(chargeDate, is(goCardlessPayment.getChargeDate()));
    }
   
    @Test
    public void collect_shouldThrowIfTryingToCollectFromAnUnknownGoCardlessMandate() {
        Mandate mandate = mandateFixture.toEntity();

        when(mockedGoCardlessMandateDao.findByMandateId(goCardlessMandate.getMandateId()))
                .thenReturn(Optional.empty());

        thrown.expect(GoCardlessMandateNotFoundException.class);
        thrown.expectMessage(format("No gocardless mandate found with mandate id: %s", MANDATE_ID));
        thrown.reportMissingExceptionWithMessage("GoCardlessMandateNotFoundException expected");
        
        service.collect(mandate, transaction);
    }
    
}
