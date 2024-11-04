package org.sunhr.es.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentPartES {

    private String id;

    private String docId;

    private String kgId;

    private String content;

    private String type;
}

