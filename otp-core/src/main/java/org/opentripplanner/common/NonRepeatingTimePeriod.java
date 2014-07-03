/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.opentripplanner.common;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import lombok.Data;

/**
 *
 * @author mabu
 */
@Data
public class NonRepeatingTimePeriod implements Serializable {
    private static final long serialVersionUID = -597765371879835782L;
    
    private NonRepeatingTimePeriod () {
        this.timeZone = null;
    }
    
    /**
     * The timezone this is represented in.
     */
    private TimeZone timeZone;
    /*
    * Start of road closure
    */
    private long startClosure;
    
    /*
    * End of road closure
    */
    private long endClosure;
    
    public boolean isRoadClosed(long time) {
        // TODO: Timezone/locale
        
        if ((this.startClosure < time) && (time < this.endClosure)) {
            return true;
        }
        return false;
    }
    
    /**
     * Parses string parameters to dates
     * @param date_on YYYY-MM-DD when road closure starts
     * @param date_off YYYY-MM-DD when road closure ends
     * @param hour_on HH:mm when road closure starts
     * @param hour_off HH:mm when road closure ends
     * @return NonRepeatingTimePeriod with road closure start and end
     * @throws ParseException
     * @throws Exception 
     */
    public static NonRepeatingTimePeriod parseRoadClosure(String date_on,
            String date_off, String hour_on, String hour_off) throws ParseException, Exception {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        NonRepeatingTimePeriod ret = new NonRepeatingTimePeriod();
        
        Date start = df.parse(date_on + " " + hour_on);
        Date end = df.parse(date_off + " " + hour_off);
        
        System.err.println(String.format("Start:%s %s => %s", date_on, hour_on, start));
        System.err.println(String.format("End:%s %s => %s", date_off, hour_off, end));
        
        ret.startClosure = start.getTime();
        ret.endClosure = end.getTime();
        
        if (ret.startClosure > ret.endClosure) {
            throw new Exception("End closure can't be less than start");
        }
        
        return ret;
    }
    
}
