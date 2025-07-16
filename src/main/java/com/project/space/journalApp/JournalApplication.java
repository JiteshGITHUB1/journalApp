package com.project.space.journalApp;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.MongoTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@Slf4j
public class JournalApplication {

	public static void main(String[] args) {
		ConfigurableApplicationContext run = SpringApplication.run(JournalApplication.class, args);
		log.info("Journal Application Started Successfully");
		ConfigurableEnvironment environment = run.getEnvironment();
		log.info(environment.getActiveProfiles()[0]);
	}
}
