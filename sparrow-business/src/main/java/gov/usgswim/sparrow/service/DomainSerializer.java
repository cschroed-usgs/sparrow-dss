package gov.usgswim.sparrow.service;

import static gov.usgswim.sparrow.service.AbstractSerializer.XMLSCHEMA_NAMESPACE;
import static gov.usgswim.sparrow.service.AbstractSerializer.XMLSCHEMA_PREFIX;
import gov.usgs.webservices.framework.dataaccess.BasicTagEvent;
import gov.usgs.webservices.framework.dataaccess.BasicXMLStreamReader;
import gov.usgswim.sparrow.domain.IPredefinedSession;
import gov.usgswim.sparrow.domain.SparrowModel;
import gov.usgswim.sparrow.domain.Source;
import gov.usgswim.sparrow.util.SparrowResourceUtils;

import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import javax.xml.stream.XMLStreamException;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
public class DomainSerializer extends BasicXMLStreamReader {
	public static String TARGET_NAMESPACE = "http://www.usgs.gov/sparrow/meta_response/v0_1";
	public static String TARGET_NAMESPACE_LOCATION = "http://www.usgs.gov/sparrow/meta_response.xsd";

	private List<SparrowModel> models;
	private Iterator<SparrowModel> mIter;
	private boolean isOutputCompleteFirstRow;
	private boolean isSessionFirstRowOutput;
	private boolean isSourcesFirstRowOutput;

	public DomainSerializer(List<SparrowModel> models) {
		this.models = models;
	}


