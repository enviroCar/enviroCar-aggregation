package org.envirocar.analyse.categories;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

/**
 *
 */
public class DEBasedCategory implements RegionalTimeBasedCategory {
    
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
