package com.carlosacademic.productmicroservice.integration;


import com.carlosacademic.producteventscore.ProductCreatedEvent;
import com.carlosacademic.productmicroservice.model.ProductCreateModel;
import com.carlosacademic.productmicroservice.services.ProductService;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DirtiesContext
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
@EmbeddedKafka(partitions = 3, count = 2, controlledShutdown = true)
@SpringBootTest(properties = "kafka.bootstrap-servers=${spring.embedded.kafka.brokers}")
class ProductServiceIntegrationTest {

    @Autowired
    private ProductService productService;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @Autowired
    Environment environment;

    private KafkaMessageListenerContainer<String, ProductCreatedEvent> container;
    private BlockingQueue<ConsumerRecord<String, ProductCreatedEvent>> records;

    @BeforeAll
    void setUp() {
        DefaultKafkaConsumerFactory<String, Object> consumerFactory = new DefaultKafkaConsumerFactory<>(getConsumerProperties());

        ContainerProperties containerProperties = new ContainerProperties(environment.getProperty("kafka.product-created-events-topic-name"));
        container = new KafkaMessageListenerContainer<>(consumerFactory, containerProperties);

        records = new LinkedBlockingQueue<>();

        container.setupMessageListener((MessageListener<String, ProductCreatedEvent>) records::add);

        container.start();

        ContainerTestUtils.waitForAssignment(container, embeddedKafkaBroker.getPartitionsPerTopic());
    }

    @Test
    void testCreateProduct_whenMethodCalled_shouldSendSuccessMessageToKafka() throws InterruptedException {
        //arrange
        String title = "testTitle";
        BigDecimal price = BigDecimal.valueOf(1.5);
        int quantity = 1;

        ProductCreateModel product = new ProductCreateModel(title, price, quantity);

        //act
        productService.createAsync(product);

        //assert
        ConsumerRecord<String, ProductCreatedEvent> message = records.poll(5000, TimeUnit.MILLISECONDS);

        assertNotNull(message, "Message should not be null");
        assertNotNull(message.key(), "Message key should not be null");
        assertEquals(quantity, message.value().quantity());
        assertEquals(title, message.value().title());
        assertEquals(price, message.value().price());
    }

    private Map<String, Object> getConsumerProperties() {
        return Map.of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, embeddedKafkaBroker.getBrokersAsString(),
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class,
                ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class,
                ConsumerConfig.GROUP_ID_CONFIG, environment.getRequiredProperty("kafka.group-id"),
                JsonDeserializer.TRUSTED_PACKAGES, environment.getRequiredProperty("kafka.trusted-packages"),
                ConsumerConfig.ISOLATION_LEVEL_CONFIG, environment.getRequiredProperty("kafka.consumer-isolation-level"),
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, environment.getRequiredProperty("kafka.auto-offset-reset"));
    }

    @AfterAll
    void tearDown() {
        container.stop();
    }
}
