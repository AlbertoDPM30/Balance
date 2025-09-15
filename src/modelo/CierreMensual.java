package modelo;

public class CierreMensual {
    private int id;
    private String mes;
    private double balance;
    private String fechaCreacion;
    private String meses;

    public CierreMensual(int id, String mes, double balance, String fechaCreacion) {
        this.id = id;
        this.mes = mes;
        this.balance = balance;
        this.fechaCreacion = fechaCreacion;
        this.meses = fechaCreacion;
    }

    // Getters
    public int getId() { return id; }
    public String getMes() { return mes; }
    public double getBalance() { return balance; }
    public String getFechaCreacion() { return fechaCreacion; }
    public String getMeses() { return fechaCreacion; }
}