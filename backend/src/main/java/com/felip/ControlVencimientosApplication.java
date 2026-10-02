package com.felip;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.Scanner;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.felip.config.SecurityUtil;
import com.felip.dao.LaboratorioDAO;
import com.felip.dao.LoteDAO;
import com.felip.dao.PresentacionDAO;
import com.felip.dao.ProductoDAO;
import com.felip.dao.UsuarioDAO;
import com.felip.model.Usuario;

@SpringBootApplication
public class ControlVencimientosApplication implements CommandLineRunner {

    @Autowired
    private UsuarioDAO usuarioDAO;

    @Autowired
    private LaboratorioDAO laboratorioDAO;

    @Autowired
    private ProductoDAO productoDAO;

    @Autowired
    private PresentacionDAO presentacionDAO;

    @Autowired
    private LoteDAO loteDAO;

    @Value("${app.console.enabled:false}")
    private boolean consoleEnabled;

    public static void main(String[] args) {
        SpringApplication.run(ControlVencimientosApplication.class, args);
    }

    @Override
    public void run(String... args) {
        if (!consoleEnabled) {
            return;
        }

        Scanner sc = new Scanner(System.in);

        while (true) {
            System.out.println("\n  CONTROL DE VENCIMIENTOS  ");
            System.out.println("1. Registrar usuario");
            System.out.println("2. Login");
            System.out.println("0. Salir");
            System.out.print("Seleccione una opcion: ");

            switch (sc.nextLine()) {
                case "1" -> {
                    System.out.print("Username: ");
                    String username = sc.nextLine();

                    System.out.print("Password: ");
                    String password = sc.nextLine();

                    boolean ok = usuarioDAO.registrar(username, SecurityUtil.hashPassword(password));
                    System.out.println(ok ? "Usuario creado." : "No se pudo crear el usuario.");
                }

                case "2" -> {
                    System.out.print("Username: ");
                    String username = sc.nextLine();

                    System.out.print("Password: ");
                    String password = sc.nextLine();

                    Optional<Usuario> usuario = usuarioDAO.buscarPorUsername(username);

                    if (usuario.isEmpty()) {
                        System.out.println("Usuario no encontrado.");
                        break;
                    }

                    if (SecurityUtil.verifyPassword(password, usuario.get().getPasswordHash())) {
                        if (SecurityUtil.needsPasswordUpgrade(usuario.get().getPasswordHash())) {
                            usuarioDAO.actualizarHash(usuario.get(), SecurityUtil.hashPassword(password));
                        }
                        System.out.println("Bienvenido, " + usuario.get().getUsername());
                        menuPrincipal(sc);
                    } else {
                        System.out.println("Password incorrecto.");
                    }
                }

                case "0" -> {
                    System.out.println("Saliendo...");
                    sc.close();
                    return;
                }

                default -> System.out.println("Opcion invalida.");
            }
        }
    }

    private void menuPrincipal(Scanner sc) {
        while (true) {
            System.out.println("\n  MENU PRINCIPAL  ");
            System.out.println("1. Gestionar Laboratorios");
            System.out.println("2. Gestionar Productos");
            System.out.println("3. Gestionar Presentaciones");
            System.out.println("4. Gestionar Lotes");
            System.out.println("5. Ver lotes por vencer (90 dias)");
            System.out.println("0. Cerrar sesión");
            System.out.print("Opcion: ");

            switch (sc.nextLine()) {
                case "1" -> menuLaboratorios(sc);
                case "2" -> menuProductos(sc);
                case "3" -> menuPresentaciones(sc);
                case "4" -> menuLotes(sc);
                case "5" -> {
                    var lotes = loteDAO.obtenerPorVencer(90);
                    System.out.println("\n  LOTES POR VENCER EN 90 DIAS  ");
                    if (lotes.isEmpty()) {
                        System.out.println("No hay lotes por vencer.");
                    } else {
                        lotes.forEach(System.out::println);
                    }
                }
                case "0" -> {
                    return;
                }
                default -> System.out.println("Opcion invalida.");
            }
        }
    }

