package uk.gov.pay.directdebit.events.model;

import com.google.common.graph.GraphBuilder;
import com.google.common.graph.ImmutableGraph;
import com.google.common.graph.MutableGraph;

import static uk.gov.pay.directdebit.events.model.GovUkPayEvent.GovUkPayEventType.MANDATE_CANCELLED_BY_USER;
import static uk.gov.pay.directdebit.events.model.GovUkPayEvent.GovUkPayEventType.MANDATE_CANCELLED_BY_USER_NOT_ELIGIBLE;
import static uk.gov.pay.directdebit.events.model.GovUkPayEvent.GovUkPayEventType.MANDATE_CREATED;
import static uk.gov.pay.directdebit.events.model.GovUkPayEvent.GovUkPayEventType.MANDATE_EXPIRED_BY_SYSTEM;
import static uk.gov.pay.directdebit.events.model.GovUkPayEvent.GovUkPayEventType.MANDATE_SUBMITTED;
import static uk.gov.pay.directdebit.events.model.GovUkPayEvent.GovUkPayEventType.MANDATE_TOKEN_EXCHANGED;
import static uk.gov.pay.directdebit.events.model.GovUkPayEvent.GovUkPayEventType.PAYMENT_SUBMITTED;

public class GovUkPayEventStateGraph {

    private final ImmutableGraph<GovUkPayEvent.GovUkPayEventType> graph;

    public GovUkPayEventStateGraph() {
        this.graph = buildGraph();
    }

    private static ImmutableGraph<GovUkPayEvent.GovUkPayEventType> buildGraph() {
        MutableGraph<GovUkPayEvent.GovUkPayEventType> graph = GraphBuilder.directed().build();

        graph.putEdge(MANDATE_CREATED, MANDATE_TOKEN_EXCHANGED);
        graph.putEdge(MANDATE_CREATED, MANDATE_EXPIRED_BY_SYSTEM);
        graph.putEdge(MANDATE_TOKEN_EXCHANGED, MANDATE_SUBMITTED);
        graph.putEdge(MANDATE_TOKEN_EXCHANGED, MANDATE_CANCELLED_BY_USER);
        graph.putEdge(MANDATE_TOKEN_EXCHANGED, MANDATE_CANCELLED_BY_USER_NOT_ELIGIBLE);
        graph.putEdge(MANDATE_TOKEN_EXCHANGED, MANDATE_EXPIRED_BY_SYSTEM);
        
        graph.addNode(PAYMENT_SUBMITTED);

        return ImmutableGraph.copyOf(graph);
    }

    public boolean isValidTransition(GovUkPayEvent.GovUkPayEventType from, GovUkPayEvent.GovUkPayEventType to) {
        return graph.hasEdgeConnecting(from, to);
    }
    
    public boolean isValidStartValue(GovUkPayEvent.GovUkPayEventType eventType) {
        return graph.predecessors(eventType).isEmpty();
    }
}
