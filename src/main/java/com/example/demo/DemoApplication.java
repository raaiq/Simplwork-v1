package com.example.demo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.util.FileSystemUtils;

import java.io.File;

//TODO: Ask preferences for each category when matching candidates, e.g if they really need experience or okay with none, distance preference, okay with high schoolers
//TODO: Version build update with every git push
//TODO: Automate CI/CD
//TODO: Have service that periodically checks for non-active users and disables matching, same for job postings
@SpringBootApplication
public class DemoApplication  {

	public static void main(String[] args) {SpringApplication.run(DemoApplication.class, args);}

	@Profile("!dev")
	@Bean
	CommandLineRunner runner(@Value("${server.tomcat.basedir}") String baseDir){
		return (args -> {Runtime.getRuntime().addShutdownHook(
														new Thread(()->{
																FileSystemUtils.deleteRecursively(new File(baseDir));}));});
	}


}
