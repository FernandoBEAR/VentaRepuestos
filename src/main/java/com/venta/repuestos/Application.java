package com.venta.repuestos;

import com.venta.repuestos.entidades.Cliente;
import com.venta.repuestos.servicios.ClienteService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.stream.Stream;

@SpringBootApplication
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	//@Bean
	CommandLineRunner start(ClienteService clienteService){
		return args -> {
			Stream.of("Cristian","Carlos","William","Shyntia").forEach(nombre ->{
				Cliente clien = new Cliente();
				clien.setNombre(nombre);
				clien.setEmail(nombre.toLowerCase()+"@latinmail.com");
				clienteService.save(clien);
			});
		};
	}

}
