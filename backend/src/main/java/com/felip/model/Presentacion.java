package com.felip.model;

import java.math.BigDecimal;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "presentacion")
public class Presentacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_presentacion")
    private Integer idPresentacion;

    @Column(name = "cantidad", nullable = false)
    private Integer cantidad;

    @Column(name = "unidad_medida", nullable = false)
    private String unidadMedida;

    @Column(name = "precio", nullable = false)
    private BigDecimal precio;

    @ManyToOne
    @JoinColumn(name = "id_producto", nullable = false)
    private Producto producto;

    @JsonIgnore
    @OneToMany(mappedBy = "presentacion")
    private List<Lote> lotes;

    public Presentacion() {}

    public Presentacion(Integer cantidad, String unidadMedida, BigDecimal precio, Producto producto) {
        this.cantidad = cantidad;
        this.unidadMedida = unidadMedida;
        this.precio = precio;
        this.producto = producto;
    }

    public Integer getIdPresentacion() { return idPresentacion; }
    public void setIdPresentacion(Integer idPresentacion) { this.idPresentacion = idPresentacion; }

    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }

    public String getUnidadMedida() { return unidadMedida; }
    public void setUnidadMedida(String unidadMedida) { this.unidadMedida = unidadMedida; }

    public BigDecimal getPrecio() { return precio; }
    public void setPrecio(BigDecimal precio) { this.precio = precio; }

    public Producto getProducto() { return producto; }
    public void setProducto(Producto producto) { this.producto = producto; }

    public List<Lote> getLotes() { return lotes; }
    public void setLotes(List<Lote> lotes) { this.lotes = lotes; }

    @Override
    public String toString() {
        return idPresentacion + " - " +
                producto.getNombre() +
                " | " + cantidad + " " + unidadMedida +
                " | $" + precio;
    }
}