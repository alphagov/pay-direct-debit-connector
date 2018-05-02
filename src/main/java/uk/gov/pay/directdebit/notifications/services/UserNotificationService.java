package uk.gov.pay.directdebit.notifications.services;

import uk.gov.pay.directdebit.app.config.DirectDebitConfig;
import uk.gov.pay.directdebit.mandate.model.Mandate;
import uk.gov.pay.directdebit.notifications.clients.AdminUsersClient;
import uk.gov.pay.directdebit.notifications.model.EmailPayload.EmailTemplate;
import uk.gov.pay.directdebit.payers.model.Payer;
import uk.gov.pay.directdebit.payments.model.Transaction;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;

public class UserNotificationService {

    private static final String PLACEHOLDER_SUN = "THE-CAKE-IS-A-LIE";

    private static final String DD_GUARANTEE_KEY = "dd guarantee link";
    private static final String MANDATE_REFERENCE_KEY = "mandate reference";
    private static final String SUN_KEY = "SUN";
    private static final String COLLECTION_DATE_KEY = "collection date";
    private static final String AMOUNT_KEY = "amount";
    private static final String PAYMENT_REFERENCE_KEY = "payment reference";
    private static final String BANK_ACCOUNT_LAST_DIGITS_KEY = "bank account last 2 digits";

    private AdminUsersClient adminUsersClient;
    private final DirectDebitConfig directDebitConfig;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Inject
    public UserNotificationService(AdminUsersClient adminUsersClient, DirectDebitConfig directDebitConfig) {
        this.adminUsersClient = adminUsersClient;
        this.directDebitConfig = directDebitConfig;
    }

    public void sendMandateFailedEmailFor(Transaction transaction, Mandate mandate, Payer payer) {
        adminUsersClient.sendEmail(EmailTemplate.MANDATE_FAILED, transaction, payer.getEmail(), buildMandateProblemPersonalisation(mandate));
    }

    public void sendMandateCancelledEmailFor(Transaction transaction, Mandate mandate, Payer payer) {
        adminUsersClient.sendEmail(EmailTemplate.MANDATE_CANCELLED, transaction, payer.getEmail(), buildMandateProblemPersonalisation(mandate));
    }

    public void sendPaymentConfirmedEmailFor(Transaction transaction, Payer payer, LocalDate earliestChargeDate) {
        adminUsersClient.sendEmail(EmailTemplate.PAYMENT_CONFIRMED, transaction, payer.getEmail(),
                buildPaymentConfirmedPersonalisation(transaction, payer, earliestChargeDate));
    }

    public void sendPaymentFailedEmailFor(Transaction transaction, Payer payer) {
        adminUsersClient.sendEmail(EmailTemplate.PAYMENT_FAILED, transaction, payer.getEmail(),
                buildPaymentProblemPersonalisation());
    }

    private HashMap<String, String> buildMandateProblemPersonalisation(Mandate mandate) {
        HashMap<String, String> map = new HashMap<>();
        // fixme use the right reference once we play PP-3547
        map.put(MANDATE_REFERENCE_KEY, mandate.getReference());
        map.put(DD_GUARANTEE_KEY, directDebitConfig.getLinks().getDirectDebitGuaranteeUrl());
        return map;
    }

    private HashMap<String, String> buildPaymentConfirmedPersonalisation(Transaction transaction, Payer payer, LocalDate earliestChargeDate) {
        HashMap<String, String> map = new HashMap<>();
        map.put(AMOUNT_KEY, formatToPounds(transaction.getAmount()));
        map.put(PAYMENT_REFERENCE_KEY, transaction.getPaymentRequest().getReference());
        map.put(BANK_ACCOUNT_LAST_DIGITS_KEY, "******" + payer.getAccountNumberLastTwoDigits());
        map.put(COLLECTION_DATE_KEY, DATE_TIME_FORMATTER.format(earliestChargeDate));
        map.put(SUN_KEY, PLACEHOLDER_SUN);
        map.put(DD_GUARANTEE_KEY, directDebitConfig.getLinks().getDirectDebitGuaranteeUrl());
        return map;
    }

    private HashMap<String, String> buildPaymentProblemPersonalisation() {
        HashMap<String, String> map = new HashMap<>();
        map.put(DD_GUARANTEE_KEY, directDebitConfig.getLinks().getDirectDebitGuaranteeUrl());
        return map;
    }

    private static String formatToPounds(long amountInPence) {
        return BigDecimal.valueOf(amountInPence, 2).toString();
    }

}
