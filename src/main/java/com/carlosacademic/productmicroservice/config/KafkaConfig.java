package com.carlosacademic.productmicroservice.config;

import com.carlosacademic.producteventscore.ProductCreatedEvent;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.kafka.transaction.KafkaTransactionManager;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {

    @Value("${kafka.bootstrap-servers}")
    private String bootstrapServers;
    @Value("${kafka.acks}")
    private String acks;
    @Value("${kafka.delivery.timeout}")
    private String deliveryTimeout;
    @Value("${kafka.linger}")
    private String linger;
    @Value("${kafka.request.timeout}")
    private String requestTimeout;
    @Value("${kafka.max-in-flight-request-per-connection}")
    private String maxInFlightRequestPerConnection;

    @Value("${kafka.transaction-id-prefix}")
    private String transactionIdPrefix;

    @Bean
    ProducerFactory<String, ProductCreatedEvent> producerFactory(){
        Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        config.put(ProducerConfig.ACKS_CONFIG, acks);
        config.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, deliveryTimeout);
        config.put(ProducerConfig.LINGER_MS_CONFIG, linger);
        config.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, requestTimeout);
        config.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, maxInFlightRequestPerConnection);
        config.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        //enable transaction
        config.put(ProducerConfig.TRANSACTIONAL_ID_CONFIG, transactionIdPrefix);


        return new DefaultKafkaProducerFactory<>(config);
    }

    @Bean
    KafkaTransactionManager<String, ProductCreatedEvent> kafkaTransactionManager(){
        return new KafkaTransactionManager<>(producerFactory());
    }

    @Bean
    KafkaTemplate<String, ProductCreatedEvent> kafkaTemplate(){
        return new KafkaTemplate<>(producerFactory());
    }

    @Bean
    NewTopic productCreatedEventsTopic(){
        return TopicBuilder.name("product-created-events-topic")
                .partitions(3)
                .replicas(2)
                .configs(Map.of("min.insync.replicas","2"))
                .build();

    }
}
