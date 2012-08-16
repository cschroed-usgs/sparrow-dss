package gov.usgswim.sparrow.action;

import gov.usgswim.datatable.ColumnData;
import gov.usgswim.datatable.DataTable;
import gov.usgswim.datatable.DataTableSet;
import gov.usgswim.datatable.HashMapColumnIndex;
import gov.usgswim.datatable.impl.DataTableSetSimple;
import gov.usgswim.datatable.impl.SimpleDataTable;
import gov.usgswim.sparrow.PredictData;
import gov.usgswim.sparrow.datatable.SparrowColumnSpecifier;
import gov.usgswim.sparrow.datatable.TableProperties;
import gov.usgswim.sparrow.domain.*;
import gov.usgswim.sparrow.request.DeliveryReportRequest;
import gov.usgswim.sparrow.request.UnitAreaRequest;
import gov.usgswim.sparrow.service.SharedApplication;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This action assembles a DataTable of Total Delivered Load by source and for
 * all sources.  The returned DataTable has this structure:
 * 
 * Row ID    :  Reach ID (actual id for the row, not a column)
 * column 0  :  Reach Name
 * Column 1  :  EDA Code
 * Column 2  :  Source 0 Total Delivered Load
 * Column 3  :  Source 1 Total Delivered Load
 * ...etc. for all sources
 * Col. Last :  Total Delivered Load for all sources
 * 
 * @author eeverman
 *
 */
public class BuildTotalDeliveredLoadSummaryReport extends Action<DataTableSet> {

	protected AdjustmentGroups adjustmentGroups;
	protected TerminalReaches terminalReaches;
	
	//Loaded or created by the action itself
	Long modelId = null;
	private transient PredictData predictData = null;
	private transient DataTable idInfo = null;
	private transient DataTable terminalReachDrainageArea = null;
	private transient List<ColumnData> expandedTotalDelLoadForAllSources;
	private transient SparrowColumnSpecifier streamFlow = null;
	
	protected String msg = null;	//statefull message for logging
	
	
	/**
	 * Clear designation of init values
	 */
	protected void initRequiredFields() throws Exception {
		modelId = adjustmentGroups.getModelID();
		predictData = SharedApplication.getInstance().getPredictData(modelId);
		idInfo = SharedApplication.getInstance().getModelReachIdentificationAttributes(modelId);
		terminalReachDrainageArea = SharedApplication.getInstance().getCatchmentAreas(new UnitAreaRequest(modelId, AggregationLevel.NONE, true));
		streamFlow = SharedApplication.getInstance().getStreamFlow(modelId);
		
		//Basic predict context, which we need data for all sources
		BasicAnalysis analysis = new BasicAnalysis(
				DataSeriesType.total_delivered_flux, null, null, null);
			
		PredictionContext basicPredictContext = new PredictionContext(
				modelId, adjustmentGroups, analysis, terminalReaches,
				null, NoComparison.NO_COMPARISON);
		
		BuildAnalysisForAllSources action =
				new BuildAnalysisForAllSources(basicPredictContext, 
				BuildAnalysisForAllSources.COLUMN_NAME_FORMAT.SOURCE_NAME_ONLY);
		
		
		expandedTotalDelLoadForAllSources = action.run();
	}
	
	@Override
	public DataTableSet doAction() throws Exception {
		
		initRequiredFields();

		DataTable srcMetadata = predictData.getSrcMetadata();
		//int srcCount = srcMetadata.getRowCount();
		
		BasicAnalysis analysis = new BasicAnalysis(
				DataSeriesType.total_delivered_flux, null, null, null);
			
		HashMapColumnIndex index = new  HashMapColumnIndex(predictData.getTopo());
		
				//We can't add immutable columns to a writable table, so we need to construct a new table
		SimpleDataTable infoTable = new SimpleDataTable(
				new ColumnData[] {idInfo.getColumn(0), idInfo.getColumn(1), terminalReachDrainageArea.getColumn(1), streamFlow.getColumnData()},
				"Identification and basic info", 
				"Identification and basic info",
				buildTableProperties(), index);
	
		
		SimpleDataTable dataTable = new SimpleDataTable(
				expandedTotalDelLoadForAllSources.toArray(new ColumnData[]{}),
				"Total Delivered Load by Source", 
				"Total Delivered Load for each source individualy and for all sources",
				buildTableProperties(), index);
		
		DataTableSet tableSet = new DataTableSetSimple(new DataTable.Immutable[]{infoTable.toImmutable(), dataTable.toImmutable()},
				"Total Delivered Load Summary Report",
				"Total Delivered Load for each source individualy and for all sources");
		
		if (tableSet.isValid()) {
			return tableSet;
		} else {
			msg = "The resulting table was invalid";
			return null;
		}
		
	}
	
	protected Map<String, String> buildTableProperties() {
		HashMap<String, String> props = new HashMap<String, String>();
		props.put(TableProperties.MODEL_ID.toString(), modelId.toString());
		props.put(TableProperties.ROW_LEVEL.toString(), AggregationLevel.REACH.toString());
		props.put(TableProperties.CONSTITUENT.toString(), predictData.getModel().getConstituent());
		return props;
	}
	
	//
	//Setter methods
	public void setAdjustmentGroups(AdjustmentGroups adjustmentGroups) {
		this.adjustmentGroups = adjustmentGroups;
	}

	public void setTerminalReaches(TerminalReaches terminalReaches) {
		this.terminalReaches = terminalReaches;
	}
	
	public void setDeliveryReportRequest(DeliveryReportRequest request) {
		terminalReaches = request.getTerminalReaches();
		adjustmentGroups = request.getAdjustmentGroups();
	}
	
	
	
	//Action override methods
	@Override
	protected String getPostMessage() {
		return msg;
	}
	
	@Override
	protected void validate() {
		
		if (adjustmentGroups == null || terminalReaches == null) {
			addValidationError("Both the adjustmentGroups and terminalReaches must be non-null.");
			return;	//Don't check beyond a null value
		}

		//
		//
		
		if (terminalReaches.isEmpty()) {
			addValidationError("the terminalReaches must be non-empty to do delivery analysis.");
		}
		
		if (adjustmentGroups.getModelID() != terminalReaches.getModelID()) {
			addValidationError("The model IDs of the adjustmentGroups and the terminalReaches must be the same.");
		}


	}
	
}
