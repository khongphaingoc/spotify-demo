package vn.ptit.spotify;

import jakarta.servlet.http.HttpSession;
import java.util.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import static org.springframework.http.HttpStatus.*;

@RestController
@RequestMapping("/api")
public class MusicController {
 private final JdbcTemplate db;
 public MusicController(JdbcTemplate db){this.db=db;}
 public record Name(String name){}
 public record Song(long songId){}
 public record Artist(long artistId){}
 public record Listen(long songId,int seconds){}
 public record Order(List<Long> songIds){}
 @GetMapping({"/songs","/songs/search"}) public List<Map<String,Object>> songs(@RequestParam(defaultValue="") String q,@RequestParam(required=false) Long albumId,@RequestParam(required=false) Long artistId,@RequestParam(required=false) Long genreId){
  if(q.length()>180)throw new ResponseStatusException(BAD_REQUEST,"Từ khóa quá dài");
  String query="SELECT * FROM vw_BaiHatChiTiet WHERE TenBaiHat LIKE ?";List<Object> args=new ArrayList<>();args.add("%"+q+"%");
  if(albumId!=null){query+=" AND MaAlbum=?";args.add(albumId);}if(artistId!=null){query+=" AND MaNgheSi=?";args.add(artistId);}if(genreId!=null){query+=" AND MaTheLoai=?";args.add(genreId);}
  return db.queryForList(query+" ORDER BY MaBaiHat LIMIT 200",args.toArray());
 }
 @GetMapping("/artists") public List<Map<String,Object>> artists(){return db.queryForList("SELECT * FROM NgheSi ORDER BY MaNgheSi");}
 @GetMapping("/albums") public List<Map<String,Object>> albums(){return db.queryForList("SELECT a.*,n.TenNgheSi FROM Album a JOIN NgheSi n USING(MaNgheSi) ORDER BY a.MaAlbum");}
 @GetMapping("/genres") public List<Map<String,Object>> genres(){return db.queryForList("SELECT * FROM TheLoai ORDER BY MaTheLoai");}
 @GetMapping("/stats/top-songs") public List<Map<String,Object>> top(){return db.queryForList("SELECT * FROM vw_BaiHatChiTiet ORDER BY LuotNghe DESC,MaBaiHat LIMIT 10");}
 @GetMapping("/playlists") public List<Map<String,Object>> playlists(@RequestParam(required=false) Long userId,HttpSession s){long id=AuthController.user(s).id();if(userId!=null)AuthController.owner(s,userId);return db.queryForList("SELECT * FROM Playlist WHERE MaNguoiDung=? ORDER BY MaPlaylist",id);}
 @PostMapping("/playlists") @ResponseStatus(CREATED)
 public Map<String,Object> create(@RequestBody Name n,HttpSession s){long u=AuthController.user(s).id();validName(n.name());db.update("INSERT INTO Playlist(TenPlaylist,MaNguoiDung) VALUES(?,?)",n.name().strip(),u);return db.queryForMap("SELECT * FROM Playlist WHERE MaNguoiDung=? ORDER BY MaPlaylist DESC LIMIT 1",u);}
 @PatchMapping("/playlists/{id}") public Map<String,String> rename(@PathVariable long id,@RequestBody Name n,HttpSession s){playlist(id,s,false);validName(n.name());db.update("UPDATE Playlist SET TenPlaylist=? WHERE MaPlaylist=?",n.name().strip(),id);return ok();}
 @DeleteMapping("/playlists/{id}") public Map<String,String> delete(@PathVariable long id,HttpSession s){playlist(id,s,false);db.update("DELETE FROM Playlist WHERE MaPlaylist=?",id);return ok();}
 @GetMapping("/playlists/{id}/songs") public List<Map<String,Object>> details(@PathVariable long id,HttpSession s){playlist(id,s,false);return db.queryForList("SELECT b.*,c.ViTri FROM ChiTietPlaylist c JOIN vw_BaiHatChiTiet b USING(MaBaiHat) WHERE MaPlaylist=? ORDER BY ViTri",id);}
 @PostMapping("/playlists/{id}/songs") @ResponseStatus(CREATED)
 public Map<String,String> addSong(@PathVariable long id,@RequestBody Song song,HttpSession s){playlist(id,s,false);db.update("CALL sp_ThemBaiHatVaoPlaylist(?,?,?)",id,song.songId(),AuthController.user(s).id());return ok();}
 @DeleteMapping("/playlists/{id}/songs/{songId}") @Transactional
 public Map<String,String> removeSong(@PathVariable long id,@PathVariable long songId,HttpSession s){playlist(id,s,true);db.update("DELETE FROM ChiTietPlaylist WHERE MaPlaylist=? AND MaBaiHat=?",id,songId);return ok();}
 @PutMapping("/playlists/{id}/order") @Transactional
 public Map<String,String> reorder(@PathVariable long id,@RequestBody Order order,HttpSession s){
  playlist(id,s,true);List<Long> existing=db.queryForList("SELECT MaBaiHat FROM ChiTietPlaylist WHERE MaPlaylist=? ORDER BY ViTri",Long.class,id);
  if(order.songIds()==null||order.songIds().size()!=existing.size()||new HashSet<>(order.songIds()).size()!=existing.size()||!new HashSet<>(order.songIds()).equals(new HashSet<>(existing)))throw new ResponseStatusException(BAD_REQUEST,"Phải gửi đúng toàn bộ bài, không trùng");
  // Chuyển từng vị trí đến vùng dương tạm không giao nhau; sau đó gán 1..N.
  int max=db.queryForObject("SELECT COALESCE(MAX(ViTri),0) FROM ChiTietPlaylist WHERE MaPlaylist=?",Integer.class,id);
  if(max>Integer.MAX_VALUE-existing.size()-1)throw new ResponseStatusException(CONFLICT,"Vị trí quá lớn");
  for(int i=0;i<existing.size();i++)db.update("UPDATE ChiTietPlaylist SET ViTri=? WHERE MaPlaylist=? AND MaBaiHat=?",max+i+1,id,existing.get(i));
  for(int i=0;i<order.songIds().size();i++)db.update("UPDATE ChiTietPlaylist SET ViTri=? WHERE MaPlaylist=? AND MaBaiHat=?",i+1,id,order.songIds().get(i));
  return ok();
 }
 @GetMapping("/users/{id}/favorites") public List<Map<String,Object>> favorites(@PathVariable long id,HttpSession s){AuthController.owner(s,id);return db.queryForList("SELECT b.* FROM BaiHatYeuThich y JOIN vw_BaiHatChiTiet b USING(MaBaiHat) WHERE MaNguoiDung=? ORDER BY y.MaYeuThich DESC",id);}
 @PostMapping("/users/{id}/favorites") @ResponseStatus(CREATED) public Map<String,String> favorite(@PathVariable long id,@RequestBody Song b,HttpSession s){AuthController.owner(s,id);db.update("INSERT INTO BaiHatYeuThich(MaNguoiDung,MaBaiHat) VALUES(?,?)",id,b.songId());return ok();}
 @DeleteMapping("/users/{id}/favorites/{songId}") public Map<String,String> unfavorite(@PathVariable long id,@PathVariable long songId,HttpSession s){AuthController.owner(s,id);db.update("DELETE FROM BaiHatYeuThich WHERE MaNguoiDung=? AND MaBaiHat=?",id,songId);return ok();}
 @GetMapping("/users/{id}/follows") public List<Map<String,Object>> follows(@PathVariable long id,HttpSession s){AuthController.owner(s,id);return db.queryForList("SELECT n.* FROM TheoDoiNgheSi t JOIN NgheSi n USING(MaNgheSi) WHERE MaNguoiDung=? ORDER BY n.MaNgheSi",id);}
 @PostMapping("/users/{id}/follows") @ResponseStatus(CREATED) public Map<String,String> follow(@PathVariable long id,@RequestBody Artist n,HttpSession s){AuthController.owner(s,id);db.update("INSERT INTO TheoDoiNgheSi(MaNguoiDung,MaNgheSi) VALUES(?,?)",id,n.artistId());return ok();}
 @DeleteMapping("/users/{id}/follows/{artistId}") public Map<String,String> unfollow(@PathVariable long id,@PathVariable long artistId,HttpSession s){AuthController.owner(s,id);db.update("DELETE FROM TheoDoiNgheSi WHERE MaNguoiDung=? AND MaNgheSi=?",id,artistId);return ok();}
 @GetMapping("/users/{id}/history") public List<Map<String,Object>> history(@PathVariable long id,HttpSession s){AuthController.owner(s,id);return db.queryForList("SELECT l.*,b.TenBaiHat,b.DuongDanAudio,b.ThoiLuong,b.TenAlbum,b.TenNgheSi FROM LichSuNghe l JOIN vw_BaiHatChiTiet b USING(MaBaiHat) WHERE MaNguoiDung=? ORDER BY ThoiDiemNghe DESC,MaLichSuNghe DESC LIMIT 100",id);}
 @PostMapping("/users/{id}/history") @Transactional @ResponseStatus(CREATED)
 public Map<String,String> listen(@PathVariable long id,@RequestBody Listen l,HttpSession s){
  AuthController.owner(s,id);List<Integer> durations=db.queryForList("SELECT ThoiLuong FROM BaiHat WHERE MaBaiHat=? FOR UPDATE",Integer.class,l.songId());
  if(durations.isEmpty())throw new ResponseStatusException(NOT_FOUND,"Bài hát không tồn tại");if(l.seconds()<0||l.seconds()>durations.get(0))throw new ResponseStatusException(BAD_REQUEST,"Thời lượng nghe không hợp lệ");
  db.update("INSERT INTO LichSuNghe(MaNguoiDung,MaBaiHat,ThoiLuongDaNghe) VALUES(?,?,?)",id,l.songId(),l.seconds());return ok();
 }
 private void playlist(long id,HttpSession s,boolean lock){List<Long> owners=db.queryForList("SELECT MaNguoiDung FROM Playlist WHERE MaPlaylist=?"+(lock?" FOR UPDATE":""),Long.class,id);if(owners.isEmpty())throw new ResponseStatusException(NOT_FOUND,"Không có playlist");AuthController.owner(s,owners.get(0));}
 private void validName(String n){if(n==null||n.isBlank()||n.length()>150)throw new ResponseStatusException(BAD_REQUEST,"Tên cần từ 1 đến 150 ký tự");}
 static Map<String,String> ok(){return Map.of("message","Thành công");}
}
