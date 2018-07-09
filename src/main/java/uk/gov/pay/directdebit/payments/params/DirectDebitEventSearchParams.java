package uk.gov.pay.directdebit.payments.params;

import lombok.Builder;
import lombok.Getter;
import uk.gov.pay.directdebit.payments.exception.UnparsableDateException;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;

@Builder
public class DirectDebitEventSearchParams {
    @Getter private final ZonedDateTime beforeDate;
    @Getter private final ZonedDateTime afterDate;
    @Getter private final String mandateExternalId;
    @Getter private final String transactionExternalId;
    @Getter private final Integer pageSize;
    @Getter private final Integer page;

    public DirectDebitEventSearchParamsBuilder copy() {
        return new DirectDebitEventSearchParamsBuilder().page(page).pageSize(pageSize).transactionExternalId(transactionExternalId).mandateExternalId(mandateExternalId).afterDate(afterDate).beforeDate(beforeDate);
    }

    public Map<String, String> getParamsAsMap() {
        Map<String, String> params = new HashMap<>();
        
        if (beforeDate != null) 
            params.put("before", beforeDate.format(DateTimeFormatter.ISO_INSTANT));
        
        if (afterDate != null)
            params.put("after", afterDate.format(DateTimeFormatter.ISO_INSTANT));
        
        if (mandateExternalId != null)
            params.put("mandate_id", mandateExternalId.toString());
        
        if (transactionExternalId != null)
            params.put("transaction_id", transactionExternalId.toString());
        
        params.put("page_size", pageSize.toString());
        params.put("page", page.toString());
        
        return params;
    }

    public static class DirectDebitEventSearchParamsBuilder {
        
        private Integer pageSize = 500;
        private Integer page = 1;
        
        public DirectDebitEventSearchParamsBuilder beforeDate(String date) {
            if (date != null) {
                this.beforeDate = parseDate(date, "beforeDate");    
            }
            return this;
        }

        public DirectDebitEventSearchParamsBuilder beforeDate(ZonedDateTime date) {
            this.beforeDate = date;
            return this;
        }
        
        public DirectDebitEventSearchParamsBuilder afterDate(String date) {
            if (date != null) {
                this.afterDate = parseDate(date, "afterDate");    
            }
            return this;
        }

        public DirectDebitEventSearchParamsBuilder afterDate(ZonedDateTime date) {
            this.afterDate = date;    
            return this;
        }
        
        public DirectDebitEventSearchParamsBuilder page(Integer page) {
            if (page != null) {
                this.page = page;
            }
            return this;
        }

        public DirectDebitEventSearchParamsBuilder pageSize(Integer pageSize) {
            if (pageSize != null && pageSize < 500) {
                this.pageSize = pageSize;
            }
            return this;
        }


        private ZonedDateTime parseDate(String date, String fieldName) {
            ZonedDateTime dateTime;
            try {
                dateTime = ZonedDateTime.parse(date);
            } catch (DateTimeParseException e) {
                throw new UnparsableDateException(fieldName, date);
            }
            return dateTime;
        }
    }
}
