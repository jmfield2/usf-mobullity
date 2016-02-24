package org.opentripplanner.api.resource;

import static org.opentripplanner.api.resource.ServerInfo.Q;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.opentripplanner.routing.graph.PoiNode;

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
import org.opentripplanner.routing.edgetype.StreetTraversalPermission;
import org.opentripplanner.routing.graph.Edge;
import org.opentripplanner.routing.graph.Graph;
import org.opentripplanner.routing.services.GraphService;
import org.opentripplanner.util.model.EncodedPolylineBean;

@Path("/routers/{routerId}/pois")
@XmlRootElement
public class POIs {

    	@Context // FIXME inject Application context
    	@Setter
    	private GraphService graphService;

		@GET
		@Produces({ MediaType.APPLICATION_JSON })
		public Map<String, ArrayList<String>> get(@PathParam("routerId") String routerId,
            @PathParam("query") String query) {

			Graph g = graphService.getGraph(routerId);
			if (g == null) return null;
		
            Map<String, ArrayList<String>> res = new HashMap<String, ArrayList<String>>();
            
            Map<String, ArrayList<PoiNode>> q = new HashMap<String, ArrayList<PoiNode>>();

            if (g.pois.containsKey( query )) {
                // XXX either an exact match, or key:value* iterative matching

                q.put( query, g.pois.get(query) );
            }
            else q = g.pois;

	        for (String k : q.keySet()) {
                ArrayList<PoiNode> ps = q.get(k);
                res.put( k, new ArrayList<String>() );

                for (PoiNode p : ps) 
                    res.get(k).add( String.format("{type: '%s', tags: '%s', locations: '%s'}", p.type, p.tags.toString(), p.locations.toString()));
            }
    
		    return res;
		}
	
}
