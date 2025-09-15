package vista;

import controlador.ControladorFinanzas;
import modelo.RegistroFinanciero;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class VistaRegistrosCerrados extends JFrame {
    private ControladorFinanzas controlador = new ControladorFinanzas();
    private JTable tablaRegistros;
    private DefaultTableModel tableModel;
    private String mesCierre;

    public VistaRegistrosCerrados(String mes) throws SQLException {
        super("Registros del Cierre de " + mes);
        this.mesCierre = mes;
        configurarUI();
        cargarRegistros();
    }

    private void configurarUI() {
        setLayout(new BorderLayout());
        setSize(800, 600);
        setLocationRelativeTo(null);

        String[] columnNames = {"ID", "Descripción", "Monto", "Fecha"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tablaRegistros = new JTable(tableModel);

        tablaRegistros.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                String descripcion = (String) table.getValueAt(row, 1);
                if (descripcion.startsWith("Deuda")) {
                    c.setBackground(new Color(255, 204, 204));
                } else {
                    c.setBackground(new Color(204, 255, 204));
                }
                return c;
            }
        });

        JScrollPane scrollPane = new JScrollPane(tablaRegistros);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void cargarRegistros() throws SQLException {
        tableModel.setRowCount(0);
        List<RegistroFinanciero> registros = controlador.getRegistrosDeCierre(mesCierre);
        for (RegistroFinanciero reg : registros) {
            String tipo = "";
            switch (reg.getTipo()) {
                case INGRESO:
                    tipo = "Ingreso";
                    break;
                case DEUDA_ACTIVA:
                    tipo = "Deuda Activa";
                    break;
                case DEUDA_PASIVA:
                    tipo = "Deuda Pasiva";
                    break;
            }
            String descripcionCompleta = tipo + " - " + reg.getNombre() + ": " + reg.getDescripcion();
            Object[] fila = new Object[4];
            fila[0] = reg.getId();
            fila[1] = descripcionCompleta;
            fila[2] = String.format("%,.2f", reg.getMonto());
            fila[3] = (reg.getFecha() != null) ? reg.getFecha().toString() : reg.getFechaInicio().toString();
            tableModel.addRow(fila);
        }
    }
}