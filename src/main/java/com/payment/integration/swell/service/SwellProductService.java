package com.payment.integration.swell.service;

import org.springframework.stereotype.Service;

import com.payment.integration.swell.SwellConnection;
import com.payment.util.ApiDataObject;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SwellProductService {

	private final SwellConnection connection;
	
	
	public ApiDataObject getAllProducts() {
		ApiDataObject response = connection.get("/products");		
		return response;
	}	
	
	public ApiDataObject getAllProducts(int page) {
		ApiDataObject response = connection.get("/products?page=" + page);
		return response;
	}
	
	public ApiDataObject getAllProductsWithLimit(int limit) {
		ApiDataObject response = connection.get("/products?limit=" + limit);
		return response;
	}
	
	
	public ApiDataObject getAllProductsWithLimit(int page, int limit) {
		ApiDataObject response = connection.get("/products?page=" + page + (limit == 0 ? "" : "&limit=" + limit));
		return response;
	}
	
	public ApiDataObject getAllProducts(int page, int limit) {
		ApiDataObject response = connection.get("/products?page=" + page + (limit == 0 ? "" : "&limit=" + limit));
		return response;
	}
	
}