    private void menuLaboratorios(Scanner sc) {
        while (true) {
            System.out.println("\n  LABORATORIOS  ");
            System.out.println("1. Crear");
            System.out.println("2. Listar");
            System.out.println("3. Actualizar");
            System.out.println("4. Eliminar");
            System.out.println("0. Volver");
            System.out.print("Opcion: ");

            switch (sc.nextLine()) {
                case "1" -> {
                    System.out.print("Nombre: ");
                    String nombre = sc.nextLine();

                    System.out.print("Ciudad: ");
                    String ciudad = sc.nextLine();

                    System.out.println(laboratorioDAO.registrar(nombre, ciudad) ? "Creado." : "Error al crear.");
                }
                case "2" -> {
                    var lista = laboratorioDAO.obtenerTodos();
                    if (lista.isEmpty()) {
                        System.out.println("No hay laboratorios.");
                    } else {
                        lista.forEach(System.out::println);
                    }
                }
                case "3" -> {
                    var lista = laboratorioDAO.obtenerTodos();
                    if (lista.isEmpty()) {
                        System.out.println("No hay laboratorios para actualizar.");
                        break;
                    }

                    System.out.println("\nLaboratorios disponibles:");
                    lista.forEach(System.out::println);

                    System.out.print("ID a actualizar: ");
                    int id = Integer.parseInt(sc.nextLine());

                    System.out.print("Nuevo nombre: ");
                    String nombre = sc.nextLine();

                    System.out.print("Nueva ciudad: ");
                    String ciudad = sc.nextLine();

                    System.out.println(laboratorioDAO.actualizar(id, nombre, ciudad) ? "Actualizado." : "Error al actualizar.");
                }
                case "4" -> {
                    var lista = laboratorioDAO.obtenerTodos();
                    if (lista.isEmpty()) {
                        System.out.println("No hay laboratorios para eliminar.");
                        break;
                    }

                    System.out.println("\nLaboratorios disponibles:");
                    lista.forEach(System.out::println);

                    System.out.print("ID a eliminar: ");
                    int id = Integer.parseInt(sc.nextLine());

                    System.out.println(laboratorioDAO.eliminar(id) ? "Eliminado." : "Error al eliminar.");
                }
                case "0" -> {
                    return;
                }
                default -> System.out.println("Opcion invalida.");
            }
        }
    }

    private void menuProductos(Scanner sc) {
        while (true) {
            System.out.println("\n  PRODUCTOS  ");
            System.out.println("1. Crear");
            System.out.println("2. Listar");
            System.out.println("3. Actualizar");
            System.out.println("4. Eliminar");
            System.out.println("0. Volver");
            System.out.print("Opcion: ");

            switch (sc.nextLine()) {
                case "1" -> {
                    var labs = laboratorioDAO.obtenerTodos();
                    if (labs.isEmpty()) {
                        System.out.println("No hay laboratorios registrados. Primero cree uno.");
                        break;
                    }

                    System.out.print("Nombre: ");
                    String nombre = sc.nextLine();

                    System.out.print("Descripcion: ");
                    String descripcion = sc.nextLine();

                    System.out.println("\nLaboratorios disponibles:");
                    labs.forEach(System.out::println);

                    System.out.print("ID Laboratorio: ");
                    int idLab = Integer.parseInt(sc.nextLine());

                    System.out.println(productoDAO.registrar(nombre, descripcion, idLab) ? "Creado." : "Error al crear.");
                }
                case "2" -> {
                    var lista = productoDAO.obtenerTodos();
                    if (lista.isEmpty()) {
                        System.out.println("No hay productos.");
                    } else {
                        lista.forEach(System.out::println);
                    }
                }
                case "3" -> {
                    var productos = productoDAO.obtenerTodos();
                    var labs = laboratorioDAO.obtenerTodos();

                    if (productos.isEmpty()) {
                        System.out.println("No hay productos para actualizar.");
                        break;
                    }
                    if (labs.isEmpty()) {
                        System.out.println("No hay laboratorios registrados.");
                        break;
                    }

                    System.out.println("\nProductos disponibles:");
                    productos.forEach(System.out::println);

                    System.out.print("ID a actualizar: ");
                    int id = Integer.parseInt(sc.nextLine());

                    System.out.print("Nuevo nombre: ");
                    String nombre = sc.nextLine();

                    System.out.print("Nueva descripcion: ");
                    String descripcion = sc.nextLine();

                    System.out.println("\nLaboratorios disponibles:");
                    labs.forEach(System.out::println);

                    System.out.print("ID Laboratorio: ");
                    int idLab = Integer.parseInt(sc.nextLine());

                    System.out.println(productoDAO.actualizar(id, nombre, descripcion, idLab) ? "Actualizado." : "Error al actualizar.");
                }
                case "4" -> {
                    var productos = productoDAO.obtenerTodos();
                    if (productos.isEmpty()) {
                        System.out.println("No hay productos para eliminar.");
                        break;
                    }

                    System.out.println("\nProductos disponibles:");
                    productos.forEach(System.out::println);

                    System.out.print("ID a eliminar: ");
                    int id = Integer.parseInt(sc.nextLine());

                    System.out.println(productoDAO.eliminar(id) ? "Eliminado." : "Error al eliminar.");
                }
                case "0" -> {
                    return;
                }
                default -> System.out.println("Opcion invalida.");
            }
        }
    }

