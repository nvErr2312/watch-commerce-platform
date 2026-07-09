package com.fullstack.commonservice.bmad.nguoi3.query.product;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchProductQuery {

    private String keyword;
    private String brand;
    private String category;
    private int page;
    private int size;
}
