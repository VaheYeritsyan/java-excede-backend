### Java Spring template project

This project is based on a GitLab [Project Template](https://docs.gitlab.com/ee/gitlab-basics/create-project.html).

Improvements can be proposed in the [original project](https://gitlab.com/gitlab-org/project-templates/spring).

### CI/CD with Auto DevOps

This template is compatible with [Auto DevOps](https://docs.gitlab.com/ee/topics/autodevops/).

If Auto DevOps is not already enabled for this project, you can [turn it on](https://docs.gitlab.com/ee/topics/autodevops/#enabling-auto-devops) in the project settings.




# Swell Connection

### Connecting to Swell Backend
Swell’s backend can be connected via creating a TLS Socket connection. They use a custom protocol to receive requests and send responses. Currently we only generate a self signed instance of a SSLSocketContext to connect, as their own native npm package does the same in nodejs. 

The swell socket must connect to:

```console
  host: "api.swell.store"
  port: 8443 
 ```

### Requests to Swell

Requests being made to Swell must be a single line String that holds an array that holds a String (type - ie get, post, put, delete), a String (url path - ie “/accounts”), a JSON object (the body - ie  the data being sent / authentication). 

Please note that all requests can be referenced from the backend documentation for guidance on the requirements for constructing a request for the target API:
https://developers.swell.is/backend-api/introduction

The request must follow the following syntax.

```console
request = "[String, String, JSON]"

[“<type>”, “<path>”, {$key: “<secret>”, $client: “<store-id>”, … }]
```
The request must be constructed as follows:

```java
// request = "[\"String\", \"String\", JSON]";
String request = "[\“<type>\”, \“<path>\”, {\"$key\": \“<secret>\”, \"$client\": \“<store-id>\”, … }]";

// The \n appended to the request marks a new line 
// to acknowledge the end of our request.
socket.write(request + “\n”);
socket.flush();
```

All requests must have the following keys in the JSON body data: 

```console
$key: <secret> (The secret key of your store)
$client: <store-id> (The id of your store)
```
An example GET request:

```console
[“get”, “/account/test@gmail.com”, 
{$key: “test-store”, $client: “eopr2f930jf2903jfsdfjs09j”}]
```
URL parameters can also be passed through the path:

```console
[“get”, “/accounts?limit=10”, {$key: “test-store”, $client: “eopr2f930jf2903jfsdfjs09j”}]
```
Here is an example of a POST request.

```console
[“post”, “/accounts”, {$key: “test-store”, $client: “eopr2f930jf2903jfsdfjs09j”, email:"test@excede.com.au", first_name: "john", last_name:"doe", phone:"+61435125659"}]
```
The body being sent with the request would follow this structure: 

```json
{
	$key: “test-store”, 
	$client: “eopr2f930jf2903jfsdfjs09j”,
	email: "test@excede.com.au", 
	first_name: "john", 
	last_name: "doe", 
	phone: "+61435125659"
}
```
 
### Response from Swell

Most data requested from Swell will be sent in a JSON object and contain the following data key:


```json
// A single result.
$data: {
	email:"example@test.com"
	...
}


// An array of results.
$data: {
	results: [
		{email:"example@test.com"},
		{...},
		...
]
}

// A an empty result. (Not found)
$data: null
```

An error response will hold an $error key in the body of the response. Here is an example error response:

```json
{
	"$error":"Resource not found /accountss/test",
	"$status":404,
	"$time":197
}
```


Here is an example successful response from a GET request of fetching a particular account:

```json
{
   "$time": 262,
   "$links": {
       "addresses": {
           "url": "/accounts:addresses?parent_id={id}"
       },
       "subscriptions": {
           "url": "/subscriptions?account_id={id}"
       },
       "cards": {
           "url": "/accounts:cards?parent_id={id}"
       },
       "carts": {
           "url": "/carts?account_id={id}"
       },
       "shipping": {
           "links": {
               "account_address": {
                   "url": "/accounts:addresses/{account_address_id}"
               }
           }
       },
       "invoices": {
           "url": "/invoices?account_id={id}"
       },
       "credits": {
           "url": "/accounts:credits?parent_id={id}"
       },
       "contact": {
           "url": "/contacts/{contact_id}"
       },
       "orders": {
           "url": "/orders?account_id={id}"
       },
       "contacts": {
           "url": "/contacts?account_id={id}"
       },
       "billing": {
           "links": {
               "account_card": {
                   "url": "/accounts:cards/{account_card_id}"
               }
           }
       }
   },
   "$data": {
       "email_optin": false,
       "date_updated": "2023-02-22T21:14:09.676Z",
       "order_count": 1,
       "date_created": "2023-02-22T21:13:10.881Z",
       "last_name": "Drew",
       "type": "individual",
       "billing": {
           "zip": "95965",
           "country": "US",
           "address2": null,
           "city": "Oroville",
           "address1": "45 Cabana Dr",
           "name": "Morgan Drew",
           "last_name": "Drew",
           "state": "AL",
           "account_card_id": "63f6859c46dd21001303f6b5",
           "first_name": "Morgan",
           "card": {
               "last4": "4242",
               "cvc_check": "unchecked",
               "address_check": "unchecked",
               "zip_check": "unchecked",
               "exp_month": 12,
               "exp_year": 2033,
               "brand": "Visa",
               "token": "card_VsYZlPrv0GhsyFAVrr881OTG"
           }
       },
       "date_last_order": "2023-02-22T21:14:05.510Z",
       "balance": 0,
       "shipping": {
           "zip": "95965",
           "account_address_id": "63f6859b46dd21001303f6b4",
           "country": "US",
           "address2": null,
           "city": "Oroville",
           "address1": "45 Cabana Dr",
           "name": "Morgan Drew",
           "last_name": "Drew",
           "state": "AL",
           "first_name": "Morgan"
       },
       "date_first_order": "2023-02-22T21:14:05.510Z",
       "name": "Morgan Drew",
       "currency": "USD",
       "id": "63f68566ffc21200129bbc0e",
       "first_name": "Morgan",
       "email": "mrabuzz@me.com",
       "order_value": 673
   },
   "$status": 200,
   "$url": "/accounts/mrabuzz@me.com",
   "$collection": "com.accounts"
}
```

### Limitations

Swell has a few limitations when it comes to connecting. Holding a connection tends to timeout when reusing the same connection, so I have found you can make more requests and less chance of timeouts from queueing by closing the connection when finished requesting.

Swell has a fetch limit of 1000 records. So when fetching a list of data, the maximum you can fetch simultaneously is 1000 in one request. This can be overcomed by batching the request. The request normally comes with a pages count and a record per page count, so you can quite easily 

The following code can be referenced is a from com.payment.integration.swell.service:243

Here is a demonstration of how you can by-pass swell’s limit of 1000 data rows. It is advised to put a limit on the number of threads the threadpool opens, as it can shutdown the application if there are too many threads opened. Keep it to a minimum and let the threadpool execute. You should only need up to 25 threads simultaneously.

```java
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
```
