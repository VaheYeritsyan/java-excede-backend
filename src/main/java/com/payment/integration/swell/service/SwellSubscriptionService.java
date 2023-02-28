package com.payment.integration.swell.service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.stereotype.Service;

import com.payment.configuration.SwellConfig;
import com.payment.integration.swell.SwellConnection;
import com.payment.util.ApiDataObject;
import com.payment.util.DateUtility;
import com.payment.util.JsonDataParser;

import lombok.RequiredArgsConstructor;


/**
 * 
 * @author Oska Jory <oska@excede.com.au>
 * 
 */
@Service
@RequiredArgsConstructor
public class SwellSubscriptionService {

	
	private final SwellConnection connection;
	

	/**
	 * Fetches a specified subscription from swell.
	 * 
	 * @param id - The ID of the subscription.
	 * @return The targeted subscription from swell.
	 */
	public ApiDataObject getSubscription(String id) {
		ApiDataObject subscription = connection.get("/subscriptions/" + id);		
		return subscription;
	}
	
	
	/**
	 * Delete an individual subscription from swell.
	 * @param id - The id of the subscription we are deleting.
	 * @return A response {@link ApiDataObject} stating if the request was successful or not.
	 */
	public ApiDataObject deleteSubscription(String id) {
		ApiDataObject response = new ApiDataObject();
		
		ApiDataObject body = new ApiDataObject();
		
		body.put("id", id);
		ApiDataObject api_response = connection.delete("/subscriptions/" + id, null, body);
		
		if (api_response.get("$data") == null) {
			System.out.println("Failed to delete: " + id);
			response.put("success", false);
		} else {
			response.put("success", true);
		}
		
		return response;
	}
	
	
	/**
	 * Fetches all subscriptions from Swell and returns as an Array List.
	 * 
	 * @return
	 */
	public List<ApiDataObject> getAllSubscriptions() throws InterruptedException {
		return getAllSubscriptions(SwellConfig.FETCH_LIMIT);
	}

	
	
	
	/**
	 * Fetches all subscriptions from Swell and returns as an Array List.
	 * 
	 * @param limit
	 * @return
	 * @throws InterruptedException 
	 */
	public List<ApiDataObject> getAllSubscriptions(int limit) throws InterruptedException {

		// Checks if the limit input is more than the fetch limit, if it is then it is
		// automatically set
		// to the maximum limit.
		if (limit > SwellConfig.FETCH_LIMIT) {
			limit = SwellConfig.FETCH_LIMIT;
		}

		if (limit <= 0) {
			limit = 25;
		}

		// Gets the count of how many subscriptions we can pull.
		ApiDataObject count_query = connection.get("/subscriptions?limit=1");

		// If it returns a null (meaning the socket probably died) we retry.
		if (count_query == null) {
			return getAllSubscriptions(limit);
		}

		// Returns how many subscriptions we can fetch.
		double count = (long) ((ApiDataObject) count_query.get("$data")).get("count");

		System.out.println("count: " + count);

		System.out.println("Preparing subscription data...");

		// Initializes an array that we will store the subscriptions in.
		List<ApiDataObject> data = new ArrayList<ApiDataObject>();

		// How many pages we cycle through to compile the data from.
		int pages = (int) Math.ceil(count / limit);

		System.out.println("pages: " + pages);
		
		
		CountDownLatch latch = new CountDownLatch(pages);
		
		ExecutorService executor = Executors.newFixedThreadPool(pages);

		// Loops through the pages and fetches the subscriptions from swell.
		for (int i = 1; i < pages + 1; i++) {

			final int page = i;
			final int page_limit = limit;
			
			executor.submit(() -> {

				System.out.println("Getting data for page: " + page);

				// Queries swell for the subscriptions from the page <i>.
				ApiDataObject query = connection.get("/subscriptions?limit=" + page_limit + "&page=" + page);

				// Grabs the data array from the query.
				JSONArray fetched_data = (JSONArray) ((ApiDataObject) query.get("$data"))
						.get("results");

				// Loops through the data array and adds the subscriptions to the fetched_data array.
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

	
	/**
	 * 
	 * Creates a subscription for a Swell customer.
	 * 
	 * @param account_id - The id of the Swell customer.
	 * @param plan_id - The Subscription plan id.
	 * @param payment_method_id - The payment method ID from Swell used to renew the subscription.
	 * @param product_id - The product id assosciated with the subscription plan.
	 * @param next_payment_date_utc - The next payment date in an ISO date time string in UTC time.
	 * @param qty - The qty of this subscription product that it will renew. 
	 * @return A response object from Swell.
	 */
	public ApiDataObject addSubscription(String account_id, String plan_id, String payment_method_id, String product_id, String next_payment_date_utc, int qty) {

		ApiDataObject body = new ApiDataObject();
		ApiDataObject billing = new ApiDataObject();
		ApiDataObject billing_schedule = new ApiDataObject();

		// Billing information.
		billing.put("account_card_id", payment_method_id);
		billing.put("default", true);
		billing.put("use_account", true);
		billing.put("billing_schedule", billing_schedule);
		
		// Body data.
		body.put("plan_id", plan_id);
		body.put("product_id", product_id);
		body.put("account_id", account_id);
		body.put("quantity", qty);
		body.put("billing", billing);
		body.put("date_trial_end", next_payment_date_utc);

		return connection.post("/subscriptions", body);

	}
	
	
	/**
	 * Add subscription with 
	 * @param account_id
	 * @param product_id
	 * @param freq
	 * @param freq_type
	 * @return
	 */
	public ApiDataObject addSubscription(String account_id, String plan_id, String product_id, String payment_method_id, int qty,
			String freq_type, LocalDate next_payment_date, int alignment_day, String renewal_hour, String renewal_minute, String timezone) {

		if (renewal_minute == null) {
			renewal_minute = "00";
		}
		
		if (renewal_hour == null) {
			renewal_hour = "00";
		}
		
		if (alignment_day <= 0) {
			alignment_day = 1;
		}
		
		if (timezone != null) {
			
			if (TimeZone.getTimeZone(ZoneId.of(timezone)) == null) { 
				timezone = "Australia/Sydney";
			}
			
		} else {
			
			timezone = "Australia/Sydney";
			
		}

		
		if (Integer.parseInt(renewal_minute) > 59) {
			throw new IllegalArgumentException("The renewal minute must be no more than 59 mintues.");
		}
		
		
		if (Integer.parseInt(renewal_hour) > 24) {
			throw new IllegalArgumentException("The renewal hour must be in 24 hour time and no more than 24.");
		}

		if (!freq_type.equalsIgnoreCase("daily") && !freq_type.equalsIgnoreCase("weekly")
				&& !freq_type.equalsIgnoreCase("yearly") && !freq_type.equalsIgnoreCase("monthly")) {
			throw new IllegalArgumentException(
					"Frequency type must be equal to either: daily, weekly, monthly or yearly.");
		}

		if (qty <= 0) {
			throw new IllegalArgumentException("Quantity must be equal to or more than 1.");
		}

		LocalDate new_payment_date = DateUtility.getNearestDay(next_payment_date, alignment_day);
		
		String date_string = DateUtility.timezoneToUTCStringFromCustomInput(new_payment_date.toString(), renewal_hour, renewal_minute, timezone);
			
		return addSubscription(account_id, plan_id, payment_method_id, product_id, date_string, qty);
	}
	
	
	/**
	 * @return A number of how many registered subscriptions there are.
	 */
	public long getSubscriptionCount() {
		
		// Gets the count of how many subscriptions we can pull.
		ApiDataObject count_query = connection.get("/subscriptions?limit=1");	
		
		// If it returns a null (meaning the socket probably died) we retry.
		if (count_query == null) {
			return getSubscriptionCount();
		}
					
		// Returns how many subscriptions we can fetch.
		long count = (long) ((ApiDataObject) count_query.get("$data")).get("count");

		System.out.println("count: " + count);
		
		return count;
	}

}
