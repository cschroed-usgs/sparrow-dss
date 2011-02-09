package gov.usgswim.sparrow.service.metadata;

import static org.custommonkey.xmlunit.XMLAssert.assertXpathEvaluatesTo;
import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static gov.usgswim.sparrow.service.ServiceResponseMimeType.*;

import java.io.ByteArrayInputStream;
import java.nio.charset.Charset;

import gov.usgswim.sparrow.SparrowServiceTestWithCannedModel50;
import gov.usgswim.sparrow.action.Action;
import gov.usgswim.sparrow.action.PredefinedSessionsTest;
import gov.usgswim.sparrow.domain.IPredefinedSession;
import gov.usgswim.sparrow.domain.PredefinedSessionBuilder;
import gov.usgswim.sparrow.service.AbstractSparrowServlet;
import gov.usgswim.sparrow.service.ServiceResponseWrapper;

import org.junit.Test;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.PostMethodWebRequest;
import com.meterware.httpunit.PutMethodWebRequest;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;

/**
 * Note:  This test uses the test db which is currently being used to store
 * semi-real data until we get a production transactional db.  As a result,
 * the 2nd test will fail b/c there are other records in the db. 1/31/2011.
 * 
 * @author eeverman
 *
 */
public class SavedSessionServiceTest extends SparrowServiceTestWithCannedModel50 {
	
	private static final String SESSION_SERVICE_URL = "http://localhost:8088/sp_session";
	
