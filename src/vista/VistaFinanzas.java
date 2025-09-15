package vista;

import controlador.ControladorFinanzas;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.SQLException;
import java.time.LocalDate;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import modelo.RegistroFinanciero;
import modelo.RegistroFinanciero.Frecuencia;
import java.util.List;

public class VistaFinanzas extends JFrame {
    private ControladorFinanzas controlador = new ControladorFinanzas();
    private JLabel lblBalance = new JLabel("Balance: 0.00");
    private JComboBox<String> cmbTipo = new JComboBox<>(new String[]{"Ingreso", "Deuda Pasiva", "Deuda Activa"});
    private JTable tablaRegistros;
    private DefaultTableModel tableModel;
    private JPanel panelDinamico;
    private JTextField txtNombre;
    private JTextArea txtDescripcion;

    public VistaFinanzas() {
        super("Sistema de Balance Financiero");
        configurarUI();
    }

    private void configurarUI() {
        setLayout(new BorderLayout());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        // Panel de registro
        JPanel panelRegistro = new JPanel(new BorderLayout(10, 10));
        panelRegistro.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel panelFormulario = new JPanel(new GridLayout(0, 2, 5, 5));
        panelFormulario.add(new JLabel("Tipo:"));
        panelFormulario.add(cmbTipo);
        panelFormulario.add(new JLabel("Nombre:"));
        txtNombre = new JTextField();
        panelFormulario.add(txtNombre);
        panelFormulario.add(new JLabel("Descripción:"));
        txtDescripcion = new JTextArea(2, 20);
        txtDescripcion.setLineWrap(true);
        panelFormulario.add(new JScrollPane(txtDescripcion));

        panelDinamico = new JPanel(new GridLayout(0, 2, 5, 5));
        panelFormulario.add(panelDinamico);

        cmbTipo.addActionListener(this::cambiarFormulario);

        JButton btnRegistrar = new JButton("Registrar");
        btnRegistrar.addActionListener(e -> registrarAccion());

        panelRegistro.add(panelFormulario, BorderLayout.CENTER);
        panelRegistro.add(btnRegistrar, BorderLayout.SOUTH);

        // Panel de balance y botones
        JPanel panelBalance = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        lblBalance.setFont(new Font("Arial", Font.BOLD, 16));
        panelBalance.add(lblBalance);

        JButton btnCierre = new JButton("Cierre Mensual");
        btnCierre.addActionListener(e -> realizarCierre());
        panelBalance.add(btnCierre);

        JButton btnRefrescar = new JButton("Refrescar");
        btnRefrescar.addActionListener(e -> actualizarUI());
        panelBalance.add(btnRefrescar);

        JButton btnHistorial = new JButton("Historial");
        btnHistorial.addActionListener(e -> abrirHistorialCierres());
        panelBalance.add(btnHistorial);

        // Área de registros con JTable
        String[] columnNames = {"ID", "Descripción", "Monto", "Fecha", "Estado", "Acción"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 5;
            }
        };
        tablaRegistros = new JTable(tableModel);
        tablaRegistros.setRowHeight(30);

