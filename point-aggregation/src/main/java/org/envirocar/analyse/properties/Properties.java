/**
 * Copyright (C) 2014 52°North Initiative for Geospatial Open Source
 * Software GmbH
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 as published
 * by the Free Software Foundation.
 *
 * If the program is linked with libraries which are licensed under one of
 * the following licenses, the combination of the program with the linked
 * library is not considered a "derivative work" of the program:
 *
 *     - Apache License, version 2.0
 *     - Apache Software License, version 1.0
 *     - GNU Lesser General Public License, version 3
 *     - Mozilla Public License, versions 1.0, 1.1 and 2.0
 *     - Common Development and Distribution License (CDDL), version 1.0
 *
 * Therefore the distribution of the program linked with libraries licensed
 * under the aforementioned licenses, is permitted by the copyright holders
 * if the distribution is compliant with both the GNU General Public
 * License version 2 and the aforementioned licenses.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 */
package org.envirocar.analyse.properties;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.envirocar.analyse.PostgresPointService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Properties {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(Properties.class);
	
    private static final String PROPERTIES = "/aggregation.properties";
    private static final String DEFAULT_PROPERTIES = "/aggregation.default.properties";
	private static java.util.Properties properties;
	
	private static Map<String, Class<?>> propertiesOfInterestDatatypeMapping;
	
	private static String baseURL;
	
	private static String requestTrackURL;

	private static String requestTracksWithinBboxURL;
	
	public static String getProperty(String propertyName){
		return getProperties().getProperty(propertyName);
	}
	
	public synchronized static java.util.Properties getProperties() {
		
		if(properties == null){
			
			properties = new java.util.Properties();
	        InputStream in = null;
	        try {
	            in = PostgresPointService.class.getResourceAsStream(PROPERTIES);
	            if (in != null) {
	                properties.load(in);
	            } else {
	            	LOGGER.info("No {} found, loading {}.", PROPERTIES, DEFAULT_PROPERTIES);
	                in = PostgresPointService.class.getResourceAsStream(DEFAULT_PROPERTIES);
	                if (in != null) {
	                    properties.load(in);
	                }else{
	                	LOGGER.warn("No {} found!", PROPERTIES);
	                }
	            }	            
	        } catch (IOException ex) {
	            LOGGER.error("Error reading " + PROPERTIES, ex);
	        } finally {
	            try {
					in.close();
				} catch (IOException e) {}
	        }
			
		}
		
		return properties;
	}
	
	public static String getBaseURL() {
		
		if(baseURL == null || baseURL.equals("")){
			baseURL = getProperties().getProperty("baseURL");
		}
		
		return baseURL;
	}

	public static String getRequestTrackURL() {
		
		if(requestTrackURL == null || requestTrackURL.equals("")){
			requestTrackURL = getBaseURL() + "tracks/";
		}
		
		return requestTrackURL;
	}

	public static String getRequestTracksWithinBboxURL() {
		
		if(requestTracksWithinBboxURL == null || requestTracksWithinBboxURL.equals("")){
			requestTracksWithinBboxURL = getBaseURL() + "tracks?bbox=";
		}
		
		return requestTracksWithinBboxURL;
	}
	
	//TODO make configurable
	public static Map<String, Class<?>> getPropertiesOfInterestDatatypeMapping(){
		
		if(propertiesOfInterestDatatypeMapping == null){
			propertiesOfInterestDatatypeMapping = new HashMap<>();
			
			propertiesOfInterestDatatypeMapping.put("CO2", Double.class);
			propertiesOfInterestDatatypeMapping.put("Speed", Double.class);
		}
		return propertiesOfInterestDatatypeMapping;
	}
	
}
