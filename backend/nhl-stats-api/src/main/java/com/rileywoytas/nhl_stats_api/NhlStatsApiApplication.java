package com.rileywoytas.nhl_stats_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class NhlStatsApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(NhlStatsApiApplication.class, args);
	}

}
