package uk.gov.pay.directdebit.util;

import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.util.StringColumnMapper;

import java.util.Map;

public class DatabaseTestHelper {

    private DBI jdbi;

    public DatabaseTestHelper(DBI jdbi) {
        this.jdbi = jdbi;
    }

    public Map<String, Object> getTokenByPaymentRequestId(Long paymentRequestId) {
        return jdbi.withHandle(handle ->
                handle
                        .createQuery("SELECT * from tokens t WHERE t.payment_request_id = :payment_request_id  ORDER BY t.id DESC")
                        .bind("payment_request_id", paymentRequestId)
                        .first()
        );
    }

    public String getTokenByPaymentRequestExternalId(String externalId) {
        return jdbi.withHandle(handle ->
                handle
                        .createQuery("SELECT secure_redirect_token from tokens t JOIN payment_requests p ON p.id = t.payment_request_id WHERE p.external_id = :external_id ORDER BY t.id DESC")
                        .bind("external_id", externalId)
                        .map(StringColumnMapper.INSTANCE)
                        .first()
        );
    }

    public Map<String, Object> getPaymentRequestById(Long id) {
        return jdbi.withHandle(handle ->
                handle
                        .createQuery("SELECT * from payment_requests p WHERE p.id = :id")
                        .bind("id", id)
                        .first()
        );
    }

    public Map<String, Object> getPaymentRequestEventById(Long id) {
        return jdbi.withHandle(handle ->
                handle
                        .createQuery("SELECT * from payment_request_events p WHERE p.id = :id")
                        .bind("id", id)
                        .first()
        );
    }

    public Map<String, Object> getTransactionById(Long id) {
        return jdbi.withHandle(handle ->
                handle
                        .createQuery("SELECT * from transactions t WHERE t.id = :id")
                        .bind("id", id)
                        .first()
        );
    }

    public Map<String, Object> getPayerById(Long id) {
        return jdbi.withHandle(handle ->
                handle
                        .createQuery("SELECT * from payers p WHERE p.id = :id")
                        .bind("id", id)
                        .first()
        );
    }

    public Map<String, Object> getPayerByPaymentRequestExternalId(String externalId) {
        return jdbi.withHandle(handle ->
                handle
                        .createQuery("SELECT p.* from payers p INNER JOIN payment_requests r ON p.payment_request_id = r.id WHERE r.external_id = :externalId")
                        .bind("externalId", externalId)
                        .first()
        );
    }

    public Map<String, Object> getMandateById(Long id) {
        return jdbi.withHandle(handle ->
                handle
                        .createQuery("SELECT * from mandates t WHERE t.id = :id")
                        .bind("id", id)
                        .first()
        );
    }

    public Map<String, Object> getGatewayAccountById(Long id) {
        return jdbi.withHandle(handle ->
                handle
                        .createQuery("SELECT * from gateway_accounts t WHERE t.id = :id")
                        .bind("id", id)
                        .first()
        );
    }

    public Map<String, Object> getGoCardlessCustomerById(Long id) {
        return jdbi.withHandle(handle ->
                handle
                        .createQuery("SELECT * from gocardless_customers g WHERE g.id = :id")
                        .bind("id", id)
                        .first()
        );
    }
}
