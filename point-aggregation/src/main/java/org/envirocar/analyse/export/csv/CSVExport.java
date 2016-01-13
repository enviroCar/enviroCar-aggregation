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
package org.envirocar.analyse.export.csv;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream.GetField;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.envirocar.analyse.entities.Point;
import org.envirocar.analyse.properties.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CSVExport {

	private static final Logger LOGGER = LoggerFactory.getLogger(CSVExport.class);
	
	public static void exportAsCSV(List<Point> resultSet, String fileName) {
		
		LOGGER.debug("CSV export file name: " + fileName);
		
		File csvFile = new File(fileName);
		
		BufferedWriter bufferedWriter = null;
		
		try {
			bufferedWriter = new BufferedWriter(new FileWriter(csvFile));
			
			bufferedWriter.write(createCSVHeader());
			
			for (Point point : resultSet) {
				bufferedWriter.write(createCSVLine(point));
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			try {
				bufferedWriter.close();
			} catch (IOException e) {}
		}
	}

	private static String createCSVHeader() {
		
		String result = "ID;lon;lat;GeneralNumberOfPointsUsed;GeneralNumberOfTracksUsed;";
		
		Iterator<String> propertyNameIterator = Properties.getPropertiesOfInterestDatatypeMapping().keySet().iterator();
				
		while (propertyNameIterator.hasNext()) {
			String propertyName = (String) propertyNameIterator.next();
			
			result = result.concat(propertyName);
			
			result = result.concat(";");
			
			result = result.concat(propertyName + "NumberOfPointsUsed");
			
			if(propertyNameIterator.hasNext()){
				result = result.concat(";");
			}			
			
		}
		
		result = result.concat("\n");
		
		return result;
	}

	private static String createCSVLine(Point point){
		
		String result = point.getID() + ";" + point.getX() + ";" + point.getY() + ";" + point.getNumberOfPointsUsedForAggregation()+ ";" + point.getNumberOfTracksUsedForAggregation() + ";";
		
		Iterator<String> propertyNameIterator = Properties.getPropertiesOfInterestDatatypeMapping().keySet().iterator();
		
		Map<String, Object> propertyMap = point.getPropertyMap();
		
		while (propertyNameIterator.hasNext()) {
			String propertyName = (String) propertyNameIterator.next();
			
			result = result.concat(String.valueOf(propertyMap.get(propertyName)));
			
			result = result.concat(";");
			
			result = result.concat(String.valueOf(point.getNumberOfPointsUsedForAggregation(propertyName)));
			
			if(propertyNameIterator.hasNext()){
				result = result.concat(";");
			}			
			
		}
		
		result = result.concat("\n");
		
		return result;		
	}
	
}
