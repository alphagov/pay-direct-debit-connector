package uk.gov.pay.directdebit.payments.params;

import lombok.Builder;
import lombok.Getter;
import uk.gov.pay.directdebit.payments.exception.UnparsableDateException;

import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;

@Builder
public class DirectDebitEventSearchParams {
    @Getter private ZonedDateTime beforeDate;
    @Getter private ZonedDateTime afterDate;
    @Getter private Long mandateId;
    @Getter private Long transactionId;
    @Getter private Integer pageSize;
    @Getter private Integer page;

    public static class DirectDebitEventSearchParamsBuilder {
        public DirectDebitEventSearchParamsBuilder beforeDate(String date) {
            if (date != null) {
                this.beforeDate = parseDate(date, "beforeDate");    
            }
            return this;
        }

        public DirectDebitEventSearchParamsBuilder afterDate(String date) {
            if (date != null) {
                this.afterDate = parseDate(date, "afterDate");    
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
