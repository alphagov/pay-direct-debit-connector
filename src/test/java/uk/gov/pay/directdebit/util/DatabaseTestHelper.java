package uk.gov.pay.directdebit.util;

import org.jdbi.v3.core.Jdbi;

import java.util.List;
import java.util.Map;

public class DatabaseTestHelper {

    private Jdbi jdbi;

    public DatabaseTestHelper(Jdbi jdbi) {
        this.jdbi = jdbi;
    }

    public Map<String, Object> getTokenByPaymentRequestId(Long paymentRequestId) {
        return jdbi.withHandle(handle ->
                handle
                        .createQuery("SELECT * from tokens t WHERE t.payment_request_id = :payment_request_id  ORDER BY t.id DESC")
                        .bind("payment_request_id", paymentRequestId)
                        .mapToMap()
                        .findFirst()
                        .get()
        );
    }

    public String getTokenByPaymentRequestExternalId(String externalId) {
        return jdbi.withHandle(handle ->
                handle
                        .createQuery("SELECT secure_redirect_token from tokens t JOIN payment_requests p ON p.id = t.payment_request_id WHERE p.external_id = :external_id ORDER BY t.id DESC")
                        .bind("external_id", externalId)
                        .mapTo(String.class)
                        .findFirst()
                        .get()
        );
    }

    public Map<String, Object> getPaymentRequestById(Long id) {
        return jdbi.withHandle(handle ->
                handle
                        .createQuery("SELECT * from payment_requests p WHERE p.id = :id")
                        .bind("id", id)
                        .mapToMap()
                        .findFirst()
                        .get()
        );
    }

    public Map<String, Object> getPaymentRequestByExternalId(String externalId) {
        return jdbi.withHandle(handle ->
                handle
                        .createQuery("SELECT * from payment_requests p WHERE p.external_id = :externalId")
                        .bind("externalId", externalId)
                        .mapToMap()
                        .findFirst()
                        .get()
        );
    }

    public Map<String, Object> getPaymentRequestEventById(Long id) {
        return jdbi.withHandle(handle ->
                handle
                        .createQuery("SELECT * from payment_request_events p WHERE p.id = :id")
                        .bind("id", id)
                        .mapToMap()
                        .findFirst()
                        .get()
        );
    }

    public Map<String, Object> getTransactionById(Long id) {
        return jdbi.withHandle(handle ->
                handle
                        .createQuery("SELECT * from transactions t WHERE t.id = :id")
                        .bind("id", id)
                        .mapToMap()
                        .findFirst()
                        .get()
        );
    }

    public Map<String, Object> getPayerById(Long id) {
        return jdbi.withHandle(handle ->
                handle
                        .createQuery("SELECT * from payers p WHERE p.id = :id")
                        .bind("id", id)
                        .mapToMap()
                        .findFirst()
                        .get()
        );
    }

    public Map<String, Object> getPayerByPaymentRequestExternalId(String externalId) {
        return jdbi.withHandle(handle ->
                handle
                        .createQuery("SELECT p.* from payers p INNER JOIN payment_requests r ON p.payment_request_id = r.id WHERE r.external_id = :externalId")
                        .bind("externalId", externalId)
                        .mapToMap()
                        .findFirst()
                        .get()
        );
    }

    public Map<String, Object> getMandateById(Long id) {
        return jdbi.withHandle(handle ->
                handle
                        .createQuery("SELECT * from mandates t WHERE t.id = :id")
                        .bind("id", id)
                        .mapToMap()
                        .findFirst()
                        .get()
        );
    }

    public Map<String, Object> getGatewayAccountById(Long id) {
        return jdbi.withHandle(handle ->
                handle
                        .createQuery("SELECT * from gateway_accounts t WHERE t.id = :id")
                        .bind("id", id)
                        .mapToMap()
                        .findFirst()
                        .get()
        );
    }

    public Map<String, Object> getGoCardlessCustomerById(Long id) {
        return jdbi.withHandle(handle ->
                handle
                        .createQuery("SELECT * from gocardless_customers g WHERE g.id = :id")
                        .bind("id", id)
                        .mapToMap()
                        .findFirst()
                        .get()
        );
    }

    public Map<String, Object> getGoCardlessMandateById(Long id) {
        return jdbi.withHandle(handle ->
                handle
                        .createQuery("SELECT * from gocardless_mandates g WHERE g.id = :id")
                        .bind("id", id)
                        .mapToMap()
                        .findFirst()
                        .get()
        );
    }

    public Map<String, Object> getGoCardlessPaymentById(Long id) {
        return jdbi.withHandle(handle ->
                handle
                        .createQuery("SELECT * from gocardless_payments g WHERE g.id = :id")
                        .bind("id", id)
                        .mapToMap()
                        .findFirst()
                        .get()
        );
    }

    public Map<String, Object> getGoCardlessEventById(Long id) {
        return jdbi.withHandle(handle ->
                handle
                        .createQuery("SELECT * from gocardless_events g WHERE g.id = :id")
                        .bind("id", id)
                        .mapToMap()
                        .findFirst()
                        .get()
        );
    }

    public List<Map<String, Object>> getAllGoCardlessEvents() {
        return jdbi.withHandle(handle ->
                handle
                        .createQuery("SELECT * from gocardless_events g")
                        .mapToMap()
                        .list()
        );
    }
}
