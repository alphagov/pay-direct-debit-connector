package uk.gov.pay.directdebit.payments.clients;


import com.gocardless.resources.Customer;
import com.gocardless.resources.CustomerBankAccount;
import uk.gov.pay.directdebit.payers.model.GoCardlessCustomer;
import uk.gov.pay.directdebit.payers.model.Payer;

//thin abstraction over the client provided in the SDK
public class GoCardlessClientWrapper {

    private com.gocardless.GoCardlessClient goCardlessClient;

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

    public GoCardlessCustomer createCustomerBankAccount(String paymentRequestExternalId, GoCardlessCustomer customer, String accountHolderName, String sortCode, String accountId){
        CustomerBankAccount goCardlessCustomerBankAccount = goCardlessClient.customerBankAccounts()
                .create()
                .withAccountHolderName(accountHolderName)
                .withAccountNumber(accountId)
                .withBranchCode(sortCode)
                .withCountryCode("GB")
                .withLinksCustomer(customer.getCustomerId())
                .withIdempotencyKey(paymentRequestExternalId)
                .execute();
        customer.setCustomerBankAccountId(goCardlessCustomerBankAccount.getId());
        return customer;
    }
}
