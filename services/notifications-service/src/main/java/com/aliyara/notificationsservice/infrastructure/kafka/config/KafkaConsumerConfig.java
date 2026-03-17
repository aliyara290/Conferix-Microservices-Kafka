package com.aliyara.notificationsservice.infrastructure.kafka.config;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafka
public class KafkaConsumerConfig {
    
    // Allow fallback to embedded Kafka brokers used in tests, and finally to localhost:9092
    @Value("${spring.kafka.bootstrap-servers:${spring.embedded.kafka.brokers:localhost:9092}}")
    private String bootstrapServers;
    
    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;
   
    @Bean
    public ConsumerFactory<String, JsonNode> consumerFactory() {
        Map<String, Object> config = new HashMap<>();
        
        // Configuration du serveur Kafka
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        
        // Configuration du groupe de consommateurs
        config.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        
        // Désactivation de l'auto-commit pour un contrôle manuel
        config.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        
        // Lecture depuis le début du topic si aucun offset n'existe
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        
        // Désérialiseurs avec ErrorHandlingDeserializer pour Spring Kafka 4.0+
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        config.put(ErrorHandlingDeserializer.KEY_DESERIALIZER_CLASS, StringDeserializer.class);
        config.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, "org.springframework.kafka.support.serializer.JsonDeserializer");
        
        // Configuration du JsonDeserializer pour JsonNode (generic type) - Spring Kafka 4.0+
        config.put("spring.json.trusted.packages", "*");
        config.put("spring.json.use.type.headers", false);
        config.put("spring.json.value.default.type", JsonNode.class.getName());
        
        // Optimisations
        config.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 10);
        config.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, 30000);
        config.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, 10000);
        
        return new DefaultKafkaConsumerFactory<>(config);
    }
    
   
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, JsonNode> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, JsonNode> factory = 
            new ConcurrentKafkaListenerContainerFactory<>();
        
        factory.setConsumerFactory(consumerFactory());
        
        // Configuration du mode d'acknowledgement manuel
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
        
        // Configuration du gestionnaire d'erreurs avec retry
        factory.setCommonErrorHandler(defaultErrorHandler());
        
        // Concurrence (nombre de threads par listener)
        factory.setConcurrency(3);
        
        return factory;
    }
  
    @Bean
    public DefaultErrorHandler defaultErrorHandler() {
        FixedBackOff fixedBackOff = new FixedBackOff(5000L, 3L);
        
        DefaultErrorHandler errorHandler = new DefaultErrorHandler(fixedBackOff);
        errorHandler.setLogLevel(org.springframework.kafka.KafkaException.Level.ERROR);
        
        return errorHandler;
    }
}
