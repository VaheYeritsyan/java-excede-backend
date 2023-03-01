package com.payment.service.swell;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.payment.exception.classes.CustomerNotFoundException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.stereotype.Service;

import com.payment.configuration.SwellConfig;
import com.payment.integration.swell.SwellConnection;
import com.payment.integration.swell.dto.SwellCustomer;
import com.payment.util.ApiDataObject;
import com.payment.util.CryptUtil;
import com.payment.util.JsonDataParser;

import lombok.RequiredArgsConstructor;


/**
 *
 *
 * An API into Swells back-end account functionality.
 *
 * @author Oska Jory <oska@excede.com.au>
 *
 */
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
		ApiDataObject accountDetails = account.getDataObject("$data");
		if (accountDetails == null) {
			throw new CustomerNotFoundException(id);
		}
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
			response.put("success", false);
			response.put("message", "Failed to delete: " + id + " or account does not exist.");
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



	public ApiDataObject createAccount(SwellCustomer customer) {
		return this.createAccount(customer.getFirst_name(), customer.getLast_name(), customer.getEmail(), customer.getPhone());
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
	public List<ApiDataObject> getAllAccounts() throws InterruptedException {
		return getAllAccounts(SwellConfig.FETCH_LIMIT);
	}


	/**
	 * Fetches all accounts from Swell and returns as an Array List.
	 * @param limit - The limit of accounts per page fetched from
	 * swell.
	 */
	public List<ApiDataObject> getAllAccounts(int limit) throws InterruptedException {

	    // Ensures the page limit is valid.
	    if (limit > SwellConfig.FETCH_LIMIT) {
	        limit = SwellConfig.FETCH_LIMIT;
	    }

	    if (limit <= 0) {
	        limit = 25;
	    }

	    // Sends a query to swell.
	    ApiDataObject count_query = connection.get("/accounts?limit= 1");

	    // If the query was null, we fail the query.
	    if (count_query == null || (boolean) count_query.get("success") == false) {
	        return null;
	    }

	    // Returns how many accounts we can fetch.
	    double count = (long) ((ApiDataObject) count_query.get("$data")).get("count");

	    // Initializes an array that we will store the accounts in.
	    List<ApiDataObject> data = new ArrayList<ApiDataObject>();

	    // How many pages we cycle through to compile the
	    // data.
	    int pages = (int) Math.ceil(count / limit);

	    // Creates a synchronized countdown that safely
	    // count-down when a thread is closed.
	    // This will trigger a promise for us to wait until
	    // all threads are finished fetching the data before
	    // we proceed to the next step.
	    CountDownLatch latch = new CountDownLatch(pages);

	    // Creates a threadpool that will be used to simultaneously
	    // fetch data from swell. (There should be a smaller limit
	    // on the threadpool as it can be dangerous when fetching
	    // excessive amounts of data.
	    ExecutorService executor = Executors.newFixedThreadPool(pages);

	    // Loops through the pages and fetches the accounts from
	     // swell.
	    for (int i = 1; i < pages + 1; i++) {
	        final int page = i;
	        final int page_limit = limit;

	        executor.submit(() -> {

	            // Queries swell for the accounts from the page
	            // <i>.
	            ApiDataObject query = connection.get("/accounts?limit=" + page_limit + "&page=" + page);

	            // Grabs the data array from the query.
	            JSONArray fetched_data = (JSONArray) ((ApiDataObject) query.get("$data")).get("results");

	            // loops through the data array and adds the
	            // accounts to the fetched_data array.
	            for (int ii = 0; ii < fetched_data.size(); ii++) {
	                data.add(JsonDataParser.createApiDataObject((JSONObject) fetched_data.get(ii)));
	            }

	            latch.countDown();
	        });

	    }


	    // Waits till all the threads are finished.
	    latch.await();

	    // Returns the finished compiled list of accounts.
	    return data;
	}


	/**
	 * Generates a password token for a customer.
	 * @param email - The identifying email of the customer account.
	 * @return {@linkplain ApiDataObject}
	 */
	public ApiDataObject generatePasswordToken(String email) {

		ApiDataObject body = new ApiDataObject();
		try {

			// Generates a secure token approximately 16 characters long using Sha256.
			String token = CryptUtil.generateToken();

			ApiDataObject response = new ApiDataObject();

			if (token != null) {

				// Adds the password token to the request body for swell.
				body.put("password_token", token);

				// Verifies the account exists.
				ApiDataObject verify_account = getAccount(email);

				System.out.println(verify_account.toString());

				// If the account doesn't exist, return failed response.
				// If we don't do this step, the following method will go ahead and create an account for the email and set a token.
				if (verify_account.get("$data") == null) {
					return response.put("success", false).put("message", "Account does not exist.");

				} else {

					// Attempts to update the account with the password token.
					ApiDataObject result = connection.put("/accounts/" + email, body);

					if (result.get("$data") != null) {
						System.out.println(result.toString());
						response.put("passwordToken", token);
					} else {
						response.put("success", false);
						response.put("message", "Unauthorized to generate token.");
					}
				}

			} else {

				response.put("success", false);
				response.put("message", "Failed to generate token.");

			}

			return response;
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

		return new ApiDataObject().put("success", false).put("message", "Failed to generate password token, algorithm exception.");

	}


}

