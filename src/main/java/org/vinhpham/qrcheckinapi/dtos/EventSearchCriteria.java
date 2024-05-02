package org.vinhpham.qrcheckinapi.dtos;

import lombok.Data;

import java.util.List;

@Data
public class EventSearchCriteria {
    private String keyword;
    private List<String> fields;
    private String sortField;
    private Integer categoryId;
    private Boolean isAsc = true;
    private Integer page;
    private Integer limit = 10;
}
