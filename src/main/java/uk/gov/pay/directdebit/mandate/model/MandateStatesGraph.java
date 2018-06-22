package uk.gov.pay.directdebit.mandate.model;

import com.google.common.graph.ImmutableValueGraph;
import com.google.common.graph.MutableValueGraph;
import com.google.common.graph.ValueGraphBuilder;
import uk.gov.pay.directdebit.payments.model.DirectDebitEvent;
import uk.gov.pay.directdebit.payments.model.DirectDebitStatesGraph;

import static uk.gov.pay.directdebit.mandate.model.MandateState.*;
import static uk.gov.pay.directdebit.payments.model.DirectDebitEvent.SupportedEvent;
import static uk.gov.pay.directdebit.payments.model.DirectDebitEvent.SupportedEvent.*;

public class MandateStatesGraph extends DirectDebitStatesGraph<MandateState> {
    

    public static MandateState initialState() {
        return PENDING;
    }
    
    public static MandateStatesGraph getStates() {
        return new MandateStatesGraph();
    }
    
    @Override
    protected ImmutableValueGraph<MandateState, DirectDebitEvent.SupportedEvent> buildStatesGraph() {
        MutableValueGraph<MandateState, SupportedEvent> graph = ValueGraphBuilder
                .directed()
                .build();

        addNodes(graph, MandateState.values());

        graph.putEdgeValue(CREATED, AWAITING_DIRECT_DEBIT_DETAILS, TOKEN_EXCHANGED);
        graph.putEdgeValue(AWAITING_DIRECT_DEBIT_DETAILS, SUBMITTED, DIRECT_DEBIT_DETAILS_CONFIRMED);
        graph.putEdgeValue(AWAITING_DIRECT_DEBIT_DETAILS, CANCELLED, PAYMENT_CANCELLED_BY_USER);
        graph.putEdgeValue(AWAITING_DIRECT_DEBIT_DETAILS, USER_CANCEL_NOT_ELIGIBLE, PAYMENT_CANCELLED_BY_USER_NOT_ELIGIBLE);

        graph.putEdgeValue(SUBMITTED, PENDING, MANDATE_PENDING);
        graph.putEdgeValue(PENDING, ACTIVE, MANDATE_ACTIVE);
        graph.putEdgeValue(PENDING, FAILED, MANDATE_FAILED);
        graph.putEdgeValue(PENDING, CANCELLED, MANDATE_CANCELLED);
        graph.putEdgeValue(ACTIVE, CANCELLED, MANDATE_CANCELLED);

        return ImmutableValueGraph.copyOf(graph);
    }
}
