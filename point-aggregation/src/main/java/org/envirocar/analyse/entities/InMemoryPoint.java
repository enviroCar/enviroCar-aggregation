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
package org.envirocar.analyse.entities;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.envirocar.analyse.categories.TimeBasedCategory;

import org.envirocar.analyse.util.Utils;
import org.joda.time.DateTime;


public class InMemoryPoint implements Point {
    
    private String id;
    private double x;
    private double y;
    private Map<String, Object> propertyMap;
    private Map<String, Integer> propertyPointsUsedForAggregationMap;
    private int numberOfPointsUsedForAggregation = 1;
    private List<String> tracksUsedForAggregation;
    private int numberOfTracksUsedForAggregation;
    private String lastContributingTrack;
    private TimeBasedCategory timeCategory;
    
    private static Map<String, String> jsonPropertyMapping;
    
    static {
        jsonPropertyMapping = new HashMap<>();
        jsonPropertyMapping.put("GPS Bearing", "bearing");
        jsonPropertyMapping.put("Speed", "speed");
        jsonPropertyMapping.put("CO2", "co2");
    }
    private DateTime time;
    
    
    public InMemoryPoint(String id, DateTime time, double x, double y, Map<String, Object> propertyMap, int numberOfPointsUsedForAggregation, int numberOfTracksUsedForAggregation, String lastContributingTrack, Map<String, Integer> propertyPointsUsedForAggregationMap){
        this.id = id;
        this.time = time;
        this.x = x;
        this.y = y;
        this.propertyMap = propertyMap;
        this.numberOfPointsUsedForAggregation = numberOfPointsUsedForAggregation;
        this.numberOfTracksUsedForAggregation = numberOfTracksUsedForAggregation;
        this.lastContributingTrack = lastContributingTrack;
        this.propertyPointsUsedForAggregationMap = propertyPointsUsedForAggregationMap;
    }
    
    public InMemoryPoint(Point otherPoint){
        this.id = otherPoint.getID();
        this.time = otherPoint.getTime();
        this.x = otherPoint.getX();
        this.y = otherPoint.getY();
        this.propertyMap = otherPoint.getPropertyMap();
        this.numberOfPointsUsedForAggregation = otherPoint.getNumberOfPointsUsedForAggregation();
        this.numberOfTracksUsedForAggregation = otherPoint.getNumberOfTracksUsedForAggregation();
        this.lastContributingTrack = otherPoint.getLastContributingTrack();
        tracksUsedForAggregation = otherPoint.getTracksUsedForAggregation();
        this.propertyPointsUsedForAggregationMap = otherPoint.getPropertyPointsUsedForAggregationMap();
    }
    
    public InMemoryPoint(){
        
    }
    
    @Override
    public String getID() {
        return id;
    }
    
    @Override
    public double getX() {
        return x;
    }
    
    @Override
    public double getY() {
        return y;
    }
    
    @Override
    public int getNumberOfPointsUsedForAggregation() {
        return numberOfPointsUsedForAggregation;
    }
    
    @Override
    public List<String> getTracksUsedForAggregation() {
        return tracksUsedForAggregation;
    }
    
    @Override
    public void setNumberOfPointsUsedForAggregation(int numberOfPoints) {
        this.numberOfPointsUsedForAggregation = numberOfPoints;
    }
    
    @Override
    public void addTrackUsedForAggregation(String trackID) {
        tracksUsedForAggregation.add(trackID);
    }
    
    @Override
    public Map<String, Object> getPropertyMap() {
        return propertyMap;
    }
    
    @Override
    public void setProperty(String propertyName, Object value) {
        propertyMap.put(propertyName, value);
    }
    
    @Override
    public Object getProperty(String propertyName) {
        return propertyMap.get(propertyName);
    }
    
    @Override
    public void setID(String id) {
        this.id = id;
    }
    
    @Override
    public int getNumberOfTracksUsedForAggregation() {
        return numberOfTracksUsedForAggregation;
    }
    
    @Override
    public String getLastContributingTrack() {
        return lastContributingTrack;
    }
    
    @Override
    public void setNumberOfTracksUsedForAggregation(
            int numberOfTracksUsedForAggregation) {
        this.numberOfTracksUsedForAggregation = numberOfTracksUsedForAggregation;
    }
    
    @Override
    public void setLastContributingTrack(String lastContributingTrack) {
        this.lastContributingTrack = lastContributingTrack;
    }
    
    @Override
    public void setX(double x) {
        this.x = x;
    }
    
    @Override
    public void setY(double y) {
        this.y = y;
    }
    
    @Override
    public int getNumberOfPointsUsedForAggregation(String propertyName) {
        
        if (propertyPointsUsedForAggregationMap.containsKey(propertyName)) {
            return propertyPointsUsedForAggregationMap.get(propertyName);
        }
        return 1;
    }
    
    @Override
    public void setNumberOfPointsUsedForAggregation(int numberOfPoints,
            String propertyName) {
        propertyPointsUsedForAggregationMap.put(propertyName, numberOfPoints);
    }
    
    @Override
    public Map<String, Integer> getPropertyPointsUsedForAggregationMap() {
        return propertyPointsUsedForAggregationMap;
    }
    
    public static Point fromMap(Map<?, ?> featureMap, String trackID) {
        Object geometryObject = featureMap.get("geometry");
        
        double[] coordinatesXY = new double[2];
        
        if (geometryObject instanceof Map<?, ?>) {
            coordinatesXY = Utils.getCoordinatesXYFromJSON((LinkedHashMap<?, ?>) geometryObject);
        }
        
        Object propertiesObject = featureMap.get("properties");
        
        if (propertiesObject instanceof Map<?, ?>) {
            Map<?, ?> propertiesMap = (Map<?, ?>) propertiesObject;
            
            String id = String.valueOf(propertiesMap.get("id"));
            
            String time = String.valueOf(propertiesMap.get("time"));
            
            Object phenomenonsObject = propertiesMap.get("phenomenons");
            
            if (phenomenonsObject instanceof Map<?, ?>) {
                Map<?, ?> phenomenonsMap = (Map<?, ?>) phenomenonsObject;
                
                Map<String, Object> propertiesofInterestMap = Utils.getValuesFromFromJSON(phenomenonsMap, jsonPropertyMapping);
                
                Point point = new InMemoryPoint(id, new DateTime(time), coordinatesXY[0], coordinatesXY[1], propertiesofInterestMap, 1, 1, trackID, new HashMap<String, Integer>());
                
                return point;
            }
        }
        
        return null;
    }
    
    @Override
    public Double getBearing() {
        if (propertyMap.containsKey("bearing")) {
            return (Double) propertyMap.get("bearing");
        }
        return null;
    }

    @Override
    public DateTime getTime() {
        return time;
    }

    @Override
    public TimeBasedCategory getTimeCategory() {
        return timeCategory;
    }

    @Override
    public void setTimeCategory(TimeBasedCategory timeCategory) {
        this.timeCategory = timeCategory;
    }
    
    
}
