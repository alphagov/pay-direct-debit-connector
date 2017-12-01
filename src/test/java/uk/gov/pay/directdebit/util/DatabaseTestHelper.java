package uk.gov.pay.directdebit.util;

import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.util.StringColumnMapper;
import uk.gov.pay.directdebit.payments.fixtures.PaymentRequestFixture;
import uk.gov.pay.directdebit.payments.fixtures.TokenFixture;

import java.sql.Timestamp;
import java.util.Map;

public class DatabaseTestHelper {

    private DBI jdbi;

    public DatabaseTestHelper(DBI jdbi) {
        this.jdbi = jdbi;
    }

    public Map<String, Object> getTokenByChargeId(Long chargeId) {
        return jdbi.withHandle(handle ->
                handle
                        .createQuery("SELECT * from tokens t WHERE t.charge_id = :charge_id")
                        .bind("charge_id", chargeId)
                        .first()
        );
    }

    public String getTokenByChargeExternalId(String externalId) {
        return jdbi.withHandle(handle ->
                handle
                        .createQuery("SELECT secure_redirect_token from tokens t JOIN payment_requests p ON p.id = t.charge_id WHERE p.external_id = :external_id ORDER BY t.id DESC")
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

}
