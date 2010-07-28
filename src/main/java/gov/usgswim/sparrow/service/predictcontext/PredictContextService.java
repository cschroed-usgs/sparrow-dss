package gov.usgswim.sparrow.service.predictcontext;

import gov.usgs.cida.config.DynamicReadOnlyProperties;
import gov.usgswim.service.HttpService;
import gov.usgswim.sparrow.parser.DataColumn;
import gov.usgswim.sparrow.parser.DataSeriesType;
import gov.usgswim.sparrow.parser.PredictionContext;
import gov.usgswim.sparrow.service.ReturnStatus;
import gov.usgswim.sparrow.service.SharedApplication;
import gov.usgswim.sparrow.util.QueryLoader;

import java.io.StringReader;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

public class PredictContextService implements HttpService<PredictContextRequest> {

	private QueryLoader props = new QueryLoader("gov/usgswim/sparrow/service/predictcontext/PredictContextServiceTemplate.properties");

	public XMLStreamReader getXMLStreamReader(PredictContextRequest o,
			boolean isNeedsCompleteFirstRow) throws Exception {

		//Store to cache
		PredictionContext context = o.getPredictionContext();
		boolean isSuccess = false;
		try {
			SharedApplication.getInstance().putPredictionContext(context);
			isSuccess = true;
			System.out.println("PREDICTION CONTEXT: " + context.getId());
		} catch (Exception e) {
			// TODO need to log failure
			e.printStackTrace();
		}

		XMLInputFactory inFact = XMLInputFactory.newInstance();
		if (isSuccess) {
			
			DataColumn dataColumn = context.getDataColumn();	//The actual response data
			
			String adjustmentGroups = "";
			String terminalReaches = "";
			String areaOfInterest = "";
			String units = dataColumn.getUnits();
			String constituent = dataColumn.getConstituent();
			String name = dataColumn.getColumnName();
			String description = dataColumn.getDescription();
			
			if (context.getAdjustmentGroups() != null) {
				String contextId = Integer.toString(context.getAdjustmentGroups().hashCode());
				adjustmentGroups = props.getParametrizedQuery("adjustmentGroups",
						new String[] { "AdjustmentContextId",  contextId });
			}
			if (context.getTerminalReaches() != null) {
				String contextId = Integer.toString(context.getTerminalReaches().hashCode());
				terminalReaches = props.getParametrizedQuery("terminalReaches",
						new String[] { "TerminalContextId",  contextId });
			}
			if (context.getAreaOfInterest() != null) {
				String contextId = Integer.toString(context.getAreaOfInterest().hashCode());
				areaOfInterest = props.getParametrizedQuery("areaOfInterest",
						new String[] { "AreaOfInterstContextId",  contextId });
			}
			
			if (units == null) {
				units = "Units Unknown";
			}
			
			if (constituent == null) {
				constituent = "";	//keep it empty so it won't display on client
			}
			
			if (description == null) {
				description = "";	//keep it empty so it won't display on client
			}
			
			

			String response = props.getParametrizedQuery("ResponseOK",
				new String[] {
					"ModelId", context.getModelID().toString(),
					"ContextId", Integer.toString( context.hashCode() ),
					"RowIdType", "reach",
					"adjustmentGroups", adjustmentGroups,
					"AnalysisContextId", Integer.toString( context.getAnalysis().hashCode() ),
					"terminalReaches", terminalReaches,
					"areaOfInterest", areaOfInterest,
					"displayName", name,
					"displayDescription", description,
					"displayUnits", units,
					"displayConstituent", constituent
			});

			return inFact.createXMLStreamReader(new StringReader(response));
		}
		// failure
		return inFact.createXMLStreamReader(
				new StringReader("<PredictionContext-response><status>" + ReturnStatus.ERROR + "</status></PredictionContext-response>"));
	}

	public void shutDown() {
		// TODO Auto-generated method stub

	}



}
