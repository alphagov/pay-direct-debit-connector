package uk.gov.pay.directdebit.util;

import java.util.List;
import java.util.Map;
import org.jdbi.v3.core.Jdbi;

public class DatabaseTestHelper {

    private Jdbi jdbi;

    public DatabaseTestHelper(Jdbi jdbi) {
        this.jdbi = jdbi;
    }

    public Map<String, Object> getTokenByMandateExternalId(String externalId) {
        return jdbi.withHandle(handle ->
                handle
                        .createQuery("SELECT * from tokens t JOIN mandates m ON t.mandate_id = m.id WHERE m.external_id = :external_id ORDER BY t.id DESC")
                        .bind("external_id", externalId)
                        .mapToMap()
                        .findFirst()
                        .get()
        );
    }

    public Map<String, Object> getTokenByMandateId(Long mandateId) {
        return jdbi.withHandle(handle ->
                handle
                        .createQuery("SELECT * from tokens t WHERE t.mandate_id = :mandate_id ORDER BY t.id DESC")
                        .bind("mandate_id", mandateId)
                        .mapToMap()
                        .findFirst()
                        .get()
        );
    }
    public String getTokenByTransactionExternalId(String externalId) {
        return jdbi.withHandle(handle ->
                handle
                        .createQuery("SELECT secure_redirect_token from tokens t JOIN transactions r ON r.mandate_id = t.mandate_id WHERE r.external_id = :external_id ORDER BY t.id DESC")
                        .bind("external_id", externalId)
                        .mapTo(String.class)
                        .findFirst()
                        .get()
        );
    }
    
    public Map<String, Object> getEventById(Long id) {
        return jdbi.withHandle(handle ->
                handle
                        .createQuery("SELECT * from events p WHERE p.id = :id")
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

    public Map<String, Object> getTransactionByExternalId(String externalId) {
        return jdbi.withHandle(handle ->
                handle
                        .createQuery("SELECT * from transactions t WHERE t.external_id = :externalId")
                        .bind("externalId", externalId)
                        .mapToMap()
                        .findFirst()
                        .get()
        );
    }
    

    public List<Map<String, Object>> getTransactionsForMandate(Long id) {
        return jdbi.withHandle(handle ->
                handle
                        .createQuery("SELECT * from transactions t JOIN mandates m ON t.mandate_id = m.id WHERE m.id = :id")
                        .bind("id", id)
                        .mapToMap()
                        .list()
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

    public Map<String, Object> getPayerByMandateExternalId(String externalId) {
        return jdbi.withHandle(handle ->
                handle
                        .createQuery("SELECT p.* from payers p INNER JOIN mandates m ON p.mandate_id = m.id WHERE m.external_id = :externalId")
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
    
    public Map<String, Object> getMandateByExternalId(String externalId) {
        return jdbi.withHandle(handle ->
                handle
                        .createQuery("SELECT * from mandates m WHERE m.external_id = :externalId")
                        .bind("externalId", externalId)
                        .mapToMap()
                        .findFirst()
                        .get()
        );
    }

    public Map<String, Object> getMandateByTransactionExternalId(String externalId) {
        return jdbi.withHandle(handle ->
                handle
                        .createQuery("SELECT * from mandates m JOIN transactions t ON t.mandate_id = m.id WHERE t.external_id = :externalId")
                        .bind("externalId", externalId)
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
