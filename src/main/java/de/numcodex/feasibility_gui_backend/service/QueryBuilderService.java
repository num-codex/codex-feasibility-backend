package de.numcodex.feasibility_gui_backend.service;

import QueryBuilderMoc.QueryBuilder;
import de.numcodex.feasibility_gui_backend.model.query.QueryDefinition;
import de.numcodex.feasibility_gui_backend.model.query.QueryResult;
import de.numcodex.feasibility_gui_backend.model.query.ResultLocation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class QueryBuilderService {

  private final String port;

  public QueryBuilderService(@Value("${server.port}") String port) {
    this.port = port;
  }

  public ResultLocation runQuery(QueryDefinition query) {
    // Create QueryMessage (QueryMetadata + Query)
    // Send to QueryEndpoint/QueryBuilder
    var result = new ResultLocation();
    result.setLocation("http://localhost:" + port + "/api/v1/querybuilder/result/123");
    return result;
  }

  public QueryResult getQueryResult(String resultLocation) {
    // Request QueryResult
    // Optional: Translate for UI
    // Forward to UI

    var result = new QueryResult();
    result.setId("123");
    result.setUrl("http://localhost:${server.port}/api/v1/queryBuilder/result/123");
    result.setNumberOfPatients(Math.round(Math.random() * 1000));
    return result;
  }

  public String getQueryContent(QueryBuilder queryBuilder) {
    return queryBuilder.getQueryContent();
  }
}