	/* Override because there's no resultset
	 * @see gov.usgs.webservices.framework.dataaccess.BasicXMLStreamReader#readNext()
	 */
	@Override
	public void readNext() throws XMLStreamException {
		try {
			if (!isStarted) {
				documentStartAction();
			}
			readModel();
			if (isDataDone && isStarted && !isEnded) {
				// Only output footer if the data is finished, the document was
				// actually started,
				// and the footer has not been output.
				documentEndAction();
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new XMLStreamException(e);
		}
	}

	@Override
	protected BasicTagEvent documentStartAction() {
		super.documentStartAction();
		// add the namespaces
		this.setDefaultNamespace(TARGET_NAMESPACE);
		addNamespace(XMLSCHEMA_NAMESPACE, XMLSCHEMA_PREFIX);
		// opening element
		events.add(new BasicTagEvent(START_DOCUMENT));
		// TODO need to add encoding and version xw.add( evtFact.createStartDocument(ENCODING, XML_VERSION) );

		events.add(new BasicTagEvent(START_ELEMENT, "models")
			.addAttribute(XMLSCHEMA_PREFIX, XMLSCHEMA_NAMESPACE, "schemaLocation", TARGET_NAMESPACE + " " + TARGET_NAMESPACE_LOCATION));

		mIter = models.iterator();
		return null;
	}


	private void readModel() {
		if (mIter.hasNext()) {
			SparrowModel model = mIter.next();
			events.add(new BasicTagEvent(START_ELEMENT, "model").addAttribute("id", model.getId().toString()));
			{
                addOpenTag("status");
                {
                    addNonNullBasicTag("approved", Boolean.toString(model.isApproved()));
                    addNonNullBasicTag("public", Boolean.toString(model.isPublic()));
                    addNonNullBasicTag("archived", Boolean.toString(model.isArchived()));
                }
                addCloseTag("status");
				addNonNullBasicTag("name", model.getName());
				addNonNullBasicTag("alias", SparrowResourceUtils.lookupModelName(model.getId().toString()));
				addNonNullBasicTag("description", StringUtils.trimToNull(model.getDescription()));
				addNonNullBasicTag("url", StringUtils.trimToNull(model.getUrl()));
				addNonNullBasicTag("dateAdded", DateFormatUtils.ISO_DATE_FORMAT.format(model.getDateAdded()));
				addNonNullBasicTag("contactId", model.getContactId().toString());
				addNonNullBasicTag("enhNetworkId", model.getEnhNetworkId().toString());
				addNonNullBasicTag("themeName", model.getThemeName());
				addNonNullBasicTag("constituent", model.getConstituent());
				addNonNullBasicTag("units", model.getUnits().getUserName());
				events.add(new BasicTagEvent("bounds", null)
					.addAttribute("north", model.getNorthBound().toString())
					.addAttribute("west", model.getWestBound().toString())
					.addAttribute("south", model.getSouthBound().toString())
					.addAttribute("east", model.getEastBound().toString()));
				addOpenTag("sessions");
				{
					for ( IPredefinedSession session: model.getSessions()) {
						if (isOutputCompleteFirstRow && !isSessionFirstRowOutput) {
							outputEmptySessionsForHeaders();
							isSessionFirstRowOutput = true;
						}
						events.add(new BasicTagEvent("session", null)
							.addAttribute("key", session.getUniqueCode())
							.addAttribute("name", session.getName())
							.addAttribute("description", session.getDescription())
							.addAttribute("group_name", session.getGroupName())
							.addAttribute("topic", session.getTopic().name())
							.addAttribute("type", session.getPredefinedSessionType().name())
							.addAttribute("approved", session.getApproved()?"T":"F")
							.addAttribute("sort_order", Integer.toString(session.getSortOrder()))
							.addAttribute("add_by", session.getAddBy())
							.addAttribute("add_date", session.getAddDate().toString())
							.addAttribute("add_note", session.getAddNote())
						);
					}
				}
				addCloseTag("sessions");
				addOpenTag("sources");
				{
					for (Source src : model.getSources()) {
						if (isOutputCompleteFirstRow && !isSourcesFirstRowOutput) {
							outputEmptySourceForHeaders();
							isSourcesFirstRowOutput = true;
							isOutputCompleteFirstRow = false; // Not sure if this is proper. Must rethink;
						}
						events.add(new BasicTagEvent(START_ELEMENT, "source")
							.addAttribute("id", src.getId().toString())
							.addAttribute("identifier", Integer.toString(src.getIdentifier()))
							.addAttribute("sortOrder", Integer.toString(src.getSortOrder()))
						);
						{
							addNonNullBasicTag("name", src.getName());
							addNonNullBasicTag("displayName", src.getDisplayName());
							addNonNullBasicTag("description",StringUtils.trimToNull(src.getDescription()));
							addNonNullBasicTag("constituent", src.getConstituent());
							addNonNullBasicTag("units", src.getUnits().toString());
						}
						addCloseTag("source");
						// add a carriage return to break up long text line
						events.add(new BasicTagEvent(SPACE));
					}
				}
				addCloseTag("sources");
			}
			addCloseTag("model");
		} else {
			isDataDone = true;
		}
	}

	@Override
	protected void documentEndAction() {
		super.documentEndAction();
		addCloseTag("models");
		events.add(new BasicTagEvent(END_DOCUMENT));
	}


	private void outputEmptySourceForHeaders() {
		events.add(new BasicTagEvent(START_ELEMENT, "source")
				.addAttribute("id", "")
				.addAttribute("identifier", "")
				.addAttribute("sortOrder", ""));
		{
			addNonNullBasicTag("name", "");
			addNonNullBasicTag("displayName", "");
			addNonNullBasicTag("description", "");
			addNonNullBasicTag("constituent", "");
			addNonNullBasicTag("units", "");
		}
		addCloseTag("source");
		// isOutputCompleteFirstRow = false; [IK] This side effect is bad and should be no longer needed.
	}

	private void outputEmptySessionsForHeaders() {
		events.add(new BasicTagEvent(START_ELEMENT, "session")
				.addAttribute("key", ""));
		addCloseTag("session");
	}

	public void setOutputCompleteFirstRow() {
		isOutputCompleteFirstRow = true;
	}

	@Override
	public void close() throws XMLStreamException {
		// TODO Auto-generated method stub
	}

}
