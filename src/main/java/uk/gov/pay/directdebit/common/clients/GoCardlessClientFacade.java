package uk.gov.pay.directdebit.common.clients;

import com.gocardless.resources.BankDetailsLookup;
import com.gocardless.resources.BankDetailsLookup.AvailableDebitScheme;
import com.gocardless.resources.Creditor;
import com.gocardless.resources.Creditor.SchemeIdentifier.Scheme;
import com.gocardless.resources.Customer;
import com.gocardless.resources.CustomerBankAccount;
import com.gocardless.resources.Payment;
import uk.gov.pay.directdebit.common.model.subtype.SunName;
import uk.gov.pay.directdebit.mandate.model.GoCardlessMandateId;
import uk.gov.pay.directdebit.mandate.model.GoCardlessPayment;
import uk.gov.pay.directdebit.mandate.model.Mandate;
import uk.gov.pay.directdebit.mandate.model.MandateBankStatementReference;
import uk.gov.pay.directdebit.mandate.model.PaymentProviderMandateIdAndBankReference;
import uk.gov.pay.directdebit.mandate.model.subtype.MandateExternalId;
import uk.gov.pay.directdebit.payers.model.AccountNumber;
import uk.gov.pay.directdebit.payers.model.BankAccountDetails;
import uk.gov.pay.directdebit.payers.model.GoCardlessBankAccountLookup;
import uk.gov.pay.directdebit.payers.model.GoCardlessCustomer;
import uk.gov.pay.directdebit.payers.model.Payer;
import uk.gov.pay.directdebit.payers.model.SortCode;
import uk.gov.pay.directdebit.payments.model.Transaction;

import javax.inject.Inject;
import java.time.LocalDate;
import java.util.Optional;

public class GoCardlessClientFacade {

    private final GoCardlessClientWrapper goCardlessClientWrapper;

    @Inject
    public GoCardlessClientFacade(GoCardlessClientWrapper goCardlessClientWrapper) {
        this.goCardlessClientWrapper = goCardlessClientWrapper;
    }

    public GoCardlessCustomer createCustomer(MandateExternalId mandateExternalId, Payer payer) {
        Customer customer = goCardlessClientWrapper.createCustomer(mandateExternalId, payer);
        return new GoCardlessCustomer(
                payer.getId(),
                customer.getId());
    }

    public GoCardlessCustomer createCustomerBankAccount(MandateExternalId mandateExternalId, GoCardlessCustomer customer,
                                                        String accountHolderName, SortCode sortCode, AccountNumber accountNumber) {
        CustomerBankAccount gcCustomerBankAccount = goCardlessClientWrapper.createCustomerBankAccount(mandateExternalId, customer,
                accountHolderName, sortCode, accountNumber);
        customer.setCustomerBankAccountId(gcCustomerBankAccount.getId());
        return customer;
    }

    public PaymentProviderMandateIdAndBankReference createMandate(Mandate mandate, GoCardlessCustomer customer) {
        var createdMandate = goCardlessClientWrapper.createMandate(mandate.getExternalId(), customer);
        return new PaymentProviderMandateIdAndBankReference(
                GoCardlessMandateId.valueOf(createdMandate.getId()),
                MandateBankStatementReference.valueOf(createdMandate.getReference()));
    }

    public GoCardlessPayment createPayment(Transaction transaction, GoCardlessMandateId goCardlessMandateId) {
        Payment gcPayment = goCardlessClientWrapper.createPayment(transaction, goCardlessMandateId);
        return new GoCardlessPayment(
                transaction.getId(),
                gcPayment.getId(),
                LocalDate.parse(gcPayment.getChargeDate()));
    }

    public GoCardlessBankAccountLookup validate(BankAccountDetails bankAccountDetails) {
        BankDetailsLookup gcBankDetailsLookup = goCardlessClientWrapper.validate(bankAccountDetails);
        return new GoCardlessBankAccountLookup(
                gcBankDetailsLookup.getBankName(),
                gcBankDetailsLookup.getAvailableDebitSchemes().contains(AvailableDebitScheme.BACS));
    }

    public Optional<SunName> getSunName() {
        return goCardlessClientWrapper
                .getCreditor()
                .getSchemeIdentifiers()
                .stream()
                .filter(schemeIdentifier -> Scheme.BACS.equals(schemeIdentifier.getScheme()))
                .findFirst()
                .map(Creditor.SchemeIdentifier::getName)
                .map(SunName::of);
    }
}
