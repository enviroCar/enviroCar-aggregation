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

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class DEBasedCategory implements RegionalTimeBasedCategory {
    
    private static final Logger logger = LoggerFactory.getLogger(DEBasedCategory.class);
    
    private DateTimeZone timeZone;

    @Override
    public TimeBasedCategory fromTime(DateTime time) {
        DateTime zoned = new DateTime(time, timeZone);
        
        return TimeBasedCategory.fromTime(zoned);
    }

    @Override
    public void updateTimeZone(DateTime trackTime) {
        int moy = trackTime.getMonthOfYear();
        String utcOffset;
        
        if (moy > 3 && moy < 10) {
            utcOffset = "+02:00";
        }
        else if (moy < 3 || moy > 10) {
            utcOffset = "+01:00";
        }
        
        else if (moy == 3) {
            DateTime lastSundayOfMarch = new DateTime(getLastSundayOfMarch(trackTime.getYear()),
                    DateTimeZone.forOffsetHours(1)).plusHours(2);
            logger.info("lastSundayOfMarch: "+lastSundayOfMarch.toString());
            logger.info("trackTime: "+trackTime.toString());
            if (trackTime.isBefore(lastSundayOfMarch)) {
                utcOffset = "+01:00";
            }
            else {
                utcOffset = "+02:00";
            }
        }
        else if (moy == 10) {
            DateTime lastSundayOfMarch = new DateTime(getLastSundayOfOctober(trackTime.getYear()),
                    DateTimeZone.forOffsetHours(2)).plusHours(3);
            if (trackTime.isBefore(lastSundayOfMarch)) {
                utcOffset = "+02:00";
            }
            else {
                utcOffset = "+01:00";
            }
        }
        else {
            utcOffset = "+01:00";
        }
        
        this.timeZone = DateTimeZone.forID(utcOffset);
    }

    protected DateTime getLastSunday(int year, int month, int day) {
        DateTime lastDay = new DateTime(year, month, day, 0, 0);

        int dow = lastDay.getDayOfWeek();
        
        if (dow == 7) {
            return lastDay;
        }
        else {
            return lastDay.minusDays(dow);
        }
    }

    private DateTime getLastSundayOfMarch(int year) {
        return getLastSunday(year, 3, 31);
    }

    private DateTime getLastSundayOfOctober(int year) {
        return getLastSunday(year, 10, 31);
    }

    @Override
    public DateTimeZone getTimeZone() {
        return timeZone;
    }
    

}