	// ============
	// TEST METHODS
	// ============
	@Test
	public void PUTandGETaSession() throws Exception {
		WebRequest req = new PutRequest(SESSION_SERVICE_URL);
		
		//PredefinedSessionBuilder[] sessions = PredefinedSessionsTest.createUnsavedPredefinedSessions();
		//assignRequestParams(req, sessions[0]);
		
		String ps1Str = Action.getText("Session1", this.getClass());
		req.setParameter(AbstractSparrowServlet.XML_SUBMIT_DEFAULT_PARAM_NAME, ps1Str);
		Object entity = getXMLXStream().fromXML(ps1Str);
		IPredefinedSession ps1 = (IPredefinedSession)entity;
		
		WebResponse response = client.sendRequest(req);
		String actualResponse = response.getText();
		//System.out.println("response: '" + actualResponse + "'");
		
		assertXpathEvaluatesTo("OK", "/ServiceResponseWrapper/status", actualResponse);
		assertXpathEvaluatesTo("CREATE", "/ServiceResponseWrapper/operation", actualResponse);
		assertXpathEvaluatesTo("gov.usgswim.sparrow.domain.IPredefinedSession", "/ServiceResponseWrapper/entityClass", actualResponse);
		assertXpathEvaluatesTo("9999", "/ServiceResponseWrapper/entityList/PredefinedSession[1]/modelId", actualResponse);
		assertXpathEvaluatesTo("test_created_delete_me_XX1", "/ServiceResponseWrapper/entityList/PredefinedSession[1]/uniqueCode", actualResponse);
		
		String dbId = getXPathValue("/ServiceResponseWrapper/entityId", actualResponse);

		
		
		//Get via /id url (full response)
		req = new GetMethodWebRequest(SESSION_SERVICE_URL + "/" + dbId);
		response = client.sendRequest(req);
		actualResponse = response.getText();
		System.out.println("Get via /id url (full response) response: " + actualResponse);
		assertXpathEvaluatesTo("OK", "/ServiceResponseWrapper/status", actualResponse);
		assertXpathEvaluatesTo("GET", "/ServiceResponseWrapper/operation", actualResponse);
		assertXpathEvaluatesTo("test_created_delete_me_XX1", "/ServiceResponseWrapper/entityList/PredefinedSession[1]/uniqueCode", actualResponse);
		assertXpathEvaluatesTo(dbId, "/ServiceResponseWrapper/entityId", actualResponse);
		
		//Get via uniqueCode parameter (full response)
		req = new GetMethodWebRequest(SESSION_SERVICE_URL);
		req.setParameter("uniqueCode", "test_created_delete_me_XX1");
		response = client.sendRequest(req);
		actualResponse = response.getText();
		assertXpathEvaluatesTo("OK", "/ServiceResponseWrapper/status", actualResponse);
		assertXpathEvaluatesTo("GET", "/ServiceResponseWrapper/operation", actualResponse);
		assertXpathEvaluatesTo("test_created_delete_me_XX1", "/ServiceResponseWrapper/entityList/PredefinedSession[1]/uniqueCode", actualResponse);
		assertXpathEvaluatesTo(dbId, "/ServiceResponseWrapper/entityId", actualResponse);
		//System.out.println("GET via Param response: " + actualResponse);
		
		//Get via /id url (context only response)
		req = new GetMethodWebRequest(SESSION_SERVICE_URL + "/" + dbId);
		req.setParameter(SavedSessionService.RETURN_CONTENT_ONLY_PARAM_NAME, "TRUE");
		response = client.sendRequest(req);
		actualResponse = response.getText();
		assertEquals("context", actualResponse);
		//System.out.println("GET response: " + actualResponse);
		
		//Get via parameter (context only response)
		req = new GetMethodWebRequest(SESSION_SERVICE_URL);
		req.setParameter(SavedSessionService.RETURN_CONTENT_ONLY_PARAM_NAME, "true");
		req.setParameter("uniqueCode", "test_created_delete_me_XX1");
		response = client.sendRequest(req);
		actualResponse = response.getText();
		assertEquals("context", actualResponse);
		//System.out.println("GET via Param response: " + actualResponse);
		
		
		PredefinedSessionBuilder deleteMe = new PredefinedSessionBuilder();
		deleteMe.setId(Long.parseLong(dbId));
		PredefinedSessionsTest.deleteSessions(deleteMe);
	}

	
	@Test
	public void CreateUsingACustomParameterNameSpecedInTheHeader() throws Exception {
		WebRequest req = new PutRequest(SESSION_SERVICE_URL);
		
		//PredefinedSessionBuilder[] sessions = PredefinedSessionsTest.createUnsavedPredefinedSessions();
		//assignRequestParams(req, sessions[0]);
		
		String ps1Str = Action.getText("Session1", this.getClass());
		
		//set a custom param name
		req.setHeaderField(AbstractSparrowServlet.XML_SUBMIT_HEADER_NAME, "i_made_this_up");
		
		req.setParameter("i_made_this_up", ps1Str);
		Object entity = getXMLXStream().fromXML(ps1Str);
		IPredefinedSession ps1 = (IPredefinedSession)entity;
		
		WebResponse response = client.sendRequest(req);
		String actualResponse = response.getText();
		//System.out.println("response: '" + actualResponse + "'");
		
		assertXpathEvaluatesTo("OK", "/ServiceResponseWrapper/status", actualResponse);

		String dbId = getXPathValue("/ServiceResponseWrapper/entityId", actualResponse);

		//Get via /id url (full response)
		req = new GetMethodWebRequest(SESSION_SERVICE_URL + "/" + dbId);
		response = client.sendRequest(req);
		actualResponse = response.getText();
		//System.out.println("Get via /id url (full response) response: " + actualResponse);
		assertXpathEvaluatesTo("OK", "/ServiceResponseWrapper/status", actualResponse);
		assertXpathEvaluatesTo("GET", "/ServiceResponseWrapper/operation", actualResponse);
		assertXpathEvaluatesTo("test_created_delete_me_XX1", "/ServiceResponseWrapper/entityList/PredefinedSession[1]/uniqueCode", actualResponse);
		assertXpathEvaluatesTo(dbId, "/ServiceResponseWrapper/entityId", actualResponse);
		
		
		PredefinedSessionBuilder deleteMe = new PredefinedSessionBuilder();
		deleteMe.setId(Long.parseLong(dbId));
		PredefinedSessionsTest.deleteSessions(deleteMe);
	}
	
