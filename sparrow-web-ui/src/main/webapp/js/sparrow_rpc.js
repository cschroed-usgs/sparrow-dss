var appbase = location.host;
var contextroot = location.pathname.split('/')[1];
appbase += '/' + contextroot;

var modelSourcesCache;

var modelSourcesAreInCache = function(id) {
	if(!modelSourcesCache) {
		modelSourcesCache = new Array();
	}
    for (var i = 0; i < modelSourcesCache.length; i++) {
        if (modelSourcesCache[i]["@id"] == model_id) {
        	return true;
        }
    }
    return false;
}

/**
 * Retrieve the model name and list of sources for the current model.
 */
function getModel() {
    if (modelSourcesAreInCache(model_id)) {
        // Pull it out of cache
        renderModel();
    } else {
        // Make a request to the model service for all of the sources
        var xmlreq = ''
            + '<?xml version="1.0" encoding="ISO-8859-1" ?>'
            + '<sparrow-meta-request '
            + '  xmlns="http://www.usgs.gov/sparrow/meta_request/v0_1" '
            + '  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">'
            + '  <model id="' + model_id + '" >'
            + '  </model>'
            + '</sparrow-meta-request>'
            ;

        // Send a request to the model service for a list of sources
        Ext.Ajax.request({
        	method: 'POST',
        	url: 'getSources',
        	success: renderModel,
        	params: {
        		xmlreq: xmlreq,
        		mimetype: 'json'
        	}
        });
    }
}

/**
 * Renders the model source options to the page.  This function renders the
 * options to the data series dropdown, and to the treatments tab used when
 * creating a group.
 *
 *
 * TODO: // THIS SHOULD ALL BE DONE USING EXT!!!!!
 *
 *
 */
