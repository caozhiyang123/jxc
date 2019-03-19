package com.site.utils.uiutil;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

public class ResourceKit {

	private static final Map<String, String> map = new ConcurrentHashMap<>();
	
	private ResourceKit() {
		
	}
	
	public static Map<String, String> readProperties(String fileName){
		Properties properties = new Properties();
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(fileName);
			properties.load(fis);
			Iterator<String> iterator = properties.stringPropertyNames().iterator();
			while (iterator.hasNext()) {
				String key = iterator.next();
				map.put(key, properties.getProperty(key));
			}
			fis.close();
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return map;
	}
	
}
