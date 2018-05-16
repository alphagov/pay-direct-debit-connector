package uk.gov.pay.directdebit.payments.clients;


import com.gocardless.resources.BankDetailsLookup;
import com.gocardless.resources.Customer;
import com.gocardless.resources.CustomerBankAccount;
import com.gocardless.resources.Mandate;
import com.gocardless.resources.Payment;
import com.gocardless.services.PaymentService;
import uk.gov.pay.directdebit.mandate.model.GoCardlessMandate;
import uk.gov.pay.directdebit.payers.model.BankAccountDetails;
import uk.gov.pay.directdebit.payers.model.GoCardlessCustomer;
import uk.gov.pay.directdebit.payers.model.Payer;
import uk.gov.pay.directdebit.payments.model.Transaction;

//thin abstraction over the client provided in the SDK
public class GoCardlessClientWrapper {

    private com.gocardless.GoCardlessClient goCardlessClient;

    public GoCardlessClientWrapper(com.gocardless.GoCardlessClient goCardlessClient) {
        this.goCardlessClient = goCardlessClient;
    }

    public Customer createCustomer(String paymentRequestExternalId, Payer payer) {
        return goCardlessClient.customers()
                .create()
                .withEmail(payer.getEmail())
                .withGivenName(payer.getName())
                .withFamilyName(payer.getName())
                .withIdempotencyKey(paymentRequestExternalId)
                .execute();
    }

    public CustomerBankAccount createCustomerBankAccount(String paymentRequestExternalId, GoCardlessCustomer customer,
                                                         String accountHolderName, String sortCode, String accountNumber){
        return goCardlessClient.customerBankAccounts()
                .create()
                .withAccountHolderName(accountHolderName)
                .withAccountNumber(accountNumber)
                .withBranchCode(sortCode)
                .withCountryCode("GB")
                .withLinksCustomer(customer.getCustomerId())
                .withIdempotencyKey(paymentRequestExternalId)
                .execute();
    }

    public Mandate createMandate(String paymentRequestExternalId, GoCardlessCustomer customer) {
        return goCardlessClient.mandates()
                .create()
                .withLinksCustomerBankAccount(customer.getCustomerBankAccountId())
                .withIdempotencyKey(paymentRequestExternalId)
                .execute();
    }

    public Payment createPayment(String paymentRequestExternalId, GoCardlessMandate mandate, Transaction transaction) {
        return goCardlessClient.payments()
                .create()
                .withAmount(Math.toIntExact(transaction.getAmount()))
                .withCurrency(PaymentService.PaymentCreateRequest.Currency.GBP)
                .withLinksMandate(mandate.getGoCardlessMandateId())
                .withIdempotencyKey(paymentRequestExternalId)
                .execute();
    }

    public BankDetailsLookup validate(BankAccountDetails bankAccountDetails) {
        return goCardlessClient.bankDetailsLookups().create()
                .withAccountNumber(bankAccountDetails.getAccountNumber())
                .withBranchCode(bankAccountDetails.getSortCode())
                .withCountryCode("GB")
                .execute();
    }
}
