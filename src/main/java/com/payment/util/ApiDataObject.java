package com.payment.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


/**
 * 
 * Represents a JSON data object.
 * 
 * @author Oska Jory <oska@excede.com.au>
 */
public class ApiDataObject extends Object implements Map<String, Object> {
	
	
	// The data constructing the object.
	private Map<String, Object> data;
	
	
	// Constructs a new Data Object.
	public ApiDataObject() {
		this.data = new HashMap<String, Object>();
	}
	
	
	public ApiDataObject(Map<String, Object> map) {
		this.data = map;
	}
	
	
	/**
	 * Returns how many main keys are stored in the object.
	 */
	@Override
	public int size() {
		return data.size();
	}

	
	// Is the data object empty?
	@Override
	public boolean isEmpty() { 
		return data.isEmpty();
	}

	
	/**
	 * Does the data object contain the specified key?
	 */
	@Override
	public boolean containsKey(Object key) {
		return data.containsKey(key);
	}

	
	/**
	 * Does the data object contain the specified value?
	 */
	@Override
	public boolean containsValue(Object value) {
		return data.containsValue(value);
	}

	
	/**
	 * Returns the value stored in the data object based on the specified key.
	 */
	@Override
	public Object get(Object key) {
		return data.get(key);
	}
	
	
	/**
	 * @param key
	 * @return A data object from the specified key.
	 */
	public ApiDataObject getDataObject(String key) {
		return (ApiDataObject) data.get(key);
	}


	/**
	 * Removes an entry from the data object.
	 */
	@Override
	public Object remove(Object key) {
		return data.remove(key);
	}
	

	/**
	 * Clears the data object.
	 */
	@Override
	public void clear() {
		data.clear();
	}

	
	/**
	 * Returns the keys stored in the data object.
	 */
	@Override
	public Set<String> keySet() {
		return data.keySet();
	}

	
	/**
	 * Returns the values stored in the data object.
	 */
	@Override
	public Collection<Object> values() {
		return data.values();
	}


	
	@Override
	public Set<Map.Entry<String, Object>> entrySet() {
		return data.entrySet();
	}


	/**
	 * Insets data / an object into the data structure of the data object.
	 */
	@Override
	public ApiDataObject put(String key, Object value) {
		data.put(key, value);
		return this;
	}


	@Override
	public void putAll(Map<? extends String, ? extends Object> m) {
		data.putAll(m);		
	}

	
	/**
	 * Constructs the data object into a JSON string.
	 */
	@Override 
	public String toString() {
	
		String string = "{";
		
		int idx = 0;
		
		for (String i : data.keySet()) {
			
			if (idx != 0) {
				string +=",";
			}
			
			string+="\"" + i + "\":" +  (data.get(i) == null ? "null" : (data.get(i) instanceof String ? ("\"" + data.get(i).getClass().cast(data.get(i).toString()) + "\"") : data.get(i).getClass().cast(data.get(i)).toString()));
			
			idx++;
			
		}
		
		string += "}";
		
		return string;
	}
	

}
