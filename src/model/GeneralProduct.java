package model;

public class GeneralProduct extends Product {
    /*======Property======*/
    private String category;
    /*=========Setter and getter==========*/
    public void setCategory(String category) {
        this.category = category;
    }
    public String getCategory() {
        return category;
    }

    /*===========method==========*/
    //Print
    @Override
    public String toString() {
        return super.toString() + String.format(" | Danh muc: %s", category != null ? category : "Chua phan loai");
    }
    //Constructor
    public GeneralProduct(int ID, String name, double importPrice, double sellPrice, int quantity,  String location, String category) {
        super(ID, name, importPrice, sellPrice, quantity, location);
        setCategory(category);
    }
    public GeneralProduct(int ID, String name, double importPrice, double sellPrice, int quantity,  String location, String category, int minStock) {
        super(ID, name, importPrice, sellPrice, quantity, location, minStock);
        setCategory(category);
    }

}
