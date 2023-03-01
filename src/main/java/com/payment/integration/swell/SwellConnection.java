package com.payment.integration.swell;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.SocketException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HashMap;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.stereotype.Component;

import com.payment.configuration.SwellConfig;
import com.payment.util.ApiDataObject;
import com.payment.util.JsonDataParser;
import com.payment.util.RequestType;


/**
 * 
 * An SSL Socket client that connects & communicates with the back-end of Swell.
 * 
 * @author Oska Jory <oska@excede.com.au>
 * 
 * TODO: Add request caching.
 * 
 */
@Component
public class SwellConnection {
	
	
	// The connection configurations for swell.
	private SwellConfig config;
	
	
	// The socket used to connect to swell.
	private SSLSocket socket;
	
	
	public static final String quote = "\"", comma = ",";;
	
	
	// Establishes a connection to swell when constructed.
	public SwellConnection(SwellConfig config) {
		this.config = config;
		this.socket = establishConnection();			
		if (socket.isConnected()) 
			System.out.println("Successfully connected to swell.");
				
				
	}
	
	
	/**
	 * Constructs a new SSL Socket connection to Swell.
	 * @return A socket connection to the host and port.
	 * @throws NoSuchAlgorithmException
	 * @throws KeyManagementException
	 * @throws IOException
	 */
	public SSLSocket establishConnection() {
		
		try {
			SSLContext sslContext = SSLContext.getInstance("TLS");
			
			sslContext.init(null, null, new SecureRandom());
	
			// Create a socket factory that trusts all certificates
			SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
	
			// Create a socket and connect to the server
			SSLSocket socket = (SSLSocket) sslSocketFactory.createSocket(InetAddress.getByName(config.getHost()), config.getPort());
	
			SSLParameters sslParams = new SSLParameters();
			
			sslParams.setEndpointIdentificationAlgorithm("HTTPS");
			socket.setSSLParameters(sslParams);
	
			// Socket options.
			socket.setTcpNoDelay(true);
			socket.setKeepAlive(true);
	
			if (!socket.isConnected())			
				System.out.println("Socket failed to connect.");
	
			return socket;
		
		} catch (KeyManagementException | NoSuchAlgorithmException | IOException e) {
			System.out.println("Failed to connect to swell.");
			e.printStackTrace();
		}
		
		
		return null;
	}
	
	
	/**
	 * 
	 * Constructs the request into a format readable by the Swell Server Protocol.
	 * 
	 * @param method - The method that the request is making to, (GET, POST, PUT, DELETE) ?
	 * @param path - The path to the 
	 * @param body - The body of data as a String in JSON format
	 * @return A constructed string holding the request being made to the server.
	 */
	private String constructRequest(String method, String path, String body) {
		return "[" + quote + method.toLowerCase() + quote + ", " + quote + path.replaceAll("\n", "") + quote + ", " + body
				+ "]";
	}
	
	
	/*
	 * Constructs a HTTP request and returns a JSON Object response.
	 * 
	 * @param type      - The type of request that is being made (GET, POST, PUT,
	 *                  DELETE).
	 * @param path      - The path that the request is being made to on the host
	 *                  e.g. "/example".
	 * @param variables - A HashMap storing the key and value pairs of any query
	 *                  parameter variables.
	 * @param body      - A HashMap storing the key and value pairs of any body data
	 *                  that will be sent with the request.
	 * @return {@link ApiDataObject} - A JSON response from the end-point.
	 */
	public ApiDataObject request(RequestType type, String path, HashMap<String, String> variables,
			ApiDataObject body) {
		return writeRequest(type.name().toLowerCase(), path, body);
	}
	
	
	/**
	 *
	 * Writes a request to the Swell Server via a SSL Socket pipeline request.
	 * 
	 * @param socket - The socket being written to.
	 * @param type - The type of request i.e (get, post, put, delete)
	 * @param path - The API url path.
	 * @param data - The data being sent with the request Key:Value HashMap.
	 * @param public_key - The authentication public key.
	 * @param private_key - The authentication client secret.
	 * @return JSONObject - The response from the server.
	 * @throws UnsupportedEncodingException 
	 * @throws IOException - Will likely be thrown if the connection is closed or the pipeline is broken.
	 * @throws ParseException - Will likely be thrown if the buffer has been exceeded. 
	 * @throws NoSuchAlgorithmException 
	 * @throws KeyManagementException 
	 */
	private ApiDataObject writeRequest(String type, String path, ApiDataObject data) {		
		
		// The generic error message that will be sent if there is an error.
		String error_message = "null";
		
		try {
			
			// Opens a new socket connection if existing one is closed or doesn't exist..
			if (socket == null || (socket != null && socket.isClosed())) 
				socket = establishConnection();
			
			// Initializes the data options if there are none.
			data = data == null ? new ApiDataObject() : data;
			
			// Inserts the public key (the store id) and secret 
			// authentication keys into the request options.
			data.put("$client", config.getStoreId());
			data.put("$key", config.getSecretKey());
			
			// Constructs the options into a readable JSON format.
			String options = data.toString();

			// Constructs the request into swell's readable protocol format [type, path, options].
			String request = constructRequest(type, path, options);
			
			// Creates an output stream writer to write to the socket stream.
			OutputStreamWriter out = new OutputStreamWriter(socket.getOutputStream(), "UTF-8");
			
			// Outputs the request for debugging.
			System.out.println(request);
			
			// Writes the request to the pipeline
			out.write(request + "\n");
			
			// Submits the request.
			out.flush();				
			
			// Creates a stream reader so we can read a response from the server in a buffered reader.
			BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			
			// Reads the response from the server to a String. 
			// The data received from the Swell server is a single line JSON object otherwise null.
			String serverResponseData = reader.readLine();
			
			// Logs the server response for debugging.
			System.out.println(serverResponseData);
			
			
			// If the response received was null, it means there was a syntax error.
			if (serverResponseData == null || serverResponseData.equalsIgnoreCase("null")) {
				
				ApiDataObject error_response = new ApiDataObject();
				error_response.put("success", false);
				error_response.put("message", "null received from server, syntax error in request.");
				
				socket.close();
				
				return error_response;
				
			} else {
			
				// Creates a JSON parser so we can parse the data received as JSON.
				JSONParser parser = new JSONParser();
				
				// Constructs a JSON response to return.
				JSONObject json_response = (JSONObject) parser.parse(serverResponseData);		
				
				ApiDataObject response = JsonDataParser.parse(json_response.toJSONString());			
				
				// Closes the socket once the connection is complete.
				socket.close();
			
				// Returns the response from the server.
				return response;
			
			}
			
		} catch (SocketException | SSLHandshakeException e) {
								
			if (e.getMessage().equalsIgnoreCase("connection reset") || e.getMessage().equalsIgnoreCase("Remote host terminated the handshake")) 
				return writeRequest(type, path, data);
			else 
				error_message = e.getMessage() + " - Request syntax error.";
				
		} catch (IOException | ParseException  e) {
			
			error_message = e.getMessage();
			e.printStackTrace();
			
		}
			
		ApiDataObject error_response = new ApiDataObject();
		
		error_response.put("success", false);
		error_response.put("message", error_message);
			
		return error_response;		
	}
	
	
	public SwellConfig getConfig() {
		return config;
	}
	
	
	/**
	 * Creates a GET request to an end-point returning a JSON response.
	 * 
	 * @param path - The end-point URL path.
	 * @return {@link ApiDataObject} - A JSON response from the end-point.
	 */
	public ApiDataObject get(String path) {
		return request(RequestType.GET, path, null, null);
	}

	
	/**
	 * Creates a POST request to an end-point returning a JSON response.
	 * 
	 * @param path      - The end-point URL path.
	 * @param variables - A key value HashMap storing Query parameters.
	 * @param body      - A key value HashMap storing body data.
	 * @return {@link ApiDataObject} - A JSON response from the end-point.
	 */
	public ApiDataObject post(String path, HashMap<String, String> variables, ApiDataObject body) {
		return request(RequestType.POST, path, variables, body);
	}

	
	/**
	 * Creates a POST request to an end-point returning a JSON response.
	 * 
	 * @param path      - The end-point URL path.
	 * @param variables - A key value HashMap storing Query parameters.
	 * @param body      - A key value HashMap storing body data.
	 * @return {@link ApiDataObject} - A JSON response from the end-point.
	 */
	public ApiDataObject post(String path, ApiDataObject body) {
		return request(RequestType.POST, path, null, body);
	}

	
	/**
	 * Creates a PUT request to an end-point returning a JSON response.
	 * 
	 * @param path      - The end-point URL path.
	 * @param variables - A key value HashMap storing Query parameters.
	 * @param body      - A key value HashMap storing body data.
	 * @return {@link ApiDataObject} - A JSON response from the end-point.
	 */
	public ApiDataObject put(String path, HashMap<String, String> variables, ApiDataObject body) {
		return request(RequestType.PUT, path, variables, body);
	}

	
	/**
	 * Creates a PUT request to an end-point returning a JSON response.
	 * 
	 * @param path - The end-point URL path.
	 * @param body - A key value HashMap storing body data.
	 * @return {@link ApiDataObject} - A JSON response from the end-point.
	 */
	public ApiDataObject put(String path, ApiDataObject body) {
		return request(RequestType.PUT, path, null, body);
	}

	
	/**
	 * Creates a DELETE request to an end-point returning a JSON response.
	 * 
	 * @param path      - The end-point URL path.
	 * @param variables - A key value HashMap storing Query parameters.
	 * @return {@link ApiDataObject} - A JSON response from the end-point.
	 */
	public ApiDataObject delete(String path, HashMap<String, String> variables) {
		return request(RequestType.DELETE, path, variables, null);
	}
	
	
	/**
	 * Creates a DELETE request to an end-point returning a JSON response.
	 * 
	 * @param path      - The end-point URL path.
	 * @param variables - A key value HashMap storing Query parameters.
	 * @param body - A key value HashMap storing the body of the request.
	 * @return {@link ApiDataObject} - A JSON response from the end-point.
	 */
	public ApiDataObject delete(String path, HashMap<String, String> variables, ApiDataObject body) {
		return request(RequestType.DELETE, path, variables, body);
	}

	
	/**
	 * Creates a POST request to an end-point returning a JSON response.
	 * 
	 * @param path - The end-point URL path.
	 * @return {@link ApiDataObject} - A JSON response from the end-point.
	 */
	public ApiDataObject delete(String path) {
		return delete(path, null);
	}
	
}