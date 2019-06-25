package uk.gov.pay.directdebit.mandate.services.gocardless;

import com.gocardless.GoCardlessException;
import com.gocardless.errors.ValidationFailedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.directdebit.common.clients.GoCardlessClientFacade;
import uk.gov.pay.directdebit.common.clients.GoCardlessClientFactory;
import uk.gov.pay.directdebit.common.exception.InternalServerErrorException;
import uk.gov.pay.directdebit.common.model.subtype.SunName;
import uk.gov.pay.directdebit.mandate.model.GoCardlessMandateId;
import uk.gov.pay.directdebit.mandate.model.Mandate;
import uk.gov.pay.directdebit.mandate.model.PaymentProviderMandateIdAndBankReference;
import uk.gov.pay.directdebit.mandate.model.subtype.MandateExternalId;
import uk.gov.pay.directdebit.payers.api.BankAccountValidationResponse;
import uk.gov.pay.directdebit.payers.dao.GoCardlessCustomerDao;
import uk.gov.pay.directdebit.payers.model.BankAccountDetails;
import uk.gov.pay.directdebit.payers.model.GoCardlessBankAccountLookup;
import uk.gov.pay.directdebit.payers.model.GoCardlessCustomer;
import uk.gov.pay.directdebit.payers.model.Payer;
import uk.gov.pay.directdebit.payments.dao.PaymentDao;
import uk.gov.pay.directdebit.payments.exception.CreateCustomerBankAccountFailedException;
import uk.gov.pay.directdebit.payments.exception.CreateCustomerFailedException;
import uk.gov.pay.directdebit.payments.exception.CreateMandateFailedException;
import uk.gov.pay.directdebit.payments.exception.CreatePaymentFailedException;
import uk.gov.pay.directdebit.payments.exception.GoCardlessMandateNotConfirmed;
import uk.gov.pay.directdebit.payments.model.DirectDebitPaymentProviderCommandService;
import uk.gov.pay.directdebit.payments.model.Payment;
import uk.gov.pay.directdebit.payments.model.PaymentProviderPaymentIdAndChargeDate;
import uk.gov.pay.directdebit.mandate.exception.PayerNotFoundException;

import javax.inject.Inject;
import java.util.Optional;

public class GoCardlessService implements DirectDebitPaymentProviderCommandService {

    private static final Logger LOGGER = LoggerFactory.getLogger(GoCardlessService.class);

    private final GoCardlessClientFactory goCardlessClientFactory;
    private final GoCardlessCustomerDao goCardlessCustomerDao;
    private final PaymentDao paymentDao;

    @Inject
    public GoCardlessService(
            GoCardlessClientFactory goCardlessClientFactory,
            GoCardlessCustomerDao goCardlessCustomerDao,
            PaymentDao paymentDao) {
        this.goCardlessClientFactory = goCardlessClientFactory;
        this.goCardlessCustomerDao = goCardlessCustomerDao;
        this.paymentDao = paymentDao;
    }

    @Override
    public PaymentProviderMandateIdAndBankReference confirmMandate(Mandate mandate, BankAccountDetails bankAccountDetails) {
        LOGGER.info("Confirming direct debit details, on demand mandate with id: {}", mandate.getExternalId());
        GoCardlessCustomer goCardlessCustomer = createCustomer(mandate);
        GoCardlessCustomer goCardlessCustomerWithBankAccount = createCustomerBankAccount(
                mandate,
                goCardlessCustomer,
                bankAccountDetails
        );
        persist(goCardlessCustomer);
        return createMandate(mandate, goCardlessCustomerWithBankAccount);
    }

    @Override
    public PaymentProviderPaymentIdAndChargeDate collect(Mandate mandate, Payment payment) {
        LOGGER.info("Collecting payment for GoCardless, mandate with id: {}, payment with id: {}", mandate.getExternalId(), payment.getExternalId());
        var goCardlessMandateId = mandate.getPaymentProviderMandateId()
                .map(a -> (GoCardlessMandateId) a)
                .orElseThrow( () -> new GoCardlessMandateNotConfirmed("mandate id", mandate.getExternalId().toString()));

        PaymentProviderPaymentIdAndChargeDate providerIdAndChargeDate = createPayment(payment, goCardlessMandateId);
        Payment updatePayment = Payment.PaymentBuilder.fromPayment(payment)
                .withProviderId(providerIdAndChargeDate.getPaymentProviderPaymentId())
                .withChargeDate(providerIdAndChargeDate.getChargeDate())
                .build();

        paymentDao.updateProviderIdAndChargeDate(updatePayment);
        return providerIdAndChargeDate;
    }

    @Override
    public BankAccountValidationResponse validate(Mandate mandate, BankAccountDetails bankAccountDetails) {
        LOGGER.info("Attempting to call GoCardless to validate a bank account, mandate with id: {}", mandate.getExternalId());
        try {
            GoCardlessClientFacade goCardlessClientFacade = goCardlessClientFactory.getClientFor(mandate.getGatewayAccount().getAccessToken());
            GoCardlessBankAccountLookup lookup = goCardlessClientFacade.validate(bankAccountDetails);
            return new BankAccountValidationResponse(lookup.isBacs(), lookup.getBankName());
        } catch (ValidationFailedException exc) {
            return new BankAccountValidationResponse(false);
        } catch (GoCardlessException exc) {
            LOGGER.error("Exception while validating bank account details in GoCardless, message: {}", exc.getMessage());
            throw new InternalServerErrorException("Exception while validating bank account details in GoCardless");
        }
    }

