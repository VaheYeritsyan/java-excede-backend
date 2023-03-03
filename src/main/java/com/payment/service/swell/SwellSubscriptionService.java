package com.payment.service.swell;

import com.payment.configuration.SwellConfig;
import com.payment.integration.swell.SwellConnection;
import com.payment.util.ApiDataObject;
import com.payment.util.DateUtility;
import com.payment.util.JsonDataParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * An API into Swell's back-end subscription functions.
 *
 * @author Oska Jory <oska@excede.com.au>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SwellSubscriptionService {
    private static final String DATA_FIELD = "$data";
    private final SwellConnection connection;

    /**
     * Fetches a specified subscription from swell.
     *
     * @param id - The ID of the subscription.
     * @return The targeted subscription from swell.
     */
    public ApiDataObject getSubscription(String id) {
        return connection.get("/subscriptions/" + id);
    }


    /**
     * Delete an individual subscription from swell.
     *
     * @param id - The id of the subscription we are deleting.
     * @return A response {@link ApiDataObject} stating if the request was successful or not.
     */
    public ApiDataObject deleteSubscription(String id) {
        ApiDataObject response = new ApiDataObject();
        ApiDataObject body = new ApiDataObject();
        body.put("id", id);
        ApiDataObject apiResponse = connection.delete("/subscriptions/" + id, body);
        if (apiResponse.get(DATA_FIELD) == null) {
            String errorMessage = "Failed to delete subscription " + id;
            log.error(errorMessage);
            throw new IllegalArgumentException(errorMessage);
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
        limit = getMaxLimitOrDefault(limit);
        ApiDataObject countQuery = connection.get("/subscriptions?limit=1");
        if (countQuery == null) {
            return getAllSubscriptions(limit);
        }
        double count = (long) ((ApiDataObject) countQuery.get(DATA_FIELD)).get("count");
        log.info("count: " + count);
        log.info("Preparing subscription data...");
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
                ApiDataObject query = connection.get("/subscriptions?limit=" + page_limit + "&page=" + page);
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

    private static int getMaxLimitOrDefault(int limit) {
        if (limit > SwellConfig.FETCH_LIMIT) {
            limit = SwellConfig.FETCH_LIMIT;
        }
        if (limit <= 0) {
            limit = 25;
        }
        return limit;
    }


    /**
     * Creates a subscription for a Swell customer.
     *
     * @param accountId          - The id of the Swell customer.
     * @param planId             - The Subscription plan id.
     * @param paymentMethodId    - The payment method ID from Swell used to renew the subscription.
     * @param productId          - The product id assosciated with the subscription plan.
     * @param nextPaymentDateUtc - The next payment date in an ISO date time string in UTC time.
     * @param qty                - The qty of this subscription product that it will renew.
     * @return A response object from Swell.
     */
    public ApiDataObject addSubscription(String accountId, String planId, String paymentMethodId, String productId, String nextPaymentDateUtc, int qty) {
        ApiDataObject body = new ApiDataObject();
        ApiDataObject billing = new ApiDataObject();
        ApiDataObject billingSchedule = new ApiDataObject();
        billing.put("account_card_id", paymentMethodId);
        billing.put("default", true);
        billing.put("use_account", true);
        billing.put("billing_schedule", billingSchedule);
        body.put("plan_id", planId);
        body.put("product_id", productId);
        body.put("account_id", accountId);
        body.put("quantity", qty);
        body.put("billing", billing);
        body.put("date_trial_end", nextPaymentDateUtc);
        return connection.post("/subscriptions", body);
    }


    /**
     * Add subscription with
     *
     * @param accountId
     * @param productId
     * @param freqType
     * @return
     */
    public ApiDataObject addSubscription(String accountId, String planId, String productId, String paymentMethodId, int qty,
                                         String freqType, LocalDate nextPaymentDate, int alignmentDay, String renewalHour, String renewalMinute, String timezone) {
        if (renewalMinute == null) {
            renewalMinute = "00";
        }
        if (renewalHour == null) {
            renewalHour = "00";
        }
        if (alignmentDay <= 0) {
            alignmentDay = 1;
        }
        if (timezone != null) {
            if (TimeZone.getTimeZone(ZoneId.of(timezone)) == null) {
                timezone = "Australia/Sydney";
            }
        } else {
            timezone = "Australia/Sydney";
        }
        if (Integer.parseInt(renewalMinute) > 59) {
            throw new IllegalArgumentException("The renewal minute must be no more than 59 mintues.");
        }
        if (Integer.parseInt(renewalHour) > 24) {
            throw new IllegalArgumentException("The renewal hour must be in 24 hour time and no more than 24.");
        }
        if (!freqType.equalsIgnoreCase("daily") && !freqType.equalsIgnoreCase("weekly")
                && !freqType.equalsIgnoreCase("yearly") && !freqType.equalsIgnoreCase("monthly")) {
            throw new IllegalArgumentException(
                    "Frequency type must be equal to either: daily, weekly, monthly or yearly.");
        }
        if (qty <= 0) {
            throw new IllegalArgumentException("Quantity must be equal to or more than 1.");
        }
        LocalDate newPaymentDate = DateUtility.getNearestDay(nextPaymentDate, alignmentDay);
        String dateString = DateUtility.timezoneToUTCStringFromCustomInput(newPaymentDate.toString(), renewalHour, renewalMinute, timezone);
        return addSubscription(accountId, planId, paymentMethodId, productId, dateString, qty);
    }

    /**
     * @return A number of how many registered subscriptions there are.
     */
    public long getSubscriptionCount() {
        ApiDataObject countQuery = connection.get("/subscriptions?limit=1");
        long count = (long) (countQuery.getDataObject(DATA_FIELD)).get("count");
        log.info("count: " + count);
        return count;
    }
}
