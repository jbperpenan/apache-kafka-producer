package org.jbp.csc611m.mc02;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;

@Component
public class CommandLineAppStartupRunner implements CommandLineRunner {

    Logger logger = LoggerFactory.getLogger(CommandLineAppStartupRunner.class);


    @Override
    public void run(String... args){
        Thread consumerThread = new Thread(CommandLineAppStartupRunner::consume);
        consumerThread.start();

        Thread producerThread = new Thread(CommandLineAppStartupRunner::produce);
        producerThread.start();

        //produce();
        //consume();
    }

    private static final String TOPIC = "my-kafka-topic";
    private static final String BOOTSTRAP_SERVERS = "localhost:9092";

    private static void produce() {
        // Create configuration options for our producer and initialize a new producer
        Properties props = new Properties();
        props.put("bootstrap.servers", BOOTSTRAP_SERVERS);
        // We configure the serializer to describe the format in which we want to produce data into
        // our Kafka cluster
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        // Since we need to close our producer, we can use the try-with-resources statement to
        // create
        // a new producer
        try (Producer<String, String> producer = new KafkaProducer<>(props)) {
            // here, we run an infinite loop to sent a message to the cluster every second
            for (int i = 0;; i++) {
                String key = Integer.toString(i);
                String message = "this is message " + Integer.toString(i);

                producer.send(new ProducerRecord<String, String>(TOPIC, key, message));

                // log a confirmation once the message is written
                System.out.println("sent msg " + key);
                try {
                    // Sleep for a second
                    Thread.sleep(1000);
                } catch (Exception e) {
                    break;
                }
            }
        } catch (Exception e) {
            System.out.println("Could not start producer: " + e);
        }
    }

    private static void consume() {
        // Create configuration options for our consumer
        Properties props = new Properties();
        props.setProperty("bootstrap.servers", BOOTSTRAP_SERVERS);
        // The group ID is a unique identified for each consumer group
        props.setProperty("group.id", "my-group-id");
        // Since our producer uses a string serializer, we need to use the corresponding
        // deserializer
        props.setProperty("key.deserializer",
                "org.apache.kafka.common.serialization.StringDeserializer");
        props.setProperty("value.deserializer",
                "org.apache.kafka.common.serialization.StringDeserializer");
        // Every time we consume a message from kafka, we need to "commit" - that is, acknowledge
        // receipts of the messages. We can set up an auto-commit at regular intervals, so that
        // this is taken care of in the background
        props.setProperty("enable.auto.commit", "true");
        props.setProperty("auto.commit.interval.ms", "1000");

        // Since we need to close our consumer, we can use the try-with-resources statement to
        // create it
        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props)) {
            // Subscribe this consumer to the same topic that we wrote messages to earlier
            consumer.subscribe(Arrays.asList(TOPIC));
            // run an infinite loop where we consume and print new messages to the topic
            while (true) {
                // The consumer.poll method checks and waits for any new messages to arrive for the
                // subscribed topic
                // in case there are no messages for the duration specified in the argument (1000 ms
                // in this case), it returns an empty list
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));
                for (ConsumerRecord<String, String> record : records) {
                    System.out.printf("received message: %s\n", record.value());
                }
            }
        }
    }


}
