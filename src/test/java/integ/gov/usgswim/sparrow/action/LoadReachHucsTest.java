package gov.usgswim.sparrow.action;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import gov.usgswim.datatable.DataTable;
import gov.usgswim.sparrow.SparrowDBTest;

import java.util.HashSet;

import org.junit.BeforeClass;
import org.junit.Test;

/**
 * 
 * 
 * @author eeverman
 */
public class LoadReachHucsTest extends SparrowDBTest {

	static final Long MODEL_ID = 50L;
	
	static final String hucName = "03030001";
	static HashSet<Integer> reachesInHuc = new HashSet<Integer>();
	
	
	@Override
	public void doSetup() throws Exception {
		
		//reaches in HUC8 #03030001
		reachesInHuc.add(81039);
		reachesInHuc.add(81040);
		reachesInHuc.add(81504);
		reachesInHuc.add(81769);
		reachesInHuc.add(81770);
		reachesInHuc.add(10700);
		reachesInHuc.add(10703);
		reachesInHuc.add(663040);
		reachesInHuc.add(10701);
		reachesInHuc.add(10704);
	
	}
	
	@Test
	public void testComparison() throws Exception {
		
		LoadReachHucsRequest req = new LoadReachHucsRequest(MODEL_ID, 8);
		LoadReachHucs action = new LoadReachHucs();
		action.setRequest(req);
		
		DataTable result = action.run();
		
		int[] rows = result.findAll(0, hucName);
		
		assertEquals(reachesInHuc.size(), rows.length);
		
		for (int i : rows) {
			Long reachId = result.getIdForRow(i);
			int intReachId = reachId.intValue();
			
			assertTrue(reachesInHuc.contains(intReachId));
		}

		assertEquals("HUC Level 8 aggregation data for model " + MODEL_ID,
				result.getName());
		assertEquals(MODEL_ID.toString(), result.getProperty("model_id"));
		assertEquals("8", result.getProperty("huc_level"));
	}
	
}

