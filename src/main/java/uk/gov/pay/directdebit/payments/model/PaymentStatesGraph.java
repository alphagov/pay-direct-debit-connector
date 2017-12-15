package uk.gov.pay.directdebit.payments.model;

import com.google.common.graph.ImmutableValueGraph;
import com.google.common.graph.MutableValueGraph;
import com.google.common.graph.ValueGraphBuilder;
import uk.gov.pay.directdebit.payments.exception.InvalidStateTransitionException;

import static uk.gov.pay.directdebit.payments.model.PaymentRequestEvent.SupportedEvent;
import static uk.gov.pay.directdebit.payments.model.PaymentRequestEvent.SupportedEvent.DIRECT_DEBIT_DETAILS_RECEIVED;
import static uk.gov.pay.directdebit.payments.model.PaymentRequestEvent.SupportedEvent.PAYER_CREATED;
import static uk.gov.pay.directdebit.payments.model.PaymentRequestEvent.SupportedEvent.TOKEN_EXCHANGED;
import static uk.gov.pay.directdebit.payments.model.PaymentState.AWAITING_CONFIRMATION;
import static uk.gov.pay.directdebit.payments.model.PaymentState.AWAITING_DIRECT_DEBIT_DETAILS;
import static uk.gov.pay.directdebit.payments.model.PaymentState.NEW;

public class PaymentStatesGraph {

    private final ImmutableValueGraph<PaymentState, PaymentRequestEvent.SupportedEvent> goCardlessGraphStates;

    private PaymentStatesGraph() {
        this.goCardlessGraphStates = buildGoCardlessStatesGraph();
    }

    public static PaymentState initialState() {
        return NEW;
    }

    public static PaymentStatesGraph getStates() {
        return new PaymentStatesGraph();
    }

    private ImmutableValueGraph<PaymentState, PaymentRequestEvent.SupportedEvent> buildGoCardlessStatesGraph() {
        MutableValueGraph<PaymentState, PaymentRequestEvent.SupportedEvent> graph = ValueGraphBuilder
                .directed()
                .build();

        addNodes(graph, PaymentState.values());

        graph.putEdgeValue(NEW, AWAITING_DIRECT_DEBIT_DETAILS, TOKEN_EXCHANGED);
        graph.putEdgeValue(AWAITING_DIRECT_DEBIT_DETAILS, AWAITING_DIRECT_DEBIT_DETAILS, DIRECT_DEBIT_DETAILS_RECEIVED);
        graph.putEdgeValue(AWAITING_DIRECT_DEBIT_DETAILS, AWAITING_CONFIRMATION, PAYER_CREATED);
        return ImmutableValueGraph.copyOf(graph);
    }

    public PaymentState getNextStateForEvent(PaymentState from, SupportedEvent event) {
        return goCardlessGraphStates.successors(from).stream()
                .filter(to -> isValidTransition(from, to, event))
                .findFirst()
                .orElseThrow(() -> new InvalidStateTransitionException(event.toString(), from.toString()));
    }

    private void addNodes(MutableValueGraph<PaymentState, PaymentRequestEvent.SupportedEvent> graph, PaymentState[] values) {
        for (PaymentState value : values) {
            graph.addNode(value);
        }
    }

    boolean isValidTransition(PaymentState from, PaymentState to, PaymentRequestEvent.SupportedEvent trigger) {
        return goCardlessGraphStates.edgeValue(from, to).filter(trigger::equals).isPresent();
    }
}
