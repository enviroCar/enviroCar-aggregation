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

import java.io.File;
import java.io.IOException;

import org.envirocar.analyse.export.csv.CSVExport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AggregationAlgorithmPostgresTest {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(AggregationAlgorithmPostgresTest.class);
	
	public static void main(String[] args) throws IOException{
		
		double maxx = 7.6339;
		double maxy = 51.96;
		double minx = 7.6224;
		double miny = 51.94799;
		
//		double maxx = 7.6539;
//		double maxy = 51.96519;
//		double minx = 7.6224;
//		double miny = 51.94799;
		        
//        AggregationAlgorithm algorithm = new AggregationAlgorithm(minx, miny, maxx, maxy, 0.00045);
		
		/*
		 * 0.00009 = 10m
		 * 0.00045 = 50m
		 * 0.00018 = 20m
		 */
        AggregationAlgorithm algorithm = new AggregationAlgorithm(0.00018);
        
		
        algorithm.runAlgorithm("53b5228ee4b01607fa566b78");
        algorithm.runAlgorithm("53b52282e4b01607fa566469");
        
		try {
			CSVExport.exportAsCSV(algorithm.getResultSet(), File.createTempFile("aggregation", ".csv").getAbsolutePath());
		} catch (IOException e) {
			LOGGER.error("Could not export resultSet as CSV:", e);
		}
		
	}
}