function renderModel(response, options) {
    // Pull out the sources response text and cache it
	if(response) {
		modelSourcesCache.push(Ext.util.JSON.decode(response.responseText)["models"]["model"][0]);
	}

    // Get the model name, list of sources, and model bounds
    var modelName = '';
    var sourceList = [];
    var themeName = "";
    var boundNorth = 0;
    var boundEast = 0;
    var boundSouth = 0;
    var boundWest = 0;
    var constituent = '';
    var units = '';
    var docUrl = '';

    for (var i = 0; i < modelSourcesCache.length; i++) {
        // Check the model id and pull the sources and bounds if we find the right model
        if (modelSourcesCache[i]["@id"] == model_id) {
            modelName = modelSourcesCache[i]["name"];
            themeName = modelSourcesCache[i]["themeName"];
            sourceList = modelSourcesCache[i]["sources"]["source"];
            boundNorth = parseFloat(modelSourcesCache[i]["bounds"]["@north"]);
            boundEast = parseFloat(modelSourcesCache[i]["bounds"]["@east"]);
            boundSouth = parseFloat(modelSourcesCache[i]["bounds"]["@south"]);
            boundWest = parseFloat(modelSourcesCache[i]["bounds"]["@west"]);
            constituent = modelSourcesCache[i]["constituent"];
            units = modelSourcesCache[i]["units"];
            try {
            	docUrl = modelSourcesCache[i]["url"];
            } catch(e) {}
            break;
        }
    }
    
    Sparrow.SESSION.setModelName(modelName);
    Sparrow.SESSION.setThemeName(themeName);
    Sparrow.SESSION.setModelConstituent(constituent);
    Sparrow.SESSION.setModelUnits(units);
    Sparrow.SESSION.setOriginalBoundSouth(boundSouth);
    Sparrow.SESSION.setOriginalBoundNorth(boundNorth);
    Sparrow.SESSION.setOriginalBoundWest(boundWest);
    Sparrow.SESSION.setOriginalBoundEast(boundEast);
    var docMenu = Ext.menu.MenuMgr.get('sparrow-documentation-menu');
    docMenu.removeAll();
    if(docUrl != null) {
    	Sparrow.SESSION.setDocUrl(docUrl);
        docMenu.add({
        	text: 'About: ' + Sparrow.SESSION.getModelName() + '...',
        	handler: function() {
        		var docUrl = Sparrow.SESSION.getDocUrl();
        		var newWindow = window.open(docUrl, '_blank');
        		newWindow.focus();
        	}
        });
    }
    docMenu.add({
    	text: 'What is SPARROW?',
    	handler: function() {
    		var newWindow = window.open('http://pubs.usgs.gov/fs/2009/3019/pdf/fs_2009_3019.pdf', '_blank');
    		newWindow.focus();
    	}
    });
   docMenu.add({
    	text: 'SPARROW Applications & Documentation',
    	handler: function() {
    		var newWindow = window.open('http://water.usgs.gov/nawqa/sparrow/', '_blank');
    		newWindow.focus();
    	}
    });
   docMenu.add({
   	text: 'SPARROW FAQs',
   	handler: function() {
   		var newWindow = window.open('http://water.usgs.gov/nawqa/sparrow/FAQs/faq.html', '_blank');
   		newWindow.focus();
   	}
   });
   docMenu.add('-');
   docMenu.add({
	   text: 'Tutorial Videos',
	   style: {'font-weight': 'bold', 'font-size': '110%'}
   });
   docMenu.add({
	   text: 'Video windows can be resized to show full detail',
	   style: {'font-style': 'italic'}
   });
   docMenu.add('-');
   docMenu.add({
	   	text: 'Video: Working with Sources',
	   	handler: function() {
	   		var newWindow = window.open('screencast.jsp?file-name=Sources', '_blank', 
	   				'resizable=1,location=0,status=0,scrollbars=0,width=640,height=384');
	   		newWindow.focus();
	   	}
   });
   docMenu.add({
	   	text: 'Video: Incremental Yield',
	   	handler: function() {
	   		var newWindow = window.open('screencast.jsp?file-name=IncrementalYield', '_blank', 
	   				'resizable=1,location=0,status=0,scrollbars=0,width=640,height=384');
	   		newWindow.focus();
	   	}
   });
   docMenu.add({
	   	text: 'Video: Selecting Downstream Outlets',
	   	handler: function() {
	   		var newWindow = window.open('screencast.jsp?file-name=SelectDownstreamOutlet', '_blank', 
	   				'resizable=1,location=0,status=0,scrollbars=0,width=640,height=384');
	   		newWindow.focus();
	   	}
   });
   docMenu.add({
	   	text: 'Video: Changing Source Inputs',
	   	handler: function() {
	   		var newWindow = window.open('screencast.jsp?file-name=ChangeSourceInputs', '_blank', 
	   				'resizable=1,location=0,status=0,scrollbars=0,width=640,height=384');
	   		newWindow.focus();
	   	}
  });
  docMenu.add({
	   	text: 'Video: Incremental Yield to an Outlet',
	   	handler: function() {
	   		var newWindow = window.open('screencast.jsp?file-name=IncrementalYieldtoOutlet', '_blank', 
	   				'resizable=1,location=0,status=0,scrollbars=0,width=640,height=384');
	   		newWindow.focus();
	   	}
  });
   
   Ext.getCmp('map-options-tab').autoBinsChk.setValue(Sparrow.SESSION.isBinAuto());
    
    // Zoom and center the map over the model's bounds
    if (response && boundEast != undefined && boundEast != 0) {
        map1.fitToBBox(boundEast, boundSouth, boundWest, boundNorth);
    }

    // Render the appropriate model 'theme'
    var siteTitleBar = document.getElementById('title-model-name');
    siteTitleBar.innerHTML = Sparrow.SESSION.getModelName();

    // Get the treaments tab from the group defintion window
    var treatmentTab = document.getElementById('treatment-tab');
    treatmentTab.innerHTML = '';

    // Iterate over the sources
    var mapOptionsTab = Ext.getCmp('map-options-tab');
    mapOptionsTab.clearSources();

    for (var i = 0; i < sourceList.length; i++) {

        // Add to the data series source select
        var displayName = sourceList[i]["displayName"];
        var description = sourceList[i]["description"];
        mapOptionsTab.addSource(displayName, i + 1, description);

        // Add a row to the treatment tab
        var data_row = document.createElement('div');
        data_row.className = 'data_row clearfix';
        (i%2) ? data_row.style.backgroundColor = '#FFFFFF' : data_row.style.backgroundColor = '#EEEEEE';

        var src_name = document.createElement('div');
        src_name.className = 'col_25';
        src_name.innerHTML = sourceList[i]["displayName"];

        var src_constituent = document.createElement('div');
        src_constituent.className = 'col_20';
        src_constituent.innerHTML = sourceList[i]["constituent"] + ' (' + sourceList[i]["units"] + ')';

        var src_adj_div = document.createElement('div');
        src_adj_div.className = 'col_20';
        var src_adj = Sparrow.USGS.createElement('select','treatment-tab_src_adj');
        src_adj.id = 'treatment-tab_src_adj_' + i;
        src_adj.size = 1;
        for (var j = 0; j <= 8; j++) {
            var opt = new Option(j * 0.25, j * 0.25);
            src_adj.options[j] = opt;
        }
        src_adj.value = 1;

        var src_cust = document.createElement('div');
        src_cust.className = 'col_30';
        //src_cust.innerHTML = '<a href="#" onclick="return false">customize...</a>';
        var src_cust_a = document.createElement('a');
        src_cust_a.href = "";
        src_cust_a.index = i;
        src_cust_a.innerHTML = 'enter custom multiplier...';
        src_cust_a.onclick = function() {
            var idx = this.index;
            var src_adj_sel = document.getElementById('treatment-tab_src_adj_' + idx);
            var reply = parseFloat(prompt("Enter new value for multiplier:",""));
            if (!isNaN(reply)) {
                src_adj_sel.value = reply;
                if (src_adj_sel.value != reply) { //number doesn't exist in list already, add it
                    src_adj_sel.options[9] = new Option(reply,reply);
                    src_adj_sel.value = reply;
                }
            }
            return false;
        };

        src_cust.appendChild(src_cust_a);
        treatmentTab.appendChild(data_row);
        data_row.appendChild(src_name);
        data_row.appendChild(src_constituent);
        data_row.appendChild(src_adj_div);
        src_adj_div.appendChild(src_adj);
        data_row.appendChild(src_cust);
    }

    mapOptionsTab.filterSourceCombo();
}

