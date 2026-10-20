package model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DebtRecord {
    private int id;
    private String customerName;
    private String phoneOrAddress;
    private String itemsNote;      // Ghi chú mặt hàng tự do
    private double amount;         // Số tiền nợ
    private LocalDateTime createdAt;
    private boolean paid;

    public DebtRecord(int id, String customerName, String phoneOrAddress, String itemsNote, double amount) {
        this.id = id;
        this.customerName = customerName;
        this.phoneOrAddress = phoneOrAddress;
        this.itemsNote = itemsNote;
        this.amount = Math.max(0, amount);
        this.createdAt = LocalDateTime.now();
        this.paid = false;
    }

    // Getters
    public int getId() { return id; }
    public String getCustomerName() { return customerName; }
    public String getPhoneOrAddress() { return phoneOrAddress; }
    public String getItemsNote() { return itemsNote; }
    public double getAmount() { return amount; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public boolean isPaid() { return paid; }

    // Setters (Đã bổ sung đầy đủ để sửa lỗi)
    public void setId(int id) { this.id = id; }
    public void setCreatedAt(LocalDateTime createdAt) {
        if (createdAt != null) this.createdAt = createdAt;
    }
    public void setCustomerName(String customerName) {
        if (customerName != null && !customerName.isBlank()) {
            this.customerName = customerName.trim();
        }
    }
    public void setPhoneOrAddress(String phoneOrAddress) {
        this.phoneOrAddress = phoneOrAddress != null ? phoneOrAddress.trim() : "";
    }
    public void setItemsNote(String itemsNote) { this.itemsNote = itemsNote; }
    public void setAmount(double amount) { if (amount >= 0) this.amount = amount; }
    public void setPaid(boolean paid) { this.paid = paid; }

    public String getFormattedDate() {
        return createdAt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }
}