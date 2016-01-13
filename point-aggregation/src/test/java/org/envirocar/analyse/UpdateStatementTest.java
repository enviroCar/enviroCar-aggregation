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
package org.envirocar.analyse;

import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.envirocar.analyse.entities.InMemoryPoint;
import org.envirocar.analyse.entities.Point;
import org.junit.Test;

public class UpdateStatementTest {

	@Test
	public void testAlgorithm(){
        
		PostgresPointService pointService = new PostgresPointService();
		
		String oldID = "528bd8a3e4b08cecc5a6f403";
		double oldX = 7.6339;
		double oldY = 51.94799;
		double oldSpeed = 20.0087251993326;
		double oldCo2 = 2.12374;
		int oldNumberOfPointsUsedForAggregation = 5;
		int oldNumberOfTracksUsedForAggregation = 7;
		int oldNumberOfPointsUsedForSpeed = 4;
		int oldNumberOfPointsUsedForCo2 = 6;
		String oldLastContributingTrack = "53b5228ee4b01607fa566b78";
		
		Map<String, Object> oldPropertyMap = new HashMap<>();
		
		oldPropertyMap.put("Speed", oldSpeed);
		oldPropertyMap.put("CO2", oldCo2);
		
		Map<String, Integer> oldPropertyPointsUsedForAggregationMap = new HashMap<>();
		
		oldPropertyPointsUsedForAggregationMap.put("Speed", oldNumberOfPointsUsedForSpeed);
		oldPropertyPointsUsedForAggregationMap.put("CO2", oldNumberOfPointsUsedForCo2);
		
		pointService.removePoint(oldID, PostgresPointService.aggregated_MeasurementsTableName);
		
		Point oldPoint = new InMemoryPoint(oldID, oldX, oldY, oldPropertyMap, oldNumberOfPointsUsedForAggregation, oldNumberOfTracksUsedForAggregation, oldLastContributingTrack, oldPropertyPointsUsedForAggregationMap);
		
		/*
		 * add measurement
		 */
		pointService.addToResultSet(oldPoint);
		
		String updatedID = "528bd8a3e4b09cecc5a6f445";
		double updatedX = 7.6539;
		double updatedY = 51.95688;
		double updatedSpeed = 35.7;
		double updatedCo2 = 1.654684;
		int updatedNumberOfPointsUsedForAggregation = 34;
		int updatedNumberOfTracksUsedForAggregation = 19;
		int updatedNumberOfPointsUsedForSpeed = 17;
		int updatedNumberOfPointsUsedForCo2 = 23;
		String updatedLastContributingTrack = "53b5448ee4b01607fa566b99";
		
		Map<String, Object> updatedPropertyMap = new HashMap<>();
		
		updatedPropertyMap.put("Speed", updatedSpeed);
		updatedPropertyMap.put("CO2", updatedCo2);
		
		Map<String, Integer> updatedPropertyPointsUsedForAggregationMap = new HashMap<>();
		
		updatedPropertyPointsUsedForAggregationMap.put("Speed", updatedNumberOfPointsUsedForSpeed);
		updatedPropertyPointsUsedForAggregationMap.put("CO2", updatedNumberOfPointsUsedForCo2);
		
		Point updatedPoint = new InMemoryPoint(updatedID, updatedX, updatedY, updatedPropertyMap, updatedNumberOfPointsUsedForAggregation, updatedNumberOfTracksUsedForAggregation, updatedLastContributingTrack, updatedPropertyPointsUsedForAggregationMap);
		 
		pointService.updateResultSet(oldID, updatedPoint);
		
		List<Point> resultSet = pointService.getResultSet();
		
		/*
		 * updated points shall keep their id
		 */
		boolean oldIDStillInResultSet = false;
		
		for (Point point : resultSet) {
			if(point.getID().equals(oldID)){
				oldIDStillInResultSet = true;
				
				assertTrue(point.getX() == updatedX);
				assertTrue(point.getY() == updatedY);
				assertTrue(point.getNumberOfPointsUsedForAggregation() == updatedNumberOfPointsUsedForAggregation);
				assertTrue(point.getNumberOfTracksUsedForAggregation() == updatedNumberOfTracksUsedForAggregation);
				assertTrue(point.getLastContributingTrack().equals(updatedLastContributingTrack));
				
				Map<String, Object> propertyMap = point.getPropertyMap();
				
				assertTrue((double)propertyMap.get("Speed") == updatedSpeed);
				assertTrue((double)propertyMap.get("CO2") == updatedCo2);
				
				Map<String, Integer> propertyPointsUsedForAggregationMap = point.getPropertyPointsUsedForAggregationMap();
				
				assertTrue(propertyPointsUsedForAggregationMap.get("Speed") == updatedNumberOfPointsUsedForSpeed);
				assertTrue(propertyPointsUsedForAggregationMap.get("CO2") == updatedNumberOfPointsUsedForCo2);
				
			}
		}
		
		assertTrue(oldIDStillInResultSet);
	}
	
}
