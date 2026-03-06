package com.crm.kizuna;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class KizunaApplication {

  public static void main(String[] args) {
    SpringApplication.run(KizunaApplication.class, args);
  }
}
