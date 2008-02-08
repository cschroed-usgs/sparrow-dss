package gov.usgswim.sparrow.service;

import gov.usgs.webservices.framework.dataaccess.BasicTagEvent;
import gov.usgs.webservices.framework.dataaccess.BasicXMLStreamReader;
import gov.usgswim.sparrow.Data2D;
import gov.usgswim.sparrow.PredictData;

import java.io.OutputStream;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.xml.XMLConstants;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLStreamException;
import static gov.usgswim.sparrow.service.AbstractSerializer.*;

public class PredictSerializer2 extends BasicXMLStreamReader{

	public static String TARGET_NAMESPACE = "http://www.usgs.gov/sparrow/prediction-response/v0_1";
	public static String TARGET_NAMESPACE_LOCATION = "http://www.usgs.gov/sparrow/prediction-response/v0_1.xsd";
	public static String T_PREFIX = "mod";
	private PredictServiceRequest request;
	private Data2D result;
	private PredictData predictData;
	//
	protected ParseState state = new ParseState();

	// ===========
	// INNER CLASS
	// ===========
	protected class ParseState{
		protected int r = 0;
		boolean writeSrcs = false;
		Data2D src = null;
		
		public boolean isDataFinished() {
			return r >= result.getRowCount();
		}
	};

	// ============
	// CONSTRUCTORS
	// ============
	public PredictSerializer2(PredictServiceRequest request, Data2D result, PredictData predictData) {
		super();
		this.request = request;
		this.result = result;
		this.predictData = predictData;
	}

	// ================
	// INSTANCE METHODS (for pull parsing)
	// ================
	/* Override because there's no resultset
	 * @see gov.usgs.webservices.framework.dataaccess.BasicXMLStreamReader#readNext()
	 */
	@Override
	public void readNext() throws XMLStreamException {
		try {
			if (!isStarted) {
				documentStartAction();
			}
			readRow();
			if (state.isDataFinished()) {
				if (isStarted && !isEnded) {
					// Only output footer if the document was actually started
					// and the footer has not been output.
					documentEndAction();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new XMLStreamException(e);
		}
	}
	
	@Override protected BasicTagEvent documentStartAction() {
		super.documentStartAction();
		// add the namespaces
		this.setDefaultNamespace(TARGET_NAMESPACE);
		addNamespace(XMLSCHEMA_NAMESPACE, XMLSCHEMA_PREFIX);
		
		// opening element
		events.add(new BasicTagEvent(START_DOCUMENT));
		events.add(new BasicTagEvent(START_ELEMENT, "sparrow-prediction-response")
			.addAttribute(XMLSCHEMA_PREFIX, XMLSCHEMA_NAMESPACE, "schemaLocation", TARGET_NAMESPACE + " " + TARGET_NAMESPACE_LOCATION));

		{
			addOpenTag("response");
			{
				events.add(new BasicTagEvent(START_ELEMENT, "metadata")
						.addAttribute("rowCount", Integer.toString(result.getRowCount()))
						.addAttribute("columnCount", Integer.toString(result.getColCount())));
				{
					addOpenTag("columns");
					{
						if (predictData != null && request.getDataSeries().equals(PredictServiceRequest.DataSeries.ALL)) {

							//Add a group for the source columns
							events.add(new BasicTagEvent(START_ELEMENT, "group")
								.addAttribute("name", "Source Values"));
							{
								for(String head : predictData.getSrc().getHeadings()) {
									events.add(makeNonNullBasicTag("col", "")
											.addAttribute("name", head)
											.addAttribute("type", "Number"));
								}
							}
							addCloseTag("group");

							events.add(new BasicTagEvent(START_ELEMENT, "group")
								.addAttribute("name", "Predicted Values"));
							{
								for(String head : result.getHeadings()) {
									events.add(makeNonNullBasicTag("col", "")
											.addAttribute("name", head)
											.addAttribute("type", "Number"));
								}
							}
							addCloseTag("group");

						} else {
							// We don't have the original predict data, so the best we can do is just the predict columns
							// IK: shouldn't this be in a group? ADDED TODO verify with Eric
							events.add(new BasicTagEvent(START_ELEMENT, "group").addAttribute("name", "Predicted Values"));
							{
								for(String head : result.getHeadings()) {
									events.add(makeNonNullBasicTag("col", "")
											.addAttribute("name", head)
											.addAttribute("type", "Number"));
								}
							}
							addCloseTag("group");
						}
					}
					addCloseTag("columns");
				}
				addCloseTag("metadata");
				
				// If true, write the source value columns into the data.  It should preceed the predict val columns
				state.writeSrcs = predictData != null && request.getDataSeries().equals(PredictServiceRequest.DataSeries.ALL);
				state.src = (state.writeSrcs)? predictData.getSrc():null;
				
				addOpenTag("data");
		
			}
		}
		return null;
	}
	
	@Override
	protected void documentEndAction() {
		super.documentEndAction();
		addCloseTag("data");
		addCloseTag("response");
		addCloseTag("sparrow-prediction-response");
		events.add(new BasicTagEvent(END_DOCUMENT));
	};
	
	protected void readRow(){
		if (!state.isDataFinished()) {
			// read the row
			events.add(new BasicTagEvent(START_ELEMENT, "r")
				.addAttribute("id", Integer.toString(result.getIdForRow(state.r))));
			{
				//write source value columns (if requested)
				if (state.writeSrcs) {
					for (int c = 0; c < state.src.getColCount(); c++)  {
						addNonNullBasicTag("c", Double.toString(state.src.getDouble(state.r, c)));
					}
				}

				//write predicted data columns
				for (int c = 0; c < result.getColCount(); c++)  {
					addNonNullBasicTag("c", Double.toString(result.getDouble(state.r, c)));
				}
			}
			addCloseTag("r");
			events.add(new BasicTagEvent(SPACE));
			
		} 
		state.r++;
	};

	@Override
	public void close() throws XMLStreamException {
		// not much needs to be done. no resources to release
		result = null;
		request = null;
		predictData = null;
	}
	
	// ==========================
	// SIMPLE GETTERS AND SETTERS
	// ==========================
	public String getTargetNamespace() {
		return TARGET_NAMESPACE;
	}
}

