/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.opentripplanner.graph_builder.impl.road_closure;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
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
    
    StreetMatcher streetMatcher;
    
    public void setPath(File path) {
        _path = path;
    }
    
    private File[] getTSVFiles() {
        return _path.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(".tsv");
            }
        });
    }
    
    private File[] getTXTFiles() {
        return _path.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(".txt");
            }
        });
    }
    
    private RoadClosure readTsv(File filepath) throws FileNotFoundException, IOException, Exception {
        log.info("Current file: {}", filepath.getName());
        List<Coordinate> coordinates = null;
        RoadClosureInfo roadClosureInfo = null;
        coordinates = new ArrayList<>();
        roadClosureInfo = new RoadClosureInfo();
        //reads coordinates
        try (BufferedReader br = new BufferedReader(new FileReader(filepath))) {
            //reads twice because first line are column names
            String line = br.readLine();
            line = br.readLine();
            String[] values = line.split("\\t", -1); // don't truncate empty fields

            while (line != null) {
                //ignores comments
                if (!line.startsWith("#") && !line.trim().isEmpty()) {
                    values = line.split("\\t", -1); // don't truncate empty fields

                    coordinates.add(new Coordinate(Double.parseDouble(values[2]), Double.parseDouble(values[1])));
                }
                line = br.readLine();

            }
        }
        //creates linestring from coordinates
        Coordinate[] coordArray = new Coordinate[coordinates.size()];
        LineString geometry = GeometryUtils.getGeometryFactory().createLineString(
                coordinates.toArray(coordArray));
        log.info("Read coordinates: {}", coordinates.size());

        //Reads closure start/stop
        String txtfilepath = filepath.getAbsolutePath().replace("tsv", "txt");
        try (BufferedReader br = new BufferedReader(new FileReader(txtfilepath))) {
            String line = br.readLine();

            String[] values = line.split("=", -1); // don't truncate empty fields

            while (line != null) {
                values = line.split("=", -1); // don't truncate empty fields
                roadClosureInfo.add(values[0], values[1]);
                line = br.readLine();

            }
        }
        //log.info("Read road info: {}", roadClosureInfo);

        //makes time period
        NonRepeatingTimePeriod rep = NonRepeatingTimePeriod.parseRoadClosure(
                roadClosureInfo.date_on, roadClosureInfo.date_off,
                roadClosureInfo.hour_on, roadClosureInfo.hour_off);

        RoadClosure roadClosure = new RoadClosure();
        roadClosure.period = rep;
        //roadClosure.title = "Zaprta cesta zaradi Rallya";
        roadClosure.description = roadClosureInfo.description;
        roadClosure.closureStart = new Date(rep.getStartClosure());
        roadClosure.closureEnd = new Date(rep.getEndClosure());

        //FIXME: biking and walking can go through closed roads
        //Matches coordinates with street network
        List<Edge> edges = streetMatcher.match(geometry);
        if (edges != null) {
            log.info("Matched with {} edges.", edges.size());

            List<Coordinate> allCoordinates = new ArrayList<Coordinate>();
            for (Edge e : edges) {
                allCoordinates.addAll(Arrays.asList(e.getGeometry().getCoordinates()));
                PlainStreetEdge pse = (PlainStreetEdge) e;
                pse.setRoadClosedPeriod(roadClosure.period);
                //pse.setName(pse.getName() + " TOTA");
            }
            Coordinate[] coordinateArray = new Coordinate[allCoordinates.size()];
            LineString ls = GeometryUtils.getGeometryFactory().createLineString(allCoordinates.toArray(coordinateArray));
            roadClosure.geometry = ls;
            log.info("Made roadClosure: {}", roadClosure);
            return roadClosure;
        } else {
            log.warn("No edges could be matched!");
            return null;

        }
    }


    @Override
    public void buildGraph(Graph graph, HashMap<Class<?>, Object> extra) {
        log.info("Building road Closures");
        log.info("Path is:{}", _path);
        streetMatcher = new StreetMatcher(graph);
        int addedClosures = 0;

        RoadClosureService roadClosureService = new RoadClosureService();
        graph.putService(RoadClosureService.class, roadClosureService);
        File[] tsvFiles = this.getTSVFiles();
        for (int i = 0; i < tsvFiles.length; i++) {
            try {

                RoadClosure roadClosure = this.readTsv(tsvFiles[i]);
                if (roadClosure != null) {
                    roadClosureService.addRoadClosure(roadClosure);
                    addedClosures++;
                }

            } catch (Exception ex) {
                java.util.logging.Logger.getLogger(RoadClosureBuilder.class.getName()).log(Level.SEVERE, "Road closure file wasn't found", ex);
            }
        }
        log.info("Added {} road closures", addedClosures);
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
        File[] txtFiles = this.getTXTFiles();
        File[] tsvFiles = this.getTSVFiles();
        if (txtFiles.length != tsvFiles.length) {
            throw new RuntimeException(
                    String.format("TXT and TSV numbers of files should be the same! TXT:%d, TSV:%d",
                            txtFiles.length, tsvFiles.length));
        }
        for (int i = 0; i < tsvFiles.length; i++) {

            if (!tsvFiles[i].canRead()) {
                throw new RuntimeException("Can't read RoadClosure tsv file: " + tsvFiles[i]);
            }
            
            String txtfilepath = tsvFiles[i].getAbsolutePath().replace("tsv", "txt");
            File txtFile = new File(txtfilepath);
            if (!txtFile.canRead()) {
                throw new RuntimeException("Can't read RoadClosure txt file: " + txtFile);
            }
        }
    }
    
}
