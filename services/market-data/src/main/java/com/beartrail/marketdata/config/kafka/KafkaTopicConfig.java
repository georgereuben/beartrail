package com.beartrail.marketdata.config.kafka;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;

import java.util.HashMap;
import java.util.Map;

@Configuration
@Slf4j
public class KafkaTopicConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${beartrail.kafka.topics.market-data-updates}")
    private String marketDataUpdatesTopic;

    @Value("${beartrail.kafka.partitions:3}")
    private int partitions;

    @Value("${beartrail.kafka.replication-factor:1}")
    private int replicationFactor;

    @Bean
    public KafkaAdmin kafkaAdmin() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        return new KafkaAdmin(configs);
    }

    @Bean
    public NewTopic marketDataUpdatesTopic() {
        return TopicBuilder.name(marketDataUpdatesTopic)
                .partitions(partitions)
                .replicas(replicationFactor)
                .config("retention.ms", "86400000") // 24 hours retention
                .config("cleanup.policy", "delete")
                .config("compression.type", "snappy")
                .build();
    }
}