package com.felip.model;

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
@Table(name = "producto")
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_producto")
    private Integer idProducto;

    @Column(name = "nombre", nullable = false, length = 150)
    private String nombre;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @ManyToOne
    @JoinColumn(name = "id_laboratorio", nullable = false)
    private Laboratorio laboratorio;

    @JsonIgnore
    @OneToMany(mappedBy = "producto")
    private List<Presentacion> presentaciones;

    public Producto() {}

    public Producto(String nombre, String descripcion, Laboratorio laboratorio) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.laboratorio = laboratorio;
    }

    public Integer getIdProducto() { return idProducto; }
    public void setIdProducto(Integer idProducto) { this.idProducto = idProducto; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public Laboratorio getLaboratorio() { return laboratorio; }
    public void setLaboratorio(Laboratorio laboratorio) { this.laboratorio = laboratorio; }

    public List<Presentacion> getPresentaciones() { return presentaciones; }
    public void setPresentaciones(List<Presentacion> presentaciones) { this.presentaciones = presentaciones; }

    @Override
    public String toString() {
        return idProducto + " - " + nombre + " | Lab: " + laboratorio.getNombre();
    }
}