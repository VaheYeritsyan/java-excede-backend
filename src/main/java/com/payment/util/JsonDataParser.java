package com.payment.util;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


/**
 * 
 * TODO: Create own custom JSON parser that parses directly into {@link ApiDataObject} rather than {@link JSONObject} using {@link JSONParser}.
 * 
 * Parses JSON data into a {@link ApiDataObject}
 * @author Oska Jory <oska@excede.com.au>
 *
 */
public class JsonDataParser {

	
	/**
	 * TODO: Add documentation
	 * @param <T>
	 * @param jsonString
	 * @param mapClass
	 * @return
	 * @throws ParseException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 */
	
	public static <T extends Map<String, Object>> T parse(String jsonString, Class<T> mapClass)
			throws ParseException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		JSONParser parser = new JSONParser();
		JSONObject json = (JSONObject) parser.parse(jsonString);
		T map = mapClass.getDeclaredConstructor().newInstance();
		
		for (Object key : json.keySet()) {
			
			if (json.get(key) instanceof JSONObject) {
				map.put(key.toString(), createApiDataObject((JSONObject) json.get(key)));
			} else {
			
				map.put(key.toString(), json.get(key));
			}
		}
		
		return map;
	}
	
	
	
	
	/**
	 * TODO: Add documentation.
	 * @param object
	 * @return
	 */
	public static ApiDataObject createApiDataObject(JSONObject object) {
		ApiDataObject new_object = new ApiDataObject();
		
		for (Object key : object.keySet()) {
			
			if (object.get(key) instanceof JSONObject) {
				ApiDataObject sub_object = createApiDataObject((JSONObject) object.get(key));
				new_object.put(key.toString(), sub_object);
			} else {
				new_object.put(key.toString(), object.get(key));
			}
		}
		
		return new_object;
	}	
	
	
	
	/**
	 * TODO: Add documentation.
	 * @param jsonString
	 * @return
	 */
	public static ApiDataObject parse(String jsonString) {
		try {
			return parse(jsonString, ApiDataObject.class);
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
}
