package uk.gov.pay.directdebit.events.dao;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.Query;
import uk.gov.pay.directdebit.payments.dao.mapper.EventMapper;
import uk.gov.pay.directdebit.payments.model.DirectDebitEvent;
import uk.gov.pay.directdebit.payments.params.DirectDebitEventSearchParams;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

public class DirectDebitEventDao {

    private static final String QUERY = "SELECT e.id, e.mandate_id, e.transaction_id, e.event_type, e.event, e.event_date, e.version, e.external_id, m.external_id AS mandate_external_id, t.external_id AS transaction_external_id " +
            "FROM events e LEFT OUTER JOIN mandates m ON (e.mandate_id = m.id) " +
            "LEFT OUTER JOIN transactions t ON (e.transaction_id = t.id) " +
            ":searchFields ORDER BY e.id DESC LIMIT :limit OFFSET :offset";
    private static final String COUNT_QUERY = "SELECT count(*) " +
            "FROM events e LEFT OUTER JOIN mandates m ON (e.mandate_id = m.id) " +
            "LEFT OUTER JOIN transactions t ON (e.transaction_id = t.id) " +
            ":searchFields";
    
    private final Jdbi jdbi;

    @Inject
    public DirectDebitEventDao(Jdbi jdbi) {
        this.jdbi = jdbi;
    }
    
    public List<DirectDebitEvent> findEvents(DirectDebitEventSearchParams searchParams) {
        QueryStringAndQueryMap queryStringAndQueryMap = generateQuery(searchParams);
        String limit = isNull(searchParams.getDisplaySize()) ? "NULL" : searchParams.getDisplaySize().toString();
        String offset = searchParams.getPage() == null ? "0" : Integer.toString((searchParams.getPage() - 1) * searchParams.getDisplaySize());
        return jdbi.withHandle(handle -> {
            Query query = handle.createQuery(QUERY
                    .replace(":searchFields", queryStringAndQueryMap.queryString)
                    .replace(":limit", limit)
                    .replace(":offset", offset));
            queryStringAndQueryMap.queryMap.forEach(query::bind);
            return query.map(new EventMapper()).list();
        });
    }

    private QueryStringAndQueryMap generateQuery(DirectDebitEventSearchParams searchParams) {
        List<String> searchStrings = Lists.newArrayList();
        Map<String, Object> queryMap = Maps.newHashMap();
        if (nonNull(searchParams.getMandateExternalId())) {
            searchStrings.add("m.external_id = :mandate_id");
            queryMap.put("mandate_id", searchParams.getMandateExternalId());
        }
        if (nonNull(searchParams.getTransactionExternalId())) {
            searchStrings.add("t.external_id = :transaction_id");
            queryMap.put("transaction_id", searchParams.getTransactionExternalId());
        }
        if (nonNull(searchParams.getToDate())) {
            searchStrings.add("e.event_date < :before_date");
            queryMap.put("before_date", searchParams.getToDate());
        }
        if (nonNull(searchParams.getFromDate())) {
            searchStrings.add("e.event_date >= :after_date");
            queryMap.put("after_date", searchParams.getFromDate());
        }
        String queryString = searchStrings.isEmpty() ? "" : "WHERE " + String.join(" AND ", searchStrings);
        return new QueryStringAndQueryMap(queryString, queryMap);
    }

    public int getTotalNumberOfEvents(DirectDebitEventSearchParams searchParams) {
        QueryStringAndQueryMap queryStringAndQueryMap = generateQuery(searchParams);
        return jdbi.withHandle(handle -> {
            Query query = handle.createQuery(COUNT_QUERY.replace(":searchFields", queryStringAndQueryMap.queryString));
            queryStringAndQueryMap.queryMap.forEach(query::bind);
            return query
                    .mapTo(Integer.class)
                    .findOnly();
        });
    }
    
    public Long insert(DirectDebitEvent directDebitEvent) {
        return (long) jdbi.withHandle(handle -> handle.createUpdate("INSERT INTO events(mandate_id, external_id, transaction_id, event_type, " +
                "event, event_date) VALUES (:mandateId, :externalId, :transactionId, :eventType, :event, :eventDate)")
                .bindBean(directDebitEvent)
                .execute());
    }

    public Optional<DirectDebitEvent> findByMandateIdAndEvent(Long mandateId, DirectDebitEvent.Type eventType, DirectDebitEvent.SupportedEvent event) {
        return jdbi.withHandle(handle -> handle.createQuery("SELECT e.id, e.mandate_id, e.transaction_id, e.event_type, e.event, e.event_date, e.version, e.external_id, " +
                "m.external_id AS mandate_external_id, t.external_id AS transaction_external_id " +
                "FROM events e LEFT OUTER JOIN mandates m ON (e.mandate_id = m.id) " + 
                "LEFT OUTER JOIN transactions t ON (e.transaction_id = t.id) " + 
                "WHERE e.mandate_id = :mandateId and e.event_type = :eventType and e.event = :event")
                .bind("mandateId", mandateId)
                .bind("eventType", eventType)
                .bind("event", event)
                .mapToBean(DirectDebitEvent.class)
                .findFirst());
    }
    
    private class QueryStringAndQueryMap {
        public final String queryString; 
        public final Map<String, Object> queryMap;
        
        QueryStringAndQueryMap(String queryString, Map<String, Object> queryMap) {
            this.queryString = queryString;
            this.queryMap = queryMap;
        }
    }
}
