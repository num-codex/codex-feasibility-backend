package de.numcodex.feasibility_gui_backend.api;

import de.numcodex.feasibility_gui_backend.model.query.QueryDefinition;
import de.numcodex.feasibility_gui_backend.model.query.QueryResult;
import de.numcodex.feasibility_gui_backend.model.query.ResultLocation;
import de.numcodex.feasibility_gui_backend.service.QueryBuilderService;
import org.springframework.web.bind.annotation.*;

/*
 Rest Interface for the UI to send queries from the ui to the ui backend.
 */

@RequestMapping("api/v1/querybuilder")
@RestController
@CrossOrigin
public class QueryBuilderRestController {

  private final QueryBuilderService queryBuilderService;

  public QueryBuilderRestController(QueryBuilderService queryBuilderService) {
    this.queryBuilderService = queryBuilderService;
  }


  @PostMapping("run-query")
  public ResultLocation runQuery(QueryDefinition query) {
    return queryBuilderService.runQuery(query);
  }

  @GetMapping(path = "result/{resultLocation}")
  public QueryResult getQueryResult(@PathVariable("resultLocation") String resultLocation) {
    return queryBuilderService.getQueryResult(resultLocation);
  }

}
