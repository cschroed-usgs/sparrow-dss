package gov.usgswim.sparrow.action;

import gov.usgs.cida.datatable.DataTable;
import gov.usgs.cida.datatable.impl.SimpleDataTableWritable;
import gov.usgswim.sparrow.PredictData;
import gov.usgswim.sparrow.SparrowTestBase;
import static org.junit.Assert.*;
import gov.usgswim.sparrow.domain.ReachRowValueMap;
import gov.usgswim.sparrow.request.FractionedWatershedAreaRequest;
import gov.usgswim.sparrow.request.ReachID;
import gov.usgswim.sparrow.service.ConfiguredCache;
import gov.usgswim.sparrow.service.SharedApplication;
import java.util.ArrayList;
import java.util.Map.Entry;
import org.junit.Test;

/**
 *
 * @author eeverman
 */
public class CalcFractionedWatershedAreaDBTest extends SparrowTestBase {


	@Test
	public void cacheOptimizedCalcVsNonCachedCalcComparison() throws Exception {
		PredictData pd = SharedApplication.getInstance().getPredictData(TEST_MODEL_ID);
		DataTable topo = pd.getTopo();
		int rowCount = topo.getRowCount();
		DataTable incAreaTable = buildFakeAreaTable(rowCount);

		ArrayList<Double> uncachedAreas = new ArrayList<Double>(rowCount);
		ArrayList<Double> cachedAreas = new ArrayList<Double>(rowCount);



		//Do test w/o the optimized cache
		long unoptimizedStartTime = System.currentTimeMillis();
		for (int row = 0; row < rowCount; row++) {
			Long rowId = topo.getIdForRow(row);
			ReachID reachId = new ReachID(TEST_MODEL_ID, rowId);
			FractionedWatershedAreaRequest req = new FractionedWatershedAreaRequest(reachId, false, false, false);

			CalcFractionedWatershedArea action = new CalcFractionedWatershedArea(req, incAreaTable, false);
			Double area = action.run();
			uncachedAreas.add(area);

			assertNotNull(area);
			assertTrue(area > 0d);
		}
		long unoptimizedTime = System.currentTimeMillis() - unoptimizedStartTime;

		//Do test allowing the cache to be used
		long optimizedStartTime = System.currentTimeMillis();
		for (int row = 0; row < rowCount; row++) {
			Long rowId = topo.getIdForRow(row);
			ReachID reachId = new ReachID(TEST_MODEL_ID, rowId);
			FractionedWatershedAreaRequest req = new FractionedWatershedAreaRequest(reachId, false, false, false);

			CalcFractionedWatershedArea action = new CalcFractionedWatershedArea(req, incAreaTable, true);
			Double area = action.run();
			ConfiguredCache.FractionedWatershedArea.put(reachId, area);
			cachedAreas.add(area);

			assertNotNull(area);
			assertTrue(area > 0d);
		}
		long optimizedTime = System.currentTimeMillis() - optimizedStartTime;

		//Compare results
		for (int row = 0; row < rowCount; row++) {
			boolean equal = isEqual(uncachedAreas.get(row), cachedAreas.get(row), .00001D);

			if (! equal) {
				System.out.println("Expect: " + uncachedAreas.get(row) + " Actual: " + cachedAreas.get(row));
				System.out.println("** Row: " + row);
			}

			assertTrue(isEqual(uncachedAreas.get(row), cachedAreas.get(row), .000001D));
		}

//		System.out.println("Optimized Time: " + optimizedTime);
//		System.out.println("Unoptimized Time: " + unoptimizedTime);
	}

	@Test
	public void compareFracMapsOfProblemReaches() throws Exception {
		PredictData pd = SharedApplication.getInstance().getPredictData(TEST_MODEL_ID);
		DataTable topo = pd.getTopo();

		ReachID reach6717 = new ReachID(TEST_MODEL_ID, 6717L);
		ReachID reach6719 = new ReachID(TEST_MODEL_ID, 6719L);
		ReachID reach6716 = new ReachID(TEST_MODEL_ID, 6716L);

		CalcReachAreaFractionMap action6717 = new CalcReachAreaFractionMap(reach6717, false, false);
		CalcReachAreaFractionMap action6719 = new CalcReachAreaFractionMap(reach6719, false, false);
		CalcReachAreaFractionMap action6716 = new CalcReachAreaFractionMap(reach6716, false, false);

		ReachRowValueMap map6717 = action6717.run();
		ReachRowValueMap map6719 = action6719.run();
		ReachRowValueMap map6716 = action6716.run();

		//reportDifferences(topo, map6716, map6717);

		assertTrue(map6717.containsKey(new Integer(8280)));
		assertTrue(! map6717.containsKey(new Integer(8281)));
		assertTrue(map6716.containsKey(new Integer(8281)));
		assertTrue(map6716.containsKey(new Integer(8280)));


	}

	public void reportDifferences(ReachRowValueMap a, ReachRowValueMap b) {
		for(Entry<Integer, Float> aEntry: a.entrySet()) {

			Integer aRow = aEntry.getKey();
			Float aVal = aEntry.getValue();

			Float bVal = b.get(aEntry.getKey());

			if (bVal == null) {
				System.out.println("b does not contain the Entry <" + aRow + ", " + aVal + ">");
			} else if (! isEqual(aVal, bVal, .00001D)) {
				System.out.println("b contains a different value for row: " +  aRow + ".  A val: " + aVal+ ", " + "b val: " + bVal);
			} else {
				System.out.println("both  contain the Entry <" + aRow + ", " + aVal + ">");
			}
		}

		for(Entry<Integer, Float> bEntry: b.entrySet()) {

			Integer bRow = bEntry.getKey();
			Float bVal = bEntry.getValue();

			Float aVal = a.get(bEntry.getKey());

			if (aVal == null) {
				System.out.println("a does not contain the Entry <" + bRow + ", " + bVal + ">");
			}
		}
	}

	public void reportDifferences(DataTable topo, ReachRowValueMap a, ReachRowValueMap b) {

		int rowCount = topo.getRowCount();

		for (int row = 0; row < rowCount; row++) {
			Float aVal = a.get(row);
			Float bVal = b.get(row);

			if (aVal != null && bVal != null) {
				if (isEqual(aVal, bVal, .00001D)) {
					System.out.println("both  contain the Entry <" + row + ", " + aVal + ">");
				} else {
					System.out.println("b has a different value for row: " +  row + ".  A val: " + aVal+ ", " + "b val: " + bVal + "( id: " + topo.getIdForRow(row) + ")");
				}
			} else if (aVal != null) {
				System.out.println("b missing the Entry <" + row + ", " + aVal + ">");
			} else if (bVal != null) {
				System.out.println("b missing the Entry <" + row + ", " + bVal + ">");
			} else {
				//both null

			}

		}
	}

	protected DataTable buildFakeAreaTable(int numberOfRows) {
		double[][] data = new double[numberOfRows][];

		for (int r = 0; r < numberOfRows; r++) {
			data[r] = new double[] {(double)r, 1d};
		}

		SimpleDataTableWritable table = new SimpleDataTableWritable(data, new String[] {"Fake ID (row)", "Incremental Area"});
		return table.toImmutable();
	}

}
