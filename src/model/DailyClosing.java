package model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class DailyClosing {
    private int id;
    private LocalDate date;
    private double cashAmount;       // Tiền mặt đếm trong ngăn kéo
    private double transferAmount;   // Tiền khách chuyển khoản
    private double importExpense;    // Tiền chi trả đại lý giao hàng trong ngày
    private String note;

    public DailyClosing(int id, LocalDate date, double cashAmount, double transferAmount, double importExpense, String note) {
        this.id = id;
        this.date = (date != null) ? date : LocalDate.now();
        this.cashAmount = Math.max(0, cashAmount);
        this.transferAmount = Math.max(0, transferAmount);
        this.importExpense = Math.max(0, importExpense);
        this.note = note;
    }

    public int getId() { return id; }
    public LocalDate getDate() { return date; }
    public double getCashAmount() { return cashAmount; }
    public double getTransferAmount() { return transferAmount; }
    public double getImportExpense() { return importExpense; }
    public String getNote() { return note; }

    // Tổng doanh thu trong ngày (Tiền mặt + Chuyển khoản)
    public double getTotalRevenue() {
        return cashAmount + transferAmount;
    }

    // Thực thu còn lại sau khi trừ tiền nhập hàng
    public double getNetCashFlow() {
        return getTotalRevenue() - importExpense;
    }

    public String getFormattedDate() {
        return date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }
}