package uk.gov.pay.directdebit.payments.dao;

import org.jdbi.v3.sqlobject.config.RegisterArgumentFactory;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.customizer.BindList;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import uk.gov.pay.directdebit.gatewayaccounts.model.PaymentProvider;
import uk.gov.pay.directdebit.mandate.model.subtype.MandateExternalId;
import uk.gov.pay.directdebit.mandate.model.subtype.MandateExternalIdArgumentFactory;
import uk.gov.pay.directdebit.payments.dao.mapper.PaymentMapper;
import uk.gov.pay.directdebit.payments.model.Payment;
import uk.gov.pay.directdebit.payments.model.PaymentProviderPaymentIdArgumentFactory;
import uk.gov.pay.directdebit.payments.model.PaymentState;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@RegisterRowMapper(PaymentMapper.class)
@RegisterArgumentFactory(MandateExternalIdArgumentFactory.class)
@RegisterArgumentFactory(PaymentProviderPaymentIdArgumentFactory.class)
public interface PaymentDao {

    String joinQuery = "SELECT" +
            "  p.id AS payment_id," +
            "  p.mandate_id AS payment_mandate_id," +
            "  p.external_id AS payment_external_id," +
            "  p.amount AS payment_amount," +
            "  p.state AS payment_state," +
            "  p.state AS payment_state," +
            "  p.description AS payment_description," +
            "  p.reference AS payment_reference," +
            "  p.created_date AS payment_created_date," +
            "  p.payment_provider_id AS payment_provider_id," +
            "  p.charge_date AS payment_charge_date," +
            "  m.id AS mandate_id," +
            "  m.external_id AS mandate_external_id," +
            "  m.mandate_reference AS mandate_mandate_reference," +
            "  m.service_reference AS mandate_service_reference," +
            "  m.gateway_account_id AS mandate_gateway_account_id," +
            "  m.return_url AS mandate_return_url," +
            "  m.state AS mandate_state," +
            "  m.created_date AS mandate_created_date," +
            "  g.id AS gateway_account_id," +
            "  g.external_id AS gateway_account_external_id," +
            "  g.payment_provider AS gateway_account_payment_provider," +
            "  g.type AS gateway_account_type," +
            "  g.description AS gateway_account_description," +
            "  g.analytics_id AS gateway_account_analytics_id," +
            "  g.access_token AS gateway_account_access_token," +
            "  g.organisation AS gateway_account_organisation," +
            "  y.id AS payer_id," +
            "  y.mandate_id AS payer_mandate_id," +
            "  y.external_id AS payer_external_id," +
            "  y.name AS payer_name," +
            "  y.email AS payer_email," +
            "  y.bank_account_number_last_two_digits AS payer_bank_account_number_last_two_digits," +
            "  y.bank_account_requires_authorisation AS payer_bank_account_requires_authorisation," +
            "  y.bank_account_number AS payer_bank_account_number," +
            "  y.bank_account_sort_code AS payer_bank_account_sort_code," +
            "  y.bank_name AS payer_bank_name," +
            "  y.created_date AS payer_created_date" +
            " FROM payments p " +
            "  JOIN mandates m ON p.mandate_id = m.id" +
            "  JOIN gateway_accounts g ON m.gateway_account_id = g.id" +
            "  LEFT JOIN payers y ON y.mandate_id = p.mandate_id";

    @SqlQuery(joinQuery + " WHERE p.id = :id")
    Optional<Payment> findById(@Bind("id") Long id);

    @SqlQuery(joinQuery + " WHERE p.external_id = :externalId")
    Optional<Payment> findByExternalId(@Bind("externalId") String externalId);

    @SqlQuery(joinQuery + " WHERE p.state = :state AND g.payment_provider = :paymentProvider")
    List<Payment> findAllByPaymentStateAndProvider(@Bind("state") PaymentState paymentState, @Bind("paymentProvider") PaymentProvider paymentProvider);

    @SqlQuery(joinQuery + " WHERE m.external_id = :mandateExternalId")
    List<Payment> findAllByMandateExternalId(@Bind("mandateExternalId") MandateExternalId mandateExternalId);

    @SqlUpdate("UPDATE payments p SET state = :state WHERE p.id = :id")
    int updateState(@Bind("id") Long id, @Bind("state") PaymentState paymentState);

    @SqlUpdate("UPDATE payments SET payment_provider_id = :providerId, charge_date = :chargeDate WHERE id = :id")
    int updateProviderIdAndChargeDate(@BindBean Payment payment);

    @SqlUpdate("INSERT INTO payments(mandate_id, external_id, amount, state, description, reference, created_date, payment_provider_id, charge_date)" +
            "VALUES (:mandate.id, :externalId, :amount, :state, :description, :reference, :createdDate, :providerId, :chargeDate)")
    @GetGeneratedKeys
    Long insert(@BindBean Payment payment);

    @SqlQuery(joinQuery + " WHERE p.state IN (<states>) AND p.created_date < :maxDateTime")
    List<Payment> findAllPaymentsBySetOfStatesAndCreationTime(@BindList("states") Set<PaymentState> states, @Bind("maxDateTime") ZonedDateTime maxDateTime);

}
