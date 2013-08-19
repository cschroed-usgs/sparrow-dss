package gov.usgswim.sparrow.cachefactory;


import gov.usgswim.sparrow.action.CalcFractionedWatershedArea;
import gov.usgswim.sparrow.request.FractionedWatershedAreaRequest;
import gov.usgswim.sparrow.request.ReachID;
import net.sf.ehcache.constructs.blocking.CacheEntryFactory;

/**
 * A thin wrapper around an Action for EHCache CacheEntryFactory.
 * 
 * Caching, blocking, and de-caching are all handled by EHCache system.
 *
 * @author eeverman
 */
public class FractionedWatershedAreaFactory implements CacheEntryFactory {

	@Override
	public Double createEntry(Object request) throws Exception {
		
		CalcFractionedWatershedArea action = new CalcFractionedWatershedArea((FractionedWatershedAreaRequest) request);
		Double result = action.run();
		
		return result;
	}
	


}
