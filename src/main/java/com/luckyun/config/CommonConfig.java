package com.luckyun.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Configuration
@Data
public class CommonConfig {

	@Value("${common.eureka-instance.host}")
	private String eurekaHost;
	
	@Value("${common.eureka-instance.port}")
	private Integer eurekaPort;
	
	@Value("${spring.profiles.active}")
	private String cenv;
}
