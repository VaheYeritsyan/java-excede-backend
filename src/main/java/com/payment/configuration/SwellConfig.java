package com.payment.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwellConfig {

	
	@Value("${api.swell.storeId}")
	private String storeId;
	
	
	@Value("${api.swell.secretKey}")
	private String secretKey;
	
	
	@Value("${api.swell.host}")
	private String host;
	
	
	@Value("${api.swell.port}")
	private int port;
	
	
	// How much data can be fetched from swell.
	public static final int FETCH_LIMIT = 1000;
	
	
	public String getStoreId() {
		return storeId;
	}
	
	
	public String getSecretKey() {
		return secretKey;
	}
	
	
	public String getHost() {
		return host;
	}
	
	
	public int getPort() {
		return port;
	}
	
}
