package uk.gov.pay.directdebit.payments.dao;

import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import uk.gov.pay.directdebit.payers.model.Payer;
import uk.gov.pay.directdebit.payments.dao.mapper.PaymentRequestMapper;
import uk.gov.pay.directdebit.payments.model.PaymentRequest;

import java.util.Optional;

@RegisterRowMapper(PaymentRequestMapper.class)
public interface PaymentRequestDao {
    @SqlQuery("SELECT"+
            "  p.id AS payment_request_id," +
            "  p.external_id AS payment_request_external_id," +
            "  p.gateway_account_id AS payment_request_gateway_account_id," +
            "  p.amount AS payment_request_amount," +
            "  p.reference AS payment_request_reference," +
            "  p.description AS payment_request_description," +
            "  p.return_url AS payment_request_return_url," +
            "  p.created_date AS payment_request_created_date," +
            "  y.id AS payer_id," +
            "  y.payment_request_id AS payer_payment_request_id," +
            "  y.external_id AS payer_external_id," +
            "  y.name AS payer_name," +
            "  y.email AS payer_email," +
            "  y.bank_account_number_last_two_digits AS payer_bank_account_number_last_two_digits," +
            "  y.bank_account_requires_authorisation AS payer_bank_account_requires_authorisation," +
            "  y.bank_account_number AS payer_bank_account_number," +
            "  y.bank_account_sort_code AS payer_bank_account_sort_code," +
            "  y.bank_name AS payer_bank_name," +
            "  y.created_date AS payer_created_date" +
            " FROM payment_requests p LEFT JOIN payers y ON y.payment_request_id = p.id" +
            " WHERE p.id = :id")
    Optional<PaymentRequest> findById(@Bind("id") Long id);

    @SqlQuery("SELECT" +
            "  p.id AS payment_request_id," +
            "  p.external_id AS payment_request_external_id," +
            "  p.gateway_account_id AS payment_request_gateway_account_id," +
            "  p.amount AS payment_request_amount," +
            "  p.reference AS payment_request_reference," +
            "  p.description AS payment_request_description," +
            "  p.return_url AS payment_request_return_url," +
            "  p.created_date AS payment_request_created_date," +
            "  y.id AS payer_id," +
            "  y.payment_request_id AS payer_payment_request_id," +
            "  y.external_id AS payer_external_id," +
            "  y.name AS payer_name," +
            "  y.email AS payer_email," +
            "  y.bank_account_number_last_two_digits AS payer_bank_account_number_last_two_digits," +
            "  y.bank_account_requires_authorisation AS payer_bank_account_requires_authorisation," +
            "  y.bank_account_number AS payer_bank_account_number," +
            "  y.bank_name AS payer_bank_name," +
            "  y.bank_account_sort_code AS payer_bank_account_sort_code," +
            "  y.created_date AS payer_created_date," +
            "  g.id AS gateway_account_id," +
            "  g.external_id AS gateway_account_external_id," +
            "  g.payment_provider AS gateway_account_payment_provider," +
            "  g.service_name AS gateway_account_service_name," +
            "  g.type AS gateway_account_type," +
            "  g.description AS gateway_account_description," +
            "  g.analytics_id AS gateway_account_analytics_id" +
            " FROM payment_requests p" +
            " JOIN gateway_accounts g ON p.gateway_account_id = g.id" +
            " LEFT JOIN payers y ON y.payment_request_id = p.id" +
            " WHERE p.external_id = :externalId AND g.external_id = :accountExternalId")
    Optional<PaymentRequest> findByExternalIdAndAccountExternalId(@Bind("externalId") String externalId, @Bind("accountExternalId") String accountExternalId);

    @SqlUpdate("INSERT INTO payment_requests(external_id, gateway_account_id, amount, reference, description, return_url, created_date) VALUES (:externalId, :gatewayAccountId, :amount, :reference, :description, :returnUrl, :createdDate)")
    @GetGeneratedKeys
    Long insert(@BindBean PaymentRequest paymentRequest);
}
