package gov.usgswim.sparrow.parser;

import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;

import javax.xml.XMLConstants;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import gov.usgswim.service.ParserHelper;
import gov.usgswim.service.XMLStreamParserComponent;

public class Select implements XMLStreamParserComponent {
	public static final String MAIN_ELEMENT_NAME = "select";

	// =============================
	// PUBLIC STATIC UTILITY METHODS
	// =============================
	public static boolean isTargetMatch(String tagName) {
		return MAIN_ELEMENT_NAME.equals(tagName);
	}

	// ===============
	// INSTANCE FIELDS
	// ===============
	private DataSeriesType dataSeries;
	private String source;
	private String dataSeriesPer;
	private String aggFunctionPer;
	private String aggFunction;
	private String partition;
	private String analyticFunction;
	private String type;
	private String nominalComparison;

	// ================
	// INSTANCE METHODS
	// ================
	public Select parse(XMLStreamReader in) throws XMLStreamException {
		String localName = in.getLocalName();
		int eventCode = in.getEventType();
		assert (isTargetMatch(localName) && eventCode == START_ELEMENT) : 
			this.getClass().getSimpleName()
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
					if (MAIN_ELEMENT_NAME.equals(localName)) {
					} else if ("data-series".equals(localName)) {
						source = in.getAttributeValue(XMLConstants.DEFAULT_NS_PREFIX, "source");
						dataSeriesPer = in.getAttributeValue(XMLConstants.DEFAULT_NS_PREFIX, "per");
						String dataSeriesString = ParserHelper.parseSimpleElementValue(in);
						dataSeries = (dataSeriesString != null)? 
								Enum.valueOf(DataSeriesType.class, dataSeriesString): null;
					} else if ("agg-function".equals(localName)) {
						aggFunctionPer = in.getAttributeValue(XMLConstants.DEFAULT_NS_PREFIX, "per");
						aggFunction = ParserHelper.parseSimpleElementValue(in);
					} else if ("analytic-function".equals(localName)) {
						partition = in.getAttributeValue(XMLConstants.DEFAULT_NS_PREFIX, "partition");
						analyticFunction = ParserHelper.parseSimpleElementValue(in);
					} else if ("nominal-comparison".equals(localName)) {
						type = in.getAttributeValue(XMLConstants.DEFAULT_NS_PREFIX, "type");
						nominalComparison = ParserHelper.parseSimpleElementValue(in);
					} else {
						throw new RuntimeException("unrecognized child element of <" + localName + "> for " + MAIN_ELEMENT_NAME);
					}
					break;
				case END_ELEMENT:
					localName = in.getLocalName();
					if (MAIN_ELEMENT_NAME.equals(localName)) {
						return this; // we're done
					}
					// otherwise, error
					throw new RuntimeException("unexpected closing tag of </" + localName + ">; expected  " + MAIN_ELEMENT_NAME);
					//break;
			}
		}
		throw new RuntimeException("tag <" + MAIN_ELEMENT_NAME + "> not closed. Unexpected end of stream?");
	}

	public String getParseTarget() {
		return MAIN_ELEMENT_NAME;
	}
	
	// =================
	// GETTERS & SETTERS
	// =================
	public String getAggFunction() {
		return aggFunction;
	}

	public String getAggFunctionPer() {
		return aggFunctionPer;
	}

	public String getAnalyticFunction() {
		return analyticFunction;
	}

	public DataSeriesType getDataSeries() {
		return dataSeries;
	}

	public String getDataSeriesPer() {
		return dataSeriesPer;
	}

	public String getNominalComparison() {
		return nominalComparison;
	}

	public String getPartition() {
		return partition;
	}

	public String getSource() {
		return source;
	}

	public String getType() {
		return type;
	}
}
