otp.namespace("otp.layers");

var vehicles = {};
var stopsA = {};
var stopsB = {};
var stopsC = {};
var stopsD = {};
var stopsE = {};
var stopsF = {};

otp.layers.BusPositionsLayer = 
	otp.Class(L.LayerGroup, {

		module : null,

		minimumZoomForStops : 14,
		
		visible : [], // Bus Layers that are visible
	
		route_polylines : [],
		routes: ['A', 'B', 'C', 'D', 'E', 'F'],
	
		initialize : function(module) {
			L.LayerGroup.prototype.initialize.apply(this);
			this.module = module;

			this.module.addLayer("buses", this);

			// Initialize layergroups for each route ID
			this.groups = {};
			for (x in this.routes) 
				this.groups[ this.routes[x] ] = L.layerGroup();		

			//Get the stops for each Bull Runner Route to draw the route...
			stopsA = this.module.webapp.transitIndex.getTripRoute('USF Bull Runner_1');
			stopsB = this.module.webapp.transitIndex.getTripRoute('USF Bull Runner_3');
			stopsC = this.module.webapp.transitIndex.getTripRoute('USF Bull Runner_5');
			stopsD = this.module.webapp.transitIndex.getTripRoute('USF Bull Runner_8');
			stopsE = this.module.webapp.transitIndex.getTripRoute('USF Bull Runner_11');
			stopsF = this.module.webapp.transitIndex.getTripRoute('USF Bull Runner_13');

			//Use patterns API to load geometries
			for (var x=0; x < this.routes.length; x++) {
				route = this.routes[x];

				// XXX use defined agency and detect the tripid (01) ?
			        $.ajax({
                		url: '/otp/routers/default/index/patterns/USF Bull Runner_'+route+'_01/geometries',
                		this_: this,
				rte: route,
		                dataType: 'json',
                		success: function(data) {
					this.this_.route_polylines[this.rte] = data;
				},
				});
			}

			//set map to refresh vehicle positions every 5 seconds and every map movement..
			this.module.webapp.map.lmap.on('dragend zoomend', $.proxy(this.refresh, this));
			setInterval($.proxy(this.refresh,this),5000);
		},

		getIconForRouteAndDirection : function(rte, dir) {

			switch (dir) {
			default:
			case "S": case "South":
				dir = "";
				break;
			case "SW": case "SouthWest":
				dir = "SouthEast";
				break;
			case "W": case "West":
				dir = "West";
                                break;
			case "NW": case "NorthWest":
				dir = "NorthWest";
                                break;
			case "N": case "North":
				dir = "North";
                                break;
			case "NE": case "NorthEast":
				dir = "NorthEast";
                                break;
			case "E": case "East":
				dir = "East";
                                break;
			case "SE": case "SouthEast":
				dir = "SouthEast";
                                break;
			}

			var icon = L.Icon.extend({
			        options: {
			                angle: 0,
			                iconUrl : resourcePath + 'images/' + rte + dir + '.svg',
			                iconSize: new L.Point(40,70)
			        }
			});

			return new icon();

		},

		refresh : function() {
			var lmap = this.module.webapp.map.lmap;
			if(lmap.getZoom() >= this.minimumZoomForStops) {
				this.liveMap(); //need to get updated vehicle positions
				this.setRoutes(); //need to reset routes display on the map
			}
			else this.clearLayers();
		},

		liveMap : function() {
			this_ = this;
			this.module.webapp.transitIndex.loadBusPositions(this, function(data){
				this_.vehicles = data.vehicles;
				this_.setMarkers();
			});
		},

		setMarkers: function(){
			var this_ = this;
			var v;

			for(v=0; v < this_.vehicles.length; v++){		
				var coord = L.latLng(this_.vehicles[v].lat,this_.vehicles[v].lon);
				var bearing = this_.vehicles[v].bearing;
				var route = this_.vehicles[v].routeId;
				var marker;

				switch (bearing) {
				case 0:
					dir = "N";
					break;
				case 45:
					dir = "NE";
					break;
				case 90:
					dir = "E";
					break;
				case 135:
					dir = "SE";
                                        break;
				case 180:
					dir = "S";
                                        break;
				case 225:
					dir = "SW";
                                        break;
				case 270:
					dir = "W";
                                        break;
				case 315:
					dir = "NW";
                                        break;
				default:
					console.log("BusPositionsLayer.js - Unknown direction for route " + route + " @ bearing " + bearing); 

					continue; // no icon, no marker
				}

				var icon = new this.getIconForRouteAndDirection(route, dir);

				marker = L.marker(coord,{icon : icon,}).bindPopup('Bus: ' + this_.vehicles[v].id + " Route: " + route);
				marker.on('mouseover', marker.openPopup.bind(marker));

				if ( ! this.groups[route].hasLayer(marker))
					this.groups[route].addLayer(marker);

			} // end of vehicle for loop
		
			// Check that route ID is marked visible in layers widget
			for (x in this.routes) {
				rte = this.routes[x];
				if (this.visible.indexOf(rte) != -1) {
					this_.addLayer( this.groups[rte] );
				}
				else {
					this_.removeLayer( this.groups[rte] );
				}
			}

		},
		
		setRoutes : function(){			
			//for route A:
			var routeA = new Array();
			//console.log(stopsA);
			for (var a = 0; a < stopsA.length; a++){
				var lat = stopsA[a].lat;
				var lng = stopsA[a].lon;
				var latlng = L.latLng(lat, lng);
				routeA.push(latlng);
			}
			
			//for route B:
			var routeB = new Array();
			for (var b = 0; b < stopsB.length; b++){
				var lat = stopsB[b].lat;
				var lng = stopsB[b].lon;
				var latlng = L.latLng(lat, lng);
				routeB.push(latlng);
			}
			
			//for route C:
			var routeC = new Array();
			for (var c = 0; c < stopsC.length; c++){
				var lat = stopsC[c].lat;
				var lng = stopsC[c].lon;
				var latlng = L.latLng(lat, lng);
				routeC.push(latlng);
			}
			
			//for route D:
			var routeD = new Array();
			for (var d = 0; d < stopsD.length; d++){
				var lat = stopsD[d].lat;
				var lng = stopsD[d].lon;
				var latlng = L.latLng(lat, lng);
				routeD.push(latlng);
			}			
			
			//for route E:
			var routeE = new Array();
			for (var e = 0; e < stopsE.length; e++){
				var lat = stopsE[e].lat;
				var lng = stopsE[e].lon;
				var latlng = L.latLng(lat, lng);
				routeE.push(latlng);
			}
						
			//for route F:
			var routeF = new Array();
			for (var f = 0; f < stopsF.length; f++){
				var lat = stopsF[f].lat;
				var lng = stopsF[f].lon;
				var latlng = L.latLng(lat, lng);
				routeF.push(latlng);
			}

		
			colors = {'A': '#00573C', 'B': '#0077D1', 'C':'#AC49D0', 'D':'#F70505', 'E':'#D4BA13', 'F':'#8F6A51'};

			for (x in this.routes) {
				rte = this.routes[x];

				if (this.visible.indexOf(rte) == -1) continue;

				p = this.drawRoutePolyline(this.route_polylines[rte], {'color': colors[rte]} );
				polylines = L.layerGroup(p);
				polylines._leaflet_id = 'route_' + rte;

				layer = this.groups[rte];
				if (!layer.hasLayer(polylines)) layer.addLayer(polylines);

			}

		},

		drawRoutePolyline : function(route, opts) {
			opts['clickable'] = false;

			p = [];
			for (x in route) {
				line = route[x];
				p.push( L.polyline(otp.util.Geo.decodePolyline(line['points']), opts) );
			}

			return p;
		},

	});
