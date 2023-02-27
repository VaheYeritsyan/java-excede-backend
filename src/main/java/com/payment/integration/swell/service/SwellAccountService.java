package com.payment.integration.swell.service;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.stereotype.Service;

import com.payment.configuration.SwellConfig;
import com.payment.integration.swell.SwellConnection;
import com.payment.util.ApiDataObject;
import com.payment.util.CryptUtil;
import com.payment.util.JsonDataParser;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SwellAccountService {

	
	private final SwellConnection connection;
	
	
	/**
	 * Fetches a specified account from swell.
	 * 
	 * @param id - The ID of the account (Can also be an email address).
	 * @return The targeted account from swell.
	 */
	public ApiDataObject getAccount(String id) {
		ApiDataObject account = connection.get("/accounts/" + id);		
		return account;
	}
	
	/**
	 * Delete an individual Customer from swell.
	 * @param id - The id of the account we are deleting.
	 * @return A response {@link ApiDataObject} stating if the request was successful or not.
	 */
	public ApiDataObject deleteAccount(String id) {
		ApiDataObject response = new ApiDataObject();
		
		ApiDataObject body = new ApiDataObject();
		
		body.put("id", id);
		body.put("$force_delete", true);
		ApiDataObject api_response = connection.delete("/accounts/" + id, null, body);
		
		if (api_response.get("$data") == null) {
			System.out.println("Failed to delete: " + id);
			response.put("success", false);
		} else {
			response.put("success", true);
		}
		
		return response;
	}
	
	
	/**
	 * Adds a payment method to the customer on 
	 * @param account_id
	 * @param gateway
	 * @param gatewayCustomerId
	 * @param token
	 */
	public ApiDataObject addPaymentMethod(String account_id, String gateway, String gatewayCustomerId, String token, boolean make_default) {
		ApiDataObject body = new ApiDataObject();
		
		body.put("id", account_id);
		body.put("gateway", gateway);
		body.put("gateway_customer", gatewayCustomerId);
		body.put("token", token);
		body.put("$vault", "true");
		body.put("active", "true");
		
		ApiDataObject response = connection.post("/accounts/" + account_id + "/cards" , body);

		return response;
		
	}
	
	
	/**
	 * Makes a specified payment method the default payment method for a customer.
	 * @param account_id - The Swell account ID.
	 * @param payment_method_id - The id of the payment method stored in Swell.
	 * @return A response object from Swell.
	 * 
	 * 
	 * Swell Default Card Body structure
	 * 
	 * {
	 * 		billing: {
	 * 			account_card_id: "<card_id>"
	 * 		}
	 * }
	 */
	public ApiDataObject makeDefaultPaymentMethod(String account_id, String payment_method_id) {
		
		ApiDataObject body = new ApiDataObject();
		
		ApiDataObject billing = new ApiDataObject();
		
		billing.put("account_card_id", payment_method_id);
		
		body.put("billing", billing);
		
		ApiDataObject response = connection.put("/accounts/" + account_id, body);
		
		return response;
	}
	
	
	/**
	 * 
	 * Creates a new address for a customer in Swell.
	 * 
	 * @param account_id - The ID of the customer we are creating the address for.
	 * @param address_1 - The address line 1.
	 * @param address_2 - The address line 2.
	 * @param city - The city of the address. 
	 * @param state - The state in which the address resides.
	 * @param country - The country in which the address resides.
	 * @return A response object from Swell.
	 */
	public ApiDataObject addAddress(String account_id, String address_1, String address_2, String city, String state, String country, String postcode) {	
		
		ApiDataObject body = new ApiDataObject();
	
		body.put("address1", address_1);
		body.put("address2", address_2);
		body.put("city", city);
		body.put("state", state);
		body.put("zip", postcode);
		body.put("country", country);
		
		ApiDataObject response = connection.post("/accounts/" + account_id + "/addresses" , body);
		
		if (response.getDataObject("$data").get("errors") != null) {
			System.out.println(response.getDataObject("$data").getDataObject("errors").toString());
		}
		
	
		return response;
	}
	
	
	/**
	 * 
	 * Makes an address the default address for a customer.
	 * 
	 * @param account_id - The ID of the customer.
	 * @param address_id - The address id of the customer.
	 * @return A response object from Swell.
	 */
	public ApiDataObject makeDefaultAddress(String account_id, String address_id) {	
		
		ApiDataObject body = new ApiDataObject();
		ApiDataObject data = new ApiDataObject();
		
		data.put("account_address_id", address_id);
		body.put("shipping", data);
		
		ApiDataObject response = connection.put("/accounts/" + account_id, body);
		
		
		if (response.getDataObject("$data").get("errors") != null) {
			System.out.println(response.getDataObject("$data").getDataObject("errors").toString());
		}
		
		
		
 		return response;
	}
	
	
	/**
	 * TODO: Finish create customer function.
	 * @param first_name
	 * @param last_name
	 * @param email
	 * @param mobile
	 * @return
	 */
	public ApiDataObject createAccount(String first_name, String last_name, String email, String phone) {
		ApiDataObject body  = new ApiDataObject();
		
		body.put("email", email);
		body.put("first_name", first_name);
		body.put("last_name", last_name);
		body.put("phone", phone);	
		
		ApiDataObject response = connection.post("/accounts", body);
		
		return response;		
	}
	
	
	/**
	 * @return A number of how many registered customer accounts there are.
	 */
	public long getAccountCount() {
		// Gets the count of how many accounts we can pull.
		ApiDataObject count_query = connection.get("/accounts?limit=1");
			
		
		// If it returns a null (meaning the socket probably died) we retry.
		if (count_query == null) {
			return getAccountCount();
		}
					
		// Returns how many accounts we can fetch.
		long count = (long) ((ApiDataObject) count_query.get("$data")).get("count");

		System.out.println("count: " + count);
		
		return count;
	}
	
	
	/**
	 * Fetches all accounts from Swell and returns as an Array List.
	 * @return
	 */
	public List<?> getAllAccounts() throws InterruptedException {
		return getAllAccounts(SwellConfig.FETCH_LIMIT);
	}
	
	
	/**
	 * Fetches all accounts from Swell and returns as an Array List.
	 * 
	 * @param limit
	 * @return
	 */
	public List<ApiDataObject> getAllAccounts(int limit) throws InterruptedException {

		// Checks if the limit input is more than the fetch limit, if it is then it is
		// automatically set
		// to the maximum limit.
		if (limit > SwellConfig.FETCH_LIMIT) {
			limit = SwellConfig.FETCH_LIMIT;
		}

		if (limit <= 0) {
			limit = 25;
		}

		
		// Gets the count of how many accounts we can pull.
		ApiDataObject count_query = connection.get("/accounts?limit=1");
		
		// If it returns a null (meaning the socket probably died) we retry.
		if (count_query == null) {
			return getAllAccounts(limit);
		}

	
		// Returns how many accounts we can fetch.
		double count = (long) ((ApiDataObject) count_query.get("$data")).get("count");

		System.out.println("count: " + count);

		
		System.out.println("Preparing account data...");
		
		// Initializes an array that we will store the accounts in.
		List<ApiDataObject> data = new ArrayList<ApiDataObject>();

		// How many pages we cycle through to compile the data from.
		int pages = (int) Math.ceil(count / limit);

		System.out.println("pages: " + pages);
		
		CountDownLatch latch = new CountDownLatch(pages);
		
		ExecutorService executor = Executors.newFixedThreadPool(pages);
		
		// Loops through the pages and fetches the accounts from swell.
		for (int i = 1; i < pages + 1; i++) {
			
			final int page = i;
			final int page_limit = limit;
			
			executor.submit(() -> {
				System.out.println("Getting data for page: " + page);
				
				// Queries swell for the accounts from the page <i>.
				ApiDataObject query = connection.get("/accounts?limit=" + page_limit + "&page=" + page);
				
				// Grabs the data array from the query.
				JSONArray fetched_data = (JSONArray) ((ApiDataObject) query.get("$data")).get("results");

				// Loops through the data array and adds the accounts to the fetched_data array.
				for (int ii = 0; ii < fetched_data.size(); ii++) {
					data.add(JsonDataParser.createApiDataObject((JSONObject) fetched_data.get(ii)));
				}
				
				latch.countDown();

			});
									
		}
		
		latch.await();

		System.out.println("Captured Data size: " + data.size());

		// Returns the data array.
		return data;

	}
	
	
	public ApiDataObject generatePasswordToken(String email) throws NoSuchAlgorithmException {

		ApiDataObject body = new ApiDataObject();

		String token = CryptUtil.generateToken();

		ApiDataObject response = new ApiDataObject();

		if (token != null) {
			body.put("password_token", token);
			ApiDataObject verify_account = connection.get("/accounts/" + email);
			System.out.println(verify_account.toString());
			if (verify_account.get("success") != null) {
				return verify_account;
			}

			ApiDataObject result = connection.put("/accounts/" + email, body);

			if (result.get("id") != null) {
				System.out.println(result.toString());
				response.put("password_token", token);
			} else {
				response.put("success", false);
				response.put("message", "Unauthorized to generate token.");
			}

		} else {

			response.put("success", false);
			response.put("message", "Failed to generate token.");

		}

		return response;

	}

	
}

