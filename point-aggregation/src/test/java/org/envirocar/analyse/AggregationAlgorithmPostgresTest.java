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
package org.envirocar.analyse;

import java.io.File;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.util.List;
import java.util.Map;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;

import org.envirocar.analyse.export.csv.CSVExport;
import org.envirocar.analyse.properties.Properties;
import org.envirocar.analyse.util.PointViaJsonMapIterator;
import org.envirocar.analyse.util.Utils;
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
        AggregationAlgorithm algorithm = new AggregationAlgorithm();
        
        HttpGet get = new HttpGet(Properties.getRequestTrackURL());
        
        HttpClient client;
        try {
            client = Utils.createClient();
        } catch (KeyManagementException | UnrecoverableKeyException
                | NoSuchAlgorithmException | KeyStoreException e) {
            throw new IllegalStateException(e);
        }
        
        HttpResponse resp = client.execute(get);
        if (resp != null && resp.getEntity() != null
                && resp.getStatusLine() != null &&
                resp.getStatusLine().getStatusCode() < HttpStatus.SC_MULTIPLE_CHOICES) {
            
            Map<?, ?> tracks = Utils.parseJsonStream(resp.getEntity().getContent());
            List<?> trackList = (List<?>) tracks.get("tracks");
            
            for (Object t : trackList) {
                String trackID = ((Map<?, ?>) t).get("id").toString();
                algorithm.runAlgorithm(trackID);
            }
            
        }
        
//        algorithm.runAlgorithm("569635d1e4b000f94da7fc7c");
//        algorithm.runAlgorithm("569635d6e4b000f94da7ff63");
        
//		try {
//			CSVExport.exportAsCSV(algorithm.getResultSet(), File.createTempFile("aggregation", ".csv").getAbsolutePath());
//		} catch (IOException e) {
//			LOGGER.error("Could not export resultSet as CSV:", e);
//		}
        
    }
}
