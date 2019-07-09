package uk.gov.pay.directdebit.mandate.params;

import uk.gov.pay.commons.validation.ValidDate;
import uk.gov.pay.directdebit.mandate.model.MandateBankStatementReference;
import uk.gov.pay.directdebit.mandate.model.MandateState;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.ws.rs.QueryParam;
import java.time.ZonedDateTime;
import java.util.Optional;

public class MandateSearchParams {

    private String serviceReference;
    private MandateState mandateState;
    private MandateBankStatementReference mandateBankStatementReference;
    private String name;
    private String email;

    @QueryParam("from_date")
    @ValidDate(message = "Invalid attribute value: from_date. Must be a valid date")
    private String fromDate;

    @QueryParam("to_date")
    @ValidDate(message = "Invalid attribute value: to_date. Must be a valid date")
    private String toDate;

    @QueryParam("page")
    @Min(value = 1, message = "Invalid attribute value: page. Must be greater than or equal to {value}")
    private Integer page = 1;

    @QueryParam("display_size")
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

    public int getPage() {
        return page;
    }

    public int getDisplaySize() {
        return displaySize;
    }

    public String getGatewayAccountExternalId() {
        return gatewayAccountExternalId;
    }

    public static MandateSearchParams aMandateSearchParams() {
        return new MandateSearchParams();
    }

    public MandateSearchParams withReference(String serviceReference) {
        this.serviceReference = serviceReference;
        return this;
    }

    public MandateSearchParams withMandateState(MandateState mandateState) {
        this.mandateState = mandateState;
        return this;
    }

    public MandateSearchParams withMandateBankStatementReference(MandateBankStatementReference mandateBankStatementReference) {
        this.mandateBankStatementReference = mandateBankStatementReference;
        return this;
    }

    public MandateSearchParams withName(String name) {
        this.name = name;
        return this;
    }

    public MandateSearchParams withEmail(String email) {
        this.email = email;
        return this;
    }

    public MandateSearchParams withFromDate(ZonedDateTime fromDate) {
        this.fromDate = fromDate.toString();
        return this;
    }

    public MandateSearchParams withToDate(ZonedDateTime toDate) {
        this.toDate = toDate.toString();
        return this;
    }

    public MandateSearchParams withPage(int page) {
        this.page = page;
        return this;
    }

    public MandateSearchParams withDisplaySize(Integer displaySize) {
        this.displaySize = displaySize;
        return this;
    }

    public MandateSearchParams withGatewayAccountId(String gatewayAccountId) {
        this.gatewayAccountExternalId = gatewayAccountId;
        return this;
    }
}
