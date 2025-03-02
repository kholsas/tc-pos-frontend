package za.co.tech.code.pointofsale;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import javax.print.*;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TcPosApplication extends JFrame {
    private JTextField barcodeField;
    private JTable cartTable;
    private DefaultTableModel tableModel;
    private JLabel totalLabel;
    private List<SaleItem> cart;
    private double total;

    public TcPosApplication() {
        cart = new ArrayList<>();
        total = 0.0;

        setTitle("TC-POS - Cashier Interface");
        setSize(700, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(Color.WHITE);

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        topPanel.setBackground(new Color(240, 240, 240));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JLabel barcodeLabel = new JLabel("Enter Barcode:");
        barcodeLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        topPanel.add(barcodeLabel);
        barcodeField = new JTextField(20);
        barcodeField.setFont(new Font("Arial", Font.PLAIN, 14));
        topPanel.add(barcodeField);
        JButton scanButton = new JButton("Scan");
        scanButton.setFont(new Font("Arial", Font.PLAIN, 14));
        scanButton.setPreferredSize(new Dimension(100, 30));
        topPanel.add(scanButton);
        add(topPanel, BorderLayout.NORTH);

        String[] columns = {"Product", "Price", "Quantity", "Subtotal"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        cartTable = new JTable(tableModel);
        cartTable.setFont(new Font("Arial", Font.PLAIN, 12));
        cartTable.setRowHeight(25);
        cartTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        cartTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        cartTable.getColumnModel().getColumn(0).setPreferredWidth(250);
        cartTable.getColumnModel().getColumn(1).setPreferredWidth(60);
        cartTable.getColumnModel().getColumn(2).setPreferredWidth(60);
        cartTable.getColumnModel().getColumn(3).setPreferredWidth(80);
        JScrollPane scrollPane = new JScrollPane(cartTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        add(scrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        bottomPanel.setBackground(new Color(240, 240, 240));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        totalLabel = new JLabel("Total: R0.00");
        totalLabel.setFont(new Font("Arial", Font.BOLD, 18));
        totalLabel.setForeground(new Color(0, 128, 0));
        bottomPanel.add(totalLabel);
        JButton removeButton = new JButton("Remove Selected");
        removeButton.setFont(new Font("Arial", Font.PLAIN, 14));
        removeButton.setPreferredSize(new Dimension(150, 30));
        bottomPanel.add(removeButton);
        JButton checkoutButton = new JButton("Checkout");
        checkoutButton.setFont(new Font("Arial", Font.PLAIN, 14));
        checkoutButton.setPreferredSize(new Dimension(120, 30));
        checkoutButton.setBackground(new Color(0, 153, 76));
        checkoutButton.setForeground(Color.WHITE);
        bottomPanel.add(checkoutButton);
        add(bottomPanel, BorderLayout.SOUTH);

        scanButton.addActionListener(e -> scanBarcode());
        barcodeField.addActionListener(e -> scanButton.doClick());
        removeButton.addActionListener(e -> removeSelectedItem());
        checkoutButton.addActionListener(e -> checkout());

        barcodeField.requestFocus();
    }

    private void scanBarcode() {
        String barcode = barcodeField.getText().trim();
        if (!barcode.isEmpty()) {
            addItemToCart(barcode);
            barcodeField.setText("");
            barcodeField.requestFocus();
        }
    }

    private void addItemToCart(String barcode) {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpGet request = new HttpGet("http://localhost:8080/api/products/" + barcode);
            try (CloseableHttpResponse response = client.execute(request)) {
                String json = EntityUtils.toString(response.getEntity());
                ObjectMapper mapper = new ObjectMapper();
                Product product = mapper.readValue(json, Product.class);
                if (product != null && product.getStock() > 0) {
                    SaleItem existingItem = null;
                    for (SaleItem item : cart) {
                        if (item.getProduct().getBarcode().equals(product.getBarcode())) {
                            existingItem = item;
                            break;
                        }
                    }
                    if (existingItem != null) {
                        existingItem.setQuantity(existingItem.getQuantity() + 1);
                    } else {
                        SaleItem newItem = new SaleItem(product, 1, product.getPrice());
                        cart.add(newItem);
                    }
                    calculateTotal();
                    updateCartDisplay();
                } else {
                    JOptionPane.showMessageDialog(this, "Product not found or out of stock!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error connecting to backend!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void removeSelectedItem() {
        int selectedRow = cartTable.getSelectedRow();
        if (selectedRow >= 0 && selectedRow < cart.size()) {
            cart.remove(selectedRow);
            calculateTotal();
            updateCartDisplay();
        }
    }

    private void calculateTotal() {
        total = 0.0;
        for (SaleItem item : cart) {
            item.setSubtotal(item.getProduct().getPrice() * item.getQuantity());
            total += item.getSubtotal();
        }
    }

    private void updateCartDisplay() {
        tableModel.setRowCount(0);
        for (SaleItem item : cart) {
            Object[] row = {item.getProduct().getName(), item.getProduct().getPrice(), item.getQuantity(), item.getSubtotal()};
            tableModel.addRow(row);
        }
        totalLabel.setText(String.format("Total: R%.2f", total));
    }

    private void checkout() {
        if (cart.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Cart is empty!", "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost request = new HttpPost("http://localhost:8080/api/sales");
            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(cart);
            request.setEntity(new StringEntity(json));
            request.setHeader("Content-Type", "application/json");
            try (CloseableHttpResponse response = client.execute(request)) {
                printReceipt();
                cart.clear();
                calculateTotal();
                updateCartDisplay();
                JOptionPane.showMessageDialog(this, "Sale completed!", "Success", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error processing sale!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void printReceipt() {
        StringBuilder receipt = new StringBuilder();
        receipt.append("----- TC-POS Receipt -----\n");
        receipt.append("Date: ").append(new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(new Date())).append("\n");
        receipt.append("--------------------------\n");
        for (SaleItem item : cart) {
            receipt.append(String.format("%-20s R%6.2f x %2d R%6.2f\n",
                    item.getProduct().getName(), item.getProduct().getPrice(), item.getQuantity(), item.getSubtotal()));
        }
        receipt.append("--------------------------\n");
        receipt.append(String.format("Total: R%.2f\n", total));
        receipt.append("--------------------------\n");

        try {
            PrintService[] services = PrintServiceLookup.lookupPrintServices(null, null);
            if (services.length == 0) {
                System.out.println("No printers found. Printing to console:\n" + receipt);
                return;
            }
            PrintService printer = services[0];
            DocPrintJob job = printer.createPrintJob();
            PrintRequestAttributeSet attrs = new HashPrintRequestAttributeSet();
            Doc doc = new SimpleDoc(new ByteArrayInputStream(receipt.toString().getBytes()), DocFlavor.INPUT_STREAM.AUTOSENSE, null);
            job.print(doc, attrs);
            System.out.println("Receipt printed successfully.");
        } catch (PrintException e) {
            System.err.println("Printing failed: " + e.getMessage());
            System.out.println("Fallback to console:\n" + receipt);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            TcPosApplication app = new TcPosApplication();
            app.setLocationRelativeTo(null);
            app.setVisible(true);
        });
    }
}