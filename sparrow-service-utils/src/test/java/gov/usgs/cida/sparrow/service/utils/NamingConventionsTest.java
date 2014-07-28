package gov.usgs.cida.sparrow.service.utils;

import gov.usgs.cida.sparrow.service.util.NamingConventions;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @author eeverman
 */
public class NamingConventionsTest {
	
	 @Test
	 public void trySomeSimpleValues() {
		 
		assertEquals("N1234", NamingConventions.convertContextIdToXMLSafeName(-1234));
		assertEquals("P1234", NamingConventions.convertContextIdToXMLSafeName(1234));
		assertEquals("P0", NamingConventions.convertContextIdToXMLSafeName(0));
		
		//With workspace
		assertEquals(NamingConventions.FLOWLINE_WORKSPACE_NAME +  ":N1234", NamingConventions.getFullFlowlineLayerName(-1234));
		assertEquals(NamingConventions.CATCHMENT_WORKSPACE_NAME + ":P1234", NamingConventions.getFullCatchmentLayerName(1234));
		assertEquals(NamingConventions.CATCHMENT_WORKSPACE_NAME + ":P0", NamingConventions.getFullCatchmentLayerName(0));
	 }
}
