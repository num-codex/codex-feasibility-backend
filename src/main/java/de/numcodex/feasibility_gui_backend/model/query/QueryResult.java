package de.numcodex.feasibility_gui_backend.model.query;

import lombok.Data;

@Data
public class QueryResult {
    private String id;
    private long numberOfPatients;
    private String url;
}
