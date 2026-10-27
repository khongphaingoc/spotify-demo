package vn.ptit.spotify;

import java.util.Map;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class Errors {
 @ExceptionHandler(ResponseStatusException.class)
 ResponseEntity<?> expected(ResponseStatusException e){return ResponseEntity.status(e.getStatusCode()).body(Map.of("message",e.getReason()==null?"Yêu cầu không hợp lệ":e.getReason()));}
 @ExceptionHandler(DataIntegrityViolationException.class)
 ResponseEntity<?> integrity(DataIntegrityViolationException e){return ResponseEntity.status(409).body(Map.of("message","Dữ liệu trùng, không tồn tại liên kết hoặc vi phạm ràng buộc"));}
 @ExceptionHandler({IllegalArgumentException.class,org.springframework.http.converter.HttpMessageNotReadableException.class,org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class})
 ResponseEntity<?> bad(Exception e){return ResponseEntity.badRequest().body(Map.of("message","Dữ liệu đầu vào không hợp lệ"));}
}
