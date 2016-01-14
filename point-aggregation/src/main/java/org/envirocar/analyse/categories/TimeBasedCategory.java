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
import org.joda.time.DateTimeConstants;

public enum TimeBasedCategory {
    
    WEEKDAY_6_10,
    WEEKDAY_10_15,
    WEEKDAY_15_19,
    WEEKDAY_OTHER,
    SATURDAY_6_10,
    SATURDAY_10_15,
    SATURDAY_15_19,
    SATURDAY_OTHER,
    SUNDAY_6_10,
    SUNDAY_10_15,
    SUNDAY_15_19,
    SUNDAY_OTHER,
    NO_CATEGORY;
    
    public static TimeBasedCategory fromTime(DateTime dt) {
        int dow = dt.getDayOfWeek();
        int hod = dt.getHourOfDay();
        
        String prefix;
        
        if (dow < DateTimeConstants.SATURDAY) {
            prefix = "WEEKDAY";
        }
        else if (dow == DateTimeConstants.SATURDAY) {
            prefix = "SATURDAY";
        }
        else if (dow == DateTimeConstants.SUNDAY) {
            prefix = "SUNDAY";            
        }
        else {
            return NO_CATEGORY;
        }
        
        if (hod >= 6 && hod < 10) {
            return TimeBasedCategory.valueOf(prefix.concat("_6_10"));
        }
        if (hod >= 10 && hod < 15) {
            return TimeBasedCategory.valueOf(prefix.concat("_10_15"));
        }
        if (hod >= 15 && hod < 19) {
            return TimeBasedCategory.valueOf(prefix.concat("_15_19"));
        }
        else {
            return TimeBasedCategory.valueOf(prefix.concat("_OTHER"));
        }
    }
    
}
