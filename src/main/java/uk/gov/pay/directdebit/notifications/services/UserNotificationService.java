package uk.gov.pay.directdebit.notifications.services;

import com.google.common.collect.ImmutableMap;
import uk.gov.pay.directdebit.app.config.DirectDebitConfig;
import uk.gov.pay.directdebit.mandate.model.Mandate;
import uk.gov.pay.directdebit.notifications.clients.AdminUsersClient;
import uk.gov.pay.directdebit.notifications.model.EmailPayload.EmailTemplate;
import uk.gov.pay.directdebit.payments.model.Transaction;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

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

    public void sendMandateFailedEmailFor(Mandate mandate) {
        adminUsersClient.sendEmail(EmailTemplate.MANDATE_FAILED, mandate,
                ImmutableMap.of(
                        MANDATE_REFERENCE_KEY, mandate.getMandateReference(),
                        DD_GUARANTEE_KEY, directDebitConfig.getLinks().getDirectDebitGuaranteeUrl()
                )
        );
    }

    public void sendMandateCancelledEmailFor(Mandate mandate) {
        adminUsersClient.sendEmail(EmailTemplate.MANDATE_CANCELLED, mandate,
                ImmutableMap.of(
                        MANDATE_REFERENCE_KEY, mandate.getMandateReference(),
                        DD_GUARANTEE_KEY, directDebitConfig.getLinks().getDirectDebitGuaranteeUrl()
                )
        );
    }

    public void sendOneOffPaymentConfirmedEmailFor(Transaction transaction, LocalDate earliestChargeDate) {
        sendPaymentConfirmedEmailFor(EmailTemplate.ONE_OFF_PAYMENT_CONFIRMED, transaction, earliestChargeDate);
    }

    public void sendOndDemandPaymentConfirmedEmailFor(Transaction transaction, LocalDate earliestChargeDate) {
        sendPaymentConfirmedEmailFor(EmailTemplate.ON_DEMAND_PAYMENT_CONFIRMED, transaction, earliestChargeDate);
    }
    
    private void sendPaymentConfirmedEmailFor(EmailTemplate template, Transaction transaction, LocalDate earliestChargeDate) {
        adminUsersClient.sendEmail(template, transaction.getMandate(),
                ImmutableMap.<String, String>builder()
                        .put(AMOUNT_KEY, formatToPounds(transaction.getAmount()))
                        .put(COLLECTION_DATE_KEY, DATE_TIME_FORMATTER.format(earliestChargeDate))
                        .put(MANDATE_REFERENCE_KEY, transaction.getMandate().getMandateReference())
                        .put(BANK_ACCOUNT_LAST_DIGITS_KEY, "******" + transaction.getMandate().getPayer().getAccountNumberLastTwoDigits())
                        .put(STATEMENT_NAME_KEY, PLACEHOLDER_STATEMENT_NAME)
                        .put(DD_GUARANTEE_KEY, directDebitConfig.getLinks().getDirectDebitGuaranteeUrl())
                        .build());
    }
    
    public void sendPaymentFailedEmailFor(Transaction transaction) {
        adminUsersClient.sendEmail(EmailTemplate.PAYMENT_FAILED, transaction.getMandate(),
                ImmutableMap.of(
                        DD_GUARANTEE_KEY, directDebitConfig.getLinks().getDirectDebitGuaranteeUrl()
                ));
    }

    private static String formatToPounds(long amountInPence) {
        return BigDecimal.valueOf(amountInPence, 2).toString();
    }

}
