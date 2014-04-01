package gov.usgswim.sparrow.service;

import static gov.usgs.cida.sparrow.service.util.ServiceResponseMimeType.UNKNOWN;
import static gov.usgs.cida.sparrow.service.util.ServiceResponseMimeType.XML;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Map;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.thoughtworks.xstream.XStream;
import gov.usgswim.sparrow.monitor.ServletInvocation;
import gov.usgs.cida.sparrow.service.util.ServiceResponseMimeType;
import gov.usgs.cida.sparrow.service.util.ServiceResponseWrapper;
import javax.servlet.ServletException;

/**
 * A based class for servlets which includes utility methods to serialize objects
 * into a wrapper using XStream, and to find those objects back again in a POST
 * or PUT.
 * 
 * @author eeverman
 *
 */
public abstract class AbstractSparrowServlet extends HttpServlet {
	protected static Logger log =
		Logger.getLogger(AbstractSparrowServlet.class); //logging for this class

	
	/**
	 * Default constructor.
	 */
	public AbstractSparrowServlet() {
		super();
	}


	//
	// A set of remappings for all requests.  The requests on the standard
	// doGet / doPost, etc, and then are remapped to doActualGet / Post, etc.
	// The base methods are final to ensure that we can setup and cleanup before
	// and after requests.

	@Override
	protected final void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
		ServletInvocation monitor = new ServletInvocation(this.getClass());
		monitor.start();
		
