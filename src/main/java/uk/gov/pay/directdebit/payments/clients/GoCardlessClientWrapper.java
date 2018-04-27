package uk.gov.pay.directdebit.payments.clients;


import com.gocardless.resources.Customer;
import com.gocardless.resources.CustomerBankAccount;
import com.gocardless.resources.Payment;
import com.gocardless.services.PaymentService;
import com.google.inject.Inject;
import uk.gov.pay.directdebit.mandate.model.GoCardlessMandate;
import uk.gov.pay.directdebit.mandate.model.GoCardlessPayment;
import uk.gov.pay.directdebit.mandate.model.Mandate;
import uk.gov.pay.directdebit.payers.model.GoCardlessCustomer;
import uk.gov.pay.directdebit.payers.model.Payer;
import uk.gov.pay.directdebit.payments.model.Transaction;

import java.time.LocalDate;

//thin abstraction over the client provided in the SDK
public class GoCardlessClientWrapper {

    private com.gocardless.GoCardlessClient goCardlessClient;

    @Inject
    public GoCardlessClientWrapper(com.gocardless.GoCardlessClient goCardlessClient) {
        this.goCardlessClient = goCardlessClient;
    }

    public GoCardlessCustomer createCustomer(String paymentRequestExternalId, Payer payer) {
        Customer customer = goCardlessClient.customers()
                .create()
                .withEmail(payer.getEmail())
                .withGivenName(payer.getName())
                .withFamilyName(payer.getName())
                .withIdempotencyKey(paymentRequestExternalId)
                .execute();
        return new GoCardlessCustomer(
                payer.getId(),
                customer.getId());
    }

    public GoCardlessCustomer createCustomerBankAccount(String paymentRequestExternalId, GoCardlessCustomer customer, String accountHolderName, String sortCode, String accountNumber){
        CustomerBankAccount goCardlessCustomerBankAccount = goCardlessClient.customerBankAccounts()
                .create()
                .withAccountHolderName(accountHolderName)
                .withAccountNumber(accountNumber)
                .withBranchCode(sortCode)
                .withCountryCode("GB")
                .withLinksCustomer(customer.getCustomerId())
                .withIdempotencyKey(paymentRequestExternalId)
                .execute();
        customer.setCustomerBankAccountId(goCardlessCustomerBankAccount.getId());
        return customer;
    }

    public GoCardlessMandate createMandate(String paymentRequestExternalId, Mandate mandate, GoCardlessCustomer customer) {
        com.gocardless.resources.Mandate goCardlessMandate = goCardlessClient.mandates()
                .create()
                .withLinksCustomerBankAccount(customer.getCustomerBankAccountId())
                .withIdempotencyKey(paymentRequestExternalId)
                .execute();
        return new GoCardlessMandate(
                mandate.getId(),
                goCardlessMandate.getId());
    }

    public GoCardlessPayment createPayment(String paymentRequestExternalId, GoCardlessMandate mandate, Transaction transaction) {
        Payment goCardlessPayment = goCardlessClient.payments()
                .create()
                .withAmount(Math.toIntExact(transaction.getAmount()))
                .withCurrency(PaymentService.PaymentCreateRequest.Currency.GBP)
                .withLinksMandate(mandate.getGoCardlessMandateId())
                .withIdempotencyKey(paymentRequestExternalId)
                .execute();
        return new GoCardlessPayment(
                transaction.getId(),
                goCardlessPayment.getId(),
                LocalDate.parse(goCardlessPayment.getChargeDate()));
    }
}
