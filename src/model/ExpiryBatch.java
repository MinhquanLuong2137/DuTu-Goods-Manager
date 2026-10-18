package model;

import java.time.LocalDate;

public class ExpiryBatch implements Comparable<ExpiryBatch> {
    private LocalDate expiryDate;
    private int quantity;

    public ExpiryBatch(LocalDate expiryDate, int quantity) {
        this.expiryDate = expiryDate;
        this.quantity = Math.max(0, quantity);
    }

    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = Math.max(0, quantity); }

    public void addQuantity(int qty) { if (qty > 0) this.quantity += qty; }
    public void reduceQuantity(int qty) { this.quantity = Math.max(0, this.quantity - qty); }

    @Override
    public int compareTo(ExpiryBatch o) {
        if (this.expiryDate == null && o.expiryDate == null) return 0;
        if (this.expiryDate == null) return 1;
        if (o.expiryDate == null) return -1;
        return this.expiryDate.compareTo(o.expiryDate); // Sắp xếp theo HSD gần nhất lên đầu
    }
}