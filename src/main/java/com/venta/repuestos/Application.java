package com.venta.repuestos;


import com.venta.repuestos.entidades.DetalleVenta;
import com.venta.repuestos.entidades.Repuesto;
import com.venta.repuestos.entidades.Venta;
import com.venta.repuestos.enums.Disponibilidad;
import com.venta.repuestos.enums.Marca;
import com.venta.repuestos.repositorios.ClienteRepository;
import com.venta.repuestos.repositorios.RepuestoRepository;

import com.venta.repuestos.entidades.Cliente;
import com.venta.repuestos.repositorios.VentaRepository;
import com.venta.repuestos.servicios.ClienteService;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.List;
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
    CommandLineRunner generarVentas(VentaRepository ventaRepository,
                                    ClienteRepository clienteRepository,
                                    RepuestoRepository repuestoRepository) {
        return args -> {
            List<Cliente> clientes = clienteRepository.findAll();
            List<Repuesto> repuestos = repuestoRepository.findAll();

            if (clientes.isEmpty() || repuestos.isEmpty()) {
                System.out.println("No hay clientes o repuestos");
                return;
            }

            for (Cliente cliente : clientes) {
                Venta venta = new Venta();
                venta.setCliente(cliente);

                int cantidadDetalles = 1 + (int)(Math.random() * 3);// de 1 a 3 por venta
                List<DetalleVenta> detalles = new java.util.ArrayList<>();

                for (int i = 0; i < cantidadDetalles; i++) {
                    Repuesto repuesto = repuestos.get((int)(Math.random() * repuestos.size()));
                    int cantidad = 1 + (int)(Math.random() * 5);

                    DetalleVenta detalle = new DetalleVenta();
                    detalle.setRepuesto(repuesto);
                    detalle.setCantidad(cantidad);
                    detalle.setPrecioUnitario(repuesto.getPrecio());
                    detalle.setSubtotal(cantidad * repuesto.getPrecio());
                    detalle.setVenta(venta);

                    // Simular reducción de stock
                    int nuevoStock = repuesto.getStock() - cantidad;
                    repuesto.setStock(Math.max(nuevoStock, 0));
                    repuestoRepository.save(repuesto);

                    detalles.add(detalle);
                }

                double total = detalles.stream().mapToDouble(DetalleVenta::getSubtotal).sum();
                venta.setTotal(total);
                venta.setDetalles(detalles);

                ventaRepository.save(venta);
            }

            System.out.println("Ventas generadas correctamente en la base de datos");
        };
    }

}
