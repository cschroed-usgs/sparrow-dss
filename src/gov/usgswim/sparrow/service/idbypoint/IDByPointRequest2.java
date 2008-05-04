package gov.usgswim.sparrow.service.idbypoint;

import static javax.xml.XMLConstants.DEFAULT_NS_PREFIX;
import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import gov.usgswim.service.XMLStreamParserComponent;
import gov.usgswim.service.pipeline.PipelineRequest;
import gov.usgswim.sparrow.parser.ParserHelper;
import gov.usgswim.sparrow.parser.PredictionContext;
import gov.usgswim.sparrow.parser.ResponseFormat;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;
import java.io.Serializable;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

public class IDByPointRequest2 implements XMLStreamParserComponent, PipelineRequest {

	private static final String ID_BY_POINT_FILENAME = "idByPoint";
	public static final String MAIN_ELEMENT_NAME = "sparrow-id-request";
	private static final String CONTENT_CHILD = "content";
	private static final String POINT_CHILD = "point";
	private static final String REACH_CHILD = "reach";
	


	// =============================
	// PUBLIC STATIC UTILITY METHODS
	// =============================
	public static boolean isTargetMatch(String tagName) {
		return MAIN_ELEMENT_NAME.equals(tagName);
	}

	private PredictionContext predictionContext;
	// TODO [IK] Implement once Eric has defined these
//	private Adjustments adjustments;
//	private Attributes attributes;
//	private Predicted predicted;
	private boolean isSelectAll;
	private ResponseFormat respFormat;
	Point.Double point;
	private Integer reachID;
	// PipelineRequest fields
	private String xmlRequest;
	private int numberOfResults;

	// ============
	// CONSTRUCTORS
	// ============
	public IDByPointRequest2() {
		
	}
	public IDByPointRequest2(Integer modelID, Double point, int numResults) {
		this.predictionContext = new PredictionContext();
		predictionContext.setModelID(modelID);
		this.point = point;
		this.numberOfResults = numResults;

		this.respFormat = makeDefaultResponseFormat();
	}


	// ================
	// INSTANCE METHODS
	// ================
	public IDByPointRequest2 parse(XMLStreamReader in) throws XMLStreamException {
		String localName = in.getLocalName();
		int eventCode = in.getEventType();
		assert (isTargetMatch(localName) && eventCode == START_ELEMENT) : this
			.getClass().getSimpleName()
			+ " can only parse " + MAIN_ELEMENT_NAME + " elements.";
		boolean isStarted = false;

		while (in.hasNext()) {
			if (isStarted) {
				// Don't advance past the first element.
				eventCode = in.next();
			} else {
				isStarted = true;
			}

			// Main event loop -- parse until corresponding target end tag encountered.
			switch (eventCode) {
				case START_ELEMENT:
					localName = in.getLocalName();
					if (isTargetMatch(localName)) {
						
					}// the following are all children matches 
					else if (PredictionContext.isTargetMatch(localName)) {
						this.predictionContext = PredictionContext.parseStream(in);
					} else if (POINT_CHILD.equals(localName)) {

						point = new Point.Double();	//required
						point.x = java.lang.Double.parseDouble(in.getAttributeValue(DEFAULT_NS_PREFIX, "long"));
						point.y = java.lang.Double.parseDouble(in.getAttributeValue(DEFAULT_NS_PREFIX, "lat"));

					} else if (REACH_CHILD.equals(localName)) {
						String reachIDString = ParserHelper.parseSimpleElementValue(in);
						reachID = Integer.parseInt(reachIDString);
					} else if (CONTENT_CHILD.equals(localName)) {
						ParserHelper.ignoreElement(in);
						// TODO [eric] define _id-point-request-1.xml in more detail so this can be filled in.
					} else if (ResponseFormat.isTargetMatch(localName)) {
						this.respFormat = ResponseFormat.parseStream(in);
					} else {
						throw new RuntimeException("unrecognized child element of <" + localName + "> for " + MAIN_ELEMENT_NAME);
					}
					break;
				case END_ELEMENT:
					localName = in.getLocalName();
					if (MAIN_ELEMENT_NAME.equals(localName)) {
						// TODO [IK] Might want to calculate PC id here.
						// TODO [eric] If the ID is unavailable because this is
						// a new PContext, when in the object lifecycle should
						// id be calculated and populated? Here? on cache.put()?
						respFormat = (respFormat == null)? makeDefaultResponseFormat(): respFormat;
						if  (respFormat.fileName == null) {
							respFormat.fileName = ID_BY_POINT_FILENAME;
						}
						return this; // we're done
					} else if (CONTENT_CHILD.equals(localName) || POINT_CHILD.equals(localName) || REACH_CHILD.equals(localName)) {
						// Do nothing. Continue
					} else {
						// otherwise, error
						throw new RuntimeException("unexpected closing tag of </" + localName + ">; expected  " + MAIN_ELEMENT_NAME);
					}
					//break;
			}
		}
		throw new RuntimeException("tag <" + MAIN_ELEMENT_NAME + "> not closed. Unexpected end of stream?");
	}

