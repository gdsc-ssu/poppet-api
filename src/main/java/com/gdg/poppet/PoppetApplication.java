package com.gdg.poppet;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class PoppetApplication {

	public static void main(String[] args) {
		SpringApplication.run(PoppetApplication.class, args);
	}

}
