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
package org.envirocar.analyse.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.envirocar.analyse.properties.GlobalProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vividsolutions.jts.geom.GeometryFactory;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.AllowAllHostnameVerifier;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.DefaultHttpClient;

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
    
    public static Map<String, Object> getValuesFromFromJSON(Map<?, ?> phenomenonMap, Map<String, String> jsonPropertyMapping) {
        
        Map<String, Object> result = new HashMap<>();
        
        for (String propertyName : GlobalProperties.getPropertiesOfInterestJson().keySet()) {
            Object propertyObject = phenomenonMap.get(propertyName);
            
            if (propertyObject == null){
                result.put(mapJsonPropertyToColumnName(propertyName, jsonPropertyMapping), 0.0);//TODO handle non number properties
            }
            else if(propertyObject instanceof LinkedHashMap<?, ?>){
                result.put(mapJsonPropertyToColumnName(propertyName, jsonPropertyMapping), ((LinkedHashMap<?, ?>)propertyObject).get("value"));
            }
            
        }
        
        return result;
        
    }
    
    private static String mapJsonPropertyToColumnName(String s, Map<String, String> jsonPropertyMapping) {
        if (jsonPropertyMapping.containsKey(s)) {
            return jsonPropertyMapping.get(s);
        }
        else {
            return s;
        }
    }
    
    public static Map<?, ?> parseJsonStream(InputStream stream) throws IOException {
        ObjectMapper om = new ObjectMapper();
        final Map<?, ?> json = om.readValue(stream, Map.class);
        return json;
    }
    
    public static boolean isNumberObjectNullOrZero(Number numberObject) {
        Number sourceValue = (Number) numberObject;
        
        double value = sourceValue.doubleValue();
        
        if (value == 0.0) {
            return true;
        }
        
        return false;
    }

    public static HttpClient createClient() throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException, UnrecoverableKeyException {
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
    
}
