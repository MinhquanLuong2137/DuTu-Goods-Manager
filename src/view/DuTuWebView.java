package view;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import model.*;
import service.LedgerService;
import service.PersistenceService;
import service.ProductService;

import java.awt.Desktop;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DuTuWebView {
    private final ProductService productService;
    private final LedgerService ledgerService;
    private final int port;

    public DuTuWebView(ProductService productService, LedgerService ledgerService, int port) {
        this.productService = productService;
        this.ledgerService = ledgerService;
        this.port = port;
    }

    public void start() {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

            server.createContext("/", this::handleDashboard);
            server.createContext("/product/add", this::handleAddProduct);
            server.createContext("/product/edit", this::handleEditProduct);
            server.createContext("/product/delete", this::handleDeleteProduct);
            server.createContext("/shelf/add", this::handleAddShelf);
            server.createContext("/shelf/rename", this::handleRenameShelf);
            server.createContext("/shelf/delete", this::handleDeleteShelf);
            server.createContext("/stock/audit", this::handleAuditStock);
            server.createContext("/stock/restock", this::handleRestock);
            server.createContext("/debt/add", this::handleAddDebt);
            server.createContext("/debt/edit", this::handleEditDebt);
            server.createContext("/debt/pay", this::handlePayDebt);
            server.createContext("/debt/restore", this::handleRestoreDebt);
            server.createContext("/closing/add", this::handleAddClosing);

            server.setExecutor(null);
            server.start();

            String url = "http://localhost:" + port;
            System.out.println("==================================================");
            System.out.println("  DuTu Web UI dang chay tai: " + url);
            System.out.println("==================================================");

            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(url));
            }
        } catch (Exception e) {
            System.out.println("[!] Khong the khoi dong Web Server: " + e.getMessage());
        }
    }

    private void handleDashboard(HttpExchange exchange) throws IOException {
        Map<String, String> queryParams = parseUrlEncoded(exchange.getRequestURI().getRawQuery());
        String activeTab = queryParams.getOrDefault("tab", "tra-gia");
        String selectedShelf = queryParams.getOrDefault("shelf", "ALL");
        String message = queryParams.getOrDefault("msg", "");

        String html = renderHtmlPage(activeTab, selectedShelf, message);
        byte[] bytes = html.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private void handleAddProduct(HttpExchange exchange) throws IOException {
        Map<String, String> form = parseForm(exchange);
        try {
            int id = parseIntOrDefault(form.get("id"), productService.getNextId());
            if (productService.findById(id) != null) {
                redirect(exchange, "tra-gia", "ALL", "Mã ID " + id + " đã tồn tại!");
                return;
            }
            String type = form.getOrDefault("type", "GENERAL");
            String name = form.getOrDefault("name", "Sản phẩm mới");

            // Xử lý 2 chế độ giá nhập: [1] Giá chia (đơn vị) vs [2] Giá nguyên cục
            double rawImport = parseDoubleOrDefault(form.get("importPrice"), 0);
            int qty = parseIntOrDefault(form.get("quantity"), 1);
            String priceMode = form.getOrDefault("priceMode", "UNIT");
            double importPrice = "BULK".equalsIgnoreCase(priceMode) && qty > 0 ? (rawImport / qty) : rawImport;

            double sellPrice = parseDoubleOrDefault(form.get("sellPrice"), 0);
            double wholesalePrice = parseDoubleOrDefault(form.get("wholesalePrice"), 0);
            String unitNote = form.getOrDefault("unitNote", "Bán lẻ");
            String location = form.getOrDefault("location", "Kệ chung");
            int minStock = parseIntOrDefault(form.get("minStock"), 5);

            Product newProduct;
            if ("EXPIRING".equalsIgnoreCase(type)) {
                String expStr = form.get("expiryDate");
                LocalDate expDate = (expStr != null && !expStr.isBlank()) ? LocalDate.parse(expStr) : LocalDate.now().plusMonths(6);
                ExpiringProduct ep = new ExpiringProduct(id, name, importPrice, sellPrice, qty, location, expDate, minStock);
                ep.addBatch(expDate, qty);
                newProduct = ep;
            } else {
                String category = form.getOrDefault("category", "Tạp hóa");
                GeneralProduct gp = new GeneralProduct(id, name, importPrice, sellPrice, qty, location, category, minStock);
                newProduct = gp;
            }
            newProduct.setWholesalePrice(wholesalePrice);
            newProduct.setUnitNote(unitNote);
            productService.addProduct(newProduct);
            PersistenceService.saveData(productService, ledgerService, null);

            redirect(exchange, "tra-gia", "ALL", "Đã thêm mặt hàng thành công: " + name);
        } catch (Exception e) {
            redirect(exchange, "tra-gia", "ALL", "Lỗi nhập liệu khi thêm hàng!");
        }
    }

    private void handleEditProduct(HttpExchange exchange) throws IOException {
        Map<String, String> form = parseForm(exchange);
        int id = parseIntOrDefault(form.get("id"), -1);
        String name = form.getOrDefault("name", "");
        double importPrice = parseDoubleOrDefault(form.get("importPrice"), 0);
        double sellPrice = parseDoubleOrDefault(form.get("sellPrice"), 0);
        double wholesalePrice = parseDoubleOrDefault(form.get("wholesalePrice"), 0);
        String unitNote = form.getOrDefault("unitNote", "");
        int quantity = parseIntOrDefault(form.get("quantity"), 0);
        String location = form.getOrDefault("location", "");
        int minStock = parseIntOrDefault(form.get("minStock"), 5);

        String expStr = form.get("expiryDate");
        LocalDate expDate = (expStr != null && !expStr.isBlank()) ? LocalDate.parse(expStr) : null;

        if (productService.updateFullProduct(id, name, importPrice, sellPrice, wholesalePrice, unitNote, quantity, location, minStock, expDate)) {
            PersistenceService.saveData(productService, ledgerService, null);
            redirect(exchange, "tra-gia", "ALL", "Đã cập nhật mặt hàng #" + id);
        } else {
            redirect(exchange, "tra-gia", "ALL", "Không thể cập nhật mặt hàng!");
        }
    }

    private void handleDeleteProduct(HttpExchange exchange) throws IOException {
        Map<String, String> form = parseForm(exchange);
        int id = parseIntOrDefault(form.get("id"), -1);
        if (productService.removeProduct(id)) {
            PersistenceService.saveData(productService, ledgerService, null);
            redirect(exchange, "tra-gia", "ALL", "Đã xóa mặt hàng #" + id);
        } else {
            redirect(exchange, "tra-gia", "ALL", "Không tìm thấy mặt hàng cần xóa!");
        }
    }

    private void handleAddShelf(HttpExchange exchange) throws IOException {
        Map<String, String> form = parseForm(exchange);
        String shelfName = form.getOrDefault("shelfName", "").trim();
        if (productService.addLocation(shelfName)) {
            PersistenceService.saveData(productService, ledgerService, null);
            redirect(exchange, "tra-gia", "ALL", "Đã tạo kệ mới: " + shelfName);
        } else if (!shelfName.isEmpty()) {
            redirect(exchange, "tra-gia", "ALL", "Kệ đã tồn tại: " + shelfName);
        } else {
            redirect(exchange, "tra-gia", "ALL", "Tên kệ không được để trống!");
        }
    }

    private void handleRenameShelf(HttpExchange exchange) throws IOException {
        Map<String, String> form = parseForm(exchange);
        String oldName = form.getOrDefault("oldName", "").trim();
        String newName = form.getOrDefault("newName", "").trim();
        if (!oldName.isEmpty() && !newName.isEmpty() && productService.getAllLocations().stream().anyMatch(s -> s.equalsIgnoreCase(oldName))) {
            int count = productService.renameLocation(oldName, newName);
            PersistenceService.saveData(productService, ledgerService, null);
            redirect(exchange, "tra-gia", "ALL", "Đã đổi tên kệ [" + oldName + "] thành [" + newName + "] (" + count + " mặt hàng)");
        } else {
            redirect(exchange, "tra-gia", "ALL", "Vui lòng nhập tên kệ hợp lệ!");
        }
    }

    private void handleDeleteShelf(HttpExchange exchange) throws IOException {
        Map<String, String> form = parseForm(exchange);
        String shelfName = form.getOrDefault("shelfName", "").trim();
        if (!productService.findByLocation(shelfName).isEmpty()) {
            int count = productService.findByLocation(shelfName).size();
            redirect(exchange, "tra-gia", "ALL", "Không thể xóa kệ [" + shelfName + "] vì còn chứa " + count + " mặt hàng!");
        } else if (productService.removeLocation(shelfName)) {
            PersistenceService.saveData(productService, ledgerService, null);
            redirect(exchange, "tra-gia", "ALL", "Đã xóa kệ trống: " + shelfName);
        } else {
            redirect(exchange, "tra-gia", "ALL", "Không tìm thấy kệ: " + shelfName);
        }
    }

    private void handleAuditStock(HttpExchange exchange) throws IOException {
        Map<String, String> form = parseForm(exchange);
        int id = parseIntOrDefault(form.get("id"), -1);
        int actualCount = parseIntOrDefault(form.get("actualCount"), -1);
        String shelf = form.getOrDefault("shelf", "ALL");
        String note = form.getOrDefault("note", "");

        Product p = productService.findById(id);
        if (p != null && actualCount >= 0) {
            int oldQty = p.getQuantity();
            int soldQty = productService.auditStockCount(id, actualCount);
            ledgerService.logAudit(p, oldQty, actualCount, soldQty, note);
            PersistenceService.saveData(productService, ledgerService, null);
            redirect(exchange, "kiem-ke", shelf, "Đã chốt đếm [" + p.getName() + "]: Tồn mới = " + actualCount);
        } else {
            redirect(exchange, "kiem-ke", shelf, "Số lượng đếm không hợp lệ!");
        }
    }

    private void handleRestock(HttpExchange exchange) throws IOException {
        Map<String, String> form = parseForm(exchange);
        int id = parseIntOrDefault(form.get("id"), -1);
        int addedQty = parseIntOrDefault(form.get("addedQty"), 0);
        double rawImport = parseDoubleOrDefault(form.get("importPrice"), 0);
        String priceMode = form.getOrDefault("priceMode", "UNIT");
        double importPrice = "BULK".equalsIgnoreCase(priceMode) && addedQty > 0 ? (rawImport / addedQty) : rawImport;

        String expStr = form.get("expiryDate");
        String note = form.getOrDefault("note", "");
        String shelf = form.getOrDefault("shelf", "ALL");

        Product p = productService.findById(id);
        if (p != null && addedQty > 0) {
            int oldQty = p.getQuantity();
            productService.restockProduct(id, addedQty, importPrice);
            if (p instanceof ExpiringProduct && expStr != null && !expStr.isBlank()) {
                LocalDate expDate = LocalDate.parse(expStr);
                p.addBatch(expDate, addedQty);
            }
            ledgerService.logRestock(p, oldQty, addedQty, importPrice, note);
            PersistenceService.saveData(productService, ledgerService, null);
            redirect(exchange, "kiem-ke", shelf, "Đã nhập thêm +" + addedQty + " [" + p.getName() + "] vào kho!");
        } else {
            redirect(exchange, "kiem-ke", shelf, "Vui lòng nhập số lượng hàng về lớn hơn 0!");
        }
    }

    private void handleAddDebt(HttpExchange exchange) throws IOException {
        Map<String, String> form = parseForm(exchange);
        String customerName = form.getOrDefault("customerName", "").trim();
        String phoneOrAddress = form.getOrDefault("phoneOrAddress", "").trim();
        String itemsNote = form.getOrDefault("itemsNote", "").trim();
        double amount = parseDoubleOrDefault(form.get("amount"), 0);

        if (!customerName.isEmpty() && amount > 0) {
            ledgerService.addDebt(customerName, phoneOrAddress, itemsNote, amount);
            PersistenceService.saveData(productService, ledgerService, null);
            redirect(exchange, "so-no", "ALL", "Đã ghi sổ nợ cho: " + customerName);
        } else {
            redirect(exchange, "so-no", "ALL", "Vui lòng nhập tên khách và số tiền nợ!");
        }
    }

    private void handleEditDebt(HttpExchange exchange) throws IOException {
        Map<String, String> form = parseForm(exchange);
        int debtId = parseIntOrDefault(form.get("debtId"), -1);
        String customerName = form.getOrDefault("customerName", "").trim();
        String phoneOrAddress = form.getOrDefault("phoneOrAddress", "").trim();
        String itemsNote = form.getOrDefault("itemsNote", "").trim();
        double amount = parseDoubleOrDefault(form.get("amount"), 0);

        if (ledgerService.updateDebt(debtId, customerName, phoneOrAddress, itemsNote, amount)) {
            PersistenceService.saveData(productService, ledgerService, null);
            redirect(exchange, "so-no", "ALL", "Đã cập nhật thông tin khoản nợ #" + debtId);
        } else {
            redirect(exchange, "so-no", "ALL", "Không tìm thấy khoản nợ cần sửa!");
        }
    }

    private void handlePayDebt(HttpExchange exchange) throws IOException {
        Map<String, String> form = parseForm(exchange);
        int debtId = parseIntOrDefault(form.get("debtId"), -1);
        if (ledgerService.markDebtPaidOrRemove(debtId)) {
            PersistenceService.saveData(productService, ledgerService, null);
            redirect(exchange, "so-no", "ALL", "Đã thanh toán và chuyển khoản nợ #" + debtId + " vào thùng rác gần đây.");
        } else {
            redirect(exchange, "so-no", "ALL", "Không tìm thấy khoản nợ!");
        }
    }

    private void handleRestoreDebt(HttpExchange exchange) throws IOException {
        Map<String, String> form = parseForm(exchange);
        int debtId = parseIntOrDefault(form.get("debtId"), -1);
        if (ledgerService.restoreDebtFromTrash(debtId)) {
            PersistenceService.saveData(productService, ledgerService, null);
            redirect(exchange, "so-no", "ALL", "Đã khôi phục khoản nợ #" + debtId + " về sổ chính.");
        } else {
            redirect(exchange, "so-no", "ALL", "Không thể khôi phục khoản nợ!");
        }
    }

    private void handleAddClosing(HttpExchange exchange) throws IOException {
        Map<String, String> form = parseForm(exchange);
        String dateStr = form.get("date");
        LocalDate date = (dateStr != null && !dateStr.isBlank()) ? LocalDate.parse(dateStr) : LocalDate.now();
        double cash = parseDoubleOrDefault(form.get("cashAmount"), 0);
        double transfer = parseDoubleOrDefault(form.get("transferAmount"), 0);
        double expense = parseDoubleOrDefault(form.get("importExpense"), 0);
        String note = form.getOrDefault("note", "");

        ledgerService.addDailyClosing(date, cash, transfer, expense, note);
        PersistenceService.saveData(productService, ledgerService, null);
        redirect(exchange, "so-cuoi-ngay", "ALL", "Đã lưu sổ cộng tiền ngày " + date);
    }

    private String renderHtmlPage(String activeTab, String selectedShelf, String message) {
        List<Product> allProducts = productService.findAll();
        List<Product> lowStock = productService.getLowStockProducts();
        List<ExpiringProduct> expiringSoon = productService.getExpiringSoonProducts(14);
        List<Product> slowMoving = productService.getSlowMovingProducts(60);
        List<Product> topSelling = productService.getTopSellingProducts();
        List<String> shelves = productService.getAllLocations();

        StringBuilder sb = new StringBuilder();
        sb.append("""
        <!DOCTYPE html>
        <html lang="vi">
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <link rel="icon" type="image/svg+xml" href="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 100 100'%3E%3Crect width='100' height='100' rx='22' fill='%23E6C280'/%3E%3Cpath d='M15 35h70l-5-15H20z' fill='%237B2869'/%3E%3Cpath d='M15 35q7 7 14 0 7 7 14 0 7 7 14 0 7 7 14 0 7 7 14 0' fill='%23F9EBF2' stroke='%23C84B83' stroke-width='4'/%3E%3Crect x='22' y='42' width='56' height='42' rx='6' fill='%23FFFDF9' stroke='%234A154B' stroke-width='3'/%3E%3Cpath d='M28 58h44M28 72h44' stroke='%23E6C280' stroke-width='3'/%3E%3Ccircle cx='75' cy='75' r='14' fill='%23C84B83'/%3E%3C/svg%3E">
            <title>DuTu - Sổ Sách Điện Tử & Tra Giá Tạp Hóa</title>
            <style>
                :root {
                    --plum-dark: #4A154B;
                    --plum-main: #7B2869;
                    --pink-accent: #C84B83;
                    --pink-soft: #F9EBF2;
                    --sand-gold: #E6C280;
                    --sand-deep: #C59B47;
                    --sand-bg: #FCF8F0;
                    --sand-card: #FFFDF9;
                    --text-main: #2D1B28;
                    --danger: #D9383A;
                    --success: #1F8A5B;
                }
                * { box-sizing: border-box; font-family: 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; }
                body { margin: 0; background-color: var(--sand-bg); color: var(--text-main); font-size: 17px; }
                header {
                    background: linear-gradient(135deg, var(--plum-dark) 0%, var(--plum-main) 55%, var(--pink-accent) 100%);
                    color: white; padding: 18px 28px; border-bottom: 5px solid var(--sand-gold);
                    display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 15px;
                    box-shadow: 0 4px 15px rgba(74, 21, 75, 0.2);
                }
                .brand-container { display: flex; align-items: center; gap: 15px; }
                .app-logo { width: 55px; height: 55px; border-radius: 12px; border: 2px solid var(--sand-gold); object-fit: cover; box-shadow: 0 3px 8px rgba(0,0,0,0.3); }
                .brand-title { font-size: 28px; font-weight: 800; display: flex; align-items: center; gap: 12px; }
                .brand-badge { background-color: var(--sand-gold); color: var(--plum-dark); padding: 4px 12px; border-radius: 20px; font-size: 15px; font-weight: 700; }
                
                .app-logo-svg {
                            display: flex;
                            align-items: center;
                            justify-content: center;
                            border-radius: 14px;
                            border: 2px solid var(--sand-gold);
                            box-shadow: 0 3px 10px rgba(0,0,0,0.25);
                            overflow: hidden;
                            background: var(--sand-gold);
                        }
                
                .quick-stats { display: flex; gap: 12px; flex-wrap: wrap; }
                .stat-pill { background: rgba(255, 255, 255, 0.16); border: 1px solid var(--sand-gold); padding: 8px 16px; border-radius: 12px; font-size: 15px; font-weight: 600; }
                .stat-pill strong { color: var(--sand-gold); font-size: 19px; }

                nav { display: flex; gap: 10px; padding: 14px 28px; background: #F4EAD5; border-bottom: 2px solid #E2D1B0; flex-wrap: wrap; position: sticky; top: 0; z-index: 50; }
                .tab-btn {
                    padding: 12px 20px; border-radius: 12px; border: 2px solid transparent; background: var(--sand-card);
                    color: var(--plum-dark); font-size: 17px; font-weight: 700; cursor: pointer; text-decoration: none; transition: 0.2s;
                    display: flex; align-items: center; gap: 8px;
                }
                .tab-btn.active { background: linear-gradient(135deg, var(--plum-main), var(--pink-accent)); color: white; border-color: var(--sand-gold); }
                .debt-view-tabs { display: flex; gap: 10px; margin: 0 0 16px; flex-wrap: wrap; }
                .debt-view-panel { display: none; }
                .debt-view-panel.active { display: block; }

                .container { max-width: 1400px; margin: 22px auto; padding: 0 20px; }
                .toast {
                    background: linear-gradient(90deg, var(--plum-main), var(--pink-accent)); color: white;
                    padding: 14px 22px; border-radius: 12px; margin-bottom: 20px; font-weight: 700; border-left: 8px solid var(--sand-gold);
                    display: flex; justify-content: space-between; align-items: center;
                }
                .card { background: var(--sand-card); border: 2px solid #EBDDC3; border-radius: 16px; padding: 22px; margin-bottom: 24px; box-shadow: 0 4px 12px rgba(74, 21, 75, 0.05); }
                .card-header { margin-top: 0; color: var(--plum-dark); font-size: 22px; border-bottom: 3px solid var(--sand-gold); padding-bottom: 10px; margin-bottom: 18px; display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; }
                
                .search-box {
                    width: 100%; padding: 16px 20px; font-size: 20px; border: 3px solid var(--plum-main);
                    border-radius: 14px; background: #FFF; color: var(--text-main); font-weight: 600; margin-bottom: 16px; outline: none;
                }
                .shelf-bar { display: flex; gap: 8px; flex-wrap: wrap; margin-bottom: 18px; }
                .shelf-chip { padding: 8px 16px; border-radius: 20px; background: var(--pink-soft); color: var(--plum-dark); font-weight: 700; border: 2px solid #E5C1D5; cursor: pointer; font-size: 16px; text-decoration: none; }
                .shelf-chip.active { background: var(--sand-gold); color: var(--plum-dark); border-color: var(--plum-dark); }

                .price-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(340px, 1fr)); gap: 18px; }
                .product-card { background: white; border: 2px solid #E6D7BE; border-radius: 14px; padding: 18px; display: flex; flex-direction: column; justify-content: space-between; }
                .product-name { font-size: 21px; font-weight: 800; color: var(--plum-dark); margin: 4px 0 10px 0; }
                .price-highlight { background: var(--pink-soft); border-left: 5px solid var(--plum-main); padding: 10px 14px; border-radius: 8px; margin-bottom: 10px; }
                .retail-price { font-size: 25px; font-weight: 900; color: var(--pink-accent); }
                .wholesale-price { font-size: 17px; font-weight: 700; color: var(--plum-dark); margin-top: 4px; }
                .meta-row { display: flex; justify-content: space-between; font-size: 15px; margin-top: 6px; color: #555; font-weight: 600; }

                .badge { display: inline-block; padding: 4px 10px; border-radius: 8px; font-size: 13px; font-weight: 700; margin-right: 5px; }
                .badge-shelf { background: var(--sand-gold); color: var(--plum-dark); }
                .badge-low { background: #FFE3E3; color: var(--danger); border: 1px solid var(--danger); }
                .badge-slow { background: #FFF3CD; color: #996500; border: 1px solid #E6C280; }
                .badge-exp { background: #F8D7DA; color: #842029; }

                table { width: 100%; border-collapse: collapse; margin-top: 10px; }
                th { background: var(--plum-dark); color: var(--sand-gold); padding: 12px; text-align: left; font-size: 16px; }
                td { padding: 12px; border-bottom: 1px solid #EBDDC3; font-size: 16px; vertical-align: middle; }
                tr:hover { background-color: #FAF3E6; }

                input, select, textarea { padding: 10px 12px; font-size: 16px; border: 2px solid #D5C3A5; border-radius: 8px; background: #FFF; width: 100%; }
                .form-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(210px, 1fr)); gap: 14px; }
                .btn { padding: 10px 18px; border-radius: 10px; border: none; font-size: 16px; font-weight: 700; cursor: pointer; transition: 0.15s; }
                .btn-primary { background: linear-gradient(135deg, var(--plum-main), var(--pink-accent)); color: white; }
                .btn-sand { background: var(--sand-gold); color: var(--plum-dark); border: 2px solid var(--plum-dark); }
                .btn-danger { background: #FCE8E8; color: var(--danger); border: 1px solid var(--danger); }
                .btn-success { background: var(--success); color: white; }
                .btn-outline { background: white; border: 2px solid var(--plum-main); color: var(--plum-main); }
                .tab-section { display: none; }
                .tab-section.active { display: block; }
                .two-col { display: grid; grid-template-columns: repeat(auto-fit, minmax(460px, 1fr)); gap: 22px; }
                .card-actions { display: flex; gap: 8px; margin-top: 14px; border-top: 1px dashed #DFC9A6; padding-top: 10px; }

                .modal { display: none; position: fixed; z-index: 100; left: 0; top: 0; width: 100%; height: 100%; background: rgba(0,0,0,0.6); }
                .modal-content { background: var(--sand-card); margin: 5% auto; padding: 25px; border-radius: 16px; max-width: 650px; border: 3px solid var(--plum-main); box-shadow: 0 10px 30px rgba(0,0,0,0.3); }
            </style>
        </head>
        <body>
        """);

        // Header có Logo thương hiệu
// Header có Logo SVG thương hiệu
        sb.append("<header>");
        sb.append("<div class='brand-container'>");
        sb.append("  <div class='app-logo-svg'>");
        sb.append("    <svg width='45' height='45' viewBox='0 0 100 100' fill='none' xmlns='http://www.w3.org/2000/svg'>");
        sb.append("      <rect width='100' height='100' rx='22' fill='#E6C280'/>");
        sb.append("      <path d='M15 35 L85 35 L80 20 L20 20 Z' fill='#7B2869'/>");
        sb.append("      <path d='M15 35 Q22 42 29 35 Q36 42 43 35 Q50 42 57 35 Q64 42 71 35 Q78 42 85 35' stroke='#C84B83' stroke-width='4' fill='#F9EBF2'/>");
        sb.append("      <rect x='22' y='42' width='56' height='42' rx='6' fill='#FFFDF9' stroke='#4A154B' stroke-width='3'/>");
        sb.append("      <line x1='28' y1='58' x2='72' y2='58' stroke='#E6C280' stroke-width='3'/>");
        sb.append("      <line x1='28' y1='72' x2='72' y2='72' stroke='#E6C280' stroke-width='3'/>");
        sb.append("      <circle cx='75' cy='75' r='14' fill='#C84B83'/>");
        sb.append("      <path d='M68 73 L71 78 L80 78' stroke='#FFF' stroke-width='2.5' stroke-linecap='round'/>");
        sb.append("      <circle cx='72' cy='81' r='1.5' fill='#FFF'/>");
        sb.append("      <circle cx='78' cy='81' r='1.5' fill='#FFF'/>");
        sb.append("    </svg>");
        sb.append("  </div>");
        sb.append("  <div class='brand-title'>DuTu <span class='brand-badge'>Tạp Hóa Thông Minh</span></div>");
        sb.append("</div>");

        sb.append("<div class='quick-stats'>");
        sb.append("<div class='stat-pill'>Tổng mặt hàng: <strong>").append(allProducts.size()).append("</strong></div>");
        sb.append("<div class='stat-pill'>Sắp hết: <strong>").append(lowStock.size()).append("</strong></div>");
        sb.append("<div class='stat-pill'>Tổng nợ: <strong>").append(formatMoney(ledgerService.getTotalUnpaidDebt())).append("đ</strong></div>");
        sb.append("</div></header>");

        // Navigation có Icon đầy đủ
        sb.append("<nav>");
        sb.append(renderTabButton("tra-gia", "🔍 1. Tra Giá & Kho", activeTab));
        sb.append(renderTabButton("kiem-ke", "📋 2. Đếm Kệ & Nhập Hàng", activeTab));
        sb.append(renderTabButton("canh-bao", "⚠️ 3. Sổ Cảnh Báo", activeTab));
        sb.append(renderTabButton("so-no", "📓 4. Sổ Nợ Khách Hàng", activeTab));
        sb.append(renderTabButton("so-cuoi-ngay", "💰 5. Sổ Cộng Tiền", activeTab));
        sb.append("</nav>");

        sb.append("<div class='container'>");
        if (message != null && !message.isBlank()) {
            sb.append("<div class='toast'><span>🔔 ").append(escapeHtml(message)).append("</span>")
                    .append("<button class='btn btn-sand' onclick='this.parentElement.remove()'>Đóng</button></div>");
        }

        sb.append("<div id='tab-tra-gia' class='tab-section ").append("tra-gia".equals(activeTab) ? "active" : "").append("'>");
        sb.append(renderTraGiaTab(allProducts, shelves));
        sb.append("</div>");

        sb.append("<div id='tab-kiem-ke' class='tab-section ").append("kiem-ke".equals(activeTab) ? "active" : "").append("'>");
        sb.append(renderKiemKeTab(selectedShelf, shelves));
        sb.append("</div>");

        sb.append("<div id='tab-canh-bao' class='tab-section ").append("canh-bao".equals(activeTab) ? "active" : "").append("'>");
        sb.append(renderCanhBaoTab(lowStock, expiringSoon, slowMoving, topSelling));
        sb.append("</div>");

        sb.append("<div id='tab-so-no' class='tab-section ").append("so-no".equals(activeTab) ? "active" : "").append("'>");
        sb.append(renderSoNoTab());
        sb.append("</div>");

        sb.append("<div id='tab-so-cuoi-ngay' class='tab-section ").append("so-cuoi-ngay".equals(activeTab) ? "active" : "").append("'>");
        sb.append(renderSoCuoiNgayTab());
        sb.append("</div>");

        sb.append(renderEditModal());
        sb.append(renderEditDebtModal());

        sb.append("</div>");

        // JavaScript hỗ trợ
        sb.append("""
        <script>
            function formatCurrencyInput(element) {
                const digits = element.value.replace(/\\D/g, '');
                element.value = digits ? Number(digits).toLocaleString('vi-VN') : '';
            }

            document.querySelectorAll('.currency-input').forEach(input => {
                input.addEventListener('input', () => formatCurrencyInput(input));
                formatCurrencyInput(input);
            });
            document.querySelectorAll('form').forEach(form => {
                form.addEventListener('submit', () => {
                    form.querySelectorAll('.currency-input').forEach(input => {
                        input.value = input.value.replace(/\\D/g, '');
                    });
                });
            });

            function removeVietnameseTones(str) {
                if (!str) return '';
                str = str.normalize('NFD').replace(/[\\u0300-\\u036f]/g, '');
                str = str.replace(/[đĐ]/g, 'd');
                return str.toLowerCase().trim();
            }

            function switchTab(tabId, btnEl) {
                document.querySelectorAll('.tab-section').forEach(s => s.classList.remove('active'));
                document.querySelectorAll('.tab-btn').forEach(b => b.classList.remove('active'));
                document.getElementById('tab-' + tabId).classList.add('active');
                if (btnEl) btnEl.classList.add('active');
            }

            function switchDebtorView(viewId, btnEl) {
                document.querySelectorAll('.debt-view-panel').forEach(panel => panel.classList.remove('active'));
                document.querySelectorAll('.debt-view-tabs .tab-btn').forEach(button => button.classList.remove('active'));
                document.getElementById(viewId).classList.add('active');
                btnEl.classList.add('active');
            }

            function filterProducts() {
                const raw = document.getElementById('quickSearchInput').value;
                const q = removeVietnameseTones(raw);
                const activeShelf = document.querySelector('.shelf-chip.active')?.getAttribute('data-shelf') || 'ALL';

                document.querySelectorAll('.product-card').forEach(card => {
                    const searchData = removeVietnameseTones(card.getAttribute('data-search'));
                    const shelf = card.getAttribute('data-shelf');
                    const matchQuery = !q || searchData.includes(q);
                    const matchShelf = (activeShelf === 'ALL') || (shelf === activeShelf);
                    card.style.display = (matchQuery && matchShelf) ? 'flex' : 'none';
                });
            }

            function selectShelfChip(el, shelfName) {
                document.querySelectorAll('#shelfFilterBar .shelf-chip').forEach(c => c.classList.remove('active'));
                el.classList.add('active');
                filterProducts();
            }

            function toggleProductType() {
                const type = document.getElementById('prodTypeSelect').value;
                document.getElementById('expiryDateBox').style.display = (type === 'EXPIRING') ? 'block' : 'none';
                document.getElementById('categoryBox').style.display = (type === 'GENERAL') ? 'block' : 'none';
            }

            function openEditModal(btn) {
                document.getElementById('editId').value = btn.dataset.id;
                document.getElementById('editName').value = btn.dataset.name;
                document.getElementById('editImportPrice').value = btn.dataset.import;
                document.getElementById('editSellPrice').value = btn.dataset.sell;
                document.getElementById('editWholesalePrice').value = btn.dataset.wholesale;
                formatCurrencyInput(document.getElementById('editImportPrice'));
                formatCurrencyInput(document.getElementById('editSellPrice'));
                formatCurrencyInput(document.getElementById('editWholesalePrice'));
                document.getElementById('editUnitNote').value = btn.dataset.unit;
                document.getElementById('editQuantity').value = btn.dataset.quantity;
                document.getElementById('editLocation').value = btn.dataset.location;
                document.getElementById('editMinStock').value = btn.dataset.minstock;

                const exp = btn.dataset.expiry;
                if (exp && exp !== 'null' && exp !== '') {
                    document.getElementById('editExpiryBox').style.display = 'block';
                    document.getElementById('editExpiryDate').value = exp;
                } else {
                    document.getElementById('editExpiryBox').style.display = 'none';
                }
                document.getElementById('editModal').style.display = 'block';
            }

            function closeEditModal() { document.getElementById('editModal').style.display = 'none'; }

            function openEditDebtModal(id, name, phone, items, amount) {
                document.getElementById('editDebtId').value = id;
                document.getElementById('editCustomerName').value = name;
                document.getElementById('editPhoneOrAddress').value = phone;
                document.getElementById('editItemsNote').value = items;
                document.getElementById('editDebtAmount').value = amount;
                formatCurrencyInput(document.getElementById('editDebtAmount'));
                document.getElementById('editDebtModal').style.display = 'block';
            }
            function closeEditDebtModal() { document.getElementById('editDebtModal').style.display = 'none'; }
        </script>
        </body></html>
        """);
        return sb.toString();
    }

    private String renderTabButton(String tabId, String label, String activeTab) {
        String activeClass = tabId.equals(activeTab) ? "active" : "";
        return "<button type='button' class='tab-btn " + activeClass + "' onclick=\"switchTab('" + tabId + "', this)\">" + label + "</button>";
    }

    private String renderTraGiaTab(List<Product> products, List<String> shelves) {
        StringBuilder sb = new StringBuilder();
        sb.append("<div class='card'>");
        sb.append("<h2 class='card-header'><span>🔍 Tra Giá Nhanh & Kho Hàng</span>")
                .append("<span style='font-size:15px;color:#666;'>Tìm kiếm không dấu (Gõ 'nuoc', 'sua'...)</span></h2>");

        sb.append("<input type='text' id='quickSearchInput' class='search-box' onkeyup='filterProducts()' ")
                .append("placeholder='🔍 Gõ tên hàng, vị trí kệ, quy cách...' autofocus>");

        sb.append("<div class='shelf-bar' id='shelfFilterBar'>");
        sb.append("<span class='shelf-chip active' data-shelf='ALL' onclick=\"selectShelfChip(this, 'ALL')\">Tất cả kệ (").append(products.size()).append(")</span>");
        for (String shelf : shelves) {
            sb.append("<span class='shelf-chip' data-shelf='").append(escapeHtml(shelf))
                    .append("' onclick=\"selectShelfChip(this, '").append(escapeHtml(shelf)).append("')\">📍 ")
                    .append(escapeHtml(shelf)).append("</span>");
        }
        sb.append("</div>");

        sb.append("<div class='price-grid'>");
        for (Product p : products) {
            LocalDate nearestExp = p.getNearestExpiryDate();
            String expDateStr = nearestExp != null ? nearestExp.toString() : "";
            String searchAttr = p.getID() + " " + p.getName() + " " + p.getLocation() + " " + p.getUnitNote() + " " + expDateStr;

            sb.append("<div class='product-card' data-search='").append(escapeHtml(searchAttr))
                    .append("' data-shelf='").append(escapeHtml(p.getLocation())).append("'>");
            sb.append("<div>");
            sb.append("<div><span class='badge badge-shelf'>📍 ").append(escapeHtml(p.getLocation())).append("</span>");
            if (p.isLowStock()) {
                sb.append("<span class='badge badge-low'>Sắp hết (Còn ").append(p.getQuantity()).append(")</span>");
            }
            if (p.isSlowMoving(60)) {
                sb.append("<span class='badge badge-slow'>⏳ Hàng ế</span>");
            }
            if (nearestExp != null) {
                sb.append("<span class='badge badge-exp'>HSD gần: ").append(nearestExp).append("</span>");
            }
            sb.append("</div>");

            sb.append("<div class='product-name'>#").append(p.getID()).append(" - ").append(escapeHtml(p.getName())).append("</div>");

            sb.append("<div class='price-highlight'>");
            sb.append("<div class='retail-price'>Giá bán lẻ: ").append(formatMoney(p.getSellPrice())).append(" đ</div>");
            if (p.getWholesalePrice() > 0) {
                sb.append("<div class='wholesale-price'>📦 Giá sỉ/thùng: ").append(formatMoney(p.getWholesalePrice()))
                        .append(" đ <small>(").append(escapeHtml(p.getUnitNote())).append(")</small></div>");
            } else {
                sb.append("<div class='wholesale-price' style='color:#777;'>📦 Chỉ bán lẻ</div>");
            }
            sb.append("</div>");

            sb.append("<div class='meta-row'><span>Giá vốn: ").append(formatMoney(p.getImportPrice())).append("đ</span>")
                    .append("<span>Tồn kho: <strong>").append(p.getQuantity()).append("</strong></span></div>");
            sb.append("<div class='meta-row'><span>Đã bán: <strong>").append(p.getTotalSold()).append("</strong></span></div>");
            sb.append("</div>");

            sb.append("<div class='card-actions'>");
            sb.append("<button type='button' class='btn btn-outline' style='flex:1;' ")
                    .append("data-id='").append(p.getID()).append("' ")
                    .append("data-name='").append(escapeHtml(p.getName())).append("' ")
                    .append("data-import='").append((long) p.getImportPrice()).append("' ")
                    .append("data-sell='").append((long) p.getSellPrice()).append("' ")
                    .append("data-wholesale='").append((long) p.getWholesalePrice()).append("' ")
                    .append("data-unit='").append(escapeHtml(p.getUnitNote())).append("' ")
                    .append("data-quantity='").append(p.getQuantity()).append("' ")
                    .append("data-location='").append(escapeHtml(p.getLocation())).append("' ")
                    .append("data-minstock='").append(p.getMinStock()).append("' ")
                    .append("data-expiry='").append(expDateStr).append("' ")
                    .append("onclick='openEditModal(this)'>✏️ Sửa</button>");

            sb.append("<form method='POST' action='/product/delete' style='flex:1; margin:0;' ")
                    .append("onsubmit=\"return confirm('CẢNH BÁO: Bạn có chắc chắn muốn xóa mặt hàng [" + escapeHtml(p.getName()) + "] khỏi kho không?');\">")
                    .append("<input type='hidden' name='id' value='").append(p.getID()).append("'>")
                    .append("<button type='submit' class='btn btn-danger' style='width:100%;'>🗑️ Xóa</button></form>");
            sb.append("</div>");

            sb.append("</div>");
        }
        sb.append("</div></div>");

        // KHU VỰC THÊM HÀNG (Có 2 chế độ giá) & QUẢN LÝ KỆ
        sb.append("<div class='two-col'>");

        sb.append("<div class='card'>");
        sb.append("<h3 class='card-header'>➕ Thêm Mặt Hàng Mới Vào Sổ Kho</h3>");
        sb.append("<form method='POST' action='/product/add'>");
        sb.append("<div class='form-grid'>");
        sb.append("<div><label>Mã hàng (ID):</label><input type='number' name='id' value='").append(productService.getNextId()).append("' required></div>");
        sb.append("<div><label>Tên mặt hàng:</label><input type='text' name='name' placeholder='VD: Sữa tươi' required></div>");
        sb.append("<div><label>Phân loại hàng:</label><select name='type' id='prodTypeSelect' onchange='toggleProductType()'>")
                .append("<option value='EXPIRING'>Hàng có HSD (Sữa, bánh, bia...)</option>")
                .append("<option value='GENERAL'>Hàng bách hóa chung (Gia vị, đồ điện...)</option></select></div>");
        sb.append("<div><label>Vị trí kệ trưng bày:</label><select name='location'>");
        for (String s : shelves) {
            sb.append("<option value='").append(escapeHtml(s)).append("'>📍 ").append(escapeHtml(s)).append("</option>");
        }
        sb.append("</select></div>");

        // 2 Chế độ nhập giá
        sb.append("<div><label>Chế độ nhập giá vốn:</label><select name='priceMode'>")
                .append("<option value='UNIT'>1. Nhập giá chia (Cho 1 đơn vị lẻ)</option>")
                .append("<option value='BULK'>2. Nhập tổng tiền nguyên cục (Cả lô)</option></select></div>");
        sb.append("<div><label>Giá vốn (Theo chế độ chọn):</label><input type='text' class='currency-input' inputmode='numeric' name='importPrice' placeholder='VD: 7.000 hoặc tổng 70.000' required></div>");

        sb.append("<div><label>Giá bán lẻ (đ):</label><input type='text' class='currency-input' inputmode='numeric' name='sellPrice' placeholder='VD: 10.000' required></div>");
        sb.append("<div><label>Giá thùng / sỉ (đ) (Bỏ trống nếu không có):</label><input type='text' class='currency-input' inputmode='numeric' name='wholesalePrice' placeholder='Để 0 nếu bán lẻ' value='0'></div>");
        sb.append("<div><label>Quy cách:</label><input type='text' name='unitNote' placeholder='VD: Thùng 0 lon' value='Bán lẻ'></div>");
        sb.append("<div><label>Số lượng tồn ban đầu:</label><input type='number' name='quantity' value='10' required></div>");
        sb.append("<div><label>Báo sắp hết khi tồn &le;:</label><input type='number' name='minStock' value='5' required></div>");
        sb.append("<div id='expiryDateBox'><label>Hạn sử dụng lô này:</label><input type='date' name='expiryDate' value='").append(LocalDate.now().plusMonths(6)).append("'></div>");
        sb.append("<div id='categoryBox' style='display:none;'><label>Ngành hàng:</label><input type='text' name='category' value='Gia dụng'></div>");
        sb.append("</div>");
        sb.append("<div style='margin-top:16px;'><button type='submit' class='btn btn-primary' onclick=\"return confirm('Xác nhận thêm mặt hàng mới vào kho?');\">➕ Lưu Mặt Hàng Mới</button></div>");
        sb.append("</form></div>");

        // Quản lý Thêm / Đổi tên / Xóa Kệ
        sb.append("<div class='card'>");
        sb.append("<h3 class='card-header'>🏷️ Quản Lý Kệ Hàng (Thêm, Đổi tên, Xóa)</h3>");

        // Form tạo kệ mới
        sb.append("<form method='POST' action='/shelf/add' style='margin-bottom:15px;'>");
        sb.append("<label><strong>1. Thêm kệ mới:</strong></label>");
        sb.append("<div style='display:flex; gap:10px; margin-top:6px;'>");
        sb.append("<input type='text' name='shelfName' placeholder='VD: Tủ lạnh' required>");
        sb.append("<button type='submit' class='btn btn-primary'>Thêm Kệ</button>");
        sb.append("</div></form>");

        // Form đổi tên kệ
        sb.append("<form method='POST' action='/shelf/rename' style='margin-bottom:15px;'>");
        sb.append("<label><strong>2. Đổi tên kệ:</strong></label>");
        sb.append("<div style='display:grid; grid-template-columns: 1fr 1fr auto; gap:10px; margin-top:6px;'>");
        sb.append("<select name='oldName'>");
        for (String s : shelves) sb.append("<option value='").append(escapeHtml(s)).append("'>").append(escapeHtml(s)).append("</option>");
        sb.append("</select>");
        sb.append("<input type='text' name='newName' placeholder='Tên mới' required>");
        sb.append("<button type='submit' class='btn btn-sand'>Đổi tên</button>");
        sb.append("</div></form>");

        // Form xóa kệ trống
        sb.append("<form method='POST' action='/shelf/delete' onsubmit=\"return confirm('Bạn có chắc muốn xóa kệ này?');\">");
        sb.append("<label><strong>3. Xóa kệ trống:</strong></label>");
        sb.append("<div style='display:flex; gap:10px; margin-top:6px;'>");
        sb.append("<select name='shelfName'>");
        for (String s : shelves) sb.append("<option value='").append(escapeHtml(s)).append("'>").append(escapeHtml(s)).append("</option>");
        sb.append("</select>");
        sb.append("<button type='submit' class='btn btn-danger'>Xóa Kệ</button>");
        sb.append("</div></form>");

        sb.append("</div>");

        sb.append("</div>");
        return sb.toString();
    }

    private String renderEditModal() {
        return """
        <div id="editModal" class="modal">
            <div class="modal-content">
                <div style="display:flex; justify-content:space-between; align-items:center; border-bottom:2px solid var(--sand-gold); padding-bottom:8px; margin-bottom:14px;">
                    <h3 style="margin:0; color:var(--plum-dark);">✏️ Chỉnh Sửa Thông Tin Mặt Hàng</h3>
                    <button type="button" class="btn btn-outline" onclick="closeEditModal()" style="padding:4px 10px;">✖ Đóng</button>
                </div>
                <form method="POST" action="/product/edit">
                    <input type="hidden" id="editId" name="id">
                    <div class="form-grid">
                        <div style="grid-column: 1 / -1;"><label>Tên mặt hàng:</label><input type="text" id="editName" name="name" required></div>
                        <div><label>Giá vốn nhập (đ):</label><input type="text" class="currency-input" inputmode="numeric" id="editImportPrice" name="importPrice" required></div>
                        <div><label>Giá bán lẻ (đ):</label><input type="text" class="currency-input" inputmode="numeric" id="editSellPrice" name="sellPrice" required></div>
                        <div><label>Giá sỉ/thùng (đ):</label><input type="text" class="currency-input" inputmode="numeric" id="editWholesalePrice" name="wholesalePrice" required></div>
                        <div><label>Quy cách:</label><input type="text" id="editUnitNote" name="unitNote"></div>
                        <div><label>Tồn kho:</label><input type="number" id="editQuantity" name="quantity" required></div>
                        <div><label>Vị trí kệ:</label><input type="text" id="editLocation" name="location" required></div>
                        <div><label>Mức cảnh báo tồn:</label><input type="number" id="editMinStock" name="minStock" required></div>
                        <div id="editExpiryBox"><label>Hạn sử dụng gần nhất:</label><input type="date" id="editExpiryDate" name="expiryDate"></div>
                    </div>
                    <div style="margin-top:18px; display:flex; gap:10px;">
                        <button type="submit" class="btn btn-primary" style="flex:1;" onclick="return confirm('Xác nhận lưu thay đổi thông tin mặt hàng?');">💾 Lưu Thay Đổi</button>
                        <button type="button" class="btn btn-sand" onclick="closeEditModal()">Hủy</button>
                    </div>
                </form>
            </div>
        </div>
        """;
    }

    private String renderKiemKeTab(String selectedShelf, List<String> shelves) {
        StringBuilder sb = new StringBuilder();
        List<Product> shelfProducts = productService.findByLocation(selectedShelf);

        sb.append("<div class='card'>");
        sb.append("<h2 class='card-header'><span>📋 Kiểm Đếm Kệ Thực Tế & Nhập Hàng Mới (FIFO)</span></h2>");

        sb.append("<div class='shelf-bar'>");
        sb.append("<a class='shelf-chip ").append("ALL".equals(selectedShelf) ? "active" : "").append("' href='/?tab=kiem-ke&shelf=ALL'>Tất cả kệ</a>");
        for (String s : shelves) {
            String encoded = URLEncoder.encode(s, StandardCharsets.UTF_8);
            sb.append("<a class='shelf-chip ").append(s.equalsIgnoreCase(selectedShelf) ? "active" : "")
                    .append("' href='/?tab=kiem-ke&shelf=").append(encoded).append("'>📍 ").append(escapeHtml(s)).append("</a>");
        }
        sb.append("</div>");

        sb.append("<table><thead><tr>")
                .append("<th>Mã & Tên mặt hàng</th><th>Kệ</th><th>Tồn sổ sách</th>")
                .append("<th>1. Chốt số đếm thực tế</th>")
                .append("<th>2. Nhập thêm hàng (Có chọn lô HSD)</th>")
                .append("</tr></thead><tbody>");

        for (Product p : shelfProducts) {
            sb.append("<tr>");
            sb.append("<td><strong>#").append(p.getID()).append(" - ").append(escapeHtml(p.getName())).append("</strong><br>")
                    .append("<small style='color:#666;'>Đã bán: ").append(p.getTotalSold()).append("</small></td>");
            sb.append("<td><span class='badge badge-shelf'>").append(escapeHtml(p.getLocation())).append("</span></td>");
            sb.append("<td style='font-size:20px; font-weight:800; color:var(--plum-dark);'>").append(p.getQuantity()).append("</td>");

            // Form Chốt số đếm
            sb.append("<td><form method='POST' action='/stock/audit' style='display:flex; gap:6px; align-items:center;' ")
                    .append("onsubmit=\"return confirm('Xác nhận chốt số đếm thực tế cho [" + escapeHtml(p.getName()) + "]?');\">")
                    .append("<input type='hidden' name='id' value='").append(p.getID()).append("'>")
                    .append("<input type='hidden' name='shelf' value='").append(escapeHtml(selectedShelf)).append("'>")
                    .append("<input type='number' name='actualCount' value='").append(p.getQuantity()).append("' min='0' style='width:85px; font-weight:700;'>")
                    .append("<input type='text' name='note' placeholder='Ghi chú' style='width:110px;'>")
                    .append("<button type='submit' class='btn btn-sand'>Chốt số</button></form></td>");

            // Form Nhập thêm hàng (Có thêm ngày HSD cho lô mới)
            sb.append("<td><form method='POST' action='/stock/restock' style='display:grid; grid-template-columns: 80px 100px 130px auto; gap:6px; align-items:center;' ")
                    .append("onsubmit=\"return confirm('Xác nhận nhập thêm hàng mới cho [" + escapeHtml(p.getName()) + "]?');\">")
                    .append("<input type='hidden' name='id' value='").append(p.getID()).append("'>")
                    .append("<input type='hidden' name='shelf' value='").append(escapeHtml(selectedShelf)).append("'>")
                    .append("<input type='number' name='addedQty' placeholder='+SL' min='1' style='width:80px;' required>")
                    .append("<input type='text' class='currency-input' inputmode='numeric' name='importPrice' value='").append((long) p.getImportPrice()).append("' title='Giá vốn' style='width:95px;'>");

            if (p instanceof ExpiringProduct) {
                sb.append("<input type='date' name='expiryDate' value='").append(LocalDate.now().plusMonths(6)).append("' title='HSD lô mới' style='width:130px;'>");
            } else {
                sb.append("<input type='hidden' name='expiryDate' value=''>");
            }
            sb.append("<button type='submit' class='btn btn-primary'>+ Nhập kho</button></form></td>");

            sb.append("</tr>");
        }
        sb.append("</tbody></table></div>");
        return sb.toString();
    }

    private String renderCanhBaoTab(List<Product> lowStock, List<ExpiringProduct> expiringSoon,
                                    List<Product> slowMoving, List<Product> topSelling) {
        StringBuilder sb = new StringBuilder();
        sb.append("<div class='two-col'>");

        sb.append("<div class='card'>");
        sb.append("<h3 class='card-header'>🚨 Sổ Hàng Sắp Hết (Cần gọi đại lý)</h3>");
        sb.append("<table><thead><tr><th>Mặt hàng</th><th>Kệ</th><th>Tồn / Mức báo</th></tr></thead><tbody>");
        for (Product p : lowStock) {
            sb.append("<tr><td><strong>").append(escapeHtml(p.getName())).append("</strong></td>")
                    .append("<td>").append(escapeHtml(p.getLocation())).append("</td>")
                    .append("<td style='color:var(--danger);font-weight:800;'>").append(p.getQuantity()).append(" / ").append(p.getMinStock()).append("</td></tr>");
        }
        sb.append("</tbody></table></div>");

        sb.append("<div class='card'>");
        sb.append("<h3 class='card-header'>⏰ Hàng Sắp Hoặc Đã Hết Hạn (14 ngày tới)</h3>");
        sb.append("<table><thead><tr><th>Mặt hàng</th><th>Kệ</th><th>Hạn sử dụng gần nhất</th></tr></thead><tbody>");
        for (ExpiringProduct ep : expiringSoon) {
            sb.append("<tr><td><strong>").append(escapeHtml(ep.getName())).append("</strong></td>")
                    .append("<td>").append(escapeHtml(ep.getLocation())).append("</td>")
                    .append("<td style='color:var(--danger);font-weight:800;'>").append(ep.getNearestExpiryDate()).append("</td></tr>");
        }
        sb.append("</tbody></table></div>");

        sb.append("<div class='card'>");
        sb.append("<h3 class='card-header'>🐢 Hàng Ế (Tồn kho &ge; 2 tháng)</h3>");
        sb.append("<table><thead><tr><th>Mặt hàng</th><th>Tồn</th><th>Số ngày đứng hàng</th></tr></thead><tbody>");
        for (Product p : slowMoving) {
            sb.append("<tr><td><strong>").append(escapeHtml(p.getName())).append("</strong></td>")
                    .append("<td>").append(p.getQuantity()).append("</td>")
                    .append("<td><span class='badge badge-slow'>").append(p.getDaysSinceLastActive()).append(" ngày</span></td></tr>");
        }
        sb.append("</tbody></table></div>");

        sb.append("<div class='card'>");
        sb.append("<h3 class='card-header'>🔥 Top Mặt Hàng Bán Chạy Nhất</h3>");
        sb.append("<table><thead><tr><th>Hạng & Tên hàng</th><th>Tổng lượng bán</th></tr></thead><tbody>");
        int rank = 1;
        for (Product p : topSelling) {
            sb.append("<tr><td><strong>#").append(rank++).append(". ").append(escapeHtml(p.getName())).append("</strong></td>")
                    .append("<td style='color:var(--plum-main);font-weight:900;'>").append(p.getTotalSold()).append("</td></tr>");
        }
        sb.append("</tbody></table></div>");

        sb.append("</div>");
        return sb.toString();
    }

    private String renderSoNoTab() {
        StringBuilder sb = new StringBuilder();
        sb.append("<div class='card'>");
        sb.append("<h2 class='card-header'><span>📓 Sổ Nợ Khách Mua Chịu</span>")
                .append("<span>Tổng nợ chưa thu: <strong style='color:var(--danger);'>")
                .append(formatMoney(ledgerService.getTotalUnpaidDebt())).append(" đ</strong></span></h2>");

        sb.append("<datalist id='debtorSuggestions'>");
        for (String name : ledgerService.getDebtorSuggestions()) {
            sb.append("<option value='").append(escapeHtml(name)).append("'></option>");
        }
        sb.append("</datalist>");
        sb.append("<form method='POST' action='/debt/add'>");
        sb.append("<div class='form-grid'>");
        sb.append("<div><label>Tên khách hàng:</label><input type='text' name='customerName' list='debtorSuggestions' placeholder='VD: Khách A' autocomplete='on' required></div>");
        sb.append("<div><label>SĐT hoặc Địa chỉ:</label><input type='text' name='phoneOrAddress' placeholder='VD: Nhà số 0'></div>");
        sb.append("<div><label>Số tiền nợ (đ):</label><input type='text' class='currency-input' inputmode='numeric' name='amount' placeholder='VD: 125.000' required></div>");
        sb.append("<div style='grid-column: 1 / -1;'><label>Chi tiết mặt hàng (Ghi chú):</label><input type='text' name='itemsNote' placeholder='VD: 1 thùng bia, 2 chai dầu ăn' required></div>");
        sb.append("</div>");
        sb.append("<div style='margin-top:14px;'><button type='submit' class='btn btn-primary' onclick=\"return confirm('Xác nhận ghi sổ nợ mới?');\">✍️ Ghi Vào Sổ Nợ</button></div>");
        sb.append("</form></div>");

        sb.append("<div class='debt-view-tabs'>")
                .append("<button type='button' class='tab-btn active' onclick=\"switchDebtorView('debtTransactionsView', this)\">Danh Sách Lượt Nợ</button>")
                .append("<button type='button' class='tab-btn' onclick=\"switchDebtorView('debtorsGroupedView', this)\">Danh Sách Người Nợ</button>")
                .append("</div>");

        sb.append("<div id='debtTransactionsView' class='debt-view-panel active'>");
        sb.append("<div class='card'>");
        sb.append("<h3 class='card-header'>Danh Sách Khoản Nợ Chưa Trả</h3>");
        sb.append("<table><thead><tr><th>Ngày giờ</th><th>Khách hàng</th><th>Mặt hàng lấy chịu</th><th>Số tiền nợ</th><th>Thao tác</th></tr></thead><tbody>");
        for (DebtRecord d : ledgerService.getAllDebts()) {
            sb.append("<tr>");
            sb.append("<td><strong>").append(d.getFormattedDate()).append("</strong></td>");
            sb.append("<td><strong>").append(escapeHtml(d.getCustomerName())).append("</strong><br><small>").append(escapeHtml(d.getPhoneOrAddress())).append("</small></td>");
            sb.append("<td>").append(escapeHtml(d.getItemsNote())).append("</td>");
            sb.append("<td style='font-size:20px; font-weight:800; color:var(--danger);'>").append(formatMoney(d.getAmount())).append(" đ</td>");
            sb.append("<td style='display:flex; gap:6px;'>");
            sb.append("<button type='button' class='btn btn-outline' onclick=\"openEditDebtModal(").append(d.getId()).append(", '").append(escapeHtml(d.getCustomerName())).append("', '").append(escapeHtml(d.getPhoneOrAddress())).append("', '").append(escapeHtml(d.getItemsNote())).append("', ").append(d.getAmount()).append(")\">✏️ Sửa</button>");
            sb.append("<form method='POST' action='/debt/pay' style='margin:0;' onsubmit=\"return confirm('Xác nhận khách [" + escapeHtml(d.getCustomerName()) + "] đã trả hết nợ?');\">")
                    .append("<input type='hidden' name='debtId' value='").append(d.getId()).append("'>")
                    .append("<button type='submit' class='btn btn-success'>✔️ Đã trả</button></form>");
            sb.append("</td></tr>");
        }
        sb.append("</tbody></table></div>");
        sb.append("</div>");

        sb.append("<div id='debtorsGroupedView' class='debt-view-panel'>");
        sb.append("<div class='card'>");
        sb.append("<h3 class='card-header'>Danh Sách Người Nợ</h3>");
        sb.append("<table><thead><tr><th>Khách hàng</th><th>SĐT / Địa chỉ</th><th>Tổng đang nợ</th><th>Chi tiết</th></tr></thead><tbody>");
        for (LedgerService.DebtorSummary debtor : ledgerService.getUnpaidDebtorSummaries()) {
            String detailsId = "debtor-details-" + debtor.debts().get(0).getId();
            sb.append("<tr>")
                    .append("<td><a href='#").append(detailsId).append("' onclick=\"document.getElementById('")
                    .append(detailsId).append("').open = true;\"><strong>")
                    .append(escapeHtml(debtor.customerName())).append("</strong></a></td>")
                    .append("<td>").append(escapeHtml(debtor.phoneOrAddress())).append("</td>")
                    .append("<td style='font-size:19px;font-weight:800;color:var(--danger);'>")
                    .append(formatMoney(debtor.totalDebt())).append(" đ</td>")
                    .append("<td>").append(debtor.debts().size()).append(" lượt</td></tr>");
            sb.append("<tr><td colspan='4' style='padding:0;'>")
                    .append("<details id='").append(detailsId).append("'><summary style='padding:10px 14px;cursor:pointer;color:var(--plum-main);'>Xem chi tiết các khoản nợ</summary>")
                    .append("<table style='margin:0;'><thead><tr><th>Ngày giờ</th><th>Chi tiết mặt hàng</th><th>Số tiền</th></tr></thead><tbody>");
            for (DebtRecord debt : debtor.debts()) {
                sb.append("<tr><td>").append(escapeHtml(debt.getFormattedDate())).append("</td>")
                        .append("<td>").append(escapeHtml(debt.getItemsNote())).append("</td>")
                        .append("<td>").append(formatMoney(debt.getAmount())).append(" đ</td></tr>");
            }
            sb.append("</tbody></table></details></td></tr>");
        }
        sb.append("</tbody></table></div>");
        sb.append("</div>");

        // Thùng rác nợ gần đây (Khôi phục nếu nhầm)
        sb.append("<div class='card'>");
        sb.append("<h3 class='card-header' style='color:#777;'>🗑️ Thùng Rác Nợ Gần Đây (Chứa 20 mục đã trả/xóa để khôi phục nếu nhầm)</h3>");
        sb.append("<table><thead><tr><th>Ngày giờ</th><th>Khách hàng</th><th>Ghi chú</th><th>Số tiền</th><th>Khôi phục</th></tr></thead><tbody>");
        for (DebtRecord d : ledgerService.getDeletedDebtTrash()) {
            sb.append("<tr style='opacity:0.7;'>");
            sb.append("<td>").append(d.getFormattedDate()).append("</td>");
            sb.append("<td><strong>").append(escapeHtml(d.getCustomerName())).append("</strong></td>");
            sb.append("<td>").append(escapeHtml(d.getItemsNote())).append("</td>");
            sb.append("<td style='font-weight:700;'>").append(formatMoney(d.getAmount())).append(" đ</td>");
            sb.append("<td><form method='POST' action='/debt/restore' style='margin:0;' onsubmit=\"return confirm('Khôi phục khoản nợ này về sổ chính?');\">")
                    .append("<input type='hidden' name='debtId' value='").append(d.getId()).append("'>")
                    .append("<button type='submit' class='btn btn-sand'>🔄 Khôi phục</button></form></td>");
            sb.append("</tr>");
        }
        sb.append("</tbody></table></div>");

        return sb.toString();
    }

    private String renderEditDebtModal() {
        return """
        <div id="editDebtModal" class="modal">
            <div class="modal-content">
                <div style="display:flex; justify-content:space-between; align-items:center; border-bottom:2px solid var(--sand-gold); padding-bottom:8px; margin-bottom:14px;">
                    <h3 style="margin:0; color:var(--plum-dark);">✏️ Chỉnh Sửa Khoản Nợ Khách Hàng</h3>
                    <button type="button" class="btn btn-outline" onclick="closeEditDebtModal()" style="padding:4px 10px;">✖ Đóng</button>
                </div>
                <form method="POST" action="/debt/edit">
                    <input type="hidden" id="editDebtId" name="debtId">
                    <div class="form-grid">
                        <div><label>Tên khách:</label><input type="text" id="editCustomerName" name="customerName" required></div>
                        <div><label>SĐT / Địa chỉ:</label><input type="text" id="editPhoneOrAddress" name="phoneOrAddress"></div>
                        <div><label>Số tiền nợ (đ):</label><input type="text" class="currency-input" inputmode="numeric" id="editDebtAmount" name="amount" required></div>
                        <div style="grid-column: 1 / -1;"><label>Chi tiết mặt hàng:</label><input type="text" id="editItemsNote" name="itemsNote" required></div>
                    </div>
                    <div style="margin-top:18px; display:flex; gap:10px;">
                        <button type="submit" class="btn btn-primary" style="flex:1;" onclick="return confirm('Xác nhận lưu thay đổi khoản nợ?');">💾 Lưu Thay Đổi</button>
                        <button type="button" class="btn btn-sand" onclick="closeEditDebtModal()">Hủy</button>
                    </div>
                </form>
            </div>
        </div>
        """;
    }

    private String renderSoCuoiNgayTab() {
        StringBuilder sb = new StringBuilder();
        sb.append("<div class='card'>");
        sb.append("<h2 class='card-header'>💰 Sổ Cộng Tiền Cuối Ngày & Tổng Kết Doanh Thu</h2>");
        sb.append("<form method='POST' action='/closing/add'>");
        sb.append("<div class='form-grid'>");
        sb.append("<div><label>Ngày chốt:</label><input type='date' name='date' value='").append(LocalDate.now()).append("' required></div>");
        sb.append("<div><label>Tiền mặt ngăn kéo (đ):</label><input type='text' class='currency-input' inputmode='numeric' name='cashAmount' required></div>");
        sb.append("<div><label>Chuyển khoản (đ):</label><input type='text' class='currency-input' inputmode='numeric' name='transferAmount' value='0'></div>");
        sb.append("<div><label>Chi trả nhập hàng (đ):</label><input type='text' class='currency-input' inputmode='numeric' name='importExpense' value='0'></div>");
        sb.append("<div style='grid-column: 1 / -1;'><label>Ghi chú:</label><input type='text' name='note' placeholder='VD: Trả tiền đại lý ABC'></div>");
        sb.append("</div>");
        sb.append("<div style='margin-top:14px;'><button type='submit' class='btn btn-primary' onclick=\"return confirm('Xác nhận lưu sổ chốt tiền ngày?');\">💾 Lưu Sổ Cuối Ngày</button></div>");
        sb.append("</form></div>");

        // Bảng tổng kết lịch sử & Doanh thu theo ngày/tháng/năm
        sb.append("<div class='card'>");
        sb.append("<h3 class='card-header'>Lịch Sử Chốt Sổ & Tổng Kết Doanh Thu</h3>");
        sb.append("<table><thead><tr><th>Ngày</th><th>Tiền mặt</th><th>Chuyển khoản</th><th>Tổng doanh thu</th><th>Chi nhập</th><th>Thực thu (Lãi/Lỗ)</th><th>Ghi chú</th></tr></thead><tbody>");

        double totalRevAll = 0;
        double totalNetAll = 0;
        for (DailyClosing c : ledgerService.getAllDailyClosings()) {
            double net = c.getNetCashFlow();
            totalRevAll += c.getTotalRevenue();
            totalNetAll += net;
            boolean isNegative = net < 0;

            sb.append("<tr>");
            sb.append("<td><strong>").append(c.getFormattedDate()).append("</strong></td>");
            sb.append("<td>").append(formatMoney(c.getCashAmount())).append(" đ</td>");
            sb.append("<td>").append(formatMoney(c.getTransferAmount())).append(" đ</td>");
            sb.append("<td style='font-weight:800; color:var(--plum-main);'>").append(formatMoney(c.getTotalRevenue())).append(" đ</td>");
            sb.append("<td style='color:var(--danger);'>-").append(formatMoney(c.getImportExpense())).append(" đ</td>");
            sb.append("<td style='font-size:19px; font-weight:900; color:").append(isNegative ? "var(--danger)" : "var(--success)").append(";'>")
                    .append(isNegative ? "⚠️ -" : "+").append(formatMoney(Math.abs(net))).append(" đ</td>");
            sb.append("<td>").append(escapeHtml(c.getNote())).append("</td>");
            sb.append("</tr>");
        }
        sb.append("</tbody></table>");
        sb.append("<div style='margin-top:15px; padding:12px; background:var(--pink-soft); border-radius:10px; font-weight:700;'>")
                .append("📊 Tổng doanh thu tất cả các ngày đã chốt: <span style='color:var(--plum-main); font-size:20px;'>").append(formatMoney(totalRevAll)).append(" đ</span> | ")
                .append("Thực thu ròng: <span style='color:var(--success); font-size:20px;'>").append(formatMoney(totalNetAll)).append(" đ</span></div>");
        sb.append("</div>");

        return sb.toString();
    }

    private void redirect(HttpExchange exchange, String tab, String shelf, String msg) throws IOException {
        String location = "/?tab=" + URLEncoder.encode(tab, StandardCharsets.UTF_8)
                + "&shelf=" + URLEncoder.encode(shelf, StandardCharsets.UTF_8)
                + "&msg=" + URLEncoder.encode(msg, StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Location", location);
        exchange.sendResponseHeaders(303, -1);
        exchange.close();
    }

    private Map<String, String> parseForm(HttpExchange exchange) throws IOException {
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        return parseUrlEncoded(body);
    }

    private Map<String, String> parseUrlEncoded(String raw) {
        Map<String, String> map = new HashMap<>();
        if (raw == null || raw.isBlank()) return map;
        for (String pair : raw.split("&")) {
            int idx = pair.indexOf('=');
            if (idx > 0) {
                String k = URLDecoder.decode(pair.substring(0, idx), StandardCharsets.UTF_8);
                String v = URLDecoder.decode(pair.substring(idx + 1), StandardCharsets.UTF_8);
                map.put(k, v);
            }
        }
        return map;
    }

    private int parseIntOrDefault(String val, int def) {
        try { return Integer.parseInt(val.trim()); } catch (Exception e) { return def; }
    }

    private double parseDoubleOrDefault(String val, double def) {
        try { return Double.parseDouble(val.trim()); } catch (Exception e) { return def; }
    }

    private String formatMoney(double amount) {
        return String.format("%,.0f", amount);
    }

    private String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&#39;");
    }
}