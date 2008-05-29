package gov.usgswim.sparrow.deprecated;

import gov.usgswim.Immutable;
import gov.usgswim.service.pipeline.PipelineRequest;
import gov.usgswim.sparrow.parser.ResponseFormat;

import java.awt.Point;

import org.apache.commons.lang.builder.HashCodeBuilder;


/**
 * A request for reaches within a model nearest a point, sorted
 * by distance.  Although no bounds is set on the numberOfResults, its primary
 * usage is intended to be for identifying a reach near a point, so values over
 * 10 in areas w/ few reaches may return fewer results.
 */
@Immutable
public class IDByPointRequest_old implements PipelineRequest{
	private final Point.Double _point;
	private final int _numberOfResults;
	private final Long _modelId;
	
	private Integer hash;	//Not strictly threadsafe, but recalculation is cheap and non-destructive
	protected String mimetype = "xml"; // default is xml
	private String xmlRequest;
	private ResponseFormat responseFormat;
	
	/**
	 * Constructs a new request instance.
	 * 
	 * Values less then 1 for numberOfResults are converted to 1.
	 * @param modelId
	 * @param point
	 * @param numberOfResults
	 */
	public IDByPointRequest_old(Long modelId, Point.Double point, int numberOfResults) {
		_modelId = modelId;
		_point = point;
		_numberOfResults = (numberOfResults < 1)?1:numberOfResults;
	}

	public IDByPointRequest_old(Integer modelId, Point.Double point, int numberOfResults) {
		_modelId = modelId.longValue();
		_point = point;
		_numberOfResults = (numberOfResults < 1)?1:numberOfResults;
	}
	
	public int getNumberOfResults() {
		return _numberOfResults;
	}

	public Long getModelId() {
		return _modelId;
	}
	
	public Point.Double getPoint() {
		return _point;
	}
	
	public boolean equals(Object object) {
		if (object instanceof IDByPointRequest_old) {
			return this.hashCode() == object.hashCode();
		} else {
			return false;
		}
	}

	public int hashCode() {
		//starts w/ some random numbers just to create unique results
		if (hash == null) {
			hash = new HashCodeBuilder(46517, 40116971).
				append(_modelId).
				append(_point).
				append(_numberOfResults).
				toHashCode();
		}
		
		return hash;
	}

	public String getXMLRequest() {
		return xmlRequest;
	}

	public void setXMLRequest(String request) {
		xmlRequest = request;		
	}

	public void setResponseFormat(ResponseFormat respFormat) {
		this.responseFormat = respFormat;
		responseFormat.fileName = "idByPoint";
	}
	
	public ResponseFormat getResponseFormat() {
		if (responseFormat == null) {
			setResponseFormat(new ResponseFormat());
		}
		return responseFormat;
	}
}
