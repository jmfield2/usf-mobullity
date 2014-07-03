/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.opentripplanner.api.ws;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author mabu
 */
@XmlRootElement(name="RoadClosureList")
public class RoadClosureList {
    @XmlElements(value = {@XmlElement(name="road_closure")})
    public List<org.opentripplanner.graph_builder.impl.road_closure.RoadClosure> road_closures = new ArrayList<org.opentripplanner.graph_builder.impl.road_closure.RoadClosure>();
    
    
}
