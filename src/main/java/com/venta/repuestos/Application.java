package com.venta.repuestos;


import com.venta.repuestos.entidades.Repuesto;
import com.venta.repuestos.enums.Disponibilidad;
import com.venta.repuestos.enums.Marca;
import com.venta.repuestos.repositorios.RepuestoRepository;

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
	CommandLineRunner start(RepuestoRepository repuestoRepository){
		return args -> {
			Stream.of("Martillo", "Destornillador", "Llave Inglesa", "Taladro").forEach(nombre ->{
				Repuesto repuesto = new Repuesto();
				repuesto.setNombre(nombre);
				Marca[] marcas = Marca.values();
				int indiceAleatorio = (int) (Math.random() * marcas.length);
				repuesto.setMarca(marcas[indiceAleatorio]);
				repuesto.setDescripcion("Descripcion de " +nombre);
				repuesto.setPrecio(10+Math.random()*400);
				repuesto.setStock((int)(Math.random()*100));
				//Siguiente metodo no necesario ya que se automatizo en la clase
			//	repuesto.setDisponibilidad(Math.random() < 0.5 ? Disponibilidad.DISPONIBLE : Disponibilidad.NO_DISPONIBLE);
				repuestoRepository.save(repuesto);
			});
		};
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
