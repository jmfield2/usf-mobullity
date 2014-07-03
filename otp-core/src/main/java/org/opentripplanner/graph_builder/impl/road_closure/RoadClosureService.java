/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.opentripplanner.graph_builder.impl.road_closure;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author mabu
 */
public class RoadClosureService implements Serializable {
    private static final long serialVersionUID = -12889924561246764L;
    
    private Set<RoadClosure> roadClosures = new HashSet<RoadClosure>();
    
    public Collection<RoadClosure> getClosures() {
        return roadClosures;
    }
    
    public void addRoadClosure(RoadClosure roadClosure) {
        roadClosures.remove(roadClosure);
        roadClosures.add(roadClosure);
    }
    
    public void removeRoadClosure(RoadClosure roadClosure) {
        roadClosures.remove(roadClosure);
    }
    
}
