<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%@ page contentType="text/html;charset=ISO-8859-1"%>
<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1"/>
    <title>Request Test</title>
    <link rel="icon" href="../favicon.ico" />
  </head>
  <body>
	  	<p><strong>Note these don't work because a prediction context is not defined?</strong></p>
		<form action="../sp_predict/formpost" method="post" enctype="application/x-www-form-urlencoded">
			<fieldset title="Prediction Request 1">
				<label for="xml_input_1">Prediction Request 1</label>
				<p>
				National Model w/ gross and specific adjustments.
				</p>
				<textarea id="xml_input_1" name="xmlreq" cols="120" rows="20">
&lt;?xml version="1.0" encoding="ISO-8859-1" ?&gt;
&lt;sparrow-prediction-request
		xmlns="http://www.usgs.gov/sparrow/prediction-request/v0_1"
		xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"&gt;

	&lt;predict model-id="22"&gt;
		&lt;change-from-nominal type="perc_change"&gt;
			&lt;source-adjustments&gt;
				&lt;!-- Sort order: 2 --&gt;&lt;gross-src src="4" coef="2"/&gt;
				&lt;!-- Sort order: 1 --&gt;&lt;gross-src src="1" coef=".5"/&gt;
				&lt;!-- Sort order: 4 --&gt;&lt;specific src="2" reach="1787602" value="7.77"/&gt;&lt;!-- VALUE WAS 315.819 --&gt;
				&lt;!-- Sort order: 3 --&gt;&lt;specific src="1" reach="1787601" value="9.99"/&gt;&lt;!-- VALUE WAS 5432.3354442 --&gt;
			&lt;/source-adjustments&gt;
		&lt;/change-from-nominal&gt;
	&lt;/predict&gt;
&lt;/sparrow-prediction-request&gt;
				</textarea>

				<br/>
				<input type="submit" name="submit" value="submit"/>
				<input type="checkbox" name="mimetype" value="csv"/>csv
				<input type="checkbox" name="mimetype" value="tab"/>tab
				<input type="checkbox" name="mimetype" value="excel"/>excel
				<input type="checkbox" name="mimetype" value="json"/>json
				<input type="checkbox" name="compress" value="zip"/>zip
			</fieldset>
		</form>

		<form action="../sp_predict/formpost" method="post" enctype="application/x-www-form-urlencoded">
			<fieldset title="Prediction Request 2">
				<label for="xml_input_2">Prediction Request 2</label>
				<p>
				Slightly changed from Request 1 to see the result of a specific reach change.
				</p>
				<textarea id="xml_input_2" name="xmlreq" cols="120" rows="20">
&lt;?xml version="1.0" encoding="ISO-8859-1" ?&gt;
&lt;sparrow-prediction-request
		xmlns="http://www.usgs.gov/sparrow/prediction-request/v0_1"
		xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"&gt;

	&lt;predict model-id="22"&gt;
		&lt;change-from-nominal type="perc_change"&gt;
			&lt;source-adjustments&gt;
				&lt;!-- Sort order: 2 --&gt;&lt;gross-src src="4" coef="2"/&gt;
				&lt;!-- Sort order: 1 --&gt;&lt;gross-src src="1" coef=".5"/&gt;
				&lt;!-- Sort order: 4 --&gt;&lt;specific src="2" reach="1787602" value="7"/&gt;&lt;!-- VALUE WAS 315.819 --&gt;
				&lt;!-- Sort order: 3 --&gt;&lt;specific src="1" reach="1787601" value="9.99"/&gt;&lt;!-- VALUE WAS 5432.3354442 --&gt;
			&lt;/source-adjustments&gt;
		&lt;/change-from-nominal&gt;
	&lt;/predict&gt;
