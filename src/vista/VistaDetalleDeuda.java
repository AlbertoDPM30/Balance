package vista;

import controlador.ControladorFinanzas;
import modelo.Abono;
import modelo.RegistroFinanciero;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class VistaDetalleDeuda extends JFrame {
    private int idDeuda;
    private ControladorFinanzas controlador = new ControladorFinanzas();
    private JLabel lblDeudaInfo;
    private JLabel lblMontoRestante;
    private JTable tablaAbonos;
    private DefaultTableModel tableModel;
    private VistaFinanzas vistaPrincipal;

    public VistaDetalleDeuda(int idDeuda, VistaFinanzas vistaPrincipal) throws SQLException {
        super("Detalles de la Deuda");
        this.idDeuda = idDeuda;
        this.vistaPrincipal = vistaPrincipal;
        configurarUI();
        cargarDetalles();
    }

    private void configurarUI() {
        setLayout(new BorderLayout());
        setSize(600, 400);
        setLocationRelativeTo(null);

        JPanel panelInfo = new JPanel(new GridLayout(2, 1));
        lblDeudaInfo = new JLabel();
        lblMontoRestante = new JLabel();
        panelInfo.add(lblDeudaInfo);
        panelInfo.add(lblMontoRestante);
        add(panelInfo, BorderLayout.NORTH);

        String[] columnNames = {"ID", "Monto", "Fecha"};
        tableModel = new DefaultTableModel(columnNames, 0);
        tablaAbonos = new JTable(tableModel);
        add(new JScrollPane(tablaAbonos), BorderLayout.CENTER);

        JPanel panelAbono = new JPanel();
        JTextField txtMontoAbono = new JTextField(10);
        JButton btnAbonar = new JButton("Abonar");
        panelAbono.add(new JLabel("Monto a Abonar:"));
        panelAbono.add(txtMontoAbono);
        panelAbono.add(btnAbonar);
        add(panelAbono, BorderLayout.SOUTH);

        btnAbonar.addActionListener(e -> {
            try {
                double montoAbono = Double.parseDouble(txtMontoAbono.getText());
                controlador.registrarAbono(idDeuda, montoAbono);
                cargarDetalles();
                vistaPrincipal.actualizarUI(); // Actualizar la tabla principal
                txtMontoAbono.setText("");
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Error: Formato numérico inválido.");
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error al registrar abono: " + ex.getMessage());
            }
        });
    }

    private void cargarDetalles() throws SQLException {
        RegistroFinanciero deuda = controlador.getRegistroPorId(idDeuda);
        if (deuda == null) {
            JOptionPane.showMessageDialog(this, "Deuda no encontrada.");
            return;
        }

        double totalAbonado = controlador.calcularTotalAbonos(idDeuda);
        double montoTotal = deuda.getMontoTotal();
        double inicial = deuda.getInicial();
        double montoRestante = montoTotal - inicial - totalAbonado;

        lblDeudaInfo.setText(String.format("Deuda: %s (Monto Total: %.2f)", deuda.getNombre(), montoTotal));
        lblMontoRestante.setText(String.format("Monto Restante: %.2f", montoRestante));
        lblMontoRestante.setForeground(montoRestante <= 0 ? new Color(0, 153, 0) : Color.RED);

        tableModel.setRowCount(0);
        List<Abono> abonos = controlador.getAbonosPorDeuda(idDeuda);
        for (Abono abono : abonos) {
            tableModel.addRow(new Object[]{abono.getId(), abono.getMonto(), abono.getFecha()});
        }
    }
}