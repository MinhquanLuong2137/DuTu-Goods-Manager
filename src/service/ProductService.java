package service;

import model.ExpiringProduct;
import model.Product;

import java.text.Normalizer;
import java.time.LocalDate;
import java.util.*;
import java.util.regex.Pattern;

public class ProductService {
    private final List<Product> inventory;
    private final Set<String> locations;

    public ProductService() {
        inventory = new ArrayList<>();
        locations = new LinkedHashSet<>();
    }

    /* Thuật toán bỏ dấu tiếng Việt để tìm kiếm không dấu (VD: "nuoc" -> "nuoc", "nước" -> "nuoc") */
    public static String removeDiacritics(String str) {
        if (str == null) return "";
        String nfd = Normalizer.normalize(str, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(nfd).replaceAll("")
                .replace('đ', 'd').replace('Đ', 'D')
                .toLowerCase().trim();
    }

    public boolean addProduct(Product product) {
        if (product == null || findById(product.getID()) != null) {
            return false;
        }
        locations.add(product.getLocation());
        return inventory.add(product);
    }

    public int getNextId() {
        int max = 0;
        for (Product p : inventory) {
            if (p.getID() > max) max = p.getID();
        }
        return max + 1;
    }

    public boolean removeProduct(int ID) {
        Product target = findById(ID);
        if (target != null) {
            return inventory.remove(target);
        }
        return false;
    }

    /* Tìm kiếm đa năng: Khớp cả tiếng Việt có dấu lẫn không dấu */
    public List<Product> findByName(String name) {
        List<Product> matches = new ArrayList<>();
        if (name == null || name.trim().isEmpty()) {
            return matches;
        }
        String keywordRaw = name.trim().toLowerCase();
        String keywordNoAccent = removeDiacritics(keywordRaw);

        for (Product p : inventory) {
            String pNameNoAccent = removeDiacritics(p.getName());
            String pLocNoAccent = removeDiacritics(p.getLocation());
            String pUnitNoAccent = removeDiacritics(p.getUnitNote());

            boolean match = pNameNoAccent.contains(keywordNoAccent)
                    || pLocNoAccent.contains(keywordNoAccent)
                    || pUnitNoAccent.contains(keywordNoAccent)
                    || p.getName().toLowerCase().contains(keywordRaw);

            if (match) {
                matches.add(p);
            }
        }
        return matches;
    }

    public Product findById(int id) {
        for (Product p : inventory) {
            if (p.getID() == id) {
                return p;
            }
        }
        return null;
    }

    public List<Product> findAll() {
        return new ArrayList<>(inventory);
    }

    public boolean updatePrices(int id, double newImportPrice, double newSellPrice) {
        Product target = findById(id);
        if (target == null || newImportPrice < 0 || newSellPrice < 0) {
            return false;
        }
        target.setImportPrice(newImportPrice);
        target.setSellPrice(newSellPrice);
        return true;
    }

    public boolean updateFullProduct(int id, String name, double importPrice, double sellPrice,
                                     double wholesalePrice, String unitNote, int quantity,
                                     String location, int minStock, LocalDate expiryDate) {
        Product target = findById(id);
        if (target == null) return false;

        target.setName(name);
        target.setImportPrice(importPrice);
        target.setSellPrice(sellPrice);
        target.setWholesalePrice(wholesalePrice);
        target.setUnitNote(unitNote);
        target.setQuantity(quantity);
        target.setLocation(location);
        if (location != null && !location.isBlank()) locations.add(location.trim());
        target.setMinStock(minStock);

        if (target instanceof ExpiringProduct ep && expiryDate != null) {
            ep.setExpiryDate(expiryDate);
        }
        return true;
    }

    public boolean updateFullPricing(int id, double newImportPrice, double newSellPrice, double newWholesalePrice, String unitNote) {
        Product target = findById(id);
        if (target == null || newImportPrice < 0 || newSellPrice < 0 || newWholesalePrice < 0) {
            return false;
        }
        target.setImportPrice(newImportPrice);
        target.setSellPrice(newSellPrice);
        target.setWholesalePrice(newWholesalePrice);
        if (unitNote != null && !unitNote.isBlank()) {
            target.setUnitNote(unitNote);
        }
        return true;
    }

    public List<Product> getLowStockProducts() {
        List<Product> lowStockItems = new ArrayList<>();
        for (Product p : inventory) {
            if (p.isLowStock()) {
                lowStockItems.add(p);
            }
        }
        return lowStockItems;
    }

    public List<ExpiringProduct> getExpiringSoonProducts(int days) {
        List<ExpiringProduct> expiringSoon = new ArrayList<>();
        for (Product p : inventory) {
            if (p instanceof ExpiringProduct ep) {
                long daysLeft = ep.getDaysUntilExpiry();
                if (daysLeft <= days) {
                    expiringSoon.add(ep);
                }
            }
        }
        return expiringSoon;
    }

    public List<String> getAllLocations() {
        Set<String> locations = new LinkedHashSet<>();
        locations.addAll(this.locations);
        for (Product p : inventory) {
            if (p.getLocation() != null && !p.getLocation().isBlank()) {
                locations.add(p.getLocation());
            }
        }
        return new ArrayList<>(locations);
    }

    /* Đổi tên kệ hàng loạt (Ví dụ đổi "Ke A1" -> "Ke Banh Keo") */
    public int renameLocation(String oldName, String newName) {
        if (oldName == null || newName == null || newName.isBlank()) return 0;
        String previousName = findLocation(oldName);
        if (previousName == null) return 0;
        String trimmedNewName = newName.trim();
        locations.remove(previousName);
        locations.add(trimmedNewName);
        int count = 0;
        for (Product p : inventory) {
            if (oldName.equalsIgnoreCase(p.getLocation())) {
                p.setLocation(trimmedNewName);
                count++;
            }
        }
        return count;
    }

    public boolean addLocation(String name) {
        if (name == null || name.isBlank() || findLocation(name) != null) return false;
        locations.add(name.trim());
        return true;
    }

    public boolean removeLocation(String name) {
        String existingName = findLocation(name);
        if (existingName == null || !findByLocation(existingName).isEmpty()) return false;
        return locations.remove(existingName);
    }

    private String findLocation(String name) {
        if (name == null) return null;
        for (String location : locations) {
            if (location.equalsIgnoreCase(name.trim())) return location;
        }
        for (Product product : inventory) {
            if (product.getLocation().equalsIgnoreCase(name.trim())) return product.getLocation();
        }
        return null;
    }

    public List<Product> findByLocation(String location) {
        if (location == null || location.isBlank() || "ALL".equalsIgnoreCase(location)) {
            return findAll();
        }
        List<Product> result = new ArrayList<>();
        for (Product p : inventory) {
            if (location.equalsIgnoreCase(p.getLocation())) {
                result.add(p);
            }
        }
        return result;
    }

    public List<Product> getSlowMovingProducts(int daysThreshold) {
        List<Product> slowList = new ArrayList<>();
        for (Product p : inventory) {
            if (p.isSlowMoving(daysThreshold)) {
                slowList.add(p);
            }
        }
        return slowList;
    }

    public List<Product> getTopSellingProducts() {
        List<Product> sorted = new ArrayList<>(inventory);
        sorted.sort(Comparator.comparingInt(Product::getTotalSold).reversed());
        return sorted;
    }

    public boolean restockProduct(int id, int addedQty, double newImportPrice) {
        Product p = findById(id);
        if (p == null || addedQty <= 0) return false;
        p.setQuantity(p.getQuantity() + addedQty);
        if (newImportPrice > 0) {
            p.setImportPrice(newImportPrice);
        }
        return true;
    }

    public int auditStockCount(int id, int actualCount) {
        Product p = findById(id);
        if (p == null || actualCount < 0) return -1;
        int oldQty = p.getQuantity();
        int soldQty = 0;
        if (actualCount < oldQty) {
            soldQty = oldQty - actualCount;
            p.addSoldQuantity(soldQty);
        } else if (actualCount > oldQty) {
            p.setLastActiveDate(LocalDate.now());
        }
        p.setQuantity(actualCount);
        return soldQty;
    }
}