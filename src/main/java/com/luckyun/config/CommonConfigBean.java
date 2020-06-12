package com.luckyun.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class CommonConfigBean {

	@Bean("restTemplateDns")
	@LoadBalanced
	public RestTemplate restTemplateDns() {
		return new RestTemplate();
	}
	
	@Bean("restTemplate")
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}
}
