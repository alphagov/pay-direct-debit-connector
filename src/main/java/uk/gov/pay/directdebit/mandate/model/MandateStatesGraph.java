package uk.gov.pay.directdebit.mandate.model;

import com.google.common.graph.ImmutableValueGraph;
import com.google.common.graph.MutableValueGraph;
import com.google.common.graph.ValueGraphBuilder;
import uk.gov.pay.directdebit.payments.exception.InvalidStateTransitionException;

import static uk.gov.pay.directdebit.mandate.model.MandateState.ACTIVE;
import static uk.gov.pay.directdebit.mandate.model.MandateState.CANCELLED;
import static uk.gov.pay.directdebit.mandate.model.MandateState.FAILED;
import static uk.gov.pay.directdebit.mandate.model.MandateState.PENDING;
import static uk.gov.pay.directdebit.payments.model.PaymentRequestEvent.SupportedEvent;
import static uk.gov.pay.directdebit.payments.model.PaymentRequestEvent.SupportedEvent.MANDATE_ACTIVE;
import static uk.gov.pay.directdebit.payments.model.PaymentRequestEvent.SupportedEvent.MANDATE_CANCELLED;
import static uk.gov.pay.directdebit.payments.model.PaymentRequestEvent.SupportedEvent.MANDATE_FAILED;

public class MandateStatesGraph {

    private final ImmutableValueGraph<MandateState, SupportedEvent> graphStates;

    private MandateStatesGraph() {
        this.graphStates = buildStatesGraph();
    }

    public static MandateState initialState() {
        return PENDING;
    }

    public static MandateStatesGraph getStates() {
        return new MandateStatesGraph();
    }

    private ImmutableValueGraph<MandateState, SupportedEvent> buildStatesGraph() {
        MutableValueGraph<MandateState, SupportedEvent> graph = ValueGraphBuilder
                .directed()
                .build();

        addNodes(graph, MandateState.values());

        graph.putEdgeValue(PENDING, ACTIVE, MANDATE_ACTIVE);
        graph.putEdgeValue(PENDING, FAILED, MANDATE_FAILED);
        graph.putEdgeValue(PENDING, CANCELLED, MANDATE_CANCELLED);
        graph.putEdgeValue(ACTIVE, CANCELLED, MANDATE_CANCELLED);

        return ImmutableValueGraph.copyOf(graph);
    }

    public MandateState getNextStateForEvent(MandateState from, SupportedEvent event) {
        return graphStates.successors(from).stream()
                .filter(to -> isValidTransition(from, to, event))
                .findFirst()
                .orElseThrow(() -> new InvalidStateTransitionException(event.toString(), from.toString()));
    }

    private void addNodes(MutableValueGraph<MandateState, SupportedEvent> graph, MandateState[] values) {
        for (MandateState value : values) {
            graph.addNode(value);
        }
    }

    boolean isValidTransition(MandateState from, MandateState to, SupportedEvent trigger) {
        return graphStates.edgeValue(from, to).filter(trigger::equals).isPresent();
    }
}
