import model.ExpiringProduct;
import model.GeneralProduct;
import model.Product;
import service.ProductService;

import java.time.LocalDate;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        ProductService productService = new ProductService();

        System.out.println("=================================================================");
        System.out.println("       KIEM THU PRODUCT SERVICE (KHO HANG) BANG TEST SINH BOI AI       ");
        System.out.println("=================================================================");

        // 1. KHOI TAO DU LIEU MAU
        System.out.println("\n[1] Them hang hoa vao kho:");
        // San pham co han su dung: Sua tuoi con 5 ngay, Sua chua con 25 ngay
        Product p1 = new ExpiringProduct(1, "Sua tuoi Vinamilk 180ml", 7000, 10000, 4, "Tu mat A1", LocalDate.now().plusDays(5), 10);
        Product p2 = new ExpiringProduct(2, "Sua chua TH True Milk", 6500, 8500, 25, "Tu mat A2", LocalDate.now().plusDays(25), 10);

        // San pham bach hoa chung
        Product p3 = new GeneralProduct(3, "Bong den Rang Dong 20W", 32000, 45000, 2, "Ke Dien 1", "Do dien", 5);
        Product p4 = new GeneralProduct(4, "Nuoc rua chen Sunlight 750g", 24000, 30000, 12, "Ke Hoa My", "Gia dung", 5);

        productService.addProduct(p1);
        productService.addProduct(p2);
        productService.addProduct(p3);
        productService.addProduct(p4);

        // Thu them trung ID de kiem tra chan loi
        boolean addDuplicate = productService.addProduct(new GeneralProduct(1, "Sua gia", 5000, 7000, 1, "Tu mat A1", "Khac", 5));
        System.out.println("-> Thu them san pham trung ID 1: " + (addDuplicate ? "That bai (Bi loi)" : "Thanh cong (Da chan trung ID)"));

        // 2. HIEN THI TOAN BO DANH SACH
        System.out.println("\n[2] Danh sach toan bo san pham trong kho:");
        for (Product p : productService.findAll()) {
            System.out.println("   + " + p);
        }

        // 3. TIM KIEM THEO TU KHOA (VD: 'sua')
        System.out.println("\n[3] Tim kiem san pham theo tu khoa 'sua':");
        List<Product> searchResults = productService.findByName("sua");
        if (searchResults.isEmpty()) {
            System.out.println("   Khong tim thay san pham nao.");
        } else {
            for (Product p : searchResults) {
                System.out.println("   -> Tim thay: " + p.getName() + " | Gia: " + String.format("%,.0f", p.getSellPrice()) + "d");
            }
        }

        // 4. KIEM TRA CANH BAO TON KHO THAP (quantity <= minStock)
        System.out.println("\n[4] Danh sach san pham sap het hang (Ton <= Toi thieu):");
        List<Product> lowStockList = productService.getLowStockProducts();
        for (Product p : lowStockList) {
            System.out.println("   [!] Canh bao nhap hang: " + p.getName()
                    + " (Ton: " + p.getQuantity() + " | Mức toi thieu: " + p.getMinStock() + ")");
        }

        // 5. KIEM TRA CANH BAO HAN SU DUNG (Trong vong 7 ngay toi)
        System.out.println("\n[5] Danh sach san pham sap het date trong 7 ngay toi:");
        List<ExpiringProduct> expiringSoonList = productService.getExpiringSoonProducts(7);
        for (ExpiringProduct ep : expiringSoonList) {
            System.out.println("   [!] Canh bao date: " + ep.getName()
                    + " (HSD: " + ep.getExpiryDate() + " | Con: " + ep.getDaysUntilExpiry() + " ngay)");
        }

        // 6. TEST CAP NHAT GIA THU CONG
        System.out.println("\n[6] Test cap nhat gia thu cong cho ID 3:");
        boolean updateOk = productService.updatePrices(3, 35000, 50000);
        Product updatedP3 = productService.findById(3);
        if (updateOk && updatedP3 != null) {
            System.out.println("   -> Cap nhat thanh cong: " + updatedP3.getName()
                    + " | Gia nhap moi: " + String.format("%,.0f", updatedP3.getImportPrice())
                    + "d | Gia ban moi: " + String.format("%,.0f", updatedP3.getSellPrice()) + "d");
        }

        // 7. TEST XOA SAN PHAM (removeProduct)
        System.out.println("\n[7] Test xoa san pham:");
        // Xoa ID 4 (Nuoc rua chen)
        boolean deleteSuccess = productService.removeProduct(4);
        System.out.println("   -> Xoa san pham ID 4: " + (deleteSuccess ? "Thanh cong" : "That bai"));

        // Thu xoa lai ID 4 lan nua (hoac ID khong ton tai)
        boolean deleteFail = productService.removeProduct(999);
        System.out.println("   -> Thu xoa ID 999 khong ton tai: " + (!deleteFail ? "Dung (Khong tim thay de xoa)" : "Loi"));
        System.out.println("   -> So luong mat hang con lai trong kho: " + productService.findAll().size());

        // 8. TEST HANG DA HET HAN (isExpired va ngay am)
        System.out.println("\n[8] Test hang da qua han su dung:");
        // Tao banh mi da het han cach day 2 ngay
        Product pExpired = new ExpiringProduct(5, "Banh mi sandwich", 10000, 15000, 3, "Ke banh", LocalDate.now().minusDays(2), 5);
        productService.addProduct(pExpired);

        if (pExpired instanceof ExpiringProduct ep) {
            System.out.println("   -> San pham: " + ep.getName());
            System.out.println("   -> So ngay con lai: " + ep.getDaysUntilExpiry() + " ngay");
            System.out.println("   -> Trang thai da het han (isExpired): " + ep.isExpired());
        }


        System.out.println("\n==================================================");
        System.out.println("               HOAN TAT KIEM THU!                ");
        System.out.println("==================================================");

    }
}