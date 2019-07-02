package uk.gov.pay.directdebit.mandate.dao;

import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.Query;
import uk.gov.pay.directdebit.mandate.dao.mapper.MandateMapper;
import uk.gov.pay.directdebit.mandate.model.Mandate;
import uk.gov.pay.directdebit.mandate.params.MandateSearchParams;

import javax.inject.Inject;
import java.util.AbstractMap.SimpleEntry;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class MandateSearchDao {
    
    private static final String MANDATE_GATEWAY_ACCOUNT_ID_COLUMN_NAME = "gateway_account_id";
    private static final String MANDATE_SERVICE_REFERENCE_COLUMN_NAME = "service_reference";
    private static final String MANDATE_STATE_COLUMN_NAME = "state";
    private static final String MANDATE_REFERENCE_COLUMN_NAME = "mandate_reference";
    
    private static final String PAYER_NAME_COLUMN_NAME = "name";
    private static final String PAYER_EMAIL_COLUMN_NAME = "email";

    private final Jdbi jdbi;

    private final String QUERY_STRING = "SELECT DISTINCT" +
            "  m.id AS mandate_id," +
            "  m.external_id AS mandate_external_id," +
            format("  m.%s AS mandate_mandate_reference,", MANDATE_REFERENCE_COLUMN_NAME) +
            format("  m.%s AS mandate_service_reference,", MANDATE_SERVICE_REFERENCE_COLUMN_NAME) +
            "  m.gateway_account_id AS mandate_gateway_account_id," +
            "  m.return_url AS mandate_return_url," +
            format("  m.%s AS mandate_state,", MANDATE_STATE_COLUMN_NAME) +
            "  m.created_date AS mandate_created_date," +
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
            format("  p.%s AS payer_name,", PAYER_NAME_COLUMN_NAME) +
            format("  p.%s AS payer_email,", PAYER_EMAIL_COLUMN_NAME) +
            "  p.bank_account_number_last_two_digits AS payer_bank_account_number_last_two_digits," +
            "  p.bank_account_requires_authorisation AS payer_bank_account_requires_authorisation," +
            "  p.bank_account_number AS payer_bank_account_number," +
            "  p.bank_account_sort_code AS payer_bank_account_sort_code," +
            "  p.bank_name AS payer_bank_name," +
            "  p.created_date AS payer_created_date" +
            " FROM mandates m" +
            format("  JOIN gateway_accounts g ON g.id = m.%s ", MANDATE_GATEWAY_ACCOUNT_ID_COLUMN_NAME) +
            "  LEFT JOIN payers p ON p.mandate_id = m.id " +
            "WHERE g.external_id = :gatewayAccountExternalId " +
            "  :searchExtraFields "; 
//            "  ORDER BY p.id DESC OFFSET :offset LIMIT :limit";

    @Inject
    public MandateSearchDao(Jdbi jdbi) {
        this.jdbi = jdbi;
    }

    public List<Mandate> search(MandateSearchParams mandateSearchParams) {
        Params params = new Params(mandateSearchParams);
        String searchExtraFields = generateQuery(params);
        return jdbi.withHandle(handle -> {
            Query query = handle.createQuery(QUERY_STRING.replace(":searchExtraFields", searchExtraFields));
            getQueryMap(params).forEach(query::bind);
            return query.map(new MandateMapper()).list();
        });
    }

    private Map<String, Object> getQueryMap(Params params) {
        Map<String, Object> queryMap = new HashMap<>();
        queryMap.put("gatewayAccountExternalId", params.gatewayAccountExternalId.getValue());
        params.getCaseInsensitivePartialSearchTermsOnMandate().forEach(e -> queryMap.put(e.getKey(), "%" + e.getValue() + "%"));
        params.getCaseInsensitivePartialSearchTermsOnPayer().forEach(e -> queryMap.put(e.getKey(), "%" + e.getValue() + "%"));
        return queryMap;
    }

    private String generateQuery(Params params) {
        StringBuilder sb = new StringBuilder();
        params.getCaseInsensitivePartialSearchTermsOnMandate().forEach(e -> sb.append(format(" AND m.%s ILIKE :%s", e.getKey(), e.getKey())));
        params.getCaseInsensitivePartialSearchTermsOnPayer().forEach(e -> sb.append(format(" AND p.%s ILIKE :%s", e.getKey(), e.getKey())));
        return sb.toString();
    }

    private class Params {

        final SimpleEntry<String, String> gatewayAccountExternalId;
        final SimpleEntry<String, String> reference;
        final SimpleEntry<String, String> mandateState;
        final SimpleEntry<String, String> mandateBankStatementReference;
        final SimpleEntry<String, String> name;
        final SimpleEntry<String, String> email;
//        final AbstractMap.SimpleEntry<String, ZonedDateTime> fromDate;
//        final AbstractMap.SimpleEntry<String, ZonedDateTime> toDate;
//        final AbstractMap.SimpleEntry<String, Integer> page;
//        final AbstractMap.SimpleEntry<String, Integer> displaySize;

        private Params(MandateSearchParams mandateSearchParams) {
            Objects.requireNonNull(mandateSearchParams.getGatewayAccountExternalId());
            
            this.gatewayAccountExternalId = new SimpleEntry<>(MANDATE_GATEWAY_ACCOUNT_ID_COLUMN_NAME, 
                    mandateSearchParams.getGatewayAccountExternalId());
            this.reference = new SimpleEntry<>(MANDATE_SERVICE_REFERENCE_COLUMN_NAME, 
                    mandateSearchParams.getReference());
            this.mandateState = new SimpleEntry<>(MANDATE_STATE_COLUMN_NAME, 
                    Optional.ofNullable(mandateSearchParams.getMandateState()).map(m -> m.name().toLowerCase()).orElse(null));
            this.mandateBankStatementReference = new SimpleEntry<>(MANDATE_REFERENCE_COLUMN_NAME, 
                    Optional.ofNullable(mandateSearchParams.getMandateBankStatementReference()).map(m -> m.toString()).orElse(null));
            this.name = new SimpleEntry<>(PAYER_NAME_COLUMN_NAME, mandateSearchParams.getName());
            this.email = new SimpleEntry<>(PAYER_EMAIL_COLUMN_NAME, mandateSearchParams.getEmail());
//            this.fromDate = new AbstractMap.SimpleEntry<>("created_date", mandateSearchParams.getFromDate());
//            this.toDate = new AbstractMap.SimpleEntry<>("created_date", mandateSearchParams.getToDate());
//            this.page = new AbstractMap.SimpleEntry<>("page;
//            this.displaySize = new AbstractMap.SimpleEntry<>("displaySize;
        }
        
        List<SimpleEntry<String, ?>> getCaseInsensitivePartialSearchTermsOnMandate() {
            return List.of(reference, mandateBankStatementReference)
                    .stream()
                    .filter(e -> isNotBlank(e.getValue()))
                    .collect(Collectors.toList());
        }

        List<SimpleEntry<String, ?>> getCaseInsensitivePartialSearchTermsOnPayer() {
            return List.of(name, email)
                    .stream()
                    .filter(e -> isNotBlank(e.getValue()))
                    .collect(Collectors.toList());
        }
    }
}
