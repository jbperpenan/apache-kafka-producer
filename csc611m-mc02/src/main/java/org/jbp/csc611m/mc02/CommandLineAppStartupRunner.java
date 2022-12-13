package org.jbp.csc611m.mc02;

import org.jbp.csc611m.mc02.services.ConsumerService;
import org.jbp.csc611m.mc02.services.ProducerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class CommandLineAppStartupRunner implements CommandLineRunner {

    @Autowired
    private ProducerService producerService;

    @Autowired
    private ConsumerService consumerService;

    @Override
    public void run(String... args) throws InterruptedException {

        Thread.sleep(10000);
        producerService.produceUrlMessages();
    }
}
