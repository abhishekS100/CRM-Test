server:
  port: 8761

eureka:
  client:
    register-with-eureka: false
    fetch-registry: false



✅ Step 2: Register Auth Service to Eureka
In auth-service/application.yml:
yaml
Copy
Edit
spring:
  application:
    name: auth-service

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka



Now, when you run auth-service, it will appear on the Eureka dashboard (http://localhost:8761).








server:
  port: 8080

spring:
  application:
    name: api-gateway
  cloud:
    gateway:
      discovery:
        locator:
          enabled: true
          lowerCaseServiceId: true
      routes:
        - id: auth-service
          uri: lb://auth-service
          predicates:
            - Path=/auth/**

        - id: customer-service
          uri: lb://customer-service
          predicates:
            - Path=/customers/**

        - id: marketing-service
          uri: lb://marketing-service
          predicates:
            - Path=/marketing/**

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka



✅ Step 4: Secure Gateway with JWT Token Validation
Update application.yml in gateway-service:

yaml
Copy
Edit
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          secret: secret-key
✅ Step 5: Add Security Config in Gateway
SecurityConfig.java
java
Copy
Edit
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
            .csrf(ServerHttpSecurity.CsrfSpec::disable)
            .authorizeExchange(ex -> ex
                .pathMatchers("/auth/**").permitAll()
                .anyExchange().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
            .build();
    }
}
✅ Test Flow
Run Services in order:

eureka-server

gateway-service

auth-service

(customer-service, marketing-service, etc.)

Access endpoints via:

http://localhost:8080/auth/login

http://localhost:8080/customers/

http://localhost:8080/marketing/

Use Postman to send Bearer <JWT> token in header for customer/marketing routes.
                                                                        
