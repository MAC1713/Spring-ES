package org.sunhr.es.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.sunhr.es.config.ElasticsearchHandle;

import java.io.IOException;

/**
 * @author mac
 */
@RestController
@RequestMapping("/api/es")
public class TestEsCurdController {

    @Autowired
    public ElasticsearchHandle elasticsearchHandle;

    @GetMapping("/index")
    public boolean searchIndex(String indexName) throws IOException {
        return elasticsearchHandle.hasIndex(indexName);
    }

}

