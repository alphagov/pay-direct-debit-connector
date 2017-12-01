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

    public void add(TokenFixture token) {
        jdbi.withHandle(handle ->
                handle
                        .createStatement("INSERT INTO tokens(charge_id, secure_redirect_token) VALUES (:charge_id, :secure_redirect_token)")
                        .bind("charge_id", token.getChargeId())
                        .bind("secure_redirect_token", token.getToken())
                        .execute()
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
    public void add(PaymentRequestFixture paymentRequest) {
        jdbi.withHandle(h ->
                h.update(
                        "INSERT INTO" +
                                "    payment_requests(\n" +
                                "        id,\n" +
                                "        external_id,\n" +
                                "        amount,\n" +
                                "        gateway_account_id,\n" +
                                "        return_url,\n" +
                                "        description,\n" +
                                "        created_date,\n" +
                                "        reference\n" +
                                "    )\n" +
                                "   VALUES(?, ?, ?, ?, ?, ?, ?, ?)\n",
                        paymentRequest.getId(),
                        paymentRequest.getExternalId(),
                        paymentRequest.getAmount(),
                        paymentRequest.getGatewayAccountId(),
                        paymentRequest.getReturnUrl(),
                        paymentRequest.getDescription(),
                        Timestamp.from(paymentRequest.getCreatedDate().toInstant()),
                        paymentRequest.getReference()
                )
        );
    }
}
