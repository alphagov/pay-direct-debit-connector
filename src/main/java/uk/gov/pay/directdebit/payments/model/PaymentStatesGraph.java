package uk.gov.pay.directdebit.payments.model;

import com.google.common.graph.ImmutableValueGraph;
import com.google.common.graph.MutableValueGraph;
import com.google.common.graph.ValueGraphBuilder;

import static uk.gov.pay.directdebit.payments.model.DirectDebitEvent.SupportedEvent;
import static uk.gov.pay.directdebit.payments.model.DirectDebitEvent.SupportedEvent.*;
import static uk.gov.pay.directdebit.payments.model.PaymentState.*;

public class PaymentStatesGraph extends DirectDebitStatesGraph<PaymentState> {

    public static PaymentState initialState() {
        return NEW;
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

        graph.putEdgeValue(NEW, USER_CANCEL_NOT_ELIGIBLE, PAYMENT_CANCELLED_BY_USER_NOT_ELIGIBLE);
        graph.putEdgeValue(NEW, CANCELLED, PAYMENT_CANCELLED_BY_USER);
        graph.putEdgeValue(NEW, PENDING, PAYMENT_SUBMITTED_TO_PROVIDER);
        
        graph.putEdgeValue(NEW, EXPIRED, PAYMENT_EXPIRED_BY_SYSTEM);

        graph.putEdgeValue(PENDING, FAILED, PAYMENT_FAILED);
        graph.putEdgeValue(PENDING, SUCCESS, PAID_OUT);

        return ImmutableValueGraph.copyOf(graph);
    }
}