&lt;/sparrow-prediction-request&gt;
				</textarea>

				<br/>
				<input type="submit" name="submit" value="submit"/>
				<input type="checkbox" name="mimetype" value="csv"/>csv
				<input type="checkbox" name="mimetype" value="tab"/>tab
				<input type="checkbox" name="mimetype" value="excel"/>excel
				<input type="checkbox" name="mimetype" value="json"/>json
				<input type="checkbox" name="compress" value="zip"/>zip
			</fieldset>
		</form>

		<form action="../sp_predict/formpost" method="post" enctype="application/x-www-form-urlencoded">
			<fieldset title="Prediction Request 3">
				<label for="xml_input_3">Prediction Request 3</label>
				<p>
				Reordered adjustements from #2.  This should not result in a re-run of the prediction.
				</p>
				<textarea id="xml_input_3" name="xmlreq" cols="120" rows="20">
&lt;?xml version="1.0" encoding="ISO-8859-1" ?&gt;
&lt;sparrow-prediction-request
		xmlns="http://www.usgs.gov/sparrow/prediction-request/v0_1"
		xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"&gt;

	&lt;predict model-id="22"&gt;
		&lt;change-from-nominal type="perc_change"&gt;
			&lt;source-adjustments&gt;
				&lt;!-- Sort order: 2 --&gt;&lt;gross-src src="4" coef="2"/&gt;
				&lt;!-- Sort order: 1 --&gt;&lt;gross-src src="1" coef=".5"/&gt;
				&lt;!-- Sort order: 4 --&gt;&lt;specific src="2" reach="1787602" value="7"/&gt;&lt;!-- VALUE WAS 315.819 --&gt;
				&lt;!-- Sort order: 3 --&gt;&lt;specific src="1" reach="1787601" value="9.99"/&gt;&lt;!-- VALUE WAS 5432.3354442 --&gt;
			&lt;/source-adjustments&gt;
		&lt;/change-from-nominal&gt;
	&lt;/predict&gt;
&lt;/sparrow-prediction-request&gt;
				</textarea>

				<br/>
				<input type="submit" name="submit" value="submit"/>
				<input type="checkbox" name="mimetype" value="csv"/>csv
				<input type="checkbox" name="mimetype" value="tab"/>tab
				<input type="checkbox" name="mimetype" value="excel"/>excel
				<input type="checkbox" name="mimetype" value="json"/>json
				<input type="checkbox" name="compress" value="zip"/>zip
			</fieldset>
		</form>


			<form action="../sp_predict/formpost" method="post" enctype="application/x-www-form-urlencoded">
			<fieldset title="Prediction Request 3">
				<label for="xml_input_3">Prediction Request 3</label>
				<p>
				Reordered adjustements from #2.  This should not result in a re-run of the prediction.
				</p>
				<textarea id="xml_input_3" name="xmlreq" cols="120" rows="20">
&lt;?xml version="1.0" encoding="ISO-8859-1" ?&gt;
&lt;sparrow-prediction-request
		xmlns="http://www.usgs.gov/sparrow/prediction-request/v0_1"
		xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"&gt;

	&lt;predict model-id="22"&gt;
		&lt;change-from-nominal type="perc_change"&gt;
			&lt;source-adjustments&gt;
				&lt;!-- Sort order: 2 --&gt;&lt;gross-src src="4" coef="2"/&gt;
				&lt;!-- Sort order: 1 --&gt;&lt;gross-src src="1" coef=".5"/&gt;
				&lt;!-- Sort order: 4 --&gt;&lt;specific src="2" reach="1787602" value="7"/&gt;&lt;!-- VALUE WAS 315.819 --&gt;
				&lt;!-- Sort order: 3 --&gt;&lt;specific src="1" reach="1787601" value="9.99"/&gt;&lt;!-- VALUE WAS 5432.3354442 --&gt;
			&lt;/source-adjustments&gt;
		&lt;/change-from-nominal&gt;
	&lt;/predict&gt;
&lt;/sparrow-prediction-request&gt;
				</textarea>

				<br/>
				<input type="submit" name="submit" value="submit"/>
			</fieldset>
		</form>
	</body>
</html>