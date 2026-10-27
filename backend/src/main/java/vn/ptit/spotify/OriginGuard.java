package vn.ptit.spotify;

import jakarta.servlet.http.*;
import org.springframework.web.servlet.HandlerInterceptor;
import java.util.Set;
import java.util.Arrays;
import java.util.stream.Collectors;

/** Vite reverse proxy keeps browser requests same-origin. Reject cross-origin writes. */
public class OriginGuard implements HandlerInterceptor {
 private final Set<String> allowed;
 public OriginGuard(String origins) {
  allowed=Arrays.stream(origins.split(",")).map(String::strip).filter(v->!v.isEmpty()).collect(Collectors.toUnmodifiableSet());
 }
 public boolean preHandle(HttpServletRequest req,HttpServletResponse res,Object handler) throws Exception {
  String origin=req.getHeader("Origin");
  if(!Set.of("GET","HEAD","OPTIONS").contains(req.getMethod()) && origin!=null && !allowed.contains(origin)) {
   res.sendError(403);return false;
  }
  return true;
 }
}
