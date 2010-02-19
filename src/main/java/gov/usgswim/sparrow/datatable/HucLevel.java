package gov.usgswim.sparrow.datatable;


public enum HucLevel implements NamedEnum<HucLevel> {
	HUC_NONE(null, "none", "Not Defined"),
	HUC_REACH(null, "reach", "No huc leve - individual reaches"),
	HUC_2(2, "huc_2", "HUC Level 2"),
	HUC_4(4, "huc_4", "HUC Level 4"),
	HUC_6(6, "huc_6", "HUC Level 6"),
	HUC_8(8, "huc_8", "HUC Level 8");
	
	private Integer level;
	private String name;
	private String description;
	
	HucLevel(Integer level, String name, String description) {
		this.level = level;
		this.name = name;
		this.description = description;
	}

	public Integer getLevel() {
		return level;
	}
	
	public String getName() {
		return name;
	}

	@Override
	public HucLevel fromString(String name) {
		for (HucLevel val : values()) {
			if (val.name.equals(name)) {
				return val;
			}
		}
		return null;
	}
	
	@Override
	public HucLevel fromStringIgnoreCase(String name) {
		for (HucLevel val : values()) {
			if (val.name.equalsIgnoreCase(name)) {
				return val;
			}
		}
		return null;
	}

	@Override
	public HucLevel getDefault() {
		return HUC_NONE;
	}
	
	@Override
	public String getDescription() {
		return description;
	}


}
