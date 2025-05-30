package com.bys.product.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductStockRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    private Double stock;
}
