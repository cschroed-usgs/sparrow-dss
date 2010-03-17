package gov.usgswim.sparrow.cachefactory;

import gov.usgswim.Immutable;

import org.apache.commons.lang.builder.HashCodeBuilder;

@Immutable
public class ModelRequestCacheKey {
	
	private final boolean _public;
	private final boolean _approved;
	private final boolean _archived;
	private final boolean _sources;
	private final long _modelId;
	
	/**
	 * Constructs a CacheKey with defaults taken from
	 * the ModelRequest class
	 * @param isPublic true
	 * @param isApproved true
	 * @param isArchived false
	 * @param getSources false
	 */
	public ModelRequestCacheKey() {
		this(-1L, true, true, false, false);
	}
	
	public ModelRequestCacheKey(long modelId) {
		this(modelId, true, true, false, false);
	}
	
	public ModelRequestCacheKey(boolean isPublic, boolean isApproved, boolean isArchived, boolean getSources) {
		this(-1L, isPublic, isApproved, isArchived, getSources);
	}
	
	public ModelRequestCacheKey(long modelId, boolean isPublic, boolean isApproved, boolean isArchived, boolean getSources) {
		this._public = isPublic;
		this._approved = isApproved;
		this._archived = isArchived;
		this._sources = getSources;
		this._modelId = (modelId > 0) ? modelId : -1L;
	}
	
	public boolean isPublic() {
		return this._public;
	}

	public boolean isApproved() {
		return this._approved;
	}

	public boolean isArchived() {
		return this._archived;
	}

	public boolean isGetSources() {
		return this._sources;
	}

	public long getModelId() {
		return this._modelId;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ModelRequestCacheKey) {
			return obj.hashCode() == hashCode();
		}
		return false;
	}

	@Override
	public int hashCode() {
		HashCodeBuilder result = new HashCodeBuilder();
		
		result.append(isPublic());
		result.append(isApproved());
		result.append(isArchived());
		result.append(isGetSources());
		result.append(getModelId());
		
		return result.toHashCode();
	}
}
