package com.github.slamdev.springmultipledatasources;

import com.github.slamdev.springmultipledatasources.chsvc.CHService;
import com.github.slamdev.springmultipledatasources.crdbsvc.CRDBService;
import com.github.slamdev.springmultipledatasources.pgsvc.PGService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application implements CommandLineRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);

    private final PGService pgService;
    private final CRDBService crdbService;
    private final CHService chService;

    public Application(PGService pgService, CRDBService crdbService, CHService chService) {
        this.pgService = pgService;
        this.crdbService = crdbService;
        this.chService = chService;
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        LOGGER.info("PGService: {}", pgService.doSomething());
        LOGGER.info("CRDBService: {}", crdbService.doSomething());
        LOGGER.info("CHService: {}", chService.doSomething());
    }
}
