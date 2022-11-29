package org.jbp.csc611m.mc02;

import org.jbp.csc611m.mc02.entities.Url;
import org.jbp.csc611m.mc02.services.CsvService;
import org.jbp.csc611m.mc02.services.EmailScraperService;
import org.jbp.csc611m.mc02.services.WebsiteLinksCrawlerService;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static java.util.concurrent.TimeUnit.SECONDS;

@Component
public class CommandLineAppStartupRunner implements CommandLineRunner {

    Logger logger = LoggerFactory.getLogger(CommandLineAppStartupRunner.class);

    @Autowired
    private CsvService csvService;

    @Autowired
    private WebsiteLinksCrawlerService websiteLinksCrawlerService;

    @Autowired
    private EmailScraperService emailScraperService;

    @Autowired
    private ThreadPoolTaskExecutor taskExecutor;

    @Value( "${website}" )
    private String website;

    @Value( "${thread}" )
    private Integer threadCount;

    @Value( "${time}" )
    private Integer runtime;

    @Override
    public void run(String... args) throws Exception {

        if(isRunningDefaultConfig(args)){
            System.out.println("Running on DEFAULT configurations...");
        }else{
            runWithCommandlineInput();
        }

        List<Url> urlList = initRunConfigurations();
        WebDriver driver = initBrowserDriverConfig();

        if(runtime == 0) {
            runWithNoTimeLimit(urlList, driver);
        } else {
            runWithGivenRuntime(urlList, driver);
        }
    }

    private WebDriver initBrowserDriverConfig() {
        System.setProperty("webdriver.chrome.driver","src/main/resources/chromedriver");
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        options.setImplicitWaitTimeout(Duration.ofSeconds(30));
        WebDriver driver = new ChromeDriver(options);
        return driver;
    }

    private void runWithGivenRuntime(List<Url> urlList, WebDriver driver) {
        for(int i = 0; i< urlList.size(); i++){
            try {
                emailScraperService.executeEmailScraping(urlList.get(i), driver);
            }catch (Exception e) {
                e.printStackTrace();
            }
        }

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

        Runnable runnable = new Runnable() {
           int countdownStarter = runtime*60;

           public void run() {

               countdownStarter--;
               if(countdownStarter == ((runtime*60)/2)){
                   System.out.println("**** 50% of runtime, "+(countdownStarter/60)+" minutes remaining ****");
               }
               if(countdownStarter == 60){
                   System.out.println("**** Last minute of runtime ****");
               }

               if(countdownStarter == 15){
                   System.out.println("**** Last 15seconds of runtime ****");
               }

               if(taskExecutor.getActiveCount() ==0 && taskExecutor.getThreadPoolExecutor().getQueue().size() ==0){
                   System.out.println("**** PAGES EMAIL SCRAPING COMPLETED at "+countdownStarter/60+"min before the runtime "+runtime+"min finished ****");
                   scheduler.shutdown();
                   taskExecutor.shutdown();
                   try {
                       processResults();
                   } catch (Exception e) {
                       e.printStackTrace();
                   }
               }

               if (countdownStarter < 0) {
                   scheduler.shutdown();
                   taskExecutor.shutdown();
                   try {
                       processResults();
                   } catch (Exception e) {
                       e.printStackTrace();
                   }
               }
           }
       };
        scheduler.scheduleAtFixedRate(runnable, 0, 1, SECONDS);
    }

    private void runWithNoTimeLimit(List<Url> urlList, WebDriver driver) throws Exception {
        for(int i = 0; i< urlList.size(); i++){
            try {
                emailScraperService.executeEmailScraping(urlList.get(i), driver);
            }catch (Exception e) {
                e.printStackTrace();
            }
        }

        while(taskExecutor.getActiveCount() !=0 && taskExecutor.getThreadPoolExecutor().getQueue().size() !=0){
            //wait for all threads to finish...
        }
        processResults();
    }

    private void runWithCommandlineInput() {
        Scanner sc = new Scanner(System.in);

        System.out.println("Enter website to scrape [default: "+ website+"]: ");
        String websiteIn = sc.nextLine();  // Read user input
        if(websiteIn != null && websiteIn != "" && !websiteIn.isEmpty()){
            website = websiteIn;
        }

        System.out.println("Enter number of thread(s) [default: "+ threadCount +"]: ");
        int tr = sc.nextInt();
        if(tr > 1){
            threadCount = tr;
        }

        System.out.println("Enter runtime in minutes [default: no time]");
        int ti = sc.nextInt();
        if(ti > 0){
            runtime = ti;
        }
    }

    private List<Url> initRunConfigurations() {
        System.out.println("...crawling links in the website: "+website);
        List<Url> urlList = websiteLinksCrawlerService.initWebsiteCrawlerConfig(website);

        System.out.println("===== Run Configurations =====");
        System.out.println("Website to scrape: "+ website);
        //System.out.println("Number of links found: "+urlList.size());
        System.out.println("Number of thread(s): "+ threadCount);
        String time = runtime != 0 ? String.valueOf(runtime) : "N/A";
        System.out.println("Runtime (minute/s): "+ time);
        return urlList;
    }

    private void processResults() throws Exception {
        List<Long> results = csvService.writeCsvOutputs();
        System.out.println("===== Run RESULTS =====");
        System.out.println("Website to scrape: "+ website);
        //System.out.println("Number of links found: "+urlList.size());
        System.out.println("Number of thread(s): "+ threadCount);
        String time = runtime != 0 ? String.valueOf(runtime) : "N/A";
        System.out.println("Runtime (minute/s): "+ time);
        System.out.println("");
        System.out.println("Website Pages: "+ results.get(0));
        System.out.println("Scraped Emails: "+ results.get(1));
        System.out.println("Unique Emails: "+ results.get(2));
        System.out.println("\n\n\n");
        System.out.println("...output csv files generated");
    }

    private boolean isRunningDefaultConfig(String[] args) {
        boolean runDefault = false;
        final String def = "default";
        if (args.length > 0) {
            for(String arg: args) {
                if(arg == def || arg.equals(def)){
                    runDefault = true;
                }
            }
        }
        return runDefault;
    }
}
