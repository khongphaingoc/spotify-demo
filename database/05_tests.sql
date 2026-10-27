USE spotify_int1313;
-- Trả 0 dòng nếu toàn bộ bộ đếm đồng bộ. Bộ test lỗi kỳ vọng chạy bởi scripts/test_database.py.
SELECT b.MaBaiHat,b.LuotNghe,COUNT(l.MaLichSuNghe) SoBanGhi
FROM BaiHat b LEFT JOIN LichSuNghe l USING(MaBaiHat)
GROUP BY b.MaBaiHat,b.LuotNghe HAVING b.LuotNghe<>COUNT(l.MaLichSuNghe);
SELECT 'NguoiDung' Bang,COUNT(*) SoDong FROM NguoiDung
UNION ALL SELECT 'NgheSi',COUNT(*) FROM NgheSi
UNION ALL SELECT 'Album',COUNT(*) FROM Album
UNION ALL SELECT 'BaiHat',COUNT(*) FROM BaiHat
UNION ALL SELECT 'TheLoai',COUNT(*) FROM TheLoai
UNION ALL SELECT 'Playlist',COUNT(*) FROM Playlist
UNION ALL SELECT 'ChiTietPlaylist',COUNT(*) FROM ChiTietPlaylist
UNION ALL SELECT 'BaiHatYeuThich',COUNT(*) FROM BaiHatYeuThich
UNION ALL SELECT 'TheoDoiNgheSi',COUNT(*) FROM TheoDoiNgheSi
UNION ALL SELECT 'LichSuNghe',COUNT(*) FROM LichSuNghe;
