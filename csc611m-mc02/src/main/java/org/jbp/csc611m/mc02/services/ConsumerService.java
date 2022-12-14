package org.jbp.csc611m.mc02.services;

import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.jbp.csc611m.mc02.entities.Url;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;
import java.util.Properties;

@Service
public class ConsumerService {

    private final String TOPIC = "my-kafka-topic-1";
    private final String BOOTSTRAP_SERVERS = "localhost:9092";

    @Autowired
    private EmailScraperService emailScraperService;

    public void runConsumerInstance () {
        Properties props = new Properties();
        props.setProperty("bootstrap.servers", BOOTSTRAP_SERVERS);
        props.setProperty("group.id", "my-group-id");
        props.setProperty("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.setProperty("value.deserializer", "org.jbp.csc611m.mc02.entities.UrlDeserializer");
        props.setProperty("enable.auto.commit", "true");
        props.setProperty("auto.commit.interval.ms", "1000");

        AtomicReference<Url> msgCons = new AtomicReference<>();
        try (KafkaConsumer<String, Url> consumer = new KafkaConsumer<>(props)) {
            consumer.subscribe(Arrays.asList(TOPIC));
            while (true) {
                ConsumerRecords<String, Url> records = consumer.poll(Duration.ofMillis(1000));
                records.forEach(record -> {
                    msgCons.set(record.value());
                    System.out.println("Message received " + record.value());
                    emailScraperService.executeEmailScraping(record.value());
                });
            }
        }
    }
}
