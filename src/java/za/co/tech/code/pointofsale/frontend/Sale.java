package za.co.tech.code.pointofsale.frontend;

import java.time.LocalDateTime;
import java.util.List;

public class Sale {
    private Long id;
    private LocalDateTime dateTime;
    private double total;
    private String transactionNumber;
    private String tillNumber;
    private String userId;
    private List<SaleItem> items;

    // Default constructor for Jackson
    public Sale() {}

    // Constructor for simplicity (match backend structure)
    public Sale(LocalDateTime dateTime, double total, List<SaleItem> items, String transactionNumber, String tillNumber, String userId) {
        this.dateTime = dateTime;
        this.total = total;
        this.items = items;
        this.transactionNumber = transactionNumber;
        this.tillNumber = tillNumber;
        this.userId = userId;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public LocalDateTime getDateTime() { return dateTime; }
    public void setDateTime(LocalDateTime dateTime) { this.dateTime = dateTime; }
    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }
    public String getTransactionNumber() { return transactionNumber; }
    public void setTransactionNumber(String transactionNumber) { this.transactionNumber = transactionNumber; }
    public String getTillNumber() { return tillNumber; }
    public void setTillNumber(String tillNumber) { this.tillNumber = tillNumber; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public List<SaleItem> getItems() { return items; }
    public void setItems(List<SaleItem> items) { this.items = items; }
}