package gov.usgswim.sparrow.cachefactory;

import gov.usgs.cida.datatable.DataTable;
import gov.usgswim.sparrow.PredictData;
import gov.usgswim.sparrow.action.CalcAdjustedSources;
import gov.usgswim.sparrow.domain.AdjustmentGroups;
import gov.usgswim.sparrow.service.SharedApplication;
import net.sf.ehcache.constructs.blocking.CacheEntryFactory;

/**
 * This factory class creates adjusted source values on demand for an EHCache.
 * 
 * When the cache receives a get(AdjustmentGroups) call and it doesn't have a cache
 * entry for that request, the createEntry() method of this class is called
 * and the returned value is cached.
 * 
 * This class implements CacheEntryFactory, which plugs into the caching system
 * so that the createEntry() method is only called when a entry needs to be
 * created/loaded.
 * 
 * Caching, blocking, and de-caching are all handled by the caching system, so
 * that this factory class only needs to worry about building a new entity in
 * (what it can consider) a single thread environment.
 * 
 * @author eeverman
 *
 */
public class AdjustedSourceFactory implements CacheEntryFactory {
	@Override
	public DataTable createEntry(Object adjustmentGroups) throws Exception {
		AdjustmentGroups groups = (AdjustmentGroups)adjustmentGroups;
		
		PredictData data = SharedApplication.getInstance().getPredictData(groups.getModelID());
		
		CalcAdjustedSources action = new CalcAdjustedSources();
		action.setAdjustments(groups);
		action.setPredictData(data);
		
		DataTable src = action.run();

		return src.toImmutable();
	}

}