	private ResponseFormat makeDefaultResponseFormat() {
		ResponseFormat result = new ResponseFormat();
		result.fileName = ID_BY_POINT_FILENAME;
		result.setMimeType("xml");
		return result;
	}
//	public int hashCode() {
//		int hash = new HashCodeBuilder(13, 16661).
//		append(modelID).
//		append(adjustmentGroupsID).
//		append(analysisID).
//		append(terminalReachesID).
//		append(areaOfInterestID).
//		toHashCode();
//		return hash;
//	}

	/**
	 * A simple clone method, caveat emptor as it doesn't deal with transient
	 * children.
	 * 
	 * @see java.lang.Object#clone()
	 */
//	public IDByPointRequest2 clone() throws CloneNotSupportedException {
//		IDByPointRequest2 myClone = new IDByPointRequest2();
//		myClone.modelID = modelID;
//		myClone.adjustmentGroupsID = adjustmentGroupsID;
//		myClone.analysisID = analysisID;
//		myClone.terminalReachesID = terminalReachesID;
//		myClone.areaOfInterestID = areaOfInterestID;
//
//		myClone.adjustmentGroups = (adjustmentGroups == null)? null: adjustmentGroups.clone();
//		myClone.analysis = (analysis == null)? null: analysis.clone();
//		myClone.terminalReaches = (terminalReaches == null)? null: terminalReaches.clone();
//		myClone.areaOfInterest = (areaOfInterest == null)? null: areaOfInterest.clone();
//
//		return myClone;
//	}
	
	/**
	 * Clones with supplied transient children. Does not clone supplied children.
	 * 
	 * @param ag
	 * @param anal
	 * @param tr
	 * @return
	 * @throws CloneNotSupportedException
	 */
//	public IDByPointRequest2 clone(AdjustmentGroups ag, Analysis anal, TerminalReaches tr, AreaOfInterest aoi) throws CloneNotSupportedException {
//		IDByPointRequest2 myClone = this.clone();
//		// TODO [IK] log error conditions appropriately. Return null if error?
//		// TODO [eric] Determine error behavior. Suggest return null if error.
//		
//		// populate the transient children only if necessary & correct
//		if (adjustmentGroupsID != null && ag != null && ag.getId().equals(adjustmentGroupsID)) {
//			myClone.adjustmentGroups = ag;
//		}
//		
//		if (analysisID != null && anal != null && anal.getId().equals(analysisID)) {
//			myClone.analysis = anal;
//		}
//		
//		if (terminalReachesID != null && tr != null && tr.getId().equals(terminalReachesID)) {
//			myClone.terminalReaches = tr;
//		}
//		
//		if (areaOfInterestID != null && aoi != null && aoi.getId().equals(areaOfInterestID)) {
//			myClone.areaOfInterest = aoi;
//		}	
//
//		return myClone;
//	}

	// =================
	// GETTERS & SETTERS
	// =================
	public String getParseTarget() {
		return MAIN_ELEMENT_NAME;
	}

	public boolean isSelectAll() {
		return isSelectAll;
	}

	public java.lang.Double getLatitude() {
		return point.y;
	}

	public java.lang.Double getLongitude() {
		return point.x;
	}

	public PredictionContext getPredictionContext() {
		return predictionContext;
	}

	public Integer getReachID() {
		return reachID;
	}

	public ResponseFormat getResponseFormat() {
		return respFormat;
	}

	public String getXMLRequest() {
		return xmlRequest;
	}
	
	public void setResponseFormat(ResponseFormat respFormat) {
		this.respFormat = respFormat;		
	}
	
	public void setXMLRequest(String request) {
		this.xmlRequest = request;		
	}
	public Point.Double getPoint() {
		return point;
	}
	public void setNumberOfResults(int numberOfResults) {
		this.numberOfResults = numberOfResults;
	}
	public Integer getModelID() {
		return predictionContext.getModelID();
	}
	public void setModelID(Integer modelID) {
		this.predictionContext.setModelID(modelID);
	}
	public int getNumberOfResults() {
		return numberOfResults;
	}
}
