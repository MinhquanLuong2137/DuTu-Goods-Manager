package model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class StockLog {
    private int id;
    private int productId;
    private String productName;
    private String type;           // "NHAP_HANG" hoặc "KIEM_KE"
    private int oldQuantity;
    private int newQuantity;
    private int changeOrSoldQty;   // Lượng nhập thêm hoặc lượng đã bán suy ra
    private double unitPrice;      // Giá nhập (nếu là nhập hàng) hoặc giá bán (nếu là kiểm kê)
    private String note;
    private LocalDateTime timestamp;

    public StockLog(int id, int productId, String productName, String type,
                    int oldQuantity, int newQuantity, int changeOrSoldQty, double unitPrice, String note) {
        this.id = id;
        this.productId = productId;
        this.productName = productName;
        this.type = type;
        this.oldQuantity = oldQuantity;
        this.newQuantity = newQuantity;
        this.changeOrSoldQty = changeOrSoldQty;
        this.unitPrice = unitPrice;
        this.note = note;
        this.timestamp = LocalDateTime.now();
    }

    public int getId() { return id; }
    public int getProductId() { return productId; }
    public String getProductName() { return productName; }
    public String getType() { return type; }
    public int getOldQuantity() { return oldQuantity; }
    public int getNewQuantity() { return newQuantity; }
    public int getChangeOrSoldQty() { return changeOrSoldQty; }
    public double getUnitPrice() { return unitPrice; }
    public String getNote() { return note; }
    public LocalDateTime getTimestamp() { return timestamp; }

    public String getFormattedTime() {
        return timestamp.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }
}