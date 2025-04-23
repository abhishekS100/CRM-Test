✅ Step 2: Entity - User
java
Copy
Edit
package com.crm.authservice.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private String password;

    @Enumerated(EnumType.STRING)
    private Role role;

    public enum Role {
        ADMIN, SALES, SUPPORT
    }
}
✅ Step 3: Repository
java
Copy
Edit
package com.crm.authservice.repository;

import com.crm.authservice.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
}
✅ Step 4: DTOs
java
Copy
Edit
package com.crm.authservice.dto;

import lombok.Data;

@Data
public class LoginRequest {
    private String username;
    private String password;
}
java
Copy
Edit
package com.crm.authservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResponse {
    private String token;
}
✅ Step 5: JWT Utility
java
Copy
Edit
package com.crm.authservice.util;

import com.crm.authservice.entity.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtUtils {

    private final String jwtSecret = "secret-key";
    private final long jwtExpiration = 86400000; // 1 day

    public String generateToken(User user) {
        return Jwts.builder()
                .setSubject(user.getUsername())
                .claim("role", user.getRole().name())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(SignatureAlgorithm.HS512, jwtSecret)
                .compact();
    }
}
✅ Step 6: Service Layer
java
Copy
Edit
package com.crm.authservice.service;

import com.crm.authservice.dto.LoginRequest;
import com.crm.authservice.dto.LoginResponse;

public interface AuthService {
    LoginResponse login(LoginRequest request);
}
java
Copy
Edit
package com.crm.authservice.service.impl;

import com.crm.authservice.dto.LoginRequest;
import com.crm.authservice.dto.LoginResponse;
import com.crm.authservice.entity.User;
import com.crm.authservice.repository.UserRepository;
import com.crm.authservice.service.AuthService;
import com.crm.authservice.util.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final JwtUtils jwtUtils;
    private final PasswordEncoder passwordEncoder;

    @Override
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        String token = jwtUtils.generateToken(user);
        return new LoginResponse(token);
    }
}
✅ Step 7: Controller
java
Copy
Edit
package com.crm.authservice.controller;

import com.crm.authservice.dto.LoginRequest;
import com.crm.authservice.dto.LoginResponse;
import com.crm.authservice.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) {
        return authService.login(request);
    }
}
✅ Step 8: Security Configuration
java
Copy
Edit
package com.crm.authservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf().disable()
                .authorizeHttpRequests(req -> req
                        .requestMatchers("/auth/**").permitAll()
                        .anyRequest().authenticated());
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
✅ Step 9: Application Properties
yaml
Copy
Edit
server:
  port: 8081

spring:
  application:
    name: auth-service
  datasource:
    url: jdbc:mysql://localhost:3306/crm_auth
    username: root
    password: your_mysql_password
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka
✅ Step 10: Main Application
java
Copy
Edit
package com.crm.authservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AuthServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
    }
}
✅ Step 11: Protect Other Microservices
In Customer / Marketing services:

Add this to application.yml:

yaml
Copy
Edit
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          secret: secret-key
Update SecurityConfig:

java
Copy
Edit
http
  .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
  .oauth2ResourceServer(oauth -> oauth.jwt());
Use @PreAuthorize("hasRole('ADMIN')") etc. in controllers.

✅ Testing
Run MySQL (with crm_auth DB).

Insert users manually with hashed passwords (or create a registration endpoint).

Use Postman:

POST /auth/login with valid credentials.

Copy token and set in Authorization header (Bearer <token>) in requests to customer-service or marketing-service.
