package uk.gov.pay.directdebit.common.model;

import uk.gov.pay.commons.validation.ValidDate;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.QueryParam;
import java.time.ZonedDateTime;
import java.util.Optional;

public abstract class SearchParams {

    protected static final String FROM_DATE_KEY = "from_date";
    protected static final String TO_DATE_KEY = "to_date";
    protected static final String PAGE_KEY = "page";
    protected static final String DISPLAY_SIZE_KEY = "display_size";

    @QueryParam(FROM_DATE_KEY)
    @ValidDate(message = "Invalid attribute value: from_date. Must be a valid date")
    protected String fromDate;

    @QueryParam(TO_DATE_KEY)
    @ValidDate(message = "Invalid attribute value: to_date. Must be a valid date")
    protected String toDate;
    
    @QueryParam(PAGE_KEY)
    @DefaultValue("1")
    @Min(value = 1, message = "Invalid attribute value: page. Must be greater than or equal to {value}")
    protected Integer page = 1;

    @QueryParam(DISPLAY_SIZE_KEY)
    @DefaultValue("500")
    @Min(value = 1, message = "Invalid attribute value: display_size. Must be greater than or equal to {value}")
    @Max(value = 500, message = "Invalid attribute value: display_size. Must be less than or equal to {value}")
    protected Integer displaySize = 500;

    public abstract String buildQueryParamString();

    public Optional<ZonedDateTime> getFromDate() {
        return Optional.ofNullable(fromDate).map(ZonedDateTime::parse);
    }

    public Optional<ZonedDateTime> getToDate() {
        return Optional.ofNullable(toDate).map(ZonedDateTime::parse);
    }

    public Integer getPage() {
        return page;
    }

    public Integer getDisplaySize() {
        return displaySize;
    }

    public int getOffset() {
        return page == 1 ? 0 : (page - 1) * displaySize;
    }

    protected String appendQueryParam(String name, String value) {
        return "&" + formatQueryParam(name, value);
    }

    protected String formatQueryParam(String name, String value) {
        return name + "=" + value;
    }
}
