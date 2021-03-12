package org.sdrc.datum19;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication
@EnableMongoRepositories(basePackages = { "org.sdrc.datum19.repository" })
@PropertySource("classpath:aggregation.properties")
public class Datum19Application {

	public static void main(String[] args) {
		SpringApplication.run(Datum19Application.class, args);
	}

}
