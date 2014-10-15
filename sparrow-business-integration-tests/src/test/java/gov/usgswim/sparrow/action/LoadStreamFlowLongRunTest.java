package gov.usgswim.sparrow.action;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import gov.usgs.cida.datatable.DataTable;
import gov.usgswim.sparrow.SparrowTestBaseWithDB;
import gov.usgswim.sparrow.datatable.SparrowColumnSpecifier;
import gov.usgswim.sparrow.datatable.TableProperties;
import gov.usgswim.sparrow.domain.DataSeriesType;

import org.junit.Test;

/**
 * Tests the gov.usgswim.sparrow.action.LoadFlux methods.
 * 
 * @author klangsto
 */

public class LoadStreamFlowLongRunTest extends SparrowTestBaseWithDB {

	
	static SparrowColumnSpecifier model50Flow;
	
	@Override
	public void doBeforeClassSingleInstanceSetup() throws Exception {
		LoadStreamFlow lf = new LoadStreamFlow();
		lf.setModelId(SparrowTestBaseWithDB.TEST_MODEL_ID);
		model50Flow = lf.run();
	}
	
	
	/**
	 * Tests the basic getter and setter functionality.
	 * @throws Exception
	 */
	@Test
	public void testLoadFluxSetters() throws Exception {

		LoadStreamFlow lf = new LoadStreamFlow();
		lf.setModelId(SparrowTestBaseWithDB.TEST_MODEL_ID);
		assertEquals(SparrowTestBaseWithDB.TEST_MODEL_ID.longValue(), lf.getModelId());
	}
	
	/**
	 * Tests the columns.
	 * @throws Exception
	 */
	@Test
	public void testLoadFluxColumns() throws Exception {
		
		DataTable dt = model50Flow.getTable();
		
		assertEquals(1, dt.getColumnCount());
		assertTrue(dt.hasRowIds());
		
		assertEquals(Action.getDataSeriesProperty(DataSeriesType.flux, false), dt.getName(0));
		assertEquals(Action.getDataSeriesProperty(DataSeriesType.flux, true), dt.getDescription(0));
		assertEquals(DataSeriesType.flux.name(),
				dt.getProperty(0, TableProperties.DATA_SERIES.toString()));
		assertEquals("Water",
				dt.getProperty(0, TableProperties.CONSTITUENT.toString()));

	}
	
	/**
	 * Tests the data.
	 * @throws Exception
	 */
	@Test
	public void testLoadFluxData() throws Exception {
		
		DataTable dt = model50Flow.getTable();
		
		assertEquals(8321, dt.getRowCount());
		int row = dt.getRowForId(9388L);
		assertEquals((Double) 1063.71, dt.getDouble(row, 0));
		row = dt.getRowForId(9390L);
		assertEquals((Double) 741.45, dt.getDouble(row, 0));

	}
	
}

