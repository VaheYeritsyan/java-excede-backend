package com.payment.service.swell;

import com.payment.configuration.SwellConfig;
import com.payment.integration.swell.SwellConnection;
import com.payment.util.ApiDataObject;
import com.payment.util.JsonDataParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * An API into Swell's order backend functionality.
 *
 * @author Oska Jory <oska@excede.com.au>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SwellOrderService {

    private final SwellConnection connection;
    private static final String DATA_FIELD = "$data";

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
     *
     * @param id - The id of the order we are deleting.
     * @return A response {@link ApiDataObject} stating if the request was successful or not.
     */
    public ApiDataObject deleteOrder(String id) {
        ApiDataObject response = new ApiDataObject();
        ApiDataObject body = new ApiDataObject();
        body.put("id", id);
        ApiDataObject apiResponse = connection.delete("/orders/" + id, body);
        if (apiResponse.get(DATA_FIELD) == null) {
            String errorMessage = "Failed to delete order with id " + id;
            log.error(errorMessage);
            throw new IllegalStateException(errorMessage);
        } else {
            response.put("success", true);
        }
        return response;
    }

    /**
     * Fetches all orders from Swell and returns as an Array List.
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
        limit = setMaxOrDefaultLimit(limit);
        ApiDataObject countQuery = connection.get("/orders?limit=1");
        if (countQuery == null) {
            return getAllOrders(limit);
        }
        double count = (long) ((ApiDataObject) countQuery.get(DATA_FIELD)).get("count");
        log.info("count: " + count);
        log.info("Preparing order data...");
        List<ApiDataObject> data = new ArrayList<>();
        int pages = (int) Math.ceil(count / limit);
        log.info("pages: " + pages);
        CountDownLatch latch = new CountDownLatch(pages);
        ExecutorService executor = Executors.newFixedThreadPool(pages);
        for (int i = 1; i < pages + 1; i++) {
            final int page = i;
            final int page_limit = limit;
            executor.submit(() -> {
                log.info("Getting data for page: " + page);
                ApiDataObject query = connection.get("/orders?limit=" + page_limit + "&page=" + page);
                JSONArray fetchedData = (JSONArray) (query.getDataObject(DATA_FIELD))
                        .get("results");
                for (Object fetchedDatum : fetchedData) {
                    data.add(JsonDataParser.createApiDataObject((JSONObject) fetchedDatum));
                }
                latch.countDown();
            });
        }
        latch.await();
        log.info("Captured Data size: " + data.size());
        return data;
    }

    private static int setMaxOrDefaultLimit(int limit) {
        if (limit > SwellConfig.FETCH_LIMIT) {
            limit = SwellConfig.FETCH_LIMIT;
        }
        if (limit <= 0) {
            limit = 25;
        }
        return limit;
    }

    /**
     * @return A number of how many registered orders there are.
     */
    public long getOrderCount() {
        ApiDataObject countQuery = connection.get("/orders?limit=1");
        if (countQuery == null) {
            return getOrderCount();
        }
        long count = (long) countQuery.getDataObject(DATA_FIELD).get("count");
        log.info("count: " + count);
        return count;
    }
}
