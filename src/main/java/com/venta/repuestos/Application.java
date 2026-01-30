package com.venta.repuestos;


import com.venta.repuestos.entidades.*;
import com.venta.repuestos.entidades.securityentities.PermissionEntity;
import com.venta.repuestos.entidades.securityentities.RoleEntity;
import com.venta.repuestos.entidades.securityentities.RoleEnum;
import com.venta.repuestos.entidades.securityentities.UserEntity;
import com.venta.repuestos.enums.*;
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

    //@Bean
    CommandLineRunner init(UserRepository userRepository) {
        return args -> {




            PermissionEntity clienteRead = PermissionEntity.builder().name("CLIENTE_READ").build();
            PermissionEntity clienteCreate = PermissionEntity.builder().name("CLIENTE_CREATE").build();
            PermissionEntity clienteUpdate = PermissionEntity.builder().name("CLIENTE_UPDATE").build();
            PermissionEntity clienteDelete = PermissionEntity.builder().name("CLIENTE_DELETE").build();


            PermissionEntity repuestoRead = PermissionEntity.builder().name("REPUESTO_READ").build();
            PermissionEntity repuestoCreate = PermissionEntity.builder().name("REPUESTO_CREATE").build();
            PermissionEntity repuestoUpdate = PermissionEntity.builder().name("REPUESTO_UPDATE").build();
            PermissionEntity repuestoDelete = PermissionEntity.builder().name("REPUESTO_DELETE").build();
            PermissionEntity repuestoStock = PermissionEntity.builder().name("REPUESTO_STOCK").build();


            PermissionEntity ventaRead = PermissionEntity.builder().name("VENTA_READ").build();
            PermissionEntity ventaCreate = PermissionEntity.builder().name("VENTA_CREATE").build();
            PermissionEntity ventaUpdate = PermissionEntity.builder().name("VENTA_UPDATE").build();
            PermissionEntity ventaDelete = PermissionEntity.builder().name("VENTA_DELETE").build();


            PermissionEntity detalleVentaRead = PermissionEntity.builder().name("DETALLE_VENTA_READ").build();
            PermissionEntity detalleVentaCreate = PermissionEntity.builder().name("DETALLE_VENTA_CREATE").build();
            PermissionEntity detalleVentaUpdate = PermissionEntity.builder().name("DETALLE_VENTA_UPDATE").build();
            PermissionEntity detalleVentaDelete = PermissionEntity.builder().name("DETALLE_VENTA_DELETE").build();


            PermissionEntity pagoRead = PermissionEntity.builder().name("PAGO_READ").build();
            PermissionEntity pagoCreate = PermissionEntity.builder().name("PAGO_CREATE").build();
            PermissionEntity pagoUpdate = PermissionEntity.builder().name("PAGO_UPDATE").build();
            PermissionEntity pagoDelete = PermissionEntity.builder().name("PAGO_DELETE").build();


            PermissionEntity userRead = PermissionEntity.builder().name("USER_READ").build();
            PermissionEntity userCreate = PermissionEntity.builder().name("USER_CREATE").build();
            PermissionEntity userUpdate = PermissionEntity.builder().name("USER_UPDATE").build();
            PermissionEntity userDelete = PermissionEntity.builder().name("USER_DELETE").build();


            RoleEntity roleAdmin = RoleEntity.builder()
                    .roleEnum(RoleEnum.ADMIN)
                    .permisos(Set.of(
                            // Clientes - CRUD completo
                            clienteRead, clienteCreate, clienteUpdate, clienteDelete,
                            // Repuestos - CRUD completo + stock
                            repuestoRead, repuestoCreate, repuestoUpdate, repuestoDelete, repuestoStock,
                            // Ventas - CRUD completo
                            ventaRead, ventaCreate, ventaUpdate, ventaDelete,
                            // Detalle Ventas - CRUD completo
                            detalleVentaRead, detalleVentaCreate, detalleVentaUpdate, detalleVentaDelete,
                            // Pagos - CRUD completo
                            pagoRead, pagoCreate, pagoUpdate, pagoDelete,
                            // Usuarios - CRUD completo
                            userRead, userCreate, userUpdate, userDelete

                    ))
                    .build();


            RoleEntity roleVendedor = RoleEntity.builder()
                    .roleEnum(RoleEnum.VENDEDOR)
                    .permisos(Set.of(
                            // Clientes - Crear, actualizar, leer
                            clienteRead, clienteCreate, clienteUpdate,
                            // Repuestos - Solo lectura
                            repuestoRead,
                            // Ventas - Crear, leer
                            ventaRead, ventaCreate,
                            // Detalle Ventas - Crear, leer
                            detalleVentaRead, detalleVentaCreate,
                            // Pagos - Crear, leer
                            pagoRead, pagoCreate
                    ))
                    .build();

            RoleEntity roleLogistica = RoleEntity.builder()
                    .roleEnum(RoleEnum.LOGISTICA)
                    .permisos(Set.of(
                            // Repuestos - Crear, actualizar, leer, controlar stock
                            repuestoRead, repuestoCreate, repuestoUpdate, repuestoStock
                    ))
                    .build();


            String pass = new BCryptPasswordEncoder().encode("1234");

            // Usuario administrador con acceso total
            UserEntity admin = UserEntity.builder()
                    .username("admin")
                    .password(pass)
                    .isEnable(true)
                    .accountNoExpired(true)
                    .accountNoLocked(true)
                    .credentialNoExpired(true)
                    .roles(Set.of(roleAdmin))
                    .build();

            // Usuario vendedor para operaciones de venta
            UserEntity vendedor = UserEntity.builder()
                    .username("vendedor")
                    .password(pass)
                    .isEnable(true)
                    .accountNoExpired(true)
                    .accountNoLocked(true)
                    .credentialNoExpired(true)
                    .roles(Set.of(roleVendedor))
                    .build();

            // Usuario logística para gestión de inventario
            UserEntity logistica = UserEntity.builder()
                    .username("logistica")
                    .password(pass)
                    .isEnable(true)
                    .accountNoExpired(true)
                    .accountNoLocked(true)
                    .credentialNoExpired(true)
                    .roles(Set.of(roleLogistica))
                    .build();

            userRepository.saveAll(List.of(admin, vendedor, logistica));

            System.out.println("  USUARIOS CREADOS EXITOSAMENTE");
            System.out.println("  Usuario: admin     | Password: 1234 | Rol: ADMIN");
            System.out.println("  Usuario: vendedor  | Password: 1234 | Rol: VENDEDOR");
            System.out.println("  Usuario: logistica | Password: 1234 | Rol: LOGISTICA");
        };
    }

    //@Bean
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

                Pago pago = new Pago();
                pago.setEstadoPago(EstadoPago.PAGADO);
                pago.setMetodoPago(MetodoPago.EFECTIVO);
                pago.setMonto(total);
                pago.setTipoComprobante(TipoComprobante.BOLETA);
                pago.setFecha(LocalDate.now());
                pago.setVenta(venta);

                venta.setPago(pago);


                ventaRepository.save(venta);
            }

            System.out.println("Ventas generadas correctamente en la base de datos");

        };
    }
}
