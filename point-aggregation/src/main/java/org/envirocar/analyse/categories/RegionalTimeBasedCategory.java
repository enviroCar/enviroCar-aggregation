package org.envirocar.analyse.categories;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

/**
 *
 */
public interface RegionalTimeBasedCategory {

    TimeBasedCategory fromTime(DateTime time);
    
    void updateTimeZone(DateTime trackTime);
    
    DateTimeZone getTimeZone();
    
}
