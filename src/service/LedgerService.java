package service;

import model.DailyClosing;
import model.DebtRecord;
import model.Product;
import model.StockLog;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class LedgerService {
    private final List<DebtRecord> debtBook = new ArrayList<>();
    private final LinkedList<DebtRecord> deletedDebtTrash = new LinkedList<>(); // Thùng rác chứa 20 nợ đã xóa/trả gần đây
    private final List<StockLog> stockLogs = new ArrayList<>();
    private final List<DailyClosing> dailyClosings = new ArrayList<>();

    private int debtIdSeq = 1;
    private int logIdSeq = 1;
    private int closingIdSeq = 1;

    public DebtRecord addDebt(String customerName, String phoneOrAddress, String itemsNote, double amount) {
        DebtRecord record = new DebtRecord(debtIdSeq++, customerName, phoneOrAddress, itemsNote, amount);
        debtBook.add(0, record);
        return record;
    }

    public boolean updateDebt(int debtId, String customerName, String phoneOrAddress, String itemsNote, double amount) {
        for (DebtRecord d : debtBook) {
            if (d.getId() == debtId) {
                d.setCustomerName(customerName);
                d.setPhoneOrAddress(phoneOrAddress);
                d.setItemsNote(itemsNote);
                d.setAmount(amount);
                return true;
            }
        }
        return false;
    }

    public boolean markDebtPaidOrRemove(int debtId) {
        DebtRecord target = null;
        for (DebtRecord d : debtBook) {
            if (d.getId() == debtId) {
                target = d;
                break;
            }
        }
        if (target != null) {
            debtBook.remove(target);
            target.setPaid(true);
            deletedDebtTrash.addFirst(target);
            if (deletedDebtTrash.size() > 20) {
                deletedDebtTrash.removeLast(); // Chỉ giữ 20 mục gần nhất
            }
            return true;
        }
        return false;
    }

    public boolean restoreDebtFromTrash(int debtId) {
        DebtRecord target = null;
        for (DebtRecord d : deletedDebtTrash) {
            if (d.getId() == debtId) {
                target = d;
                break;
            }
        }
        if (target != null) {
            deletedDebtTrash.remove(target);
            target.setPaid(false);
            debtBook.add(0, target);
            return true;
        }
        return false;
    }

    public List<DebtRecord> getAllDebts() { return new ArrayList<>(debtBook); }
    public List<DebtRecord> getDeletedDebtTrash() { return new ArrayList<>(deletedDebtTrash); }

    public void loadDebt(DebtRecord debt) {
        debtBook.add(debt);
        debtIdSeq = Math.max(debtIdSeq, debt.getId() + 1);
    }

    public void loadDeletedDebt(DebtRecord debt) {
        deletedDebtTrash.add(debt);
        debtIdSeq = Math.max(debtIdSeq, debt.getId() + 1);
    }

    public void loadStockLog(StockLog log) {
        stockLogs.add(log);
        logIdSeq = Math.max(logIdSeq, log.getId() + 1);
    }

    public void loadDailyClosing(DailyClosing closing) {
        dailyClosings.add(closing);
        closingIdSeq = Math.max(closingIdSeq, closing.getId() + 1);
    }

    public double getTotalUnpaidDebt() {
        double total = 0;
        for (DebtRecord d : debtBook) {
            if (!d.isPaid()) total += d.getAmount();
        }
        return total;
    }

    public StockLog logRestock(Product p, int oldQty, int addedQty, double importPrice, String note) {
        StockLog log = new StockLog(
                logIdSeq++, p.getID(), p.getName(), "NHAP_HANG",
                oldQty, p.getQuantity(), addedQty, importPrice,
                (note == null || note.isBlank()) ? "Nhập hàng từ đại lý" : note
        );
        stockLogs.add(0, log);
        return log;
    }

    public StockLog logAudit(Product p, int oldQty, int actualQty, int soldQty, String note) {
        StockLog log = new StockLog(
                logIdSeq++, p.getID(), p.getName(), "KIEM_KE",
                oldQty, actualQty, soldQty, p.getSellPrice(),
                (note == null || note.isBlank()) ? "Kiểm đếm kệ: " + p.getLocation() : note
        );
        stockLogs.add(0, log);
        return log;
    }

    public List<StockLog> getAllStockLogs() { return Collections.unmodifiableList(stockLogs); }

    public DailyClosing addDailyClosing(LocalDate date, double cash, double transfer, double expense, String note) {
        DailyClosing closing = new DailyClosing(closingIdSeq++, date, cash, transfer, expense, note);
        dailyClosings.add(0, closing);
        return closing;
    }

    public List<DailyClosing> getAllDailyClosings() { return Collections.unmodifiableList(dailyClosings); }
}