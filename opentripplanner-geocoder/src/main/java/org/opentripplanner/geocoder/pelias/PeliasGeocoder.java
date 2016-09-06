/* This program is free software: you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public License
 as published by the Free Software Foundation, either version 3 of
 the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>. */

package org.opentripplanner.geocoder.pelias;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.UriBuilder;

import org.opentripplanner.geocoder.Geocoder;
import org.opentripplanner.geocoder.GeocoderResult;
import org.opentripplanner.geocoder.GeocoderResults;

import com.vividsolutions.jts.geom.Envelope;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.geojson.Feature;
import org.geojson.Point;

public class PeliasGeocoder implements Geocoder {
    private String mapzenUrl;
    private Integer resultLimit;
    private String viewBox;
    private String focusPoint;
    
    private PeliasJsonDeserializer peliasJsonDeserializer; 
    
    public PeliasGeocoder() {
        peliasJsonDeserializer = new PeliasJsonDeserializer();
    }
    
    public String getMapzenUrl() {
        return mapzenUrl;
    }
    
    public void setMapzenUrl(String Url) {
        this.mapzenUrl = Url; 
    }

    public String getFocusPoint() {
        return focusPoint;
    }
    
    public void setFocusPoint(String str) {
        this.focusPoint = str; 
    }
    
    public Integer getResultLimit() {
        return resultLimit;
    }

    public void setResultLimit(Integer resultLimit) {
        this.resultLimit = resultLimit;
    }

    public String getViewBox() {
        return viewBox;
    }

    public void setViewBox(String viewBox) {
        this.viewBox = viewBox;
    }   
    
    @Override 
    public GeocoderResults geocode(String address, Envelope bbox) {
        String content = null;
        try {
            // make json request
            URL GeocoderUrl = getGeocoderUrl(address, bbox);
            URLConnection conn = GeocoderUrl.openConnection();
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
            
            StringBuilder sb = new StringBuilder(128);
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
                sb.append("\n");
            }
            reader.close();
            content = sb.toString();

        } catch (IOException e) {
            e.printStackTrace();
            return noGeocoderResult("Error parsing geocoder response");
        }
           
        GeocoderResults Results = new GeocoderResults();
        PeliasResponse jsonResults = null;
        Point p = null;
        
        try {
            jsonResults = peliasJsonDeserializer.parseResults(content);                                    
        } catch (IOException ex) {
            Logger.getLogger(PeliasGeocoder.class.getName()).log(Level.SEVERE, null, ex);
        }
                        
        if (jsonResults != null) {
            for (Feature x : jsonResults.features) {
                
                if (x.getGeometry().getClass() == Point.class) {
                    p = (Point) x.getGeometry();

                    Double lat = p.getCoordinates().getLatitude();
                    Double lng = p.getCoordinates().getLongitude();
                    String displayName = x.getProperties().get("label").toString();
                    
                    GeocoderResult geocoderResult = new GeocoderResult(lat, lng, displayName);
                    Results.addResult(geocoderResult);
                }
            }
        }
                      
        return Results;
    }
    
    private URL getGeocoderUrl(String address, Envelope bbox) throws IOException {        
        
        UriBuilder uriBuilder = UriBuilder.fromUri(mapzenUrl);

        uriBuilder.queryParam("sources", "osm");
        
        uriBuilder.queryParam("text", address);
        if (bbox != null) {
            uriBuilder.queryParam("focus.point.lat", (bbox.getMinX() + bbox.getMaxX()) / 2 );
            uriBuilder.queryParam("focus.point.lon", (bbox.getMinY() + bbox.getMaxY()) / 2 );
        } else if (viewBox != null) {     
            uriBuilder.queryParam("focus.point.lat", focusPoint.split(",")[0] );
            uriBuilder.queryParam("focus.point.lon", focusPoint.split(",")[1] );            
        }
        
        uriBuilder.queryParam("boundary.rect.min_lat", viewBox.split(";")[0].split(",")[0] );
        uriBuilder.queryParam("boundary.rect.min_lon", viewBox.split(";")[0].split(",")[1] );                       
        uriBuilder.queryParam("boundary.rect.max_lat", viewBox.split(";")[1].split(",")[0] );
        uriBuilder.queryParam("boundary.rect.max_lon", viewBox.split(";")[1].split(",")[1] );                        
        
        if (resultLimit != null) {
            uriBuilder.queryParam("size", resultLimit);
        }
        
        URI uri = uriBuilder.build();
        return new URL(uri.toString());
    }  
    
    private GeocoderResults noGeocoderResult(String error) {
        return new GeocoderResults(error);
    }

}
