package org.sunhr.sql.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.sunhr.sql.service.SqlService;

/**
 *
 * @author MAC1713
 * @date 2024/10/28 14:06
 */
@RestController
@RequestMapping("/api/sql")
public class SqlController {

    @Autowired
    private SqlService sqlService;

    @GetMapping("/sql2es")
    public void sql2es() {
        sqlService.sql2es();
    }

}
