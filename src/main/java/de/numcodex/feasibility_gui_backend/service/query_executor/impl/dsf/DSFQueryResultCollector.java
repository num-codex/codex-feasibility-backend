package de.numcodex.feasibility_gui_backend.service.query_executor.impl.dsf;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import de.numcodex.feasibility_gui_backend.service.query_executor.QueryNotFoundException;
import de.numcodex.feasibility_gui_backend.service.query_executor.QueryStatus;
import de.numcodex.feasibility_gui_backend.service.query_executor.QueryStatusListener;
import de.numcodex.feasibility_gui_backend.service.query_executor.SiteNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.highmed.fhir.client.WebsocketClient;
import org.hl7.fhir.r4.model.DomainResource;

/**
 * Collector for collecting the results of feasibility queries that are running in a distributed fashion.
 * <p>
 * The collector gathers query results from a single FHIR server. Communication with this FHIR server
 * happens using a websocket. The FHIR server sends all task resources that are associated with a subscription.
 */
class DSFQueryResultCollector implements QueryResultCollector {

    private final QueryResultStore store;
    private final FhirContext fhirContext;
    private final FhirWebClientProvider fhirWebClientProvider;
    private final DSFQueryResultHandler resultHandler;
    private final List<QueryStatusListener> listeners;
    private boolean websocketConnectionEstablished;

    /**
     * Creates a new {@link DSFQueryResultCollector}.
     *
     * @param store                 Storage facility for storing collected results.
     * @param fhirContext           The FHIR context used for communication purposes with the FHIR server results are
     *                              gathered from.
     * @param fhirWebClientProvider Provider capable of providing a websocket client.
     * @param resultHandler         Handler able to process query results received from the FHIR server.
     */
    public DSFQueryResultCollector(QueryResultStore store, FhirContext fhirContext,
                                   FhirWebClientProvider fhirWebClientProvider, DSFQueryResultHandler resultHandler) {
        this.store = store;
        this.fhirContext = fhirContext;
        this.fhirWebClientProvider = fhirWebClientProvider;
        this.resultHandler = resultHandler;
        this.websocketConnectionEstablished = false;
        this.listeners = new ArrayList<>();
    }

    private void listenForQueryResults() throws FhirWebClientProvisionException {
        if (!websocketConnectionEstablished) {
            WebsocketClient fhirWebsocketClient = fhirWebClientProvider.provideFhirWebsocketClient();
            fhirWebsocketClient.setDomainResourceHandler(this::setUpQueryResultHandler, this::setUpResourceParser);

            fhirWebsocketClient.connect();
            websocketConnectionEstablished = true;
        }
    }

    private void setUpQueryResultHandler(DomainResource resource) {
        resultHandler.onResult(resource).ifPresent((res) -> {
            store.storeResult(res);
            notifyResultListeners(res);
        });
    }

    private IParser setUpResourceParser() {
        return fhirContext.newJsonParser()
                .setStripVersionsFromReferences(false)
                .setOverrideResourceIdWithBundleEntryFullUrl(false);
    }

    private void notifyResultListeners(DSFQueryResult result) {
        for (QueryStatusListener listener : listeners) {
            listener.onClientUpdate(result.getQueryId(), result.getSiteId(), QueryStatus.COMPLETED);
        }
    }

    @Override
    public void addResultListener(QueryStatusListener listener) throws IOException {
        listeners.add(listener);
        try {
            listenForQueryResults();
        } catch (FhirWebClientProvisionException e) {
            listeners.remove(listener);
            throw new IOException("failed to establish websocket connection to listen for results", e);
        }
    }

    @Override
    public int getResultFeasibility(String queryId, String siteId) throws QueryNotFoundException, SiteNotFoundException {
        return store.getMeasureCount(queryId, siteId);
    }

    @Override
    public List<String> getResultSiteIds(String queryId) throws QueryNotFoundException {
        return store.getSiteIdsWithResult(queryId);
    }

    @Override
    public void removeResults(String queryId) throws QueryNotFoundException {
        store.removeResult(queryId);
    }
}
