package gov.usgswim.sparrow.domain;

import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;

import gov.usgswim.sparrow.parser.XMLParseValidationException;
import gov.usgswim.sparrow.parser.XMLStreamParserComponent;
import gov.usgswim.sparrow.util.ParserHelper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.commons.lang.builder.HashCodeBuilder;

public class TerminalReaches implements XMLStreamParserComponent {

	private static final long serialVersionUID = 10L;
	private static final String REACHES_CHILD = "reach";
	public static final String MAIN_ELEMENT_NAME = "terminalReaches";


	// =============================
	// PUBLIC STATIC UTILITY METHODS
	// =============================
	public static boolean isTargetMatch(String tagName) {
		return MAIN_ELEMENT_NAME.equals(tagName);
	}

	public static TerminalReaches parseStream(XMLStreamReader in, Long modelID) throws XMLStreamException, XMLParseValidationException {
		TerminalReaches tr = new TerminalReaches(modelID);
		return tr.parse(in);
	}

	// ===============
	// INSTANCE FIELDS
	// ===============
	private Long modelID;
	protected ArrayList<String> reachIDs = new ArrayList<String>();
	private Integer id;

	/**
	 * Constructor requires a modelID
	 */
	public TerminalReaches(Long modelID) {
		this.modelID = modelID;
	}
	
	/**
	 * Creates a fully specified TerminalReach instance.
	 * 
	 * targetReachIDs are copied to ensure immutability.
	 * 
	 * @param modelID
	 * @param targetReachIDs
	 */
	public TerminalReaches(Long modelID, List<String> targetReachIDs) {
		this.modelID = modelID;
		
		reachIDs.addAll(targetReachIDs);
	}

	// ================
	// INSTANCE METHODS
	// ================
	public TerminalReaches parse(XMLStreamReader in) throws XMLStreamException, XMLParseValidationException {
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
						id = ParserHelper.parseAttribAsInt(in, XMLStreamParserComponent.ID_ATTR, false);
                    } else if (ReachElement.isTargetMatch(localName)) {
                        ReachElement r = new ReachElement();
                        r.parse(in);
						reachIDs.add(r.getId());
					} else if ("logicalSet".equals(localName)) {
						ParserHelper.ignoreElement(in);
					} else {
						throw new XMLParseValidationException("unrecognized child element of <" + localName + "> for " + MAIN_ELEMENT_NAME);
					}
					break;
				case END_ELEMENT:
					localName = in.getLocalName();
					if (MAIN_ELEMENT_NAME.equals(localName)) {
						checkValidity();
						return this; // we're done
					} else if (REACHES_CHILD.equals(localName)) {

					} else {// otherwise, error
						throw new XMLParseValidationException("unexpected closing tag of </" + localName + ">; expected  " + MAIN_ELEMENT_NAME);
					}
					break;
			}
		}
		throw new XMLParseValidationException("tag <" + MAIN_ELEMENT_NAME + "> not closed. Unexpected end of stream?");
	}

	public String getParseTarget() {
		return MAIN_ELEMENT_NAME;
	}

	public boolean isParseTarget(String name) {
		return MAIN_ELEMENT_NAME.equals(name);
	}

	/**
	 * Returns the terminal reaches as a set.
	 * @return Set or reach IDs
	 */
	public Set<String> getReachIdsAsSet() {
		// [IK] Why don't we just return the List reachIDs or just make reachIDs
		// a set? The second option doesn't work because we want a deterministic
		// hashcode function as we loop over the reachIDs. The first doesn't work
		// because we want independence of reach id order.
		Set<String> targetReaches = new HashSet<String>();
		for (String reach: reachIDs) {
			targetReaches.add(reach);
		}
		return targetReaches;
	}
	
	/**
	 * Returns true if there are no terminal reaches.
	 * 
	 * @return
	 */
	public boolean isEmpty() {
		return reachIDs.size() == 0;
	}
	/**
	 * Consider two instances the same if they have the same calculated hashcodes
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof TerminalReaches) {
			return obj.hashCode() == hashCode();
		}
		return false;
	}

	@Override
	public synchronized int hashCode() {
		if (id == null) {
			HashCodeBuilder hashBuilder = new HashCodeBuilder(137, 1729);

			hashBuilder.append(modelID);
			for (String idValue: reachIDs) {
				hashBuilder.append(idValue);
			}
			int hash = hashBuilder.toHashCode();

			id = hash;
		}

		return id;
	}

	@Override
	public TerminalReaches clone() throws CloneNotSupportedException {
		TerminalReaches myClone = new TerminalReaches(modelID);
		myClone.reachIDs = new ArrayList<String>(reachIDs.size());
		myClone.reachIDs.addAll(reachIDs);

		return myClone;
	}

	public void checkValidity() throws XMLParseValidationException {
		if (!isValid()) {
			// throw a custom error message depending on the error
			throw new XMLParseValidationException(MAIN_ELEMENT_NAME + " is not valid");
		}
	}

	public boolean isValid() {
		return true;
	}
	
	/**
	 * Returns true if the passed ID is a terminal reach.
	 * 
	 * @param reachId
	 * @return
	 */
	public boolean contains(String reachId) {
		return reachIDs.contains(reachId);
	}

	// =================
	// GETTERS & SETTERS
	// =================
	public List<String> getReachIdsAsList(){
		ArrayList<String> copy = new ArrayList<String>(reachIDs.size());
		copy.addAll(reachIDs);
		return copy;
	}
	
	/**
	 * Returns the number of reach ids
	 * @return
	 */
	public int size() {
		return reachIDs.size();
	}

	public Long getModelID() {
		return modelID;
	}

	public Integer getId() {
		return hashCode();
	}



}
