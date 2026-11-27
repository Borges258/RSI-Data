package com.horsecoder.agentlab.whereprior;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** Isolated WHERE-prior service; it does not scan the legacy base application's DB/flow beans. */
@SpringBootApplication
public class WherePriorApplication {
    public static void main(String[] args) {
        SpringApplication.run(WherePriorApplication.class, args);
    }
}