	@Test
	public void CreateByPostingXMLinTheBodyWithoutUsingParams() throws Exception {
		
		String ps1Str = Action.getText("Session1", this.getClass());
		
		assertTrue(Charset.isSupported("UTF-8"));
		ByteArrayInputStream inputStream = new ByteArrayInputStream(ps1Str.getBytes(Charset.forName("UTF-8")));
		WebRequest req = new PutMethodWebRequest(SESSION_SERVICE_URL, inputStream, XML.toString() +"; UTF-8");
		
		Object entity = getXMLXStream().fromXML(ps1Str);
		IPredefinedSession ps1 = (IPredefinedSession)entity;
		
		WebResponse response = client.sendRequest(req);
		String actualResponse = response.getText();
		//System.out.println("response: '" + actualResponse + "'");
		
		assertXpathEvaluatesTo("OK", "/ServiceResponseWrapper/status", actualResponse);

		String dbId = getXPathValue("/ServiceResponseWrapper/entityId", actualResponse);

		//Get via /id url (full response)
		req = new GetMethodWebRequest(SESSION_SERVICE_URL + "/" + dbId);
		response = client.sendRequest(req);
		actualResponse = response.getText();
		//System.out.println("Get via /id url (full response) response: " + actualResponse);
		assertXpathEvaluatesTo("OK", "/ServiceResponseWrapper/status", actualResponse);
		assertXpathEvaluatesTo("GET", "/ServiceResponseWrapper/operation", actualResponse);
		assertXpathEvaluatesTo("test_created_delete_me_XX1", "/ServiceResponseWrapper/entityList/PredefinedSession[1]/uniqueCode", actualResponse);
		assertXpathEvaluatesTo(dbId, "/ServiceResponseWrapper/entityId", actualResponse);
		
		
		PredefinedSessionBuilder deleteMe = new PredefinedSessionBuilder();
		deleteMe.setId(Long.parseLong(dbId));
		PredefinedSessionsTest.deleteSessions(deleteMe);
	}
	
