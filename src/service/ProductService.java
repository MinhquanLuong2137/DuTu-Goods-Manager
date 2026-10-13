package service;

import model.Product;
import model.ExpiringProduct;
import java.util.ArrayList;
import java.util.List;

public class ProductService {
    /*=======Property=========*/
    // Kho lưu trữ products
    private List<Product> inventory;

    /*=========METHOD===========*/
    // Constructor
    public ProductService() {
        inventory = new ArrayList<>();
    }

    // Thêm Product mới (chặn trùng ID hoặc đối tượng null)
    public boolean addProduct(Product product) {
        if (product == null || findById(product.getID()) != null) {
            return false;
        }
        return inventory.add(product);
    }

    // Xoá Product theo ID
    public boolean removeProduct(int ID) {
        Product target = findById(ID);
        if (target != null) {
            return inventory.remove(target);
        }
        return false;
    }

    // Tìm kiếm theo tên Product (hỗ trợ gõ từ khoá/tên tắt, không phân biệt hoa thường)
    public List<Product> findByName(String name) {
        List<Product> matches = new ArrayList<>();
        if (name == null || name.trim().isEmpty()) {
            return matches;
        }
        String keyword = name.trim().toLowerCase();
        for (Product p : inventory) {
            if (p.getName().toLowerCase().contains(keyword)) {
                matches.add(p);
            }
        }
        return matches;
    }

    // Tìm kiếm chính xác theo Mã ID
    public Product findById(int id) {
        for (Product p : inventory) {
            if (p.getID() == id) {
                return p;
            }
        }
        return null;
    }

    // Lấy toàn bộ danh sách mặt hàng (trả về bản sao để bảo vệ danh sách gốc)
    public List<Product> findAll() {
        return new ArrayList<>(inventory);
    }

    // Cập nhật giá bán và giá nhập
    public boolean updatePrices(int id, double newImportPrice, double newSellPrice) {
        Product target = findById(id);
        if (target == null || newImportPrice < 0 || newSellPrice < 0) {
            return false;
        }
        target.setImportPrice(newImportPrice);
        target.setSellPrice(newSellPrice);
        return true;
    }

    // Lọc các Product chạm hoặc dưới ngưỡng tồn kho tối thiểu
    public List<Product> getLowStockProducts() {
        List<Product> lowStockItems = new ArrayList<>();
        for (Product p : inventory) {
            if (p.isLowStock()) {
                lowStockItems.add(p);
            }
        }
        return lowStockItems;
    }

    // Lọc ExpiringProduct sắp hết hạn trong vòng `days` ngày tới
    public List<ExpiringProduct> getExpiringSoonProducts(int days) {
        List<ExpiringProduct> expiringSoon = new ArrayList<>();
        for (Product p : inventory) {
            if (p instanceof ExpiringProduct ep) {
                long daysLeft = ep.getDaysUntilExpiry();
                if (daysLeft >= 0 && daysLeft <= days) {
                    expiringSoon.add(ep);
                }
            }
        }
        return expiringSoon;
    }
}