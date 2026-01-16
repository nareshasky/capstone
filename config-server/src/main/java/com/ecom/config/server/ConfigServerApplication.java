package com.ecom.config.server;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@SpringBootApplication
@RestController
@RequestMapping("/config")
@EnableConfigServer
public class ConfigServerApplication {


    @Value("${message:default message}")
    static private String message;

	public static void main(String[] args) {
		SpringApplication.run(ConfigServerApplication.class, args);
	}

    @GetMapping("/hello")
    public String getSingleUser() {
            return message;
    }

}
