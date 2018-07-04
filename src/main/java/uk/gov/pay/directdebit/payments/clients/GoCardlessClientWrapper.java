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
import uk.gov.pay.directdebit.payers.model.SortCode;
import uk.gov.pay.directdebit.payments.model.Transaction;

//thin abstraction over the client provided in the SDK
public class GoCardlessClientWrapper {

    private com.gocardless.GoCardlessClient goCardlessClient;

    public GoCardlessClientWrapper(com.gocardless.GoCardlessClient goCardlessClient) {
        this.goCardlessClient = goCardlessClient;
    }

    public Customer createCustomer(String mandateExternalId, Payer payer) {
        return goCardlessClient.customers()
                .create()
                .withEmail(payer.getEmail())
                .withGivenName(payer.getName())
                .withFamilyName(payer.getName())
                .withIdempotencyKey(mandateExternalId)
                .execute();
    }

    public CustomerBankAccount createCustomerBankAccount(String mandateExternalId, GoCardlessCustomer customer,
                                                         String accountHolderName, SortCode sortCode, String accountNumber){
        return goCardlessClient.customerBankAccounts()
                .create()
                .withAccountHolderName(accountHolderName)
                .withAccountNumber(accountNumber)
                .withBranchCode(sortCode.toString())
                .withCountryCode("GB")
                .withLinksCustomer(customer.getCustomerId())
                .withIdempotencyKey(mandateExternalId)
                .execute();
    }

    public Mandate createMandate(String mandateExternalId, GoCardlessCustomer customer) {
        return goCardlessClient.mandates()
                .create()
                .withLinksCustomerBankAccount(customer.getCustomerBankAccountId())
                .withIdempotencyKey(mandateExternalId)
                .execute();
    }

    public Payment createPayment(Transaction transaction, GoCardlessMandate mandate) {
        return goCardlessClient.payments()
                .create()
                .withAmount(Math.toIntExact(transaction.getAmount()))
                .withCurrency(PaymentService.PaymentCreateRequest.Currency.GBP)
                .withLinksMandate(mandate.getGoCardlessMandateId())
                .withIdempotencyKey(transaction.getExternalId())
                .execute();
    }

    public BankDetailsLookup validate(BankAccountDetails bankAccountDetails) {
        return goCardlessClient.bankDetailsLookups().create()
                .withAccountNumber(bankAccountDetails.getAccountNumber())
                .withBranchCode(bankAccountDetails.getSortCode().toString())
                .withCountryCode("GB")
                .execute();
    }
}
