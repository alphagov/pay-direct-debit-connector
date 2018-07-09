package uk.gov.pay.directdebit.events.dao;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.AllArgsConstructor;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.Query;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
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

public class DirectDebitEventSearchDao {

    private static final String QUERY = "SELECT * from EVENTS e :searchFields ORDER BY e.id DESC LIMIT :limit OFFSET :offset";
    private static final String COUNT_QUERY = "SELECT count(*) from EVENTS e :searchFields";
    
    private final Jdbi jdbi;

    @Inject
    public DirectDebitEventSearchDao(Jdbi jdbi) {
        this.jdbi = jdbi;
    }
    
    public List<DirectDebitEvent> findEvents(DirectDebitEventSearchParams searchParams) {
        QueryStringAndQueryMap queryStringAndQueryMap = generateQuery(searchParams);
        String limit = isNull(searchParams.getPageSize()) ? "NULL" : searchParams.getPageSize().toString();
        String offset = searchParams.getPage() == null ? "0" : Integer.toString((searchParams.getPage() - 1) * searchParams.getPageSize());
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
        if (nonNull(searchParams.getMandateId())) {
            searchStrings.add("e.mandate_id = :mandate_id");
            queryMap.put("mandate_id", searchParams.getMandateId());
        }
        if (nonNull(searchParams.getTransactionId())) {
            searchStrings.add("e.transaction_id = :transaction_id");
            queryMap.put("transaction_id", searchParams.getTransactionId());
        }
        if (nonNull(searchParams.getBeforeDate())) {
            searchStrings.add("e.event_date < :before_date");
            queryMap.put("before_date", searchParams.getBeforeDate());
        }
        if (nonNull(searchParams.getAfterDate())) {
            searchStrings.add("e.event_date > :after_date");
            queryMap.put("after_date", searchParams.getAfterDate());
        }
        String queryString = searchStrings.isEmpty() ? "" : "WHERE " + searchStrings.stream().collect(Collectors.joining(" AND "));
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
        return (long) jdbi.withHandle(handle -> {
            return handle.createUpdate("INSERT INTO events(mandate_id, external_id, transaction_id, event_type, " +
                    "event, event_date) VALUES (:mandateId, :externalId, :transactionId, :eventType, :event, :eventDate)")
                    .bindBean(directDebitEvent)
                    .execute();
        });
    }

    public Optional<DirectDebitEvent> findByMandateIdAndEvent(Long mandateId, DirectDebitEvent.Type eventType, DirectDebitEvent.SupportedEvent event) {
        return jdbi.withHandle(handle -> handle.createQuery("SELECT e.id, e.mandate_id, e.transaction_id, e.event_type, e.event, e.event_date, e.version, m.external_id " +
                "FROM events e WHERE e.mandate_id = :mandateId and " +
                "e.event_type = :eventType and e.event = :event INNER JOIN mandates m ON (e.mandate_id = m.id)")
                .bind("mandateId", mandateId)
                .bind("eventType", eventType)
                .bind("event", event)
                .mapToBean(DirectDebitEvent.class)
                .findFirst());
    }
    
    @AllArgsConstructor
    private class QueryStringAndQueryMap {
        public final String queryString; 
        public final Map<String, Object> queryMap;
    }
}
