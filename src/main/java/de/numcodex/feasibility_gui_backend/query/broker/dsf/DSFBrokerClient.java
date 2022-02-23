package de.numcodex.feasibility_gui_backend.query.broker.dsf;


import de.numcodex.feasibility_gui_backend.query.broker.BrokerClient;
import de.numcodex.feasibility_gui_backend.query.broker.QueryNotFoundException;
import de.numcodex.feasibility_gui_backend.query.broker.SiteNotFoundException;
import de.numcodex.feasibility_gui_backend.query.broker.UnsupportedMediaTypeException;
import de.numcodex.feasibility_gui_backend.query.collect.QueryStatusListener;
import de.numcodex.feasibility_gui_backend.query.persistence.BrokerClientType;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static de.numcodex.feasibility_gui_backend.query.persistence.BrokerClientType.DSF;

/**
 * A {@link BrokerClient} to be used in conjunction with a Data Sharing Framework (DSF) instance.
 */
public final class DSFBrokerClient implements BrokerClient {
    private final QueryManager queryManager;
    private final QueryResultCollector queryResultCollector;
    private final Map<String, Long> brokerToBackendQueryIdMapping;

    /**
     * Creates a new {@link DSFBrokerClient} instance.
     *
     * @param queryManager         Manager capable of setting up queries and managing them throughout their lifecycle.
     * @param queryResultCollector Collector for collecting results of running feasibility queries.
     */
    public DSFBrokerClient(QueryManager queryManager, QueryResultCollector queryResultCollector) {
        this.queryManager = queryManager;
        this.queryResultCollector = queryResultCollector;
        brokerToBackendQueryIdMapping = new HashMap<>();
    }

    @Override
    public BrokerClientType getBrokerType() {
        return DSF;
    }

    @Override
    public void addQueryStatusListener(QueryStatusListener queryStatusListener) throws IOException {
        queryResultCollector.addResultListener(this, queryStatusListener);
    }

    @Override
    public String createQuery(Long backendQueryId) {
        var brokerQueryId = queryManager.createQuery();
        brokerToBackendQueryIdMapping.put(brokerQueryId, backendQueryId);

        return brokerQueryId;
    }

    @Override
    public void addQueryDefinition(String brokerQueryId, String mediaType, String content) throws QueryNotFoundException,
            UnsupportedMediaTypeException {
        queryManager.addQueryDefinition(brokerQueryId, mediaType, content);
    }

    @Override
    public void publishQuery(String brokerQueryId) throws QueryNotFoundException, IOException {
        queryManager.publishQuery(brokerQueryId);
    }

    @Override
    public void closeQuery(String brokerQueryId) throws QueryNotFoundException {
        queryManager.removeQuery(brokerQueryId);
        queryResultCollector.removeResults(brokerQueryId);
    }

    @Override
    public int getResultFeasibility(String brokerQueryId, String siteId) throws QueryNotFoundException, SiteNotFoundException {
        return queryResultCollector.getResultFeasibility(brokerQueryId, siteId);
    }

    @Override
    public List<String> getResultSiteIds(String brokerQueryId) throws QueryNotFoundException {
        return queryResultCollector.getResultSiteIds(brokerQueryId);
    }

    @Override
    public String getSiteName(String siteId) throws SiteNotFoundException {
        // TODO: replace this identity function with a real resolve routine later on.
        if (siteId == null) {
            throw new SiteNotFoundException("cannot find site name of site id with value 'null'");
        }
        return siteId;
    }

    Long getBackendQueryId(String brokerQueryId) {
        return brokerToBackendQueryIdMapping.get(brokerQueryId);
    }
}
