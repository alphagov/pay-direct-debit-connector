package uk.gov.pay.directdebit.payments.params;

import uk.gov.pay.directdebit.payments.exception.UnparsableDateException;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.Map;

public class DirectDebitEventSearchParams {
    private final ZonedDateTime toDate;
    private final ZonedDateTime fromDate;
    private final String mandateExternalId;
    private final String transactionExternalId;
    private final Integer displaySize;
    private final Integer page;

    public DirectDebitEventSearchParamsBuilder copy() {
        return new DirectDebitEventSearchParamsBuilder().page(getPage()).pageSize(getDisplaySize()).transactionExternalId(getTransactionExternalId()).mandateExternalId(getMandateExternalId()).fromDate(getFromDate()).toDate(getToDate());
    }
    
    private DirectDebitEventSearchParams(DirectDebitEventSearchParamsBuilder builder) {
        this.toDate = builder.toDate;
        this.fromDate = builder.fromDate;
        this.mandateExternalId = builder.mandateExternalId;
        this.transactionExternalId = builder.transactionExternalId;
        this.displaySize = builder.displaySize;
        this.page = builder.page;
    }

    public Map<String, String> getParamsAsMap() {
        Map<String, String> params = new LinkedHashMap<>();
        
        if (toDate != null) 
            params.put("to_date", toDate.format(DateTimeFormatter.ISO_INSTANT));
        
        if (fromDate != null)
            params.put("from_date", fromDate.format(DateTimeFormatter.ISO_INSTANT));
        
        if (mandateExternalId != null)
            params.put("mandate_external_id", mandateExternalId);
        
        if (transactionExternalId != null)
            params.put("transaction_external_id", transactionExternalId);

        params.put("page", page.toString());
        params.put("display_size", displaySize.toString());
        
        return params;
    }

    public ZonedDateTime getToDate() {
        return toDate;
    }

    public ZonedDateTime getFromDate() {
        return fromDate;
    }

    public String getMandateExternalId() {
        return mandateExternalId;
    }

    public String getTransactionExternalId() {
        return transactionExternalId;
    }

    public Integer getDisplaySize() {
        return displaySize;
    }

    public Integer getPage() {
        return page;
    }

    public static class DirectDebitEventSearchParamsBuilder {
        
        private Integer displaySize = 500;
        private Integer page = 1;
        private ZonedDateTime toDate;
        private ZonedDateTime fromDate;
        private String mandateExternalId;
        private String transactionExternalId;

        public DirectDebitEventSearchParams build() {
            return new DirectDebitEventSearchParams(this);
        }
        
        public DirectDebitEventSearchParamsBuilder mandateExternalId(String mandateExternalId) {
            this.mandateExternalId = mandateExternalId;
            return this;
        }

        public DirectDebitEventSearchParamsBuilder transactionExternalId(String transactionExternalId) {
            this.transactionExternalId = transactionExternalId;
            return this;
        }

        public DirectDebitEventSearchParamsBuilder toDate(String date) {
            if (date != null) {
                this.toDate = parseDate(date, "toDate");    
            }
            return this;
        }

        public DirectDebitEventSearchParamsBuilder toDate(ZonedDateTime date) {
            this.toDate = date;
            return this;
        }

        public DirectDebitEventSearchParamsBuilder fromDate(String date) {
            if (date != null) {
                this.fromDate = parseDate(date, "fromDate");
            }
            return this;
        }

        public DirectDebitEventSearchParamsBuilder fromDate(ZonedDateTime date) {
            this.fromDate = date;
            return this;
        }

        public DirectDebitEventSearchParamsBuilder page(Integer page) {
            if (page != null) {
                this.page = page;
            }
            return this;
        }

        public DirectDebitEventSearchParamsBuilder pageSize(Integer displaySize) {
            if (displaySize != null && displaySize < 500) {
                this.displaySize = displaySize;
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
