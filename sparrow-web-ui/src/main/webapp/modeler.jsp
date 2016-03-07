<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<%@ page contentType="text/html; charset=utf-8"%>
<html>
<head>
	<jsp:include page="template_meta_tags.jsp" flush="true" />
	
	<title>Model Selection - SPARROW Model Decision Support</title>
	<link rel="icon" href="favicon.ico" />

	<link type="text/css" rel="stylesheet" href="http://www.usgs.gov/styles/common.css" />
	<link type="text/css" rel="stylesheet" href="http://infotrek.er.usgs.gov/docs/usgs_vis_std/style/usgs_style_main.css" />
	<link type="text/css" rel="stylesheet" href="css/custom.css" />

	<link rel="stylesheet" type="text/css" href="webjars/extjs/3.4.1.1/resources/css/ext-all.css" />
	<jsp:include page="template_ie7_sizer_fix.jsp" flush="true" />
	
	<script type="text/javascript" src="webjars/extjs/3.4.1.1/adapter/ext/ext-base.js"></script>
	<script type="text/javascript" src="webjars/extjs/3.4.1.1/ext-all.js"></script>
	<script type="text/javascript" src="js/ModelSelector.js"></script>
	<script type="text/javascript">
		function updateCoreVersion(){
			Ext.Ajax.request({
				method: 'GET',
				url: 'getCoreInfo',
				success: function(r,o) {
					var item = document.getElementById("versionInfo");
					item.firstChild.nodeValue += "[core: " + r.responseText + "]";
				},
				failure: function(r,o) {
					alert("request failed");
				}
			});
		};
		
		/*
		* Callback function for the model request.  This function renders the model
		* select box adding an option for each model returned.
		*/
		function renderModelList(response, request) {
			var modelResponse = response.responseText;
		    // Pull back the response
		    modelResponse = Ext.util.JSON.decode(modelResponse);

		    var mcol1 = document.getElementById('models-col1');
		    mcol1.innerHTML = '';
		    var mcol2 = document.getElementById('models-col2');
		    mcol2.innerHTML = '';
		    for (var i = 0; i < modelResponse.models.model.length; i++) {
			var model = modelResponse.models.model[i];
			var bbox = model.bounds["@west"] + ',' + model.bounds["@south"] + ',' + model.bounds["@east"] + ',' + model.bounds["@north"];
			var html =
			    '<div class="model-item clearfix">' +
				'<div class="model-screenshot-area">' +
					'<a href="map.jsp?model=' + model['@id'] + '&bbox=' + bbox + '">' +
						'<img src="images/model_screens/ss_model_' + model['@id'] + '.png" alt="model screen shot" class="model-screenshot"/>' +
					'</a>' +
				'</div>' +
					'<div class="model-description-area">' + 
						'<h3 class="model-title"><a href="map.jsp?model=' + model['@id'] + '&bbox=' + bbox + '">' + model['name'] + '</a></h3>' +
						'<div class="model-dateadded">Added: ' + model['dateAdded'] + '</div>' +
						'<div class="model-description">' + model['description'] + '</div>' +
					'</div>';

			if (model.sessions && model.sessions.session) {
				html += '<div class="model-predefined-session-area"><h3>Predefined Scenarios</h3><ul>';
				var sessions =  model.sessions.session;
				for (var j = 0; j < sessions.length; j++){
					var session = sessions[j];
					html += '<li>'
					html += '<h4><a href="map.jsp?model=' + model['@id'] + '&session=' + session['@key'] + '">' + session['@name'] + '</a></h4>';
					html += '<p>' + session['@description'] + '</p>';
					html += '</li>';
				}
				html += '</ul></div>';

			}


			html += '</div>';
			if (i < Math.floor(modelResponse.models.model.length / 2)) {
				mcol1.innerHTML += html;
			} else {
				mcol2.innerHTML += html;
			}
		    }
		}
		
		Ext.onReady(function(){
			var nonpublic = false;
			var nonapproved = false;
			var archived = false;

			if (document.getElementById('model-controls-area')) {
				nonpublic = document.getElementById('model-controls-show-nonpublic').checked;
				nonapproved = document.getElementById('model-controls-show-nonapproved').checked;
				archived = document.getElementById('model-controls-show-archived').checked
			}
			
			loadModels(nonpublic, nonapproved, archived, renderModelList);
		});
		Ext.onReady(updateCoreVersion);
	 </script>
	
	<jsp:include page="template_page_tracking.jsp" flush="true" />
