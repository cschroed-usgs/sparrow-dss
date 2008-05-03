package gov.usgswim.task;


import gov.usgswim.sparrow.service.predict.PredictDatasetComputable;

import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import org.apache.log4j.Logger;

/**
 * A cache that takes Computable units of work, runs them, and stores the results.
 * 
 * The cache is thread safe such that if one Computable is actively processing
 * an identical request, the added computable will block until the other completes
 * and will return the completed result once it is available.
 * 
 * Concurrency utility classes from Java 1.5 are used.
 * 
 * This code was take from _Java Concurrency in Practice_ by Brian Goetz
 * Addison-Wesley, 2006.
 */
public class ComputableCache<A, V> implements Computable<A, V> {
	// TODO Make the concurrent map a map of weak references. Obsolete with ehcache
	private final ConcurrentMap<A, Future<V>> cache = new ConcurrentHashMap<A, Future<V>>();
	private final Computable<A, V> c;
	private final String cacheName;
	protected final static Logger log = Logger.getLogger(ComputableCache.class);

	//TODO  Need to make this take a max number of items argument and a FIFO usage
	//buffer to determine which (oldest) item to remove
	public ComputableCache(Computable<A, V> c, String name) {
		this.c = c;
		this.cacheName = name;
	}

	public V compute(final A arg) throws Exception {
		while (true) {
			log.info(cacheName + ": " + arg.toString());
			Future<V> f = cache.get(arg);
			if (f == null) {
			
				Callable<V> eval = new Callable<V>() {
						public V call() throws Exception {
							return c.compute(arg);
						}
					};
				
				FutureTask<V> ft = new FutureTask<V>(eval);
				f = cache.putIfAbsent(arg, ft);
				if (log.isInfoEnabled()) {
					log.info(cacheName + ": " + "putIfAbsent (" + arg.toString() + "," + ft.toString());				
				}
				if (f == null) {
					//It was not already in the cache
					if (log.isInfoEnabled()) {
						log.info(cacheName + ": " + "Absent, running: " + ft.toString());			
					}

					f = ft;
					ft.run();
				} /* else {
					//Added by Eric Everman
					//If a non-null value is returned, it means that our requested
					//computation was added while this thread was setting up for the run.
					//recall this method, which will block until the result is ready.
					compute(arg);
					
					//not needed b/c the result is returned from f.get() below.
					
				} */
			}
			
			try {
				Object result = f.get();
				log.info("returned cache object of " + result.getClass());
				return f.get();	//This will block until the result is available
			} catch (CancellationException e) {
				//Cancelled the request
				cache.remove(arg, f);
				
				throw e;	//pass it on...
			} catch (InterruptedException e) {
				//For some reason, the call was interupted
				cache.remove(arg, f);
				
				throw e;	//pass it on...
			} catch (ExecutionException e) {
				//An error occured during the execution of the request.
				//Not sure what to do here: if its a data error in the request, it will
				//probably recur if it's tried again.  Otherwise, it could be a
				//db connection problem or something...
				cache.remove(arg, f);
				
				throw new Exception("The process threw an exception during its execution", e.getCause());
			}
		}
	}
	
	/**
	 * Removes the task from the Cache.
	 * 
	 * If the task is not yet completed, it will continue to run until it completes
	 * and will notify its caller when complete, it will just no longer be cached.
	 * 
	 * @param arg
	 * @return
	 */
	public boolean remove(final A arg) {
		Future<V> f = cache.remove(arg);
		return (f != null);
	}
	
	/**
	 * Removes all items from the cache.
	 */
	public void clear() {
		cache.clear();
	}
	
	/**
	 * Returns a snapshot of the number of items in the cache.
	 * 
	 * The returned size is just an estimate, since the size continually changes.
	 * @return
	 */
	public int size() {
		return cache.size();
	}
}
