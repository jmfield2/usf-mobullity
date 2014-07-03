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


otp.layers.RoadClosureLayer = 
    otp.Class(L.LayerGroup, {
   
    module : null,
    icons       : null,
    
    minimumZoomForClosures : 10,
    minimumZoomForClosuresPoly : 14,
    
    initialize : function(module) {
        L.LayerGroup.prototype.initialize.apply(this);
        this.module = module;
        this.icons = new otp.modules.planner.IconFactory();

        this.roadClosures = [];
        
        this.module.addLayer("roadClosed", this);
        this.module.webapp.map.lmap.on('dragend zoomend', $.proxy(this.refresh, this));
    },
    
    refresh : function() {
        this.clearLayers();                
        var lmap = this.module.webapp.map.lmap;
        if(lmap.getZoom() >= this.minimumZoomForClosures) {
            this.loadClosedRoadsInRectangle(lmap.getBounds(), this, function(data) {
                /*console.log(data);*/
                this.roadClosures = [];
                for(var i = 0; i < data.road_closures.length; i++) {
                    this.roadClosures.push(data.road_closures[i]);
                }
                this.updateRoadClosures();
            });
        }
    },

    //Functions were added in moment 2.0.0 we are using 1.7.2
    isAfter: function(a,b, units) {
        units = typeof units !== 'undefined' ? units : 'millisecond';
        return +a.clone().startOf(units) > +moment(b).startOf(units);
    },

    isBefore: function(a,b, units) {
        units = typeof units !== 'undefined' ? units : 'millisecond';
        return +a.clone().startOf(units) < +moment(b).startOf(units);
    },

    isSame: function(a,b, units) {
        units = typeof units !== 'undefined' ? units : 'millisecond';
        return +a.clone().startOf(units) == +moment(b).startOf(units);
    },

    
    updateRoadClosures : function(roadClosures) {
        var roadClosures = this.roadClosures;
        var this_ = this;
        var showPolyline = false;
        //Zoom is big show polyline
        if (this.module.webapp.map.lmap.getZoom() >= this.minimumZoomForClosuresPoly) {
            showPolyline = true;
        }
        
        for(var i=0; i<roadClosures.length; i++) {

            var roadClosure = roadClosures[i];

            /*console.log(roadClosure);*/

            var context = _.clone(roadClosure);
            context.title = roadClosure.title || "Zaprta cesta";
            var from_moment = moment(context.closureStart);
            var to_moment = moment(context.closureEnd);
            var now = moment();
            var style = null;
            var diff = from_moment.diff(now, 'hours');
            var icon = null;
            //Road closure is passed
            if (this.isAfter(now,to_moment)) {
                console.info("Road opened");
                continue;
            //Closure is happening
            } else if(this.isBefore(now,to_moment) && this.isAfter(now, from_moment)) {
                context.closed = "Zaprto";
                console.info("Road closed");
                style = {color: 'red', opacity:1};
                icon = this.icons.roadClosedNow;
            } else if (this.isBefore(now, from_moment)) {
                icon = this.icons.roadClosedFuture;
                //More then 1 hour before road closes
                if (diff > 1) {
                    console.info("Road still opened");
                    context.closed = "Bo zaprta";
                    style = {
                        color: 'green',
                        opacity: 0.1
                    };
                //Road will close in one hour
                } else {
                    console.info("Road will close");
                    context.closed = "Bo kmalu zaprta";
                    style = {
                        color: 'orange',
                       opacity:0.5
                    };
                }
            }
            
            if (this.isSame(from_moment, to_moment, 'day')) {
                context.day = from_moment.format(otp.config.locale.time.date_format);
                context.from = from_moment.format(otp.config.locale.time.time_format);
                context.to = to_moment.format(otp.config.locale.time.time_format);
            } else {
                context.from = from_moment.format(otp.config.locale.time.format);
                context.to = to_moment.format(otp.config.locale.time.format);
            }
            var popupContent = ich['otp-roadClosureLayer-popup'](context);

            var polyline = new L.Polyline(otp.util.Geo.decodePolyline(roadClosure.geometry.points), style);

            //Zoom is big show polyline
            if (showPolyline) {
                polyline.addTo(this)
                .bindPopup(popupContent.get(0));
            //small zoom show only sign
            } else {
                var center_of_polyline = polyline.getBounds().getCenter();
                var marker = new L.Marker(center_of_polyline, {icon:icon, draggable: false});
                marker.addTo(this)
                .bindPopup(popupContent.get(0));
            }

        }
    },

    loadClosedRoadsInRectangle : function(bounds, callbackTarget, callback) {
        var params = {
            leftUpLat : bounds.getNorthWest().lat,
            leftUpLon : bounds.getNorthWest().lng,
            rightDownLat : bounds.getSouthEast().lat,
            rightDownLon : bounds.getSouthEast().lng,
            extended : true
        };
        if(typeof otp.config.routerId !== 'undefined') {
            params.routerId = otp.config.routerId;
        }
        
        var url = otp.config.hostname + '/' + otp.config.restService + '/ws/road_closure';
        $.ajax(url, {
            data:       params,
            dataType:   'jsonp',
                
            success: function(data) {
                callback.call(callbackTarget, data);                
            }
        });
    }
});
