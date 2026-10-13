package model;

public abstract class Product {
    /*========propery==========*/
    private int ID;
    private String name;
    private double importPrice;
    private double sellPrice;
    private int quantity;
    private String location;

    /*========Method=========*/

    //setter

    public void setID(int ID) {
        if (ID > 0) {
            this.ID = ID;
        }
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setImportPrice(double importPrice) {
        if (importPrice > 0) {
            this.importPrice = importPrice;
        }
    }

    public void setSellPrice(double sellPrice) {
        if (sellPrice > 0) {
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
    //print properties
    @Override
    public String toString() {
        return String.format("ID: %d | Ten: %-20s | Gia nhap: %,.0f | Gia ban: %,.0f | Ton: %d",
                ID, name, importPrice, sellPrice, quantity);
    }

    //constructor
    public Product(int ID, String name, double importPrice, double sellPrice, int quantity,  String location) {
        setID(ID);
        setName(name);
        setImportPrice(importPrice);
        setSellPrice(sellPrice);
        setQuantity(quantity);
        setLocation(location);
    }
}
