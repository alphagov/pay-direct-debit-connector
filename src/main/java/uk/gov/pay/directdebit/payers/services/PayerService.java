package uk.gov.pay.directdebit.payers.services;

import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import uk.gov.pay.directdebit.app.config.DirectDebitConfig;
import uk.gov.pay.directdebit.app.logger.PayLoggerFactory;
import uk.gov.pay.directdebit.payers.api.CreatePayerResponse;
import uk.gov.pay.directdebit.payers.dao.PayerDao;
import uk.gov.pay.directdebit.payers.model.Payer;
import uk.gov.pay.directdebit.payments.dao.PaymentRequestDao;
import uk.gov.pay.directdebit.payments.dao.PaymentRequestEventDao;
import uk.gov.pay.directdebit.payments.exception.PaymentRequestNotFoundException;
import uk.gov.pay.directdebit.payments.model.PaymentRequest;
import uk.gov.pay.directdebit.payments.model.PaymentRequestEvent;
import uk.gov.pay.directdebit.payments.services.PaymentRequestService;

import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Optional;

public class PayerService {
    private static final Logger logger = PayLoggerFactory.getLogger(PaymentRequestService.class);

    private final PayerDao payerDao;
    private final PaymentRequestEventDao paymentRequestEventDao;
    private final PaymentRequestDao paymentRequestDao;
    private final String encryptDBSalt;

    public PayerService(DirectDebitConfig config, PayerDao payerDao, PaymentRequestDao paymentRequestDao, PaymentRequestEventDao paymentRequestEventDao) {
        this.payerDao = payerDao;
        this.paymentRequestDao = paymentRequestDao;
        this.paymentRequestEventDao = paymentRequestEventDao;
        this.encryptDBSalt = config.getEncryptDBSalt();
    }

    public CreatePayerResponse create(String paymentRequestExternalId, Map<String, String> createPayerMap) {
        PaymentRequest paymentRequest = paymentRequestDao
                .findByExternalId(paymentRequestExternalId)
                .orElseThrow(() -> new PaymentRequestNotFoundException(paymentRequestExternalId));

        insertReceivedEventFor(paymentRequest.getId(), paymentRequestExternalId);

        String accountNumber = createPayerMap.get("account_number");
        String sortCode = createPayerMap.get("sort_code");

        Payer payer = new Payer(
                paymentRequest.getId(),
                createPayerMap.get("account_holder_name"),
                createPayerMap.get("email"),
                encrypt(sortCode),
                encrypt(accountNumber),
                accountNumber.substring(accountNumber.length()-3),
                Boolean.parseBoolean(createPayerMap.get("requires_authorisation")),
                createPayerMap.get("address_line1"),
                createPayerMap.get("address_line2"),
                createPayerMap.get("postcode"),
                createPayerMap.get("city"),
                createPayerMap.get("country_code")
        );
        Long id = payerDao.insert(payer);
        payer.setId(id);
        insertCreatedEventFor(payer);
        return new CreatePayerResponse(payer.getExternalId());
    }

    //todo change event
    private void insertCreatedEventFor(Payer payer) {
        PaymentRequestEvent paymentRequestEvent = new PaymentRequestEvent(
                payer.getId(),
                PaymentRequestEvent.Type.PAYER,
                PaymentRequestEvent.SupportedEvent.CHARGE_CREATED,
                ZonedDateTime.now());
        logger.info("Created event for payer {}", payer.getExternalId());
        paymentRequestEventDao.insert(paymentRequestEvent);
    }

    //todo change event
    private void insertReceivedEventFor(Long paymentRequestId, String paymentRequestExternalId) {
        PaymentRequestEvent paymentRequestEvent = new PaymentRequestEvent(
                paymentRequestId,
                PaymentRequestEvent.Type.PAYER,
                PaymentRequestEvent.SupportedEvent.CHARGE_CREATED,
                ZonedDateTime.now());
        logger.info("Created event for payment request {}", paymentRequestExternalId);
        paymentRequestEventDao.insert(paymentRequestEvent);
    }
    private String encrypt(String toEncrypt) {
        return BCrypt.hashpw(toEncrypt, encryptDBSalt);
    }

}
