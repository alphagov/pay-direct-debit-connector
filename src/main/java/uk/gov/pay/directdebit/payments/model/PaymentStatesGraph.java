package uk.gov.pay.directdebit.payments.model;

import com.google.common.graph.ImmutableValueGraph;
import com.google.common.graph.MutableValueGraph;
import com.google.common.graph.ValueGraphBuilder;
import uk.gov.pay.directdebit.payments.exception.InvalidStateTransitionException;

import static java.lang.String.format;
import static uk.gov.pay.directdebit.payments.model.PaymentRequestEvent.SupportedEvent;
import static uk.gov.pay.directdebit.payments.model.PaymentRequestEvent.SupportedEvent.*;
import static uk.gov.pay.directdebit.payments.model.PaymentState.*;

public class PaymentStatesGraph {
    private ImmutableValueGraph<PaymentState, PaymentRequestEvent.SupportedEvent> buildGoCardlessStatesGraph() {
        MutableValueGraph<PaymentState, PaymentRequestEvent.SupportedEvent> graph = ValueGraphBuilder
                .directed()
                .build();

        addNodes(graph, PaymentState.values());

        graph.putEdgeValue(NEW, ENTER_DIRECT_DEBIT_DETAILS, SHOW_ENTER_DIRECT_DEBIT_DETAILS);
        graph.putEdgeValue(NEW, SYSTEM_CANCELLED, SYSTEM_CANCEL);
        graph.putEdgeValue(NEW, EXPIRED, PAYMENT_EXPIRED);

        graph.putEdgeValue(ENTER_DIRECT_DEBIT_DETAILS, CONFIRM_DIRECT_DEBIT_DETAILS, SHOW_CONFIRM);
        graph.putEdgeValue(ENTER_DIRECT_DEBIT_DETAILS, ENTER_DIRECT_DEBIT_DETAILS_FAILED, CREATE_CUSTOMER_FAILED);
        graph.putEdgeValue(ENTER_DIRECT_DEBIT_DETAILS, ENTER_DIRECT_DEBIT_DETAILS_ERROR, CREATE_CUSTOMER_BANK_ACCOUNT_ERROR);
        graph.putEdgeValue(ENTER_DIRECT_DEBIT_DETAILS, SYSTEM_CANCELLED, SYSTEM_CANCEL);
        graph.putEdgeValue(ENTER_DIRECT_DEBIT_DETAILS, USER_CANCELLED, USER_CANCEL);
        graph.putEdgeValue(ENTER_DIRECT_DEBIT_DETAILS, EXPIRED, PAYMENT_EXPIRED);

        graph.putEdgeValue(CONFIRM_DIRECT_DEBIT_DETAILS, REQUESTED, MANDATE_PAYMENT_CREATED);
        graph.putEdgeValue(CONFIRM_DIRECT_DEBIT_DETAILS, CONFIRM_DIRECT_DEBIT_DETAILS_FAILED, CREATE_MANDATE_FAILED);
        graph.putEdgeValue(CONFIRM_DIRECT_DEBIT_DETAILS, CONFIRM_DIRECT_DEBIT_DETAILS_ERROR, CREATE_MANDATE_ERROR);
        graph.putEdgeValue(CONFIRM_DIRECT_DEBIT_DETAILS, SYSTEM_CANCELLED, SYSTEM_CANCEL);
        graph.putEdgeValue(CONFIRM_DIRECT_DEBIT_DETAILS, USER_CANCELLED, USER_CANCEL);
        graph.putEdgeValue(CONFIRM_DIRECT_DEBIT_DETAILS, EXPIRED, PAYMENT_EXPIRED);

        graph.putEdgeValue(REQUESTED, IN_PROGRESS, WEBHOOK_ACTION_PAYMENT_CREATED);
        graph.putEdgeValue(REQUESTED, REQUESTED_FAILED, WEBHOOK_ACTION_CUSTOMER_DENIED);
        graph.putEdgeValue(REQUESTED, REQUESTED_ERROR, WEBHOOK_ACTION_PAYMENT_ERROR);

        graph.putEdgeValue(IN_PROGRESS, SUCCESS, WEBHOOK_ACTION_CONFIRMED);
        graph.putEdgeValue(IN_PROGRESS, IN_PROGRESS_FAILED, WEBHOOK_ACTION_DATA_MISSING);
        graph.putEdgeValue(IN_PROGRESS, PROVIDER_CANCELLED, WEBHOOK_ACTION_CUSTOMER_CANCELLED);

        graph.putEdgeValue(SUCCESS, PAID_OUT, WEBHOOK_ACTION_PAID_OUT);

        return ImmutableValueGraph.copyOf(graph);
    }
    private final ImmutableValueGraph<PaymentState, PaymentRequestEvent.SupportedEvent> goCardlessGraphStates;

    private PaymentStatesGraph() {
        this.goCardlessGraphStates = buildGoCardlessStatesGraph();
    }

    public PaymentState getNextStateForEvent(PaymentState from, SupportedEvent event) {
        return goCardlessGraphStates.successors(from).stream()
                .filter(to -> isValidTransition(from, to, event))
                .findFirst()
                .orElseThrow(() -> new InvalidStateTransitionException(event.toString(), from.toString()));
    }

    public static PaymentState initialState() {
        return NEW;
    }

    public static PaymentStatesGraph getStates() {
        return new PaymentStatesGraph();
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
