package com.fullstack.commonservice.bmad.nguoi3.command.product;

import org.axonframework.modelling.command.TargetAggregateIdentifier;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProductCommand {

    @TargetAggregateIdentifier
    private String productId;

    private String name;
    private String brand;
    private String category;
    private String description;
    private String imageUrl;
}
