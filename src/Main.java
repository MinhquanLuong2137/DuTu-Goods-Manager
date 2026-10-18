import model.ExpiringProduct;
import model.GeneralProduct;
import model.Product;
import service.LedgerService;
import service.PersistenceService;
import service.ProductService;
import view.DuTuWebView;
import view.ProductView;

import java.time.LocalDate;

public class Main {
    public static void main(String[] args) {
        ProductService productService = new ProductService();
        LedgerService ledgerService = new LedgerService();

        // 1. Nạp dữ liệu mẫu khởi đầu nếu chưa có
        Product p1 = new ExpiringProduct(1, "Sữa tươi Vinamilk 180ml", 28000, 34000, 4, "Tủ mát A1", LocalDate.now().plusDays(5), 10)
                .withWholesaleInfo(395000, "Lẻ: Lốc 4 hộp | Thùng: 12 lốc", 85, LocalDate.now().minusDays(1));
        Product p2 = new GeneralProduct(2, "Nước rửa chén Sunlight 750g", 24000, 30000, 15, "Kệ Hóa Phẩm", "Gia dụng", 5)
                .withWholesaleInfo(330000, "Lẻ: Chai | Thùng: 12 chai", 42, LocalDate.now().minusDays(4));

        productService.addProduct(p1);
        productService.addProduct(p2);

        ledgerService.addDebt("Cô Lan (Nhà số 12 ngõ 3)", "0912.345.678", "1 thùng Bia Hà Nội, 2 lốc sữa tươi", 383000);

        // 2. Đăng ký Shutdown Hook: Tự động lưu dữ liệu ra file khi tắt ứng dụng
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("[*] Đang lưu trữ dữ liệu trước khi thoát...");
            PersistenceService.saveData(productService, ledgerService, null);
        }));

        // 3. Khởi chạy Giao diện Web DuTu (Cổng 8080)
        DuTuWebView webView = new DuTuWebView(productService, ledgerService, 8080);
        webView.start();

        // 4. Console View chạy song song
        ProductView consoleView = new ProductView(productService);
        consoleView.displayMenu();
    }
}