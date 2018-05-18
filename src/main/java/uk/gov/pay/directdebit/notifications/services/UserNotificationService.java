package uk.gov.pay.directdebit.notifications.services;

import com.google.common.collect.ImmutableMap;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import javax.inject.Inject;
import uk.gov.pay.directdebit.app.config.DirectDebitConfig;
import uk.gov.pay.directdebit.mandate.model.Mandate;
import uk.gov.pay.directdebit.notifications.clients.AdminUsersClient;
import uk.gov.pay.directdebit.notifications.model.EmailPayload.EmailTemplate;
import uk.gov.pay.directdebit.payers.model.Payer;
import uk.gov.pay.directdebit.payments.model.Transaction;

public class UserNotificationService {

    private static final String PLACEHOLDER_STATEMENT_NAME = "THE-CAKE-IS-A-LIE";

    private static final String DD_GUARANTEE_KEY = "dd guarantee link";
    private static final String MANDATE_REFERENCE_KEY = "mandate reference";
    private static final String STATEMENT_NAME_KEY = "statement name";
    private static final String COLLECTION_DATE_KEY = "collection date";
    private static final String AMOUNT_KEY = "amount";
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
        adminUsersClient.sendEmail(EmailTemplate.MANDATE_FAILED, transaction, payer.getEmail(),
                ImmutableMap.of(
                        MANDATE_REFERENCE_KEY, mandate.getReference(),
                        DD_GUARANTEE_KEY, directDebitConfig.getLinks().getDirectDebitGuaranteeUrl()
                )
        );
    }

    public void sendMandateCancelledEmailFor(Transaction transaction, Mandate mandate, Payer payer) {
        adminUsersClient.sendEmail(EmailTemplate.MANDATE_CANCELLED, transaction, payer.getEmail(),
                ImmutableMap.of(
                        MANDATE_REFERENCE_KEY, mandate.getReference(),
                        DD_GUARANTEE_KEY, directDebitConfig.getLinks().getDirectDebitGuaranteeUrl()
                )
        );
    }

    public void sendPaymentConfirmedEmailFor(Transaction transaction, Payer payer, Mandate mandate, LocalDate earliestChargeDate) {
        adminUsersClient.sendEmail(EmailTemplate.PAYMENT_CONFIRMED, transaction, payer.getEmail(),
                ImmutableMap.<String, String>builder()
                        .put(AMOUNT_KEY, formatToPounds(transaction.getAmount()))
                        .put(COLLECTION_DATE_KEY, DATE_TIME_FORMATTER.format(earliestChargeDate))
                        .put(MANDATE_REFERENCE_KEY, mandate.getReference())
                        .put(BANK_ACCOUNT_LAST_DIGITS_KEY, "******" + payer.getAccountNumberLastTwoDigits())
                        .put(STATEMENT_NAME_KEY, PLACEHOLDER_STATEMENT_NAME)
                        .put(DD_GUARANTEE_KEY, directDebitConfig.getLinks().getDirectDebitGuaranteeUrl())
                        .build());
    }

    public void sendPaymentFailedEmailFor(Transaction transaction, Payer payer) {
        adminUsersClient.sendEmail(EmailTemplate.PAYMENT_FAILED, transaction, payer.getEmail(),
                ImmutableMap.of(
                        DD_GUARANTEE_KEY, directDebitConfig.getLinks().getDirectDebitGuaranteeUrl()
                ));
    }

    private static String formatToPounds(long amountInPence) {
        return BigDecimal.valueOf(amountInPence, 2).toString();
    }

}
