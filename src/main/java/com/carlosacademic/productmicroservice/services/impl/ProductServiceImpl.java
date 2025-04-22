package com.carlosacademic.productmicroservice.services.impl;

import com.carlosacademic.producteventscore.ProductCreatedEvent;
import com.carlosacademic.productmicroservice.model.EventCreationException;
import com.carlosacademic.productmicroservice.model.ProductCreateModel;
import com.carlosacademic.productmicroservice.repositories.ProductEventEntity;
import com.carlosacademic.productmicroservice.repositories.ProductEventEntityRepository;
import com.carlosacademic.productmicroservice.services.ProductService;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
public class ProductServiceImpl implements ProductService {

    private final Logger LOG = LoggerFactory.getLogger(ProductServiceImpl.class);

    private final KafkaTemplate<String, ProductCreatedEvent> kafkaTemplate;

    private final ProductEventEntityRepository productEventEntityRepository;

    public ProductServiceImpl(KafkaTemplate<String, ProductCreatedEvent> kafkaTemplate, ProductEventEntityRepository productEventEntityRepository) {
        this.kafkaTemplate = kafkaTemplate;
        this.productEventEntityRepository = productEventEntityRepository;
    }

    @Transactional(value = "transactionManager", rollbackFor = {EventCreationException.class})
    @Override
    public String createAsync(ProductCreateModel model) {
        String productId = UUID.randomUUID().toString();
        ProducerRecord<String, ProductCreatedEvent> productCreatedEventProducerRecord = getProducerRecord(productId, model);

        saveProductEvent(productId);

        CompletableFuture<SendResult<String, ProductCreatedEvent>> future =
                kafkaTemplate.send(productCreatedEventProducerRecord);

        future.whenComplete((result, exception) -> {
            if (exception!=null){
                LOG.info("Error: {}", exception.getMessage());
                throw new EventCreationException(exception.getMessage());
            }else {
                LOG.info("Product Send!!");
                LOG.info("Partition: {}", result.getRecordMetadata().partition());
                LOG.info("Topic: {}", result.getRecordMetadata().topic());
                LOG.info("Offset: {}", result.getRecordMetadata().offset());
            }
        });

        return productId;
    }

    private void saveProductEvent(String productId) {
        ProductEventEntity productEvent = new ProductEventEntity(productId, LocalDate.now(), "PRODUCT_CREATED");
        try {
            productEventEntityRepository.save(productEvent);
        }catch (Exception e) {
            LOG.info("Error saving product event with id: {}", productId);
            throw new EventCreationException(e.getMessage());
        }
    }

    private ProducerRecord<String, ProductCreatedEvent> getProducerRecord(String productId, ProductCreateModel model) {

        ProductCreatedEvent productCreatedEvent = new ProductCreatedEvent(
                productId, model.title(), model.price(), model.quantity());

        ProducerRecord<String, ProductCreatedEvent> productCreatedEventProducerRecord =
                new ProducerRecord<>("product-created-events-topic", productId, productCreatedEvent);

        productCreatedEventProducerRecord.headers().add("messageId", UUID.randomUUID().toString().getBytes());

        return productCreatedEventProducerRecord;
    }
}
