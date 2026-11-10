<div align="center">

# 🏪 DuTu - Hệ Thống Sổ Sách Điện Tử & Tra Giá Tạp Hóa

<img src="DuTu.ico" alt="Biểu tượng DuTu Goods Manager" width="96" />

*Giải pháp phần mềm sổ sách điện tử và tra cứu giá thông minh dành riêng cho cửa hàng tạp hóa / bách hóa gia đình.*

<br />

<!-- Badges đã được sửa cú pháp chuẩn & căn giữa -->
[![Java Version](https://img.shields.io/badge/Java-17%2B-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](#)
[![Architecture](https://img.shields.io/badge/Architecture-3--Tier%20(MVC)-7B2869?style=for-the-badge)](#)
[![UI Theme](https://img.shields.io/badge/UI%20Theme-Plum%20Pink%20%26%20Sand%20Gold-C84B83?style=for-the-badge)](#)
[![Status](https://img.shields.io/badge/Status-Releases%20Ready-1F8A5B?style=for-the-badge)](#)
[![License](https://img.shields.io/badge/License-MIT-4169E1?style=for-the-badge)](#)

</div>

---

### 📖 Giới thiệu tổng quan

**DuTu** là giải pháp phần mềm sổ sách điện tử và tra cứu giá thông minh dành riêng cho cửa hàng tạp hóa / bách hóa gia đình.

Hệ thống được tối ưu hóa cho đối tượng người dùng lớn tuổi (phụ huynh), thay thế phương pháp tính tiền rườm rà từng đơn hàng bằng cơ chế **Kiểm đếm kệ định kỳ (FIFO)** và **Bộ 4 Cuốn Sổ Điện Tử** trực quan.

## 🌸 Điểm Nổi Bật & Triết Lý Thiết Kế

- **Chữ to, màu sắc rõ ràng:** Giao diện Web UI mang bảng màu chủ đạo **Tím Hồng & Vàng Cát** (`#7B2869` & `#E6C280`), nút bấm kích thước lớn, tương thích hoàn hảo trên máy tính và máy tính bảng tại quầy.
- **Tra giá nhanh không dấu:** Thuật toán chuẩn hóa tiếng Việt (`Normalizer`) hỗ trợ gõ không dấu (*"nuoc"*, *"mi"*, *"bong den"*) ra kết quả tức thì.
- **Cơ chế kiểm kê FIFO:** Tự động trừ tồn kho theo lô hạn sử dụng cũ nhất khi đếm kệ cuối ngày/cuối tuần, tự động suy ra lượng hàng đã bán mà không cần tạo hóa đơn bán lẻ rườm rà.
- **Hai chế độ nhập giá vốn:** Hỗ trợ nhập giá vốn chia theo đơn vị lẻ hoặc nhập tổng tiền "nguyên cục/nguyên thùng" (máy tự động chia đều).
- **An toàn dữ liệu:** Tích hợp hộp thoại xác nhận trước các thao tác nguy hiểm (xóa hàng, xóa kệ, trả nợ) cùng thùng rác lưu trữ 20 khoản nợ đã xóa gần đây.
- **Tích hợp sổ nợ khách hàng:** Ghi nhận ngày ghi nợ, tự động cảnh báo khoản chưa thu quá hạn theo ngưỡng có thể tùy chỉnh (mặc định 14 ngày), đồng thời cho phép sửa ngày và khôi phục 20 khoản nợ gần đây.
- **Tích hợp sổ cộng tiền cuối ngày:** Ghi nhận tiền mặt, tiền chuyển khoản và tiền chi trả nhập hàng trong ngày, tự động tính **Thực thu ròng** và tổng kết doanh thu trực quan tích lũy qua tất cả các ngày chốt sổ.
- **Dữ liệu lưu trữ cục bộ:** Tất cả dữ liệu được lưu vào tệp JSON trong thư mục chạy, không cần cài đặt cơ sở dữ liệu rườm rà, dễ dàng sao lưu và di chuyển.
- **Khởi động cùng máy tính:** Có thể bật hoặc tắt trong phần cuối Web UI; hỗ trợ Windows và Linux.
- **Ứng dụng Desktop:** JavaFX WebView hiển thị giao diện trong cửa sổ riêng, hỗ trợ hộp thoại JavaScript và biểu tượng ứng dụng `DuTu.ico`.
---

## 📌 Bộ Tính Năng Chính

### 1. 🔍 Tra Giá Nhanh & Quản Lý Kho Hàng
- Tra cứu song song **Giá bán lẻ** và **Giá bán thùng / bán sỉ** kèm ghi chú quy cách (*"Thùng 24 lon"*).
- Lọc danh sách mặt hàng tức thì theo từng **Vị trí Kệ / Tủ** trưng bày.
- Quản lý linh hoạt: Thêm mới, chỉnh sửa thông tin chi tiết, đổi tên kệ hàng loạt hoặc xóa kệ trống.

### 2. 📋 Đếm Kệ Thực Tế & Nhập Hàng (Quản lý Lô HSD FIFO)
- **Đếm kệ định kỳ:** Chọn kệ đang đứng, gõ số lượng đếm thực tế $\rightarrow$ Hệ thống tự động tính: $\text{Số lượng bán} = \text{Tồn cũ} - \text{Tồn đếm}$.
- **Nhập hàng đại lý:** Cộng dồn số lượng, chọn chế độ nhập giá vốn và ghi nhận ngày hết hạn (HSD) cho từng lô hàng mới.
- **Cơ chế FIFO:** Khi bán hàng, hệ thống tự động trừ tồn kho từ lô HSD cũ nhất, đảm bảo hàng hóa luôn được luân chuyển đúng hạn sử dụng.
### 3. ⚠️ Sổ Cảnh Báo & Thống Kê Bán Chạy / Hàng Ế
- **Sổ Hàng Sắp Hết:** Cảnh báo các mặt hàng chạm hoặc dưới ngưỡng tồn kho tối thiểu (`minStock`).
- **Sổ Cảnh Báo HSD:** Báo đỏ các sản phẩm có HSD lô gần nhất còn dưới 14 ngày hoặc đã hết hạn.
- **Sổ Hàng Ế (Slow-Moving):** Phát hiện các mặt hàng đứng hàng $\ge 60$ ngày không có lượt bán.
- **Top Bán Chạy:** Xếp hạng các mặt hàng đắt khách nhất cửa hàng.

### 4. 📓 Sổ Nợ Khách Hàng (Kèm Thùng Rác Khôi Phục)
- Ghi nhận chi tiết: Tên khách, SĐT/địa chỉ, ngày ghi nợ, số tiền riêng biệt và ghi chú mặt hàng tự do.
- Hỗ trợ chỉnh sửa/cộng dồn khoản nợ trực tiếp nếu ghi nhầm.
- Tự động đánh dấu khoản chưa thu quá hạn khi số ngày từ ngày ghi nợ vượt ngưỡng; ngưỡng mặc định 14 ngày có thể đổi ngay trong tab Sổ Nợ.
- **Thùng rác Nợ:** Lưu trữ 20 khoản nợ đã thanh toán/xóa gần đây, cho phép khôi phục về sổ chính chỉ với 1 cú nhấp chuột.
- **Tự động tính toán:** Hiển thị tổng nợ hiện tại và danh sách khoản quá hạn trong tab Cảnh Báo.

### 5. 💰 Sổ Cộng Tiền Cuối Ngày & Báo Cáo Doanh Thu
- Ghi nhận tiền mặt ngăn kéo, tiền chuyển khoản và tiền chi trả nhập hàng trong ngày.
- Tự động tính **Thực thu ròng**: Hiển thị màu xanh khi có lãi và **màu đỏ rực (`⚠️ -`)** khi chi nhập hàng lớn hơn thu.
- Tổng kết doanh thu trực quan tích lũy qua tất cả các ngày chốt sổ.

---

## 🏗️ Cấu Trúc Kiến Trúc (3-Tier Architecture)

Dự án tuân thủ nghiêm ngặt mô hình phân tách nghiệp vụ và nguyên lý Hướng đối tượng (OOP - Encapsulation, Inheritance, Polymorphism, Abstraction):

```text
src/
├── model/                  # Tầng Thực thể Dữ liệu (Domain Entities)
│   ├── Product.java        # Lớp trừu tượng sản phẩm cơ sở (Giá sỉ, kệ, tồn kho, lô HSD)
│   ├── ExpiringProduct.java# Sản phẩm có Hạn Sử Dụng (Kế thừa từ Product)
│   ├── GeneralProduct.java # Sản phẩm bách hóa chung (Kế thừa từ Product)
│   ├── ExpiryBatch.java    # Lớp quản lý lô HSD phục vụ cơ chế FIFO
│   ├── DebtRecord.java     # Sổ nợ khách hàng (Ngày giờ, chi tiết nợ, trạng thái)
│   ├── StockLog.java       # Lịch sử kiểm kê kệ & nhập hàng
│   └── DailyClosing.java   # Sổ cộng tiền chốt doanh thu cuối ngày
├── service/                # Tầng Xử lý Nghiệp vụ Logic (Business Layer)
│   ├── ProductService.java # Logic quản lý kho, tìm kiếm không dấu, lọc kệ, xử lý FIFO
│   └── LedgerService.java  # Logic quản lý bộ sổ điện tử (Nợ, Thùng rác 20 mục, Chốt sổ)
└── view/                   # Tầng Giao diện Người dùng (Presentation Layer)
    ├── DuTuWebView.java    # Embedded Web UI (Tím Hồng & Vàng Cát, HTTP Server)
    ├── ProductView.java    # Console View (Menu tương tác dòng lệnh)
    └── AppWindow.java      # Đóng gói thành desktop app

```

## 🛠️ Công Nghệ Sử Dụng

- **Ngôn ngữ:** Java Core (JDK 17+)
- **Server Web:** Embedded `com.sun.net.httpserver.HttpServer` (Không dùng framework nặng, khởi động cực nhanh)
- **Giao diện:** JavaFX WebView hiển thị giao diện HTML5, CSS3 Custom Properties và JavaScript ES6 trong cửa sổ Desktop
- **Công cụ phát triển:** IntelliJ IDEA
- **Quản lý phiên bản:** Git & GitHub
- **Lưu trữ dữ liệu:** JSON
- **Xử lý dữ liệu:** Jackson (Library for JSON processing)

---

## 🚀 Hướng Dẫn Khởi Chạy

### 1. Yêu cầu hệ thống
- Máy tính đã cài đặt **JDK 17** hoặc cao hơn và **Maven**.
- Đóng gói app-image Windows cần JDK có `jpackage`; bật autostart trên Linux tạo tệp `.desktop` trong `~/.config/autostart`.
- JavaFX được quản lý tự động qua Maven; không cần cài trình duyệt riêng.

### 2. Các bước khởi chạy
#### Cách 1: Chạy trực tiếp từ IntelliJ IDEA
1. Clone dự án về máy:
   ```bash
   git clone [https://github.com/your-username/dutu-goods-manager.git](https://github.com/your-username/dutu-goods-manager.git)
   cd dutu-goods-manager
   ```

2. Mở thư mục dự án bằng IntelliJ IDEA. Nếu project đang được mở như Java module thông thường, nhấp phải `pom.xml` → **Add as Maven Project**, sau đó chọn **Reload All Maven Projects** trong cửa sổ Maven để tải JavaFX dependencies.

3. Chạy `Main` sau khi Maven sync hoàn tất, hoặc chạy `mvn javafx:run` trong Terminal của IntelliJ. Không chạy `Main.java` trước khi Maven tải xong các dependency.

- Ứng dụng sẽ nạp dữ liệu hiện có từ `dutu_database.json` trong thư mục chạy. Chỉ lần chạy đầu tiên mới tạo dữ liệu mẫu; các thay đổi được tự động lưu vào tệp này. Giao diện mở trong cửa sổ Desktop độc lập:

  `DuTu Goods Manager - Sổ Sách Điện Tử`

### Cách 2: Khởi chạy ứng dụng đã đóng gói (Windows / Linux)

#### 🪟 Trên Windows (Bản Portable)
1. Tải tệp `DuTu-Goods-Manager-Portable-Window-x64-v1.0.9.zip` từ mục **Releases**.
2. **Bắt buộc giải nén** (`Extract All...`) tệp ZIP ra một thư mục cố định trên máy.
3. Click đúp vào file `DuTu Goods Manager.exe` để bắt đầu sử dụng (không cần cài Java).
   > ⚠️ **Lưu ý:** Không kéo riêng file `.exe` ra ngoài thư mục (bắt buộc nằm chung với 2 thư mục `app` và `runtime`). Để đưa biểu tượng ra màn hình chính, click chuột phải vào file `.exe` ➔ **Send to ➔ Desktop (create shortcut)**.

---

#### 🐧 Trên Linux (Ubuntu / Debian / Mint)
* **Cách 1: File cài đặt `.deb` (Khuyên dùng)**
    1. Tải tệp `dutu-goods-manager_1.0.9_amd64.deb`.
    2. Click đúp vào file `.deb` ➔ Chọn **Install** trong App Center (hoặc chạy lệnh: `sudo dpkg -i dutu-goods-manager_1.0.9_amd64.deb && sudo apt install -f`).
    3. Mở phím **Super (Windows)**, gõ `DuTu` và bấm vào ứng dụng để khởi chạy.

* **Cách 2: Bản Portable (`.tar.gz`)**
    1. Tải và giải nén tệp `DuTu-Goods-Manager-Linux-x64-v1.0.9.tar.gz`.
    2. Mở Terminal tại thư mục đã giải nén và chạy lệnh:
       ```bash
       chmod +x "./bin/DuTu Goods Manager"
       ./bin/"DuTu Goods Manager"
       ```

---

#### 💻 Khởi chạy từ Mã nguồn (Developer)
Tại thư mục gốc của dự án, mở Terminal/Command Prompt và chạy lệnh Maven:
```bash
mvn javafx:run
```
#### 📌 Lưu ý chung về Dữ liệu & Hệ thống:
#### 💾 Lưu trữ dữ liệu: 
Dữ liệu quản lý sẽ tự động lưu vào tệp dutu_database.json nằm tại thư mục chạy ứng dụng. Tuyệt đối không xóa tệp này để tránh mất dữ liệu.

#### 🛡️ Cổng kết nối: 
Ứng dụng chạy máy chủ web nội bộ trên cổng 8080. Đảm bảo tường lửa hoặc phần mềm diệt virus không chặn truy cập vào cổng này.


### 3. Đóng gói ứng dụng (Windows & Linux)

#### 🪟 Đóng gói trên Windows (`.exe` Portable)
Chạy script `build_portable_app.bat` ở thư mục gốc dự án để tạo bản portable dạng `app-image` bằng `jpackage`:
* Script tự động lấy tệp JAR và các dependency trong `out/artifacts/DuTu_Goods_Manager_jar/`, gán biểu tượng `DuTu.ico` và xuất kết quả tại `out/artifacts/exe_output_app/`.
* Nếu có Maven trong `PATH`, script sẽ tự biên dịch và sao chép dependency trước khi đóng gói.
* Sau khi hoàn tất, nén thư mục `DuTu Goods Manager` thành tệp `DuTu-Goods-Manager-Portable-Window-x64-v1.0.9.zip` để phát hành.

#### 🐧 Đóng gói trên Linux (WSL2 / Ubuntu)
Yêu cầu hệ thống đã cài đặt **JDK 21**, **OpenJFX** và **fakeroot** (`sudo apt install openjdk-21-jdk openjfx fakeroot -y`).

1. **Tạo gói cài đặt `.deb` (Khuyên dùng cho Ubuntu / Debian / Mint):**
   ```bash
   jpackage --type deb \
     --name "dutu-goods-manager" \
     --app-version "1.0.9" \
     --input "out/artifacts/DuTu_Goods_Manager_jar" \
     --main-jar "DuTu Goods Manager.jar" \
     --main-class Main \
     --icon "DuTu.png" \
     --module-path /usr/share/openjfx/lib \
     --add-modules javafx.controls,javafx.web,javafx.graphics,javafx.media,javafx.base,jdk.httpserver \
     --dest "out/artifacts/linux_output_app"
   ```
   *Kết quả:* Tệp `dutu-goods-manager_1.0.9_amd64.deb` hỗ trợ người dùng click đúp cài đặt trực tiếp vào Menu ứng dụng (Dash).

2. **Tạo gói nén Portable `.tar.gz`:**
   ```bash
   # Đóng gói app-image Linux
   jpackage --type app-image \
     --name "DuTu Goods Manager" \
     --input "out/artifacts/DuTu_Goods_Manager_jar" \
     --main-jar "DuTu Goods Manager.jar" \
     --main-class Main \
     --icon "DuTu.png" \
     --module-path /usr/share/openjfx/lib \
     --add-modules javafx.controls,javafx.web,javafx.graphics,javafx.media,javafx.base,jdk.httpserver \
     --dest "out/artifacts/linux_output_app"

   # Nén thành file tar.gz
   tar -czvf DuTu-Goods-Manager-Linux-x64-v1.0.9.tar.gz -C out/artifacts/linux_output_app "DuTu Goods Manager"
   ```

---

## 📋 Trạng Thái Phát Triển (Roadmap)

- [x] Thiết kế kiến trúc 3 tầng (Model - Service - View) & Hoàn thiện mô hình OOP.
- [x] Xây dựng logic quản lý tồn kho, tìm kiếm tiếng Việt không dấu & lọc theo kệ.
- [x] Xây dựng bộ 4 Cuốn Sổ Điện Tử (Cảnh báo, Đếm kệ/Nhập hàng, Sổ nợ, Sổ cuối ngày).
- [x] Triển khai giao diện Web UI tông Tím Hồng & Vàng Cát với SVG Icon chuẩn thương hiệu.
- [x] Xử lý cơ chế lô HSD theo chuẩn FIFO & Thùng rác khôi phục nợ 20 mục.
- [x] Tích hợp cảnh báo an toàn (`confirm`) & hỗ trợ 2 chế độ nhập giá vốn.
- [x] Cảnh báo nợ quá hạn với ngày ghi nợ có thể chỉnh sửa và ngưỡng cảnh báo lưu trong JSON.
- [x] Hỗ trợ bật/tắt khởi động cùng máy tính trên Windows và Linux.
- [x] Gán biểu tượng `DuTu.ico` (Windows) & `DuTu.png` (Linux) cho cửa sổ JavaFX và bản đóng gói.
- [x] Tích hợp Module lưu trữ dữ liệu tự động ra tệp tin JSON/Text đĩa cục bộ.
- [x] Đóng gói ứng dụng đa nền tảng (Windows Portable `.exe`, Linux `.deb` installer & Linux `.tar.gz` Portable) kèm JRE tích hợp.

---

## 📜 Giấy phép (License)

Dự án được phân phối chính thức theo **Giấy phép MIT (MIT License)**. Bạn được tự do sử dụng, sao chép, chỉnh sửa và phân phối lại cho mục đích cá nhân hoặc thương mại.

Xem chi tiết điều khoản tại tệp [LICENSE](LICENSE).

---

## 🤝 Đóng góp & Phát triển (Contributing)

Mọi ý kiến đóng góp, báo lỗi (Issues) hoặc đề xuất tính năng mới cho **DuTu** luôn được hoan nghênh:

1. **Fork** repository này về tài khoản của bạn.
2. Tạo nhánh tính năng mới (`git checkout -b feature/TinhNangMoi`).
3. **Commit** các thay đổi (`git commit -m 'Thêm tính năng X'`).
4. **Push** lên nhánh của bạn (`git push origin feature/TinhNangMoi`).
5. Mở một **Pull Request** để thảo luận và hợp nhất code.

---

## 💖 Lời cảm ơn (Acknowledgements)

* [Shields.io](https://shields.io/) — Dịch vụ tạo huy hiệu giao diện chuyên nghiệp.
* [OpenJDK / Java](https://www.oracle.com/java/) — Nền tảng phát triển ứng dụng mạnh mẽ.
* Cảm ơn **Tiệm tạp hóa DuTu** đã cung cấp ý tưởng và bài toán thực tế để hoàn thiện phần mềm này.

---

<div align="center">

**DuTu Goods Manager** • Developed with ❤️ for Family Business

⭐ *Nếu bạn thấy dự án này hữu ích, hãy dành tặng 1 Star trên GitHub nhé!* ⭐

</div>
