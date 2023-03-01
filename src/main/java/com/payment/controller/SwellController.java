package com.payment.controller;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.payment.integration.swell.dto.SwellCustomer;
import com.payment.integration.swell.service.SwellAccountService;
import com.payment.util.ApiDataObject;

import lombok.RequiredArgsConstructor;


/**
 * TODO: Secure these end-points so they cannot be publicly accessed, these are only for future
 * admin use and API testing.
 *
 * @author Oska Jory <oska@excede.com.au>
 *
 */
@RestController
@RequestMapping("swell")
@RequiredArgsConstructor
public class SwellController {

	
	private final SwellAccountService accounts;
	

	// Returns a count of total users.
	@GetMapping("/accounts/count")
	public long getAccountCount() {
		return accounts.getAccountCount();
	}
	
	
	// Fetches an account from swell.
	@GetMapping("/account/{email}")
	public ApiDataObject getAccount(@PathVariable("email") String email) {
		return accounts.getAccount(email);
	}
	
	
	// Deletes an account from swell.
	@DeleteMapping("/account/{email}")
	public ApiDataObject deleteAccount(@PathVariable("email") String email) {
		return accounts.deleteAccount(email);
	}
	
	
	// Creates an account on swell.
	@PostMapping("/account/create")
	public ApiDataObject createAccount(SwellCustomer customer) {
		return accounts.createAccount(customer);
	}
	
	
	// Generates a password token for an account.
	@PutMapping("/account/generate-token/{email}")
	public ApiDataObject generateToken(@PathVariable("email") String email) {
		return accounts.generatePasswordToken(email);
	}
	
}
