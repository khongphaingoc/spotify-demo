USE spotify_int1313;
-- Q01: Bài của nghệ sĩ 1; nghệ sĩ suy ra qua album.
SELECT b.* FROM BaiHat b JOIN Album a ON a.MaAlbum=b.MaAlbum WHERE a.MaNgheSi=1 ORDER BY b.MaBaiHat;
-- Q02: Tìm bài có tên chứa Bình minh; tìm chứa không tận dụng B-tree prefix.
SELECT * FROM BaiHat WHERE TenBaiHat LIKE '%Bình minh%';
-- Q03: Bài thuộc album 1.
SELECT * FROM BaiHat WHERE MaAlbum=1 ORDER BY MaBaiHat;
-- Q04: Playlist của người dùng 2.
SELECT * FROM Playlist WHERE MaNguoiDung=2 ORDER BY NgayTao DESC;
-- Q05: Bài trong playlist 1 theo vị trí.
SELECT c.ViTri,b.TenBaiHat FROM ChiTietPlaylist c JOIN BaiHat b USING(MaBaiHat) WHERE c.MaPlaylist=1 ORDER BY c.ViTri;
-- Q06: Yêu thích của người dùng 2.
SELECT b.*,y.NgayYeuThich FROM BaiHatYeuThich y JOIN BaiHat b USING(MaBaiHat) WHERE y.MaNguoiDung=2;
-- Q07: Nghệ sĩ người dùng 2 theo dõi.
SELECT n.*,t.NgayTheoDoi FROM TheoDoiNgheSi t JOIN NgheSi n USING(MaNgheSi) WHERE t.MaNguoiDung=2;
-- Q08: Lịch sử của người dùng 2, mới nhất trước.
SELECT l.*,b.TenBaiHat FROM LichSuNghe l JOIN BaiHat b USING(MaBaiHat) WHERE l.MaNguoiDung=2 ORDER BY l.ThoiDiemNghe DESC,l.MaLichSuNghe DESC LIMIT 50;
-- Q09: Top 10 bài, thứ tự phụ ổn định khi bằng lượt.
SELECT MaBaiHat,TenBaiHat,LuotNghe FROM BaiHat ORDER BY LuotNghe DESC,MaBaiHat LIMIT 10;
-- Q10: Top nghệ sĩ; LEFT JOIN giữ nghệ sĩ chưa có lượt.
SELECT n.MaNgheSi,n.TenNgheSi,COALESCE(SUM(b.LuotNghe),0) TongLuot FROM NgheSi n LEFT JOIN Album a USING(MaNgheSi) LEFT JOIN BaiHat b USING(MaAlbum) GROUP BY n.MaNgheSi,n.TenNgheSi ORDER BY TongLuot DESC,n.MaNgheSi LIMIT 10;
-- Q11: Phút nghe theo tháng của người dùng 2.
SELECT DATE_FORMAT(ThoiDiemNghe,'%Y-%m') Thang,ROUND(SUM(ThoiLuongDaNghe)/60,2) SoPhut FROM LichSuNghe WHERE MaNguoiDung=2 GROUP BY Thang ORDER BY Thang;
-- Q12: Lượt nghe theo thể loại.
SELECT t.MaTheLoai,t.TenTheLoai,COALESCE(SUM(b.LuotNghe),0) TongLuot FROM TheLoai t LEFT JOIN BaiHat b USING(MaTheLoai) GROUP BY t.MaTheLoai,t.TenTheLoai ORDER BY TongLuot DESC,t.MaTheLoai;