var IDENTIFY = new (function(){
	var self = this;
	var idRequest;
	var clicked_lat, clicked_lon;
	
	

	/**
	 * user had identify tool selected and clicked on map
	 */
	this.identifyPoint = function(event, map) {
		event = event || window.event;

		//calc lat/lon of mouse click event
		var coordsPx = JMap.util.getRelativeCoords(event, map.pane);
		var ll = map.getLatLonFromScreenPixel(coordsPx.x, coordsPx.y);

		//destroy any previous popup stuff
		if (map.identifyPopup) map.identifyPopup.kill();

		self.identifyReach(ll.lat,ll.lon, null, 0, true);
	};

	this.identifyPoint.cursor = "cursor-identify-point";
	

	/**
	 * Initiates a request to the reach identify service.  This function can make a
	 * request based on the user clicking a specific point on the map (lat/lon), or
	 * by the reach id (reachId).  
	 * 
	 * animOpts is defined in svn_overlay
	 */
	this.identifyReach = function(lat, lon, reachId, animOpts, showInfo) {
		if(!(context_id)) {
			getContextIdAsync({
		    	callback: function() {
					IDENTIFY.identifyReachWithContextId(lat, lon, reachId, animOpts, showInfo);
				},
				isIdentifying: true
			});
		}
		else {
			IDENTIFY.identifyReachWithContextId(lat, lon, reachId, animOpts, showInfo);
		}
	}
	this.identifyReachWithContextId = function(lat, lon, reachId, animOpts, showInfo) {

		if (Ext.isNumber(animOpts)){
			// user gave a standard option set
			animOpts = SvgOverlay.standardAnimateOptions[animOpts];
		}

		if ( animOpts.showPleaseWait )
			IDENTIFY_REACH_SPINNER.show();

	    // Begin building the IdByPoint request
		//var b = getContextIdAsync({
		//	scope: this,
		//	callback: function() {
		
			    var xmlreq = ''
			        + '<sparrow-id-request xmlns="http://www.usgs.gov/sparrow/id-point-request/v0_2" '
			        + 'xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">';
			        if (context_id) xmlreq += '<context-id>' + context_id + '</context-id>';
			        else xmlreq += '<model-id>' + model_id + '</model-id>';


			    // Identify by reach id or point clicked on the map
			    if (!reachId) {
			        xmlreq += '<point lat="' + lat + '" long="' + lon + '" />';
			        clicked_lat = lat;
			        clicked_lon = lon;
			    } else {
			        xmlreq += '<reach id="' + reachId + '" />';
			    }

			    // Request adjustments and attributes from the service
			    xmlreq += ''
			        + '<content>'
			        //+ ((animate == 1)? '<adjustments /><attributes />' : '') trying to change as to request less content when zooming. doesn't work. TODO debug later
			        + '<adjustments /><attributes />'
			        + '</content>'
			        + '<response-format><mime-type>json</mime-type></response-format>'
			        + '</sparrow-id-request>'
			        ;

			    last_id_JSON = null;
			    this.abort();
			    idRequest = Ext.Ajax.request({
			    	method: 'POST',
			    	url: 'getIdentify',
			    	success: function(r,o) {
			    		this.loadIdentifyReach(r,animOpts, showInfo);
			    		if (animOpts.showPleaseWait)
			    			IDENTIFY_REACH_SPINNER.hide();
			    	},
			    	failure: function(r,o) {
			    		Ext.Msg.alert('Warning', 'identify failed');
			    		if (animOpts.showPleaseWait)
			    			IDENTIFY_REACH_SPINNER.hide();
			    	},
			    	params: {
			    		xmlreq: xmlreq,
			    		mimetype: 'json'
			    	},
			    	scope: this
			    });
		//	 }
		//});

	};

	/**
	 * Callback function for the Reach Identify service.  This function receives the
	 * callback and decodes the response.  It then delegates to the render function.
	 */
	this.loadIdentifyReach =  function(response, animateOptions, showInfo) {
	    var srcJSON = response.responseText;
	    var id_JSON = Ext.util.JSON.decode(srcJSON);

	    self.renderIdentify(id_JSON, animateOptions, showInfo);

	    idRequest = null;
	};


	/**
	 * Renders the reach information contained within reachResponse to the user
	 * interface.  Calculates and draws a bounding box around the reach, places a
	 * marker at the point clicked, and opens the Reach Identify window.
	 */
	this.renderIdentify = function(reachResponse, animateOptions, showInfo) {

		{ // Check the status of the response
			var status = reachResponse["sparrow-id-response"]["status"];

			if (status != "OK") {
				// If failed, display message to user and return
				var message = reachResponse["sparrow-id-response"]["message"];
    			IDENTIFY_REACH_SPINNER.hide();
				Ext.Msg.alert('Identify', message);
	        	return;
			}
		}
		//
		//
		//
		//
		///
		///
		///
		//
		//
		///
		//
		///
		///
		SvgOverlay.removeAllOverlays();
		var geom = reachResponse["sparrow-id-response"]["results"]["result"][0]["identification"]["ReachGeometry"]["basin"];
		SvgOverlay.renderGeometry(geom, animateOptions, 'black', 'white');
		
		if (showInfo) {
			renderReachIdentifyWindow(reachResponse);
		}
		
	    clicked_lat = null;
	    clicked_lon = null;
		
//		{ // Get the bounding box of the identified reach
//			var bbox = reachResponse["sparrow-id-response"].results.result[0].identification.bbox;
//			var xmin = parseFloat(bbox["@min-long"]);
//			var ymin = parseFloat(bbox["@min-lat"]);
//			var xmax = parseFloat(bbox["@max-long"]);
//			var ymax = parseFloat(bbox["@max-lat"]);
//
//			var marker_lat = (clicked_lat || parseFloat(bbox["@marker-lat"]));
//			var marker_lon = (clicked_lon || parseFloat(bbox["@marker-long"]));
//		}
//
//		{ //create reach svg shape
//			// TODO refactor into utility method
//			var reachPoints = [];
//			var polygon = reachResponse["sparrow-id-response"].results.result[0].identification.polygon;
//			var polyLine = reachResponse["sparrow-id-response"].results.result[0].identification.polyLine;
//			var source = polygon || polyLine;
//			if (source != null){
//				var isPolygon = (polygon != null);
//				var isPolyLine = (polyLine != null);
//				var reachPointPairs = source["@points"].trim().split(' ');
//
//				xmin = 180;
//				ymin = 90;
//				xmax = -180;
//				ymax = -90;
//
//				for (var i = 0; i < reachPointPairs.length; i++) {
//					var tp = reachPointPairs[i].split(',')
//					var lat = parseFloat(tp[1]);
//					var lon = parseFloat(tp[0]);
//
//					if (lat > ymax) ymax = lat;
//					if (lat < ymin) ymin = lat;
//					if (lon > xmax) xmax = lon;
//					if (lon < xmin) xmin = lon;
//
//					reachPoints.push(lat);
//					reachPoints.push(lon)
//				}
//			}
//		}
//
//	    clicked_lat = null;
//	    clicked_lon = null;
//
//	    if (animateOptions.reCenterMap) { // recenter window to reach, zooming if necessary
//
//	    	var xExtent = xmax - xmin;
//	    	var yExtent = ymax - ymin;
//
//	    	// This temporarily centers the catchment squarely in upper right of the map
//	    	// and sets the zoom level
//	    	if (animateOptions.zoom) map1.fitToBBox(xmin - xExtent, ymin - yExtent, xmax, ymax);
//
//
//	        // Calculate how far south and west we should center the map from the reach
//	        var vBBox = map1.getViewportBoundingBox();
//	        var yDelta = (vBBox.ymax - vBBox.ymin) / 4;
//	        var xDelta = (vBBox.xmax - vBBox.xmin) / 4;
//
//	        // Calculate the center point of the reach's bounding box
//	        var y = (ymax + ymin) / 2;
//	        var x = (xmax + xmin) / 2;
//
//	        // Animate the map to the point south and west of the reach's box
//	        map1.moveTo(y - yDelta, x - xDelta);
//
//	    }
//
//		IDENTIFY_REACH_SPINNER.hide();
//	    if (animateOptions.showInfo) renderReachIdentifyWindow(reachResponse);
//
//			if (SvgOverlay.hasOverlay()) {
//				SvgOverlay.removeAllOverlays();
//			}
//
//	    if (reachPoints.length > 0) {
//	    	if (isPolygon){
//		        map1.drawShape(new JMap.svg.Polygon({
//		        	points: reachPoints,
//		        	properties: {
//		    	    	stroke: 'black',
//		    	    	'stroke-width': '4px',
//		    	    	fill: 'white',
//		    	    	'fill-opacity': 0.1,
//		    	    	'stroke-linejoin': 'round'
//		    	    },
//		    	    events: {
//		    	    	//onclick: function(e) {alert('identify handler'); return false;}
//		    	    }
//		        }));
//	    	} else if (isPolyLine){
//		        map1.drawShape(new JMap.svg.PolyLine({
//		        	points: reachPoints,
//		        	properties: {
//		    	    	stroke: 'yellow',
//		    	    	'stroke-width': '9px',
//		    	    	fill: 'white',
//		    	    	'fill-opacity': 0.1,
//		    	    	'stroke-linejoin': 'round'
//		    	    }
//		        }));
//		        map1.drawShape(new JMap.svg.PolyLine({
//		        	points: reachPoints,
//		        	properties: {
//		    	    	stroke: 'black',
//		    	    	'stroke-width': '4px',
//		    	    	fill: 'white',
//		    	    	'fill-opacity': 0.1,
//		    	    	'stroke-linejoin': 'round'
//		    	    },
//		    	    events: {
//		    	    	//onclick: function(e) {alert('identify handler'); return false;}
//		    	    }
//		        }));
//	    	}
//
//				SvgOverlay.internalNotifyOverlayAdded(false, true);
//
//	    }

	    /*
	    // Draw a bounding box around the reach and place a marker
	    map1.FOIManager.removeAllFOIs();
	    map1.FOIManager.addFOI(new IdentifyMarker({
	    	map: map1,
	    	minLat: ymin,
	    	minLon: xmin,
	    	maxLat: ymax,
	    	maxLon: xmax
	    }));
	    */
	};
	
	/**
	 * user had identify calibration tool selected and clicked on map
	 */
	this.identifyCalibrationSite = function(event, map) {
		event = event || window.event;

		//calc lat/lon of mouse click event
		var coordsPx = JMap.util.getRelativeCoords(event, map.pane);
		var ll = map.getLatLonFromScreenPixel(coordsPx.x, coordsPx.y);
		var model_id = Sparrow.SESSION.PredictionContext["@model-id"];

		//destroy any previous popup stuff
		if (map.identifyPopup) map.identifyPopup.kill();

		self.identifyCalibSite(ll.lat, ll.lon, model_id);
	};
	this.identifyCalibrationSite.cursor = "cursor-identify-point";

	this.identifyCalibSite = function(lat, lon, model_id) {
		IDENTIFY_CALIB_SITE_SPINNER.show();
		Ext.Ajax.request({
	    	method: 'POST',
	    	url: 'getCalibrationSite',
	    	success: function(r,o) {
	    		this.loadIdentifyCalibrationSite(r);
	    			IDENTIFY_CALIB_SITE_SPINNER.hide();
	    	},
	    	failure: function(r,o) {
	    		Ext.Msg.alert('Warning', 'identify failed');
	    			IDENTIFY_CALIB_SITE_SPINNER.hide();
	    	},
	    	params: {
	    		lat: lat,
	    		lon: lon,
	    		model_id: model_id
	    	},
	    	scope: this
	    });
	};
	this.loadIdentifyCalibrationSite =  function(response) {
		var responseObj = response.responseXML.lastChild;
		if(responseObj.getElementsByTagName('stationId')[0]) {
		    var stationId =  responseObj.getElementsByTagName('stationId')[0].firstChild.nodeValue;
		    var stationName = responseObj.getElementsByTagName('stationName')[0] ? responseObj.getElementsByTagName('stationName')[0].firstChild.nodeValue : '';
		    var reachId = responseObj.getElementsByTagName('reachId') ? responseObj.getElementsByTagName('reachId')[0].firstChild.nodeValue : '';
		    var reachName =  responseObj.getElementsByTagName('reachName') ? responseObj.getElementsByTagName('reachName')[0].firstChild.nodeValue : '';
		    var lat = responseObj.getElementsByTagName('latitude') ? parseFloat(responseObj.getElementsByTagName('latitude')[0].firstChild.nodeValue) : '';
		    var lon = responseObj.getElementsByTagName('longitude') ? parseFloat(responseObj.getElementsByTagName('longitude')[0].firstChild.nodeValue) : '';
		    var actual = responseObj.getElementsByTagName('actualValue') ? parseFloat(responseObj.getElementsByTagName('actualValue')[0].firstChild.nodeValue) : '';
		    var predict = responseObj.getElementsByTagName('predictedValue') ? parseFloat(responseObj.getElementsByTagName('predictedValue')[0].firstChild.nodeValue) : '';
		    
		    //getPixelFromLatLon
		    //getLatLonFromPixel
		    //Draw the marker square
		    var pixelRadius = 7;
		    var originalPixel = map1.getPixelFromLatLon(lon, lat);
		    var leftPoint = map1.getLatLonFromPixel(originalPixel.x, originalPixel.y-pixelRadius);
		    var topPoint = map1.getLatLonFromPixel(originalPixel.x+pixelRadius, originalPixel.y);
		    var rightPoint = map1.getLatLonFromPixel(originalPixel.x, originalPixel.y+pixelRadius);
		    var bottomPoint = map1.getLatLonFromPixel(originalPixel.x-pixelRadius, originalPixel.y);
		    
		    var marker = new JMap.svg.PolyLine({
	        	points: [
	        	         leftPoint.lon, leftPoint.lat, 
	        	         topPoint.lon, topPoint.lat, 
	        	         rightPoint.lon, rightPoint.lat, 
	        	         bottomPoint.lon, bottomPoint.lat, 
	        	         leftPoint.lon, leftPoint.lat],
	        	properties: {
	    	    	stroke: 'red',
	    	    	'stroke-width': '2px',
	    	    	fill: 'white',
	    	    	'fill-opacity': 0.1,
	    	    	'stroke-linejoin': 'round'
	    	    }
	        });
		    map1.drawShape(marker);
		    var window = new Ext.Window({
		    	width: 300,
		    	height: 250,
		    	resizable: false,
		    	title: 'Station ID: '+ stationId,
		    	padding: 5,
		    	html: //TODO maybe use a template
		    		"Station ID: "+ stationId + "<br/>"+
		    		"Station Name: "+ stationName + "<br/>"+
		    		"Reach: "+ reachName+ " (ID: "+reachId+")" + "<br/>"+
		    		"Latitude: "+ lat + "<br/>"+
		    		"Longitude: "+ lon + "<br/>" +
		    		"Measured Total Load (kg&middot;year<sup>-1</sup>): " + actual + "<br/>" +
		    		"Predicted Total Load (kg&middot;year<sup>-1</sup>): " + predict + "<br/>" +
		    		"Measured / Predicted: " + Math.round((actual/predict)*1000)/1000
		    	, 
		    	listeners: {
		    		close: function() {
		    			map1._SVGManager.removeShape(marker);
		    		}
		    	},
		    	scope: this
		    });
		    window.show();
		}
	};
	
	

	
	
	this.abort = function(){
		Ext.Ajax.abort(idRequest);
	}

})();


