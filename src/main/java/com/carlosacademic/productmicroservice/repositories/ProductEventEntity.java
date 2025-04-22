package com.carlosacademic.productmicroservice.repositories;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@NoArgsConstructor
@AllArgsConstructor
public class ProductEventEntity{

        @Id
        private String id;
        LocalDate createdAt;
        String eventType;
}
