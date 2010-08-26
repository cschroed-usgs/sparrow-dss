package gov.usgswim.sparrow.cachefactory;

import gov.usgswim.sparrow.PredictData;
import gov.usgswim.sparrow.action.LoadModelPredictData;

import net.sf.ehcache.constructs.blocking.CacheEntryFactory;

/**
 * Loads data the data needed to run predictions for a SPARROW model.
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
public class PredictDataFactory implements CacheEntryFactory {

	@Override
	public PredictData createEntry(Object modelId) throws Exception {
		Long id = (Long)modelId;
		PredictData result = null;
		
		result = new LoadModelPredictData(id).run();
		
		return result;
	}

}
