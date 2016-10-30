/* This program is free software: you can redistribute it and/or
   modify it under the terms of the GNU Lesser General Public License
   as published by the Free Software Foundation, either version 3 of
   the License, or (at your option) any later version.
   
   This program is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.
   
   You should have received a copy of the GNU General Public License
   along with this program.  If not, see <http://www.gnu.org/licenses/>. 
*/

otp.namespace("otp.layers");

otp.layers.BikeLanesLayer = 
    otp.Class(L.LayerGroup, {
   
    module : null,
    
    minimumZoomForStops : 15,
   
    visible: false,
 
    initialize : function(module) {
        L.LayerGroup.prototype.initialize.apply(this);
        this.module = module;
        
<<<<<<< HEAD
        this.bikeLanes = {"both":[], "oneway":[]}
        this.module.addLayer("bikelanes", this);        
//        this.module.webapp.map.lmap.on('dragend zoomend', $.proxy(this.refresh, this));
=======
        this.bikeLanes = []
        this.module.addLayer("bikelanes", this);        
        this.module.webapp.map.lmap.on('dragend zoomend', $.proxy(this.refresh, this));
>>>>>>> 2439d0475e9e17f1515977f7d94f6c15fec184ea
        
        $.ajax({
        	url: '/otp/routers/default/bike_lanes', 
        	webapp: this.module.webapp,
        	this_: this,
        	dataType: 'json',
        	success: function(data) {        
        		for (x in data) {
        			row = data[x];
        			
        			var p = otp.util.Geo.decodePolyline(row);
        			
<<<<<<< HEAD
        			this.this_.bikeLanes['both'].push( p );        			        			        	
        		}
        	},
        });
       
        $.ajax({
                url: '/otp/routers/default/bike_lanes/one_way',
                webapp: this.module.webapp,
                this_: this,
                dataType: 'json',
                success: function(data) {
                        for (x in data) {
                                row = data[x];

                                var p = otp.util.Geo.decodePolyline(row);

                                this.this_.bikeLanes['oneway'].push( p );                                                                    
                        }
                },
        });

=======
        			this.this_.bikeLanes.push( p );        			        			        	
        		}
        	},
        });
        
>>>>>>> 2439d0475e9e17f1515977f7d94f6c15fec184ea
    },
    
    refresh : function() {
        this.clearLayers();                
        var lmap = this.module.webapp.map.lmap;
        if(lmap.getZoom() >= this.minimumZoomForStops && this.visible) {
<<<<<<< HEAD
        	for (p in this.bikeLanes['both']) {

    			ret=L.polyline(this.bikeLanes['both'][p], {color: 'red'});    			

    			this.addLayer(ret);
        	}

                for (p in this.bikeLanes['oneway']) {

                        ret=L.polyline(this.bikeLanes['oneway'][p], {dashArray: "5,5", color: 'red'});

                        this.addLayer(ret);
                }

		this.addTo(lmap);
=======
        	for (p in this.bikeLanes) {

    			ret=L.polyline(this.bikeLanes[p], {color: 'red'});    			

    			this.addLayer(ret).addTo(lmap);
    			
            		
        	}
>>>>>>> 2439d0475e9e17f1515977f7d94f6c15fec184ea
        }
    },
    
});
