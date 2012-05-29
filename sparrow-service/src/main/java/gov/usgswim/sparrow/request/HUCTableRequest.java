package gov.usgswim.sparrow.request;

import java.io.Serializable;

import gov.usgswim.Immutable;
import gov.usgswim.sparrow.domain.HucAggregationLevel;

import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * A request for a HUC8 table of data.
 * 
 * The request indicates which model.
 * 
 * @author thongsav
 * 
 */
@Immutable
public class HUCTableRequest implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private final long modelID;

	public HUCTableRequest(long modelID) {
		this.modelID = modelID;
	}

	public long getModelID() {
		return modelID;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof HUCTableRequest) {
			return obj.hashCode() == hashCode();
		}
		return false;
	}
	
	@Override
	public synchronized int hashCode() {
		int hash = new HashCodeBuilder(2459, 141).
		append(modelID).
		append(this.getClass()).
		toHashCode();
		return hash;
	}

}
