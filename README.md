# 🏪 DuTu Goods Manager

[![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://www.oracle.com/java/)
[![IDE](https://img.shields.io/badge/IntelliJ_IDEA-000000?style=for-the-badge&logo=intellij-idea&logoColor=white)](https://www.jetbrains.com/idea/)
[![Git](https://img.shields.io/badge/Git-F05032?style=for-the-badge&logo=git&logoColor=white)](https://git-scm.com/)
[![Status](https://img.shields.io/badge/Status-In_Development-blue?style=for-the-badge)](#)

Hệ thống phần mềm quản lý hàng hóa và tồn kho dành cho cửa hàng tạp hóa / tiện lợi của gia đình, nhằm số hóa quy trình ghi chép sổ sách thủ công.

---

## 📌 Tính năng chính (Dự kiến)

- 📦 **Quản lý sản phẩm**: Thêm, sửa, xóa, tìm kiếm hàng hóa theo danh mục.
- 💰 **Quản lý giá & tồn kho**: Cảnh báo hết hàng, quản lý giá nhập và giá bán lẻ.
- 📊 **Thống kê doanh thu**: Báo cáo xuất/nhập/tồn kho cơ bản.
- 💾 **Lưu trữ dữ liệu**: Đồng bộ dữ liệu ra tệp tin để tránh mất mát khi tắt máy.

---

## 🏗️ Cấu trúc kiến trúc (Package Architecture)

Dự án áp dụng mô hình phân tách logic và hướng đối tượng (OOP):

```text
src/
├── model/        # Các lớp thực thể dữ liệu (Product, Food, Beverage, ...)
├── service/      # Nghiệp vụ xử lý logic và tính toán (chức năng quản lý kho)
└── view/         # Giao diện tương tác người dùng (CLI / Console, mở rộng GUI sau này)
```
## 🛠️ Công nghệ sử dụng
Ngôn ngữ: Java Core (JDK 17+)

Mô hình lập trình: Hướng đối tượng (OOP - Encapsulation, Abstraction, Inheritance, Polymorphism)

Công cụ phát triển: IntelliJ IDEA

Quản lý phiên bản: Git & GitHub

## 🚀 Tiến độ hiện tại
[x] Khởi tạo cấu trúc dự án & thiết lập .gitignore chuẩn.

[x] Kết nối và đồng bộ kho lưu trữ với GitHub.

[x] Hoàn thiện lớp cơ sở trừu tượng Product 

[x] Xây dựng tầng Service xử lý nghiệp vụ quản lý.

[x] Xây dựng menu tương tác người dùng (View).

[ ] Bổ sung các tính năng

[ ] Hiệu chỉnh mã nguồn

[ ] Xây dựng giao diện UI

[ ] Cập nhật và cải thiện tính năng

[ ] Hoàn thiện
