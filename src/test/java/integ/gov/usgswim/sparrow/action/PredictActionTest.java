package gov.usgswim.sparrow.action;

import static org.junit.Assert.assertEquals;
import gov.usgswim.datatable.DataTable;
import gov.usgswim.sparrow.PredictData;
import gov.usgswim.sparrow.SparrowDBTest;
import gov.usgswim.sparrow.datatable.PredictResult;
import gov.usgswim.sparrow.parser.AdjustmentGroups;
import gov.usgswim.sparrow.service.SharedApplication;
import gov.usgswim.sparrow.test.TestHelper;
import gov.usgswim.sparrow.util.DLUtils;

import org.junit.BeforeClass;
import org.junit.Test;

/**
 * This test was created to recreate a calc error where delivery based calcs
 * (like total delivered flux) were resulting in mapped values identical
 * to the base data (i.e. total flux).
 * 
 * @author eeverman
 */
public class PredictActionTest extends SparrowDBTest {

	static final Long MODEL_ID = 50L;	//MRB 2
	
	static PredictData unmodifiedPredictData;
	static PredictData predictData;
	
	static DataTable stdData;
	
	@BeforeClass
	public static void setUp() throws Exception {
		String stdDataQuery = TestHelper.getFileAsString(PredictActionTest.class, "query", "sql");

		stdData = DLUtils.readAsDouble(getConnection(), stdDataQuery, 40);
	}
	
	@Test
	public void testComparison() throws Exception {
		
		AdjustmentGroups groups = new AdjustmentGroups(MODEL_ID);
		PredictResult result = SharedApplication.getInstance().getPredictResult(groups);

		int incValueFail = 0;
		int incRowFail = 0;
		int totalValueFail = 0;
		int totalRowFail = 0;
		
		int anyValueFail = 0;
		int anyRowFail = 0;
		
		int goodIncRows = 0;
		int goodTotalRows = 0;
		
		for (int r=0; r < stdData.getRowCount(); r++) {
			//The first column of stdData is the IDENTIFIER
			
			Long id = stdData.getLong(r, 0);
			int predictDataRow = result.getRowForId(id);
			boolean rowMatches = true;	//assume this row matches
			boolean incMatches = true;
			boolean totalMatches = true;
			
			//Compare Incremental Values (c is column in std data)
			for (int c=1; c < 6; c++) {
				double stdVal = stdData.getDouble(r, c);
				double predictVal = result.getDouble(predictDataRow, c - 1);
				
				if (Math.abs(stdVal - predictVal) < 0.0001d) {
					//Its good!
				} else {
					rowMatches = false;
					incMatches = false;
					incValueFail++;
					anyValueFail++;
				}
			}
			
			{
				//Check the inc total column
				double stdVal = stdData.getDouble(r, 11);
				double predictVal = result.getDouble(predictDataRow, 10);
				if (Math.abs(stdVal - predictVal) < 0.0001d) {
					//Its good!
				} else {
					rowMatches = false;
					incMatches = false;
					incValueFail++;
					anyValueFail++;
				}
			}
			
			//Compare Total Values (c is column in std data)
			for (int c=6; c < 11; c++) {
				double stdVal = stdData.getDouble(r, c);
				double predictVal = result.getDouble(predictDataRow, c - 1);
				
				if (Math.abs(stdVal - predictVal) < 0.0001d) {
					//Its good!
				} else {
					rowMatches = false;
					totalMatches = false;
					totalValueFail++;
					anyValueFail++;
				}
			}
			
			{
				//Check the total column
				double stdVal = stdData.getDouble(r, 12);
				double predictVal = result.getDouble(predictDataRow, 11);
				if (Math.abs(stdVal - predictVal) < 0.0001d) {
					//Its good!
				} else {
					rowMatches = false;
					totalMatches = false;
					totalValueFail++;
					anyValueFail++;
				}
			}
			
			if (! rowMatches) anyRowFail++;

			
			if (incMatches) {
				goodIncRows++;
				
				//Print the first 10 good INC rows
				if (goodIncRows < 10) {
					//printGoodIncRow(stdData, r, result, predictDataRow);
				}
			} else {
				incRowFail++;
				printBadIncRow(stdData, r, result, predictDataRow);
			}
			
			if (totalMatches) {
				goodTotalRows++;
				
				//Print the first 10 good INC rows
				if (goodTotalRows < 10) {
					//printGoodTotalRow(stdData, r, result, predictDataRow);
				}
			} else {
				totalRowFail++;
				printBadTotalRow(stdData, r, result, predictDataRow);
			}
			
		}
		
		System.out.println("Comparison Results for model " + MODEL_ID +  " (" + result.getRowCount() + " rows)");
		System.out.println("Non Matching Incremental Values: " + incValueFail +  " (" + incRowFail + ")");
		System.out.println("Non Matching Total Values: " + totalValueFail +  " (" + totalRowFail + ")");
		System.out.println("Non Matching Values (all): " + anyValueFail + " (" + anyRowFail + ")");
		
		assertEquals("There should be zero rows w/ non-matching values", 0, anyRowFail);
		
	}
	
	
	public void printRow(DataTable table, int row, String tableName) {
		
		System.out.println(tableName + " row " + row + " (" + table.getRowCount() + " rows)");
		
		
		final String TAB = "\t";
		StringBuffer line = new StringBuffer();
		
		line.append("Index");
		for (int c=0; c< table.getColumnCount(); c++) {
			line.append(TAB);
			line.append(table.getName(c));
		}
		System.out.println(line.toString());
		
		line = new StringBuffer();
		
		if (table.hasRowIds()) {
			line.append(table.getIdForRow(row));
		} else {
			line.append("No Ids");
		}
		for (int c=0; c< table.getColumnCount(); c++) {
			line.append(TAB);
			line.append(table.getValue(row, c));
		}
		System.out.println(line.toString());
		
	}
	
