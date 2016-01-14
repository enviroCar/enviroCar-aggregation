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
    
    private static Map<String, Class<?>> getPropertiesOfInterestDatabase;
    private static Map<String, Class<?>> getPropertiesOfInterestJson;
    
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
    public static Map<String, Class<?>> getPropertiesOfInterestDatabase(){
        
        if(getPropertiesOfInterestDatabase == null){
            getPropertiesOfInterestDatabase = new HashMap<>();
            
            getPropertiesOfInterestDatabase.put("co2", Double.class);
            getPropertiesOfInterestDatabase.put("speed", Double.class);
            getPropertiesOfInterestDatabase.put("bearing", Double.class);
        }
        return getPropertiesOfInterestDatabase;
    }
    
    //TODO make configurable
    public static Map<String, Class<?>> getPropertiesOfInterestJson(){
        
        if(getPropertiesOfInterestJson == null){
            getPropertiesOfInterestJson = new HashMap<>();
            
            getPropertiesOfInterestJson.put("CO2", Double.class);
            getPropertiesOfInterestJson.put("Speed", Double.class);
            getPropertiesOfInterestJson.put("GPS Bearing", Double.class);
        }
        return getPropertiesOfInterestJson;
    }
    
}
