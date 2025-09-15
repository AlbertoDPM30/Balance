// MODELO DE BASE DE DATOS:
package modelo;

import java.sql.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper {
    private static final String DB_URL = "jdbc:sqlite:finanzas.db";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");

    static {
        try {
            crearTablas();
        } catch (SQLException e) {
            System.err.println("Error al crear tablas: " + e.getMessage());
        }
    }

    private static void crearTablas() throws SQLException {
        String sqlRegistros = "CREATE TABLE IF NOT EXISTS registros (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "tipo TEXT NOT NULL CHECK(tipo IN ('INGRESO', 'DEUDA_ACTIVA', 'DEUDA_PASIVA'))," +
                "nombre TEXT NOT NULL," +
                "descripcion TEXT," +
                "monto REAL CHECK(monto > 0)," +
                "fecha TEXT," +
                "fecha_inicio TEXT," +
                "fecha_culminacion TEXT," +
                "frecuencia TEXT CHECK(frecuencia IN ('DIARIO', 'SEMANAL', 'MENSUAL', 'TRIMESTRAL', 'SEMESTRAL', 'ANUAL', NULL))," +
                "estado TEXT CHECK(estado IN ('ACTIVO', 'SALDADA', 'INACTIVO', NULL)) DEFAULT 'ACTIVO'," +
                "monto_total REAL CHECK(monto_total > 0 OR monto_total IS NULL)," +
                "inicial REAL CHECK(inicial >= 0 OR inicial IS NULL)," +
                "cuotas INTEGER CHECK(cuotas >= 0 OR cuotas IS NULL)," +
                "cerrado INTEGER DEFAULT 0" + // Nueva columna
                ");";

        String sqlCierres = "CREATE TABLE IF NOT EXISTS cierres_mensuales (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "mes TEXT NOT NULL UNIQUE," +
                "balance REAL NOT NULL," +
                "fecha_creacion TEXT NOT NULL" + // Nuevo campo para la fecha de creación del cierre
                ");";

        String sqlAbonos = "CREATE TABLE IF NOT EXISTS abonos (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "id_deuda INTEGER," +
                "monto REAL NOT NULL," +
                "fecha TEXT NOT NULL," +
                "FOREIGN KEY (id_deuda) REFERENCES registros(id)" +
                ");";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
            stmt.execute(sqlRegistros);
            stmt.execute(sqlCierres);
            stmt.execute(sqlAbonos);
        }
    }

    public static Connection conectar() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    public static void insertarRegistro(RegistroFinanciero registro) throws SQLException {
        String sql = "INSERT INTO registros (tipo, nombre, descripcion, monto, fecha, " +
                "fecha_inicio, fecha_culminacion, frecuencia, estado, monto_total, inicial, cuotas, cerrado) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, registro.getTipo().name());
            pstmt.setString(2, registro.getNombre());
            pstmt.setString(3, registro.getDescripcion());

            if (registro.getTipo() == RegistroFinanciero.Tipo.INGRESO) {
                pstmt.setDouble(4, registro.getMonto());
                pstmt.setString(5, registro.getFecha().format(DATE_FORMATTER));
                pstmt.setNull(6, Types.VARCHAR);
                pstmt.setNull(7, Types.VARCHAR);
                pstmt.setNull(8, Types.VARCHAR);
                pstmt.setNull(9, Types.VARCHAR);
                pstmt.setNull(10, Types.DOUBLE);
                pstmt.setNull(11, Types.DOUBLE);
                pstmt.setNull(12, Types.INTEGER);
                pstmt.setInt(13, registro.getCerrado());
            } else if (registro.getTipo() == RegistroFinanciero.Tipo.DEUDA_PASIVA) {
                pstmt.setDouble(4, registro.getMonto());
                pstmt.setNull(5, Types.VARCHAR);
                pstmt.setString(6, registro.getFechaInicio().format(DATE_FORMATTER));
                pstmt.setString(7, registro.getFechaCulminacion() != null ?
                        registro.getFechaCulminacion().format(DATE_FORMATTER) : null);
                pstmt.setString(8, registro.getFrecuencia().name());
                pstmt.setString(9, registro.getEstado().name());
                pstmt.setNull(10, Types.DOUBLE);
                pstmt.setNull(11, Types.DOUBLE);
                pstmt.setNull(12, Types.INTEGER);
                pstmt.setInt(13, registro.getCerrado());
            } else { // DEUDA_ACTIVA
                // Calculamos el monto de la cuota para que no sea 0 o NULL
                double montoCuota = (registro.getMontoTotal() - registro.getInicial()) / registro.getCuotas();
                pstmt.setDouble(4, montoCuota);
                pstmt.setNull(5, Types.VARCHAR);
                pstmt.setString(6, registro.getFechaInicio().format(DATE_FORMATTER));
                pstmt.setString(7, registro.getFechaCulminacion().format(DATE_FORMATTER));
                pstmt.setNull(8, Types.VARCHAR);
                pstmt.setString(9, registro.getEstado().name());
                pstmt.setDouble(10, registro.getMontoTotal());
                pstmt.setDouble(11, registro.getInicial());
                pstmt.setInt(12, registro.getCuotas());
                pstmt.setInt(13, registro.getCerrado());
            }

            pstmt.executeUpdate();
        }
    }

    public static List<RegistroFinanciero> obtenerRegistrosActivos() throws SQLException {
        List<RegistroFinanciero> registros = new ArrayList<>();
        String sql = "SELECT * FROM registros WHERE cerrado = 0";

        try (Connection conn = conectar();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                RegistroFinanciero registro = mapearRegistro(rs);
                registros.add(registro);
            }
        }
        return registros;
    }

    public static List<RegistroFinanciero> obtenerRegistrosCerrados() throws SQLException {
        List<RegistroFinanciero> registros = new ArrayList<>();
        String sql = "SELECT * FROM registros WHERE cerrado = 1";

        try (Connection conn = conectar();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                RegistroFinanciero registro = mapearRegistro(rs);
                registros.add(registro);
            }
        }
        return registros;
    }

    public static RegistroFinanciero obtenerRegistroPorId(int id) throws SQLException {
        String sql = "SELECT * FROM registros WHERE id = ?";

        try (Connection conn = conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapearRegistro(rs);
                }
            }
        }
        return null;
    }

    private static RegistroFinanciero mapearRegistro(ResultSet rs) throws SQLException {
        RegistroFinanciero.Tipo tipo = RegistroFinanciero.Tipo.valueOf(rs.getString("tipo"));
        String nombre = rs.getString("nombre");
        String descripcion = rs.getString("descripcion");
        int id = rs.getInt("id");

        RegistroFinanciero registro = null;

        if (tipo == RegistroFinanciero.Tipo.INGRESO) {
            double monto = rs.getDouble("monto");
            LocalDate fecha = LocalDate.parse(rs.getString("fecha"), DATE_FORMATTER);
            registro = new RegistroFinanciero(id, tipo, nombre, descripcion, monto, fecha);
        } else if (tipo == RegistroFinanciero.Tipo.DEUDA_PASIVA) {
            double monto = rs.getDouble("monto");
            LocalDate fechaInicio = LocalDate.parse(rs.getString("fecha_inicio"), DATE_FORMATTER);
            String fechaCulmStr = rs.getString("fecha_culminacion");
            LocalDate fechaCulminacion = fechaCulmStr != null ?
                    LocalDate.parse(fechaCulmStr, DATE_FORMATTER) : null;
            RegistroFinanciero.Frecuencia frecuencia = RegistroFinanciero.Frecuencia.valueOf(rs.getString("frecuencia"));
            RegistroFinanciero.Estado estado = RegistroFinanciero.Estado.valueOf(rs.getString("estado"));

            registro = new RegistroFinanciero(id, tipo, nombre, descripcion, fechaInicio, frecuencia, monto);
            registro.setFechaCulminacion(fechaCulminacion);
            registro.setEstado(estado);
        } else { // DEUDA_ACTIVA
            LocalDate fechaInicio = LocalDate.parse(rs.getString("fecha_inicio"), DATE_FORMATTER);
            LocalDate fechaCulminacion = LocalDate.parse(rs.getString("fecha_culminacion"), DATE_FORMATTER);
            double montoTotal = rs.getDouble("monto_total");
            double inicial = rs.getDouble("inicial");
            int cuotas = rs.getInt("cuotas");
            RegistroFinanciero.Estado estado = RegistroFinanciero.Estado.valueOf(rs.getString("estado"));

            registro = new RegistroFinanciero(id, tipo, nombre, descripcion, fechaInicio,
                    fechaCulminacion, montoTotal, inicial, cuotas);
            registro.setEstado(estado);
        }
        registro.setCerrado(rs.getInt("cerrado"));
        return registro;
    }

    public static double calcularBalanceActual() throws SQLException {
        String sql = "SELECT SUM(CASE WHEN tipo = 'INGRESO' THEN monto ELSE -monto END) AS balance " +
                "FROM registros WHERE cerrado = 0";

        try (Connection conn = conectar();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getDouble("balance");
            }
            return 0.0;
        }
    }

    public static void realizarCierreMensual() throws SQLException {
        YearMonth mesActual = YearMonth.now();
        String mesFormato = mesActual.format(MONTH_FORMATTER);

        String sqlVerificarCierre = "SELECT COUNT(*) FROM cierres_mensuales WHERE mes = ?";
        try (Connection conn = conectar();
             PreparedStatement pstmtVerificar = conn.prepareStatement(sqlVerificarCierre)) {
            pstmtVerificar.setString(1, mesFormato);
            ResultSet rs = pstmtVerificar.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                // Si ya existe, no hacemos nada y salimos del método
                return;
            }
        }

        double balance = calcularBalanceActual();

        String sqlCierre = "INSERT INTO cierres_mensuales (mes, balance, fecha_creacion) VALUES (?, ?, ?)";
        String sqlActualizarRegistros = "UPDATE registros SET cerrado = 1 WHERE estado != 'ACTIVO'";

        try (Connection conn = conectar();
             PreparedStatement pstmtCierre = conn.prepareStatement(sqlCierre);
             PreparedStatement pstmtActualizar = conn.prepareStatement(sqlActualizarRegistros)) {

            conn.setAutoCommit(false); // Iniciar transacción

            pstmtCierre.setString(1, mesFormato);
            pstmtCierre.setDouble(2, balance);
            pstmtCierre.setString(3, LocalDate.now().format(DATE_FORMATTER));
            pstmtCierre.executeUpdate();

            pstmtActualizar.executeUpdate();

            conn.commit(); // Confirmar transacción
        }
    }

    public static void registrarAbono(int idDeuda, double montoAbono, LocalDate fechaAbono) throws SQLException {
        String sql = "INSERT INTO abonos (id_deuda, monto, fecha) VALUES (?, ?, ?)";
        try (Connection conn = conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idDeuda);
            pstmt.setDouble(2, montoAbono);
            pstmt.setString(3, fechaAbono.format(DATE_FORMATTER));
            pstmt.executeUpdate();
        }
    }

    public static List<Abono> obtenerAbonosPorDeuda(int idDeuda) throws SQLException {
        List<Abono> abonos = new ArrayList<>();
        String sql = "SELECT * FROM abonos WHERE id_deuda = ?";
        try (Connection conn = conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idDeuda);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    abonos.add(new Abono(
                            rs.getInt("id"),
                            rs.getInt("id_deuda"),
                            rs.getDouble("monto"),
                            LocalDate.parse(rs.getString("fecha"), DATE_FORMATTER)
                    ));
                }
            }
        }
        return abonos;
    }

    public static double calcularTotalAbonos(int idDeuda) throws SQLException {
        String sql = "SELECT SUM(monto) AS totalAbonado FROM abonos WHERE id_deuda = ?";
        try (Connection conn = conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idDeuda);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("totalAbonado");
                }
            }
        }
        return 0.0;
    }

    public static void actualizarEstadoDeuda(int idDeuda, RegistroFinanciero.Estado nuevoEstado) throws SQLException {
        String sql = "UPDATE registros SET estado = ? WHERE id = ?";
        try (Connection conn = conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nuevoEstado.name());
            pstmt.setInt(2, idDeuda);
            pstmt.executeUpdate();
        }
    }

    public static List<CierreMensual> obtenerCierres() throws SQLException {
        List<CierreMensual> cierres = new ArrayList<>();
        String sql = "SELECT * FROM cierres_mensuales ORDER BY mes DESC";
        try (Connection conn = conectar();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while(rs.next()) {
                cierres.add(new CierreMensual(
                        rs.getInt("id"),
                        rs.getString("mes"),
                        rs.getDouble("balance"),
                        rs.getString("fecha_creacion")
                ));
            }
        }
        return cierres;
    }

    public static List<RegistroFinanciero> obtenerRegistrosDeCierre(String mes) throws SQLException {
        List<RegistroFinanciero> registros = new ArrayList<>();
        String sql = "SELECT * FROM registros WHERE fecha_inicio LIKE ? OR fecha LIKE ?";
        String mesPattern = mes + "-%";
        try (Connection conn = conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, mesPattern);
            pstmt.setString(2, mesPattern);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    registros.add(mapearRegistro(rs));
                }
            }
        }
        return registros;
    }
}