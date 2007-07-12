package gov.usgswim.sparrow.service;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 * Implementations are capable of parsing an incoming XML document into a
 * generically defined request bean.
 */
public interface RequestParser<T> {
	public T parse(XMLStreamReader in) throws XMLStreamException;
}