/**
 * Initiates a request to the Reach Identify service to update the Predicted
 * Values tab.
 */
function updateReachPredictions(reachId) {
	//getContextIdAsync({
	//	scope: this,
	//	callback: function() {
		    var xmlreq = ''
		        + '<sparrow-id-request '
		        + 'xmlns="http://www.usgs.gov/sparrow/id-point-request/v0_2" '
		        + 'xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">'
		        + '<context-id>' + context_id + '</context-id>'
		        + '<reach id="' + reachId + '" />'
		        + '<content><predicted /></content>'
		        + '<response-format><mime-type>json</mime-type></response-format>'
		        + '</sparrow-id-request>'
		        ;

		    //document.getElementById('debug').innerHTML = xmlreq.replace(/</g,'&lt;');
		    Ext.Ajax.request({
		    	method: 'POST',
		    	url: 'getIdentify',
		    	success: renderPredictionsTabOnly,
		    	params: {
		    		xmlreq: xmlreq,
		    		mimetype: 'json'
		    	}
		    });
	//	}
	//});
}

/**
 * Callback function for the Reach Identify service when initiated by the
 * updateReachPredictions() function.  This function receives the callback,
 * decodes the response, and delegates rendering.
 */
function renderPredictionsTabOnly(response, options) {
    var srcJSON = response.responseText;
    var id_JSON = Ext.util.JSON.decode(srcJSON);

    // TODO: Should we delegate this through the ReachIdentify window instead?
    Ext.getCmp('predictedValuesTabGrid').refresh(id_JSON);
    renderGraph(id_JSON);
}

