package model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class ExpiringProduct extends Product {
    //Property
    private LocalDate  expiryDate;

    //Getter and setter
    public LocalDate getExpiryDate() {
        return expiryDate;
    }
    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    //Print
    @Override
    public String toString() {
        return super.toString() + String.format(" | HSD: %s", expiryDate != null ? expiryDate.toString() : "N/A");
    }

    //Method
    public boolean isExpired() {
        return this.expiryDate != null && getDaysUntilExpiry() < 0;
    }

    public long getDaysUntilExpiry() {
        //trả về số ngày hạn sử dụng còn lại
        if (this.expiryDate == null) {
            return 0;
        }
        return ChronoUnit.DAYS.between(LocalDate.now(), this.expiryDate);
    }


    //Constructor
// Constructor
    public ExpiringProduct(int ID, String name, double importPrice, double sellPrice, int quantity, LocalDate expiryDate) {
        super(ID, name, importPrice, sellPrice, quantity);
        setExpiryDate(expiryDate);
    }

}
