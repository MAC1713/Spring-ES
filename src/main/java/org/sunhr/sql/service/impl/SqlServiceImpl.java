package org.sunhr.sql.service.impl;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.sunhr.sql.entity.CMS;
import org.sunhr.sql.mapper.SqlMapper;
import org.sunhr.sql.service.SqlService;

import java.util.List;

/**
 * @author mac
 */
@Service
public class SqlServiceImpl implements SqlService {

    @Resource
    private SqlMapper sqlMapper;

    /**
     * sql数据导入es
     */
    @Override
    public void sql2es() {
        List<CMS> cms;
        int eachTurn = 10;
        Integer maxCount = sqlMapper.countCmsinfo();
        System.out.println("maxCount = " + maxCount);
    }
}
