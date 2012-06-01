package gov.usgswim.sparrow.domain;

import gov.usgswim.sparrow.datatable.NamedEnum;


public enum AggregationLevel implements NamedEnum<AggregationLevel> {
	NONE("NA", "Not Defined", false, false, false),
	REACH("reach", "No aggregation - individual reaches", false, false, true),
	STATE("state", "Aggregate all reaches in a State", false, true, false),
	HUC2(HucLevel.HUC2),
	HUC4(HucLevel.HUC4),
	HUC6(HucLevel.HUC6),
	HUC8(HucLevel.HUC8);
	
	private final String name;
	private final String description;

	private final HucLevel hucLevel;
	private final boolean _isHuc;
	private final boolean _isPolitical;
	private final boolean _isReach;
	
	AggregationLevel(String name, String description, boolean isAHuc, boolean isAPoliticalRegion, boolean isAReach) {
		this.hucLevel = null;
		this.name = name;
		this.description = description;
		
		//Classifiers
		this._isHuc = isAHuc;
		this._isPolitical = isAPoliticalRegion;
		this._isReach = isAReach;
	}
	
	AggregationLevel(HucLevel hucLevel) {
		this.hucLevel = hucLevel;
		this.name = hucLevel.getName();
		this.description = hucLevel.getDescription();
		
		//Classifiers
		this._isHuc = true;
		this._isPolitical = false;
		this._isReach = false;
	}

	public HucLevel getHucLevel() {
		return hucLevel;
	}
	
	/**
	 * Returns true if the passed name matches the name of this instance.
	 * 
	 * This method is case insensitive.
	 * @param name
	 * @return 
	 */
	public boolean nameEquals(String name) {
		AggregationLevel al = fromStringIgnoreCase(name);
		return al != null && this.equals(al);
	}
	
	public String getName() {
		return name;
	}

	@Override
	public AggregationLevel fromString(String name) {
		for (AggregationLevel val : values()) {
			if (val.name.equals(name)) {
				return val;
			}
		}
		return null;
	}
	
	@Override
	public AggregationLevel fromStringIgnoreCase(String name) {
		if (name == null) return null;
		
		for (AggregationLevel val : values()) {
			if (val.name.equalsIgnoreCase(name)) {
				return val;
			}
		}
		return null;
	}

	@Override
	public AggregationLevel getDefault() {
		return NONE;
	}
	
	@Override
	public String getDescription() {
		return description;
	}
	
	public boolean isHuc() {
		return _isHuc;
	}

	public boolean isPolitical() {
		return _isPolitical;
	}

	public boolean isReach() {
		return _isReach;
	}



}
