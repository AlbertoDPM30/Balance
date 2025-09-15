// Nuevo archivo: Abono.java
package modelo;

import java.time.LocalDate;

public class Abono {
    private int id;
    private int idDeuda;
    private double monto;
    private LocalDate fecha;

    public Abono(int id, int idDeuda, double monto, LocalDate fecha) {
        this.id = id;
        this.idDeuda = idDeuda;
        this.monto = monto;
        this.fecha = fecha;
    }

    // Getters
    public int getId() { return id; }
    public int getIdDeuda() { return idDeuda; }
    public double getMonto() { return monto; }
    public LocalDate getFecha() { return fecha; }
}