package uk.gov.pay.directdebit.mandate.params;

import uk.gov.pay.directdebit.common.exception.validation.ValidExternalMandateState;
import uk.gov.pay.directdebit.common.model.SearchParams;
import uk.gov.pay.directdebit.mandate.model.MandateBankStatementReference;
import uk.gov.pay.directdebit.mandate.model.MandateState;

import javax.ws.rs.QueryParam;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class MandateSearchParams extends SearchParams {

    private static final String REFERENCE_KEY = "reference";
    private static final String STATE_KEY = "state";
    private static final String BANK_STATEMENT_REFERENCE_KEY = "bank_statement_reference";
    private static final String NAME_KEY = "name";
    private static final String EMAIL_KEY = "email";

    @QueryParam(REFERENCE_KEY)
    private String serviceReference;

    @QueryParam(STATE_KEY)
    @ValidExternalMandateState(message = "Invalid attribute value: state. Must be a valid mandate external state")
    private String externalMandateState;

    @QueryParam(BANK_STATEMENT_REFERENCE_KEY)
    private MandateBankStatementReference mandateBankStatementReference;

    @QueryParam(NAME_KEY)
    private String name;

    @QueryParam(EMAIL_KEY)
    private String email;

    public MandateSearchParams() {
        super();
    }

    private MandateSearchParams(MandateSearchParamsBuilder builder) {
        super();
        this.serviceReference = builder.serviceReference;
        this.externalMandateState = builder.externalMandateState;
        this.mandateBankStatementReference = builder.mandateBankStatementReference;
        this.name = builder.name;
        this.email = builder.email;
        this.fromDate = builder.fromDate;
        this.toDate = builder.toDate;
        this.page = builder.page;
        this.displaySize = builder.displaySize;
    }

    public Optional<String> getServiceReference() {
        return Optional.ofNullable(serviceReference);
    }

    public Optional<String> getExternalMandateState() {
        return Optional.ofNullable(externalMandateState);
    }

    public Optional<MandateBankStatementReference> getMandateBankStatementReference() {
        return Optional.ofNullable(mandateBankStatementReference);
    }

    public Optional<String> getName() {
        return Optional.ofNullable(name);
    }

    public Optional<String> getEmail() {
        return Optional.ofNullable(email);
    }

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

    public List<MandateState> getInternalStates() {
        return Arrays.stream(MandateState.values())
                .filter(state -> state.toExternal().getState().equals(this.externalMandateState))
                .collect(Collectors.toUnmodifiableList());
    }

    @Override
    public String buildQueryParamString() {
        var query = new StringBuilder();

        query.append(formatQueryParam(PAGE_KEY, page.toString()));
        query.append(appendQueryParam(DISPLAY_SIZE_KEY, displaySize.toString()));

        if (serviceReference != null) {
            query.append(appendQueryParam(REFERENCE_KEY, serviceReference));
        }

        if (externalMandateState != null) {
            query.append(appendQueryParam(STATE_KEY, externalMandateState));
        }

        if (mandateBankStatementReference != null) {
            query.append(appendQueryParam(BANK_STATEMENT_REFERENCE_KEY, mandateBankStatementReference.toString()));
        }

        if (name != null) {
            query.append(appendQueryParam(NAME_KEY, name));
        }

        if (email != null) {
            query.append(appendQueryParam(EMAIL_KEY, email));
        }

        if (fromDate != null) {
            query.append(appendQueryParam(FROM_DATE_KEY, fromDate));
        }

        if (toDate != null) {
            query.append(appendQueryParam(TO_DATE_KEY, toDate));
        }

        return query.toString();
    }

    public static final class MandateSearchParamsBuilder {
        private String serviceReference;
        private String externalMandateState;
        private MandateBankStatementReference mandateBankStatementReference;
        private String name;
        private String email;
        private String fromDate;
        private String toDate;
        private Integer page = 1;
        private Integer displaySize = 500;

        public static MandateSearchParamsBuilder aMandateSearchParams() {
            return new MandateSearchParamsBuilder();
        }

        public MandateSearchParamsBuilder withServiceReference(String serviceReference) {
            this.serviceReference = serviceReference;
            return this;
        }

        public MandateSearchParamsBuilder withExternalMandateState(String externalMandateState) {
            this.externalMandateState = externalMandateState;
            return this;
        }

        public MandateSearchParamsBuilder withMandateBankStatementReference(MandateBankStatementReference mandateBankStatementReference) {
            this.mandateBankStatementReference = mandateBankStatementReference;
            return this;
        }

        public MandateSearchParamsBuilder withName(String name) {
            this.name = name;
            return this;
        }

        public MandateSearchParamsBuilder withEmail(String email) {
            this.email = email;
            return this;
        }

        public MandateSearchParamsBuilder withFromDate(String fromDate) {
            this.fromDate = fromDate;
            return this;
        }

        public MandateSearchParamsBuilder withToDate(String toDate) {
            this.toDate = toDate;
            return this;
        }

        public MandateSearchParamsBuilder withPage(Integer page) {
            this.page = page;
            return this;
        }

        public MandateSearchParamsBuilder withDisplaySize(Integer displaySize) {
            this.displaySize = displaySize;
            return this;
        }

        public MandateSearchParams build() {
            return new MandateSearchParams(this);
        }
    }
}
