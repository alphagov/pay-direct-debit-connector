package uk.gov.pay.directdebit.notifications.services;

import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.directdebit.app.config.DirectDebitConfig;
import uk.gov.pay.directdebit.common.model.subtype.SunName;
import uk.gov.pay.directdebit.common.services.SunService;
import uk.gov.pay.directdebit.mandate.exception.PayerNotFoundException;
import uk.gov.pay.directdebit.mandate.model.Mandate;
import uk.gov.pay.directdebit.notifications.clients.AdminUsersClient;
import uk.gov.pay.directdebit.notifications.model.EmailPayload.EmailTemplate;
import uk.gov.pay.directdebit.payments.model.Payment;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;

public class UserNotificationService {

    private static final String DD_GUARANTEE_KEY = "dd guarantee link";
    private static final String MANDATE_REFERENCE_KEY = "mandate reference";
    private static final String STATEMENT_NAME_KEY = "statement name";
    private static final String COLLECTION_DATE_KEY = "collection date";
    private static final String AMOUNT_KEY = "amount";
    private static final String BANK_ACCOUNT_LAST_DIGITS_KEY = "bank account last 2 digits";
    private static final Logger LOGGER = LoggerFactory.getLogger(UserNotificationService.class);

    private AdminUsersClient adminUsersClient;
    private final DirectDebitConfig directDebitConfig;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final SunService sunService;

    @Inject
    public UserNotificationService(AdminUsersClient adminUsersClient, DirectDebitConfig directDebitConfig, SunService sunService) {
        this.adminUsersClient = adminUsersClient;
        this.directDebitConfig = directDebitConfig;
        this.sunService = sunService;
    }

    public void sendMandateFailedEmailFor(Mandate mandate) {
        // it is safe to call mandate.getMandateBankStatementReference().get() here because when we create a mandate 
        // with GoCardless, their library returns one of their `Mandate` objects. We call `getReference()` on that, 
        // which the Javadoc implies will not be null. Then we call `MandateBankStatementReference.valueOf(…)` on that, 
        // which will throw an exception if it’s null.
        String mandateReference = mandate.getMandateBankStatementReference().get().toString();
        String directDebitGuaranteeUrl = directDebitConfig.getLinks().getDirectDebitGuaranteeUrl();
        adminUsersClient.sendEmail(EmailTemplate.MANDATE_FAILED, mandate,
                Map.of(MANDATE_REFERENCE_KEY, mandateReference, DD_GUARANTEE_KEY, directDebitGuaranteeUrl));
    }

    public void sendMandateCreatedEmailFor(Mandate mandate) {
        EmailTemplate template = EmailTemplate.ON_DEMAND_MANDATE_CREATED;
        Optional<SunName> sunName = sunService.getSunNameFor(mandate);
        if (sunName.isEmpty()) {
            logMissingSunName(template, mandate);
            return;
        }

        var personalisation = Map.of(MANDATE_REFERENCE_KEY, mandate.getMandateBankStatementReference().toString(),
                BANK_ACCOUNT_LAST_DIGITS_KEY, mandate.getPayer().orElseThrow(
                        () -> new PayerNotFoundException(mandate.getExternalId())).getAccountNumberLastTwoDigits(),
                STATEMENT_NAME_KEY, sunName.get().toString(),
                DD_GUARANTEE_KEY, directDebitConfig.getLinks().getDirectDebitGuaranteeUrl());
        adminUsersClient.sendEmail(template, mandate, personalisation);
    }

    public void sendMandateCancelledEmailFor(Mandate mandate) {
        adminUsersClient.sendEmail(EmailTemplate.MANDATE_CANCELLED, mandate,
                ImmutableMap.of(
                        MANDATE_REFERENCE_KEY, mandate.getMandateBankStatementReference().get().toString(),
                        DD_GUARANTEE_KEY, directDebitConfig.getLinks().getDirectDebitGuaranteeUrl()
                )
        );
    }

    public void sendPaymentConfirmedEmailFor(Payment payment) {
        sendPaymentConfirmedEmailFor(EmailTemplate.ON_DEMAND_PAYMENT_CONFIRMED, payment);
    }

    private void sendPaymentConfirmedEmailFor(EmailTemplate template, Payment payment) {
        Mandate mandate = payment.getMandate();
        Optional<SunName> sunName = sunService.getSunNameFor(mandate);
        if (!sunName.isPresent()) {
            logMissingSunName(template, mandate);
            return;
        }

        LocalDate chargeDate = payment.getChargeDate()
                .orElseThrow(() -> new IllegalArgumentException("No charge date on payment " + payment.getExternalId()));

        var personalisation = Map.of(AMOUNT_KEY, formatToPounds(payment.getAmount()),
                COLLECTION_DATE_KEY, DATE_TIME_FORMATTER.format(chargeDate),
                MANDATE_REFERENCE_KEY, mandate.getMandateBankStatementReference().toString(),
                BANK_ACCOUNT_LAST_DIGITS_KEY, mandate.getPayer().orElseThrow(
                        () -> new PayerNotFoundException(mandate.getExternalId())).getAccountNumberLastTwoDigits(),
                STATEMENT_NAME_KEY, sunName.get().toString(),
                DD_GUARANTEE_KEY, directDebitConfig.getLinks().getDirectDebitGuaranteeUrl());
        adminUsersClient.sendEmail(template, mandate, personalisation);
    }

    public void sendPaymentFailedEmailFor(Payment payment) {
        adminUsersClient.sendEmail(EmailTemplate.PAYMENT_FAILED, payment.getMandate(),
                ImmutableMap.of(
                        DD_GUARANTEE_KEY, directDebitConfig.getLinks().getDirectDebitGuaranteeUrl()
                ));
    }

    private static String formatToPounds(long amountInPence) {
        return BigDecimal.valueOf(amountInPence, 2).toString();
    }

    private void logMissingSunName(EmailTemplate template, Mandate mandate) {
        LOGGER.error("Mandate {} does not have a Service User Number. " +
                "Email with template {} is not being sent.", mandate.getExternalId(), template);
    }

}