/**
 * Renders the Graph tab in the Reach Identify window.  Pulls the data out of
 * the prediction response, and builds URLs to Google Charts to display the
 * graphs.
 *
 * TODO: Move this rendering to the GraphPanel object.
 */
function renderGraph(id_JSON) {
	
	if (document.getElementById('gcapi-warn')) {
		//The test above is a generic test if the graph contents are rendered -
		//Can't really modify them if not yet init'ed by EXT.
		
	    var predictions_sections = id_JSON["sparrow-id-response"].results.result[0].predicted.data.section;
	    var adjustments = Sparrow.SESSION.hasEnabledAdjustments();
	
	    for (var i = predictions_sections.length - 1; i >= 1; i--) {
	        var orig_url = '';
	        var orig_vals = '';
	        var orig_col = '';
	        var adj_url = '';
	        var adj_vals = '';
	        var adj_col = '';
	        var orig_adj_col = '';
	        var label_url = '';
	        var orig_label_url = '';
	        var adj_label_url = '';
	        var max_val = 0;
	        var legend_html = '<table id="graph_legend_table"><tr><th colspan="'+((adjustments) ? '6': '4')+'">Sources</th></tr>';
	
	        if(adjustments)
	        	legend_html += "<tr><td>Orig.</td><td>Adj.</td><td></td><td>Orig.</td><td>Adj.&nbsp;</td><td></td></tr>";
	        //find sum of values for the pie chart (need to do percentage of total)
	        var orig_total = 0;
	        var adj_total = 0;
	        var predictions_rows = predictions_sections[i]["r"];
	        for (var j = 0; j < predictions_rows.length - 1; j++) {
	            var predictions_cols = predictions_rows[j]["c"];
	            orig_total += parseFloat(predictions_cols[4]);
	            adj_total += parseFloat(predictions_cols[5]);
	        }
	        
	        var getColor = function(indx, adj) {
	        	if(adj) return Sparrow.config.GraphColorArray[indx % Sparrow.config.GraphColorArray.length][1];
	        	return Sparrow.config.GraphColorArray[indx % Sparrow.config.GraphColorArray.length][0];
	        };
	        
	        var getColorHtml = function(indx, adj) {
	        	var html = "<td style='width: 10px; height: 10px; background-color: #"+getColor(indx, false)+";'>&nbsp;</td>";
	        	if(adj)
	        		html += "<td style='width: 10px; height: 10px; background-color: #"+getColor(indx, true)+";'>&nbsp;</td>";
	        	return html;
	        };
	        
	        //get values for bar charts
	        for (var j = 0; j < predictions_rows.length - 1; j++) {
	            var predictions_cols = predictions_rows[j]["c"];
	            
	            //Get the source name, removing ' Total Load' from the end if present
	            var TL = " total load";
	            var valueLabel = predictions_cols[0];
	            var lcValueLabel = valueLabel.toLowerCase();
	            var tlIndex = lcValueLabel.lastIndexOf(TL);
	            
	            if (tlIndex > -1 && valueLabel.length == tlIndex + TL.length) {
	            	//' total load' is the last portion of the src name
	            	valueLabel = valueLabel.substr(0, valueLabel.length - TL.length);
	            }

	            if(!(j%2)) {
	            	legend_html += '<tr>';
	            }
	            legend_html += getColorHtml(j, adjustments)+"<td>" + valueLabel + "</td>";
	            
	            if(j % 2 || j === predictions_rows.length-2) {
	            	legend_html += '</tr>';
	            }
	            
	            orig_label_url+= (Math.round(predictions_cols[4]/orig_total*1000)/10)+"%|";
	            orig_url += (parseFloat(predictions_cols[4]) / orig_total) + ",";
	            orig_vals += predictions_cols[4] + ",";
	            orig_col += getColor(j, false)+"|";
	            
	            adj_label_url+= (Math.round(predictions_cols[5]/adj_total*1000)/10)+"%|";
	            adj_url += (parseFloat(predictions_cols[5]) / adj_total) + ",";
	            adj_vals += predictions_cols[5] + ",";
	            adj_col += getColor(j, true)+"|";
	
	            //find max val in data for graph scaling
	            if (parseFloat(predictions_cols[4]) > max_val) max_val = parseFloat(predictions_cols[4]);
	            if (parseFloat(predictions_cols[5]) > max_val) max_val = parseFloat(predictions_cols[5]);
	        }
	
	        legend_html += "</table>";
	        orig_label_url = orig_label_url.substring(0,orig_label_url.length-1);
	        adj_label_url = adj_label_url.substring(0,adj_label_url.length-1);
	        orig_url = orig_url.substring(0,orig_url.length-1);
	        adj_url = adj_url.substring(0,adj_url.length-1);
	        orig_vals = orig_vals.substring(0,orig_vals.length-1);
	        adj_vals = adj_vals.substring(0,adj_vals.length-1);
	        orig_col = orig_col.substring(0,orig_col.length-1);
	        adj_col = adj_col.substring(0,adj_col.length-1);
	        orig_adj_col = orig_col +","+adj_col;
	
	        //set src of graph images
	    	var chart_width = 170 + (predictions_rows.length-1)*56;
	    	var chart_height = 50 + (predictions_rows.length-1)*30;
	        if (chart_width > 1000) {
	        	document.getElementById('gcapi-warn').style.display = 'block';
	        	document.getElementById('gcapi-graphs').style.display = 'none';
	        } else {
	        	document.getElementById('gcapi-warn').style.display = 'none';
	        	document.getElementById('gcapi-graphs').style.display = 'block';
	        	var prettyUnits = Sparrow.USGS.prettyPrintUnitsForGoogleQueryParam(Sparrow.SESSION.getModelUnits());
	        	if(Sparrow.SESSION.hasEnabledAdjustments()) {
	        		document.getElementById('src_total_adj').style.display = 'block';
	        		document.getElementById('src_bvg').src = 'http://chart.apis.google.com/chart?chco='+ orig_adj_col +'&chtt=Total ' + Sparrow.SESSION.getModelConstituent() + ' Load by Source (' + prettyUnits + ')&chs='+chart_width+'x'+chart_height+'&cht=bvg&chd=t:' + orig_vals + '|' + adj_vals + '&chl=' + label_url + '&chds=0,' + max_val + '&chxt=y&chxr=0,0,' + max_val;
	            	document.getElementById('src_total_orig').src = 'http://chart.apis.google.com/chart?chco='+ orig_col +'&chtt=Share of Total ' + Sparrow.SESSION.getModelConstituent() + ' Load by Source - Original&chs=420x140&cht=p&chd=t:' + orig_url + '&chl=' + orig_label_url;
	            	document.getElementById('src_total_adj').src = 'http://chart.apis.google.com/chart?chco='+ adj_col +'&chtt=Share of Total ' + Sparrow.SESSION.getModelConstituent() + ' Load by Source - Adjusted&chs=420x140&cht=p&chd=t:' + adj_url + '&chl=' + adj_label_url;
	            	document.getElementById('src_graph_legend').innerHTML = legend_html;
	        	}
	        	else {
	        		chart_width = chart_width / 2;
	        		document.getElementById('src_total_adj').style.display = 'none';
	        		document.getElementById('src_bvg').src = 'http://chart.apis.google.com/chart?chco='+ orig_col +'&chtt=Total Load by Source (' + prettyUnits + ')&chs='+chart_width+'x130&cht=bvg&chd=t:' + orig_vals + '&chl=' + label_url + '&chds=0,' + max_val + '&chxt=y&chxr=0,0,' + max_val;
	            	document.getElementById('src_total_orig').src = 'http://chart.apis.google.com/chart?chco='+ orig_col +'&chtt=Share of Total ' + Sparrow.SESSION.getModelConstituent() + ' Load by Source&chs=420x140&cht=p&chd=t:' + orig_url + '&chl=' + orig_label_url;
	            	document.getElementById('src_graph_legend').innerHTML = legend_html;
	        	}
	        }
	    }

	}
}

