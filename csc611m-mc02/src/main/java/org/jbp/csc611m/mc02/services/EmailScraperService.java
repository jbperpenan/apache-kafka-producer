package org.jbp.csc611m.mc02.services;

import org.jbp.csc611m.mc02.entities.Email;
import org.jbp.csc611m.mc02.entities.Url;
import org.jbp.csc611m.mc02.repositories.EmailRepository;
import org.jbp.csc611m.mc02.repositories.UrlRepository;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class EmailScraperService {

    Logger logger = LoggerFactory.getLogger(EmailScraperService.class);

    private static final String EMAIL_SUFFIX = "@dlsu.edu.ph";
    private static final String EMAIL_REGEX = "[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+";

    @Autowired
    private EmailRepository emailRepository;

    @Autowired
    private UrlRepository urlRepository;

    @Async
    public CompletableFuture<Set<String>> executeEmailScraping(Url url, WebDriver driver){
        Set<String> scrapedEmails = new HashSet<>();

        //logger.info("Starting: url = {} with thread {}", url.getUrl(), Thread.currentThread().getName());
        try{
            driver.get(url.getUrl());
            String source = driver.getPageSource();
            Matcher m = Pattern.compile(EMAIL_REGEX).matcher(source);

            while (m.find()) {
                if(m.group().endsWith(EMAIL_SUFFIX)){
                    scrapedEmails.add(m.group());
                }
            }

            url.setStatus("SCRAPED");
            url.setWorker(Thread.currentThread().getName());
            urlRepository.save(url);

            scrapedEmails.stream()
                    .forEach(eadd -> emailRepository.save(new Email(eadd.split("@")[0],eadd)));

            return CompletableFuture.completedFuture(scrapedEmails);

        }catch (Exception e) {
            url.setStatus("ERROR");
            url.setWorker(Thread.currentThread().getName());
            urlRepository.save(url);

            //logger.error("Error scraping email for {}", url.getUrl());
        }

        //logger.info("Complete: url = {} with thread {}", url.getUrl(), Thread.currentThread().getName());
        return CompletableFuture.completedFuture(scrapedEmails);
    }
}
