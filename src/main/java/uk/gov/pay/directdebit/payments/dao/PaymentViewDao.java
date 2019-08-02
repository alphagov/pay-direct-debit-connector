package uk.gov.pay.directdebit.payments.dao;

import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.Query;
import uk.gov.pay.directdebit.payments.api.PaymentResponse;
import uk.gov.pay.directdebit.payments.dao.mapper.PaymentResponseMapper;
import uk.gov.pay.directdebit.payments.params.PaymentViewSearchParams;

import javax.inject.Inject;
import java.util.List;

public class PaymentViewDao {
    private final Jdbi jdbi;
    private final String QUERY_STRING = "SELECT ga.external_id as gateway_external_id, " +
            "p.external_id AS payment_external_id, " +
            "p.amount AS amount, " +
            "p.reference AS reference, " +
            "p.description AS description, " +
            "p.created_date as created_date, " +
            "p.state AS state, " +
            "p.state_details AS state_details, " +
            "p.state_details_description AS state_details_description, " +
            "p.payment_provider_id as provider_id, " +
            "pa.name AS name, " +
            "pa.email AS email, " +
            "ga.payment_provider as payment_provider, " +
            "m.external_id as mandate_external_id " +
            "FROM payments p " +
            "INNER JOIN mandates m ON p.mandate_id = m.id " +
            "INNER JOIN gateway_accounts ga ON ga.id = m.gateway_account_id " +
            "INNER JOIN payers pa ON m.id = pa.mandate_id " +
            "WHERE ga.external_id = :gatewayAccountExternalId " +
            ":searchExtraFields " +
            "ORDER BY p.id DESC OFFSET :offset LIMIT :limit";

    private final String COUNT_QUERY_STRING = "SELECT count(p.id) " +
            "FROM payments p " +
            "INNER JOIN mandates m ON p.mandate_id = m.id " +
            "INNER JOIN gateway_accounts ga ON ga.id = m.gateway_account_id " +
            "INNER JOIN payers pa ON m.id = pa.mandate_id " +
            "WHERE ga.external_id = :gatewayAccountExternalId " +
            ":searchExtraFields ";

    @Inject
    public PaymentViewDao(Jdbi jdbi) {
        this.jdbi = jdbi;
    }

    public List<PaymentResponse> searchPaymentView(PaymentViewSearchParams searchParams) {
        String searchExtraFields = searchParams.generateQuery();
        return jdbi.withHandle(handle -> {
            Query query = handle.createQuery(QUERY_STRING.replace(":searchExtraFields", searchExtraFields));
            searchParams.getQueryMap().forEach(query::bind);
            return query
                    .map(new PaymentResponseMapper())
                    .list();
        });
    }
    
    public Integer getPaymentViewCount(PaymentViewSearchParams searchParams) {
        String searchExtraFields = searchParams.generateQuery();
        return jdbi.withHandle(handle -> {
            Query query = handle.createQuery(COUNT_QUERY_STRING.replace(":searchExtraFields", searchExtraFields));
            searchParams.getQueryMap().forEach(query::bind);
            return query
                    .mapTo(Integer.class)
                    .findOnly();
        });
    }
}
