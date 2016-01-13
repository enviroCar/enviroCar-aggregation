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

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.AllowAllHostnameVerifier;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.DefaultHttpClient;
import org.envirocar.analyse.entities.Point;
import org.envirocar.analyse.properties.Properties;
import org.envirocar.analyse.util.PointViaJsonMapIterator;
import org.envirocar.analyse.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

/**
 * Algorithm to aggregate measurements of tracks that are running through a defined bounding box.
 * 
 * @author Benjamin Pross
 *
 */
public class AggregationAlgorithm {

	private static final Logger LOGGER = LoggerFactory.getLogger(AggregationAlgorithm.class);
	
	private Geometry bbox;
	private double distance;
	private PointService pointService;
	private double maxx, maxy, minx, miny;
	
	public AggregationAlgorithm() {
		this(Double.parseDouble(Properties.getProperty("pointDistance")));
	}
	
	public AggregationAlgorithm(double distance) {
		pointService = new PostgresPointService(this.getBbox());
		this.distance = distance;
	}
	
	public AggregationAlgorithm(double minx, double miny, double maxx, double maxy){
		this();
		this.maxx = maxx;
		this.maxy = maxy;
		this.minx = minx;
		this.miny = miny;
		
        Coordinate upperRight = new Coordinate(maxx, maxy);
        Coordinate upperLeft = new Coordinate(minx, maxy);
        Coordinate lowerRight = new Coordinate(maxx, miny);
        Coordinate lowerLeft = new Coordinate(minx, miny);
        
        Coordinate[] coordinates = new Coordinate[] {
                lowerLeft,
                lowerRight,
                upperRight,
                upperLeft,
                lowerLeft
            };
        
        bbox =  Utils.geometryFactory.createPolygon(coordinates);
		this.distance = Double.parseDouble(Properties.getProperty("pointDistance"));
		pointService = new PostgresPointService(this.getBbox());
	}
	
	public void runAlgorithm(Iterator<Point> newPoints, String trackId) {
		if (pointService.trackAlreadyAggregated(trackId)) {
			LOGGER.info("Track already aggregated. skipping. "+trackId);
			return;
		}
		
		pointService.insertTrackIntoAggregatedTracksTable(trackId);
		
		Point nextPoint;
		while (newPoints.hasNext()) {
			nextPoint = newPoints.next();
			
			/*
			 * check if point is fit for aggregation (one or more value not null or 0)
			 */
			if(!pointService.isFitForAggregation(nextPoint)){
				LOGGER.info("Skipping original point " + nextPoint.getID() + ". All values are null or 0.");
				continue;
			}

			/*
			 * get nearest neighbor from resultSet
			 */
			
			Point nearestNeighbor = pointService.getNearestNeighbor(
					nextPoint, distance);

			if (nearestNeighbor != null) {
				
				/*
				 * check if point is fit for aggregation (one or more value not null or 0)
				 */
				if(!pointService.isFitForAggregation(nearestNeighbor)){
					LOGGER.info("Skipping result set point " + nearestNeighbor.getID() + ". All values are null or 0.");
					continue;
				}
				
				/*
				 * if there is one
				 * 
				 * aggregate values (avg, function should be
				 * replaceable)
				 */
				LOGGER.info("aggregating point: "+ nextPoint.getID());
				pointService.aggregate(nextPoint, nearestNeighbor, trackId);

			} else {
				/*
				 * if there is no nearest neighbor
				 * 
				 * add point to resultSet
				 */					
				LOGGER.info("No nearest neighbor found for " + nextPoint.getID() + ". Adding to resultSet.");
				
				/*
				 * add point to result set, give it a new id
				 */
				pointService.addToResultSet(nextPoint);
			}
		}
		
	}
	
	
	public void runAlgorithm(final String trackID) throws IOException {
		
		LOGGER.debug("");
		LOGGER.debug("");
		LOGGER.debug("");
		LOGGER.debug("");
		
		LOGGER.debug("Track: " + trackID);
		LOGGER.debug("ResultSet size: " + pointService.getResultSet().size());
		
		LOGGER.debug("");
		LOGGER.debug("");
		LOGGER.debug("");
		LOGGER.debug("");

		if (pointService.trackAlreadyAggregated(trackID)) {
			LOGGER.info("Track already aggregated. skipping. "+trackID);
			return;
		}
		
		HttpGet get = new HttpGet(Properties.getRequestTrackURL()+trackID);
		
		HttpClient client;
		try {
			client = createClient();
		} catch (KeyManagementException | UnrecoverableKeyException
				| NoSuchAlgorithmException | KeyStoreException e) {
			throw new IllegalStateException(e);
		}
		
		HttpResponse resp = client.execute(get);
		if (resp != null && resp.getEntity() != null
				&& resp.getStatusLine() != null &&
				resp.getStatusLine().getStatusCode() < HttpStatus.SC_MULTIPLE_CHOICES) {
			
			PointViaJsonMapIterator it = new PointViaJsonMapIterator(
					Utils.parseJsonStream(resp.getEntity().getContent()));
			
			runAlgorithm(it, trackID);
		}
		
	}
	
	protected HttpClient createClient() throws IOException, KeyManagementException, UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException {
		DefaultHttpClient result = new DefaultHttpClient();
		SchemeRegistry sr = result.getConnectionManager().getSchemeRegistry();

		SSLSocketFactory sslsf = new SSLSocketFactory(new TrustStrategy() {

			@Override
			public boolean isTrusted(X509Certificate[] arg0, String arg1)
					throws CertificateException {
				return true;
			}
		}, new AllowAllHostnameVerifier());

		Scheme httpsScheme2 = new Scheme("https", 443, sslsf);
		sr.register(httpsScheme2);

		return result;
	}
	
	public void runAlgorithm() throws IOException{
		
		/*
		 * get tracks
		 */
				
        List<String> trackIDs = getTrackIDs(minx, miny, maxx, maxy);
		
		/*
		 * foreach track
		 * 
		 */
        
		for (String trackID : trackIDs) {

			runAlgorithm(trackID);
			/* 
			 * continue with next track
			 */
		}
	}
	
	public List<String> getTrackIDs(double minx, double miny, double maxx, double maxy){
		
		List<String> result = new ArrayList<>();
        
		URL url = null;
		try {		
			url = new URL(Properties.getRequestTracksWithinBboxURL() + minx + "," + miny + "," + maxx + "," + maxy);
			
			LOGGER.debug("URL for fetching tracks: " + url.toString());
			
			InputStream in = url.openStream();

			ObjectMapper objMapper = new ObjectMapper();

			Map<?, ?> map = objMapper.readValue(in, Map.class);

			ArrayList<?> tracks = (ArrayList<?>) map.get("tracks");

			LOGGER.info("Number of tracks: " + tracks.size());
			
			for (Object object : tracks) {
				
				if(object instanceof LinkedHashMap<?, ?>){
					String id = String.valueOf(((LinkedHashMap<?, ?>)object).get("id"));
					
					result.add(id);
				}
			}
			
		} catch (MalformedURLException e) {
			LOGGER.error("URL seems to be malformed: " + url);
		} catch (IOException e) {
			LOGGER.error("Could not read from URL: " + url);
		}
		
		return result;
		
	}

	public Geometry getBbox() {
		return bbox;
	}

	public void setBbox(Geometry bbox) {
		this.bbox = bbox;
	}

	public double getDistance() {
		return distance;
	}

	public void setDistance(double distance) {
		this.distance = distance;
	}
	
	public List<Point> getResultSet(){
		return pointService.getResultSet();
	}
	
}
