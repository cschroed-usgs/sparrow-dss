package gov.usgswim.service;

import gov.usgswim.ThreadSafe;
import gov.usgswim.service.pipeline.PipelineRequest;

import javax.servlet.http.HttpServletRequest;
import javax.xml.stream.XMLStreamReader;

/**
 * A basic implementation of HttpRequestParser that attempts to return an
 * XMLStreamReader as the request object.
 */
@ThreadSafe
public class SimpleHttpRequestParser extends AbstractHttpRequestParser<XMLStreamReader> {
	public SimpleHttpRequestParser() {
	}

	public XMLStreamReader parse(HttpServletRequest request) throws Exception {
		return getXMLStream(request);
	}

	public XMLStreamReader parse(XMLStreamReader in) {
		return in;
	}

	public XMLStreamReader parse(String in) throws Exception {
		return getXMLStream(in);
	}

	public PipelineRequest parseForPipeline(HttpServletRequest request) throws Exception {
		// IK: is this class used? Cannot find refernces to it.
		throw new UnsupportedOperationException("may have to refactor interfaces");
	}
}
