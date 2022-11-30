package org.jbp.csc611m.mc02.services;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.jbp.csc611m.mc02.entities.Url;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Properties;

@Service
public class ProducerService {

    private final String TOPIC = "my-kafka-topic";
    private final String BOOTSTRAP_SERVERS = "localhost:9092";

    @Value( "${website}" )
    private String website;

    @Autowired
    private WebsiteLinksCrawlerService websiteLinksCrawlerService;

    public void produceUrlMessages() {
        List<Url> urlList = websiteLinksCrawlerService.initWebsiteCrawlerConfig(website);

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
            for(Url url: urlList) {
                String key = String.valueOf(url.getId());

                String message = url.getUrl();

                producer.send(new ProducerRecord<String, String>(TOPIC, key, message));
                System.out.println("sent msg with key " + key +" and message "+message);
            }
        } catch (Exception e) {
            System.out.println("Could not start producer: " + e);
        }
    }
}