    private void menuPresentaciones(Scanner sc) {
        while (true) {
            System.out.println("\n  PRESENTACIONES  ");
            System.out.println("1. Crear");
            System.out.println("2. Listar");
            System.out.println("3. Actualizar");
            System.out.println("4. Eliminar");
            System.out.println("0. Volver");
            System.out.print("Opcion: ");

            switch (sc.nextLine()) {
                case "1" -> {
                    var productos = productoDAO.obtenerTodos();
                    if (productos.isEmpty()) {
                        System.out.println("No hay productos registrados. Primero cree uno.");
                        break;
                    }

                    System.out.print("Cantidad: ");
                    int cantidad = Integer.parseInt(sc.nextLine());

                    System.out.print("Unidad de medida: ");
                    String unidad = sc.nextLine();

                    System.out.print("Precio: ");
                    BigDecimal precio = new BigDecimal(sc.nextLine());

                    System.out.println("\nProductos disponibles:");
                    productos.forEach(System.out::println);

                    System.out.print("ID Producto: ");
                    int idProducto = Integer.parseInt(sc.nextLine());

                    System.out.println(presentacionDAO.registrar(cantidad, unidad, precio, idProducto) ? "Creada." : "Error al crear.");
                }
                case "2" -> {
                    var lista = presentacionDAO.obtenerTodas();
                    if (lista.isEmpty()) {
                        System.out.println("No hay presentaciones.");
                    } else {
                        lista.forEach(System.out::println);
                    }
                }
                case "3" -> {
                    var presentaciones = presentacionDAO.obtenerTodas();
                    var productos = productoDAO.obtenerTodos();

                    if (presentaciones.isEmpty()) {
                        System.out.println("No hay presentaciones para actualizar.");
                        break;
                    }
                    if (productos.isEmpty()) {
                        System.out.println("No hay productos registrados.");
                        break;
                    }

                    System.out.println("\nPresentaciones disponibles:");
                    presentaciones.forEach(System.out::println);

                    System.out.print("ID a actualizar: ");
                    int id = Integer.parseInt(sc.nextLine());

                    System.out.print("Nueva cantidad: ");
                    int cantidad = Integer.parseInt(sc.nextLine());

                    System.out.print("Nueva unidad de medida: ");
                    String unidad = sc.nextLine();

                    System.out.print("Nuevo precio: ");
                    BigDecimal precio = new BigDecimal(sc.nextLine());

                    System.out.println("\nProductos disponibles:");
                    productos.forEach(System.out::println);

                    System.out.print("ID Producto: ");
                    int idProducto = Integer.parseInt(sc.nextLine());

                    System.out.println(presentacionDAO.actualizar(id, cantidad, unidad, precio, idProducto) ? "Actualizada." : "Error al actualizar.");
                }
                case "4" -> {
                    var presentaciones = presentacionDAO.obtenerTodas();
                    if (presentaciones.isEmpty()) {
                        System.out.println("No hay presentaciones para eliminar.");
                        break;
                    }

                    System.out.println("\nPresentaciones disponibles:");
                    presentaciones.forEach(System.out::println);

                    System.out.print("ID a eliminar: ");
                    int id = Integer.parseInt(sc.nextLine());

                    System.out.println(presentacionDAO.eliminar(id) ? "Eliminada." : "Error al eliminar.");
                }
                case "0" -> {
                    return;
                }
                default -> System.out.println("Opcion invalida.");
            }
        }
    }

