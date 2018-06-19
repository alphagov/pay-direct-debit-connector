package uk.gov.pay.directdebit.payments.model;

import com.google.common.graph.ImmutableValueGraph;
import com.google.common.graph.MutableValueGraph;
import com.google.common.graph.ValueGraphBuilder;
import uk.gov.pay.directdebit.payments.exception.InvalidStateTransitionException;

import static uk.gov.pay.directdebit.payments.model.DirectDebitEvent.SupportedEvent;
import static uk.gov.pay.directdebit.payments.model.DirectDebitEvent.SupportedEvent.PAID_OUT;
import static uk.gov.pay.directdebit.payments.model.DirectDebitEvent.SupportedEvent.PAYMENT_CANCELLED_BY_USER;
import static uk.gov.pay.directdebit.payments.model.DirectDebitEvent.SupportedEvent.PAYMENT_CANCELLED_BY_USER_NOT_ELIGIBLE;
import static uk.gov.pay.directdebit.payments.model.DirectDebitEvent.SupportedEvent.PAYMENT_FAILED;
import static uk.gov.pay.directdebit.payments.model.DirectDebitEvent.SupportedEvent.PAYMENT_SUBMITTED_TO_PROVIDER;
import static uk.gov.pay.directdebit.payments.model.PaymentState.CANCELLED;
import static uk.gov.pay.directdebit.payments.model.PaymentState.FAILED;
import static uk.gov.pay.directdebit.payments.model.PaymentState.NEW;
import static uk.gov.pay.directdebit.payments.model.PaymentState.PENDING;
import static uk.gov.pay.directdebit.payments.model.PaymentState.SUCCESS;
import static uk.gov.pay.directdebit.payments.model.PaymentState.USER_CANCEL_NOT_ELIGIBLE;

public class PaymentStatesGraph {

    private final ImmutableValueGraph<PaymentState, SupportedEvent> graphStates;

    private PaymentStatesGraph() {
        this.graphStates = buildStatesGraph();
    }

    public static PaymentState initialState() {
        return NEW;
    }

    public static PaymentStatesGraph getStates() {
        return new PaymentStatesGraph();
    }

    private ImmutableValueGraph<PaymentState, DirectDebitEvent.SupportedEvent> buildStatesGraph() {
        MutableValueGraph<PaymentState, SupportedEvent> graph = ValueGraphBuilder
                .directed()
                .build();

        addNodes(graph, PaymentState.values());

        graph.putEdgeValue(NEW, USER_CANCEL_NOT_ELIGIBLE, PAYMENT_CANCELLED_BY_USER_NOT_ELIGIBLE);
        graph.putEdgeValue(NEW, CANCELLED, PAYMENT_CANCELLED_BY_USER);

        graph.putEdgeValue(NEW, PENDING,
                PAYMENT_SUBMITTED_TO_PROVIDER);

        graph.putEdgeValue(PENDING, FAILED, PAYMENT_FAILED);
        graph.putEdgeValue(PENDING, SUCCESS, PAID_OUT);

        return ImmutableValueGraph.copyOf(graph);
    }

    public PaymentState getNextStateForEvent(PaymentState from, DirectDebitEvent.SupportedEvent event) {
        return graphStates.successors(from).stream()
                .filter(to -> isValidTransition(from, to, event))
                .findFirst()
                .orElseThrow(() -> new InvalidStateTransitionException(event.toString(), from.toString()));
    }

    private void addNodes(MutableValueGraph<PaymentState, SupportedEvent> graph, PaymentState[] values) {
        for (PaymentState value : values) {
            graph.addNode(value);
        }
    }

    boolean isValidTransition(PaymentState from, PaymentState to, DirectDebitEvent.SupportedEvent trigger) {
        return graphStates.edgeValue(from, to).filter(trigger::equals).isPresent();
    }
}
