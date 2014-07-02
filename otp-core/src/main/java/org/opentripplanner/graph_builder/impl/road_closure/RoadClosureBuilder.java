/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.opentripplanner.graph_builder.impl.road_closure;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import lombok.Setter;
import org.opentripplanner.common.NonRepeatingTimePeriod;
import org.opentripplanner.common.geometry.GeometryUtils;
import org.opentripplanner.graph_builder.impl.map.StreetMatcher;
import org.opentripplanner.graph_builder.services.GraphBuilder;
import org.opentripplanner.routing.edgetype.PlainStreetEdge;
import org.opentripplanner.routing.graph.Edge;
import org.opentripplanner.routing.graph.Graph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author mabu
 */
public class RoadClosureBuilder implements GraphBuilder {
    
    private static final Logger log = LoggerFactory.getLogger(RoadClosureBuilder.class);
    
    
    private File _path;
    
    public void setPath(File path) {
        _path = path;
    }
    
    private List<Coordinate> coordinates = null;
    private RoadClosureInfo roadClosureInfo = null;
    
    private void readTsv() throws FileNotFoundException, IOException, Exception {
        this.coordinates = new ArrayList<>();
        this.roadClosureInfo = new RoadClosureInfo();
        try(BufferedReader br = new BufferedReader(new FileReader(this._path))) {
            String line = br.readLine();
            line = br.readLine();
            String[] values = line.split("\\t", -1); // don't truncate empty fields
            
            
            while(line != null) {
                values = line.split("\\t", -1); // don't truncate empty fields
                this.coordinates.add(new Coordinate(Double.parseDouble(values[2]), Double.parseDouble(values[1])));
                line = br.readLine();
                
            }
        }
        log.info("Read coordinates: {}", this.coordinates.size());
        
        String txtfilepath = this._path.getAbsolutePath().replace("tsv", "txt");
        try(BufferedReader br = new BufferedReader(new FileReader(txtfilepath))) {
            String line = br.readLine();
            
            String[] values = line.split("=", -1); // don't truncate empty fields
            
            
            while(line != null) {
                values = line.split("=", -1); // don't truncate empty fields
                this.roadClosureInfo.add(values[0], values[1]);
                line = br.readLine();
                
            }
        }
        log.info("Read road info: {}", this.roadClosureInfo);
        
    }


    @Override
    public void buildGraph(Graph graph, HashMap<Class<?>, Object> extra)  {
        log.info("Path is:{}", _path);
        try {
            this.readTsv();
            StreetMatcher streetMatcher = new StreetMatcher(graph);
            Coordinate[] coordArray = new Coordinate[this.coordinates.size()];
            Geometry geometry = GeometryUtils.getGeometryFactory().createLineString(
                    this.coordinates.toArray(coordArray));
            List<Edge> edges = streetMatcher.match(geometry);
            if (edges != null) {
                log.info("Found {} edges.", edges.size());
                NonRepeatingTimePeriod rep = NonRepeatingTimePeriod.parseRoadClosure(
                        this.roadClosureInfo.date_on, this.roadClosureInfo.date_off,
                        this.roadClosureInfo.hour_on, this.roadClosureInfo.hour_off);
                for (Edge e:edges) {
                    PlainStreetEdge pse = (PlainStreetEdge) e;
                    pse.setRoadClosedPeriod(rep);
                //    pse.setName(pse.getName() + " TOTA");
                }
            }
        } catch (Exception ex) {
            java.util.logging.Logger.getLogger(RoadClosureBuilder.class.getName()).log(Level.SEVERE, "Road closure file wasn't found", ex);
        }
    }

    @Override
    public List<String> provides() {
        return Arrays.asList("road closures");
    }

    @Override
    public List<String> getPrerequisites() {
        return Arrays.asList("streets");
    }

    @Override
    public void checkInputs() {
       if (!_path.canRead()) {
           throw new RuntimeException("Can't read RoadClosure path: " + _path);
       }
       String txtfilepath = this._path.getAbsolutePath().replace("tsv", "txt");
       if (! new File(txtfilepath).canRead()) {
           throw new RuntimeException("Can't read RoadClosure path: " + txtfilepath);
       }
    }
    
}
