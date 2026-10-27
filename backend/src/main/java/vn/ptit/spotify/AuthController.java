package vn.ptit.spotify;

import jakarta.servlet.http.*;
import java.util.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import static org.springframework.http.HttpStatus.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
 private final JdbcTemplate db;
 private final BCryptPasswordEncoder encoder=new BCryptPasswordEncoder(10);
 public AuthController(JdbcTemplate db){this.db=db;}
 public record Credentials(String email,String password,String name){}
 public record User(long id,String name,String email,String role) implements java.io.Serializable{}
 static User user(HttpSession s){Object u=s.getAttribute("user");if(!(u instanceof User))throw new ResponseStatusException(UNAUTHORIZED,"Cần đăng nhập");return (User)u;}
 static User owner(HttpSession s,long id){User u=user(s);if(u.id()!=id)throw new ResponseStatusException(FORBIDDEN,"Không có quyền với người dùng này");return u;}
 static void admin(HttpSession s){if(!user(s).role().equals("ADMIN"))throw new ResponseStatusException(FORBIDDEN,"Cần quyền quản trị");}
 @PostMapping("/register")
 @ResponseStatus(CREATED)
 public Map<String,String> register(@RequestBody Credentials c){
  validate(c);if(c.name()==null||c.name().isBlank()||c.name().length()>120)throw new ResponseStatusException(BAD_REQUEST,"Họ tên không hợp lệ");
  db.update("INSERT INTO NguoiDung(HoTen,Email,MatKhauHash) VALUES(?,?,?)",c.name().strip(),c.email().strip().toLowerCase(Locale.ROOT),encoder.encode(c.password()));
  return Map.of("message","Đăng ký thành công");
 }
 @PostMapping("/login")
 public User login(@RequestBody Credentials c,HttpServletRequest req){
  validate(c);List<Map<String,Object>> rows=db.queryForList("SELECT * FROM NguoiDung WHERE Email=?",c.email().strip());
  if(rows.isEmpty()||!encoder.matches(c.password(),(String)rows.get(0).get("MatKhauHash")))throw new ResponseStatusException(UNAUTHORIZED,"Email hoặc mật khẩu không đúng");
  Map<String,Object> r=rows.get(0);User u=new User(((Number)r.get("MaNguoiDung")).longValue(),(String)r.get("HoTen"),(String)r.get("Email"),(String)r.get("VaiTro"));
  HttpSession old=req.getSession(false);if(old!=null)old.invalidate();req.getSession(true).setAttribute("user",u);return u;
 }
 @GetMapping("/me") public User me(HttpSession s){return user(s);}
 @PostMapping("/logout") public Map<String,String> logout(HttpSession s){s.invalidate();return Map.of("message","Đã đăng xuất");}
 private void validate(Credentials c){if(c.email()==null||!c.email().matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")||c.email().length()>190||c.password()==null||c.password().length()<8||c.password().getBytes(java.nio.charset.StandardCharsets.UTF_8).length>72)throw new ResponseStatusException(BAD_REQUEST,"Email hợp lệ và mật khẩu 8–72 byte là bắt buộc");}
}
