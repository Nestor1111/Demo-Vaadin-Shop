package com.nridev.vaadinshop;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;

@SpringBootApplication(exclude = ErrorMvcAutoConfiguration.class)
public class VaadinShopApplication extends SpringBootServletInitializer {

	private static final Logger log = LoggerFactory.getLogger(VaadinShopApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(VaadinShopApplication.class);
	}

}
