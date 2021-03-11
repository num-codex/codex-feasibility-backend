package de.numcodex.feasibility_gui_backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.numcodex.feasibility_gui_backend.model.db.Query;
import de.numcodex.feasibility_gui_backend.model.db.Result;
import de.numcodex.feasibility_gui_backend.model.query.QueryResult;
import de.numcodex.feasibility_gui_backend.model.query.QueryResultLine;
import de.numcodex.feasibility_gui_backend.model.query.StructuredQuery;
import de.numcodex.feasibility_gui_backend.repository.QueryRepository;
import de.numcodex.feasibility_gui_backend.repository.ResultRepository;
import de.numcodex.feasibility_gui_backend.service.query_builder.QueryBuilder;
import de.numcodex.feasibility_gui_backend.service.query_executor.BrokerClient;
import de.numcodex.feasibility_gui_backend.service.query_executor.QueryNotFoundException;
import de.numcodex.feasibility_gui_backend.service.query_executor.SiteNotFoundException;
import de.numcodex.feasibility_gui_backend.service.query_executor.UnsupportedMediaTypeException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class QueryHandlerService {

  // TODO: Find correct media types
  private static final String MEDIA_TYPE_CQL = "CQL";
  private static final String MEDIA_TYPE_FHIR = "FHIR";

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
  private static final String UNKNOWN_SITE = "Unbekannter Standort";

  @Autowired private QueryRepository queryRepository;

  @Autowired private ResultRepository resultRepository;

  @Qualifier("applied")
  @Autowired
  private BrokerClient brokerClient;

  public String runQuery(StructuredQuery structuredQuery)
      throws UnsupportedMediaTypeException, QueryNotFoundException, IOException {

    String structuredQueryContent = OBJECT_MAPPER.writeValueAsString(structuredQuery);

    var queryId = this.brokerClient.createQuery();
    var query = new Query();
    query.setQueryId(queryId);
    query.setStructuredQuery(OBJECT_MAPPER.readTree(structuredQueryContent));

    this.brokerClient.addQueryDefinition(queryId, MEDIA_TYPE_FHIR, structuredQueryContent);

    String cqlContent = getCqlContent(structuredQuery);
    this.brokerClient.addQueryDefinition(queryId, MEDIA_TYPE_CQL, cqlContent);

    String fhirContent = getFhirContent(structuredQuery);
    this.brokerClient.addQueryDefinition(queryId, MEDIA_TYPE_FHIR, fhirContent);

    this.brokerClient.publishQuery(queryId);
    this.queryRepository.save(query);

    return queryId;
  }

  // TODO: implement using QueryBuilderCql
  private String getCqlContent(StructuredQuery structuredQuery) {
    // return getQueryContent(...);
    return "CQL query";
  }

  // TODO: implement using QueryBuilderFhir
  private String getFhirContent(StructuredQuery structuredQuery) {
    // return getQueryContent(...);
    return "FHIR Search query";
  }

  public QueryResult getQueryResult(String queryId) {
    var resultLines = this.resultRepository.findByQueryId(queryId);
    var result = new QueryResult();

    result.setQueryId(queryId);
    result.setTotalNumberOfPatients(
        resultLines.stream().map(Result::getNumberOfPatients).reduce(0, Integer::sum));

    resultLines.forEach(
        line -> {
          var resultLine = new QueryResultLine();
          resultLine.setNumberOfPatients(line.getNumberOfPatients());
          try {
            resultLine.setSiteName(brokerClient.getSiteName(line.getSiteId()));
          } catch (SiteNotFoundException | IOException e) {
            resultLine.setSiteName(UNKNOWN_SITE);
          }

          result.getResultLines().add(resultLine);
        });

    return result;
  }

  public String getQueryContent(QueryBuilder queryBuilder, StructuredQuery structuredQuery) {
    // TODO: adjust to restructuring
    return queryBuilder.getQueryContent(structuredQuery);
  }
}
