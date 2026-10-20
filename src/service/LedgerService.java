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
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class LedgerService {
    public record DebtorSummary(String customerName, String phoneOrAddress, double totalDebt,
                                List<DebtRecord> debts) {}

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

    public List<DebtRecord> getAllDebts() {
        List<DebtRecord> debts = new ArrayList<>(debtBook);
        debts.sort(Comparator.comparing(DebtRecord::getCreatedAt).reversed());
        return debts;
    }
    public List<DebtRecord> getDeletedDebtTrash() { return new ArrayList<>(deletedDebtTrash); }

    public List<String> getDebtorSuggestions() {
        Map<String, Integer> frequencyByName = new LinkedHashMap<>();
        Map<String, LocalDate> mostRecentByName = new LinkedHashMap<>();
        for (DebtRecord debt : allKnownDebts()) {
            String key = debt.getCustomerKey();
            if (key.isBlank()) continue;
            frequencyByName.merge(key, 1, Integer::sum);
            LocalDate date = debt.getCreatedAt().toLocalDate();
            mostRecentByName.merge(key, date, (current, candidate) ->
                    candidate.isAfter(current) ? candidate : current);
        }
        Map<String, String> displayNames = new LinkedHashMap<>();
        for (DebtRecord debt : allKnownDebts()) {
            displayNames.putIfAbsent(debt.getCustomerKey(), debt.getCustomerName().trim());
        }
        List<String> suggestions = new ArrayList<>(displayNames.values());
        suggestions.sort(Comparator
                .comparingInt((String name) -> frequencyByName.getOrDefault(name.toLowerCase(java.util.Locale.ROOT), 0))
                .reversed()
                .thenComparing((String name) -> mostRecentByName.get(name.toLowerCase(java.util.Locale.ROOT)), Comparator.reverseOrder())
                .thenComparing(String.CASE_INSENSITIVE_ORDER));
        return suggestions;
    }

    public List<DebtorSummary> getUnpaidDebtorSummaries() {
        Map<String, List<DebtRecord>> debtsByName = new LinkedHashMap<>();
        for (DebtRecord debt : getAllDebts()) {
            if (!debt.isPaid() && debt.getAmount() > 0) {
                debtsByName.computeIfAbsent(debt.getCustomerKey(), ignored -> new ArrayList<>()).add(debt);
            }
        }
        List<DebtorSummary> summaries = new ArrayList<>();
        for (List<DebtRecord> debts : debtsByName.values()) {
            if (debts.isEmpty()) continue;
            DebtRecord latestDebt = debts.get(0);
            double total = debts.stream().mapToDouble(DebtRecord::getAmount).sum();
            if (total > 0) {
                summaries.add(new DebtorSummary(latestDebt.getCustomerName(),
                        latestDebt.getPhoneOrAddress(), total, List.copyOf(debts)));
            }
        }
        summaries.sort(Comparator.comparingDouble(DebtorSummary::totalDebt).reversed()
                .thenComparing(DebtorSummary::customerName, String.CASE_INSENSITIVE_ORDER));
        return summaries;
    }

    private List<DebtRecord> allKnownDebts() {
        List<DebtRecord> debts = new ArrayList<>(debtBook);
        debts.addAll(deletedDebtTrash);
        return debts;
    }

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