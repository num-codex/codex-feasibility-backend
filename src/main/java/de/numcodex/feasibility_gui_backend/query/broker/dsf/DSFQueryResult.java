package de.numcodex.feasibility_gui_backend.query.broker.dsf;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Holds query result information.
 *
 * The information is related to a specific query (queryId) and an organization that the result is coming from (siteId).
 */
@Data
@AllArgsConstructor
class DSFQueryResult {
    private final String queryId;
    private final String siteId;
    private final int measureCount;
}
