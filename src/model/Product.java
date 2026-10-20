package model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class Product {
    /*==============Properties*================*/
    private int ID;
    private String name;
    private double importPrice;
    private double sellPrice;
    private double wholesalePrice;     // Có thể để 0 nếu chỉ bán lẻ
    private String unitNote;
    private int quantity;
    private String location;
    private int minStock;

    // Danh sách các lô HSD (FIFO)
    private List<ExpiryBatch> expiryBatches = new ArrayList<>();

    private int totalSold;
    private LocalDate lastActiveDate;

    /*=============setters and getters===============*/

    public void setID(int ID) { if (ID > 0) this.ID = ID; }
    public void setName(String name) { if (name != null && !name.trim().isEmpty()) this.name = name.trim(); }
    public void setImportPrice(double importPrice) { if (importPrice >= 0) this.importPrice = importPrice; }
    public void setSellPrice(double sellPrice) { if (sellPrice >= 0) this.sellPrice = sellPrice; }
    public void setWholesalePrice(double wholesalePrice) { this.wholesalePrice = Math.max(0, wholesalePrice); }
    public void setUnitNote(String unitNote) { this.unitNote = (unitNote != null && !unitNote.trim().isEmpty()) ? unitNote.trim() : "Bán lẻ"; }

    public void setQuantity(int quantity) {
        this.quantity = Math.max(0, quantity);
    }

    public void setLocation(String location) { this.location = (location != null && !location.trim().isEmpty()) ? location.trim() : "Kệ chung"; }
    public void setMinStock(int minStock) { if (minStock >= 0) this.minStock = minStock; }
    public void setTotalSold(int totalSold) { if (totalSold >= 0) this.totalSold = totalSold; }

    public void addSoldQuantity(int sold) {
        if (sold > 0) {
            this.totalSold += sold;
            this.lastActiveDate = LocalDate.now();
            reduceBatchQuantity(sold); // Trừ hàng theo nguyên tắc FIFO (lô hết hạn gần nhất)
        }
    }

    public void setLastActiveDate(LocalDate lastActiveDate) { this.lastActiveDate = (lastActiveDate != null) ? lastActiveDate : LocalDate.now(); }

    public int getID() { return ID; }
    public String getName() { return name; }
    public double getImportPrice() { return importPrice; }
    public double getSellPrice() { return sellPrice; }
    public double getWholesalePrice() { return wholesalePrice; }
    public String getUnitNote() { return unitNote; }
    public int getQuantity() {
        int totalQ = 0;
        for (ExpiryBatch b : expiryBatches) totalQ += b.getQuantity();
        return expiryBatches.isEmpty() ? this.quantity : totalQ;
    }
    public String getLocation() { return location; }
    public int getMinStock() { return minStock; }
    public int getTotalSold() { return totalSold; }
    public LocalDate getLastActiveDate() { return lastActiveDate; }
    public List<ExpiryBatch> getExpiryBatches() { return expiryBatches; }

    /*==================Functional Methods====================*/

    public void addBatch(LocalDate expiryDate, int qty) {
        if (qty <= 0) return;
        if (expiryDate != null) {
            boolean found = false;
            for (ExpiryBatch b : expiryBatches) {
                if (b.getExpiryDate() != null && b.getExpiryDate().equals(expiryDate)) {
                    b.addQuantity(qty);
                    found = true;
                    break;
                }
            }
            if (!found) {
                expiryBatches.add(new ExpiryBatch(expiryDate, qty));
            }
            Collections.sort(expiryBatches);
        }
        this.quantity = getQuantity();
    }

    private void reduceBatchQuantity(int qtyToReduce) {
        for (ExpiryBatch b : expiryBatches) {
            if (qtyToReduce <= 0) break;
            int current = b.getQuantity();
            if (current <= qtyToReduce) {
                qtyToReduce -= current;
                b.setQuantity(0);
            } else {
                b.setQuantity(current - qtyToReduce);
                qtyToReduce = 0;
            }
        }
        expiryBatches.removeIf(b -> b.getQuantity() <= 0);
        this.quantity = getQuantity();
    }

    // Lấy HSD của lô gần nhất để cảnh báo
    public LocalDate getNearestExpiryDate() {
        if (expiryBatches.isEmpty()) return null;
        Collections.sort(expiryBatches);
        return expiryBatches.get(0).getExpiryDate();
    }

    public boolean isLowStock() { return getQuantity() <= this.minStock; }

    public long getDaysSinceLastActive() {
        if (this.lastActiveDate == null) return 0;
        return ChronoUnit.DAYS.between(this.lastActiveDate, LocalDate.now());
    }

    public boolean isSlowMoving(int daysThreshold) { return getDaysSinceLastActive() >= daysThreshold; }

    public Product(int ID, String name, double importPrice, double sellPrice, int quantity, String location) {
        setID(ID);
        setName(name);
        setImportPrice(importPrice);
        setSellPrice(sellPrice);
        setWholesalePrice(0);
        setUnitNote("Bán lẻ");
        setQuantity(quantity);
        setLocation(location);
        setMinStock(5);
        this.totalSold = 0;
        this.lastActiveDate = LocalDate.now();
    }


    //Constructor
    public Product(int ID, String name, double importPrice, double sellPrice, int quantity, String location, int minStock) {
        this(ID, name, importPrice, sellPrice, quantity, location);
        setMinStock(minStock);
    }

    public Product withWholesaleInfo(double wholesalePrice, String unitNote, int totalSold, LocalDate lastActiveDate) {
        setWholesalePrice(wholesalePrice);
        setUnitNote(unitNote);
        setTotalSold(totalSold);
        setLastActiveDate(lastActiveDate);
        return this;
    }
}