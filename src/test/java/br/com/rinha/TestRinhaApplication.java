package br.com.rinha;

import org.springframework.boot.SpringApplication;

public class TestRinhaApplication {

	public static void main(String[] args) {
		SpringApplication.from(RinhaApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
