package org.sunhr.es.service;

import org.sunhr.es.model.CrossIndexSearchResult;
import org.sunhr.sql.entity.CMS;

import java.util.List;

/**
 * @author mac
 */
public interface CrossIndexSearchService {
    List<CMS> searchAcrossIndices(String keyword);
}