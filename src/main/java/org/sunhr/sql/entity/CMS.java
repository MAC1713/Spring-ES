package org.sunhr.sql.entity;

import java.io.Serial;
import java.util.Date;
import lombok.Data;
import java.io.Serializable;

/**
 * (CmsEs)实体类
 *
 * @author sunhr
 * @since 2024-10-30 10:16:20
 */
@Data
public class CMS implements Serializable {
    @Serial
    private static final long serialVersionUID = -28736950236792191L;

    private Integer fInfoId;

    private Date fPublishDate;

    private Integer fSiteId;

    private String fMetaDescription;

    private String fTitle;

    private String fValues;
}

