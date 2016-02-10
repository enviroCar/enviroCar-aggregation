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
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.envirocar.aggregation;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author matthes
 */
public class TrackServletPathParsingTest {
    
    @Test
    public void testPathParsing() {
        AggregatedTracksServlet ts = new AggregatedTracksServlet();
        
        String[] array = ts.resolveDatabaseNameAndTrackId("/testDb/testTrack");
        
        Assert.assertThat(array.length, CoreMatchers.is(2));
        Assert.assertThat(array[0], CoreMatchers.equalTo("testDb"));
        Assert.assertThat(array[1], CoreMatchers.equalTo("testTrack"));
        
        array = ts.resolveDatabaseNameAndTrackId("//testDb/testTrack//");
        
        Assert.assertThat(array.length, CoreMatchers.is(2));
        Assert.assertThat(array[0], CoreMatchers.equalTo("testDb"));
        Assert.assertThat(array[1], CoreMatchers.equalTo("testTrack"));
        
        array = ts.resolveDatabaseNameAndTrackId("/testDb/");
        
        Assert.assertThat(array.length, CoreMatchers.is(1));
        Assert.assertThat(array[0], CoreMatchers.equalTo("testDb"));
    }
    
}