var context_id = null;
var last_id_JSON = null;

/**
 * sets the id for the current PredictionContext.
 *
 * returns bool indicates whether or not function passed pre-conditions for retrieving context
 */

function getContextIdAsync(options) {
	
	var formulateLegendText = function(displayUnits, displayConstituent) {
		return displayUnits + ' ' + displayConstituent;
	};

	var getDisplayInfo = function(xmlText, tagName) {
		var display = xmlText.getElementsByTagName(tagName)[0].firstChild ? xmlText.getElementsByTagName(tagName)[0].firstChild.nodeValue : '';
		return display;
	};

    if (Sparrow.SESSION.isChanged() || context_id == null) {
    	Ext.Ajax.request({
    		method: 'POST',
    		url: 'getContextId',
    		params: {
    			xmlreq: Sparrow.SESSION.getPredictionContextAsXML()
    		},
    		success: function(r,o) {
    	        context_id = r.responseXML.childNodes[0].getAttribute("context-id");
    	        
    	        var displayName = getDisplayInfo(r.responseXML, 'name');
    	        var displayDesc = getDisplayInfo(r.responseXML, 'description');
    	        var displayUnits = getDisplayInfo(r.responseXML, 'units');
    	        var displayConstituent = getDisplayInfo(r.responseXML, 'constituent');
    	        var rowCount = getDisplayInfo(r.responseXML, 'rowCount');
    	        
    	        Sparrow.SESSION.setSeriesName(displayName);
    	        Sparrow.SESSION.setSeriesDescription(displayDesc);
    	        Sparrow.SESSION.setSeriesUnits(displayUnits);
    	        Sparrow.SESSION.setSeriesConstituent(displayConstituent);
    	        Sparrow.SESSION.setRowCount(rowCount);
    	        
    	        Sparrow.SESSION.mark();
    	        options.callback.call(options.scope);
    		},
    		failure: function(r,o) {
    			Ext.Msg.alert('Warning', 'failed getting context id')
    		}
    	});
    } else {
        options.callback.call(options.scope);
    }

    return true;
}







