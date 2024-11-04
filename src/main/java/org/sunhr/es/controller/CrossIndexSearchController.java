package org.sunhr.es.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.sunhr.es.model.CrossIndexSearchResult;
import org.sunhr.es.service.CrossIndexSearchService;
import org.sunhr.sql.entity.CMS;

import java.util.List;

/**
 * @author mac
 */
@RestController
@RequestMapping("/api/search")
public class CrossIndexSearchController {

    @Autowired
    private CrossIndexSearchService crossIndexSearchService;

    @GetMapping("/cross-index")
    public List<CMS> searchAcrossIndices(@RequestParam String keyword) {
        return crossIndexSearchService.searchAcrossIndices(keyword);
    }
}