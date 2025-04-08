package com.carlosacademic.productmicroservice.services;

import com.carlosacademic.productmicroservice.model.ProductCreateModel;

public interface ProductService {

    String createAsync(ProductCreateModel model);
}
