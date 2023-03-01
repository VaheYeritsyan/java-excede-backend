package com.payment.service.swell;

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
import com.payment.util.JsonDataParser;

import lombok.RequiredArgsConstructor;


/**
 *
 * An API into Swell's order backend functionality.
 *
 * @author Oska Jory <oska@excede.com.au>
 *
 */
@Service
@RequiredArgsConstructor
public class SwellOrderService {

	private final SwellConnection connection;


	/**
	 * Fetches a specified order from swell.
	 *
	 * @param id - The ID of the order.
	 * @return The targeted order from swell.
	 */
	public ApiDataObject getOrder(String id) {
		return connection.get("/orders/" + id);
	}


	/**
	 * Delete an individual order from swell.
	 * @param id - The id of the order we are deleting.
	 * @return A response {@link ApiDataObject} stating if the request was successful or not.
	 */
	public ApiDataObject deleteOrder(String id) {
		ApiDataObject response = new ApiDataObject();

		ApiDataObject body = new ApiDataObject();

		body.put("id", id);
		ApiDataObject api_response = connection.delete("/orders/" + id, null, body);

		if (api_response.get("$data") == null) {
			System.out.println("Failed to delete: " + id);
			response.put("success", false);
		} else {
			response.put("success", true);
		}

		return response;
	}



	/**
	 * Fetches all orders from Swell and returns as an Array List.
	 *
	 * @return
	 */
	public List<ApiDataObject> getAllOrders() throws InterruptedException {
		return getAllOrders(SwellConfig.FETCH_LIMIT);
	}


	/**
	 * Fetches all orders from Swell and returns as an Array List.
	 *
	 * @param limit
	 * @return
	 */
	public List<ApiDataObject> getAllOrders(int limit) throws InterruptedException {

		// Checks if the limit input is more than the fetch limit, if it is then it is
		// automatically set
		// to the maximum limit.
		if (limit > SwellConfig.FETCH_LIMIT) {
			limit = SwellConfig.FETCH_LIMIT;
		}

		if (limit <= 0) {
			limit = 25;
		}

		// Gets the count of how many orders we can pull.
		ApiDataObject count_query = connection.get("/orders?limit=1");

		// If it returns a null (meaning the socket probably died) we retry.
		if (count_query == null) {
			return getAllOrders(limit);
		}

		// Returns how many orders we can fetch.
		double count = (long) ((ApiDataObject) count_query.get("$data")).get("count");

		System.out.println("count: " + count);

		System.out.println("Preparing order data...");

		// Initializes an array that we will store the orders in.
		List<ApiDataObject> data = new ArrayList<ApiDataObject>();

		// How many pages we cycle through to compile the data from.
		int pages = (int) Math.ceil(count / limit);

		System.out.println("pages: " + pages);

		CountDownLatch latch = new CountDownLatch(pages);

		ExecutorService executor = Executors.newFixedThreadPool(pages);

		for (int i = 1; i < pages + 1; i++) {

			final int page = i;
			final int page_limit = limit;

			executor.submit(() -> {
				System.out.println("Getting data for page: " + page);

				// Queries swell for the subscriptions from the page <i>.
				ApiDataObject query = connection.get("/orders?limit=" + page_limit + "&page=" + page);

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
	 * @return A number of how many registered orders there are.
	 */
	public long getOrderCount() {

		// Gets the count of how many orders we can pull.
		ApiDataObject count_query = connection.get("/orders?limit=1");

		// If it returns a null (meaning the socket probably died) we retry.
		if (count_query == null) {
			return getOrderCount();
		}

		// Returns how many orders we can fetch.
		long count = (long) ((ApiDataObject) count_query.get("$data")).get("count");

		System.out.println("count: " + count);

		return count;
	}
}
