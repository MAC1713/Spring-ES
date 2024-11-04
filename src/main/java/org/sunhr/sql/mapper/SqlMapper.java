package org.sunhr.sql.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.sunhr.sql.entity.CMS;

import java.util.List;

/**
 * @author mac
 */
@Mapper
public interface SqlMapper {
    CMS select();
    Integer countCmsinfo();
    List<CMS> selectFromCmsinfo(int offset, int limit);
    void insertToEs(List<CMS> cms);
}
