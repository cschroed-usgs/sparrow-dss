package gov.usgswim.sparrow.test.cachefactory;

import gov.usgswim.sparrow.UncertaintyData;
import gov.usgswim.sparrow.UncertaintyDataRequest;
import gov.usgswim.sparrow.UncertaintySeries;
import gov.usgswim.sparrow.cachefactory.UncertaintyDataFactory;

import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

/**
 * @author eeverman
 *
 */
public class UncertaintyDataFactoryTest {

	@Test
	public void testLoad() throws Exception {

		final long MODEL_ID = 50L;
		final int REACH_COUNT = 8321;
		final int SOURCE_COUNT = 5;
		
		for (UncertaintySeries series : UncertaintySeries.values()) {
			
			if (series.isSourceSpecific()) {
				for (int src = 1; src < SOURCE_COUNT; src++) {
					
					UncertaintyDataRequest req =
						new UncertaintyDataRequest(MODEL_ID, series, src);
					UncertaintyDataFactory factory = new UncertaintyDataFactory();
					UncertaintyData data = factory.createEntry(req);
					
					assertEquals(REACH_COUNT, data.getRowCount());
					
					//System.out.println("*** Series " + series + " for source " + src + " ***");
					//printFirstTest(data);
				}
			} else {
				UncertaintyDataRequest req =
					new UncertaintyDataRequest(MODEL_ID, series, null);
				UncertaintyDataFactory factory = new UncertaintyDataFactory();
				UncertaintyData data = factory.createEntry(req);
				
				assertEquals(REACH_COUNT, data.getRowCount());
				
				//System.out.println("*** Series " + series + ", non source specific ***");
				//printFirstTest(data);
			}
			
		}
		
	}
	
	
	public void printFirstTest(UncertaintyData data) {
		for (int i=0; i<10; i++) {
			System.out.println(
					"" + i + " Mean: " + data.getMean(i) +
					" SE: " + data.getStandardError(i) + 
					" COV: " + data.calcCoeffOfVariation(i));
		}
	}
}
