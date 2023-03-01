package com.payment.service.swell;

import org.springframework.stereotype.Service;

import com.payment.integration.swell.SwellConnection;
import com.payment.util.ApiDataObject;

import lombok.RequiredArgsConstructor;



/**
 *
 * An API into Swells back-end Product functions.
 * @author Oska Jory <oska@excede.com.au>
 *
 */
@Service
@RequiredArgsConstructor
public class SwellProductService {

	private final SwellConnection connection;


	public ApiDataObject getAllProducts() {
		return connection.get("/products");
	}

	public ApiDataObject getAllProducts(int page) {
		return connection.get("/products?page=" + page);
	}

	public ApiDataObject getAllProductsWithLimit(int limit) {
		return connection.get("/products?limit=" + limit);
	}


	public ApiDataObject getAllProductsWithLimit(int page, int limit) {
		return connection.get("/products?page=" + page + (limit == 0 ? "" : "&limit=" + limit));
	}

	public ApiDataObject getAllProducts(int page, int limit) {
		return connection.get("/products?page=" + page + (limit == 0 ? "" : "&limit=" + limit));
	}

}
