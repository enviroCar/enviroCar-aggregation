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
