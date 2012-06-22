package gov.usgswim.sparrow.monitor;

import java.lang.ref.WeakReference;

/**
 *
 * @author eeverman
 */
public class CacheInvocation extends Invocation {
	private volatile Boolean hit;
	
	
	public CacheInvocation(Enum target, Object request, String requestAsString) {
		super(target, request, requestAsString);
	}
	
	public CacheInvocation(Class target, Object request) {
		super(target, request, null);
	}
	
	public String getName() { return "cache"; }

	public Boolean isCacheHit() {
		return hit;
	}

	public void setCacheHit(Boolean hit) {
		this.hit = hit;
	}
	
}