/**
 * Add the sparrow datalayer to the map
 */
function make_map() {
	
	var _autoGenBins = function() {
		var bucketCount = Sparrow.SESSION.getBinCount();
		var bucketType = Sparrow.SESSION.getBinType();
		var bucketDisplayType = Sparrow.SESSION.getBinTypeName();

        Ext.Ajax.request({
            url: 'getBins',
            method: 'GET',
            params: 'context-id=' + context_id + '&bin-count=' + bucketCount + '&bin-type=' + bucketType,
            scope: this,
            success: function(response, options) {
	            var binValues = Sparrow.ui.parseBinDataResponse(response.responseXML);
	         
	            Sparrow.SESSION.setBinData(binValues);
		        var comparisonBucketLbl = Ext.getCmp('map-options-tab').bucketLabel;
		        comparisonBucketLbl.setText(bucketCount + ' ' + Sparrow.SESSION.getBinTypeName() + ' Bins');
	            addDataLayer();
	        },
            failure: function(response, options) {
            	//TODO:  Should really detect failure based on the status flag
            	if (response.isTimeout) {
            		Ext.Msg.alert('Warning', 'autogen bins timed out')
            	} else {

            	}
	        },
            timeout: 40000
        });
	};

	if (Sparrow.SESSION.isBinAuto()) {
	    getContextIdAsync({ callback: _autoGenBins });	//Adds the dataLayer if successful
	} else {
		getContextIdAsync({
	    	callback: function() {
	    	    var urlParams = 'context-id=' + context_id || '';

	    		var bins = Sparrow.SESSION.getBinData()['functionalBins'];
	    		//PermanentMapState["binning"]["bins"];
	    		urlParams += '&binLowList=' + Ext.pluck(bins, 'low').join();
	    		urlParams += '&binHighList=' + Ext.pluck(bins, 'high').join();
	    		urlParams = encodeURI(urlParams);
	    		
	    		Ext.Ajax.request({
		            url: 'confirmBins',
		            method: 'GET',
		            params: urlParams,
		            scope: this,
		            success: function(response, options) {
			            var result = response.responseXML.getElementsByTagName('entity')[0].firstChild.nodeValue;
			            var allValuesInABin = response.responseXML.lastChild.getElementsByTagName('entity')[0].firstChild.nodeValue;
			            var allValuesInASingleBin = response.responseXML.lastChild.getElementsByTagName('entity')[1].firstChild.nodeValue;
			            
			            if(allValuesInABin!='true' || allValuesInASingleBin!='true'){
			            	var msg = '<br/>Would you like to automatically adjust the bins to include the entire set of results on the map in \'Equal Count\' bins?';
			            	
			            	if(allValuesInABin=='false') msg = '- There are results not included in your custom bins that will not be displayed on the map.<br/>' + msg;
			            	if(allValuesInASingleBin=='false') msg = '- All results fall into a single bin.<br/>' + msg;
			            	
			            	Ext.Msg.confirm(
			            		'Potential Binning Issues', 
			            		msg, 
			            		function(yes){
			            			if(yes!='yes'){
			            				addDataLayer();
			            			} else {
			            				_autoGenBins();
			            			}
			            		}
			            	);
			            } else {
			            	addDataLayer();
			            }
			        },
		            failure: function(response, options) {
		            	Ext.Msg.alert('Warning', 'Failed to confirm bins');
			        },
		            timeout: 40000
		        });
	    	}
		});
	}
}

