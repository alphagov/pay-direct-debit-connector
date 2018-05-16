package uk.gov.pay.directdebit.payments.clients;

import com.gocardless.resources.BankDetailsLookup;
import com.gocardless.resources.Customer;
import com.gocardless.resources.CustomerBankAccount;
import com.gocardless.resources.Payment;
import uk.gov.pay.directdebit.mandate.model.GoCardlessMandate;
import uk.gov.pay.directdebit.mandate.model.GoCardlessPayment;
import uk.gov.pay.directdebit.mandate.model.Mandate;
import uk.gov.pay.directdebit.payers.model.BankAccountDetails;
import uk.gov.pay.directdebit.payers.model.GoCardlessBankAccountLookup;
import uk.gov.pay.directdebit.payers.model.GoCardlessCustomer;
import uk.gov.pay.directdebit.payers.model.Payer;
import uk.gov.pay.directdebit.payments.model.Transaction;

import javax.inject.Inject;
import java.time.LocalDate;

import static com.gocardless.resources.BankDetailsLookup.AvailableDebitScheme.BACS;

public class GoCardlessClientFacade {

    private final GoCardlessClientWrapper goCardlessClientWrapper;

    @Inject
    public GoCardlessClientFacade(GoCardlessClientWrapper goCardlessClientWrapper) {
        this.goCardlessClientWrapper = goCardlessClientWrapper;
    }

    public GoCardlessCustomer createCustomer(String paymentRequestExternalId, Payer payer) {
        Customer customer = goCardlessClientWrapper.createCustomer(paymentRequestExternalId, payer);
        return new GoCardlessCustomer(
                payer.getId(),
                customer.getId());
    }

    public GoCardlessCustomer createCustomerBankAccount(String paymentRequestExternalId, GoCardlessCustomer customer,
                                                        String accountHolderName, String sortCode, String accountNumber) {
        CustomerBankAccount gcCustomerBankAccount = goCardlessClientWrapper.createCustomerBankAccount(paymentRequestExternalId, customer,
                accountHolderName, sortCode, accountNumber);
        customer.setCustomerBankAccountId(gcCustomerBankAccount.getId());
        return customer;
    }

    public GoCardlessMandate createMandate(String paymentRequestExternalId, Mandate mandate, GoCardlessCustomer customer) {
        com.gocardless.resources.Mandate gcMandate = goCardlessClientWrapper.createMandate(paymentRequestExternalId, customer);
        return new GoCardlessMandate(
                mandate.getId(),
                gcMandate.getId());
    }

    public GoCardlessPayment createPayment(String paymentRequestExternalId, GoCardlessMandate mandate, Transaction transaction) {
        Payment gcPayment = goCardlessClientWrapper.createPayment(paymentRequestExternalId, mandate, transaction);
        return new GoCardlessPayment(
                transaction.getId(),
                gcPayment.getId(),
                LocalDate.parse(gcPayment.getChargeDate()));
    }

    public GoCardlessBankAccountLookup validate(BankAccountDetails bankAccountDetails) {
        BankDetailsLookup gcBankDetailsLookup = goCardlessClientWrapper.validate(bankAccountDetails);
        return new GoCardlessBankAccountLookup(
                gcBankDetailsLookup.getBankName(),
                gcBankDetailsLookup.getAvailableDebitSchemes().contains(BACS));
    }

}