    private void menuLotes(Scanner sc) {
        while (true) {
            System.out.println("\n  LOTES  ");
            System.out.println("1. Crear");
            System.out.println("2. Listar");
            System.out.println("3. Actualizar");
            System.out.println("4. Eliminar");
            System.out.println("0. Volver");
            System.out.print("Opcion: ");

            switch (sc.nextLine()) {
                case "1" -> {
                    var presentaciones = presentacionDAO.obtenerTodas();
                    if (presentaciones.isEmpty()) {
                        System.out.println("No hay presentaciones registradas. Primero cree una.");
                        break;
                    }

                    System.out.print("Fecha ingreso (YYYY-MM-DD): ");
                    LocalDate ingreso = LocalDate.parse(sc.nextLine());

                    System.out.print("Fecha vencimiento (YYYY-MM-DD): ");
                    LocalDate vencimiento = LocalDate.parse(sc.nextLine());

                    System.out.print("Cantidad: ");
                    int cantidad = Integer.parseInt(sc.nextLine());

                    System.out.println("\nPresentaciones disponibles:");
                    presentaciones.forEach(System.out::println);

                    System.out.print("ID Presentacion: ");
                    int idPresentacion = Integer.parseInt(sc.nextLine());

                    System.out.println(loteDAO.registrar(ingreso, vencimiento, cantidad, idPresentacion) ? "Creado." : "Error al crear.");
                }
                case "2" -> {
                    var lista = loteDAO.obtenerTodos();
                    if (lista.isEmpty()) {
                        System.out.println("No hay lotes.");
                    } else {
                        lista.forEach(System.out::println);
                    }
                }
                case "3" -> {
                    var lotes = loteDAO.obtenerTodos();
                    var presentaciones = presentacionDAO.obtenerTodas();

                    if (lotes.isEmpty()) {
                        System.out.println("No hay lotes para actualizar.");
                        break;
                    }
                    if (presentaciones.isEmpty()) {
                        System.out.println("No hay presentaciones registradas.");
                        break;
                    }

                    System.out.println("\nLotes disponibles:");
                    lotes.forEach(System.out::println);

                    System.out.print("ID a actualizar: ");
                    int id = Integer.parseInt(sc.nextLine());

                    System.out.print("Fecha ingreso (YYYY-MM-DD): ");
                    LocalDate ingreso = LocalDate.parse(sc.nextLine());

                    System.out.print("Fecha vencimiento (YYYY-MM-DD): ");
                    LocalDate vencimiento = LocalDate.parse(sc.nextLine());

                    System.out.print("Cantidad: ");
                    int cantidad = Integer.parseInt(sc.nextLine());

                    System.out.println("\nPresentaciones disponibles:");
                    presentaciones.forEach(System.out::println);

                    System.out.print("ID Presentacion: ");
                    int idPresentacion = Integer.parseInt(sc.nextLine());

                    System.out.println(loteDAO.actualizar(id, ingreso, vencimiento, cantidad, idPresentacion) ? "Actualizado." : "Error al actualizar.");
                }
                case "4" -> {
                    var lotes = loteDAO.obtenerTodos();
                    if (lotes.isEmpty()) {
                        System.out.println("No hay lotes para eliminar.");
                        break;
                    }

                    System.out.println("\nLotes disponibles:");
                    lotes.forEach(System.out::println);

                    System.out.print("ID a eliminar: ");
                    int id = Integer.parseInt(sc.nextLine());

                    System.out.println(loteDAO.eliminar(id) ? "Eliminado." : "Error al eliminar.");
                }
                case "0" -> {
                    return;
                }
                default -> System.out.println("Opcion invalida.");
            }
        }
    }
}
