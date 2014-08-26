/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.opentripplanner.graph_builder.impl.road_closure;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import java.io.Serializable;
import java.util.Date;
import java.util.Objects;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.opentripplanner.common.NonRepeatingTimePeriod;
import org.opentripplanner.model.json_serialization.EncodedPolylineJSONSerializer;
import org.opentripplanner.routing.transit_index.adapters.LineStringAdapter;

/**
 *
 * @author mabu
 */
public class RoadClosure implements Serializable {
    private static final long serialVersionUID = 83114604568089384L;
    
    @XmlAttribute
    @JsonSerialize
    public String title;
    
    @XmlAttribute
    @JsonSerialize
    public String description;
    
    @XmlAttribute
    @JsonSerialize
    public Boolean full;
    
    @XmlAttribute
    @JsonSerialize
    public String url;
    
    @XmlAttribute
    @JsonSerialize
    public Date closureStart;
    
    @XmlAttribute
    @JsonSerialize
    public Date closureEnd;
    
    @JsonSerialize(using = EncodedPolylineJSONSerializer.class)
    @XmlJavaTypeAdapter(LineStringAdapter.class)
    public LineString geometry;
    
    
    NonRepeatingTimePeriod period;

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 29 * hash + Objects.hashCode(this.closureStart);
        hash = 29 * hash + Objects.hashCode(this.closureEnd);
        hash = 29 * hash + Objects.hashCode(this.geometry);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final RoadClosure other = (RoadClosure) obj;
        if (!Objects.equals(this.closureStart, other.closureStart)) {
            return false;
        }
        if (!Objects.equals(this.closureEnd, other.closureEnd)) {
            return false;
        }
        if (!Objects.equals(this.geometry, other.geometry)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "RoadClosure{" + "title=" + title + ", description=" + description + ", closureStart=" + closureStart + ", closureEnd=" + closureEnd + ", geometry=" + geometry.getCoordinates().length + '}';
    }
    
}
