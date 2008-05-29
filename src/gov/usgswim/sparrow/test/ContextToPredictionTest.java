package gov.usgswim.sparrow.test;

import gov.usgswim.sparrow.LifecycleListener;
import gov.usgswim.sparrow.PredictData;
import gov.usgswim.sparrow.PredictResult;
import gov.usgswim.sparrow.parser.PredictionContext;
import gov.usgswim.sparrow.service.SharedApplication;
import gov.usgswim.sparrow.service.predictcontext.PredictContextPipeline;
import gov.usgswim.sparrow.service.predictcontext.PredictContextRequest;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;

import junit.framework.TestCase;

public class ContextToPredictionTest extends TestCase {

	LifecycleListener lifecycle = new LifecycleListener();
	
	protected void setUp() throws Exception {
		super.setUp();
		lifecycle.contextInitialized(null);
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		
		lifecycle.contextDestroyed(null);
	}
	
	public void testBasicPredictionValues() throws Exception {

		PredictContextRequest contextReq = buildRequest();
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		PredictContextPipeline pipe = new PredictContextPipeline();
		pipe.dispatch(contextReq, out);
		

		System.out.println("***");
		System.out.println("Response: " + out.toString());
		System.out.println("PredictContextID: " + contextReq.getPredictionContext().hashCode());
		System.out.println("***");
		
		assertTrue(out.toString().contains(new Integer(contextReq.getPredictionContext().hashCode()).toString() ));
		assertTrue(out.toString().contains("1612937363"));
		
		//Now reproduce the steps taken by running a prediction...
		PredictionContext contextFromCache = SharedApplication.getInstance().getPredictionContext(1612937363);
		PredictResult predictResult = SharedApplication.getInstance().getPredictResult(contextFromCache);
		PredictData predictData = SharedApplication.getInstance().getPredictData(contextFromCache.getModelID());
		
		assertEquals(new Long(1L), contextFromCache.getModelID());
		assertNotNull(predictData);
		assertNotNull(predictResult);
		
		
		//Test some of the adjusted values
		
	}
	
	public void testHashCode() throws Exception {
		PredictionContext context1 = buildRequest().getPredictionContext();
		PredictionContext context2 = buildRequest().getPredictionContext();
		
		assertEquals(context1.hashCode(), context2.hashCode());
		assertEquals(context1.getId(), context2.getId());
		assertEquals(context1.getId().intValue(), context2.hashCode());
	}
	
	
	public PredictContextRequest buildRequest() throws Exception {
		InputStream is = getClass().getResourceAsStream("/gov/usgswim/sparrow/test/sample/predict-context-1.xml");
		String xml = readToString(is);
		
		PredictContextPipeline pipe = new PredictContextPipeline();
		return pipe.parse(xml);
	}
	
	public String readToString(InputStream is) {
		InputStreamReader isr = new InputStreamReader(is);
		BufferedReader br = new BufferedReader(isr);

		StringBuffer sb = new StringBuffer();
		try {
			String line = null;
			while ((line = br.readLine()) != null) {
				sb.append(line + "\n");
			}
		} catch (Exception ex) {
			ex.getMessage();
		} finally {
			try {
				is.close();
			} catch (Exception ex) {
			}
		}
		return sb.toString();
	}


}

