package uk.gov.pay.directdebit.mandate.params;

import uk.gov.pay.commons.validation.ValidDate;
import uk.gov.pay.directdebit.common.model.SearchParams;
import uk.gov.pay.directdebit.mandate.model.MandateBankStatementReference;
import uk.gov.pay.directdebit.mandate.model.MandateState;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.ws.rs.QueryParam;
import java.time.ZonedDateTime;
import java.util.Optional;

public class MandateSearchParams implements SearchParams {

    private static final String REFERENCE = "reference";
    private static final String STATE = "state";
    private static final String BANK_STATEMENT_REFERENCE = "bank_statement_reference";
    private static final String NAME = "name";
    private static final String EMAIL = "email";
    private static final String FROM_DATE = "from_date";
    private static final String TO_DATE = "to_date";
    private static final String PAGE = "page";
    private static final String DISPLAY_SIZE = "display_size";

    @QueryParam(REFERENCE)
    private String serviceReference;

    @QueryParam(STATE)
    private MandateState mandateState;

    @QueryParam(BANK_STATEMENT_REFERENCE)
    private MandateBankStatementReference mandateBankStatementReference;

    @QueryParam(NAME)
    private String name;

    @QueryParam(EMAIL)
    private String email;

    @QueryParam(FROM_DATE)
    @ValidDate(message = "Invalid attribute value: from_date. Must be a valid date")
    private String fromDate;

    @QueryParam(TO_DATE)
    @ValidDate(message = "Invalid attribute value: to_date. Must be a valid date")
    private String toDate;

    @QueryParam(PAGE)
    @Min(value = 1, message = "Invalid attribute value: page. Must be greater than or equal to {value}")
    private Integer page = 1;

    @QueryParam(DISPLAY_SIZE)
    @Min(value = 1, message = "Invalid attribute value: display_size. Must be greater than or equal to {value}")
    @Max(value = 500, message = "Invalid attribute value: display_size. Must be less than or equal to {value}")
    private Integer displaySize = 500;

    private String gatewayAccountExternalId;

    public Optional<String> getServiceReference() {
        return Optional.ofNullable(serviceReference);
    }

    public Optional<MandateState> getMandateState() {
        return Optional.ofNullable(mandateState);
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

    @Override
    public String getGatewayExternalId() {
        return this.gatewayAccountExternalId;
    }

    @Override
    public String buildQueryParamString() {
        var query = new StringBuilder();

        query.append(createQueryParamFor(PAGE, page.toString()));
        query.append(appendQueryParam(DISPLAY_SIZE, displaySize.toString()));

        if (serviceReference != null) {
            query.append(appendQueryParam(REFERENCE, serviceReference));
        }

        if (mandateState != null) {
            query.append(appendQueryParam(STATE, mandateState.toString()));
        }

        if (mandateBankStatementReference != null) {
            query.append(appendQueryParam(BANK_STATEMENT_REFERENCE, mandateBankStatementReference.toString()));
        }

        if (name != null) {
            query.append(appendQueryParam(NAME, name));
        }

        if (email != null) {
            query.append(appendQueryParam(EMAIL, email));
        }

        if (fromDate != null) {
            query.append(appendQueryParam(FROM_DATE, fromDate));
        }

        if (toDate != null) {
            query.append(appendQueryParam(TO_DATE, toDate));
        }

        return query.toString();
    }

    private String appendQueryParam(String name, String value) {
        return "&" + createQueryParamFor(name, value);
    }

    private String createQueryParamFor(String name, String value) {
        return name + "=" + value;
    }

    public String getGatewayAccountExternalId() {
        return gatewayAccountExternalId;
    }

    public static final class MandateSearchParamsBuilder {
        private String serviceReference;
        private MandateState mandateState;
        private MandateBankStatementReference mandateBankStatementReference;
        private String name;
        private String email;
        private String fromDate;
        private String toDate;
        private Integer page = 1;
        private Integer displaySize = 500;
        private String gatewayAccountExternalId;

        private MandateSearchParamsBuilder(String gatewayAccountExternalId) {
            this.gatewayAccountExternalId = gatewayAccountExternalId;
        }

        public static MandateSearchParamsBuilder aMandateSearchParams(String gatewayAccountExternalId) {
            return new MandateSearchParamsBuilder(gatewayAccountExternalId);
        }

        public MandateSearchParamsBuilder withServiceReference(String serviceReference) {
            this.serviceReference = serviceReference;
            return this;
        }

        public MandateSearchParamsBuilder withMandateState(MandateState mandateState) {
            this.mandateState = mandateState;
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

        public MandateSearchParamsBuilder withGatewayAccountExternalId(String gatewayAccountExternalId) {
            this.gatewayAccountExternalId = gatewayAccountExternalId;
            return this;
        }

        public MandateSearchParams build() {
            MandateSearchParams mandateSearchParams = new MandateSearchParams();
            mandateSearchParams.mandateBankStatementReference = this.mandateBankStatementReference;
            mandateSearchParams.page = this.page;
            mandateSearchParams.serviceReference = this.serviceReference;
            mandateSearchParams.displaySize = this.displaySize;
            mandateSearchParams.toDate = this.toDate;
            mandateSearchParams.name = this.name;
            mandateSearchParams.gatewayAccountExternalId = this.gatewayAccountExternalId;
            mandateSearchParams.email = this.email;
            mandateSearchParams.fromDate = this.fromDate;
            mandateSearchParams.mandateState = this.mandateState;
            return mandateSearchParams;
        }
    }
}
