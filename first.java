// CustomerServiceApplication.java
package com.crm.customer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class CustomerServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(CustomerServiceApplication.class, args);
    }
}

// Customer.java (Entity)
package com.crm.customer.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String email;
    private String phone;
    
    @ElementCollection
    private List<String> interactions;
}

// CustomerRepository.java
package com.crm.customer.repository;

import com.crm.customer.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
}

// CustomerDTO.java
package com.crm.customer.dto;

import lombok.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerDTO {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private List<String> interactions;
}

// CustomerService.java
package com.crm.customer.service;

import com.crm.customer.dto.CustomerDTO;
import java.util.List;

public interface CustomerService {
    CustomerDTO createCustomer(CustomerDTO customerDTO);
    CustomerDTO getCustomerById(Long id);
    List<CustomerDTO> getAllCustomers();
    CustomerDTO updateCustomer(Long id, CustomerDTO customerDTO);
    void deleteCustomer(Long id);
}

// CustomerServiceImpl.java
package com.crm.customer.service.impl;

import com.crm.customer.dto.CustomerDTO;
import com.crm.customer.entity.Customer;
import com.crm.customer.repository.CustomerRepository;
import com.crm.customer.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {
    private final CustomerRepository repository;
    private final ModelMapper modelMapper;

    @Override
    public CustomerDTO createCustomer(CustomerDTO dto) {
        Customer customer = modelMapper.map(dto, Customer.class);
        return modelMapper.map(repository.save(customer), CustomerDTO.class);
    }

    @Override
    public CustomerDTO getCustomerById(Long id) {
        Customer customer = repository.findById(id).orElseThrow();
        return modelMapper.map(customer, CustomerDTO.class);
    }

    @Override
    public List<CustomerDTO> getAllCustomers() {
        return repository.findAll().stream()
                .map(c -> modelMapper.map(c, CustomerDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public CustomerDTO updateCustomer(Long id, CustomerDTO dto) {
        Customer existing = repository.findById(id).orElseThrow();
        existing.setName(dto.getName());
        existing.setEmail(dto.getEmail());
        existing.setPhone(dto.getPhone());
        existing.setInteractions(dto.getInteractions());
        return modelMapper.map(repository.save(existing), CustomerDTO.class);
    }

    @Override
    public void deleteCustomer(Long id) {
        repository.deleteById(id);
    }
}

// CustomerController.java
package com.crm.customer.controller;

import com.crm.customer.dto.CustomerDTO;
import com.crm.customer.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/customers")
@RequiredArgsConstructor
public class CustomerController {
    private final CustomerService customerService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CustomerDTO> createCustomer(@RequestBody CustomerDTO dto) {
        return ResponseEntity.ok(customerService.createCustomer(dto));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES', 'SUPPORT')")
    public ResponseEntity<List<CustomerDTO>> getAllCustomers() {
        return ResponseEntity.ok(customerService.getAllCustomers());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES', 'SUPPORT')")
    public ResponseEntity<CustomerDTO> getCustomerById(@PathVariable Long id) {
        return ResponseEntity.ok(customerService.getCustomerById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES')")
    public ResponseEntity<CustomerDTO> updateCustomer(@PathVariable Long id, @RequestBody CustomerDTO dto) {
        return ResponseEntity.ok(customerService.updateCustomer(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCustomer(@PathVariable Long id) {
        customerService.deleteCustomer(id);
        return ResponseEntity.noContent().build();
    }
}

// application.yml
server:
  port: 8082

spring:
  application:
    name: customer-service
  datasource:
    url: jdbc:oracle:thin:@localhost:1521:xe
    username: your_oracle_username
    password: your_oracle_password
    driver-class-name: oracle.jdbc.OracleDriver
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    database-platform: org.hibernate.dialect.Oracle10gDialect

  cloud:
    config:
      uri: http://localhost:8888

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/

# dependencies in pom.xml include: spring-boot-starter-web, spring-boot-starter-data-jpa, spring-boot-starter-security, lombok, modelmapper, Oracle JDBC, spring-cloud-starter-netflix-eureka-client
