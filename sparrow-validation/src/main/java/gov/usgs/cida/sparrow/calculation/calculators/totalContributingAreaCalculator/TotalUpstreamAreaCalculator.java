package gov.usgs.cida.sparrow.calculation.calculators.totalContributingAreaCalculator;

import gov.usgs.cida.sparrow.calculation.framework.SparrowCalculatorBase;
import gov.usgs.cida.sparrow.calculation.framework.CalculationResult;
import gov.usgswim.sparrow.PredictData;
import gov.usgswim.sparrow.TopoData;
import gov.usgswim.sparrow.request.FractionedWatershedAreaRequest;
import gov.usgswim.sparrow.request.ReachID;
import gov.usgswim.sparrow.service.ConfiguredCache;
import gov.usgswim.sparrow.service.SharedApplication;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Compares the db value for cumulative watershed area to the aggregated value,
 * built by adding up all the catchments upstream of the reach.
 *
 * @author eeverman
 */
public class TotalUpstreamAreaCalculator extends SparrowCalculatorBase {


	/** If true, don't correct frac values that do not total to one */
	private boolean forceUncorrectedFracValues = true;

	/** If true, IfTran is ignored for calculating upstream reaches.  Having this true really is the definition of 'upstream' area. */
	private final boolean forceIgnoreIfTran = true;

	//This flag can be set to true to force the fractional watershed area
	//Action to do pure cumulative area calculations, not fractionalal ones.
	private boolean forceNonFractionedArea = false;

	int numberOfReachAreaFractionMapsAllowedInMemory_original;
	int numberOfReachAreaFractionMapsAllowedInMemory_forTest = 100000;
	private Connection connection;

	@Override
	public boolean requiresDb() { return true; }
	@Override
	public boolean requiresTextFile() { return false; }



	@Override
	public CalculationResult calculate(Long modelId) throws Exception {
		return this.calculateTotalUpstreamAreaAndPutInDb(modelId);
		//return testModelBasedOnHuc2Aggregation(modelId);
	}

