package com.fullstack.productservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProductRequest {

    @NotBlank(message = "Tên sản phẩm không được để trống")
    private String name;

    private String brand;
    private String category;
    private String description;
    private String imageUrl;
}
