package com.carlosacademic.productmicroservice.model;

import java.math.BigDecimal;

public record ProductCreateModel(String title, BigDecimal price,int quantity) {
}
