package controlador;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import modelo.Abono;
import modelo.CierreMensual;
import modelo.DatabaseHelper;
import modelo.RegistroFinanciero;
import modelo.RegistroFinanciero.Frecuencia;
import modelo.RegistroFinanciero.Tipo;
import modelo.RegistroFinanciero.Estado;

public class ControladorFinanzas {
    public void registrarIngreso(String nombre, String descripcion, double monto, LocalDate fecha) throws SQLException {
        RegistroFinanciero registro = new RegistroFinanciero(
                Tipo.INGRESO, nombre, descripcion, monto, fecha
        );
        DatabaseHelper.insertarRegistro(registro);
    }

    public void registrarDeudaPasiva(String nombre, String descripcion, LocalDate fechaInicio,
                                     Frecuencia frecuencia, double monto) throws SQLException {
        RegistroFinanciero registro = new RegistroFinanciero(
                Tipo.DEUDA_PASIVA, nombre, descripcion, fechaInicio, frecuencia, monto
        );
        DatabaseHelper.insertarRegistro(registro);
    }

    public void registrarDeudaActiva(String nombre, String descripcion, LocalDate fechaInicio,
                                     LocalDate fechaCulminacion, double montoTotal, double inicial, int cuotas) throws SQLException {
        RegistroFinanciero registro = new RegistroFinanciero(
                Tipo.DEUDA_ACTIVA, nombre, descripcion, fechaInicio,
                fechaCulminacion, montoTotal, inicial, cuotas
        );
        DatabaseHelper.insertarRegistro(registro);
    }

    public double calcularBalance() throws SQLException {
        return DatabaseHelper.calcularBalanceActual();
    }

    public void realizarCierreMensual() throws SQLException {
        DatabaseHelper.realizarCierreMensual();
    }

    public List<RegistroFinanciero> getRegistrosActivos() throws SQLException {
        return DatabaseHelper.obtenerRegistrosActivos();
    }

    public List<RegistroFinanciero> getRegistrosCerrados() throws SQLException {
        return DatabaseHelper.obtenerRegistrosCerrados();
    }

    public RegistroFinanciero getRegistroPorId(int id) throws SQLException {
        return DatabaseHelper.obtenerRegistroPorId(id);
    }

    public List<Abono> getAbonosPorDeuda(int idDeuda) throws SQLException {
        return DatabaseHelper.obtenerAbonosPorDeuda(idDeuda);
    }

    public double calcularTotalAbonos(int idDeuda) throws SQLException {
        return DatabaseHelper.calcularTotalAbonos(idDeuda);
    }

    public void registrarAbono(int idDeuda, double monto) throws SQLException {
        DatabaseHelper.registrarAbono(idDeuda, monto, LocalDate.now());

        // Lógica para verificar si la deuda ha sido saldada
        RegistroFinanciero deuda = DatabaseHelper.obtenerRegistroPorId(idDeuda);
        if (deuda != null) {
            double totalAbonado = DatabaseHelper.calcularTotalAbonos(idDeuda);
            double montoRestante = deuda.getMontoTotal() - deuda.getInicial() - totalAbonado;
            if (montoRestante <= 0) {
                DatabaseHelper.actualizarEstadoDeuda(idDeuda, Estado.SALDADA);
            }
        }
    }

    public List<CierreMensual> getCierresMensuales() throws SQLException {
        return DatabaseHelper.obtenerCierres();
    }

    public List<RegistroFinanciero> getRegistrosDeCierre(String mes) throws SQLException {
        return DatabaseHelper.obtenerRegistrosDeCierre(mes);
    }
}