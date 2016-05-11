<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ page contentType="text/html; charset=utf-8"%>
<html>
<head>
	<jsp:include page="template_meta_tags.jsp" flush="true" />
	
	<title>Model Selection - SPARROW Model Decision Support</title>
	<link rel="icon" href="favicon.ico" >

	<link type="text/css" rel="stylesheet" href="http://www.usgs.gov/styles/common.css" />
	<link type="text/css" rel="stylesheet" href="css/usgs_style_main.css" />
	<link type="text/css" rel="stylesheet" href="css/custom.css" />
	<link rel="stylesheet" type="text/css" href="webjars/extjs/3.4.1.1/resources/css/ext-all.css" />
	<jsp:include page="template_ie7_sizer_fix.jsp" flush="true" />
	
	<script type="text/javascript" src="webjars/extjs/3.4.1.1/adapter/ext/ext-base.js"></script>
	<script type="text/javascript" src="webjars/extjs/3.4.1.1/ext-all.js"></script>
	<script type="text/javascript" src="js/ModelSelector.js"></script>

	<script type="text/javascript">
	(function(){
		/*
		 * Callback function for the model request.  This function renders the model
		 * select box adding an option for each model returned.
		 */
		function renderModelList(response, request) {
			var modelResponse = response.responseText;
		    // Pull back the response
		    var modelResponse = Ext.util.JSON.decode(modelResponse);

		    var mcol1 = document.getElementById('models-col1');
		    mcol1.innerHTML = '';
		    var mcol2 = document.getElementById('models-col2');
		    mcol2.innerHTML = '';
		    for (var i = 0; i < modelResponse.models.model.length; i++) {
			var model = modelResponse.models.model[i];
			var bbox = model.bounds["@west"] + ',' + model.bounds["@south"] + ',' + model.bounds["@east"] + ',' + model.bounds["@north"];
			var html =
			    '<div class="model-item clearfix" style="display:block">' +
					'<img src="images/model_screens/ss_model_' + model['@id'] + '.png" alt="model screen shot" class="model-screenshot"/>' +
					'<div style="padding-left: 180px">' + 
					'<div class="model-title"><a href="map.jsp?model=' + model['@id'] + '&bbox=' + bbox + '">' + model['name'] + '</a></div>' +
					'<div class="model-dateadded">Added: ' + model['dateAdded'] + '</div>' +
					'<div class="model-description">' + model['description'] + '</div>';

			if (model.sessions && model.sessions.session) {
				html += '<br/><div><u>Predefined Sessions:</u></div><ul>';
				var sessions =  model.sessions.session;
				for (var j = 0; j < sessions.length; j++){
					var session = sessions[j];
					html += '<li><a href="map.jsp?model=' + model['@id'] + '&session=' + session['@key'] + '">' + session['@key'] + '</a></li>';
				}
				html += '</ul>';

			}


			html += '</div></div>';
			if (i < Math.floor(modelResponse.models.model.length / 2)) {
				mcol1.innerHTML += html;
			} else {
				mcol2.innerHTML += html;
			}
		    }
		};
		Ext.onReady(function(){
			loadModels(true, true, false, renderModelList);
		});
	}());
	</script>
	<jsp:include page="template_page_tracking.jsp" flush="true" />
	
</head>
<body>
    <jsp:include page="header.jsp" flush="true" />
    <div style="padding: 1em">
    	<div class="clearfix section" id="introduction">
			<p>The U.S. Geological Survey (USGS) National Water Quality Assessment Program
			(NAWQA) is developing water-quality predictive models for Major River Basins (MRBs)
			covering most of the conterminous United States. These MRB models are built using
			the Spatially Referenced Regressions On Watershed Attributes (SPARROW) methodology.
			A calibrated SPARROW model can be used to both extrapolate measured water-quality
			conditions to unmonitored areas and produce statistical predictions (with error
			estimates) of load, concentration, or yield of any modeled constituent for each
			stream reach under different land-use and management scenarios.</p><br/>
	
			<p>The SPARROW Decision Support System (DSS) uses existing calibrated SPARROW models
			 and offers sophisticated predictive, scenario testing, and regulatory impact assessment
			 capabilities for water-quality research and resource management through a standard
			 web-browser interface. SPARROW DSS capabilities include model views, model scenario
			 predictions, targeted reach analysis and data export.</p>
			 
			 <h4>Found a bug or have a comment?</h4>
			 <p>Please send bugs and suggestions to
			 <a title="Contact Email" href="mailto:lmurphy@usgs.gov?subject=Sparrow Map Comments">Lorraine Murphy</a></p>
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
    <jsp:include page="footer.jsp" flush="true" />
</body>
</html>