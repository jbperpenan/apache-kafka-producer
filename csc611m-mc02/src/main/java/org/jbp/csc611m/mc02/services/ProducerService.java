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

    private final String TOPIC = "my-kafka-topic-1";
    private final String BOOTSTRAP_SERVERS = "localhost:9092";

    @Value( "${website}" )
    private String website;

    @Autowired
    private WebsiteLinksCrawlerService websiteLinksCrawlerService;

    public void produceUrlMessages() throws InterruptedException {
        List<Url> urlList = websiteLinksCrawlerService.initWebsiteCrawlerConfig(website);
        //Thread.sleep(10000);
        // Create configuration options for our producer and initialize a new producer
        Properties props = new Properties();
        props.put("bootstrap.servers", BOOTSTRAP_SERVERS);
        // We configure the serializer to describe the format in which we want to produce data into
        // our Kafka cluster
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        //props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.jbp.csc611m.mc02.entities.UrlSerializer");

        // Since we need to close our producer, we can use the try-with-resources statement to
        // create
        // a new producer
        try (Producer<String, Url> producer = new KafkaProducer<>(props)) {
            for(Url url: urlList) {
                String key = String.valueOf(url.getId());

                System.out.println("waiting 5 seconds before sending the next message...");
                //Thread.sleep(5000);
                producer.send(new ProducerRecord<>(TOPIC, key, url));
                System.out.println("sent msg with id " + url.getId() +" and url "+url.getUrl());
            }
        } catch (Exception e) {
            System.out.println("Could not start producer: " + e);
        }
    }
}
