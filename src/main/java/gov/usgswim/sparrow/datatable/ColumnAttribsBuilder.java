package gov.usgswim.sparrow.datatable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * A builder for ColumnAttribs
 * 
 * @author eeverman
 */
public class ColumnAttribsBuilder implements ColumnAttribs {

	protected String description;
	protected String units;
	protected String name;
	protected Map<String, String> properties;


	public void setProperty(String key, String value) {
		if (properties == null && value != null) {
			properties = new HashMap<String, String>();
			properties.put(key, value);
		} else if (properties != null && value == null) {
			properties.remove(key);
			if (properties.size() == 0) {
				properties = null;
			}
		} else {
			properties.put(key, value);
		}
	}
	
	/**
	 * Forces the properties collection to be non-null and empty, erasing any
	 * existing property values.
	 * 
	 * In cases where a columns properties are being overlayed, null vs empty
	 * is the difference between inheriting the underlying properties (null)
	 * or having no properties inherited or other (empty).
	 */
	public void setPropertiesEmpty() {
		if (properties == null) {
			properties = new HashMap<String, String>();
		} else {
			properties.clear();
		}
	}
	
	/**
	 * Forces the properties collection to be null, which will erase all
	 * property values.
	 * 
	 * In cases where a columns properties are being overlayed, null vs empty
	 * is the difference between inheriting the underlying properties (null)
	 * or having no properties inherited or other (empty).
	 */
	public void setPropertiesNull() {
		properties = null;
	}
	
	/* (non-Javadoc)
	 * @see gov.usgswim.sparrow.datatable.ColumnAttribs#getProperty(java.lang.String)
	 */
	@Override
	public String getProperty(String key) {
		if (properties != null) {
			return properties.get(key);
		} else {
			return null;
		}
	}
	
	/* (non-Javadoc)
	 * @see gov.usgswim.sparrow.datatable.ColumnAttribs#getProperty(java.lang.String)
	 */
	@Override
	public String getProperty(String key, String defaultValue) {
		if (properties != null && properties.containsKey(key)) {
			return properties.get(key);
		} else {
			return defaultValue;
		}
	}
	
	/* (non-Javadoc)
	 * @see gov.usgswim.sparrow.datatable.ColumnAttribs#getPropertyNames()
	 */
	@Override
	public Set<String> getPropertyNames() {
		if (properties != null) {
			return properties.keySet();
		} else {
			Set<String> s = Collections.emptySet();
			return s;
		}
	}
	
	/* (non-Javadoc)
	 * @see gov.usgswim.sparrow.datatable.ColumnAttribs#isPropertiesNull()
	 */
	@Override
	public boolean isPropertiesNull() {
		return properties == null;
	}
	
	/* (non-Javadoc)
	 * @see gov.usgswim.sparrow.datatable.ColumnAttribs#isPropertiesEmpty()
	 */
	@Override
	public boolean isPropertiesEmpty() {
		return (properties == null || properties.isEmpty());
	}
	
	/* (non-Javadoc)
	 * @see gov.usgswim.sparrow.datatable.ColumnAttribs#isPropertiesEmptyAndNotNull()
	 */
	@Override
	public boolean isPropertiesEmptyAndNotNull() {
		return (properties != null && properties.isEmpty());
	}
	
	/**
	 * Returns the actual underlying property set or an empty one if null.
	 * @return
	 */
	@Override
	public Map<String, String> getProperties() {
		
		if (properties != null) {
			return properties;
		} else {
			Map<String, String> tmp = Collections.emptyMap();
			return tmp;
		}
	}

	public void setDescription(String desc) {
		this.description = desc;
	}

	/* (non-Javadoc)
	 * @see gov.usgswim.sparrow.datatable.ColumnAttribs#getDescription()
	 */
	@Override
	public String getDescription() {
		return description;
	}
	
	/* (non-Javadoc)
	 * @see gov.usgswim.sparrow.datatable.ColumnAttribs#getDescription()
	 */
	@Override
	public String getDescription(String defaultValue) {
		if (description != null) {
			return description;
		} else {
			return defaultValue;
		}
	}

	public void setUnits(String units) {
		this.units = units;
	}
	
	/* (non-Javadoc)
	 * @see gov.usgswim.sparrow.datatable.ColumnAttribs#getUnits()
	 */
	@Override
	public String getUnits() {
		return units;
	}
	
	/* (non-Javadoc)
	 * @see gov.usgswim.sparrow.datatable.ColumnAttribs#getUnits()
	 */
	@Override
	public String getUnits(String defaultValue) {
		if (units != null) {
			return units;
		} else {
			return defaultValue;
		}
	}

	public void setName(String name) {
		this.name = name;
	}

	/* (non-Javadoc)
	 * @see gov.usgswim.sparrow.datatable.ColumnAttribs#getName()
	 */
	@Override
	public String getName() {
		return name;
	}
	
	/* (non-Javadoc)
	 * @see gov.usgswim.sparrow.datatable.ColumnAttribs#getName()
	 */
	@Override
	public String getName(String defaultValue) {
		if (name != null) {
			return name;
		} else {
			return defaultValue;
		}
	}

	@Override
	public ColumnAttribs toImmutable() throws IllegalStateException {
		return new ColumnAttribsImm(description, units, name, properties);
	}
}
