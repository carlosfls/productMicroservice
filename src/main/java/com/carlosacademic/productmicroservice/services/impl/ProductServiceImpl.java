package com.carlosacademic.productmicroservice.services.impl;

import com.carlosacademic.producteventscore.ProductCreatedEvent;
import com.carlosacademic.productmicroservice.model.ProductCreateModel;
import com.carlosacademic.productmicroservice.services.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final KafkaTemplate<String, ProductCreatedEvent> kafkaTemplate;

    @Override
    public String createAsync(ProductCreateModel model) {
        String productId = UUID.randomUUID().toString();

        //PUBLISH THE EVENT
        ProductCreatedEvent productCreatedEvent = new ProductCreatedEvent(
                productId, model.title(), model.price(), model.quantity());

        ProducerRecord<String, ProductCreatedEvent> productCreatedEventProducerRecord =
                new ProducerRecord<>("product-created-events-topic", productId, productCreatedEvent);

        productCreatedEventProducerRecord.headers().add("messageId", UUID.randomUUID().toString().getBytes());

        CompletableFuture<SendResult<String, ProductCreatedEvent>> future =
                kafkaTemplate.send(productCreatedEventProducerRecord);

        future.whenComplete((result, exception) -> {
            if (exception!=null){
                log.info("Error: {}", exception.getMessage());
            }else {
                log.info("Product Send!!");

                log.info("Partition: {}",result.getRecordMetadata().partition());
                log.info("Topic: {}",result.getRecordMetadata().topic());
                log.info("Offset: {}",result.getRecordMetadata().offset());
            }
        });

        return productId;
    }
}
