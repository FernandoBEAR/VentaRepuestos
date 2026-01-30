package com.venta.repuestos;


import com.venta.repuestos.entidades.*;
import com.venta.repuestos.entidades.securityentities.PermissionEntity;
import com.venta.repuestos.entidades.securityentities.RoleEntity;
import com.venta.repuestos.entidades.securityentities.RoleEnum;
import com.venta.repuestos.entidades.securityentities.UserEntity;
import com.venta.repuestos.enums.Marca;
import com.venta.repuestos.enums.TipoMovimiento;
import com.venta.repuestos.repositorios.*;

import com.venta.repuestos.servicios.ClienteService;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

@SpringBootApplication
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

    @Bean
    CommandLineRunner init(UserRepository userRepository) {
        return args -> {

            /* -------------------------
               PERMISOS DEL SISTEMA
            -------------------------- */

            // CLIENTE
            PermissionEntity clienteRead = PermissionEntity.builder().name("CLIENTE_READ").build();
            PermissionEntity clienteWrite = PermissionEntity.builder().name("CLIENTE_WRITE").build();
            PermissionEntity clienteDelete = PermissionEntity.builder().name("CLIENTE_DELETE").build();

            // REPUESTO
            PermissionEntity repuestoRead = PermissionEntity.builder().name("REPUESTO_READ").build();
            PermissionEntity repuestoWrite = PermissionEntity.builder().name("REPUESTO_WRITE").build();
            PermissionEntity repuestoDelete = PermissionEntity.builder().name("REPUESTO_DELETE").build();
            PermissionEntity repuestoStockManual = PermissionEntity.builder().name("REPUESTO_STOCK_MANUAL").build();

            // VENTA
            PermissionEntity ventaRead = PermissionEntity.builder().name("VENTA_READ").build();
            PermissionEntity ventaWrite = PermissionEntity.builder().name("VENTA_WRITE").build();
            PermissionEntity ventaDelete = PermissionEntity.builder().name("VENTA_DELETE").build();

            /* -------------------------
               ROLES DEL SISTEMA
            -------------------------- */

            // ADMIN – Acceso total
            RoleEntity roleAdmin = RoleEntity.builder()
                    .roleEnum(RoleEnum.ADMIN)
                    .permisos(Set.of(
                            clienteRead, clienteWrite, clienteDelete,
                            repuestoRead, repuestoWrite, repuestoDelete, repuestoStockManual,
                            ventaRead, ventaWrite, ventaDelete
                    ))
                    .build();

            // VENDEDOR – Operaciones cotidianas
            RoleEntity roleVendedor = RoleEntity.builder()
                    .roleEnum(RoleEnum.VENDEDOR)
                    .permisos(Set.of(
                            clienteRead, clienteWrite,
                            repuestoRead,
                            ventaRead, ventaWrite
                    ))
                    .build();

            // INVITADO – Solo consulta
            RoleEntity roleInvitado = RoleEntity.builder()
                    .roleEnum(RoleEnum.INVITADO)
                    .permisos(Set.of(
                            clienteRead,
                            repuestoRead,
                            ventaRead
                    ))
                    .build();
            /* -------------------------
               USUARIOS INICIALES
            -------------------------- */

            String pass = new BCryptPasswordEncoder().encode("1234");

            UserEntity admin = UserEntity.builder()
                    .username("admin")
                    .password(pass)
                    .isEnable(true)
                    .accountNoExpired(true)
                    .accountNoLocked(true)
                    .credentialNoExpired(true)
                    .roles(Set.of(roleAdmin))
                    .build();

            UserEntity vendedor = UserEntity.builder()
                    .username("vendedor")
                    .password(pass)
                    .isEnable(true)
                    .accountNoExpired(true)
                    .accountNoLocked(true)
                    .credentialNoExpired(true)
                    .roles(Set.of(roleVendedor))
                    .build();

            UserEntity invitado = UserEntity.builder()
                    .username("invitado")
                    .password(pass)
                    .isEnable(true)
                    .accountNoExpired(true)
                    .accountNoLocked(true)
                    .credentialNoExpired(true)
                    .roles(Set.of(roleInvitado))
                    .build();

            userRepository.saveAll(List.of(admin, vendedor, invitado));
        };
    }

    @Bean
    CommandLineRunner start(ClienteService clienteService, RepuestoRepository repuestoRepository, VentaRepository ventaRepository, ClienteRepository clienteRepository, MovimientoRepository movimientoRepository) {
        return args -> {
            Stream.of("Cristian","Carlos","William","Shyntia").forEach(nombre ->{
                Cliente clien = new Cliente();
                clien.setNombre(nombre);
                clien.setEmail(nombre.toLowerCase()+"@latinmail.com");
                clienteService.save(clien);
            });
            Stream.of("Martillo", "Destornillador", "Llave Inglesa", "Taladro").forEach(nombre ->{
                Repuesto repuesto = new Repuesto();
                repuesto.setNombre(nombre);
                Marca[] marcas = Marca.values();
                int indiceAleatorio = (int) (Math.random() * marcas.length);
                repuesto.setMarca(marcas[indiceAleatorio]);
                repuesto.setDescripcion("Descripcion de " +nombre);
                repuesto.setPrecio(10+Math.random()*400);
                repuesto.setStock(((int)(Math.random()*100)+100));
                repuestoRepository.save(repuesto);
            });

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
                    Movimiento movimiento = new Movimiento();
                    movimiento.setRepuesto(detalle.getRepuesto());
                    movimiento.setTipoMovimiento(TipoMovimiento.VENTA);
                    movimiento.setCantidadDeMovimientos(detalle.getCantidad());
                    movimiento.setFechaMovimiento(
                            new FechaMovimiento(
                                    LocalDate.now().getDayOfMonth(),
                                    LocalDate.now().getMonthValue(),
                                    LocalDate.now().getYear()
                            )
                    );

                    repuesto.setStock(Math.max(nuevoStock, 0));
                    repuestoRepository.save(repuesto);
                    movimientoRepository.save(movimiento);

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
