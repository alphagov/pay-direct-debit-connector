package uk.gov.pay.directdebit.payments.model;

import com.google.common.graph.ImmutableValueGraph;
import com.google.common.graph.MutableValueGraph;
import com.google.common.graph.ValueGraphBuilder;
import uk.gov.pay.directdebit.payments.exception.InvalidStateTransitionException;

import static uk.gov.pay.directdebit.payments.model.PaymentRequestEvent.SupportedEvent;
import static uk.gov.pay.directdebit.payments.model.PaymentRequestEvent.SupportedEvent.DIRECT_DEBIT_DETAILS_CONFIRMED;
import static uk.gov.pay.directdebit.payments.model.PaymentRequestEvent.SupportedEvent.PAID_OUT;
import static uk.gov.pay.directdebit.payments.model.PaymentRequestEvent.SupportedEvent.PAYMENT_CANCELLED_BY_USER;
import static uk.gov.pay.directdebit.payments.model.PaymentRequestEvent.SupportedEvent.PAYMENT_PENDING_WAITING_FOR_PROVIDER;
import static uk.gov.pay.directdebit.payments.model.PaymentRequestEvent.SupportedEvent.PAYMENT_FAILED;
import static uk.gov.pay.directdebit.payments.model.PaymentRequestEvent.SupportedEvent.TOKEN_EXCHANGED;
import static uk.gov.pay.directdebit.payments.model.PaymentRequestEvent.SupportedEvent.PAYMENT_CANCELLED_BY_USER_NOT_ELIGIBLE;
import static uk.gov.pay.directdebit.payments.model.PaymentState.SUBMITTING_DIRECT_DEBIT_PAYMENT;
import static uk.gov.pay.directdebit.payments.model.PaymentState.AWAITING_DIRECT_DEBIT_DETAILS;
import static uk.gov.pay.directdebit.payments.model.PaymentState.CANCELLED;
import static uk.gov.pay.directdebit.payments.model.PaymentState.FAILED;
import static uk.gov.pay.directdebit.payments.model.PaymentState.NEW;
import static uk.gov.pay.directdebit.payments.model.PaymentState.PENDING_DIRECT_DEBIT_PAYMENT;
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

    private ImmutableValueGraph<PaymentState, PaymentRequestEvent.SupportedEvent> buildStatesGraph() {
        MutableValueGraph<PaymentState, SupportedEvent> graph = ValueGraphBuilder
                .directed()
                .build();

        addNodes(graph, PaymentState.values());

        graph.putEdgeValue(NEW, AWAITING_DIRECT_DEBIT_DETAILS, TOKEN_EXCHANGED);

        graph.putEdgeValue(AWAITING_DIRECT_DEBIT_DETAILS, SUBMITTING_DIRECT_DEBIT_PAYMENT, DIRECT_DEBIT_DETAILS_CONFIRMED);
        graph.putEdgeValue(AWAITING_DIRECT_DEBIT_DETAILS, CANCELLED, PAYMENT_CANCELLED_BY_USER);
        graph.putEdgeValue(AWAITING_DIRECT_DEBIT_DETAILS, USER_CANCEL_NOT_ELIGIBLE, PAYMENT_CANCELLED_BY_USER_NOT_ELIGIBLE);

        graph.putEdgeValue(SUBMITTING_DIRECT_DEBIT_PAYMENT, PENDING_DIRECT_DEBIT_PAYMENT,
                PAYMENT_PENDING_WAITING_FOR_PROVIDER);

        graph.putEdgeValue(PENDING_DIRECT_DEBIT_PAYMENT, FAILED, PAYMENT_FAILED);
        graph.putEdgeValue(PENDING_DIRECT_DEBIT_PAYMENT, SUCCESS, PAID_OUT);

        return ImmutableValueGraph.copyOf(graph);
    }

    public PaymentState getNextStateForEvent(PaymentState from, PaymentRequestEvent.SupportedEvent event) {
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

    boolean isValidTransition(PaymentState from, PaymentState to, PaymentRequestEvent.SupportedEvent trigger) {
        return graphStates.edgeValue(from, to).filter(trigger::equals).isPresent();
    }
}
