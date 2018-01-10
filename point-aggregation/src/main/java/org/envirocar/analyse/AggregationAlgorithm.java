/**
 * Copyright 2016 52°North Initiative for Geospatial Open Source
 * Software GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.envirocar.analyse.entities.Point;
import org.envirocar.analyse.properties.GlobalProperties;
import org.envirocar.analyse.util.PointViaJsonMapIterator;
import org.envirocar.analyse.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import java.util.Properties;
import org.envirocar.analyse.categories.DEBasedCategory;
import org.envirocar.analyse.categories.RegionalTimeBasedCategory;
import org.envirocar.analyse.categories.TimeBasedCategory;
import org.joda.time.DateTime;

/**
 * Algorithm to aggregate measurements of tracks that are running through a defined bounding box.
 *
 * @author Benjamin Pross
 *
 */
public class AggregationAlgorithm {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(AggregationAlgorithm.class);
    private static final double DEFAULT_DISTANCE = 20.0;
    private static final double DEFAULT_BEARING = 0.0;
    
    private Geometry bbox;
    private double distance;
    private final PointService pointService;
    private final boolean useBearing;
    private final RegionalTimeBasedCategory timeBasedManager;
    private final boolean useCategories;
    private final double maxBearingDelta;
    private final String databaseName;
    
    public AggregationAlgorithm() {
        this(DEFAULT_DISTANCE, DEFAULT_BEARING, false);
    }
    
    public AggregationAlgorithm(Properties props) {
        this.useCategories = Boolean.parseBoolean(props.getProperty("useCategories"));
        String distanceProp = props.getProperty("pointDistance");
        this.distance = distanceProp != null ? Double.parseDouble(distanceProp) : DEFAULT_DISTANCE;
        String bearingProp = props.getProperty("maxBearingDelta");
        this.maxBearingDelta = bearingProp != null ? Double.parseDouble(bearingProp) : DEFAULT_BEARING;
        this.useBearing = maxBearingDelta != 0.0;
        this.timeBasedManager = new DEBasedCategory();
        this.databaseName = props.getProperty("database");
        
        this.pointService = new PostgresPointService(this.bbox, this.databaseName);
    }
    
    public AggregationAlgorithm(double distance, double maxBearing, boolean useCategories) {
        this.distance = distance;
        
        this.timeBasedManager = new DEBasedCategory();
        this.useCategories = useCategories;
        this.maxBearingDelta = maxBearing;
        this.useBearing = maxBearingDelta != 0.0;
        this.databaseName = "aggregation";
        
        this.pointService = new PostgresPointService(this.bbox, this.databaseName);
    }
    
    public AggregationAlgorithm(Envelope bbox) {
        this();
        double maxx = bbox.getMaxX();
        double maxy = bbox.getMaxY();
        double minx = bbox.getMinX();
        double miny = bbox.getMinY();
        
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
        
        this.bbox =  Utils.geometryFactory.createPolygon(coordinates);
    }
    
    public void runAlgorithm(Iterator<Point> newPoints, String trackId) {
        if (pointService.trackAlreadyAggregated(trackId)) {
            LOGGER.info("Track already aggregated. skipping. "+trackId);
            return;
        }
        
        LOGGER.info("Aggregating Track: " + trackId);
        
        pointService.insertTrackIntoAggregatedTracksTable(trackId);
        
        Point nextPoint;
        DateTime trackTime = null;
        List<MeasurementRelation> newRelations = new ArrayList<>();
        while (newPoints.hasNext()) {
            nextPoint = newPoints.next();
            
            /**
            * set the timezone of the track
            */
            if (trackTime == null) {
                trackTime = nextPoint.getTime();
                timeBasedManager.updateTimeZone(trackTime);
            }
            
            if (useCategories) {
                nextPoint.setTimeCategory(timeBasedManager.fromTime(nextPoint.getTime()));
            }
            else {
                nextPoint.setTimeCategory(TimeBasedCategory.NO_CATEGORY);
            }
            
            /*
            * check if point is fit for aggregation (one or more value not null or 0)
            */
            if(!isFitForAggregation(nextPoint)){
                LOGGER.debug("Skipping original point " + nextPoint.getID() + ". All values are null or 0.");
                continue;
            }
            
            /*
            * get nearest neighbor from resultSet
            */
            Point nearestNeighbor = pointService.getNearestNeighbor(
                    nextPoint, distance, useBearing ? maxBearingDelta : 0.0);
            
            if (nearestNeighbor != null) {
                
                /*
                * if there is one
                *
                * aggregate values (avg, function should be
                * replaceable)
                */
                LOGGER.debug("aggregating point: "+ nextPoint.getID());
                newRelations.add(pointService.aggregate(nextPoint, nearestNeighbor, trackId));
            } else {
                /*
                * if there is no nearest neighbor
                *
                * add point to resultSet
                */
                LOGGER.debug("No nearest neighbor found for " + nextPoint.getID() + ". Adding to resultSet.");
                
                /*
                * add point to result set, give it a new id
                */
                newRelations.add(pointService.addToResultSet(nextPoint));
            }
        }
        
        pointService.insertMeasurementRelations(newRelations);
    }
    
    
    public void runAlgorithm(final String trackID) throws IOException {
        LOGGER.info("Aggregating Track: " + trackID);
        
        if (pointService.trackAlreadyAggregated(trackID)) {
            LOGGER.info("Track already aggregated. skipping. "+trackID);
            return;
        }
        
        HttpGet get = new HttpGet(GlobalProperties.getRequestTrackURL()+trackID);
        
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
    
    public boolean isFitForAggregation(Point point) {
        
        boolean result = false;
        
        for (String propertyName : GlobalProperties.getPropertiesOfInterestDatabase().keySet()) {
            
            Object numberObject = point.getProperty(propertyName);
            
            if (numberObject instanceof Number) {
                result = result || !Utils.isNumberObjectNullOrZero((Number) numberObject);
            } else{
                /*
                * not a number, we cannot aggregate this currently
                */
                result = result || false;
            }
        }
        
        /*
        * also check for bbox
        */
        if (bbox != null) {
            Coordinate pointCoordinate = new Coordinate(point.getX(), point.getY());
            
            if (!bbox.contains(Utils.geometryFactory
                    .createPoint(pointCoordinate))) {
                return false;
            }
        }
        
        return result;
    }
    
    protected HttpClient createClient() throws IOException, KeyManagementException, UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException {
        return Utils.createClient();
    }
    
    public void runAlgorithm() throws IOException{
        
        /*
         * get tracks
         */
        Envelope env = bbox.getEnvelopeInternal();
        List<String> trackIDs = getTrackIDs(env.getMinX(), env.getMinY(), env.getMaxX(), env.getMaxY());
        
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
            url = new URL(GlobalProperties.getRequestTracksWithinBboxURL() + minx + "," + miny + "," + maxx + "," + maxy);
            
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

    public void shutdown() throws IOException {
        this.pointService.shutdown();
    }

}
