package org.jbp.csc611m.mc02.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class CsvServiceJob {

    @Autowired
    private CsvService csvService;

    //run every 15 seconds
    @Scheduled(fixedRate = 15000)
    public void generateCsv(){
        if ( csvService.shouldWriteOutputFilesNow()) {
            try {
                csvService.writeCsvOutputs();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
