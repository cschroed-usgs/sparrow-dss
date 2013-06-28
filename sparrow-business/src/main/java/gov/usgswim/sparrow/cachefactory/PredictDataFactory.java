package gov.usgswim.sparrow.cachefactory;

import gov.usgswim.sparrow.PredictData;
import gov.usgswim.sparrow.action.ILoadModelPredictData;
import gov.usgswim.sparrow.action.LoadModelPredictData;
import gov.usgswim.sparrow.action.LoadModelPredictDataFromSerializationFile;
import gov.usgswim.sparrow.service.SharedApplication;
import javax.servlet.ServletException;

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

	public static final String ACTION_IMPLEMENTATION_CLASS =
		PredictDataFactory.class.getName() + ".ACTION_IMPLEMENTATION_CLASS";
	
	@Override
	public PredictData createEntry(Object modelId) throws Exception {
		Long id = (Long)modelId;
		PredictData result = null;
		
		ILoadModelPredictData action = null;
		
		String impClass = SharedApplication.getInstance().getConfiguration().getProperty(ACTION_IMPLEMENTATION_CLASS);

		if (impClass == null) {
			action = new LoadModelPredictData();
		} else {
			getClass();
			action = loadForName(impClass);
		}
		
		action.setModelId(id);
		result = action.run();
		
		return result;
	}
	
	private ILoadModelPredictData loadForName(String name) throws Exception {
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		Class<?> _theClass;

		if ( name.contains("LoadModelPredictDataFromSerializationFile")) {
			return new LoadModelPredictDataFromSerializationFile();
		} else if (loader != null) {
			_theClass = Class.forName(name, false, loader);
		} else {
			_theClass = Class.forName(name);
		}
		return (ILoadModelPredictData) _theClass.newInstance();

	}

}
