package com.hrbank3.hrbank3;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@EnableJpaAuditing
@SpringBootApplication
public class Hrbank3Application {

  public static void main(String[] args) {
    SpringApplication.run(Hrbank3Application.class, args);
  }
}
