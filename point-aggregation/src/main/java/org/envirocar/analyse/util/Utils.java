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
package org.envirocar.analyse.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.envirocar.analyse.properties.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vividsolutions.jts.geom.GeometryFactory;

public class Utils {
    
	private static final Logger LOGGER = LoggerFactory
			.getLogger(Utils.class);
	
	public static GeometryFactory geometryFactory = new GeometryFactory();
	
    public static double[] convertWKTPointToXY(String wktPointAsString){
    	
    	double[] result = new double[2];
    	
    	wktPointAsString = wktPointAsString.replace("POINT(", "");
    	wktPointAsString = wktPointAsString.replace(")", "");
    	
    	String[] xyAsStringArray = wktPointAsString.split(" ");
    	
    	result[0] = Double.parseDouble(xyAsStringArray[0].trim());
    	result[1] = Double.parseDouble(xyAsStringArray[1].trim());
    	
    	return result;
    }
    
    public static double[] getCoordinatesXYFromJSON(LinkedHashMap<?, ?> geometryMap) {

		double[] result = new double[2];

		Object coordinatesObject = geometryMap.get("coordinates");

		if (coordinatesObject instanceof List<?>) {
			List<?> coordinatesList = (List<?>) coordinatesObject;

			if (coordinatesList.size() > 1) {
				result[0] = (Double) coordinatesList.get(0);
				result[1] = (Double) coordinatesList.get(1);
			} else {
				LOGGER.error("Coordinates array is too small (must be 2), size is: "
						+ coordinatesList.size());
			}
		}

		return result;

	}
	
    public static Map<String, Object> getValuesFromFromJSON(Map<?, ?> phenomenonMap) {
		
		Map<String, Object> result = new HashMap<>();
		
		for (String propertyName : Properties.getPropertiesOfInterestDatatypeMapping().keySet()) {
			Object propertyObject = phenomenonMap.get(propertyName);
			
            if (propertyObject == null){
            	result.put(propertyName, 0.0);//TODO handle non number properties
			}else if(propertyObject instanceof LinkedHashMap<?, ?>){
				result.put(propertyName, ((LinkedHashMap<?, ?>)propertyObject).get("value"));
			} 
			
		}
				
		return result;
		
	}
    
    public static Map<?, ?> parseJsonStream(InputStream stream) throws IOException {
		ObjectMapper om = new ObjectMapper();
		final Map<?, ?> json = om.readValue(stream, Map.class);
		return json;
    }
	
}