		try {
			doActualGet(req, resp);
		} catch (ServletException e) {
			monitor.setError(e);
			throw e;
		} catch (IOException e) {
			monitor.setError(e);
			throw e;
		} catch (RuntimeException e) {
			monitor.setError(e);
			throw e;
		} finally {
			monitor.finish();
		}
		
	}
	
	protected void doActualGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		super.doGet(req, resp);
	}
	
	@Override
	protected final void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
		ServletInvocation monitor = new ServletInvocation(this.getClass());
		monitor.start();
		
		try {
			doActualPost(req, resp);
		} catch (ServletException e) {
			monitor.setError(e);
			throw e;
		} catch (IOException e) {
			monitor.setError(e);
			throw e;
		} catch (RuntimeException e) {
			monitor.setError(e);
			throw e;
		} finally {
			monitor.finish();
		}
	}
	
	protected void doActualPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		super.doPost(req, resp);
	}

	@Override
	protected final void doHead(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
		ServletInvocation monitor = new ServletInvocation(this.getClass());
		monitor.start();
		
		try {
			doActualHead(req, resp);
		} catch (ServletException e) {
			monitor.setError(e);
			throw e;
		} catch (IOException e) {
			monitor.setError(e);
			throw e;
		} catch (RuntimeException e) {
			monitor.setError(e);
			throw e;
		} finally {
			monitor.finish();
		}
	}
	
	protected  void doActualHead(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		super.doHead(req, resp);
	}

	@Override
	protected final void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
		ServletInvocation monitor = new ServletInvocation(this.getClass());
		monitor.start();
		
		try {
			doActualOptions(req, resp);
		} catch (ServletException e) {
			monitor.setError(e);
			throw e;
		} catch (IOException e) {
			monitor.setError(e);
			throw e;
		} catch (RuntimeException e) {
			monitor.setError(e);
			throw e;
		} finally {
			monitor.finish();
		}
	}
	
	protected final void doActualOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		super.doOptions(req, resp);
	}

	@Override
	protected final void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
		ServletInvocation monitor = new ServletInvocation(this.getClass());
		monitor.start();
		
		try {
			doActualPut(req, resp);
		} catch (ServletException e) {
			monitor.setError(e);
			throw e;
		} catch (IOException e) {
			monitor.setError(e);
			throw e;
		} catch (RuntimeException e) {
			monitor.setError(e);
			throw e;
		} finally {
			monitor.finish();
		}
	}
	
	protected void doActualPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		super.doPut(req, resp);
	}

	@Override
	protected final void doTrace(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
		ServletInvocation monitor = new ServletInvocation(this.getClass());
		monitor.start();
		
		try {
			doActualTrace(req, resp);
		} catch (ServletException e) {
			monitor.setError(e);
			throw e;
		} catch (IOException e) {
			monitor.setError(e);
			throw e;
		} catch (RuntimeException e) {
			monitor.setError(e);
			throw e;
		} finally {
			monitor.finish();
		}
	}
	
	protected void doActualTrace(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		super.doTrace(req, resp);
	}
	
	@Override
	protected final void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
		ServletInvocation monitor = new ServletInvocation(this.getClass());
		monitor.start();
		
		try {
			doActualDelete(req, resp);
		} catch (ServletException e) {
			monitor.setError(e);
			throw e;
		} catch (IOException e) {
			monitor.setError(e);
			throw e;
		} catch (RuntimeException e) {
			monitor.setError(e);
			throw e;
		} finally {
			monitor.finish();
		}
	}
	
	protected void doActualDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		super.doDelete(req, resp);
	}
	
	
	/**
	 * Returns a trimmed to null value from the parameter map for the passed name.
	 * 
	 * @param params
	 * @param name
	 */
	protected static String getClean(Map params, String name) {
		Object v = params.get(name);
		if (v != null) {
			String[] vs = (String[]) v;
			if (vs.length > 0) {
				return StringUtils.trimToNull(vs[0]);
			}
		}
		return null;
	}

	/**
	 * Returns a Long value from the parameter map for the passed name.
	 * If unparsable, missing, or null, null is returned.
	 * 
	 * @param params
	 * @param name
	 */
	protected static Long getLong(Map params, String name) {
		String s = getClean(params, name);
		if (s != null) {
			try {
				return Long.parseLong(s);
			} catch (NumberFormatException e) {
				//allow null to be returned
			}
		}
		return null;
	}
	
	/**
	 * Returns a Double value from the parameter map for the passed name.
	 * If unparsable, missing, or null, null is returned.
	 * 
	 * @param params
	 * @param name
	 */
	protected static Double getDouble(Map params, String name) {
		String s = getClean(params, name);
		if (s != null) {
			try {
				return Double.parseDouble(s);
			} catch (NumberFormatException e) {
				//allow null to be returned
			}
		}
		return null;
	}

	/**
	 * Returns an Integer value from the parameter map for the passed name.
	 * If unparsable, empty, or missing, null is returned.
	 * 
	 * @param params
	 * @param name
	 */
	protected static Integer getInteger(Map params, String name) {
		String s = getClean(params, name);
		if (s != null) {
			try {
				return Integer.parseInt(s);
			} catch (NumberFormatException e) {
				//allow null to be returned
			}
		}
		return null;
	}

	/**
	 * Returns a Boolean value from the parameter map for the passed name.
	 * If empty or missing, null is returned.  Otherwise, values are considered
	 * true if they are 'T' or 'TRUE', case insensitive.
	 * 
	 * @param params
	 * @param name
	 */
	protected static Boolean getBoolean(Map params, String key) {
		String s = getClean(params, key);
		if (s != null) {
			return ("T".equalsIgnoreCase(s) || "true".equalsIgnoreCase(s));
		}
		return null;
	}
	
	/**
	 * Determines if a string represents true.
	 * Values are considered true if they are 'T' or 'TRUE', case insensitive.
	 * @param value
	 * @return
	 */
	protected boolean parseBoolean(String value) {
		value = StringUtils.trimToNull(value);
		return ("true".equalsIgnoreCase(value) || "t".equalsIgnoreCase(value));
	}

	/**
	 * Serializes the passed wrapper using the mimetype and encoding in
	 * the wrapper.
	 * 
	 * @param resp
	 * @param wrap
	 * @throws IOException
	 */
	protected void sendResponse(HttpServletResponse resp, ServiceResponseWrapper wrap) throws IOException {
		
		XStream xs = null;
		resp.setCharacterEncoding(wrap.getEncoding());
		resp.setContentType(wrap.getMimeType().toString());
	
		switch (wrap.getMimeType()) {
		case XML: 
			xs = SharedApplication.getInstance().getXmlXStream();
			break;
		case JSON:
			xs = SharedApplication.getInstance().getJsonXStreamWriter();
			break;
		default:
			throw new RuntimeException("Unknown MIMEType.");
		}
		
		xs.toXML(wrap, resp.getWriter());
	}
	
	/**
	 * Writes the passed content as the specified type.  Assumed UTF-8.
	 * 
	 * @param resp
	 * @param content
	 * @param mimeType
	 * @throws IOException
	 */
	protected void sendResponse(HttpServletResponse resp, String content, ServiceResponseMimeType mimeType) throws IOException {
		
		XStream xs = null;
		resp.setCharacterEncoding("UTF-8");
		resp.setContentType(mimeType.toString());
	
		resp.getWriter().print(content);
		resp.getWriter().flush();
	}

	/**
	 * Determines the desired mime type of the client from the request.
	 * This method is used in a GET request, since likely only http parameters
	 * were posted, but XML or JSON needs to be returned.
	 * 
	 * @param req
	 * @return
	 */
	protected ServiceResponseMimeType parseMime(HttpServletRequest req) {
		String mimeStr = StringUtils.trimToNull(
				req.getParameter(ServletResponseParser.REQUESTED_MIME_TYPE_PARAM_NAME));
		
		ServiceResponseMimeType type = ServiceResponseMimeType.parse(mimeStr);
		
		if (type != UNKNOWN) {
			return type;
		} else {
			Enumeration heads = req.getHeaders("Accept");
			
			while (heads.hasMoreElements()) {
				String a = heads.nextElement().toString();
				type = ServiceResponseMimeType.parse(a);
				if (type != UNKNOWN) return type;
			}
			
		}
		
		//Couldn't find a type - use the default
		return XML;
	}

	/**
	 * Trims the extraPath to remove the leading slash and completely trims it
	 * to null.
	 * 
	 * @param req
	 * @return
	 */
	protected String cleanExtraPath(HttpServletRequest req) {
		String extraPath = StringUtils.trimToNull(req.getPathInfo());
		
		
		if (extraPath != null) {
			if (extraPath.startsWith("/")) {
				extraPath = StringUtils.trimToNull(extraPath.substring(1));
			}
		}
		
		return extraPath;
	}

}