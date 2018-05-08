package uk.gov.pay.directdebit.payments.params;

import java.time.ZonedDateTime;

public class SearchDateParams {
    
    private ZonedDateTime fromDate;
    private ZonedDateTime toDate;
    
    public SearchDateParams(ZonedDateTime fromDateZoned, ZonedDateTime toDateZoned) {
        this.fromDate = fromDateZoned;
        this.toDate = toDateZoned;
    }

    public ZonedDateTime getFromDate() { return fromDate; }

    public ZonedDateTime getToDate() { return toDate; }
}
