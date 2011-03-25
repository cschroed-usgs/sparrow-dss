package gov.usgswim.sparrow.action;

import gov.usgswim.datatable.ColumnData;
import gov.usgswim.datatable.DataTable;
import gov.usgswim.sparrow.datatable.ColumnAttribs;
import gov.usgswim.sparrow.datatable.DataColumn;
import gov.usgswim.sparrow.datatable.DataTableCompare;
import gov.usgswim.sparrow.datatable.PercentageColumnData;
import gov.usgswim.sparrow.datatable.SingleColumnOverrideDataTable;
import gov.usgswim.sparrow.domain.AdvancedComparison;
import gov.usgswim.sparrow.domain.Comparison;
import gov.usgswim.sparrow.domain.ComparisonType;
import gov.usgswim.sparrow.domain.SourceShareComparison;
import gov.usgswim.sparrow.domain.NominalComparison;
import gov.usgswim.sparrow.domain.PredictionContext;
import gov.usgswim.sparrow.service.SharedApplication;

/**
 * This action creates a DataColumn containing the requested comparison result.
 * 
 * @author eeverman
 *
 */
public class CalcComparison extends Action<DataColumn> {


	protected PredictionContext context;
	
	protected String msg;
	
	@Override
	protected String getPostMessage() {
		return msg;
	}

	@Override
	public DataColumn doAction() throws Exception {

		Comparison comparison = context.getComparison();
		
		if (comparison == null || ComparisonType.none.equals(comparison.getComparisonType())) {
			msg = "The prediction context '" + context.getId() + "' has no " +
				"comparison.";
			throw new Exception(msg);
		}
		
		PredictionContext noCompContext = context.getNoComparisonVersion();
		PredictionContext baseContext = null;	//TBD based on type of comparison
		
		if (comparison instanceof NominalComparison) {
			
			baseContext = noCompContext.getNoAdjustmentVersion();
			
		} else if (comparison instanceof SourceShareComparison) {
			
			baseContext = noCompContext.getNoSourceClone();

		} else if (comparison instanceof AdvancedComparison) {
			AdvancedComparison ac = (AdvancedComparison) comparison;
			baseContext = ac.getBasePredictionContext();
			
			Integer id = ac.getPredictionContextId();
			baseContext = SharedApplication.getInstance().getPredictionContext(id);
			
			if (baseContext == null) {
				msg = "Unable to locate context id '" + id + "' for comparison.";
				throw new Exception(msg);
			}
			
		} else {
			msg = "Unrecognized Comparison Subclass '"
				+ comparison.getClass().getName() + "'";
			throw new Exception(msg);
		}
		
		//Get analysis results from analysis cache
		DataColumn baseCol = SharedApplication.getInstance().getAnalysisResult(baseContext);
		DataColumn compCol = SharedApplication.getInstance().getAnalysisResult(noCompContext);
		DataTable baseResult = baseCol.getTable();
		DataTable compResult = compCol.getTable();

		DataTable resultTable = null;
		
		if (! (comparison instanceof SourceShareComparison)) {
			resultTable = new DataTableCompare(baseResult, compResult, comparison.getComparisonType());
		} else {
			ColumnData baseColData = baseResult.getColumn(baseCol.getColumn());
			ColumnData compColData = compResult.getColumn(compCol.getColumn());
			PercentageColumnData percColData = new PercentageColumnData(baseColData, compColData, compColData, null);
			resultTable = new SingleColumnOverrideDataTable(compResult, percColData, compCol.getColumn(), null);
		}
		

		return new DataColumn(resultTable, compCol.getColumn(), context.getId());
	}

	public PredictionContext getContext() {
		return context;
	}

	public void setContext(PredictionContext context) {
		this.context = context;
	}

}
