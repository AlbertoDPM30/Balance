package vista;

import controlador.ControladorFinanzas;
import modelo.CierreMensual;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class VistaHistorialCierres extends JFrame {
    private ControladorFinanzas controlador = new ControladorFinanzas();
    private JTable tablaCierres;
    private DefaultTableModel tableModel;

    public VistaHistorialCierres() throws SQLException {
        super("Historial de Cierres Mensuales");
        configurarUI();
        cargarCierres();
    }

    private void configurarUI() {
        setLayout(new BorderLayout());
        setSize(500, 400);
        setLocationRelativeTo(null);

        String[] columnNames = {"ID", "Mes", "Balance", "Fecha Creación", "Acciones"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 4;
            }
        };
        tablaCierres = new JTable(tableModel);

        tablaCierres.getColumnModel().getColumn(4).setCellRenderer(new ButtonRenderer());
        tablaCierres.getColumnModel().getColumn(4).setCellEditor(new ButtonEditor(new JTextField()));

        JScrollPane scrollPane = new JScrollPane(tablaCierres);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void cargarCierres() throws SQLException {
        tableModel.setRowCount(0);
        List<CierreMensual> cierres = controlador.getCierresMensuales();
        for (CierreMensual cierre : cierres) {
            Object[] fila = new Object[5];
            fila[0] = cierre.getId();
            fila[1] = cierre.getMes();
            fila[2] = String.format("%,.2f", cierre.getBalance());
            fila[3] = cierre.getFechaCreacion();
            fila[4] = "Ver Registros";
            tableModel.addRow(fila);
        }
    }

    private class ButtonRenderer extends DefaultTableCellRenderer {
        private final JButton button = new JButton();
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            button.setText((value == null) ? "" : value.toString());
            return button;
        }
    }

    private class ButtonEditor extends DefaultCellEditor {
        private JButton button;
        private String label;
        private boolean isPushed;
        private int row;

        public ButtonEditor(JTextField textField) {
            super(textField);
            button = new JButton();
            button.setOpaque(true);
            button.addActionListener(e -> fireEditingStopped());
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            this.row = row;
            label = (value == null) ? "" : value.toString();
            button.setText(label);
            isPushed = true;
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            if (isPushed) {
                if ("Ver Registros".equals(label)) {
                    String mes = (String) tablaCierres.getValueAt(this.row, 1);
                    try {
                        VistaRegistrosCerrados vistaRegistros = new VistaRegistrosCerrados(mes);
                        vistaRegistros.setVisible(true);
                    } catch (SQLException ex) {
                        JOptionPane.showMessageDialog(null, "Error al cargar registros cerrados: " + ex.getMessage());
                    }
                }
            }
            isPushed = false;
            return label;
        }

        @Override
        public boolean stopCellEditing() {
            isPushed = false;
            return super.stopCellEditing();
        }

        @Override
        protected void fireEditingStopped() {
            super.fireEditingStopped();
        }
    }
}