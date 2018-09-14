package uk.gov.pay.directdebit.common.clients;

import com.gocardless.GoCardlessException;
import com.gocardless.resources.BankDetailsLookup;
import com.gocardless.resources.BankDetailsLookup.AvailableDebitScheme;
import com.gocardless.resources.Creditor;
import com.gocardless.resources.Creditor.SchemeIdentifier.Scheme;
import com.gocardless.resources.Customer;
import com.gocardless.resources.CustomerBankAccount;
import com.gocardless.resources.Payment;
import org.slf4j.Logger;
import uk.gov.pay.directdebit.app.logger.PayLoggerFactory;
import uk.gov.pay.directdebit.common.model.subtype.SunName;
import uk.gov.pay.directdebit.common.model.subtype.gocardless.creditor.GoCardlessCreditorId;
import uk.gov.pay.directdebit.mandate.model.GoCardlessMandate;
import uk.gov.pay.directdebit.mandate.model.GoCardlessPayment;
import uk.gov.pay.directdebit.mandate.model.Mandate;
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

    private static final Logger LOGGER = PayLoggerFactory.getLogger(GoCardlessClientFacade.class);

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

    public GoCardlessMandate createMandate(Mandate mandate, GoCardlessCustomer customer) {
        com.gocardless.resources.Mandate gcMandate = goCardlessClientWrapper.createMandate(mandate.getExternalId(), customer);
        return new GoCardlessMandate(
                mandate.getId(),
                gcMandate.getId(),
                gcMandate.getReference(),
                GoCardlessCreditorId.of(gcMandate.getLinks().getCreditor()));
    }

    public GoCardlessPayment createPayment(Transaction transaction, GoCardlessMandate mandate) {
        Payment gcPayment = goCardlessClientWrapper.createPayment(transaction, mandate);
        return new GoCardlessPayment(
                transaction.getId(),
                gcPayment.getId(),
                LocalDate.parse(gcPayment.getChargeDate()));
    }

    public GoCardlessBankAccountLookup validate(BankAccountDetails bankAccountDetails) {
        try {
            BankDetailsLookup gcBankDetailsLookup = goCardlessClientWrapper.validate(bankAccountDetails);
            return new GoCardlessBankAccountLookup(
                    gcBankDetailsLookup.getBankName(),
                    gcBankDetailsLookup.getAvailableDebitSchemes().contains(AvailableDebitScheme.BACS));
        } catch (GoCardlessException goCardlessException) {
            // this code is temporary setup for debugging purposes to investigate https://github.com/gocardless/gocardless-pro-java/issues/12
            LOGGER.error("!!! GOCARDLESS ERROR GoCardlessException !!!", goCardlessException);
            StackTraceElement[] stackTraceElements = goCardlessException.getStackTrace(); // this can be used with live debugging
            LOGGER.error("!!! GOCARDLESS ERROR goCardlessException.printStackTrace() !!!");
            goCardlessException.printStackTrace();
        }

        return null;
    }

    public Optional<SunName> getSunName(GoCardlessCreditorId goCardlessCreditorId) {
        return goCardlessClientWrapper
                .getCreditor(goCardlessCreditorId.toString())
                .getSchemeIdentifiers()
                .stream()
                .filter(schemeIdentifier -> Scheme.BACS.equals(schemeIdentifier.getScheme()))
                .findFirst()
                .map(Creditor.SchemeIdentifier::getName)
                .map(SunName::of);
    }
}
