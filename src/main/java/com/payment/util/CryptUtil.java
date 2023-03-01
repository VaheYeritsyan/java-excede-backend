package com.payment.util;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class CryptUtil {

	
	public String hmacSha256(String data, String secret) {
	    try {

	        byte[] hash = secret.getBytes(StandardCharsets.UTF_8);
	        Mac sha256Hmac = Mac.getInstance("HmacSHA256");
	        SecretKeySpec secretKey = new SecretKeySpec(hash, "HmacSHA256");
	        sha256Hmac.init(secretKey);

	        byte[] signedBytes = sha256Hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));

	        return Base64.getUrlEncoder().withoutPadding().encodeToString(signedBytes);
	    } catch (NoSuchAlgorithmException | InvalidKeyException ex) {
	        return null;
	    }
	}
	
	
	public static String sha256(String data) {
		MessageDigest digest;
		try {
			digest = MessageDigest.getInstance("SHA-256");
			byte[] encodedhash = digest.digest(data.toString().getBytes(StandardCharsets.UTF_8));
			
			String encoded = CryptUtil.bytesToHex(encodedhash);
			
			return encoded;
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		return null;
	}
	
	
	// Generate a unique access token using SHA-256
    public static String generateToken() throws NoSuchAlgorithmException {
        
        // Generate a random salt
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        
        // Convert the salt and current timestamp to a byte array
        long timestamp = System.currentTimeMillis();
        byte[] bytes = new byte[Long.BYTES + salt.length];
        System.arraycopy(salt, 0, bytes, 0, salt.length);
        System.arraycopy(longToBytes(timestamp), 0, bytes, salt.length, Long.BYTES);
        
        // Generate the SHA-256 hash
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(bytes);
        
        // Convert the hash to a string and return it
        return bytesToHex(hash);
    }
    
    

    // Generate a unique access token using SHA-256
    public static String generateToken(int length) throws NoSuchAlgorithmException {

        // Make sure the length is at least 1
        if (length < 1) {
            throw new IllegalArgumentException("Token length must be at least 1");
        }

        // Generate a random salt
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[length];
        random.nextBytes(salt);

        // Convert the salt and current timestamp to a byte array
        long timestamp = System.currentTimeMillis();
        byte[] bytes = new byte[Long.BYTES + salt.length];
        System.arraycopy(salt, 0, bytes, 0, salt.length);
        System.arraycopy(longToBytes(timestamp), 0, bytes, salt.length, Long.BYTES);

        // Generate the SHA-256 hash
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(bytes);

        // Convert the hash to a string and return it
        return bytesToHex(hash);
    }

    
    
    // Convert a long value to a byte array
    private static byte[] longToBytes(long value) {
        byte[] bytes = new byte[Long.BYTES];
        for (int i = 0; i < Long.BYTES; i++) {
            bytes[i] = (byte) (value >> (8 * i));
        }
        return bytes;
    }

    
    // Convert a byte array to a hex string
    private static String bytesToHex(byte[] bytes) {
        StringBuilder builder = new StringBuilder();
        for (byte b : bytes) {
            builder.append(String.format("%02x", b));
        }
        return builder.toString();
    }
}
