package view;

import model.ExpiringProduct;
import model.GeneralProduct;
import model.Product;
import service.ProductService;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

public class ProductView {
    /*========Property========*/
    private final ProductService productService;
    private final Scanner scanner;
    /*=========Method========*/
    //constructor
    public ProductView(ProductService productService) {
        this.productService = productService;
        this.scanner = new Scanner(System.in);
    }

    // Vòng lặp hiển thị Menu chính của kho hàng
    public void displayMenu() {
        while (true) {
            System.out.println("\n==========================================");
            System.out.println("       HỆ THỐNG QUẢN LÝ KHO HÀNG          ");
            System.out.println("==========================================");
            System.out.println("1. Xem danh sách tất cả sản phẩm");
            System.out.println("2. Tìm kiếm sản phẩm (theo mã hoặc tên)");
            System.out.println("3. Thêm sản phẩm mới");
            System.out.println("4. Cập nhật giá bán / giá nhập");
            System.out.println("5. Xem cảnh báo tồn kho thấp");
            System.out.println("6. Xem cảnh báo hàng sắp hoặc đã hết hạn");
            System.out.println("7. xoá sản phẩm");
            System.out.println("0. Thoát / Quay lại");
            System.out.print("-> Chọn chức năng [0-7]: ");

            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> showAllProducts();
                case "2" -> searchProduct();
                case "3" -> addProduct();
                case "4" -> updatePrice();
                case "5" -> showLowStock();
                case "6" -> showExpiringAlerts();
                case "7" -> deleteProduct();
                case "0" -> {
                    System.out.println("Tam biet!");
                    return;
                }
                default -> System.out.println("[!] Lua chon khong hop le, vui long nhap lai.");
            }
        }
    }

    // 1. Hien thi danh sach
    private void showAllProducts() {
        List<Product> list = productService.findAll();
        if (list.isEmpty()) {
            System.out.println("[*] Kho hang hien dang trong.");
            return;
        }
        System.out.println("\n--- DANH SACH SAN PHAM TRONG KHO ---");
        for (Product p : list) {
            System.out.println(p);
        }
    }

    // 2. Tim kiem nhanh
    private void searchProduct() {
        System.out.print("Nhap ten can tim (hoac ID): ");
        String query = scanner.nextLine().trim();

        // Kiem tra neu nguoi dung nhap so -> tim theo ID truoc
        try {
            int id = Integer.parseInt(query);
            Product p = productService.findById(id);
            if (p != null) {
                System.out.println("[Tim thay theo ID] " + p);
                return;
            }
        } catch (NumberFormatException ignored) {}

        // Neu khong tim ra ID, tim theo tu khoa ten
        List<Product> results = productService.findByName(query);
        if (results.isEmpty()) {
            System.out.println("[*] Khong tim thay san pham nao phu hop voi '" + query + "'.");
        } else {
            System.out.println("Tim thay " + results.size() + " ket qua:");
            for (Product p : results) {
                System.out.println(" + " + p);
            }
        }
    }

    // 3. Them san pham moi
    private void addProduct() {
        System.out.println("\n--- THEM SAN PHAM MOI ---");
        System.out.println("Chon loai: 1. Hang co HSD (Sua, banh...) | 2. Hang thong thuong (Do gia dung, gia vi...)");
        System.out.print("Lua chon: ");
        String type = scanner.nextLine().trim();

        try {
            System.out.print("Nhap Ma ID: ");
            int id = Integer.parseInt(scanner.nextLine().trim());
            if (productService.findById(id) != null) {
                System.out.println("[!] Loi: ID nay da ton tai trong kho!");
                return;
            }

            System.out.print("Nhap Ten san pham: ");
            String name = scanner.nextLine().trim();

            System.out.print("Nhap Gia nhap: ");
            double importPrice = Double.parseDouble(scanner.nextLine().trim());

            System.out.print("Nhap Gia ban: ");
            double sellPrice = Double.parseDouble(scanner.nextLine().trim());

            System.out.print("Nhap So luong ton: ");
            int quantity = Integer.parseInt(scanner.nextLine().trim());

            System.out.print("Nhap Vi tri luu kho (ke/tu): ");
            String location = scanner.nextLine().trim();

            System.out.print("Nhap Nguong ton kho toi thieu de canh bao (mac dinh 5): ");
            String minStockStr = scanner.nextLine().trim();
            int minStock = minStockStr.isEmpty() ? 5 : Integer.parseInt(minStockStr);

            if ("1".equals(type)) {
                System.out.print("Nhap Han su dung (dinh dang YYYY-MM-DD, vd: 2026-12-31): ");
                LocalDate expiryDate = LocalDate.parse(scanner.nextLine().trim());
                productService.addProduct(new ExpiringProduct(id, name, importPrice, sellPrice, quantity, location, expiryDate, minStock));
            } else {
                System.out.print("Nhap Nganh hang / Danh muc: ");
                String category = scanner.nextLine().trim();
                productService.addProduct(new GeneralProduct(id, name, importPrice, sellPrice, quantity, location, category, minStock));
            }
            System.out.println("[+] Them san pham thanh cong!");
        } catch (NumberFormatException e) {
            System.out.println("[!] Loi: Vui long nhap dung dinh dang so!");
        } catch (DateTimeParseException e) {
            System.out.println("[!] Loi: Dinh dang ngay khong dung (phai la YYYY-MM-DD)!");
        }
    }

    // 4. Cap nhat gia
    private void updatePrice() {
        System.out.print("Nhap ID san pham can cap nhat gia: ");
        try {
            int id = Integer.parseInt(scanner.nextLine().trim());
            Product p = productService.findById(id);
            if (p == null) {
                System.out.println("[!] Khong tim thay san pham co ID " + id);
                return;
            }
            System.out.println("San pham: " + p.getName());
            System.out.print("Gia nhap moi (Hien tai: " + p.getImportPrice() + "): ");
            double newImport = Double.parseDouble(scanner.nextLine().trim());
            System.out.print("Gia ban moi (Hien tai: " + p.getSellPrice() + "): ");
            double newSell = Double.parseDouble(scanner.nextLine().trim());

            if (productService.updatePrices(id, newImport, newSell)) {
                System.out.println("[+] Cap nhat gia thanh cong!");
            } else {
                System.out.println("[!] Cap nhat that bai (gia phai >= 0)!");
            }
        } catch (NumberFormatException e) {
            System.out.println("[!] Loi: Vui long nhap so hop le!");
        }
    }

    // 5. Xem ton kho thap
    private void showLowStock() {
        List<Product> lowStock = productService.getLowStockProducts();
        if (lowStock.isEmpty()) {
            System.out.println("[*] Khong co san pham nao duoi nguong ton kho.");
            return;
        }
        System.out.println("\n[!] DANH SACH CAN NHAP THEM HANG:");
        for (Product p : lowStock) {
            System.out.println(" - " + p.getName() + " | Ton: " + p.getQuantity() + " | Canh bao: " + p.getMinStock());
        }
    }

    // 6. Canh bao het han
    private void showExpiringAlerts() {
        System.out.print("Xem cac mat hang sap het han trong bao nhieu ngay toi? (vd: 7): ");
        try {
            int days = Integer.parseInt(scanner.nextLine().trim());
            List<ExpiringProduct> list = productService.getExpiringSoonProducts(days);
            if (list.isEmpty()) {
                System.out.println("[*] Khong co san pham nao sap het han trong " + days + " ngay toi.");
                return;
            }
            System.out.println("\n[!] DANH SACH HANG SAP HET DATE:");
            for (ExpiringProduct ep : list) {
                System.out.println(" - " + ep.getName() + " | HSD: " + ep.getExpiryDate() + " | Con " + ep.getDaysUntilExpiry() + " ngay");
            }
        } catch (NumberFormatException e) {
            System.out.println("[!] Loi: Vui long nhap so ngay hop le!");
        }
    }

    // 7. Xoa san pham
    private void deleteProduct() {
        System.out.print("Nhap ID san pham muon xoa: ");
        try {
            int id = Integer.parseInt(scanner.nextLine().trim());
            if (productService.removeProduct(id)) {
                System.out.println("[+] Da xoa san pham ID " + id);
            } else {
                System.out.println("[!] Khong tim thay san pham voi ID " + id);
            }
        } catch (NumberFormatException e) {
            System.out.println("[!] Loi: ID phai la so nguyen!");
        }
    }
}