package gov.usgswim.sparrow.service.deliveryaggreport;

import gov.usgswim.sparrow.service.deliveryterminalreport.ReportRequest;
import static org.custommonkey.xmlunit.XMLAssert.assertXpathEvaluatesTo;
import gov.usgswim.sparrow.SparrowServiceTestBaseWithDB;
import gov.usgswim.sparrow.SparrowTestBase;

import org.junit.Test;
import static org.junit.Assert.*;

import com.meterware.httpunit.*;
import org.junit.Ignore;

public class ReportServiceLongRunTest extends SparrowServiceTestBaseWithDB {

	private static final String CONTEXT_SERVICE_URL = "http://localhost:8088/sp_predictcontext";
	private static final String REPORT_SERVICE_URL = "http://localhost:8088/sp_delivery_aggreport";

	@Override
	public void doOneTimeCustomSetup() throws Exception {
		//Uncomment to debug
		//setLogLevel(Level.DEBUG);
	}
	
	@Test
	public void model50NoAdjustXMLStateReport() throws Exception {
		String contextRequestText = SparrowTestBase.getXmlAsString(this.getClass(), "context1");
		
		WebRequest contextWebRequest = new PostMethodWebRequest(CONTEXT_SERVICE_URL);
		contextWebRequest.setParameter("xmlreq", contextRequestText);
		WebResponse contextWebResponse = client.sendRequest(contextWebRequest);
		String actualContextResponse = contextWebResponse.getText();
		
		assertXpathEvaluatesTo("OK", "//*[local-name()='status']", actualContextResponse);
		int id = getContextIdFromContext(actualContextResponse);
		
		WebRequest reportWebRequest = new GetMethodWebRequest(REPORT_SERVICE_URL);
		reportWebRequest.setParameter(ReportRequest.ELEMENT_CONTEXT_ID, Integer.toString(id));
		reportWebRequest.setParameter(ReportRequest.ELEMENT_MIME_TYPE, "xhtml_table");
		reportWebRequest.setParameter(ReportRequest.ELEMENT_INCLUDE_ZERO_TOTAL_ROWS, "false");
		reportWebRequest.setParameter(ReportRequest.ELEMENT_REGION_TYPE, ReportRequest.RegionType.state.name());

		WebResponse reportWebResponse = client. sendRequest(reportWebRequest);
		String actualReportResponse = reportWebResponse.getText();
		
		//System.out.println(actualReportResponse);
		
		String rowCountStr = ReportServiceLongRunTest.getXPathValue("count(//tbody/tr)", actualReportResponse);
		String nonZeroRowCountStr = ReportServiceLongRunTest.getXPathValue("count(//tr[td[position() = 8 and .!=0]])", actualReportResponse);
		
		assertEquals("9", rowCountStr);
		assertEquals("9", nonZeroRowCountStr);
		
	}
	
	@Test
	public void model50NoAdjustCVSExportCheckContextStateReport() throws Exception {
		String contextRequestText = SparrowTestBase.getXmlAsString(this.getClass(), "context1");
		
		WebRequest contextWebRequest = new PostMethodWebRequest(CONTEXT_SERVICE_URL);
		contextWebRequest.setParameter("xmlreq", contextRequestText);
		WebResponse contextWebResponse = client.sendRequest(contextWebRequest);
		String actualContextResponse = contextWebResponse.getText();
		
		assertXpathEvaluatesTo("OK", "//*[local-name()='status']", actualContextResponse);
		int id = getContextIdFromContext(actualContextResponse);
		
		WebRequest reportWebRequest = new GetMethodWebRequest(REPORT_SERVICE_URL);
		reportWebRequest.setParameter(ReportRequest.ELEMENT_CONTEXT_ID, Integer.toString(id));
		reportWebRequest.setParameter(ReportRequest.ELEMENT_MIME_TYPE, "xhtml_table");
		reportWebRequest.setParameter(ReportRequest.ELEMENT_INCLUDE_ZERO_TOTAL_ROWS, "true");
		reportWebRequest.setParameter(ReportRequest.ELEMENT_REGION_TYPE, ReportRequest.RegionType.state.name());

		WebResponse reportWebResponse = client. sendRequest(reportWebRequest);
		String actualReportResponse = reportWebResponse.getText();
		
		//System.out.println(actualReportResponse);
		
		String rowCountStr = ReportServiceLongRunTest.getXPathValue("count(//tbody/tr)", actualReportResponse);
		String nonZeroRowCountStr = ReportServiceLongRunTest.getXPathValue("count(//tr[td[position() = 8 and .!=0]])", actualReportResponse);
		
		assertEquals("11", rowCountStr);
		assertEquals("9", nonZeroRowCountStr);
		
		
		String firstStateWithZeroLoad = ReportServiceLongRunTest.getXPathValue("//tr[td[position() = 8 and .=0]][1]/td[1]", actualReportResponse);
		String secondStateWithZeroLoad = ReportServiceLongRunTest.getXPathValue("//tr[td[position() = 8 and .=0]][2]/td[1]", actualReportResponse);
		
		assertEquals("FLORIDA", firstStateWithZeroLoad);
		assertEquals("LOUISIANA", secondStateWithZeroLoad);
		
		
	}
	

	
}
