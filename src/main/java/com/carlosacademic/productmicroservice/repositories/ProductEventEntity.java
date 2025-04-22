package com.carlosacademic.productmicroservice.repositories;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class ProductEventEntity{

        @Id
        private String id;
        LocalDate createdAt;
        String eventType;
}
