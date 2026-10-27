package vn.ptit.spotify;

import jakarta.servlet.http.HttpSession;
import java.util.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import static org.springframework.http.HttpStatus.*;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
 record Resource(String table,String key,List<String> columns){}
 private static final Map<String,Resource> RESOURCES=Map.of(
  "artists",new Resource("NgheSi","MaNgheSi",List.of("TenNgheSi","QuocGia")),
  "albums",new Resource("Album","MaAlbum",List.of("TenAlbum","NgayPhatHanh","MaNgheSi")),
  "genres",new Resource("TheLoai","MaTheLoai",List.of("TenTheLoai")),
  "songs",new Resource("BaiHat","MaBaiHat",List.of("TenBaiHat","ThoiLuong","MaAlbum","MaTheLoai","DuongDanAudio")));
 private final JdbcTemplate db;
 public AdminController(JdbcTemplate db){this.db=db;}
 private Resource resource(String type,HttpSession s){AuthController.admin(s);Resource r=RESOURCES.get(type);if(r==null)throw new ResponseStatusException(NOT_FOUND,"Không có nhóm dữ liệu");return r;}
 @GetMapping("/{type}") public List<Map<String,Object>> list(@PathVariable String type,HttpSession s){Resource r=resource(type,s);return db.queryForList("SELECT * FROM "+r.table()+" ORDER BY "+r.key());}
 @PostMapping("/{type}") @ResponseStatus(CREATED)
 public Map<String,String> create(@PathVariable String type,@RequestBody Map<String,Object> body,HttpSession s){Resource r=resource(type,s);validate(r,body);db.update("INSERT INTO "+r.table()+"("+String.join(",",r.columns())+") VALUES("+String.join(",",Collections.nCopies(r.columns().size(),"?"))+")",r.columns().stream().map(body::get).toArray());return MusicController.ok();}
 @PutMapping("/{type}/{id}") @Transactional
 public Map<String,String> update(@PathVariable String type,@PathVariable long id,@RequestBody Map<String,Object> body,HttpSession s){Resource r=resource(type,s);validate(r,body);
  if(db.queryForList("SELECT "+r.key()+" FROM "+r.table()+" WHERE "+r.key()+"=? FOR UPDATE",id).isEmpty())throw new ResponseStatusException(NOT_FOUND,"Không có dữ liệu");
  if(type.equals("songs")){Integer max=db.queryForObject("SELECT COALESCE(MAX(ThoiLuongDaNghe),0) FROM LichSuNghe WHERE MaBaiHat=?",Integer.class,id);if(Integer.parseInt(body.get("ThoiLuong").toString())<max)throw new ResponseStatusException(CONFLICT,"Thời lượng mới ngắn hơn lịch sử đã lưu");}
  List<Object> values=new ArrayList<>();r.columns().forEach(c->values.add(body.get(c)));values.add(id);
  db.update("UPDATE "+r.table()+" SET "+String.join(",",r.columns().stream().map(c->c+"=?").toList())+" WHERE "+r.key()+"=?",values.toArray());return MusicController.ok();
 }
 @DeleteMapping("/{type}/{id}") public Map<String,String> delete(@PathVariable String type,@PathVariable long id,HttpSession s){Resource r=resource(type,s);if(db.update("DELETE FROM "+r.table()+" WHERE "+r.key()+"=?",id)==0)throw new ResponseStatusException(NOT_FOUND,"Không có dữ liệu");return MusicController.ok();}
 private void validate(Resource r,Map<String,Object> b){
  if(!b.keySet().equals(new HashSet<>(r.columns())))throw new ResponseStatusException(BAD_REQUEST,"Cần đúng các thuộc tính: "+r.columns());
  for(String c:r.columns()){Object v=b.get(c);if(v==null||v.toString().isBlank()||v.toString().length()>255)throw new ResponseStatusException(BAD_REQUEST,"Thuộc tính không hợp lệ: "+c);}
  if(r.table().equals("BaiHat")){
   int seconds=Integer.parseInt(b.get("ThoiLuong").toString());if(seconds<=0)throw new ResponseStatusException(BAD_REQUEST,"Thời lượng phải dương");
   if(!b.get("DuongDanAudio").toString().matches("/audio/demo-[123]\\.wav")||seconds!=12)throw new ResponseStatusException(BAD_REQUEST,"Demo dùng ba WAV nội bộ dài 12 giây");
  }
 }
}
