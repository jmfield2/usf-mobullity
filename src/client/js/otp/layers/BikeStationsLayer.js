otp.namespace("otp.layers");

var vehicles = {};

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
					this_.vehicles = data.stations;
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
			for(v=0; v < this_.vehicles.length; v++){
				var coord = L.latLng(this_.vehicles[v].y,this_.vehicles[v].x);
				var marker;
				
				marker =  L.marker(coord, {icon: this.module.icons.getSmall(this_.vehicles[v])} ).bindPopup('Bike Rack: ' + this_.vehicles[v].id + " Bikes Available: " + this_.vehicles[v].spacesAvailable + " Spaces: " + this_.vehicles[v].bikesAvailable);
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
