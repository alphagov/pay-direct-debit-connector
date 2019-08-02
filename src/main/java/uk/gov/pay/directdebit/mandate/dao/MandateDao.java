package uk.gov.pay.directdebit.mandate.dao;

import org.jdbi.v3.sqlobject.config.RegisterArgumentFactory;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.customizer.BindList;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import uk.gov.pay.directdebit.events.model.GoCardlessOrganisationIdArgumentFactory;
import uk.gov.pay.directdebit.gatewayaccounts.model.GoCardlessOrganisationId;
import uk.gov.pay.directdebit.gatewayaccounts.model.PaymentProvider;
import uk.gov.pay.directdebit.mandate.dao.mapper.MandateMapper;
import uk.gov.pay.directdebit.mandate.model.Mandate;
import uk.gov.pay.directdebit.mandate.model.MandateBankStatementReferenceArgumentFactory;
import uk.gov.pay.directdebit.mandate.model.MandateState;
import uk.gov.pay.directdebit.mandate.model.PaymentProviderMandateId;
import uk.gov.pay.directdebit.mandate.model.PaymentProviderMandateIdArgumentFactory;
import uk.gov.pay.directdebit.mandate.model.subtype.MandateExternalId;
import uk.gov.pay.directdebit.mandate.model.subtype.MandateExternalIdArgumentFactory;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@RegisterArgumentFactory(MandateExternalIdArgumentFactory.class)
@RegisterArgumentFactory(MandateBankStatementReferenceArgumentFactory.class)
@RegisterArgumentFactory(PaymentProviderMandateIdArgumentFactory.class)
@RegisterArgumentFactory(GoCardlessOrganisationIdArgumentFactory.class)
@RegisterRowMapper(MandateMapper.class)
public interface MandateDao {

    @SqlUpdate("INSERT INTO mandates (\n" +
            "  external_id,\n" +
            "  gateway_account_id,\n" +
            "  mandate_reference,\n" +
            "  service_reference,\n" +
            "  description,\n" +
            "  state,\n" +
            "  return_url,\n" +
            "  created_date,\n" +
            "  payment_provider_id\n" +
            ") VALUES (\n" +
            "  :externalId,\n" +
            "  :gatewayAccount.id,\n" +
            "  :mandateBankStatementReference,\n" +
            "  :serviceReference,\n" +
            "  :description,\n" +
            "  :state,\n" +
            "  :returnUrl,\n" +
            "  :createdDate," +
            "  :paymentProviderMandateId" +
            ")")
    @GetGeneratedKeys
    Long insert(@BindBean Mandate mandate);

    String query = "SELECT DISTINCT" +
            "  m.id AS mandate_id," +
            "  m.external_id AS mandate_external_id," +
            "  m.mandate_reference AS mandate_mandate_reference," +
            "  m.service_reference AS mandate_service_reference," +
            "  m.gateway_account_id AS mandate_gateway_account_id," +
            "  m.return_url AS mandate_return_url," +
            "  m.state AS mandate_state," +
            "  m.state_details AS mandate_state_details," +
            "  m.state_details_description AS mandate_state_details_description," +
            "  m.created_date AS mandate_created_date," +
            "  m.description AS mandate_description," +
            "  m.payment_provider_id AS mandate_payment_provider_id," +
            "  g.id AS gateway_account_id," +
            "  g.external_id AS gateway_account_external_id," +
            "  g.payment_provider AS gateway_account_payment_provider," +
            "  g.type AS gateway_account_type," +
            "  g.description AS gateway_account_description," +
            "  g.analytics_id AS gateway_account_analytics_id," +
            "  g.access_token AS gateway_account_access_token," +
            "  g.organisation AS gateway_account_organisation," +
            "  p.id AS payer_id," +
            "  p.mandate_id AS payer_mandate_id," +
            "  p.external_id AS payer_external_id," +
            "  p.name AS payer_name," +
            "  p.email AS payer_email," +
            "  p.bank_account_number_last_two_digits AS payer_bank_account_number_last_two_digits," +
            "  p.bank_account_requires_authorisation AS payer_bank_account_requires_authorisation," +
            "  p.bank_account_number AS payer_bank_account_number," +
            "  p.bank_account_sort_code AS payer_bank_account_sort_code," +
            "  p.bank_name AS payer_bank_name," +
            "  p.created_date AS payer_created_date" +
            " FROM mandates m" +
            "  JOIN gateway_accounts g ON g.id = m.gateway_account_id " +
            "  LEFT JOIN payers p ON p.mandate_id = m.id ";


    @SqlQuery(query + "JOIN tokens t ON t.mandate_id = m.id WHERE t.secure_redirect_token = :tokenId")
    Optional<Mandate> findByTokenId(@Bind("tokenId") String tokenId);

    @SqlQuery(query + "WHERE m.id = :mandateId")
    Optional<Mandate> findById(@Bind("mandateId") Long mandateId);

    @SqlQuery(query + "WHERE m.external_id = :mandateExternalId")
    Optional<Mandate> findByExternalId(@Bind("mandateExternalId") MandateExternalId mandateExternalId);

    @SqlQuery(query + "WHERE m.external_id = :mandateExternalId AND g.external_id = :gatewayAccountExternalId")
    Optional<Mandate> findByExternalIdAndGatewayAccountExternalId(@Bind("mandateExternalId") MandateExternalId mandateExternalId,
                                                                  @Bind("gatewayAccountExternalId") String gatewayAccountExternalId);

    @SqlQuery(query + "WHERE m.payment_provider_id = :paymentProviderMandateId AND g.organisation = :goCardlessOrganisationId AND g.payment_provider = :provider")
    Optional<Mandate> findByPaymentProviderMandateIdAndOrganisation(@Bind("provider") PaymentProvider paymentProvider,
                                                                    @Bind("paymentProviderMandateId") PaymentProviderMandateId paymentProviderMandateId,
                                                                    @Bind("goCardlessOrganisationId") GoCardlessOrganisationId goCardlessOrganisationId);

    @SqlQuery(query + "WHERE m.payment_provider_id = :paymentProviderMandateId AND g.organisation IS NULL AND g.payment_provider = :provider")
    Optional<Mandate> findByPaymentProviderMandateId(@Bind("provider") PaymentProvider paymentProvider,
                                                     @Bind("paymentProviderMandateId") PaymentProviderMandateId paymentProviderMandateId);

    @SqlQuery(query + "WHERE m.state IN (<states>) AND m.created_date < :maxDateTime")
    List<Mandate> findAllMandatesBySetOfStatesAndMaxCreationTime(@BindList("states") Set<MandateState> states, @Bind("maxDateTime") ZonedDateTime maxDateTime);

    @SqlUpdate("UPDATE mandates m SET state = :state WHERE m.id = :id")
    int updateState(@Bind("id") Long id, @Bind("state") MandateState mandateState);

    @SqlUpdate("UPDATE mandates SET state = :state, state_details = :stateDetails, state_details_description = :stateDetailsDescription " +
            "WHERE id = :id")
    int updateStateAndDetails(@Bind("id") Long id,
                              @Bind("state") MandateState mandateState,
                              @Bind("stateDetails") String details,
                              @Bind("stateDetailsDescription") String detailsDescription);

    @SqlUpdate("UPDATE mandates m SET mandate_reference = :mandateBankStatementReference, payment_provider_id = :paymentProviderMandateId WHERE m.id = :id")
    int updateReferenceAndPaymentProviderId(@BindBean Mandate mandate);
}