	public void printBadIncRow(DataTable std, int stdRow, PredictResult pred, int predRow) {
		
		long id = pred.getIdForRow(predRow);
		System.out.println("** Failed INC values for reach ID " + id);
		
		//Compare Incremental Values (c is column in std data)
		for (int c=1; c < 6; c++) {
			double stdVal = stdData.getDouble(stdRow, c);
			double predictVal = pred.getDouble(predRow, c - 1);
			
			String line;
			if (Math.abs(stdVal - predictVal) < 0.0001d) {
				line = ". . Inc " + c;
			} else {
				line = "*** Inc " + c;
			}
			
			line = line + " " + stdVal + " | " + predictVal;
			System.out.println(line);
		}
	}
	
	public void printGoodIncRow(DataTable std, int stdRow, PredictResult pred, int predRow) {
		
		long id = pred.getIdForRow(predRow);
		System.out.println("** Good INC values for reach ID " + id);
		
		//Compare Incremental Values (c is column in std data)
		for (int c=1; c < 6; c++) {
			double stdVal = stdData.getDouble(stdRow, c);
			double predictVal = pred.getDouble(predRow, c - 1);
			
			String line;
			if (Math.abs(stdVal - predictVal) < 0.0001d) {
				line = ". . Inc " + c;
			} else {
				line = "*** Inc " + c;
			}
			
			line = line + " " + stdVal + " | " + predictVal;
			System.out.println(line);
		}
		
	}
	
	public void printBadTotalRow(DataTable std, int stdRow, PredictResult pred, int predRow) {
		
		long id = pred.getIdForRow(predRow);
		System.out.println("** Failed Total values for reach ID " + id);
		
		//Compare Incremental Values (c is column in std data)
		for (int c=6; c < 11; c++) {
			double stdVal = stdData.getDouble(stdRow, c);
			double predictVal = pred.getDouble(predRow, c - 1);
			
			String line;
			if (Math.abs(stdVal - predictVal) < 0.0001d) {
				line = ". . Tot " + c;
			} else {
				line = "*** Tot " + c;
			}
			
			line = line + " " + stdVal + " | " + predictVal;
			System.out.println(line);
		}
		
		//total total column
		double stdVal = stdData.getDouble(stdRow, 12);
		double predictVal = pred.getDouble(predRow, 11);
		
		String line;
		if (Math.abs(stdVal - predictVal) < 0.0001d) {
			line = ". . Tot T";
		} else {
			line = "*** Tot T";
		}
		
		line = line + " " + stdVal + " | " + predictVal;
		System.out.println(line);
	}
	
	public void printGoodTotalRow(DataTable std, int stdRow, PredictResult pred, int predRow) {
		
		long id = pred.getIdForRow(predRow);
		System.out.println("** Good Total values for reach ID " + id);
		
		//Compare Incremental Values (c is column in std data)
		for (int c=6; c < 11; c++) {
			double stdVal = stdData.getDouble(stdRow, c);
			double predictVal = pred.getDouble(predRow, c - 1);
			
			String line;
			if (Math.abs(stdVal - predictVal) < 0.0001d) {
				line = ". . Tot " + c;
			} else {
				line = "*** Tot " + c;
			}
			
			line = line + " " + stdVal + " | " + predictVal;
			System.out.println(line);
		}
		
		//total total column
		double stdVal = stdData.getDouble(stdRow, 12);
		double predictVal = pred.getDouble(predRow, 11);
		
		String line;
		if (Math.abs(stdVal - predictVal) < 0.0001d) {
			line = ". . Tot T";
		} else {
			line = "*** Tot T";
		}
		
		line = line + " " + stdVal + " | " + predictVal;
		System.out.println(line);
		
	}
	
}

