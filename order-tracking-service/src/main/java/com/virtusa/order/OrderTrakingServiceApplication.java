package com.virtusa.order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class OrderTrakingServiceApplication {
	public static void main(String[] args) {
		SpringApplication.run(OrderTrakingServiceApplication.class, args);
	}

}
