package uk.gov.pay.directdebit.common.clients;

import com.gocardless.resources.BankDetailsLookup;
import com.gocardless.resources.Creditor;
import com.gocardless.resources.Customer;
import com.gocardless.resources.CustomerBankAccount;
import com.gocardless.resources.Mandate;
import com.gocardless.services.PaymentService;
import uk.gov.pay.directdebit.mandate.model.GoCardlessMandateId;
import uk.gov.pay.directdebit.mandate.model.subtype.MandateExternalId;
import uk.gov.pay.directdebit.payers.model.AccountNumber;
import uk.gov.pay.directdebit.payers.model.BankAccountDetails;
import uk.gov.pay.directdebit.payers.model.GoCardlessCustomer;
import uk.gov.pay.directdebit.payers.model.Payer;
import uk.gov.pay.directdebit.payers.model.SortCode;
import uk.gov.pay.directdebit.payments.model.Payment;

//thin abstraction over the client provided in the SDK
public class GoCardlessClientWrapper {

    private com.gocardless.GoCardlessClient goCardlessClient;

    public GoCardlessClientWrapper(com.gocardless.GoCardlessClient goCardlessClient) {
        this.goCardlessClient = goCardlessClient;
    }

    public Customer createCustomer(MandateExternalId mandateExternalId, Payer payer) {
        return goCardlessClient.customers()
                .create()
                .withEmail(payer.getEmail())
                .withGivenName(payer.getName())
                .withFamilyName(payer.getName())
                .withIdempotencyKey(mandateExternalId.toString())
                .execute();
    }

    public CustomerBankAccount createCustomerBankAccount(MandateExternalId mandateExternalId, GoCardlessCustomer customer,
                                                         String accountHolderName, SortCode sortCode, AccountNumber accountNumber) {
        return goCardlessClient.customerBankAccounts()
                .create()
                .withAccountHolderName(accountHolderName)
                .withAccountNumber(accountNumber.toString())
                .withBranchCode(sortCode.toString())
                .withCountryCode("GB")
                .withLinksCustomer(customer.getCustomerId())
                .withIdempotencyKey(mandateExternalId.toString())
                .execute();
    }

    public Mandate createMandate(MandateExternalId mandateExternalId, GoCardlessCustomer customer) {
        return goCardlessClient.mandates()
                .create()
                .withLinksCustomerBankAccount(customer.getCustomerBankAccountId())
                .withIdempotencyKey(mandateExternalId.toString())
                .execute();
    }

    public com.gocardless.resources.Payment createPayment(Payment payment, GoCardlessMandateId goCardlessMandateId) {
        return goCardlessClient.payments()
                .create()
                .withAmount(Math.toIntExact(payment.getAmount()))
                .withCurrency(PaymentService.PaymentCreateRequest.Currency.GBP)
                .withLinksMandate(goCardlessMandateId.toString())
                .withIdempotencyKey(payment.getExternalId())
                .execute();
    }

    public BankDetailsLookup validate(BankAccountDetails bankAccountDetails) {
        return goCardlessClient.bankDetailsLookups().create()
                .withAccountNumber(bankAccountDetails.getAccountNumber().toString())
                .withBranchCode(bankAccountDetails.getSortCode().toString())
                .withCountryCode("GB")
                .execute();
    }

    public Creditor getCreditor() {
        return goCardlessClient.creditors().all().execute().iterator().next();
    }
}
