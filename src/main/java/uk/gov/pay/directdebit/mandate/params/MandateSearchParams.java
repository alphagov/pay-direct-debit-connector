package uk.gov.pay.directdebit.mandate.params;

import uk.gov.pay.directdebit.mandate.model.MandateBankStatementReference;
import uk.gov.pay.directdebit.mandate.model.MandateState;

import java.time.ZonedDateTime;

public class MandateSearchParams {

    private String reference;
    private MandateState mandateState;
    private MandateBankStatementReference mandateBankStatementReference;
    private String name;
    private String email;
    private ZonedDateTime fromDate;
    private ZonedDateTime toDate;
    private int page;
    private int displaySize;
    private String gatewayAccountExternalId;

    private MandateSearchParams() {
    }

    public String getReference() {
        return reference;
    }

    public MandateState getMandateState() {
        return mandateState;
    }

    public MandateBankStatementReference getMandateBankStatementReference() {
        return mandateBankStatementReference;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public ZonedDateTime getFromDate() {
        return fromDate;
    }

    public ZonedDateTime getToDate() {
        return toDate;
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

    public MandateSearchParams withReference(String reference) {
        this.reference = reference;
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
        this.fromDate = fromDate;
        return this;
    }

    public MandateSearchParams withToDate(ZonedDateTime toDate) {
        this.toDate = toDate;
        return this;
    }

    public MandateSearchParams withPage(int page) {
        this.page = page;
        return this;
    }

    public MandateSearchParams withDisplaySize(int displaySize) {
        this.displaySize = displaySize;
        return this;
    }

    public MandateSearchParams withGatewayAccountId(String gatewayAccountId) {
        this.gatewayAccountExternalId = gatewayAccountId;
        return this;
    }
}
