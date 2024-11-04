package org.sunhr.es.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.sunhr.es.service.TestSearchService;

import java.util.List;
import java.util.Map;

/**
 * @author mac
 */
@RestController
@RequestMapping("/api/test")
public class TestSearchController {

    @Autowired
    private TestSearchService testSearchService;

    @GetMapping("/search")
    public List<Map<String, Object>> search(@RequestParam String keyword) {
        return testSearchService.searchTest(keyword);
    }
}
