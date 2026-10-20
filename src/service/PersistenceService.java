package service;

import model.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PersistenceService {
    private static final String DEFAULT_FILE_NAME = "dutu_database.json";

    public static String getDefaultFilePath() {
        return Paths.get("").toAbsolutePath().resolve(DEFAULT_FILE_NAME).toString();
    }

    public static boolean saveData(ProductService productService, LedgerService ledgerService, String customPath) {
        String filePath = (customPath != null && !customPath.isBlank()) ? customPath : getDefaultFilePath();
        StringBuilder json = new StringBuilder("{\n  \"initialized\": true,\n");

        json.append("  \"locations\": ");
        appendStringArray(json, productService.getAllLocations());
        json.append(",\n  \"products\": [");
        List<Product> products = productService.findAll();
        for (int i = 0; i < products.size(); i++) {
            Product p = products.get(i);
            json.append(i == 0 ? "\n" : ",\n").append("    {\n")
                    .append("      \"id\": ").append(p.getID()).append(",\n")
                    .append("      \"name\": ").append(jsonString(p.getName())).append(",\n")
                    .append("      \"importPrice\": ").append(p.getImportPrice()).append(",\n")
                    .append("      \"sellPrice\": ").append(p.getSellPrice()).append(",\n")
                    .append("      \"wholesalePrice\": ").append(p.getWholesalePrice()).append(",\n")
                    .append("      \"unitNote\": ").append(jsonString(p.getUnitNote())).append(",\n")
                    .append("      \"quantity\": ").append(p.getQuantity()).append(",\n")
                    .append("      \"location\": ").append(jsonString(p.getLocation())).append(",\n")
                    .append("      \"minStock\": ").append(p.getMinStock()).append(",\n")
                    .append("      \"totalSold\": ").append(p.getTotalSold()).append(",\n")
                    .append("      \"lastActiveDate\": ").append(jsonString(p.getLastActiveDate() == null ? "" : p.getLastActiveDate().toString())).append(",\n");
            if (p instanceof ExpiringProduct ep) {
                json.append("      \"type\": \"EXPIRING\",\n")
                        .append("      \"expiryDate\": ").append(jsonString(ep.getExpiryDate() == null ? "" : ep.getExpiryDate().toString())).append(",\n");
            } else if (p instanceof GeneralProduct gp) {
                json.append("      \"type\": \"GENERAL\",\n")
                        .append("      \"category\": ").append(jsonString(gp.getCategory())).append(",\n");
            } else {
                json.append("      \"type\": \"GENERAL\",\n");
            }
            json.append("      \"expiryBatches\": [");
            List<ExpiryBatch> batches = p.getExpiryBatches();
            for (int j = 0; j < batches.size(); j++) {
                ExpiryBatch batch = batches.get(j);
                json.append(j == 0 ? "\n" : ",\n")
                        .append("        {\"expiryDate\": ")
                        .append(jsonString(batch.getExpiryDate() == null ? "" : batch.getExpiryDate().toString()))
                        .append(", \"quantity\": ").append(batch.getQuantity()).append("}");
            }
            if (!batches.isEmpty()) json.append('\n').append("      ");
            json.append("]\n    }");
        }
        if (!products.isEmpty()) json.append('\n').append("  ");
        json.append("],\n  \"debts\": ");
        appendDebtArray(json, ledgerService.getAllDebts());
        json.append(",\n  \"deletedDebts\": ");
        appendDebtArray(json, ledgerService.getDeletedDebtTrash());
        json.append(",\n  \"stockLogs\": [");
        List<StockLog> logs = ledgerService.getAllStockLogs();
        for (int i = 0; i < logs.size(); i++) {
            StockLog log = logs.get(i);
            json.append(i == 0 ? "\n" : ",\n")
                    .append("    {\"id\": ").append(log.getId())
                    .append(", \"productId\": ").append(log.getProductId())
                    .append(", \"productName\": ").append(jsonString(log.getProductName()))
                    .append(", \"type\": ").append(jsonString(log.getType()))
                    .append(", \"oldQuantity\": ").append(log.getOldQuantity())
                    .append(", \"newQuantity\": ").append(log.getNewQuantity())
                    .append(", \"changeOrSoldQty\": ").append(log.getChangeOrSoldQty())
                    .append(", \"unitPrice\": ").append(log.getUnitPrice())
                    .append(", \"note\": ").append(jsonString(log.getNote()))
                    .append(", \"timestamp\": ").append(jsonString(log.getTimestamp().toString())).append("}");
        }
        if (!logs.isEmpty()) json.append('\n').append("  ");
        json.append("],\n  \"dailyClosings\": [");
        List<DailyClosing> closings = ledgerService.getAllDailyClosings();
        for (int i = 0; i < closings.size(); i++) {
            DailyClosing closing = closings.get(i);
            json.append(i == 0 ? "\n" : ",\n")
                    .append("    {\"id\": ").append(closing.getId())
                    .append(", \"date\": ").append(jsonString(closing.getDate().toString()))
                    .append(", \"cashAmount\": ").append(closing.getCashAmount())
                    .append(", \"transferAmount\": ").append(closing.getTransferAmount())
                    .append(", \"importExpense\": ").append(closing.getImportExpense())
                    .append(", \"note\": ").append(jsonString(closing.getNote())).append("}");
        }
        if (!closings.isEmpty()) json.append('\n').append("  ");
        json.append("]\n}\n");

        try {
            Path target = Path.of(filePath).toAbsolutePath();
            Path temporary = Files.createTempFile(target.getParent(), target.getFileName().toString(), ".tmp");
            try {
                Files.writeString(temporary, json.toString(), StandardCharsets.UTF_8);
                try {
                    Files.move(temporary, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException atomicMoveError) {
                    Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING);
                }
            } finally {
                Files.deleteIfExists(temporary);
            }
            System.out.println("[+] Đã tự động lưu dữ liệu vào: " + filePath);
            return true;
        } catch (IOException e) {
            System.out.println("[!] Lỗi khi lưu file dữ liệu: " + e.getMessage());
            return false;
        }
    }

    public static boolean loadData(ProductService productService, LedgerService ledgerService, String customPath) throws IOException {
        String filePath = (customPath != null && !customPath.isBlank()) ? customPath : getDefaultFilePath();
        Path path = Path.of(filePath);
        if (!Files.exists(path) || Files.size(path) == 0) return false;

        Object parsed = new JsonParser(Files.readString(path, StandardCharsets.UTF_8)).parse();
        Map<String, Object> root = asObject(parsed, "database");
        loadLocations(root, productService);
        loadProducts(root, productService);
        loadDebts(root, ledgerService, "debts", false);
        loadDebts(root, ledgerService, "deletedDebts", true);
        loadStockLogs(root, ledgerService);
        loadDailyClosings(root, ledgerService);

        return Boolean.TRUE.equals(root.get("initialized"))
                || hasItems(root, "locations") || hasItems(root, "products")
                || hasItems(root, "debts") || hasItems(root, "deletedDebts")
                || hasItems(root, "stockLogs") || hasItems(root, "dailyClosings");
    }

    private static void loadLocations(Map<String, Object> root, ProductService productService) {
        for (Object value : asArray(root.get("locations"), "locations")) {
            if (!(value instanceof String location) || !productService.addLocation(location)) {
                throw new IllegalArgumentException("Invalid or duplicate shelf location in database.");
            }
        }
    }

    private static void loadProducts(Map<String, Object> root, ProductService productService) {
        for (Object value : asArray(root.get("products"), "products")) {
            Map<String, Object> item = asObject(value, "product");
            int id = integer(item, "id", 0);
            String name = string(item, "name", "");
            String type = string(item, "type", "GENERAL");
            String location = string(item, "location", "Kệ chung");
            int quantity = integer(item, "quantity", 0);
            LocalDate expiryDate = date(string(item, "expiryDate", ""));
            Product product;
            if ("EXPIRING".equalsIgnoreCase(type)) {
                product = new ExpiringProduct(id, name, decimal(item, "importPrice", 0),
                        decimal(item, "sellPrice", 0), quantity, location, expiryDate,
                        integer(item, "minStock", 5));
            } else {
                product = new GeneralProduct(id, name, decimal(item, "importPrice", 0),
                        decimal(item, "sellPrice", 0), quantity, location,
                        string(item, "category", "Tạp hóa"), integer(item, "minStock", 5));
            }
            product.setWholesalePrice(decimal(item, "wholesalePrice", 0));
            product.setUnitNote(string(item, "unitNote", "Bán lẻ"));
            product.setTotalSold(integer(item, "totalSold", 0));
            String lastActive = string(item, "lastActiveDate", "");
            if (!lastActive.isBlank()) product.setLastActiveDate(LocalDate.parse(lastActive));
            for (Object batchValue : asArray(item.get("expiryBatches"), "expiryBatches")) {
                Map<String, Object> batch = asObject(batchValue, "expiry batch");
                LocalDate batchDate = date(string(batch, "expiryDate", ""));
                int batchQuantity = integer(batch, "quantity", 0);
                if (batchDate != null && batchQuantity > 0) product.addBatch(batchDate, batchQuantity);
            }
            if (!productService.addProduct(product)) {
                throw new IllegalArgumentException("Duplicate or invalid product ID " + id + " in database.");
            }
        }
    }

    private static void loadDebts(Map<String, Object> root, LedgerService ledgerService, String key, boolean deleted) {
        for (Object value : asArray(root.get(key), key)) {
            Map<String, Object> item = asObject(value, "debt");
            DebtRecord debt = new DebtRecord(integer(item, "id", 0),
                    string(item, "customerName", ""), string(item, "phoneOrAddress", ""),
                    string(item, "itemsNote", ""), decimal(item, "amount", 0));
            String createdAt = string(item, "createdAt", "");
            if (!createdAt.isBlank()) debt.setCreatedAt(LocalDateTime.parse(createdAt));
            debt.setPaid(Boolean.TRUE.equals(item.get("paid")));
            if (deleted) ledgerService.loadDeletedDebt(debt);
            else ledgerService.loadDebt(debt);
        }
    }

    private static void loadStockLogs(Map<String, Object> root, LedgerService ledgerService) {
        for (Object value : asArray(root.get("stockLogs"), "stockLogs")) {
            Map<String, Object> item = asObject(value, "stock log");
            StockLog log = new StockLog(integer(item, "id", 0), integer(item, "productId", 0),
                    string(item, "productName", ""), string(item, "type", ""),
                    integer(item, "oldQuantity", 0), integer(item, "newQuantity", 0),
                    integer(item, "changeOrSoldQty", 0), decimal(item, "unitPrice", 0),
                    string(item, "note", ""));
            String timestamp = string(item, "timestamp", "");
            if (!timestamp.isBlank()) log.setTimestamp(LocalDateTime.parse(timestamp));
            ledgerService.loadStockLog(log);
        }
    }

    private static void loadDailyClosings(Map<String, Object> root, LedgerService ledgerService) {
        for (Object value : asArray(root.get("dailyClosings"), "dailyClosings")) {
            Map<String, Object> item = asObject(value, "daily closing");
            DailyClosing closing = new DailyClosing(integer(item, "id", 0),
                    date(string(item, "date", "")), decimal(item, "cashAmount", 0),
                    decimal(item, "transferAmount", 0), decimal(item, "importExpense", 0),
                    string(item, "note", ""));
            ledgerService.loadDailyClosing(closing);
        }
    }

    private static void appendDebtArray(StringBuilder json, List<DebtRecord> debts) {
        json.append('[');
        for (int i = 0; i < debts.size(); i++) {
            DebtRecord debt = debts.get(i);
            json.append(i == 0 ? "\n" : ",\n")
                    .append("    {\"id\": ").append(debt.getId())
                    .append(", \"customerName\": ").append(jsonString(debt.getCustomerName()))
                    .append(", \"phoneOrAddress\": ").append(jsonString(debt.getPhoneOrAddress()))
                    .append(", \"itemsNote\": ").append(jsonString(debt.getItemsNote()))
                    .append(", \"amount\": ").append(debt.getAmount())
                    .append(", \"paid\": ").append(debt.isPaid())
                    .append(", \"createdAt\": ").append(jsonString(debt.getCreatedAt().toString())).append("}");
        }
        if (!debts.isEmpty()) json.append('\n').append("  ");
        json.append(']');
    }

    private static void appendStringArray(StringBuilder json, List<String> values) {
        json.append('[');
        for (int i = 0; i < values.size(); i++) {
            if (i > 0) json.append(", ");
            json.append(jsonString(values.get(i)));
        }
        json.append(']');
    }

    private static String jsonString(String value) {
        if (value == null) return "\"\"";
        StringBuilder result = new StringBuilder("\"");
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '"' -> result.append("\\\"");
                case '\\' -> result.append("\\\\");
                case '\b' -> result.append("\\b");
                case '\f' -> result.append("\\f");
                case '\n' -> result.append("\\n");
                case '\r' -> result.append("\\r");
                case '\t' -> result.append("\\t");
                default -> {
                    if (c < 0x20) result.append(String.format("\\u%04x", (int) c));
                    else result.append(c);
                }
            }
        }
        return result.append('"').toString();
    }

    private static boolean hasItems(Map<String, Object> root, String key) {
        return root.get(key) instanceof List<?> values && !values.isEmpty();
    }

    private static LocalDate date(String value) {
        return value == null || value.isBlank() ? null : LocalDate.parse(value);
    }

    private static int integer(Map<String, Object> object, String key, int fallback) {
        Object value = object.get(key);
        return value instanceof Number number ? number.intValue() : fallback;
    }

    private static double decimal(Map<String, Object> object, String key, double fallback) {
        Object value = object.get(key);
        return value instanceof Number number ? number.doubleValue() : fallback;
    }

    private static String string(Map<String, Object> object, String key, String fallback) {
        Object value = object.get(key);
        return value instanceof String text ? text : fallback;
    }

    private static List<?> asArray(Object value, String label) {
        if (value == null) return List.of();
        if (value instanceof List<?> list) return list;
        throw new IllegalArgumentException("Expected JSON array for " + label + ".");
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> asObject(Object value, String label) {
        if (value instanceof Map<?, ?>) return (Map<String, Object>) value;
        throw new IllegalArgumentException("Expected JSON object for " + label + ".");
    }

    private static final class JsonParser {
        private final String source;
        private int index;

        private JsonParser(String source) {
            this.source = source;
        }

        private Object parse() {
            Object value = readValue();
            skipWhitespace();
            if (index != source.length()) fail("Unexpected trailing content");
            return value;
        }

        private Object readValue() {
            skipWhitespace();
            if (index >= source.length()) return fail("Unexpected end of JSON");
            return switch (source.charAt(index)) {
                case '{' -> readObject();
                case '[' -> readArray();
                case '"' -> readString();
                case 't' -> readLiteral("true", true);
                case 'f' -> readLiteral("false", false);
                case 'n' -> readLiteral("null", null);
                default -> readNumber();
            };
        }

        private Map<String, Object> readObject() {
            index++;
            Map<String, Object> object = new java.util.LinkedHashMap<>();
            skipWhitespace();
            if (consume('}')) return object;
            while (true) {
                skipWhitespace();
                if (index >= source.length() || source.charAt(index) != '"') fail("Expected object key");
                String key = readString();
                skipWhitespace();
                require(':');
                object.put(key, readValue());
                skipWhitespace();
                if (consume('}')) return object;
                require(',');
            }
        }

        private List<Object> readArray() {
            index++;
            List<Object> array = new ArrayList<>();
            skipWhitespace();
            if (consume(']')) return array;
            while (true) {
                array.add(readValue());
                skipWhitespace();
                if (consume(']')) return array;
                require(',');
            }
        }

        private String readString() {
            require('"');
            StringBuilder value = new StringBuilder();
            while (index < source.length()) {
                char c = source.charAt(index++);
                if (c == '"') return value.toString();
                if (c != '\\') {
                    if (c < 0x20) fail("Invalid control character in string");
                    value.append(c);
                    continue;
                }
                if (index >= source.length()) fail("Incomplete escape sequence");
                char escaped = source.charAt(index++);
                switch (escaped) {
                    case '"', '\\', '/' -> value.append(escaped);
                    case 'b' -> value.append('\b');
                    case 'f' -> value.append('\f');
                    case 'n' -> value.append('\n');
                    case 'r' -> value.append('\r');
                    case 't' -> value.append('\t');
                    case 'u' -> {
                        if (index + 4 > source.length()) fail("Incomplete unicode escape");
                        try {
                            value.append((char) Integer.parseInt(source.substring(index, index + 4), 16));
                        } catch (NumberFormatException e) {
                            fail("Invalid unicode escape");
                        }
                        index += 4;
                    }
                    default -> fail("Invalid escape sequence");
                }
            }
            return fail("Unterminated string");
        }

        private Object readNumber() {
            int start = index;
            if (consume('-') && index >= source.length()) fail("Invalid number");
            while (index < source.length() && Character.isDigit(source.charAt(index))) index++;
            if (consume('.')) {
                while (index < source.length() && Character.isDigit(source.charAt(index))) index++;
            }
            if (index < source.length() && (source.charAt(index) == 'e' || source.charAt(index) == 'E')) {
                index++;
                if (index < source.length() && (source.charAt(index) == '+' || source.charAt(index) == '-')) index++;
                while (index < source.length() && Character.isDigit(source.charAt(index))) index++;
            }
            try {
                return Double.parseDouble(source.substring(start, index));
            } catch (NumberFormatException e) {
                return fail("Invalid number");
            }
        }

        private Object readLiteral(String literal, Object value) {
            if (!source.startsWith(literal, index)) return fail("Invalid JSON token");
            index += literal.length();
            return value;
        }

        private boolean consume(char expected) {
            if (index < source.length() && source.charAt(index) == expected) {
                index++;
                return true;
            }
            return false;
        }

        private void require(char expected) {
            if (!consume(expected)) fail("Expected '" + expected + "'");
        }

        private void skipWhitespace() {
            while (index < source.length() && Character.isWhitespace(source.charAt(index))) index++;
        }

        private <T> T fail(String message) {
            throw new IllegalArgumentException(message + " at JSON offset " + index);
        }
    }
}
