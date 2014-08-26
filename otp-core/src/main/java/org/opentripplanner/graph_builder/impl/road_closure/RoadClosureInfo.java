/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.opentripplanner.graph_builder.impl.road_closure;

/**
 *
 * @author mabu
 */
public class RoadClosureInfo {
    String date_on = null;
    String date_off = null;
    String day_on = null;
    String day_off = null;
    String hour_on = null;
    String hour_off = null;
    String description = null;
    String title = null;
    String url = null;
    String way_id = null;
    Boolean full = false;
    Boolean showOnly = false;
    

    @Override
    public String toString() {
        return "RoadClosureInfo{" + "date_on=" + date_on + ", date_off=" + date_off + ", day_on=" + day_on + ", day_off=" + day_off + ", hour_on=" + hour_on + ", hour_off=" + hour_off + ", description=" + description + '}';
    }
   
    public void add(String tagName, String tagValue) throws Exception {
        tagValue = tagValue.trim();
        switch (tagName) {
            case "date_on":
                date_on = tagValue;
                break;
            case "date_off":
                date_off = tagValue;
                break;
            case "day_on":
                day_on = tagValue;
                break;
            case "day_off":
                day_off = tagValue;
                break;
            case "hour_on":
                hour_on = tagValue;
                break;
            case "hour_off":
                hour_off = tagValue;
                break;
            case "description":
                description = tagValue;
                break;
            case "title":
                title = tagValue;
                break;
            case "url":
                url = tagValue;
                break;
            case "way_id":
                way_id = tagValue;
                break;
            case "full":
                full = new Boolean(tagValue.toLowerCase());
                break;
            case "show_only":
                showOnly = new Boolean(tagValue.toLowerCase());
                break;
            default:
                throw new Exception(String.format("Invalid tagName:%s", tagName));
        }
    }
    
}

