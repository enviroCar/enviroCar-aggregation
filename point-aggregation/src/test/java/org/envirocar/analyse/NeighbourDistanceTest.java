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

import org.envirocar.analyse.entities.InMemoryPoint;
import org.joda.time.DateTime;
import org.junit.Test;

/**
 *
 */
public class NeighbourDistanceTest {

//    @Test
    public void testDistance() {
        DateTime now = new DateTime();
        InMemoryPoint p = new InMemoryPoint("test-1", now,
                11.54699172, 48.60661091,
                null, 1, 1, null, null);
        
        InMemoryPoint p2 = new InMemoryPoint("test-2", now.plusSeconds(5),
                11.54695757, 48.60664279,
                null, 1, 1, null, null);
        
        PostgresPointService pps = new PostgresPointService();
        
        pps.addToResultSet(p);
        
        pps.getNearestNeighbor(p2, 0.0018, 0.0);
    }
    
}
