/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.opentripplanner.geocoder.pelias;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.ArrayList;
import java.util.Map;

/**
 *
 * @author icmp
 */

@JsonIgnoreProperties(value = { "geocoding" })
public class PeliasResponse {
    public Map<String, String> geocoding;
    public String type;
    public ArrayList<org.geojson.Feature> features;
    public ArrayList<Float> bbox;       
}
