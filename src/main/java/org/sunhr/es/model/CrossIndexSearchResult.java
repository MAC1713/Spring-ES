package org.sunhr.es.model;

import lombok.Data;
import java.util.List;
import java.util.Map;

/**
 * @author mac
 */
@Data
public class CrossIndexSearchResult {
    private List<Map<String, Object>> infoResults;
    private List<Map<String, Object>> detailResults;
    private List<Map<String, Object>> customResults;
    private List<Map<String, Object>> nodeResults;
    private List<Map<String, Object>> modelFieldResults;
}