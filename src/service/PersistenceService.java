package service;

import model.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class PersistenceService {
    private static final String DEFAULT_FILE_NAME = "dutu_database.json";

    // Lấy đường dẫn file mặc định tại thư mục chứa chương trình
    public static String getDefaultFilePath() {
        return Paths.get("").toAbsolutePath().resolve(DEFAULT_FILE_NAME).toString();
    }

    // Lưu toàn bộ dữ liệu ra file JSON thủ công bằng Java thuần
    public static boolean saveData(ProductService productService, LedgerService ledgerService, String customPath) {
        String filePath = (customPath != null && !customPath.isBlank()) ? customPath : getDefaultFilePath();
        StringBuilder json = new StringBuilder();
        json.append("{\n");

        // 1. Lưu danh sách sản phẩm
        json.append("  \"products\": [\n");
        List<Product> products = productService.findAll();
        for (int i = 0; i < products.size(); i++) {
            Product p = products.get(i);
            json.append("    {\n");
            json.append("      \"id\": ").append(p.getID()).append(",\n");
            json.append("      \"name\": \"").append(escapeJson(p.getName())).append("\",\n");
            json.append("      \"importPrice\": ").append(p.getImportPrice()).append(",\n");
            json.append("      \"sellPrice\": ").append(p.getSellPrice()).append(",\n");
            json.append("      \"wholesalePrice\": ").append(p.getWholesalePrice()).append(",\n");
            json.append("      \"unitNote\": \"").append(escapeJson(p.getUnitNote())).append("\",\n");
            json.append("      \"quantity\": ").append(p.getQuantity()).append(",\n");
            json.append("      \"location\": \"").append(escapeJson(p.getLocation())).append("\",\n");
            json.append("      \"minStock\": ").append(p.getMinStock()).append(",\n");
            json.append("      \"totalSold\": ").append(p.getTotalSold()).append(",\n");

            if (p instanceof ExpiringProduct ep) {
                json.append("      \"type\": \"EXPIRING\",\n");
                json.append("      \"expiryDate\": \"").append(ep.getExpiryDate() != null ? ep.getExpiryDate().toString() : "").append("\"\n");
            } else if (p instanceof GeneralProduct gp) {
                json.append("      \"type\": \"GENERAL\",\n");
                json.append("      \"category\": \"").append(escapeJson(gp.getCategory())).append("\"\n");
            } else {
                json.append("      \"type\": \"GENERAL\"\n");
            }
            json.append("    }").append(i < products.size() - 1 ? "," : "").append("\n");
        }
        json.append("  ],\n");

        // 2. Lưu Sổ Nợ
        json.append("  \"debts\": [\n");
        List<DebtRecord> debts = ledgerService.getAllDebts();
        for (int i = 0; i < debts.size(); i++) {
            DebtRecord d = debts.get(i);
            json.append("    {\n");
            json.append("      \"id\": ").append(d.getId()).append(",\n");
            json.append("      \"customerName\": \"").append(escapeJson(d.getCustomerName())).append("\",\n");
            json.append("      \"phoneOrAddress\": \"").append(escapeJson(d.getPhoneOrAddress())).append("\",\n");
            json.append("      \"itemsNote\": \"").append(escapeJson(d.getItemsNote())).append("\",\n");
            json.append("      \"amount\": ").append(d.getAmount()).append(",\n");
            json.append("      \"paid\": ").append(d.isPaid()).append("\n");
            json.append("    }").append(i < debts.size() - 1 ? "," : "").append("\n");
        }
        json.append("  ]\n");

        json.append("}\n");

        try {
            Files.writeString(Path.of(filePath), json.toString(), StandardCharsets.UTF_8);
            System.out.println("[+] Đã tự động lưu dữ liệu vào: " + filePath);
            return true;
        } catch (IOException e) {
            System.out.println("[!] Lỗi khi lưu file dữ liệu: " + e.getMessage());
            return false;
        }
    }

    private static String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", " ");
    }
}