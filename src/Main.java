import model.ExpiringProduct;
import model.GeneralProduct;
import service.ProductService;
import view.ProductView;

import java.time.LocalDate;

public class Main {
    public static void main(String[] args) {
        // 1. Khoi tao Service
        ProductService productService = new ProductService();

        // 2. Nap san mot so du lieu mau de test ngay tren giao dien
        productService.addProduct(new ExpiringProduct(1, "Sua tuoi Vinamilk 180ml", 7000, 10000, 4, "Tu mat A1", LocalDate.now().plusDays(5), 10));
        productService.addProduct(new ExpiringProduct(2, "Sua chua TH True Milk", 6500, 8500, 25, "Tu mat A2", LocalDate.now().plusDays(25), 10));
        productService.addProduct(new GeneralProduct(3, "Bong den Rang Dong 20W", 32000, 45000, 2, "Ke Dien 1", "Do dien", 5));
        productService.addProduct(new GeneralProduct(4, "Nuoc rua chen Sunlight 750g", 24000, 30000, 12, "Ke Hoa My", "Gia dung", 5));

        // 3. Bat giao dien console
        ProductView productView = new ProductView(productService);
        productView.displayMenu();
    }
}