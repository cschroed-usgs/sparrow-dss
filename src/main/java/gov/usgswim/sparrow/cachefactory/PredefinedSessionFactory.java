package gov.usgswim.sparrow.cachefactory;

import gov.usgswim.sparrow.action.LoadPredefinedSessions;
import gov.usgswim.sparrow.domain.IPredefinedSession;

import java.util.List;

import net.sf.ehcache.constructs.blocking.CacheEntryFactory;

/**
 * Wraps the LoadPredefinedSessions Action for cache usage
 * 
 * @author eeverman
 *
 */
public class PredefinedSessionFactory implements CacheEntryFactory {

	@Override
	public List<IPredefinedSession> createEntry(Object modelId) throws Exception {
		LoadPredefinedSessions action = new LoadPredefinedSessions((Long) modelId);
		List<IPredefinedSession> sessionList = action.run();

		sessionList = action.run();
		
		return sessionList;
	}

}
