package model;

public abstract class Product {
    /*========propery==========*/
    private int ID;
    private String name;
    private double importPrice;
    private double sellPrice;
    private int quantity;
    private String location;
    private  int minStock;
    /*========Method=========*/

    //setter

    public void setID(int ID) {
        if (ID > 0) {
            this.ID = ID;
        }
    }

    public void setName(String name) {
        if (name != null && !name.trim().isEmpty()) {
            this.name = name.trim();
        }
    }

    public void setImportPrice(double importPrice) {
        if (importPrice >= 0) {
            this.importPrice = importPrice;
        }
    }

    public void setSellPrice(double sellPrice) {
        if (sellPrice >= 0) {
            this.sellPrice = sellPrice;
        }
    }

    public void setQuantity(int quantity) {
        if (quantity >= 0) {
            this.quantity = quantity;
        }
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setMinStock(int minStock) {
        if (minStock >= 0) {
            this.minStock = minStock;
        }
    }

    //getter
    public int getID() {
        return ID;
    }

    public String getName() {
        return name;
    }

    public double getImportPrice() {
        return importPrice;
    }

    public double getSellPrice() {
        return sellPrice;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getLocation() {
        return location;
    }

    public int getMinStock() {
        return minStock;
    }

    //print properties
    @Override
    public String toString() {
        return String.format("ID: %d | Ten: %-20s | Gia nhap: %,.0f | Gia ban: %,.0f | Ton: %d | Vi tri: %s | Canh bao: %d",
                ID, name, importPrice, sellPrice, quantity, location != null ? location : "Chua ro", minStock);
    }
    //extension
    public boolean isLowStock() {
        //Kiểm tra mức tồn kho đã chạm ngưỡng tối thiểu ?
        return this.quantity <= this.minStock;
    }


    //constructor
    public Product(int ID, String name, double importPrice, double sellPrice, int quantity,  String location) {
        setID(ID);
        setName(name);
        setImportPrice(importPrice);
        setSellPrice(sellPrice);
        setQuantity(quantity);
        setLocation(location);
        setMinStock(5);
    }
    public Product(int ID, String name, double importPrice, double sellPrice, int quantity,  String location, int minStock) {
        this(ID, name, importPrice, sellPrice, quantity, location);
        setMinStock(minStock);
    }


}
