package gov.usgswim.sparrow.request;

import gov.usgswim.Immutable;
import gov.usgswim.sparrow.domain.PredefinedSessionTopic;
import gov.usgswim.sparrow.domain.PredefinedSessionType;

import java.io.Serializable;

import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * A request for PredefinedSession's.
 * @author eeverman
 *
 */
@Immutable
public class PredefinedSessionRequest implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private final Long modelId;
	private final Boolean approved;
	private final PredefinedSessionType predefinedSessionType;
	private final String groupName;
	private final PredefinedSessionTopic topic;
	
	public PredefinedSessionRequest(Long modelId, Boolean approved, PredefinedSessionType predefinedSessionType) {
		
		this.modelId = modelId;
		this.approved = approved;
		this.predefinedSessionType = predefinedSessionType;
		this.groupName = null;
		this.topic = null;
	}
	
	public PredefinedSessionRequest(Long modelId, Boolean approved,
			PredefinedSessionType predefinedSessionType, String groupName, PredefinedSessionTopic topic) {
		
		this.modelId = modelId;
		this.approved = approved;
		this.predefinedSessionType = predefinedSessionType;
		this.groupName = groupName;
		this.topic = topic;
	}
	

	
	/**
	 * @return the modelId
	 */
	public Long getModelId() {
		return modelId;
	}
	/**
	 * @return the approved
	 */
	public Boolean getApproved() {
		return approved;
	}
	/**
	 * @return the predefinedSessionType
	 */
	public PredefinedSessionType getPredefinedSessionType() {
		return predefinedSessionType;
	}
	/**
	 * @return the groupName
	 */
	public String getGroupName() {
		return groupName;
	}
	
	/**
	 * @return the topic
	 */
	public PredefinedSessionTopic getTopic() {
		return topic;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof PredefinedSessionRequest) {
			return obj.hashCode() == hashCode();
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		HashCodeBuilder hash = new HashCodeBuilder(197, 1343);
		hash.append(modelId);
		hash.append(approved);
		hash.append(predefinedSessionType);
		hash.append(groupName);
		hash.append(topic);
		return hash.toHashCode();
	}



}
