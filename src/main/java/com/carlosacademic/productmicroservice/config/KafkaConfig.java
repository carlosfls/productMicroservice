package com.carlosacademic.productmicroservice.config;

import com.carlosacademic.producteventscore.ProductCreatedEvent;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {

    @Value("${spring.kafka.producer.bootstrap-servers}")
    private String bootstrapServers;
    @Value("${spring.kafka.producer.key-serializer}")
    private String keySerializer;
    @Value("${spring.kafka.producer.value-serializer}")
    private String valueSerializer;
    @Value("${spring.kafka.producer.acks}")
    private String acks;
    @Value("${spring.kafka.producer.properties.delivery.timeout.ms}")
    private String deliveryTimeout;
    @Value("${spring.kafka.producer.properties.linger.ms}")
    private String linger;
    @Value("${spring.kafka.producer.properties.request.timeout.ms}")
    private String requestTimeout;
    @Value("${spring.kafka.producer.properties.max.in.flight.request.per.connection}")
    private String maxInFlightRequestPerConnection;

    @Bean
    ProducerFactory<String, ProductCreatedEvent> producerFactory(){
        Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, keySerializer);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, valueSerializer);
        config.put(ProducerConfig.ACKS_CONFIG, acks);
        config.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, deliveryTimeout);
        config.put(ProducerConfig.LINGER_MS_CONFIG, linger);
        config.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, requestTimeout);
        config.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, maxInFlightRequestPerConnection);
        config.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);

        return new DefaultKafkaProducerFactory<>(config);
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
