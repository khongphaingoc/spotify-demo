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

## Triển khai bằng gói miễn phí

[![Deploy to Render](https://render.com/images/deploy-to-render-button.svg)](https://render.com/deploy?repo=https://github.com/khongphaingoc/spotify-demo)

1. Đăng ký/đăng nhập Aiven tại https://console.aiven.io/ và tạo dịch vụ **MySQL – Free**. Chỉ chọn gói Free; không cần mua VPS. Chờ MySQL sẵn sàng.
2. Dùng thông tin kết nối của dịch vụ để import `01_schema.sql`, `02_seed.sql`, `04_views_indexes_procedures.sql` theo đúng thứ tự vào schema mới. Không dùng tài khoản/mật khẩu của MySQL cục bộ cho MySQL trên Aiven.
3. Đăng ký/đăng nhập Render tại https://dashboard.render.com/. Bấm nút Deploy to Render ở trên. File render.yaml đã đặt gói web **Free** và build bằng Docker.
4. Điền DB_URL (JDBC URL theo thông tin kết nối MySQL của bạn, dùng kết nối TLS theo hướng dẫn Aiven), DB_USER và DB_PASSWORD trong cấu hình Render. Không đưa các giá trị riêng này vào mã nguồn hay commit.
5. Triển khai và chờ build. Render cấp URL HTTPS thực tế sau khi tạo dịch vụ; dùng URL đó gửi giảng viên. Ứng dụng tự nhận RENDER_EXTERNAL_URL để cho phép yêu cầu đăng nhập/đăng ký từ chính website này.

Render Free có thể ngủ sau 15 phút không có truy cập và cần khoảng một phút để khởi động lại khi mở. Dữ liệu nằm ở MySQL Aiven, không nằm trên ổ đĩa tạm của Render. Bản này chưa được triển khai online chỉ bằng việc thêm cấu hình; cần hoàn tất tài khoản và kết nối DB trước.

Nguồn: https://render.com/docs/free ; https://render.com/docs/blueprint-spec ; https://aiven.io/docs/products/mysql/concepts/mysql-free-tier .
