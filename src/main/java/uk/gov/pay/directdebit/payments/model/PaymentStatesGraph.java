package uk.gov.pay.directdebit.payments.model;

import com.google.common.graph.ImmutableValueGraph;
import com.google.common.graph.MutableValueGraph;
import com.google.common.graph.ValueGraphBuilder;

import static uk.gov.pay.directdebit.payments.model.DirectDebitEvent.SupportedEvent;
import static uk.gov.pay.directdebit.payments.model.DirectDebitEvent.SupportedEvent.PAID_OUT;
import static uk.gov.pay.directdebit.payments.model.DirectDebitEvent.SupportedEvent.PAYMENT_CANCELLED_BY_USER;
import static uk.gov.pay.directdebit.payments.model.DirectDebitEvent.SupportedEvent.PAYMENT_CANCELLED_BY_USER_NOT_ELIGIBLE;
import static uk.gov.pay.directdebit.payments.model.DirectDebitEvent.SupportedEvent.PAYMENT_EXPIRED_BY_SYSTEM;
import static uk.gov.pay.directdebit.payments.model.DirectDebitEvent.SupportedEvent.PAYMENT_FAILED;
import static uk.gov.pay.directdebit.payments.model.DirectDebitEvent.SupportedEvent.PAYMENT_SUBMITTED_TO_PROVIDER;
import static uk.gov.pay.directdebit.payments.model.PaymentState.CANCELLED;
import static uk.gov.pay.directdebit.payments.model.PaymentState.EXPIRED;
import static uk.gov.pay.directdebit.payments.model.PaymentState.FAILED;
import static uk.gov.pay.directdebit.payments.model.PaymentState.CREATED;
import static uk.gov.pay.directdebit.payments.model.PaymentState.SUBMITTED_TO_PROVIDER;
import static uk.gov.pay.directdebit.payments.model.PaymentState.USER_CANCEL_NOT_ELIGIBLE;

public class PaymentStatesGraph extends DirectDebitStatesGraph<PaymentState> {

    public static PaymentState initialState() {
        return CREATED;
    }

    public static PaymentStatesGraph getStates() {
        return new PaymentStatesGraph();
    }
    
    @Override
    protected ImmutableValueGraph<PaymentState, DirectDebitEvent.SupportedEvent> buildStatesGraph() {
        MutableValueGraph<PaymentState, SupportedEvent> graph = ValueGraphBuilder
                .directed()
                .build();

        addNodes(graph, PaymentState.values());

        graph.putEdgeValue(CREATED, USER_CANCEL_NOT_ELIGIBLE, PAYMENT_CANCELLED_BY_USER_NOT_ELIGIBLE);
        graph.putEdgeValue(CREATED, CANCELLED, PAYMENT_CANCELLED_BY_USER);
        graph.putEdgeValue(CREATED, SUBMITTED_TO_PROVIDER, PAYMENT_SUBMITTED_TO_PROVIDER);
        
        graph.putEdgeValue(CREATED, EXPIRED, PAYMENT_EXPIRED_BY_SYSTEM);

        graph.putEdgeValue(SUBMITTED_TO_PROVIDER, FAILED, PAYMENT_FAILED);
        graph.putEdgeValue(SUBMITTED_TO_PROVIDER, PaymentState.PAID_OUT, PAID_OUT);

        return ImmutableValueGraph.copyOf(graph);
    }
}
