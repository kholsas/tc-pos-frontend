package za.co.tech.code.pointofsale.frontend;

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
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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
    private JTextField searchField;
    private JList<Product> searchResultsList;
    private DefaultListModel<Product> searchResultsModel;

    public TcPosApplication() {
        cart = new ArrayList<>();
        total = 0.0;

        setTitle("TC-POS - Cashier Interface");
        setSize(1000, 600); // Increased size for better visibility
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(Color.WHITE);

        // Top Panel: Barcode Entry
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

        // Center Panel: Cart Table
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
        JScrollPane cartScrollPane = new JScrollPane(cartTable);
        cartScrollPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

        // Add Key Listener to cartTable for 'Q' (after creating cartTable)
        cartTable.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if ((e.getKeyChar() == 'Q' || e.getKeyChar() == 'q') && cartTable.getSelectedRow() >= 0) {
                    modifyQuantity();
                }
            }
        });

        // Ensure cartTable gets focus when an item is selected
        cartTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && cartTable.getSelectedRow() >= 0) {
                cartTable.requestFocusInWindow(); // Focus the table
            }
        });

        // Right Panel: Search Field and Results
        JPanel rightPanel = new JPanel(new BorderLayout(5, 5));
        rightPanel.setBackground(new Color(240, 240, 240));
        rightPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JLabel searchLabel = new JLabel("Search Products:");
        searchLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        rightPanel.add(searchLabel, BorderLayout.NORTH);
        searchField = new JTextField(20); // Increased width for better usability
        searchField.setFont(new Font("Arial", Font.PLAIN, 14));
        rightPanel.add(searchField, BorderLayout.CENTER);
        searchResultsModel = new DefaultListModel<>();
        searchResultsList = new JList<>(searchResultsModel);
        searchResultsList.setFont(new Font("Arial", Font.PLAIN, 12));
        searchResultsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        searchResultsList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                Product product = (Product) value;
                label.setText(product.getName() + " (R" + String.format("%.2f", product.getPrice()) + ")");
                return label;
            }
        });
        JScrollPane searchScrollPane = new JScrollPane(searchResultsList);
        searchScrollPane.setPreferredSize(new Dimension(250, 400)); // Increased size for visibility
        rightPanel.add(searchScrollPane, BorderLayout.SOUTH);

        // Split Center and Right Panels
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, cartScrollPane, rightPanel);
        splitPane.setDividerLocation(700); // Adjusted for better split
        splitPane.setResizeWeight(0.7); // Cart takes 70% of space initially
        add(splitPane, BorderLayout.CENTER);

        // Bottom Panel: Total and Buttons
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

        // Action Listeners
        scanButton.addActionListener(e -> scanBarcode());
        barcodeField.addActionListener(e -> scanButton.doClick());
        removeButton.addActionListener(e -> removeSelectedItem());
        checkoutButton.addActionListener(e -> checkout());
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { searchProducts(); }
            @Override
            public void removeUpdate(DocumentEvent e) { searchProducts(); }
            @Override
            public void changedUpdate(DocumentEvent e) { searchProducts(); }
        });
        searchResultsList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                Product selected = searchResultsList.getSelectedValue();
                if (selected != null) {
                    addItemToCart(selected.getBarcode());
                    searchResultsList.clearSelection(); // Clear selection after adding
                    searchField.setText(""); // Clear search field after selection
                }
            }
        });

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

    private void searchProducts() {
        String query = searchField.getText().trim();
        if (query.isEmpty()) {
            searchResultsModel.clear();
            return;
        }
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8.toString());
            HttpGet request = new HttpGet("http://localhost:8080/api/products/search?name=" + encodedQuery);
            try (CloseableHttpResponse response = client.execute(request)) {
                String json = EntityUtils.toString(response.getEntity());
                ObjectMapper mapper = new ObjectMapper();
                // Handle empty or null response
                if (json == null || json.trim().isEmpty()) {
                    searchResultsModel.clear();
                    return;
                }
                // Try to parse as JSON array, default to empty array if invalid
                Product[] products = mapper.readValue(json, Product[].class);
                searchResultsModel.clear();
                for (Product product : products) {
                    if (product.getStock() > 0) {
                        searchResultsModel.addElement(product);
                    }
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error searching products: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void modifyQuantity() {
        int selectedRow = cartTable.getSelectedRow();
        if (selectedRow < 0 || selectedRow >= cart.size()) {
            JOptionPane.showMessageDialog(this, "Please select an item in the cart!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        SaleItem item = cart.get(selectedRow);
        String input = JOptionPane.showInputDialog(this, "Enter new quantity for " + item.getProduct().getName() + " (current: " + item.getQuantity() + "):", "Modify Quantity", JOptionPane.QUESTION_MESSAGE);
        if (input != null && !input.trim().isEmpty()) {
            try {
                int newQuantity = Integer.parseInt(input.trim());
                if (newQuantity <= 0) {
                    JOptionPane.showMessageDialog(this, "Quantity must be greater than 0!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                // Check if enough stock is available
                Product product = item.getProduct();
                if (newQuantity > product.getStock()) {
                    JOptionPane.showMessageDialog(this, "Not enough stock available! (Max: " + product.getStock() + ")", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                item.setQuantity(newQuantity);
                calculateTotal();
                updateCartDisplay();
                JOptionPane.showMessageDialog(this, "Quantity updated to " + newQuantity + " for " + product.getName(), "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Please enter a valid number!", "Error", JOptionPane.ERROR_MESSAGE);
            }
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

