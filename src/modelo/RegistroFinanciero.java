package modelo;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class RegistroFinanciero {
    public enum Tipo { INGRESO, DEUDA_ACTIVA, DEUDA_PASIVA }
    public enum Frecuencia { DIARIO, SEMANAL, MENSUAL, TRIMESTRAL, SEMESTRAL, ANUAL }
    public enum Estado { ACTIVO, INACTIVO, SALDADA }

    private int id;
    private Tipo tipo;
    private String nombre;
    private String descripcion;
    private double monto;
    private LocalDate fecha;
    private LocalDate fechaInicio;
    private LocalDate fechaCulminacion;
    private Frecuencia frecuencia;
    private Estado estado;
    private double montoTotal;
    private double inicial;
    private int cuotas;
    private int cerrado = 0;

    // Constructor para Ingresos
    public RegistroFinanciero(Tipo tipo, String nombre, String descripcion, double monto, LocalDate fecha) {
        this.tipo = tipo;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.monto = monto;
        this.fecha = fecha;
        this.estado = Estado.ACTIVO;
    }

    // Constructor para Deuda Pasiva
    public RegistroFinanciero(Tipo tipo, String nombre, String descripcion, LocalDate fechaInicio, Frecuencia frecuencia, double monto) {
        this.tipo = tipo;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.fechaInicio = fechaInicio;
        this.frecuencia = frecuencia;
        this.monto = monto;
        this.estado = Estado.ACTIVO;
    }

    // Constructor para Deuda Activa
    public RegistroFinanciero(Tipo tipo, String nombre, String descripcion, LocalDate fechaInicio, LocalDate fechaCulminacion, double montoTotal, double inicial, int cuotas) {
        this.tipo = tipo;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.fechaInicio = fechaInicio;
        this.fechaCulminacion = fechaCulminacion;
        this.montoTotal = montoTotal;
        this.inicial = inicial;
        this.cuotas = cuotas;
        this.estado = Estado.ACTIVO;
    }

    // Constructores para mapear desde la base de datos (con ID)
    public RegistroFinanciero(int id, Tipo tipo, String nombre, String descripcion, double monto, LocalDate fecha) {
        this(tipo, nombre, descripcion, monto, fecha);
        this.id = id;
    }

    public RegistroFinanciero(int id, Tipo tipo, String nombre, String descripcion, LocalDate fechaInicio, Frecuencia frecuencia, double monto) {
        this(tipo, nombre, descripcion, fechaInicio, frecuencia, monto);
        this.id = id;
    }

    public RegistroFinanciero(int id, Tipo tipo, String nombre, String descripcion, LocalDate fechaInicio, LocalDate fechaCulminacion, double montoTotal, double inicial, int cuotas) {
        this(tipo, nombre, descripcion, fechaInicio, fechaCulminacion, montoTotal, inicial, cuotas);
        this.id = id;
    }

    // Nuevos constructores que incluyen 'cerrado'
    public RegistroFinanciero(int id, Tipo tipo, String nombre, String descripcion, double monto, LocalDate fecha, int cerrado) {
        this(id, tipo, nombre, descripcion, monto, fecha);
        this.cerrado = cerrado;
    }

    public RegistroFinanciero(int id, Tipo tipo, String nombre, String descripcion, LocalDate fechaInicio, Frecuencia frecuencia, double monto, int cerrado) {
        this(id, tipo, nombre, descripcion, fechaInicio, frecuencia, monto);
        this.cerrado = cerrado;
    }

    public RegistroFinanciero(int id, Tipo tipo, String nombre, String descripcion, LocalDate fechaInicio, LocalDate fechaCulminacion, double montoTotal, double inicial, int cuotas, int cerrado) {
        this(id, tipo, nombre, descripcion, fechaInicio, fechaCulminacion, montoTotal, inicial, cuotas);
        this.cerrado = cerrado;
    }

    // Getters y Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public Tipo getTipo() { return tipo; }
    public void setTipo(Tipo tipo) { this.tipo = tipo; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public double getMonto() { return monto; }
    public void setMonto(double monto) { this.monto = monto; }
    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }
    public LocalDate getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDate fechaInicio) { this.fechaInicio = fechaInicio; }
    public LocalDate getFechaCulminacion() { return fechaCulminacion; }
    public void setFechaCulminacion(LocalDate fechaCulminacion) { this.fechaCulminacion = fechaCulminacion; }
    public Frecuencia getFrecuencia() { return frecuencia; }
    public void setFrecuencia(Frecuencia frecuencia) { this.frecuencia = frecuencia; }
    public Estado getEstado() { return estado; }
    public void setEstado(Estado estado) { this.estado = estado; }
    public double getMontoTotal() { return montoTotal; }
    public void setMontoTotal(double montoTotal) { this.montoTotal = montoTotal; }
    public double getInicial() { return inicial; }
    public void setInicial(double inicial) { this.inicial = inicial; }
    public int getCuotas() { return cuotas; }
    public void setCuotas(int cuotas) { this.cuotas = cuotas; }
    public int getCerrado() { return cerrado; }
    public void setCerrado(int cerrado) { this.cerrado = cerrado; }
}