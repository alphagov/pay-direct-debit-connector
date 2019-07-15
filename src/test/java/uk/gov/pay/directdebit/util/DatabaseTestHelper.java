package uk.gov.pay.directdebit.util;

import org.jdbi.v3.core.Jdbi;
import uk.gov.pay.directdebit.mandate.model.subtype.MandateExternalId;

import java.util.List;
import java.util.Map;

public class DatabaseTestHelper {

    private Jdbi jdbi;

    public DatabaseTestHelper(Jdbi jdbi) {
        this.jdbi = jdbi;
    }

    public Map<String, Object> getTokenByMandateExternalId(MandateExternalId externalId) {
        return jdbi.withHandle(handle ->
                handle
                        .createQuery("SELECT t.* from tokens t JOIN mandates m ON t.mandate_id = m.id WHERE m.external_id = :external_id ORDER BY t.id DESC")
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

    public String getTokenByPaymentExternalId(String externalId) {
        return jdbi.withHandle(handle ->
                handle
                        .createQuery("SELECT secure_redirect_token from tokens t JOIN payments p ON p.mandate_id = t.mandate_id WHERE p.external_id = :external_id ORDER BY t.id DESC")
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

    public Map<String, Object> getPaymentById(Long id) {
        return jdbi.withHandle(handle ->
                handle
                        .createQuery("SELECT * from payments p WHERE p.id = :id")
                        .bind("id", id)
                        .mapToMap()
                        .findFirst()
                        .get()
        );
    }

    public Map<String, Object> getPaymentByExternalId(String externalId) {
        return jdbi.withHandle(handle ->
                handle
                        .createQuery("SELECT * from payments p WHERE p.external_id = :externalId")
                        .bind("externalId", externalId)
                        .mapToMap()
                        .findFirst()
                        .get()
        );
    }


    public List<Map<String, Object>> getPaymentsForMandate(MandateExternalId mandateExternalId) {
        return jdbi.withHandle(handle ->
                handle
                        .createQuery("SELECT p.* from payments p JOIN mandates m ON p.mandate_id = m.id WHERE m.external_id = :mandateExternalId")
                        .bind("mandateExternalId", mandateExternalId)
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

    public Map<String, Object> getPayerByMandateExternalId(MandateExternalId externalId) {
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

    public Map<String, Object> getMandateByExternalId(MandateExternalId externalId) {
        return jdbi.withHandle(handle ->
                handle
                        .createQuery("SELECT * from mandates m WHERE m.external_id = :externalId")
                        .bind("externalId", externalId)
                        .mapToMap()
                        .findFirst()
                        .get()
        );
    }

    public Map<String, Object> getMandateByPaymentExternalId(String externalId) {
        return jdbi.withHandle(handle ->
                handle
                        .createQuery("SELECT m.* from mandates m JOIN payments p ON p.mandate_id = m.id WHERE p.external_id = :externalId")
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

    public Map<String, Object> getSandboxEventById(Long id) {
        return jdbi.withHandle(handle ->
                handle
                        .createQuery("SELECT * from sandbox_events g WHERE g.id = :id")
                        .bind("id", id)
                        .mapToMap()
                        .findFirst()
                        .get()
        );
    }

    public Map<String, Object> getGoCardlessPartnerAppTokenById(Long id) {
        return jdbi.withHandle(handle ->
                handle.createQuery("SELECT * FROM gocardless_partner_app_account_connect_tokens g WHERE g.id= :id")
                        .bind("id", id)
                        .mapToMap()
                        .findFirst()
                        .get());
    }

    public Map<String, Object> getGovUkPayEventById(Long id) {
        return jdbi.withHandle(handle ->
                handle
                        .createQuery("SELECT * from govukpay_events g WHERE g.id = :id")
                        .bind("id", id)
                        .mapToMap()
                        .findFirst()
                        .get()
        );
    }

    public void truncateAllData() {
        jdbi.withHandle(h -> h.createScript(
                "TRUNCATE TABLE events CASCADE; " +
                "TRUNCATE TABLE gateway_accounts CASCADE"
        ).execute());
    }
}