    @Override
    public Optional<SunName> getSunName(Mandate mandate) {
        LOGGER.info("Attempting to call GoCardless to retrieve service user name from creditor for mandate with id: {}", mandate.getExternalId());
        try {
            return goCardlessClientFactory.getClientFor(mandate.getGatewayAccount().getAccessToken()).getSunName();
        } catch (Exception exc) {
            LOGGER.error("Exception while retrieving service user name from GoCardless, message: {}", exc.getMessage());
            return Optional.empty();
        }
    }

    private GoCardlessCustomer createCustomer(Mandate mandate) {
        MandateExternalId mandateExternalId = mandate.getExternalId();
        Payer payer = mandate.getPayer().orElseThrow(() -> new PayerNotFoundException(mandateExternalId));
        try {
            LOGGER.info("Attempting to call GoCardless to create a customer, mandate id: {}", mandateExternalId);
            GoCardlessClientFacade goCardlessClientFacade = goCardlessClientFactory.getClientFor(mandate.getGatewayAccount().getAccessToken());
            GoCardlessCustomer customer = goCardlessClientFacade.createCustomer(mandateExternalId, payer);
            LOGGER.info("Created customer in GoCardless, mandate id: {}", mandateExternalId);

            return customer;
        } catch (Exception exc) {
            logException(exc, "customer", mandateExternalId.toString());
            throw new CreateCustomerFailedException(mandateExternalId, payer.getExternalId());
        }
    }

    private GoCardlessCustomer createCustomerBankAccount(Mandate mandate, GoCardlessCustomer goCardlessCustomer, BankAccountDetails bankAccountDetails) {
        MandateExternalId mandateExternalId = mandate.getExternalId();
        Payer payer = mandate.getPayer().orElseThrow(() -> new PayerNotFoundException(mandateExternalId));

        try {
            LOGGER.info("Attempting to call GoCardless to create a customer bank account, mandate id: {}", mandateExternalId);
            GoCardlessClientFacade goCardlessClientFacade = goCardlessClientFactory.getClientFor(mandate.getGatewayAccount().getAccessToken());
            GoCardlessCustomer customerWithBankAccount = goCardlessClientFacade.createCustomerBankAccount(
                    mandateExternalId,
                    goCardlessCustomer,
                    payer.getName(),
                    bankAccountDetails.getSortCode(),
                    bankAccountDetails.getAccountNumber()
            );

            LOGGER.info("Created customer bank account in GoCardless, mandate id: {}", mandateExternalId);

            return customerWithBankAccount;
        } catch (Exception exc) {
            logException(exc, "bank account", mandateExternalId.toString());
            throw new CreateCustomerBankAccountFailedException(mandateExternalId, payer.getExternalId());
        }
    }

    private PaymentProviderMandateIdAndBankReference createMandate(Mandate mandate, GoCardlessCustomer goCardlessCustomer) {
        try {

            LOGGER.info("Attempting to call GoCardless to create a mandate, pay mandate id: {}", mandate.getExternalId());

            GoCardlessClientFacade goCardlessClientFacade = goCardlessClientFactory.getClientFor(mandate.getGatewayAccount().getAccessToken());
            PaymentProviderMandateIdAndBankReference mandateIdAndBankReference = goCardlessClientFacade.createMandate(mandate, goCardlessCustomer);

            LOGGER.info("Created mandate in GoCardless, pay mandate id: {}, GoCardless mandate id: {}",
                    mandate.getExternalId(),
                    mandateIdAndBankReference.getPaymentProviderMandateId());

            return mandateIdAndBankReference;

        } catch (Exception exc) {
            logException(exc, "mandate", mandate.getExternalId().toString());
            throw new CreateMandateFailedException(mandate.getExternalId().toString());
        }
    }

    private PaymentProviderPaymentIdAndChargeDate createPayment(Payment payment, GoCardlessMandateId goCardlessMandateId) {
        try {
            LOGGER.info("Attempting to call GoCardless to create a payment, mandate id: {}, payment id: {}",
                    payment.getMandate().getExternalId(),
                    payment.getExternalId());
            GoCardlessClientFacade goCardlessClientFacade = goCardlessClientFactory.getClientFor(payment.getMandate().getGatewayAccount().getAccessToken());

            PaymentProviderPaymentIdAndChargeDate paymentIdAndChargeDate = goCardlessClientFacade.createPayment(payment, goCardlessMandateId);

            LOGGER.info("Created payment in GoCardless, mandate id: {}, payment id: {}, GoCardless payment id: {}",
                    payment.getMandate().getExternalId(),
                    payment.getExternalId(),
                    paymentIdAndChargeDate.getPaymentProviderPaymentId());

            return paymentIdAndChargeDate;
        } catch (Exception exc) {
            logException(exc, "payment", payment.getExternalId());
            throw new CreatePaymentFailedException(payment.getMandate().getExternalId().toString(), payment.getExternalId());
        }
    }

    private void persist(GoCardlessCustomer customer) {
        goCardlessCustomerDao.insert(customer);
    }

    private void logException(Exception exc, String resource, String id) {
        if (exc.getCause() != null) {
            LOGGER.error("Failed to create a {} in GoCardless, id : {}, error: {}, cause: {}", resource, id, exc.getMessage(), exc.getCause().getMessage());
        } else {
            LOGGER.error("Failed to create a {} in GoCardless, id: {}, error: {}", resource, id, exc.getMessage());
        }
    }

}
