package com.felip.controller;

import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDate;
import java.util.Map;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.felip.model.Laboratorio;
import com.felip.model.Lote;
import com.felip.model.Presentacion;
import com.felip.model.Producto;
import com.felip.repository.LaboratorioRepository;
import com.felip.repository.LoteRepository;
import com.felip.repository.PresentacionRepository;
import com.felip.repository.ProductoRepository;


@RestController
@RequestMapping
public class InventarioWriteController {
    private final LaboratorioRepository laboratorios;
    private final ProductoRepository productos;
    private final PresentacionRepository presentaciones;
    private final LoteRepository lotes;

    public InventarioWriteController(
            LaboratorioRepository laboratorios,
            ProductoRepository productos,
            PresentacionRepository presentaciones,
            LoteRepository lotes) {
        this.laboratorios = laboratorios;
        this.productos = productos;
        this.presentaciones = presentaciones;
        this.lotes = lotes;
    }

    @PostMapping("/laboratorios")
    public ResponseEntity<?> crearLaboratorio(@RequestBody LaboratorioInput input) {
        String nombre = required(input.nombre(), "El nombre del laboratorio");
        String ciudad = required(input.ciudad(), "La ciudad");
        if (nombre.length() > 100 || ciudad.length() > 100) return badRequest("Nombre y ciudad deben tener máximo 100 caracteres.");
        Laboratorio saved = laboratorios.save(new Laboratorio(nombre, ciudad));
        return ResponseEntity.created(URI.create("/laboratorios/" + saved.getIdLaboratorio())).body(saved);
    }

    @PutMapping("/laboratorios/{id}")
    public ResponseEntity<?> actualizarLaboratorio(@PathVariable int id, @RequestBody LaboratorioInput input) {
        Laboratorio laboratorio = laboratorios.findById(id).orElse(null);
        if (laboratorio == null) return notFound("No encontramos ese laboratorio.");
        String nombre = required(input.nombre(), "El nombre del laboratorio");
        String ciudad = required(input.ciudad(), "La ciudad");
        if (nombre.length() > 100 || ciudad.length() > 100) return badRequest("Nombre y ciudad deben tener máximo 100 caracteres.");
        laboratorio.setNombre(nombre);
        laboratorio.setCiudad(ciudad);
        return ResponseEntity.ok(laboratorios.save(laboratorio));
    }

    @PostMapping("/productos")
    @Transactional
    public ResponseEntity<?> crearProducto(@RequestBody ProductoInput input) {
        String error = validateProduct(input, true);
        if (error != null) return badRequest(error);
        Laboratorio laboratorio = laboratorios.findById(input.idLaboratorio()).orElse(null);
        if (laboratorio == null) return notFound("Selecciona un laboratorio registrado.");

        Producto producto = productos.save(new Producto(input.nombre().trim(), clean(input.descripcion()), laboratorio));
        presentaciones.save(new Presentacion(input.cantidadPresentacion(), input.unidadMedida().trim(),
                input.precio(), producto));
        return ResponseEntity.created(URI.create("/productos/" + producto.getIdProducto())).body(producto);
    }

