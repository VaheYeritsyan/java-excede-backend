package com.payment.service.swell;

import com.payment.configuration.SwellConfig;
import com.payment.exception.classes.CustomerNotFoundException;
import com.payment.integration.swell.SwellConnection;
import com.payment.integration.swell.dto.SwellCustomer;
import com.payment.util.ApiDataObject;
import com.payment.util.CryptUtil;
import com.payment.util.JsonDataParser;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * An API into Swells back-end account functionality.
 *
 * @author Oska Jory <oska@excede.com.au>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SwellAccountService {
    private final SwellConnection connection;
    private static final String ACCOUNT_ENDPOINT = "/accounts/";
    private static final String DATA_FIELD = "$data";

    /**
     * Fetches a specified account from swell.
     *
     * @param id - The ID of the account (Can also be an email address).
     * @return The targeted account from swell.
     */
    public ApiDataObject getAccount(String id) {
        ApiDataObject account = connection.get(ACCOUNT_ENDPOINT + id);
        ApiDataObject accountDetails = account.getDataObject(DATA_FIELD);
        if (accountDetails == null) {
            throw new CustomerNotFoundException(id);
        }
        return account;
    }

    /**
     * Delete an individual Customer from swell.
     *
     * @param id - The id of the account we are deleting.
     * @return A response {@link ApiDataObject} stating if the request was successful or not.
     */
    public ApiDataObject deleteAccount(String id) {
        ApiDataObject response = new ApiDataObject();

        ApiDataObject body = new ApiDataObject();

        body.put("id", id);
        body.put("$force_delete", true);

        ApiDataObject apiResponse = connection.delete(ACCOUNT_ENDPOINT + id, body);

        if (apiResponse.get(DATA_FIELD) == null) {
            String errorMessage = "Failed to delete: " + id + " or account does not exist.";
            log.error(errorMessage);
            throw new IllegalStateException(errorMessage);
        } else {
            response.put("success", true);
        }
        return response;
    }


    /**
     * Adds a payment method to the customer on
     *
     * @param accountId
     * @param gateway
     * @param gatewayCustomerId
     * @param token
     */
    public ApiDataObject addPaymentMethod(String accountId, String gateway, String gatewayCustomerId, String token) {
        ApiDataObject body = new ApiDataObject();
        body.put("id", accountId);
        body.put("gateway", gateway);
        body.put("gateway_customer", gatewayCustomerId);
        body.put("token", token);
        body.put("$vault", "true");
        body.put("active", "true");
        return connection.post(ACCOUNT_ENDPOINT + accountId + "/cards", body);
    }


    /**
     * Makes a specified payment method the default payment method for a customer.
     *
     * @param accountId       - The Swell account ID.
     * @param paymentMethodId - The id of the payment method stored in Swell.
     * @return A response object from Swell.
     * <p>
     * <p>
     * Swell Default Card Body structure
     * <p>
     * {
     * billing: {
     * account_card_id: "<card_id>"
     * }
     * }
     */
    public ApiDataObject makeDefaultPaymentMethod(String accountId, String paymentMethodId) {
        ApiDataObject body = new ApiDataObject();
        ApiDataObject billing = new ApiDataObject();
        billing.put("account_card_id", paymentMethodId);
        body.put("billing", billing);
        return connection.put(ACCOUNT_ENDPOINT + accountId, body);
    }


    /**
     * Creates a new address for a customer in Swell.
     *
     * @param accountId - The ID of the customer we are creating the address for.
     * @param address1  - The address line 1.
     * @param address2  - The address line 2.
     * @param city      - The city of the address.
     * @param state     - The state in which the address resides.
     * @param country   - The country in which the address resides.
     * @return A response object from Swell.
     */
    public ApiDataObject addAddress(String accountId, String address1, String address2, String city, String state, String country, String postcode) {
        ApiDataObject body = new ApiDataObject();
        body.put("address1", address1);
        body.put("address2", address2);
        body.put("city", city);
        body.put("state", state);
        body.put("zip", postcode);
        body.put("country", country);
        ApiDataObject response = connection.post(ACCOUNT_ENDPOINT + accountId + "/addresses", body);
        if (response.getDataObject(DATA_FIELD).get("errors") != null) {
            log.info(response.getDataObject(DATA_FIELD).getDataObject("errors").toString());
        }
        return response;
    }


    /**
     * Makes an address the default address for a customer.
     *
     * @param accountId - The ID of the customer.
     * @param addressId - The address id of the customer.
     * @return A response object from Swell.
     */
    public ApiDataObject makeDefaultAddress(String accountId, String addressId) {
        ApiDataObject body = new ApiDataObject();
        ApiDataObject data = new ApiDataObject();
        data.put("account_address_id", addressId);
        body.put("shipping", data);
        ApiDataObject response = connection.put(ACCOUNT_ENDPOINT + accountId, body);
        if (response.getDataObject(DATA_FIELD).get("errors") != null) {
            log.info(response.getDataObject(DATA_FIELD).getDataObject("errors").toString());
        }
        return response;
    }


    /**
     * TODO: Finish create customer function.
     *
     * @param firstName
     * @param lastName
     * @param email
     * @return
     */
    public ApiDataObject createAccount(String firstName, String lastName, String email, String phone) {
        ApiDataObject body = new ApiDataObject();
        body.put("email", email);
        body.put("first_name", firstName);
        body.put("last_name", lastName);
        body.put("phone", phone);
        return connection.post("/accounts", body);
    }


    public ApiDataObject createAccount(SwellCustomer customer) {
        return this.createAccount(customer.getFirstName(), customer.getLastName(), customer.getEmail(), customer.getPhone());
    }


    /**
     * @return A number of how many registered customer accounts there are.
     */
    public long getAccountCount() {
        ApiDataObject countQuery = connection.get("/accounts?limit=1");
        if (countQuery == null) {
            return getAccountCount();
        }
        long count = (long) (countQuery.getDataObject(DATA_FIELD)).get("count");
        log.info("count: " + count);

        return count;
    }


    /**
     * Fetches all accounts from Swell and returns as an Array List.
     *
     * @return
     */
    public List<ApiDataObject> getAllAccounts() throws InterruptedException {
        return getAllAccounts(SwellConfig.FETCH_LIMIT);
    }


    /**
     * Fetches all accounts from Swell and returns as an Array List.
     *
     * @param limit - The limit of accounts per page fetched from
     *              swell.
     */
    public List<ApiDataObject> getAllAccounts(int limit) throws InterruptedException {
        if (limit > SwellConfig.FETCH_LIMIT) {
            limit = SwellConfig.FETCH_LIMIT;
        }
        if (limit <= 0) {
            limit = 25;
        }
        ApiDataObject countQuery = connection.get("/accounts?limit= 1");
        if (countQuery == null || !((boolean) countQuery.get("success"))) {
            return Collections.emptyList();
        }
        double count = (long) (countQuery.getDataObject(DATA_FIELD)).get("count");
        List<ApiDataObject> data = new ArrayList<>();
        int pages = (int) Math.ceil(count / limit);
        CountDownLatch latch = new CountDownLatch(pages);
        ExecutorService executor = Executors.newFixedThreadPool(pages);
        for (int i = 1; i < pages + 1; i++) {
            final int page = i;
            final int page_limit = limit;

            executor.submit(() -> {
                ApiDataObject query = connection.get("/accounts?limit=" + page_limit + "&page=" + page);
                JSONArray fetchedData = (JSONArray) ((ApiDataObject) query.get(DATA_FIELD)).get("results");
                for (Object fetchedDatum : fetchedData) {
                    data.add(JsonDataParser.createApiDataObject((JSONObject) fetchedDatum));
                }
                latch.countDown();
            });
        }
        latch.await();
        return data;
    }


    /**
     * Generates a password token for a customer.
     *
     * @param email - The identifying email of the customer account.
     * @return {@linkplain ApiDataObject}
     */
    @SneakyThrows
    public ApiDataObject generatePasswordToken(String email) {
        ApiDataObject body = new ApiDataObject();
        String token = CryptUtil.generateToken();
        ApiDataObject response = new ApiDataObject();
        body.put("password_token", token);
        ApiDataObject verifyAccount = getAccount(email);
        log.info(verifyAccount.toString());
        if (verifyAccount.get(DATA_FIELD) == null) {
            return response.put("success", false).put("message", "Account does not exist.");
        } else {
            ApiDataObject result = connection.put(ACCOUNT_ENDPOINT + email, body);
            if (result.get(DATA_FIELD) == null) {
                String errorMessage = "Unauthorized to generate token.";
                log.error(errorMessage);
                throw new IllegalStateException(errorMessage);
            }
            log.info(result.toString());
            response.put("passwordToken", token);
        }
        return response;
    }
}

