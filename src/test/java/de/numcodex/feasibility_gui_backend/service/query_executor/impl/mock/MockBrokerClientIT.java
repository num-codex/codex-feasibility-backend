package de.numcodex.feasibility_gui_backend.service.query_executor.impl.mock;

import de.numcodex.feasibility_gui_backend.service.query_executor.QueryNotFoundException;
import de.numcodex.feasibility_gui_backend.service.query_executor.QueryStatusListener;
import de.numcodex.feasibility_gui_backend.service.query_executor.SiteNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static de.numcodex.feasibility_gui_backend.service.query_executor.QueryStatus.COMPLETED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

public class MockBrokerClientIT {

    private static final int ASYNC_TIMEOUT_WAIT_MS = 9000;

    MockBrokerClient client;

    @BeforeEach
    void setUp() {
        client = new MockBrokerClient();
    }


    @Test
    void testPublishQuery() throws QueryNotFoundException, SiteNotFoundException {
        var queryId = client.createQuery();

        var statusListener = mock(QueryStatusListener.class);
        client.addQueryStatusListener(statusListener);
        client.publishQuery(queryId);

        verify(statusListener, timeout(ASYNC_TIMEOUT_WAIT_MS)).onClientUpdate(queryId, "1", COMPLETED);
        verify(statusListener, timeout(ASYNC_TIMEOUT_WAIT_MS)).onClientUpdate(queryId, "2", COMPLETED);
        verify(statusListener, timeout(ASYNC_TIMEOUT_WAIT_MS)).onClientUpdate(queryId, "3", COMPLETED);
        verify(statusListener, timeout(ASYNC_TIMEOUT_WAIT_MS)).onClientUpdate(queryId, "4", COMPLETED);

        assertEquals(4, client.getResultSiteIds(queryId).size());
        assertTrue(client.getResultFeasibility(queryId, "1") >= 10);
        assertTrue(client.getResultFeasibility(queryId, "2") >= 10);
        assertTrue(client.getResultFeasibility(queryId, "3") >= 10);
        assertTrue(client.getResultFeasibility(queryId, "4") >= 10);
    }

    @Test
    void testCloseQueryWhichIsRunning() throws QueryNotFoundException {
        var queryId = client.createQuery();

        var statusListener = mock(QueryStatusListener.class);
        client.addQueryStatusListener(statusListener);
        client.publishQuery(queryId);
        client.closeQuery(queryId);

        verify(statusListener, never()).onClientUpdate(queryId, "1", COMPLETED);
        verify(statusListener, never()).onClientUpdate(queryId, "2", COMPLETED);
        verify(statusListener, never()).onClientUpdate(queryId, "3", COMPLETED);
        verify(statusListener, never()).onClientUpdate(queryId, "4", COMPLETED);
    }
}