    @PutMapping("/productos/{id}")
    @Transactional
    public ResponseEntity<?> actualizarProducto(@PathVariable int id, @RequestBody ProductoInput input) {
        String error = validateProduct(input, false);
        if (error != null) return badRequest(error);
        Producto producto = productos.findById(id).orElse(null);
        Laboratorio laboratorio = laboratorios.findById(input.idLaboratorio()).orElse(null);
        if (producto == null) return notFound("No encontramos ese producto.");
        if (laboratorio == null) return notFound("Selecciona un laboratorio registrado.");
        producto.setNombre(input.nombre().trim());
        producto.setDescripcion(clean(input.descripcion()));
        producto.setLaboratorio(laboratorio);
        Producto saved = productos.save(producto);

        if (input.cantidadPresentacion() != null || input.unidadMedida() != null || input.precio() != null) {
            if (input.cantidadPresentacion() == null || input.unidadMedida() == null || input.precio() == null) {
                return badRequest("Completa los tres datos de la presentación principal.");
            }
            Presentacion principal = presentaciones.findByProducto_IdProducto(id).stream().findFirst().orElse(null);
            if (principal == null) {
                principal = new Presentacion(input.cantidadPresentacion(), input.unidadMedida().trim(), input.precio(), saved);
            } else {
                principal.setCantidad(input.cantidadPresentacion());
                principal.setUnidadMedida(input.unidadMedida().trim());
                principal.setPrecio(input.precio());
            }
            presentaciones.save(principal);
        }
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/productos/{id}")
    @Transactional
    public ResponseEntity<?> eliminarProducto(@PathVariable int id) {
        Producto producto = productos.findById(id).orElse(null);
        if (producto == null) return notFound("No encontramos ese producto; puede que ya se haya eliminado.");

        // A product owns its presentations and lots in the inventory workflow. Delete
        // children explicitly so foreign-key constraints are respected in every DB.
        lotes.deleteAll(lotes.findByPresentacion_Producto_IdProducto(id));
        presentaciones.deleteAll(presentaciones.findByProducto_IdProducto(id));
        productos.delete(producto);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/presentaciones")
    public ResponseEntity<?> crearPresentacion(@RequestBody PresentacionInput input) {
        String error = validatePresentation(input);
        if (error != null) return badRequest(error);
        Producto producto = productos.findById(input.idProducto()).orElse(null);
        if (producto == null) return notFound("Selecciona un producto registrado.");
        Presentacion saved = presentaciones.save(new Presentacion(input.cantidad(), input.unidadMedida().trim(), input.precio(), producto));
        return ResponseEntity.created(URI.create("/presentaciones/" + saved.getIdPresentacion())).body(saved);
    }

    @PutMapping("/presentaciones/{id}")
    public ResponseEntity<?> actualizarPresentacion(@PathVariable int id, @RequestBody PresentacionInput input) {
        String error = validatePresentation(input);
        if (error != null) return badRequest(error);
        Presentacion presentacion = presentaciones.findById(id).orElse(null);
        Producto producto = productos.findById(input.idProducto()).orElse(null);
        if (presentacion == null) return notFound("No encontramos esa presentación.");
        if (producto == null) return notFound("Selecciona un producto registrado.");
        presentacion.setCantidad(input.cantidad());
        presentacion.setUnidadMedida(input.unidadMedida().trim());
        presentacion.setPrecio(input.precio());
        presentacion.setProducto(producto);
        return ResponseEntity.ok(presentaciones.save(presentacion));
    }

    @PostMapping("/lotes")
    public ResponseEntity<?> crearLote(@RequestBody LoteInput input) {
        String error = validateLot(input);
        if (error != null) return badRequest(error);
        Presentacion presentacion = presentaciones.findById(input.idPresentacion()).orElse(null);
        if (presentacion == null) return notFound("Selecciona una presentación registrada.");
        Lote saved = lotes.save(new Lote(input.fechaIngreso(), input.fechaVencimiento(), input.cantidad(), presentacion));
        return ResponseEntity.created(URI.create("/lotes/" + saved.getIdLote())).body(saved);
    }

    @PutMapping("/lotes/{id}")
    public ResponseEntity<?> actualizarLote(@PathVariable int id, @RequestBody LoteInput input) {
        String error = validateLot(input);
        if (error != null) return badRequest(error);
        Lote lote = lotes.findById(id).orElse(null);
        Presentacion presentacion = presentaciones.findById(input.idPresentacion()).orElse(null);
        if (lote == null) return notFound("No encontramos ese lote.");
        if (presentacion == null) return notFound("Selecciona una presentación registrada.");
        lote.setFechaIngreso(input.fechaIngreso());
        lote.setFechaVencimiento(input.fechaVencimiento());
        lote.setCantidad(input.cantidad());
        lote.setPresentacion(presentacion);
        return ResponseEntity.ok(lotes.save(lote));
    }

    private static String validateProduct(ProductoInput input, boolean includePresentation) {
        String nombre = required(input.nombre(), "El nombre del producto");
        if (nombre.length() > 150) return "El nombre del producto debe tener máximo 150 caracteres.";
        if (input.idLaboratorio() == null) return "Selecciona un laboratorio.";
        if (includePresentation || input.cantidadPresentacion() != null || input.unidadMedida() != null || input.precio() != null) {
            if (input.cantidadPresentacion() == null || input.cantidadPresentacion() < 1) return "La cantidad de la presentación debe ser mayor que cero.";
            String unidad = required(input.unidadMedida(), "La unidad de medida");
            if (unidad.length() > 100) return "La unidad de medida debe tener máximo 100 caracteres.";
            if (input.precio() == null || input.precio().compareTo(BigDecimal.ZERO) < 0) return "El precio no puede ser negativo.";
        }
        return null;
    }

    private static String validatePresentation(PresentacionInput input) {
        if (input.cantidad() == null || input.cantidad() < 1) return "La cantidad debe ser mayor que cero.";
        String unidad = required(input.unidadMedida(), "La unidad de medida");
        if (unidad.length() > 100) return "La unidad de medida debe tener máximo 100 caracteres.";
        if (input.precio() == null || input.precio().compareTo(BigDecimal.ZERO) < 0) return "El precio no puede ser negativo.";
        if (input.idProducto() == null) return "Selecciona un producto.";
        return null;
    }

    private static String validateLot(LoteInput input) {
        if (input.fechaIngreso() == null || input.fechaVencimiento() == null) return "Completa las dos fechas.";
        if (input.fechaVencimiento().isBefore(input.fechaIngreso())) return "El vencimiento no puede ser anterior al ingreso.";
        if (input.cantidad() == null || input.cantidad() < 1) return "La cantidad del lote debe ser mayor que cero.";
        if (input.idPresentacion() == null) return "Selecciona una presentación.";
        return null;
    }

    private static String required(String value, String label) {
        if (value == null || value.isBlank()) throw new InvalidInput(label + " es obligatorio.");
        return value.trim();
    }

    private static String clean(String value) { return value == null || value.isBlank() ? null : value.trim(); }

    private static ResponseEntity<Map<String, String>> badRequest(String message) {
        return ResponseEntity.badRequest().body(Map.of("message", message));
    }

    private static ResponseEntity<Map<String, String>> notFound(String message) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", message));
    }

    public record LaboratorioInput(String nombre, String ciudad) {}
    public record ProductoInput(String nombre, String descripcion, Integer idLaboratorio,
            Integer cantidadPresentacion, String unidadMedida, BigDecimal precio) {}
    public record PresentacionInput(Integer cantidad, String unidadMedida, BigDecimal precio, Integer idProducto) {}
    public record LoteInput(LocalDate fechaIngreso, LocalDate fechaVencimiento, Integer cantidad, Integer idPresentacion) {}

    static final class InvalidInput extends RuntimeException {
        private static final long serialVersionUID = 1L;
        InvalidInput(String message) { super(message); }
    }
}

@RestControllerAdvice
class InventarioWriteErrors {
    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<Map<String, String>> conflict(DataIntegrityViolationException error) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("message", "No se pudo guardar porque el registro está relacionado con otros datos. Revisa los campos e inténtalo de nuevo."));
    }

    @ExceptionHandler(InventarioWriteController.InvalidInput.class)
    ResponseEntity<Map<String, String>> invalid(InventarioWriteController.InvalidInput error) {
        return ResponseEntity.badRequest().body(Map.of("message", error.getMessage()));
    }
}
