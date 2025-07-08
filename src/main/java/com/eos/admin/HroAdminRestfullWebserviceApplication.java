package com.eos.admin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.eos.admin")
public class HroAdminRestfullWebserviceApplication {

	public static void main(String[] args) {
		SpringApplication.run(HroAdminRestfullWebserviceApplication.class, args);
	}

}
