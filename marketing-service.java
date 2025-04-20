// MarketingServiceApplication.java
package com.crm.marketing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class MarketingServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(MarketingServiceApplication.class, args);
    }
}

// Campaign.java (Entity)
package com.crm.marketing.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Campaign {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String targetSegment;
    private String content;
}

// CampaignDTO.java
package com.crm.marketing.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CampaignDTO {
    private Long id;
    private String name;
    private String targetSegment;
    private String content;
}

// CustomerDTO.java (from Customer Service)
package com.crm.marketing.dto;

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

// CampaignRepository.java
package com.crm.marketing.repository;

import com.crm.marketing.entity.Campaign;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CampaignRepository extends JpaRepository<Campaign, Long> {
}

// CampaignService.java
package com.crm.marketing.service;

import com.crm.marketing.dto.CampaignDTO;
import java.util.List;

public interface CampaignService {
    CampaignDTO createCampaign(CampaignDTO dto);
    List<CampaignDTO> getAllCampaigns();
}

// CampaignServiceImpl.java
package com.crm.marketing.service.impl;

import com.crm.marketing.dto.CampaignDTO;
import com.crm.marketing.entity.Campaign;
import com.crm.marketing.repository.CampaignRepository;
import com.crm.marketing.service.CampaignService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CampaignServiceImpl implements CampaignService {
    private final CampaignRepository repository;
    private final ModelMapper modelMapper;

    @Override
    public CampaignDTO createCampaign(CampaignDTO dto) {
        Campaign campaign = modelMapper.map(dto, Campaign.class);
        return modelMapper.map(repository.save(campaign), CampaignDTO.class);
    }

    @Override
    public List<CampaignDTO> getAllCampaigns() {
        return repository.findAll().stream()
                .map(campaign -> modelMapper.map(campaign, CampaignDTO.class))
                .collect(Collectors.toList());
    }
}

// CustomerClient.java (Feign Client to talk to Customer Service)
package com.crm.marketing.client;

import com.crm.marketing.dto.CustomerDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.List;

@FeignClient(name = "customer-service")
public interface CustomerClient {
    @GetMapping("/customers")
    List<CustomerDTO> getAllCustomers();
}

// CampaignController.java
package com.crm.marketing.controller;

import com.crm.marketing.client.CustomerClient;
import com.crm.marketing.dto.CampaignDTO;
import com.crm.marketing.dto.CustomerDTO;
import com.crm.marketing.service.CampaignService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/marketing/campaigns")
@RequiredArgsConstructor
public class CampaignController {
    private final CampaignService campaignService;
    private final CustomerClient customerClient;

    @PostMapping
    public ResponseEntity<CampaignDTO> createCampaign(@RequestBody CampaignDTO dto) {
        return ResponseEntity.ok(campaignService.createCampaign(dto));
    }

    @GetMapping
    public ResponseEntity<List<CampaignDTO>> getAllCampaigns() {
        return ResponseEntity.ok(campaignService.getAllCampaigns());
    }

    @GetMapping("/customers")
    public ResponseEntity<List<CustomerDTO>> getCustomersForMarketing() {
        return ResponseEntity.ok(customerClient.getAllCustomers());
    }
}

// application.properties
server.port=8083
spring.application.name=marketing-service

spring.datasource.url=jdbc:mysql://localhost:3306/marketing_db?useSSL=false&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=your_password
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect

# Eureka Discovery Client
eureka.client.service-url.defaultZone=http://localhost:8761/eureka/
eureka.instance.prefer-ip-address=true

# Spring Cloud Config
spring.cloud.config.uri=http://localhost:8888

# Feign Client logging
logging.level.com.crm.marketing.client=DEBUG
```

Let me know if you’d like help testing inter-service communication or setting up Swagger docs or resilience (like Retry/Fallback with Resilience4J).
