package org.sunhr.es.service;

import java.util.List;
import java.util.Map;

/**
 * @author mac
 */
public interface TestSearchService {
    List<Map<String, Object>> searchTest(String keyword);
}
