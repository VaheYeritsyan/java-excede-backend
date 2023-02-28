package com.payment.controller;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.payment.integration.swell.service.SwellAccountService;
import com.payment.util.ApiDataObject;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("swell")
@RequiredArgsConstructor
public class SwellController {

	
	private final SwellAccountService accounts;
	
	
	@GetMapping("/account/{email}")
	public ApiDataObject getAccount(@PathVariable("email") String email) {
		return accounts.getAccount(email);
	}
	
	
	@DeleteMapping("/account/{email}")
	public ApiDataObject deleteAccount(@PathVariable("email") String email) {
		return accounts.deleteAccount(email);
	}
}
