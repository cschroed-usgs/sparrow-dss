package gov.usgswim.sparrow.test;

import gov.usgswim.sparrow.Adjustment;
import gov.usgswim.sparrow.Data2DCompare;
import gov.usgswim.sparrow.Double2D;
import gov.usgswim.sparrow.PredictionRequest;
import gov.usgswim.sparrow.service.PredictService;
import gov.usgswim.sparrow.service.PredictServiceRequest;
import gov.usgswim.sparrow.util.TabDelimFileUtil;

import java.awt.Point;

import java.io.IOException;
import java.io.InputStream;

import java.sql.SQLException;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;

import org.codehaus.stax2.XMLInputFactory2;


public class PredictServiceTest extends TestCase {
	//private Connection conn;

	public PredictServiceTest(String sTestName) {
		super(sTestName);
	}

	/**
	 */
	public void testReadData1() throws SQLException, XMLStreamException,
																					IOException {
		
		XMLInputFactory xinFact = XMLInputFactory2.newInstance();
		XMLStreamReader xsr = xinFact.createXMLStreamReader(
			this.getClass().getResourceAsStream("/gov/usgswim/sparrow/test/sample/predict-request-1.xml"));
		
		PredictService service = new PredictService();
		PredictServiceRequest req = service.parse(xsr);
		
		this.assertEquals(PredictServiceRequest.ResponseType.ALL_RESULTS, req.getResponseType());
		this.assertEquals(PredictServiceRequest.PredictType.VALUES, req.getPredictType());
		this.assertEquals(PredictServiceRequest.DataSeries.TOTAL, req.getDataSeries());
		
		PredictionRequest pReq = req.getPredictionRequest();
		this.assertEquals(22L, pReq.getModelId());
		this.assertEquals(2, pReq.getAdjustmentSet().getAdjustmentCount());
		
		
		Adjustment adj = pReq.getAdjustmentSet().getAdjustments()[0];
		this.assertEquals(1, adj.getId());
		this.assertEquals(.5d, adj.getValue());
		
		adj = pReq.getAdjustmentSet().getAdjustments()[1];
		this.assertEquals(4, adj.getId());
		this.assertEquals(2d, adj.getValue());
	}
	
	public void testReadData2() throws SQLException, XMLStreamException,
																					IOException {
		Point.Double point = new Point.Double();
		point.x = -100;
		point.y = 40;
		
		XMLInputFactory xinFact = XMLInputFactory2.newInstance();
		XMLStreamReader xsr = xinFact.createXMLStreamReader(
			this.getClass().getResourceAsStream("/gov/usgswim/sparrow/test/sample/predict-request-2.xml"));
		
		PredictService service = new PredictService();
		PredictServiceRequest req = service.parse(xsr);
		
		this.assertEquals(PredictServiceRequest.ResponseType.IDENTIFY_BY_POINT, req.getResponseType());
		this.assertEquals(5, req.getNumberOfResults());
		this.assertEquals(point, req.getIdPoint());
		this.assertEquals(PredictServiceRequest.PredictType.PERC_CHG_FROM_NOMINAL, req.getPredictType());
		this.assertEquals(PredictServiceRequest.DataSeries.INCREMENTAL_ADD, req.getDataSeries());
		
		PredictionRequest pReq = req.getPredictionRequest();
		this.assertEquals(22L, pReq.getModelId());
		this.assertEquals(2, pReq.getAdjustmentSet().getAdjustmentCount());
		
		
		Adjustment adj = pReq.getAdjustmentSet().getAdjustments()[0];
		this.assertEquals(1, adj.getId());
		this.assertEquals(.5d, adj.getValue());
		
		adj = pReq.getAdjustmentSet().getAdjustments()[1];
		this.assertEquals(4, adj.getId());
		this.assertEquals(2d, adj.getValue());
	}
	

	public void testBasicPrediction() throws Exception {
		
		XMLInputFactory xinFact = XMLInputFactory2.newInstance();
		XMLStreamReader xsr = xinFact.createXMLStreamReader(
			this.getClass().getResourceAsStream("/gov/usgswim/sparrow/test/sample/predict-request-0.xml"));
		
		PredictService service = new PredictService();
		Double2D result = (Double2D) service.dispatch(xsr);

		Data2DCompare comp = buildPredictionComparison(result);
		
		for (int i = 0; i < comp.getColCount(); i++)  {
			System.out.println("col " + i + " error: " + comp.findMaxCompareValue(i));
		}
		
		assertEquals(0d, comp.findMaxCompareValue(), 0.004d);

	}
	
	protected Data2DCompare buildPredictionComparison(Double2D toBeCompared) throws Exception {
		InputStream fileStream = this.getClass().getResourceAsStream("/gov/usgswim/sparrow/test/predict.txt");
		Double2D data = TabDelimFileUtil.readAsDouble(fileStream, true);
		int[] DEFAULT_COMP_COLUMN_MAP =
			new int[] {40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 39, 15};
		
		Data2DCompare comp = new Data2DCompare(toBeCompared, data, DEFAULT_COMP_COLUMN_MAP);
		
		return comp;
	}

}
