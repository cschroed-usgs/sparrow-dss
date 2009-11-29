package gov.usgswim.sparrow.service.metadata;

import static gov.usgswim.sparrow.util.SparrowResourceUtils.lookupModelID;
import static gov.usgswim.sparrow.util.SparrowResourceUtils.retrieveSavedSession;
import static gov.usgswim.sparrow.util.SparrowResourceUtils.retrieveAllSavedSessions;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Set;
import java.util.Map.Entry;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author ilinkuo
 *
 */
public class SavedSessionService extends HttpServlet {

	public static StringBuilder retrieveAllSavedSessionsXML(String modelID) {
		StringBuilder result = new StringBuilder();
		Set<Entry<Object, Object>> sessionList = retrieveAllSavedSessions(modelID);
		if (sessionList != null && !sessionList.isEmpty()) {
			result.append("<sessions>");
			for (Entry<Object, Object> entry: sessionList) {
				result.append("<session key=\"" + entry.getKey().toString() + "\"/>\n");
			}
			result.append("</sessions>");
		}
		return result;
	}

	private static final long serialVersionUID = 1L;

	// ================
	// Instance Methods
	// ================
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		doPost(req, resp);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		// The current exposed Sparrow RESTlike interface isn't very RESTful, but
		// in the future, these would be components of the URL rather than query
		// parameters
		String model = req.getParameter("model");
		Long modelID = lookupModelID(model);
		String session = req.getParameter("session");

		PrintWriter out = resp.getWriter();

		if (modelID == null) {
			resp.setContentType("text/plain");
			out.write("invalid required parameter value for model: " + model);
			out.flush();
			return;
		}
		if (session == null || session.length() == 0) {
			// output a list of all sessions for the model
			// TODO may need to convert this to JSON

			StringBuilder sessions = retrieveAllSavedSessionsXML(modelID.toString());
			if (sessions.length() > 0) {
				resp.setContentType("text/xml");
				out.write(sessions.toString());
			} else {
				resp.setContentType("text/plain");
				// TODO what to return in case of empty?
				out.write("No sessions found");
			}
			return;
		}
		// output the named session
		resp.setContentType("text/html");
		String result = retrieveSavedSession(modelID.toString(), session);
		// TODO decide on what to return if no session found
		// Note that this result is already in JSON
		out.write(result);

		out.flush();
	}

}
