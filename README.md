# Spotify Demo

Ứng dụng quản lý thư viện nhạc với React, Java Spring Boot và MySQL. Người dùng có thể tìm kiếm và nghe nhạc mẫu, tạo playlist, lưu bài yêu thích, theo dõi nghệ sĩ và xem lịch sử nghe. Quản trị viên quản lý nghệ sĩ, album, thể loại và bài hát.

## Mã nguồn

| Thư mục | Nội dung |
|---|---|
| frontend | Giao diện React/Vite và trình phát HTML5 audio |
| backend | API Java Spring Boot, đăng nhập, session và phân quyền |
| database | 10 bảng, dữ liệu mẫu, 12 truy vấn, view, trigger và procedure |
| demo-audio | Ba file WAV mẫu dài 12 giây và giấy phép |

## Chạy đầy đủ bằng Docker

Cài và mở Docker Desktop trên Windows/macOS hoặc Docker Engine cùng Compose v2 trên Linux. Từ thư mục repository, chạy:

```sh
docker compose up -d --build --wait --wait-timeout 300
```

Mở http://127.0.0.1:15173/. Lần đầu cần Internet để tải các image và thư viện. Docker tự biên dịch React, đóng gói Java, khởi tạo MySQL và đợi CSDL sẵn sàng. MySQL dùng volume để giữ dữ liệu giữa các lần chạy.

Để dừng và giữ dữ liệu:

```sh
docker compose down
```

Không thêm `-v` nếu cần giữ tài khoản mới, playlist và lịch sử đã tạo. Nếu cổng 15173 đang được dùng, sao chép .env.example thành .env rồi đổi APP_PORT thành 18080.

## Tài khoản mẫu

| Vai trò | Email | Mật khẩu |
|---|---|---|
| Người dùng | user2@demo.local | Demo@1313 |
| Quản trị | admin@demo.local | Demo@1313 |

Tài khoản mới được lưu trong bảng NguoiDung, mật khẩu được băm bằng bcrypt. Tài khoản demo này độc lập với dịch vụ Spotify thật. Dữ liệu và âm thanh trong repository là mẫu cho ứng dụng.

## Triển khai website để xem từ xa

Repository chứa toàn bộ sản phẩm, nhưng việc đưa mã lên GitHub không tự tạo máy chủ Java hoặc MySQL. GitHub Pages chỉ phục vụ HTML/CSS/JavaScript, vì vậy không đủ để chạy toàn bộ ứng dụng này.

Để có một link dùng đầy đủ chức năng, triển khai Dockerfile lên máy chủ hỗ trợ container và kết nối MySQL. Giao diện đã được đóng gói cùng backend nên không cần tách thành hai website. Cấu hình các biến môi trường:

| Biến | Giá trị |
|---|---|
| PORT | Cổng do dịch vụ hosting cấp; image mặc định 8080 |
| DB_URL | JDBC URL của MySQL, gồm tên schema và tùy chọn SSL theo nhà cung cấp |
| DB_USER | Tài khoản ứng dụng trên MySQL |
| DB_PASSWORD | Mật khẩu DB do bạn cấu hình, không commit vào Git |
| APP_ALLOWED_ORIGINS | URL HTTPS chính xác của website, ví dụ https://your-site.example; nhiều URL ngăn bằng dấu phẩy |

Chạy lần lượt database/01_schema.sql, 02_seed.sql, 04_views_indexes_procedures.sql trên schema mới. Tạo tài khoản ứng dụng có quyền SELECT, INSERT, UPDATE, DELETE, EXECUTE. File 99_app_user.sql chỉ dùng cho Docker demo cục bộ với mật khẩu mẫu; cấu hình tài khoản phù hợp cho môi trường hosting. Không chạy lại seed trên dữ liệu cần giữ.

Không đưa mật khẩu DB thật, thư mục dữ liệu MySQL hoặc file .env lên GitHub. Tài khoản mẫu được công khai để phục vụ demo; không sử dụng dữ liệu cá nhân hoặc dữ liệu thật trên bản demo công khai.

## Tài liệu kỹ thuật

- frontend/src/main.jsx: các màn hình và lời gọi API.
- backend/src/main/java/vn/ptit/spotify/AuthController.java: đăng ký, đăng nhập và session.
- backend/src/main/java/vn/ptit/spotify/MusicController.java: playlist, yêu thích, theo dõi và lịch sử nghe.
- backend/src/main/java/vn/ptit/spotify/AdminController.java: quản trị danh mục.
- database/01_schema.sql: bảng và các ràng buộc dữ liệu.
- database/03_queries.sql: truy vấn nghiệp vụ.
- database/04_views_indexes_procedures.sql: view, chỉ mục và stored procedure.

GitHub Pages: https://docs.github.com/en/pages/getting-started-with-github-pages/what-is-github-pages