	public TotalUpstreamAreaCalculator(
			boolean failedTestIsOnlyAWarning) {
		super(failedTestIsOnlyAWarning);
		try {
			this.connection = SharedApplication.getInstance().getRWConnection();
		} catch (SQLException ex) {
			Logger.getLogger(TotalUpstreamAreaCalculator.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	/**
	 * @param failedTestIsOnlyAWarning
	 * @param forceNonFractionedArea Takes precidence over forceUncorrectedFracValues.
	 * @param forceUncorrectedFracValues
	 */
	public TotalUpstreamAreaCalculator(
			boolean failedTestIsOnlyAWarning,
			boolean forceNonFractionedArea,
			boolean forceUncorrectedFracValues) {

		super(failedTestIsOnlyAWarning);
		this.forceNonFractionedArea = forceNonFractionedArea;
		this.forceUncorrectedFracValues = forceUncorrectedFracValues;

	}

	@Override
	public void beforeEachCalc(Long modelId) {
		numberOfReachAreaFractionMapsAllowedInMemory_original =
				ConfiguredCache.FractionedWatershedArea.getCacheImplementation().getCacheConfiguration().getMaxElementsInMemory();

		ConfiguredCache.FractionedWatershedArea.getCacheImplementation().getCacheConfiguration().
				setMaxElementsInMemory(numberOfReachAreaFractionMapsAllowedInMemory_forTest);

		//The settings for this test are important, so record them to the log
		this.recordInfo(modelId,
				"Settings:  forceNonFractionedArea: " + forceNonFractionedArea +
				", forceIgnoreIfTran: " + forceIgnoreIfTran +
				", forceUncorrectedFracValues:" + forceUncorrectedFracValues ,
				true);

		super.beforeEachCalc(modelId);

		try {
			this.connection = SharedApplication.getInstance().getRWConnection();
		} catch (SQLException ex) {
			Logger.getLogger(TotalUpstreamAreaCalculator.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	@Override
	public void afterEachCalc(Long modelId) {
		ConfiguredCache.FractionedWatershedArea.getCacheImplementation().getCacheConfiguration().
						setMaxElementsInMemory(numberOfReachAreaFractionMapsAllowedInMemory_original);
		ConfiguredCache.FractionedWatershedArea.getCacheImplementation().removeAll();

		super.afterEachCalc(modelId);
	}

	/**
	 * Runs QA checks against the data.
	 * @param modelId
	 * @return
	 * @throws Exception
	 */
	public CalculationResult calculateTotalUpstreamAreaAndPutInDb(Long modelId) throws Exception {

		recordInfo(modelId, "Starting:  Load model predict data from the db");
		PredictData predictData = SharedApplication.getInstance().getPredictData(modelId);
		recordInfo(modelId, "Completed:  Load model predict data from the db");
		TopoData topo = predictData.getTopo();
		//ModelReachAreaRelations reachToHuc2Relation = SharedApplication.getInstance().getModelReachAreaRelations(new ModelAggregationRequest(modelId, AggregationLevel.HUC2));

		//All the HUC2s in this model, with the HUC id as the row ID.
		//DataTable regionDetail = SharedApplication.getInstance().getHucsForModel(new ModelHucsRequest(modelId, HucLevel.HUC2));

		int topoRowCount = topo.getRowCount();
		//@todo really use a hashmap, or just an arraylist of {reachId, area} pairs?
		IdAreaPair[] idAreaPairs = new IdAreaPair[topoRowCount];
		recordInfo(modelId, "Starting calculation of upstream reach areas for model.");
		for (int row = 0; row < topoRowCount; row++) {
			Long reachId = predictData.getIdForRow(row);
			Double calculatedFractionalWatershedArea = null;
			ReachID reachUId = new ReachID(modelId, reachId);
			//Do Fractioned watershed area calc
			recordRowTrace(modelId, reachId, row, "Starting: CalcFractionedWatershedArea for row # " + row + " of " + topoRowCount);
			FractionedWatershedAreaRequest areaReq = new FractionedWatershedAreaRequest(
					reachUId, forceUncorrectedFracValues, forceIgnoreIfTran, forceNonFractionedArea);
			calculatedFractionalWatershedArea =  SharedApplication.getInstance().getFractionedWatershedArea(areaReq);
			idAreaPairs[row] = new IdAreaPair(reachId, calculatedFractionalWatershedArea);
			recordRowTrace(modelId, reachId, row, "Completed: CalcFractionedWatershedArea");
		}
		recordInfo(modelId, "Finished calculation of upstream reach areas for model.");

		this.updateAreas(modelId, idAreaPairs);

		return result;
	}

	private void updateAreas(Long modelId, IdAreaPair[] idAreaPairs) throws SQLException {
		final int pairCount = idAreaPairs.length;
		if(0 == pairCount){
			return;
		}
		final int lastPair = pairCount - 1;	//checked at the bottom of each iteration to see if the last query batch should get executed
		final int minimumBatchSize = 10000;	//minimum batch size for queries
		final double fractionOfTotal = 0.10;	//if the fraction of the total # of queries is larger than minimumBatchSize, use this fraction as the batch size
		final int batchSize = Math.max((int)Math.floor(pairCount * fractionOfTotal), minimumBatchSize);
		int batchBoundary = batchSize;

		PreparedStatement updateArea = null;

		String updateString =
			"UPDATE model_reach_attrib SET tot_upstream_area = ? WHERE model_reach_id = (" +
				"SELECT model_reach_id FROM model_reach " +
					"WHERE " +
					"model_reach.identifier = ? " +
					"AND " +
					"model_reach.sparrow_model_id = ? " +
			")";

		try {
		    connection.setAutoCommit(false);
		    updateArea = connection.prepareStatement(updateString);
		    this.recordInfo(modelId, "Begin persisting model area calculations in batches of size " + batchSize);
		    this.recordInfo(modelId, "0%...");
		    for (int i = 0; i < pairCount; ++i) {
				IdAreaPair idAreaPair = idAreaPairs[i];
				updateArea.setDouble(1, idAreaPair.getArea());
				updateArea.setLong(2, idAreaPair.getReachId());
				updateArea.setLong(3, modelId);
				updateArea.addBatch();
				if(batchBoundary == i || lastPair == i){
					this.recordInfo(modelId, "Pushing a batch of updates to the db (row " + i + ") percent complete: " + (int)Math.floor(100.0 * ((double)i/pairCount)) + "%...");
					batchBoundary += batchSize;
					updateArea.executeBatch();
				}
		    }
		    connection.commit();
		    this.recordInfo(modelId, "100%");
		    this.recordInfo(modelId, "Done persisting model area calculations");

		} catch (SQLException e ) {
		    if (connection != null) {
				try {
					this.recordError(modelId, "Error committing transaction. Rolling back...");
					connection.rollback();
					this.recordInfo(modelId, "Successfully rolled back transaction", false);
				} catch(SQLException excep) {
					String msg = "Error rolling back transaction.";
					this.recordError(modelId, msg);
					throw new SQLException(msg);
				}
			} else{
			    this.recordError(modelId, "Database connection Error");
			    throw new SQLException();
		    }
		} finally {
		    if (updateArea != null) {
				updateArea.close();
		    }
		    connection.setAutoCommit(true);
		}
	}


}

