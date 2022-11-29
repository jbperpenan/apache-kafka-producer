package org.jbp.csc611m.mc02.services;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import org.jbp.csc611m.mc02.entities.Email;
import org.jbp.csc611m.mc02.repositories.EmailRepository;
import org.jbp.csc611m.mc02.repositories.UrlRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.FileWriter;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Service
public class CsvService {

    @Autowired
    private EmailRepository emailRepository;

    @Autowired
    private UrlRepository urlRepository;

    public List<Long> writeCsvOutputs() throws Exception {
        List<Long> counts = new ArrayList<>();

        //write stats csv
        List<String[]> lines = new ArrayList<>();
        lines.add(new String[] { "page_count", "eadd_count" });
        Long emailCount = emailRepository.count();
        Long pageCount = urlRepository.countLongByStatus("SCRAPED");
        lines.add(new String[] { String.valueOf(pageCount), String.valueOf(emailCount)});
        writeLineByLine(lines, "dlsu-stats.csv");
        lines.clear();
        counts.add(pageCount);
        counts.add(emailCount);

        //write all email csv
        lines.add(new String[] { "associated_name", "email" });
        Iterable<Email> i1 = emailRepository.findAll();
        List<Email> emails = new LinkedList<>();
        i1.forEach(emails::add);
        Collections.shuffle(emails);
        for(Email email: emails){
            lines.add(new String[] { email.getName(), email.getEmail()});
        }
        writeLineByLine(lines, "dlsu-emails.csv");
        lines.clear();

        //write unique email csv
        lines.add(new String[] { "associated_name", "email" });
        Map<String, Email> eadd = new HashMap<>();
        for(Email email: emails){
            if (eadd.get(email.getEmail()) == null){
                lines.add(new String[] { email.getName(), email.getEmail()});
                eadd.put(email.getEmail(), email);
            }
        }
        writeLineByLine(lines, "dist-dlsu-emails.csv");
        counts.add(Long.valueOf(eadd.size()));

        return counts;
    }

    private void writeLineByLine(List<String[]> lines, String fileName) throws Exception {
        Path path = Paths.get(ClassLoader.getSystemResource(fileName).toURI());
        CSVWriter writer = new CSVWriter(new FileWriter(path.toString()),
                CSVWriter.DEFAULT_SEPARATOR , CSVWriter.NO_QUOTE_CHARACTER, CSVWriter.NO_ESCAPE_CHARACTER, CSVWriter.DEFAULT_LINE_END);
        try {
            writer.writeAll(lines);
        }catch (Exception e) {
            e.printStackTrace();
        }finally {
            writer.close();
        }
    }

    public List<String> getUrlsFromCsv() throws Exception {
        List<String[]> urlList = readAllLines("dlsu-urls.csv");
        List<String> urls = new ArrayList<>();
        for(String[] line: urlList) {
            urls.add(line[0]);
        }

        return urls;
    }

    private List<String[]> readAllLines(String fileName) throws Exception {
        Path path = Paths.get(ClassLoader.getSystemResource(fileName).toURI());
        try (Reader reader = Files.newBufferedReader(path)) {
            try (CSVReader csvReader = new CSVReader(reader)) {
                return csvReader.readAll();
            }
        }
    }
}