	@Test
	public void FilterSessions() throws Exception {
		WebRequest req = new GetMethodWebRequest(SESSION_SERVICE_URL);
		
		////////////////
		/// setup a whole bunch of sessions into groups, approved/not approved, etc.
		////////////////
		
		PredefinedSessionBuilder[] sessions = PredefinedSessionsTest.createUnsavedPredefinedSessions();
		IPredefinedSession[] savedSessions = PredefinedSessionsTest.saveSessions(sessions);
		
		//This set of tests is a modified copy of the filter test in PredefinedSessionsTest.
		PredefinedSessionBuilder set2ps1 = new PredefinedSessionBuilder(savedSessions[0]);
		PredefinedSessionBuilder set2ps2 = new PredefinedSessionBuilder(savedSessions[1]);
		PredefinedSessionBuilder set2ps3 = new PredefinedSessionBuilder(savedSessions[2]);
		PredefinedSessionBuilder set3ps1 = new PredefinedSessionBuilder(savedSessions[0]);
		PredefinedSessionBuilder set3ps2 = new PredefinedSessionBuilder(savedSessions[1]);
		PredefinedSessionBuilder set3ps3 = new PredefinedSessionBuilder(savedSessions[2]);
		
		PredefinedSessionBuilder[] newSessions = PredefinedSessionsTest.stripUniqueness(
			set2ps1, set2ps2, set2ps3, set3ps1, set3ps2, set3ps3);
		
		//Assign some group names
		newSessions[0].setGroupName("set2");
		newSessions[1].setGroupName("set2");
		newSessions[2].setGroupName("set2");
		newSessions[3].setGroupName("set3");
		newSessions[4].setGroupName("set3");
		newSessions[5].setGroupName("set3");
		
		//Our set2ps1 style references are now old
		newSessions = PredefinedSessionsTest.toBuilder(PredefinedSessionsTest.saveSessions(newSessions));
		
		//Set a few approved
		newSessions[0].setApproved(true);
		newSessions[1].setApproved(false);
		newSessions[2].setApproved(false);
		newSessions[3].setApproved(false);
		newSessions[4].setApproved(true);
		newSessions[5].setApproved(true);
		
		newSessions = PredefinedSessionsTest.toBuilder(PredefinedSessionsTest.saveSessions(newSessions));
		
		
		////////////////
		/// end of setup
		////////////////
		req.setParameter("modelId", "9999");
		WebResponse response = client.sendRequest(req);
		String actualResponse = response.getText();
		System.out.println("Filter 1 actual response: " + actualResponse);
		//Should be nine all together (no criteria)
		assertXpathEvaluatesTo("9", "count(/ServiceResponseWrapper/entityList/PredefinedSession)", actualResponse);

		
		//Should be 3 approved
		req = new GetMethodWebRequest(SESSION_SERVICE_URL);
		req.setParameter("modelId", "9999");
		req.setParameter("approved", "true");
		response = client.sendRequest(req);
		actualResponse = response.getText();
		assertXpathEvaluatesTo("3", "count(/ServiceResponseWrapper/entityList/PredefinedSession)", actualResponse);

		//Should be 1 approved & FEATURED
		req = new GetMethodWebRequest(SESSION_SERVICE_URL);
		req.setParameter("modelId", "9999");
		req.setParameter("approved", "true");
		req.setParameter("type", "FEATURED");
		response = client.sendRequest(req);
		actualResponse = response.getText();
		assertXpathEvaluatesTo("1", "count(/ServiceResponseWrapper/entityList/PredefinedSession)", actualResponse);
		
		//Should be 2 approved & in group 'set3'
		req = new GetMethodWebRequest(SESSION_SERVICE_URL);
		req.setParameter("modelId", "9999");
		req.setParameter("approved", "true");
		req.setParameter("groupName", "set3");
		response = client.sendRequest(req);
		actualResponse = response.getText();
		assertXpathEvaluatesTo("2", "count(/ServiceResponseWrapper/entityList/PredefinedSession)", actualResponse);
		
		//Should be 2 NOT approved & in group 'set2'
		req = new GetMethodWebRequest(SESSION_SERVICE_URL);
		req.setParameter("modelId", "9999");
		req.setParameter("approved", "false");
		req.setParameter("groupName", "set2");
		response = client.sendRequest(req);
		actualResponse = response.getText();
		assertXpathEvaluatesTo("2", "count(/ServiceResponseWrapper/entityList/PredefinedSession)", actualResponse);

		//Should be 1 approved, in group 'set3', and UNLISTED
		req = new GetMethodWebRequest(SESSION_SERVICE_URL);
		req.setParameter("modelId", "9999");
		req.setParameter("approved", "true");
		req.setParameter("type", "UNLISTED");
		req.setParameter("groupName", "set3");
		response = client.sendRequest(req);
		actualResponse = response.getText();
		assertXpathEvaluatesTo("1", "count(/ServiceResponseWrapper/entityList/PredefinedSession)", actualResponse);

		PredefinedSessionsTest.deleteSessions(newSessions);
		
		
		PredefinedSessionsTest.deleteSessions(savedSessions);
	}
	
	
	public static void assignRequestParams(WebRequest req, IPredefinedSession ps) {
		req.setParameter("code", ps.getUniqueCode());
		req.setParameter("modelId", ps.getModelId().toString());
		req.setParameter("type", ps.getPredefinedSessionType().name());
		
		if (ps.getApproved() != null) {
			req.setParameter("approved", ps.getApproved()?"T":"F");
		}
		req.setParameter("name", ps.getName());
		req.setParameter("description", ps.getDescription());
		req.setParameter("sort_order", ps.getSortOrder().toString());
		req.setParameter("context_string", ps.getContextString());
		req.setParameter("add_by", ps.getAddBy());
		req.setParameter("add_note", ps.getAddNote());
		req.setParameter("add_contact_info", ps.getAddContactInfo());
		req.setParameter("group_name", ps.getGroupName());
	}
	
	
	private class PutRequest extends PostMethodWebRequest {
		public PutRequest(String url) {
			super(url);
		}
		
		@Override
		public String getMethod() {
			return "PUT";
		}
	}
	
	protected static XStream getXMLXStream() {
		XStream xs = new XStream(new StaxDriver());
        xs.setMode(XStream.NO_REFERENCES);
        xs.processAnnotations(ServiceResponseWrapper.class);
        return xs;
	}
}

