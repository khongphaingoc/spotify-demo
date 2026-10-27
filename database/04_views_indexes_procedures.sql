USE spotify_int1313;
CREATE OR REPLACE VIEW vw_BaiHatChiTiet AS
 SELECT b.*,a.TenAlbum,a.MaNgheSi,n.TenNgheSi,t.TenTheLoai
 FROM BaiHat b JOIN Album a USING(MaAlbum) JOIN NgheSi n USING(MaNgheSi) JOIN TheLoai t USING(MaTheLoai);
CREATE OR REPLACE VIEW vw_TopBaiHat AS
 SELECT MaBaiHat,TenBaiHat,LuotNghe FROM BaiHat;
-- View không bảo đảm thứ tự; SELECT view cần ORDER BY và LIMIT.
CREATE INDEX ix_song_name ON BaiHat(TenBaiHat);
CREATE INDEX ix_history_user_time ON LichSuNghe(MaNguoiDung,ThoiDiemNghe DESC,MaLichSuNghe DESC);
DELIMITER $$
CREATE PROCEDURE sp_ThemBaiHatVaoPlaylist(IN p_playlist BIGINT,IN p_song BIGINT,IN p_user BIGINT)
BEGIN
 DECLARE v_owner BIGINT DEFAULT NULL;
 DECLARE v_position INT;
 DECLARE EXIT HANDLER FOR SQLEXCEPTION BEGIN ROLLBACK; RESIGNAL; END;
 START TRANSACTION;
 SELECT MaNguoiDung INTO v_owner FROM Playlist WHERE MaPlaylist=p_playlist FOR UPDATE;
 IF v_owner IS NULL OR v_owner<>p_user THEN
  SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Playlist khong thuoc nguoi dung';
 END IF;
 SELECT COALESCE(MAX(ViTri),0)+1 INTO v_position FROM ChiTietPlaylist WHERE MaPlaylist=p_playlist;
 INSERT INTO ChiTietPlaylist(ViTri,MaPlaylist,MaBaiHat) VALUES(v_position,p_playlist,p_song);
 COMMIT;
END$$
CREATE PROCEDURE sp_LichSuNgheTheoNguoiDung(IN p_user BIGINT)
BEGIN
 SELECT l.*,b.TenBaiHat FROM LichSuNghe l JOIN BaiHat b USING(MaBaiHat)
 WHERE l.MaNguoiDung=p_user ORDER BY l.ThoiDiemNghe DESC,l.MaLichSuNghe DESC LIMIT 100;
END$$
DELIMITER ;
