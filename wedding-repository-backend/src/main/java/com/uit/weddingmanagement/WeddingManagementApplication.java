package com.uit.weddingmanagement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @SpringBootApplication là một annotation tổng hợp của Spring Boot, nó kết hợp
 *                        các annotation sau:
 *                        - @Configuration: cho phép class này chứa các bean
 *                        được định nghĩa bằng phương thức @Bean
 *                        - @EnableAutoConfiguration: kích hoạt tính năng tự
 *                        động cấu hình của Spring Boot,
 *                        giúp tự động cấu hình các bean dựa trên classpath và
 *                        các thiết lập mặc định
 *                        - @ComponentScan: cho phép Spring Boot tự động quét
 *                        các package của class main trở xuống để tìm kiếm các
 *                        component, service, repository, v.v. để đăng ký chúng
 *                        vào Spring context
 */

@SpringBootApplication
public class WeddingManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(WeddingManagementApplication.class, args);
    }
}
