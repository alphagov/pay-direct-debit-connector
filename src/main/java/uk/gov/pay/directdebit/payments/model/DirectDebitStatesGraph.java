package uk.gov.pay.directdebit.payments.model;

import com.google.common.graph.ImmutableValueGraph;
import com.google.common.graph.MutableValueGraph;
import uk.gov.pay.directdebit.mandate.model.MandateState;
import uk.gov.pay.directdebit.mandate.model.MandateStatesGraph;
import uk.gov.pay.directdebit.payments.exception.InvalidStateTransitionException;

import java.util.HashSet;
import java.util.Set;

import static uk.gov.pay.directdebit.mandate.model.MandateState.PENDING;

public abstract class DirectDebitStatesGraph<T extends DirectDebitState> {

    public final ImmutableValueGraph<T, DirectDebitEvent.SupportedEvent> graphStates;

    public DirectDebitStatesGraph() {
        this.graphStates = buildStatesGraph();
    }


    protected abstract ImmutableValueGraph<T, DirectDebitEvent.SupportedEvent> buildStatesGraph();

    public Set<T> getPriorStates(T state) {
        return recursiveGetPriorStates(state);
    }

    private Set<T> recursiveGetPriorStates(T state) {
        Set<T> priorStates = new HashSet<>();
        for (T directDebitState : graphStates.asGraph().predecessors(state)) {
            priorStates.add(directDebitState);
            priorStates.addAll(recursiveGetPriorStates(directDebitState));
        }
        return priorStates;
    }

    public T getNextStateForEvent(T from, DirectDebitEvent.SupportedEvent event) {
        return graphStates.successors(from).stream()
                .filter(to -> isValidTransition(from, to, event))
                .findFirst()
                .orElseThrow(() -> new InvalidStateTransitionException(event.toString(), from.toString()));
    }

    protected void addNodes(MutableValueGraph<T, DirectDebitEvent.SupportedEvent> graph, T[] values) {
        for (T value : values) {
            graph.addNode(value);
        }
    }

    public boolean isValidTransition(T from, T to, DirectDebitEvent.SupportedEvent trigger) {
        return graphStates.edgeValue(from, to).filter(trigger::equals).isPresent();
    }
}
