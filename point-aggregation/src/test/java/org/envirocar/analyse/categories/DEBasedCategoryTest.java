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
package org.envirocar.analyse.categories;

import org.hamcrest.CoreMatchers;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.DateTimeZone;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 */
public class DEBasedCategoryTest {
    
    @Test
    public void testLastSunday() {
        DEBasedCategory deb = new DEBasedCategory();
        DateTime lastMarch = deb.getLastSunday(2016, 3, 31);
        
        Assert.assertThat(lastMarch.getDayOfWeek(), CoreMatchers.is(DateTimeConstants.SUNDAY));
        Assert.assertThat(lastMarch, CoreMatchers.equalTo(new DateTime(2016, 03, 27, 0, 0)));
        
        DateTime lastOct = deb.getLastSunday(2016, 10, 31);
        
        Assert.assertThat(lastOct.getDayOfWeek(), CoreMatchers.is(DateTimeConstants.SUNDAY));
        Assert.assertThat(lastOct, CoreMatchers.equalTo(new DateTime(2016, 10, 30, 0, 0)));
    }
    
    @Test
    public void testWinterTime() {
        DEBasedCategory deb = new DEBasedCategory();
        
        deb.updateTimeZone(new DateTime("2016-03-27T00:59:59Z"));
        Assert.assertThat(deb.getTimeZone().getID(), CoreMatchers.is("+01:00"));
        
        deb.updateTimeZone(new DateTime("2016-03-27T01:00:01Z"));
        Assert.assertThat(deb.getTimeZone().getID(), CoreMatchers.is("+02:00"));
        
        
        deb.updateTimeZone(new DateTime("2016-10-30T00:59:59Z"));
        Assert.assertThat(deb.getTimeZone().getID(), CoreMatchers.is("+02:00"));
        
        deb.updateTimeZone(new DateTime("2016-10-30T01:00:01Z"));
        Assert.assertThat(deb.getTimeZone().getID(), CoreMatchers.is("+01:00"));
    }
    
    @Test
    public void testCategories() {
        DEBasedCategory deb = new DEBasedCategory();
        
        /**
         * friday morning
         */
        DateTime trackTime = new DateTime("2016-03-25T5:59:59Z", DateTimeZone.UTC);
        deb.updateTimeZone(trackTime);
        
        TimeBasedCategory cat = deb.fromTime(trackTime);
        Assert.assertThat(cat, CoreMatchers.is(TimeBasedCategory.WEEKDAY_6_10));
        
        /**
         * friday mid
         */
        trackTime = new DateTime("2016-03-25T13:59:59Z", DateTimeZone.UTC);
        deb.updateTimeZone(trackTime);
        
        cat = deb.fromTime(trackTime);
        Assert.assertThat(cat, CoreMatchers.is(TimeBasedCategory.WEEKDAY_10_15));
        
        /**
         * friday mid
         */
        trackTime = new DateTime("2016-03-25T14:01:59Z", DateTimeZone.UTC);
        deb.updateTimeZone(trackTime);
        
        cat = deb.fromTime(trackTime);
        Assert.assertThat(cat, CoreMatchers.is(TimeBasedCategory.WEEKDAY_15_19));
        
        /**
         * friday other
         */
        trackTime = new DateTime("2016-03-25T18:01:59Z", DateTimeZone.UTC);
        deb.updateTimeZone(trackTime);
        
        cat = deb.fromTime(trackTime);
        Assert.assertThat(cat, CoreMatchers.is(TimeBasedCategory.WEEKDAY_OTHER));
        
        /**
         * saturday morning
         */
        trackTime = new DateTime("2016-03-26T5:59:59Z", DateTimeZone.UTC);
        deb.updateTimeZone(trackTime);
        
        cat = deb.fromTime(trackTime);
        Assert.assertThat(cat, CoreMatchers.is(TimeBasedCategory.SATURDAY_6_10));
        
        /**
         * sunday other
         */
        trackTime = new DateTime("2016-03-20T2:59:59Z", DateTimeZone.UTC);
        deb.updateTimeZone(trackTime);
        
        cat = deb.fromTime(trackTime);
        Assert.assertThat(cat, CoreMatchers.is(TimeBasedCategory.SUNDAY_OTHER));
    }

}
