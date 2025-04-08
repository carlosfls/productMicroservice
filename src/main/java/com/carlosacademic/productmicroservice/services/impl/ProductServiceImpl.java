package com.carlosacademic.productmicroservice.services.impl;

import com.carlosacademic.producteventscore.ProductCreatedEvent;
import com.carlosacademic.productmicroservice.model.ProductCreateModel;
import com.carlosacademic.productmicroservice.services.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final KafkaTemplate<String, ProductCreatedEvent> kafkaTemplate;

    @Override
    public String createAsync(ProductCreateModel model) {
        String productId = UUID.randomUUID().toString();
        //TODO SAVE IN DB

        //PUBLISH THE EVENT
        ProductCreatedEvent productCreatedEvent = new ProductCreatedEvent(
                productId, model.title(), model.price(), model.quantity());

        CompletableFuture<SendResult<String, ProductCreatedEvent>> future =
                kafkaTemplate.send("product-created-events-topic", productId, productCreatedEvent);

        future.whenComplete((result, exception) -> {
            if (exception!=null){
                System.out.println("Error: "+ exception.getMessage());
            }else {
                System.out.println("Product Send!!");

                System.out.println(result.getRecordMetadata().partition());
                System.out.println(result.getRecordMetadata().topic());
                System.out.println(result.getRecordMetadata().offset());

            }
        });

        return productId;
    }
}
