package com.carlosacademic.productmicroservice.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductEventEntityRepository extends JpaRepository<ProductEventEntity, String> {
}