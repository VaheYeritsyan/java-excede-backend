package com.payment.integration.swell;

import com.payment.configuration.SwellConfig;
import com.payment.util.ApiDataObject;
import com.payment.util.JsonDataParser;
import com.payment.util.RequestType;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.stereotype.Component;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;


/**
 * An SSL Socket client that connects & communicates with the back-end of Swell.
 *
 * @author Oska Jory <oska@excede.com.au>
 * <p>
 * TODO: Add request caching.
 */
@Component
@Slf4j
public class SwellConnection {
    public static final String QUOTE = "\"";
    private final SwellConfig config;
    private SSLSocket socket;

    public SwellConnection(SwellConfig config) {
        this.config = config;
        this.socket = establishConnection();
        if (socket.isConnected()) {
            log.info("Successfully connected to to swell");
        }
    }

    /**
     * Constructs a new SSL Socket connection to Swell.
     *
     * @return A socket connection to the host and port.
     */
    public SSLSocket establishConnection() {
        try {
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, null, new SecureRandom());
            SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
            SSLSocket tempSocket = (SSLSocket) sslSocketFactory.createSocket(InetAddress.getByName(config.getHost()), config.getPort());
            SSLParameters sslParams = new SSLParameters();
            sslParams.setEndpointIdentificationAlgorithm("HTTPS");
            tempSocket.setSSLParameters(sslParams);
            tempSocket.setTcpNoDelay(true);
            tempSocket.setKeepAlive(true);
            if (!tempSocket.isConnected()) {
                log.info("Swell already connected");
            }
            return tempSocket;
        } catch (KeyManagementException | NoSuchAlgorithmException |
                 IOException e) {
            log.error("Failed to connect to swell.", e);
            throw new IllegalStateException("Failed to connect to swell", e);
        }

    }

    /**
     * Constructs the request into a format readable by the Swell Server Protocol.
     *
     * @param method - The method that the request is making to, (GET, POST, PUT, DELETE) ?
     * @param path   - The path to the
     * @param body   - The body of data as a String in JSON format
     * @return A constructed string holding the request being made to the server.
     */
    private String constructRequest(String method, String path, String body) {
        return "[" + QUOTE + method.toLowerCase() + QUOTE + ", " + QUOTE + path.replace("\n", "") + QUOTE + ", " + body + "]";
    }

    public ApiDataObject request(RequestType type, String path, ApiDataObject body) {
        return writeRequest(type.name().toLowerCase(), path, body);
    }

    /**
     * Writes a request to the Swell Server via a SSL Socket pipeline request.
     *
     * @param type - The type of request i.e (get, post, put, delete)
     * @param path - The API url path.
     * @param data - The data being sent with the request Key:Value HashMap.
     * @return JSONObject - The response from the server.
     */
    private ApiDataObject writeRequest(String type, String path, ApiDataObject data) {
        try {
            if (socket == null || socket.isClosed()) {
                socket = establishConnection();
            }
            data = data == null ? new ApiDataObject() : data;
            data.put("$client", config.getStoreId());
            data.put("$key", config.getSecretKey());
            String options = data.toString();
            String request = constructRequest(type, path, options);
            OutputStreamWriter out = new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8);
            log.info("The request was: {}", request);
            out.write(request + "\n");
            out.flush();
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String serverResponseData = reader.readLine();
            log.info("The response was: {}", serverResponseData);
            if (serverResponseData == null || serverResponseData.equalsIgnoreCase("null")) {
                socket.close();
                throw new IllegalStateException("Null received from swell server, syntax error in request.");
            } else {
                JSONParser parser = new JSONParser();
                JSONObject jsonObject = (JSONObject) parser.parse(serverResponseData);
                socket.close();
                return JsonDataParser.parse(jsonObject.toJSONString());
            }
        } catch (SocketException | SSLHandshakeException e) {
            if (e.getMessage().equalsIgnoreCase("connection reset") || e.getMessage().equalsIgnoreCase("Remote host terminated the handshake")) {
                return writeRequest(type, path, data);
            } else {
                throw new IllegalStateException("Request syntax error.", e);
            }
        } catch (IOException | ParseException e) {
            throw new IllegalStateException("Issues while connecting to swell", e);
        }
    }

    /**
     * Creates a GET request to an end-point returning a JSON response.
     *
     * @param path - The end-point URL path.
     * @return {@link ApiDataObject} - A JSON response from the end-point.
     */
    public ApiDataObject get(String path) {
        return request(RequestType.GET, path, null);
    }

    /**
     * Creates a POST request to an end-point returning a JSON response.
     *
     * @param path - The end-point URL path.
     * @param body - A key value HashMap storing body data.
     * @return {@link ApiDataObject} - A JSON response from the end-point.
     */
    public ApiDataObject post(String path, ApiDataObject body) {
        return request(RequestType.POST, path, body);
    }

    /**
     * Creates a PUT request to an end-point returning a JSON response.
     *
     * @param path - The end-point URL path.
     * @param body - A key value HashMap storing body data.
     * @return {@link ApiDataObject} - A JSON response from the end-point.
     */
    public ApiDataObject put(String path, ApiDataObject body) {
        return request(RequestType.PUT, path, body);
    }

    /**
     * Creates a DELETE request to an end-point returning a JSON response.
     *
     * @param path - The end-point URL path.
     * @param body - A key value HashMap storing the body of the request.
     * @return {@link ApiDataObject} - A JSON response from the end-point.
     */
    public ApiDataObject delete(String path, ApiDataObject body) {
        return request(RequestType.DELETE, path, body);
    }
}
