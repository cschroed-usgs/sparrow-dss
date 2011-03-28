package gov.usgswim.sparrow.service;

import static org.custommonkey.xmlunit.XMLAssert.assertXpathEvaluatesTo;
import static org.junit.Assert.assertTrue;
import gov.usgswim.sparrow.SparrowServiceTestBaseClass;

import org.junit.Test;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.PostMethodWebRequest;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;

/**
 * 
 * @author eeverman
 *
 */
public class HUCServiceLongRunTest extends SparrowServiceTestBaseClass {
	
	private static final String SERVICE_URL = "http://localhost:8088/huc";

	// ============
	// TEST METHODS
	// ============
	@Test
	public void get01viaExtraPath() throws Exception {
		WebRequest req = new GetMethodWebRequest(SERVICE_URL + "/" + "01");
		

		WebResponse response = client.sendRequest(req);
		String actualResponse = response.getText();
		System.out.println("response: " + actualResponse);
		
		assertXpathEvaluatesTo("OK", "/ServiceResponseWrapper/status", actualResponse);
		assertXpathEvaluatesTo("GET", "/ServiceResponseWrapper/operation", actualResponse);
		assertXpathEvaluatesTo("gov.usgswim.sparrow.domain.HUC", "/ServiceResponseWrapper/entityClass", actualResponse);
		assertXpathEvaluatesTo("01", "/ServiceResponseWrapper/entityList/entity[1]/hucCode", actualResponse);
		
		
		//Quick check that the cache is working
		long startTime = System.currentTimeMillis();
		client.sendRequest(req);
		actualResponse = response.getText();
		long endTime = System.currentTimeMillis();
		
		assertTrue((endTime - startTime) < 200L);
	}
	
	@Test
	public void get06020002viaParameter() throws Exception {
		WebRequest req = new PostMethodWebRequest(SERVICE_URL);
		req.setParameter("huc", "06020002");

		WebResponse response = client.sendRequest(req);
		String actualResponse = response.getText();
		//System.out.println("response: " + actualResponse);
		
		assertXpathEvaluatesTo("OK", "/ServiceResponseWrapper/status", actualResponse);
		assertXpathEvaluatesTo("GET", "/ServiceResponseWrapper/operation", actualResponse);
		assertXpathEvaluatesTo("gov.usgswim.sparrow.domain.HUC", "/ServiceResponseWrapper/entityClass", actualResponse);
		assertXpathEvaluatesTo("06020002", "/ServiceResponseWrapper/entityList/entity[1]/hucCode", actualResponse);
		
	}

	
}

