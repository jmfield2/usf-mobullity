package org.opentripplanner.api.resource;

import static org.opentripplanner.api.resource.ServerInfo.Q;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.annotation.XmlRootElement;

import lombok.Setter;

import org.opentripplanner.routing.edgetype.PlainStreetEdge;
import org.opentripplanner.routing.edgetype.StreetEdge;
import org.opentripplanner.routing.edgetype.StreetTraversalPermission;
import org.opentripplanner.routing.graph.Edge;
import org.opentripplanner.routing.graph.Graph;
import org.opentripplanner.routing.services.GraphService;

import com.vividsolutions.jts.geom.Envelope;

@Path("/routers/{routerId}/bike_lanes")
@XmlRootElement
public class BikeLanes {

    	@Context // FIXME inject Application context
    	@Setter
    	private GraphService graphService;

		@GET
		@Produces({ MediaType.APPLICATION_JSON })
		public ArrayList<String> getBikeLanes(
				@PathParam("routerId") String routerId,
	            @QueryParam("lowerLeft") String lowerLeft,
	            @QueryParam("upperRight") String upperRight) {
			
			ArrayList<String> tmp = new ArrayList<String>();

	        Envelope bboxEnvelope;
	        if (lowerLeft != null) {
	            bboxEnvelope = getEnvelope(lowerLeft, upperRight);
	        } else {
	            bboxEnvelope = new Envelope(-180,180,-90,90); 
	        }
			
			Graph g = graphService.getGraph(routerId);
			Collection<StreetEdge> edges = g.streetIndex.getEdgesForEnvelope(bboxEnvelope);
			if (g == null) return null;
			
			for (Edge e : edges) {					
				if (e.getClass() == PlainStreetEdge.class) {
					if (((PlainStreetEdge) e).getPermission().allows(StreetTraversalPermission.BICYCLE_LANE)) {
						tmp.add(String.format("{\"lat1\":\"%f\", \"lon1\":\"%f\", \"lat2\":\"%f\", \"lon2\":\"%f\"}", e.getFromVertex().getLat(), e.getFromVertex().getLon(), e.getToVertex().getLat(), e.getToVertex().getLon()));	
					}								
					
				}				
			}		
			
			return tmp;
		}

	    /** Envelopes are in latitude, longitude format */
	    public static Envelope getEnvelope(String lowerLeft, String upperRight) {
	        String[] lowerLeftParts = lowerLeft.split(",");
	        String[] upperRightParts = upperRight.split(",");

	        Envelope envelope = new Envelope(Double.parseDouble(lowerLeftParts[1]),
	                Double.parseDouble(upperRightParts[1]), Double.parseDouble(lowerLeftParts[0]),
	                Double.parseDouble(upperRightParts[0]));
	        return envelope;
	    }
		
}
