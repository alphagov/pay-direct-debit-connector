package uk.gov.pay.directdebit.mandate.services;

import org.apache.commons.lang3.RandomUtils;
import org.slf4j.Logger;
import uk.gov.pay.directdebit.app.logger.PayLoggerFactory;
import uk.gov.pay.directdebit.mandate.dao.MandateDao;
import uk.gov.pay.directdebit.mandate.model.Mandate;
import uk.gov.pay.directdebit.payments.dao.PaymentRequestDao;
import uk.gov.pay.directdebit.payments.dao.PaymentRequestEventDao;
import uk.gov.pay.directdebit.payments.dao.TransactionDao;
import uk.gov.pay.directdebit.payments.exception.ChargeNotFoundException;
import uk.gov.pay.directdebit.payments.exception.PaymentRequestNotFoundException;
import uk.gov.pay.directdebit.payments.model.PaymentRequest;
import uk.gov.pay.directdebit.payments.model.PaymentRequestEvent;
import uk.gov.pay.directdebit.payments.model.Transaction;

import java.time.ZonedDateTime;

import static uk.gov.pay.directdebit.payments.model.PaymentState.CONFIRMED_DIRECT_DEBIT_DETAILS;

public class PaymentConfirmService {

    private static final Logger logger = PayLoggerFactory.getLogger(PaymentConfirmService.class);

    private final TransactionDao transactionDao;
    private final PaymentRequestDao paymentRequestDao;
    private final PaymentRequestEventDao paymentRequestEventDao;
    private final MandateDao mandateDao;

    public PaymentConfirmService(TransactionDao transactionDao, PaymentRequestDao paymentRequestDao, PaymentRequestEventDao paymentRequestEventDao, MandateDao mandateDao) {
        this.transactionDao = transactionDao;
        this.paymentRequestDao = paymentRequestDao;
        this.paymentRequestEventDao = paymentRequestEventDao;
        this.mandateDao = mandateDao;
    }

    public void confirm(Long accountId, String paymentExternalId) {
        PaymentRequest paymentRequest = paymentRequestDao
                .findByExternalId(paymentExternalId)
                .map(payment -> {
                    Transaction transaction = transactionDao.findByPaymentRequestId(payment.getId())
                            .orElseThrow(() -> new ChargeNotFoundException(paymentExternalId));
                    logger.info("Found charge for payment request with id: {}", payment.getExternalId());
                    transaction.setState(CONFIRMED_DIRECT_DEBIT_DETAILS);
                    transactionDao.updateState(transaction.getId(), CONFIRMED_DIRECT_DEBIT_DETAILS);
                    mandateDao.insert(new Mandate(RandomUtils.nextLong(0, 9999)));
                    return payment;
                })
                .orElseThrow(() -> new PaymentRequestNotFoundException(paymentExternalId));

        PaymentRequestEvent paymentRequestEvent = new PaymentRequestEvent(
                paymentRequest.getId(),
                PaymentRequestEvent.Type.CHARGE,
                PaymentRequestEvent.SupportedEvent.PAYMENT_CONFIRMED,
                ZonedDateTime.now());
        paymentRequestEventDao.insert(paymentRequestEvent);
        logger.info("Created event for payment request {}", paymentExternalId);
    }
}
