/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.opentripplanner.api.ws;

import com.sun.jersey.api.core.InjectParam;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.annotation.XmlRootElement;

import org.opentripplanner.api.ws.internals.GraphInternals;
import org.opentripplanner.routing.bike_rental.BikeRentalStation;
import org.opentripplanner.routing.bike_rental.BikeRentalStationService;
import org.opentripplanner.routing.graph.Graph;
import org.opentripplanner.routing.services.GraphService;
import org.springframework.beans.factory.annotation.Autowired;

import com.sun.jersey.api.spring.Autowire;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import org.opentripplanner.graph_builder.impl.road_closure.RoadClosureService;


/**
 *
 * @author mabu
 */
@Path("/road_closure")
@XmlRootElement
@Autowire
public class RoadClosure {
    @InjectParam
    private GraphService graphService;
    
    public void setGraphService(GraphService graphService) {
        this.graphService = graphService;
    }
    
    @GET
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_XML })
    public RoadClosureList getRoadClosures(
             @QueryParam("leftUpLat") Double leftUpLat, @QueryParam("leftUpLon") Double leftUpLon,
            @QueryParam("rightDownLat") Double rightDownLat,
            @QueryParam("rightDownLon") Double rightDownLon, @QueryParam("extended") Boolean extended,
            @QueryParam("routerId") String routerId) {
        Graph graph = graphService.getGraph(routerId);
        if (graph == null) return null;
        RoadClosureService roadClosureService = graph.getService(RoadClosureService.class);
        if (roadClosureService == null) return new RoadClosureList();
        Envelope envelope;

        //empty parameters
        if (leftUpLat == null || leftUpLon == null || rightDownLat == null || rightDownLon == null) {
            envelope = new Envelope(-180,180,-90,90); 
        } else {
            Coordinate upperLeft = new Coordinate(leftUpLon, leftUpLat);
            Coordinate lowerRight = new Coordinate(rightDownLon, rightDownLat);
            envelope = new Envelope(upperLeft, lowerRight);
        }
        Collection<org.opentripplanner.graph_builder.impl.road_closure.RoadClosure> roadClosures = roadClosureService.getClosures();
        List<org.opentripplanner.graph_builder.impl.road_closure.RoadClosure> out = new ArrayList<org.opentripplanner.graph_builder.impl.road_closure.RoadClosure>();
        for (org.opentripplanner.graph_builder.impl.road_closure.RoadClosure roadClosure: roadClosures) {
            if (envelope.intersects(roadClosure.geometry.getEnvelopeInternal())) {
                out.add(roadClosure);
            }
        }
        RoadClosureList rcl = new RoadClosureList();
        rcl.road_closures = out;
        return rcl;
    }
}