</head>
<body>
    <jsp:include page="header.jsp" flush="true" />
    <div style="padding: 1em">
    	<div class="clearfix section" id="introduction">
    		<p><b>Additional regional SPARROW models will be available in the Decision Support System in August/September 2011.</b></p><br/>
    		
			<p>The Decision Support System assists water research and resource management through access to national,
			 regional, and basin-wide SPARROW models (<a href="http://water.usgs.gov/nawqa/sparrow/">Spatially Referenced Regressions On Watershed attributes</a>) for 
			 the conterminous United States.   The system has sophisticated capabilities for displaying model predictions 
			 of water-quality conditions and sources by stream reach and catchment, tracking transport to downstream 
			 receiving waters, and evaluating management source-reduction scenarios.  Complementary map overlays include 
			 land use, shaded relief, street-level data, and hydrologic unit boundaries. </p><br/>
	
			<p>SPARROW models explain spatial patterns in monitored stream water-quality conditions in relation to 
			human activities and natural processes.  The system uses existing calibrated models to predict long-term 
			average loads, concentrations, yields, and source contributions (and associated error estimates) for all 
			stream reaches within the modeled watersheds.  Models are reported for a variety of water-quality constituents 
			and time periods, and include some common geographical areas.  Differences among models for the same 
			constituent, geographical area, and time period reflect an evolution of the model applications, technology, 
			and geospatial data as described in supporting documentation and scientific publications.</p>
			 
			 <h4>Found a bug or have a comment?</h4>
			 <p>Please send bugs and suggestions to the
			 <a title="Contact Email" href="mailto:sparrowdss@usgs.gov?subject=Sparrow Map Comments">SPARROW DSS Administrator</a>.</p>
		 </div>
		 <!--
		 <div class="clearfix section" id="screencasts-area">
		 	<h3>Introductory Screencasts</h3>
		 	<p>
		 		These are short (3-5 minutes video demonstrations of how to use the decision support tool.  View fullscreen to see the full detail of the video.
		 	</p>
		 	
		 	<div class="screencast">
			 	<object width="240" height="148">
			 		<param name="movie" value="http://www.youtube.com/v/yfs7P3RoMgU&hl=en_US&fs=1&rel=0&hd=1"></param>
			 		<param name="allowFullScreen" value="true"></param>
			 		<param name="allowscriptaccess" value="always"></param>
			 		<embed src="http://www.youtube.com/v/yfs7P3RoMgU&hl=en_US&fs=1&rel=0&hd=1" type="application/x-shockwave-flash" allowscriptaccess="always" allowfullscreen="true" width="240" height="148"></embed>
			 	</object>
	  			<p>Demonstration of some basic features and use.</p>
			</div>

			<div class="screencast">
				<object width="240" height="148">
				 	<param name="movie" value="http://www.youtube.com/v/cM92KHXCfTA&hl=en_US&fs=1&rel=0&hd=1"></param>
				 	<param name="allowFullScreen" value="true"></param>
				 	<param name="allowscriptaccess" value="always"></param>
				 	<embed src="http://www.youtube.com/v/cM92KHXCfTA&hl=en_US&fs=1&rel=0&hd=1" type="application/x-shockwave-flash" allowscriptaccess="always" allowfullscreen="true" width="240" height="148"></embed>
			 	</object>
			 	<p>Demonstration of target (terminal) reaches.</p>
		 	</div>
	 	</div>
		-->
		<% if (request.isUserInRole("sparrow_admin") || request.isUserInRole("sparrow_modeler")) { %>
		 <div class="clearfix section" id="model-controls-area">
		 	<form>
		 		<fieldset>
		 			<legend>Model Listing Options</legend>
		 			<span><label for="show-nonpublic">Show non-public models: </label><input type="checkbox" name="show-nonpublic" id="model-controls-show-nonpublic"></input></span>
		 			<span><label for="show-nonapproved">Show non-approved models: </label><input type="checkbox" name="show-nonapproved" id="model-controls-show-nonapproved"></input></span>
		 			<span><label for="show-archived">Show archived models: </label><input type="checkbox" name="show-archived" id="model-controls-show-archived"></input></span>
		 			<span><input type="button" name="refresh" value="Refresh List" onclick="loadModels()" /></span>
		 			<span class="logout-link"><a href="logout.jsp" title="logout">logout</a></span>
		 		</fieldset>
		 	</form>
	 	</div>
		<% } %>
        <div class="clearfix section" id="models-area" style="display: block">
        	<h3>Select a Model:</h3>
        	<div class="col_50" id="models-col1"></div>
        	<div class="col_50" id="models-col2"></div>
        </div>

        <p class="section">
            See an <a href="modelLinkExamples.jsp">example</a> of how to link
            to a specific model from your own site.
        </p>
    </div>
    <div class="logout-link">
    <% if (request.getUserPrincipal() == null) { %>
	<a href="secure_home.jsp" title="login">~~ login ~~</a>&nbsp;&nbsp;&nbsp;&nbsp;
	<% } %>
    <a href="logout.jsp" title="logout">~~ logout/change user ~~</a>
    </div>
    <jsp:include page="footer.jsp" flush="true" />
</body>
</html>