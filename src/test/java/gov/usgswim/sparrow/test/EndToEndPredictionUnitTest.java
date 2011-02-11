package gov.usgswim.sparrow.test;

import static org.junit.Assert.assertEquals;
import gov.usgswim.datatable.DataTable;
import gov.usgswim.sparrow.SparrowServiceUnitTest;
import gov.usgswim.sparrow.action.Action;
import gov.usgswim.sparrow.parser.DataSeriesType;
import gov.usgswim.sparrow.util.TabDelimFileUtil;

import java.io.BufferedReader;
import java.io.StringReader;

import org.junit.Test;

/**
 * A prediction service request to export request test, using canned file data
 * for model 50.  Serves as an overall fast check of system health.
 * 
 * @author eeverman
 *
 */
public class EndToEndPredictionUnitTest extends SparrowServiceUnitTest {

	@Test
	public void checkPredictionWithNoAdjustments() throws Exception {
		String contextReq = getSharedTestResource("predict-context-no-adj.xml");
		String contextResp = sendPostRequest(PREDICT_CONTEXT_SERVICE_URL, contextReq);
		
		Integer contextId = getContextIdFromContext(contextResp);
		
		String exportUrl = PREDICT_EXPORT_SERVICE_URL + "?" +
				"context-id=" + contextId.toString() + "&" +
				"mime-type=tab";
		
		String exportResponse = this.sendGetRequest(exportUrl);
		
		StringReader reader = new StringReader(exportResponse);
		BufferedReader br = new BufferedReader(reader);
		
		DataTable resultTable = TabDelimFileUtil.readAsDouble(br , true, -1);
		DataTable expectResults = this.getTestModelPredictResult();
		//System.out.println(exportResponse.substring(0, 1000));
		
		String totColName = Action.getDataSeriesProperty(DataSeriesType.total, false);
		int expectCol = expectResults.getColumnByName(totColName);
		int actualCol = 1;	//Its the mapped value column, which is the 2nd.
		
		for (int r = 0; r < expectResults.getRowCount(); r++) {
			Double expect = expectResults.getDouble(r, expectCol);
			Double actual = resultTable.getDouble(r, actualCol);
			assertEquals(expect, actual, .0001d);
		}
	}
	

}
