package uk.gov.pay.directdebit.payments.services;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.pay.directdebit.mandate.model.ConfirmationDetails;
import uk.gov.pay.directdebit.mandate.model.Mandate;
import uk.gov.pay.directdebit.mandate.model.MandateType;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class GoCardlessServiceOnDemandTest extends GoCardlessServiceTest {

//    @Before
//    public void setUp() {
//        mandateFixture.withMandateType(MandateType.ON_DEMAND);
//        confirmationDetails = new ConfirmationDetails(mandateFixture.toEntity(), transaction, ACCOUNT_NUMBER, SORT_CODE);
//        service = new GoCardlessService(mockedPayerService, mockedTransactionService,
//                mockedMandateService, mockedGoCardlessClientFacade, mockedGoCardlessCustomerDao, mockedGoCardlessPaymentDao,
//                mockedGoCardlessMandateDao, mockedGoCardlessEventDao, mockedMandateDao, mockedBankAccountDetailsParser);
//
//        when(mockedGoCardlessClientFacade.createCustomer(MANDATE_ID, payerFixture.toEntity())).thenReturn(goCardlessCustomer);
//        when(mockedGoCardlessClientFacade.createCustomerBankAccount(MANDATE_ID, goCardlessCustomer, payerFixture.getName(), SORT_CODE, ACCOUNT_NUMBER)).thenReturn(goCardlessCustomer);
//        when(mockedMandateService.confirm(MANDATE_ID, confirmDetails)).thenReturn(confirmationDetails);
//        when(mockedGoCardlessClientFacade.createMandate(mandateFixture.toEntity(), goCardlessCustomer)).thenReturn(goCardlessMandate);
//    }
//
//    @Test
//    public void confirm_shouldSendAnEmailWhenOnDemandMandateCreated() {
//        service.confirm(MANDATE_ID, gatewayAccountFixture.toEntity(), confirmDetails);
//        Mandate onDemandMandate = mandateFixture.toEntity();
//        verify(mockedTransactionService).onDemandMandateConfirmedFor(onDemandMandate);
//    }
}
