package com.fullstack.commonservice.bmad.nguoi3.query.product;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FindProductByIdQuery {

    private String productId;
}
