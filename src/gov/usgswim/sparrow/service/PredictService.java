package gov.usgswim.sparrow.service;


import com.ctc.wstx.stax.WstxOutputFactory;

import gov.usgswim.sparrow.Adjustment;
import gov.usgswim.sparrow.Adjustment.AdjustmentType;
import gov.usgswim.sparrow.AdjustmentSet;
import gov.usgswim.sparrow.AdjustmentSetBuilder;
import gov.usgswim.sparrow.AdjustmentSetImm;
import gov.usgswim.sparrow.Data2D;
import gov.usgswim.sparrow.Data2DPercentCompare;
import gov.usgswim.sparrow.PredictionRequest;
import gov.usgswim.sparrow.domain.DomainSerializer;

import gov.usgswim.sparrow.domain.ModelBuilder;
import gov.usgswim.sparrow.util.JDBCUtil;

import java.awt.Point;

import java.io.IOException;

import java.io.OutputStream;

import java.sql.Connection;

import java.sql.SQLException;

import java.util.Hashtable;
import java.util.List;


import javax.naming.NamingException;

import javax.servlet.http.HttpServletResponse;


import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import oracle.mapviewer.share.ext.NSDataSet;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;


public class PredictService implements HttpServiceHandler, ServiceHandler,
			RequestParser<PredictServiceRequest>, RequestHandler<PredictServiceRequest> {
			
			
	protected static Logger log =
		Logger.getLogger(PredictService.class); //logging for this class
		
	protected static String RESPONSE_MIME_TYPE = "application/xml";
		
	
	//They promise these factories are threadsafe
	private static Object factoryLock = new Object();
	//protected static XMLInputFactory xinFact;
	protected static XMLOutputFactory xoFact;
	
	
	public PredictService() {
	}
	



	public void dispatch(XMLStreamReader in,
											 HttpServletResponse response) throws XMLStreamException, IOException {
											 
		PredictServiceRequest req = parse(in);
		response.setContentType(RESPONSE_MIME_TYPE);
		dispatch(req, response.getOutputStream());
	}

	public void dispatch(XMLStreamReader in, OutputStream out) throws XMLStreamException, IOException {
																							
		PredictServiceRequest req = parse(in);
		dispatch(req, out);
	}
	
	public void dispatch(PredictServiceRequest req, OutputStream outStream) throws IOException {
																																 
		synchronized (factoryLock) {
			if (xoFact == null) {
				xoFact = WstxOutputFactory.newInstance();
			}
		}
		
		//TODO need to actually return something here
		Data2D result = runPrediction(req);
		/*
		XMLEventWriter xw = xoFact.createXMLEventWriter(outStream);
																																 
		List<ModelBuilder> models = JDBCUtil.loadModelMetaData(getConnection());
		DomainSerializer ds = new DomainSerializer();
		ds.writeModels(xw, models);
		*/
		
		/*
		 * query for iding a reach...
		 * SELECT * FROM (
select REACH_GEOM as GEOM, model_reach_id, round(
  SDO_GEOM.SDO_DISTANCE(REACH_GEOM, sdo_geometry(2001, 8307, sdo_point_type(-100, 40, NULL), NULL, NULL), 0.00005, 'unit=M'),4
) DISTANCE_IN_METERS_FROM_CLICK
from ALL_GEOM_VW
where SPARROW_MODEL_ID = 22
order by DISTANCE_IN_METERS_FROM_CLICK
) inner
WHERE rownum < 50
		 * 
		 * 
		 */
	}
	
	public Data2D runPrediction(PredictServiceRequest req) {
		Data2D result = null;		//The prediction result
		
		Long modelId = req.getPredictionRequest().getModelId();
		long startTime = System.currentTimeMillis();	//Time started

		try {

			Data2D adjResult = SharedApplication.getInstance().getPredictResultCache().compute(req.getPredictionRequest());
			
			if (req.getPredictType().isComparison()) {
				//need to run the base prediction and the adjusted prediction
				AdjustmentSetImm noAdj = new AdjustmentSetImm();
				PredictionRequest noAdjRequest = new PredictionRequest(modelId, noAdj);
				Data2D noAdjResult = SharedApplication.getInstance().getPredictResultCache().compute(noAdjRequest);
	
				result = new Data2DPercentCompare(
						noAdjResult, adjResult,
						req.getPredictType().equals(PredictServiceRequest.PredictType.DEC_CHG_FROM_NOMINAL),
						true);
						
			} else {
				//need to run only the adjusted prediction
				result = adjResult;
			}
			
			log.debug("Predict service done for model #" + modelId + " (" + result.getRowCount() + " rows) Time: " + (System.currentTimeMillis() - startTime) + "ms");
			
			return result;
			
		} catch (InterruptedException e) {
			log.error("No way to indicate this error to mapViewer, so throwing a runtime exception.", e.getCause());
			throw new RuntimeException(e);
		}
		

	}
	
	public PredictServiceRequest parse(XMLStreamReader reader) throws XMLStreamException {
		PredictServiceRequest req = null;
		
		while (reader.hasNext()) {
			int eventCode = reader.next();
			
			switch (eventCode) {
			case XMLStreamReader.START_ELEMENT:
				String lName = reader.getLocalName();
				
				if ("value-prediction".equals(lName)) {
					req.setPredictType(gov.usgswim.sparrow.service.PredictServiceRequest.PredictType.VALUES);
					PredictionRequest pr = parsePredictSection(reader);
					req.setPredictionRequest(pr);
				} else if ("change-from-nominal".equals(lName)) {
					if (reader.getAttributeValue(null, "type") != null) {
						req.setPredictType(
							PredictServiceRequest.PredictType.find(reader.getAttributeValue(null, "type"))
						);
					} else {
						req.setPredictType(PredictServiceRequest.PredictType.PERC_CHG_FROM_NOMINAL);
					}
					
					PredictionRequest pr = parsePredictSection(reader);
					req.setPredictionRequest(pr);
				} else if ("response".equals(lName)) {
					parseResponseSection(reader, req);
				} else if ("sparrow-prediction-request".equals(lName)) {
					req = new PredictServiceRequest();
				}
				
				
				break;
			}
		}
		
		return req;
	}
	
	/**
	 * Reads just the response portion of the request and then returns.
	 * 
	 * @param reader
	 * @param request
	 * @throws XMLStreamException
	 */
	protected void parseResponseSection(XMLStreamReader reader, PredictServiceRequest req) throws XMLStreamException {
		while (reader.hasNext()) {
			int eventCode = reader.next();
			
			switch (eventCode) {
			case XMLStreamReader.START_ELEMENT:
				{
					String lName = reader.getLocalName();
					
					if ("all-results".equals(lName)) {
						req.setResponseType(gov.usgswim.sparrow.service.PredictServiceRequest.ResponseType.ALL_RESULTS);
					} else if ("identify-by-point".equals(lName)) {
						req.setResponseType(gov.usgswim.sparrow.service.PredictServiceRequest.ResponseType.IDENTIFY_BY_POINT);
						int numResults = Integer.parseInt( reader.getAttributeValue(null, "number-of-results") );
						req.setNumberOfResults(numResults);
					} else if ("point".equals(lName)) {
						double lat = Double.parseDouble( reader.getAttributeValue(null, "lat") );
						double lng = Double.parseDouble( reader.getAttributeValue(null, "long") );
						Point.Double pt = new Point.Double();
						pt.x = lng;
						pt.y = lat;
						req.setIdPoint(pt);
					} else if ("data-series".equals(lName)) {
						req.setDataSeries(PredictServiceRequest.DataSeries.find(StringUtils.trimToEmpty(reader.getElementText()))
						);
					} 
					
				}
				break;
			
			case XMLStreamReader.END_ELEMENT:
				{
					String lName = reader.getLocalName();
					if ("response".equals(lName)) {
						return;
					}
				}
				break;
			}
		}
	}
	
	/**
	 * Reads just the prediction portion of the request and then returns.
	 * 
	 * @param reader
	 * @param request
	 * @throws XMLStreamException
	 */
	protected PredictionRequest parsePredictSection(XMLStreamReader reader) throws XMLStreamException {
		Long modelId = null;
		AdjustmentSet adjSet = null;
		
		while (reader.hasNext()) {
			int eventCode = reader.next();
			
			switch (eventCode) {
			case XMLStreamReader.START_ELEMENT:
				{
					String lName = reader.getLocalName();
					
					if ("model-id".equals(lName)) {
						modelId = Long.parseLong(reader.getElementText());
					} else if ("source-adjustments".equals(lName)) {
						adjSet = parseAdjustmentsSection(reader);
						return new PredictionRequest(modelId, adjSet);
					}
					
				}
				break;
			}
		}
		
		return new PredictionRequest(modelId, adjSet);	//should not get here
	}
	
	/**
	 * Reads just the adjustments portion of the request and then returns.
	 * 
	 * @param reader
	 * @throws XMLStreamException
	 */
	protected AdjustmentSet parseAdjustmentsSection(XMLStreamReader reader) throws XMLStreamException {
		AdjustmentSetBuilder adj = new AdjustmentSetBuilder();
		
		while (reader.hasNext()) {
			int eventCode = reader.next();
			
			switch (eventCode) {
			case XMLStreamReader.START_ELEMENT:
				{
					String lName = reader.getLocalName();
					
					if ("gross-adjust".equals(lName)) {
						int src = Integer.parseInt(reader.getAttributeValue(null, "src"));
						double coef = Double.parseDouble(reader.getAttributeValue(null, "coef"));
						Adjustment a = new Adjustment(AdjustmentType.GROSS_ADJUST, src, coef);
						adj.addAdjustment(a);
					} else {
						throw new XMLStreamException("Unsupported adjustment type");
					}
					
				}
				break;
				
				case XMLStreamReader.END_ELEMENT:
				{
					String lName = reader.getLocalName();
					if (lName.equals("source-adjustments")) {
						return adj.getImmutable();
					}
					
				}
			}
		}
		
		return adj.getImmutable();	//shouldn't get here
	}

}
