package com.eshop.payment.configurations;

import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class ApplicationConfiguration {
    @Value("${razorpay.key}")
    private String key;
    @Value("${razorpay.secret}")
    private String secret;

    @Bean
    public RazorpayClient getRazorPayClient(){
        try {
            return new RazorpayClient(key, secret);
        } catch (RazorpayException e) {
            System.out.println("Unable to create client of Razorpay");
            throw new RuntimeException("Failed to instantiate Razorpay");
        }
    }

    @Bean
    @LoadBalanced
    public RestTemplate createRestTemplate(){
        return new RestTemplate();
    }

}
