otp.namespace("otp.layers");

var stations = {};

otp.layers.BikeStationsLayer = 
	otp.Class(L.LayerGroup, {

		module : null,

		minimumZoomForStops : 14,

		initialize : function(module) {
			L.LayerGroup.prototype.initialize.apply(this);
			this.module = module;
			
			this.module.addLayer("bikes", this);
			this.module.webapp.map.lmap.on('dragend zoomend', $.proxy(this.refresh, this));
		},

		refresh : function() {
			this.clearLayers();
			var lmap = this.module.webapp.map.lmap;
			if(lmap.getZoom() >= this.minimumZoomForStops) {
				this.liveMap();
				this.setRoutes();
			}
		},

		liveMap : function() {
			this_ = this;
			var url = otp.config.hostname + '/' + "otp/routers/default/bike_rental";
			$.ajax(url, {
				type: 'GET',
				dataType: 'JSON',
				async: false,
				timeout: 60000,
				success: function(data){
//					var x;
//					for (x = 0; x < data.vehicles.length; x++){
//					console.log("Vehicle "+x+": id:"+data.vehicles[x].id+" route:"+data.vehicles[x].routeId+" lat:"+data.vehicles[x].lat.toFixed(3)+" lon:"+data.vehicles[x].lon.toFixed(3)+" dir:"+data.vehicles[x].bearing);
//					}
					this_.stations = data.stations;
					this_.setMarkers();
				}
			});
//			console.log(this_.vehicles);
		},

		setMarkers: function(){
			var this_ = this;
			this.clearLayers();
			var a = new Array();
			var v;
			for(v=0; v < this_.stations.length; v++){
				var coord = L.latLng(this_.stations[v].x,this_.stations[v].y);
				var marker;
				
				marker =  L.marker(coord, {icon: this.module.icons.getSmall(this_.stations[v])} ).bindPopup('Bike Rack: ' + this_.stations[v].id + " Bikes Available: " + this_.stations[v].spacesAvailable + " Spaces: " + this_.stations[v].bikesAvailable);
				marker.on('mouseover', marker.openPopup.bind(marker));
				
				marker.addTo(this_);
				//a.push(marker);
				
			}
			
			//L.layerGroup(a).addTo(this_);
		},
		
		setRoutes : function(){
//			var routeData = this.module.webapp.transitIndex.routes;
//			if(routeData['USF Bull Runner_A']){};
//			if(routeData['USF Bull Runner_B']){};
//			if(routeData['USF Bull Runner_C']){};
//			if(routeData['USF Bull Runner_D']){};
//			if(routeData['USF Bull Runner_E']){};
//			if(routeData['USF Bull Runner_F']){};
		},
	});
