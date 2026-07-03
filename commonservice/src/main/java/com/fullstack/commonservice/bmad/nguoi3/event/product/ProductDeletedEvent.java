package com.fullstack.commonservice.bmad.nguoi3.event.product;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDeletedEvent {

    private String productId;
    private Instant deletedAt;
}
