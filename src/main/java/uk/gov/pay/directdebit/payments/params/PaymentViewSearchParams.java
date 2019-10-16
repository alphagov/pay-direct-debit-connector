package uk.gov.pay.directdebit.payments.params;

import uk.gov.pay.directdebit.common.exception.validation.ValidExternalPaymentState;
import uk.gov.pay.directdebit.common.model.SearchParams;
import uk.gov.pay.directdebit.payments.model.PaymentState;

import javax.ws.rs.QueryParam;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class PaymentViewSearchParams extends SearchParams {

    private static final String REFERENCE_KEY = "reference";
    private static final String AMOUNT_KEY = "amount";
    private static final String STATE_KEY = "state";
    private static final String MANDATE_ID_KEY = "mandate_id";

    @QueryParam(REFERENCE_KEY)
    private String reference;

    @QueryParam(AMOUNT_KEY)
    private Long amount;

    @QueryParam(MANDATE_ID_KEY)
    private String mandateId;

    @QueryParam(STATE_KEY)
    @ValidExternalPaymentState(message = "Invalid attribute value: state. Must be a valid payment external state")
    private String state;

    public PaymentViewSearchParams() {
        super();
    }

    private PaymentViewSearchParams(PaymentViewSearchParamsBuilder builder) {
        super();
        this.page = builder.page;
        this.displaySize = builder.displaySize;
        this.fromDate = builder.fromDateString;
        this.toDate = builder.toDateString;
        this.reference = builder.reference;
        this.amount = builder.amount;
        this.mandateId = builder.mandateId;
        this.state = builder.state;
    }

    public Optional<String> getReference() {
        return Optional.ofNullable(reference);
    }

    public Optional<Long> getAmount() {
        return Optional.ofNullable(amount);
    }

    public Optional<String> getMandateId() {
        return Optional.ofNullable(mandateId);
    }

    public Optional<String> getState() {
        return Optional.ofNullable(state);
    }

    public List<PaymentState> getInternalStates() {
        return Arrays.stream(PaymentState.values())
                .filter(paymentState -> paymentState.toExternal().getStatus().equals(this.state))
                .collect(Collectors.toUnmodifiableList());
    }

    public String buildQueryParamString() {
        var paramsList = new ArrayList<String>();
        
        if (isNotBlank(mandateId)) {
            paramsList.add(formatQueryParam(MANDATE_ID_KEY, mandateId));
        }

        if (isNotBlank(reference)) {
            paramsList.add(formatQueryParam(REFERENCE_KEY, reference));
        }

        if (amount != null) {
            paramsList.add(formatQueryParam(AMOUNT_KEY, amount.toString()));
        }

        if (isNotBlank(state)) {
            paramsList.add(formatQueryParam(STATE_KEY, state));
        }

        if (fromDate != null) {
            paramsList.add(formatQueryParam(FROM_DATE_KEY, fromDate));
        }

        if (toDate != null) {
            paramsList.add(formatQueryParam(TO_DATE_KEY, toDate));
        }

        paramsList.add(formatQueryParam(PAGE_KEY, page.toString()));
        paramsList.add(formatQueryParam(DISPLAY_SIZE_KEY, displaySize.toString()));

        return String.join("&", paramsList);
    }

    public static final class PaymentViewSearchParamsBuilder {
        private Integer page = 1;
        private Integer displaySize = 500;
        private String fromDateString;
        private String toDateString;
        private String reference;
        private Long amount;
        private String mandateId;
        private String state;

        public static PaymentViewSearchParamsBuilder aPaymentViewSearchParams() {
            return new PaymentViewSearchParamsBuilder();
        }

        public PaymentViewSearchParamsBuilder withPage(Integer page) {
            this.page = page;
            return this;
        }

        public PaymentViewSearchParamsBuilder withDisplaySize(Integer displaySize) {
            this.displaySize = displaySize;
            return this;
        }

        public PaymentViewSearchParamsBuilder withFromDateString(String fromDateString) {
            this.fromDateString = fromDateString;
            return this;
        }

        public PaymentViewSearchParamsBuilder withToDateString(String toDateString) {
            this.toDateString = toDateString;
            return this;
        }

        public PaymentViewSearchParamsBuilder withReference(String reference) {
            this.reference = reference;
            return this;
        }

        public PaymentViewSearchParamsBuilder withAmount(Long amount) {
            this.amount = amount;
            return this;
        }

        public PaymentViewSearchParamsBuilder withMandateId(String mandateId) {
            this.mandateId = mandateId;
            return this;
        }

        public PaymentViewSearchParamsBuilder withState(String state) {
            this.state = state;
            return this;
        }

        public PaymentViewSearchParams build() {
            return new PaymentViewSearchParams(this);
        }
    }
}