/**
 * when/if context is returned, add the sparrow data layer to the map
 */
function addDataLayer() {

	//Internal ID used for the map layer
	var mappedValueLayerID = Sparrow.config.LayerIds.mainDataLayerId;
	
    //get parameters to create base url for sparrow data layer
    var what_to_map = Sparrow.SESSION.PermanentMapState["what_to_map"];
    var theme_name = Sparrow.SESSION.getThemeName();

    // Set up the map tile url
    var urlParams = 'model_id=' + model_id;
    urlParams += (context_id ? '&context_id=' + context_id : '');
    urlParams += '&what_to_map=' + what_to_map + '&theme_name=' + theme_name;

    var bins = Sparrow.SESSION.getBinData()["functionalBins"];
    var colors = Sparrow.SESSION.getBinData()["binColors"];

	urlParams += '&binLowList=' + Ext.pluck(bins, 'low').join();
	urlParams += '&binHighList=' + Ext.pluck(bins, 'high').join();
	urlParams += '&binColorList=' + colors.join();
	urlParams = encodeURI(urlParams);

    // Update the legend
    var legendEl = Ext.get('legend');
    Ext.DomHelper.overwrite(legendEl, '');
    Sparrow.ui.renderLegend();
    
    map1.layerManager.unloadMapLayer(mappedValueLayerID);
    map1.appendLayer(
    	new JMap.web.mapLayer.WMSLayer({
    		zDepth: 60000,
    		id: mappedValueLayerID,
    		scaleMin: 0,
    		scaleMax: 100,
    		baseUrl: 'getMap?' + urlParams,
    		legendUrl: 'getLegend?' + urlParams,
    		title: "sparrow reaches",
    		name: "sparrow_reaches",
    		isHiddenFromUser: true,
    		description: 'Sparrow Reaches',
    		opacity: Sparrow.SESSION.getDataLayerOpacity()
    	})
    );
    
    Sparrow.SESSION.fireContextEvent('map-updated-and-synced');

}