        tablaRegistros.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                String estado = (String) table.getValueAt(row, 4);
                if ("SALDADA".equals(estado)) {
                    c.setBackground(new Color(204, 255, 255)); // Azul claro
                } else if ("ACTIVO".equals(estado)) {
                    String tipo = (String) table.getValueAt(row, 1);
                    if (tipo.contains("Ingreso")) {
                        c.setBackground(new Color(204, 255, 204)); // Verde claro
                    } else {
                        c.setBackground(new Color(255, 204, 204)); // Rojo claro
                    }
                } else {
                    c.setBackground(Color.WHITE);
                }
                return c;
            }
        });

        tablaRegistros.getColumnModel().getColumn(5).setCellRenderer(new ButtonRenderer());
        tablaRegistros.getColumnModel().getColumn(5).setCellEditor(new ButtonEditor(new JTextField(), this));

        JScrollPane scrollRegistros = new JScrollPane(tablaRegistros);

        add(panelRegistro, BorderLayout.NORTH);
        add(scrollRegistros, BorderLayout.CENTER);
        add(panelBalance, BorderLayout.SOUTH);

        cambiarFormulario(null);
        actualizarUI();
    }

    private void cambiarFormulario(ActionEvent e) {
        panelDinamico.removeAll();
        int tipo = cmbTipo.getSelectedIndex();
        switch (tipo) {
            case 0: // Ingreso
                JTextField txtMontoIngreso = new JTextField();
                panelDinamico.add(new JLabel("Monto:"));
                panelDinamico.add(txtMontoIngreso);
                panelDinamico.putClientProperty("monto", txtMontoIngreso);
                break;
            case 1: // Deuda Pasiva
                JTextField txtMontoDeudaPasiva = new JTextField();
                JComboBox<String> cmbFrecuencia = new JComboBox<>(new String[]{"DIARIO", "SEMANAL", "MENSUAL"});
                panelDinamico.add(new JLabel("Monto:"));
                panelDinamico.add(txtMontoDeudaPasiva);
                panelDinamico.add(new JLabel("Frecuencia:"));
                panelDinamico.add(cmbFrecuencia);
                panelDinamico.putClientProperty("monto", txtMontoDeudaPasiva);
                panelDinamico.putClientProperty("frecuencia", cmbFrecuencia);
                break;
            case 2: // Deuda Activa
                JTextField txtMontoActiva = new JTextField();
                JTextField txtInicial = new JTextField();
                JTextField txtCuotas = new JTextField();
                panelDinamico.add(new JLabel("Monto Total:"));
                panelDinamico.add(txtMontoActiva);
                panelDinamico.add(new JLabel("Inicial:"));
                panelDinamico.add(txtInicial);
                panelDinamico.add(new JLabel("Cuotas:"));
                panelDinamico.add(txtCuotas);
                panelDinamico.putClientProperty("montoTotal", txtMontoActiva);
                panelDinamico.putClientProperty("inicial", txtInicial);
                panelDinamico.putClientProperty("cuotas", txtCuotas);
                break;
        }
        panelDinamico.revalidate();
        panelDinamico.repaint();
    }

    private void registrarAccion() {
        try {
            int tipo = cmbTipo.getSelectedIndex();
            String nombre = txtNombre.getText();
            String descripcion = txtDescripcion.getText();

            switch (tipo) {
                case 0: // Ingreso
                    JTextField txtMontoIngreso = (JTextField) panelDinamico.getClientProperty("monto");
                    double montoIngreso = Double.parseDouble(txtMontoIngreso.getText());
                    controlador.registrarIngreso(nombre, descripcion, montoIngreso, LocalDate.now());
                    break;
                case 1: // Deuda Pasiva
                    JTextField txtMontoDeudaPasiva = (JTextField) panelDinamico.getClientProperty("monto");
                    JComboBox<String> cmbFrecuencia = (JComboBox<String>) panelDinamico.getClientProperty("frecuencia");
                    double montoDeudaPasiva = Double.parseDouble(txtMontoDeudaPasiva.getText());
                    Frecuencia frecuencia = Frecuencia.valueOf(cmbFrecuencia.getSelectedItem().toString());
                    controlador.registrarDeudaPasiva(nombre, descripcion, LocalDate.now(), frecuencia, montoDeudaPasiva);
                    break;
                case 2: // Deuda Activa
                    JTextField txtMontoActiva = (JTextField) panelDinamico.getClientProperty("montoTotal");
                    JTextField txtInicial = (JTextField) panelDinamico.getClientProperty("inicial");
                    JTextField txtCuotas = (JTextField) panelDinamico.getClientProperty("cuotas");
                    double montoTotal = Double.parseDouble(txtMontoActiva.getText());
                    double inicial = Double.parseDouble(txtInicial.getText());
                    int cuotas = Integer.parseInt(txtCuotas.getText());
                    controlador.registrarDeudaActiva(nombre, descripcion, LocalDate.now(), LocalDate.now().plusMonths(cuotas), montoTotal, inicial, cuotas);
                    break;
            }
            JOptionPane.showMessageDialog(this, "Registro creado con éxito.");
            limpiarCampos();
            actualizarUI();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Error: Formato numérico inválido");
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error de base de datos: " + ex.getMessage());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private void limpiarCampos() {
        txtNombre.setText("");
        txtDescripcion.setText("");
        cambiarFormulario(null);
    }

    private void realizarCierre() {
        try {
            controlador.realizarCierreMensual();
            JOptionPane.showMessageDialog(this, "Cierre mensual realizado con éxito");
            actualizarUI();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al realizar cierre: " + ex.getMessage());
        }
    }

    public void actualizarUI() {
        try {
            actualizarBalance();
            actualizarTablaRegistros();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al actualizar datos: " + ex.getMessage());
        }
    }

    private void actualizarBalance() throws SQLException {
        double balance = controlador.calcularBalance();
        lblBalance.setText(String.format("Balance: %.2f", balance));
        lblBalance.setForeground(balance >= 0 ? new Color(0, 153, 0) : Color.RED);
    }

    private void actualizarTablaRegistros() throws SQLException {
        tableModel.setRowCount(0);
        List<RegistroFinanciero> registros = controlador.getRegistrosActivos();
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
            Object[] fila = new Object[6];
            fila[0] = reg.getId();
            fila[1] = descripcionCompleta;
            fila[2] = String.format("%,.2f", reg.getMonto());
            fila[3] = (reg.getFecha() != null) ? reg.getFecha().toString() : (reg.getFechaInicio() != null ? reg.getFechaInicio().toString() : "N/A");
            fila[4] = reg.getEstado() != null ? reg.getEstado().name() : "ACTIVO";
            fila[5] = (reg.getTipo() == RegistroFinanciero.Tipo.DEUDA_ACTIVA) ? "Ver Detalles" : "";
            tableModel.addRow(fila);
        }
    }

    private void abrirHistorialCierres() {
        try {
            VistaHistorialCierres historial = new VistaHistorialCierres();
            historial.setVisible(true);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al cargar historial de cierres: " + ex.getMessage());
        }
    }

    // Clases ButtonRenderer y ButtonEditor
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
        private VistaFinanzas parentFrame;

        public ButtonEditor(JTextField textField, VistaFinanzas parentFrame) {
            super(textField);
            this.parentFrame = parentFrame;
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
                if ("Ver Detalles".equals(label)) {
                    try {
                        int idDeuda = (int) tablaRegistros.getValueAt(this.row, 0);
                        VistaDetalleDeuda detalle = new VistaDetalleDeuda(idDeuda, parentFrame);
                        detalle.setVisible(true);
                    } catch (SQLException ex) {
                        JOptionPane.showMessageDialog(null, "Error al cargar detalles de la deuda: " + ex.getMessage());
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

    /**
     * El método main es el punto de entrada de la aplicación.
     * Crea una instancia de VistaFinanzas y la hace visible.
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            VistaFinanzas frame = new VistaFinanzas();
            frame.setVisible(true);
        });
    }
}