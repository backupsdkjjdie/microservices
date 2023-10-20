package com.vivek.ordermicroservice.config;

import com.vivek.ordermicroservice.external.decoder.CustomErrorDecoder;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfig {
    @Bean
    ErrorDecoder errorDecoder(){
        return new CustomErrorDecoder();
    }
}
