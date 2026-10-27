package vn.ptit.spotify;

import java.nio.file.Path;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;

@SpringBootApplication
public class Application {
 public static void main(String[] args) { SpringApplication.run(Application.class,args); }
 @Configuration
 static class WebConfig implements WebMvcConfigurer {
  @Value("${app.audio-dir}") String audio;
  @Value("${app.allowed-origins}") String origins;
  public void addResourceHandlers(ResourceHandlerRegistry r) {
   r.addResourceHandler("/audio/**").addResourceLocations(Path.of(audio).toAbsolutePath().normalize().toUri().toString());
  }
  public void addInterceptors(InterceptorRegistry registry) { registry.addInterceptor(new OriginGuard(origins)); }
 }
}
