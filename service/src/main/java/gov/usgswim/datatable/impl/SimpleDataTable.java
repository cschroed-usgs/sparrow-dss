package gov.usgswim.datatable.impl;

import gov.usgswim.Immutable;
import gov.usgswim.datatable.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Immutable
public class SimpleDataTable implements DataTable.Immutable {

	private static final long serialVersionUID = 1L;
	
	protected ColumnData[] columns;
	protected String description;
	protected String name;
	protected Map<String, String> properties = new HashMap<String, String>();
	protected boolean isValid;
	protected ColumnIndex index;
	
	// ===========
	// Constructors
	// ===========
	/**
	 * Simplest possible constructor w/ list of columns.
	 * 
	 * toImmutable() is called on each column before adding to the table.
	 * 
	 * @param columns
	 */
	public SimpleDataTable(List<ColumnData> columns) {
		this(columns.toArray(new ColumnData[]{}));
	}
	
	/**
	 * Simplest possible constructor w/ array of columns.
	 * 
	 * toImmutable() is called on each column before adding to the table.
	 * 
	 * @param columns
	 */
	public SimpleDataTable(ColumnData[] columns) {
		// convert the columns to immutable and add
		this.columns = new ColumnData[columns.length];

		for (int i=0; i<this.columns.length; i++) {
			this.columns[i] = columns[i].toImmutable();
		}

		index = new NoIdsColumnIndex(
				(this.columns.length > 0)?this.columns[0].getRowCount() : 0
		);
		
		// Check validity
		isValid = validateStructure();
	}
	
	public SimpleDataTable(DataTableWritable writable, List<ColumnDataWritable> columns, Map<String, String> properties, Map<Long, Integer> idIndex, List<Long> idColumn) {
		// convert the columns to immutable and add
		this.columns = new ColumnData[columns.size()];

		for (int i=0; i<this.columns.length; i++) {
			this.columns[i] = columns.get(i).toImmutable();
		}
		
		if (idColumn != null) {
			index = new HashMapColumnIndex(idColumn);
		} else {
			index = new NoIdsColumnIndex(
					(this.columns.length > 0)?this.columns[0].getRowCount() : 0
			);
		}
		
		// convert the metadata
		this.description = writable.getDescription();
		this.name = writable.getName();
		
		if (properties != null) {
			this.properties.putAll(properties);
		}
		
		// Check validity
		isValid = validateStructure();
	}
	
	/**
	 * Creates a new DataTable that is collaboratively immutable, so the caller
	 * should not hold a copy of the rowIds or the ColumnData.
	 * 
	 * @param columns
	 * @param name
	 * @param description
	 * @param properties
	 * @param rowIds An array of Long ID values where entry is the ID for the corresponding row
	 */
	public SimpleDataTable(ColumnData[] columns, String name, String description, Map<String, String> properties, long[] rowIds) {
		// convert the columns to immutable and add
		this.columns = columns;

		for (int i=0; i<this.columns.length; i++) {
			this.columns[i] = columns[i].toImmutable();
		}
		
		if (rowIds != null) {
			index = new HashMapColumnIndex(rowIds);
		} else {
			index = new NoIdsColumnIndex(
					(this.columns.length > 0)?this.columns[0].getRowCount() : 0
			);
		}
		
		
		
		// convert the metadata
		this.description = description;
		this.name = name;
		
		if (properties != null) {
			this.properties.putAll(properties);
		}
		
		// Check validity
		isValid = validateStructure();
	}
	
	/**
	 * Creates a new DataTable that is collaboratively immutable, so the caller
	 * should not hold a copy of the rowIds or the ColumnData.
	 * 
	 * @param columns
	 * @param name
	 * @param description
	 * @param properties
	 */
	public SimpleDataTable(ColumnData[] columns, String name, String description, Map<String, String> properties) {
		// convert the columns to immutable and add
		this.columns = columns;

		for (int i=0; i<this.columns.length; i++) {
			this.columns[i] = columns[i].toImmutable();
		}
		

		index = new NoIdsColumnIndex(
				(this.columns.length > 0)?this.columns[0].getRowCount() : 0
		);
		
		
		// convert the metadata
		this.description = description;
		this.name = name;
		
		if (properties != null) {
			this.properties.putAll(properties);
		}
		
		// Check validity
		isValid = validateStructure();
	}
	
	/**
	 * Creates a new DataTable that is collaboratively immutable, so the caller
	 * should not hold a copy of the rowIds or the ColumnData.
	 * 
	 * @param columns
	 * @param name
	 * @param description
	 * @param properties
	 * @param columnIndex An index for this table to use.  An immutable version of the index will be kept.
	 */
	public SimpleDataTable(ColumnData[] columns, String name, String description, Map<String, String> properties, ColumnIndex columnIndex) {
		// convert the columns to immutable and add
		this.columns = columns;

		for (int i=0; i<this.columns.length; i++) {
			this.columns[i] = columns[i].toImmutable();
		}
		
		if (columnIndex != null) {
			index = columnIndex.toImmutable();
		} else {
			index = new NoIdsColumnIndex(
					(this.columns.length > 0)?this.columns[0].getRowCount() : 0
			);
		}
		
		
		// convert the metadata
		this.description = description;
		this.name = name;
		
		if (properties != null) {
			this.properties.putAll(properties);
		}
		
		// Check validity
		isValid = validateStructure();
	}
	
	protected boolean validateStructure() {
		int rowCount = columns[0].getRowCount();
		boolean isAllColumnsSameSize = true;
		for (int i=1; i < columns.length; i++) {
			isAllColumnsSameSize &= (rowCount == columns[i].getRowCount());
		}
		
		if (rowCount > 0) {
			isAllColumnsSameSize &= (index.getMaxRowNumber() + 1) >= rowCount;
		}
		
		return isAllColumnsSameSize;
	}
	
	// =================================
	// Data Population Lifecycle Methods
	// =================================

	public boolean isValid() {
		return isValid;
	}
	
	public boolean isValid(int columnIndex) {
		return columns[columnIndex].isValid();
	}

	public DataTable.Immutable toImmutable() {
		return this;
	}
	
	@Override
	public ColumnData getColumn(int colIndex) {
		return columns[colIndex];
	}
	
	// =======================
	// Global Metadata Methods
	// =======================
	public Integer getColumnByName(String colName) {
		for (int i=0; i<columns.length; i++) {
			if (colName.equals(columns[i].getName())) {
				return i;
			}
		}
		return null;
	}
	
	public int getColumnCount() {
		return columns.length;
	}

	public String getDescription() {
		return description;
	}
	public String getName() {
		return name;
	}

	public String getProperty(String key) {
		return properties.get(key);
	}
	
	@Override
	public Map<String, String> getProperties() {
		HashMap<String, String> tmp = new HashMap<String, String>();
		tmp.putAll(properties);
		return tmp;
	}

	@Override
	public Map<String, String> getProperties(int col) {
		return columns[col].getProperties();
	}

	public int getRowCount() {
		int result = 0;
		for (ColumnData column: columns) {
			result = Math.max(result, column.getRowCount());
		}
		return result;
	}

	public Set<String> getPropertyNames() {
		return properties.keySet();
	}
	
	// ====================
	// Find & Index Methods
	// ====================
	public int[] findAll(int col, Object value) {
		return columns[col].findAll(value);
	}

	public int findFirst(int col, Object value) {
		return columns[col].findFirst(value);
	}

	public int findLast(int col, Object value) {
		return columns[col].findLast(value);
	}

	public Long getIdForRow(int row) {
		return index.getIdForRow(row);
	}

	public boolean isIndexed(int col) {
		return columns[col].isIndexed();
	}
	
	public int getRowForId(Long id) {
		return index.getRowForId(id);
	}


	public boolean hasRowIds() {
		return index.hasIds();
	}
	
	// =======================
	// Column Metadata Methods
	// =======================
	public Set<String> getPropertyNames(int col) {
		return columns[col].getPropertyNames();
	}

	public String getName(int col) {
		return columns[col].getName();
	}

	public String getProperty(int col, String key) {
		return columns[col].getProperty(key);
	}
	
	public String getUnits(int col) {
		return columns[col].getUnits();
	}

	public Class<?> getDataType(int col) {
		return columns[col].getDataType();
	}
	
	public String getDescription(int col) {
		return columns[col].getDescription();
	}

	// ===========================================
	// getXXX(int row, int col) cell value methods
	// ===========================================
	public Integer getInt(int row, int col) {
		ColumnData column = columns[col];
		return (column != null)? column.getInt(row): null;
	}
	public Float getFloat(int row, int col) {
		ColumnData column = columns[col];
		return (column != null)? column.getFloat(row): null;
	}

	public String getString(int row, int col) {
		ColumnData column = columns[col];
		return (column != null)? column.getString(row): null;
	}
	
	public Double getDouble(int row, int col) {
		ColumnData column = columns[col];
		return (column != null)? column.getDouble(row): null;
	}

	public Object getValue(int row, int col) {
		ColumnData column = columns[col];
		return (column != null)? column.getValue(row): null;
	}
	
	public Long getLong(int row, int col) {
		ColumnData column = columns[col];
		return (column != null)? column.getLong(row): null;
	}


	// ===============
	// MAX-MIN Methods
	// ===============
	public Double getMaxDouble(int col) {
		return columns[col].getMaxDouble();
	}

	public Double getMaxDouble() {
		return FindHelper.bruteForceFindMaxDouble(this);
	}

	public Integer getMaxInt(int col) {
		return columns[col].getMaxInt();
	}

	public Integer getMaxInt() {
		// this is important to keep as it's likely to require less conversion from native type
		Integer result = null;
		for (int col=0; col<columns.length; col++ ) {
			Integer max = getMaxInt(col);
			if (max != null) {
				result = (result == null)? max: Math.max(result, max);
			}
		}
		return result;
	}

	public Double getMinDouble(int col) {
		return columns[col].getMinDouble();
	}

	public Double getMinDouble() {
		return FindHelper.bruteForceFindMinDouble(this);
	}

	public Integer getMinInt(int col) {
		return columns[col].getMinInt();
	}

	public Integer getMinInt() {
		// this is important to keep as it's likely to require less conversion from native type
		Integer result = null;
		for (int col=0; col<columns.length; col++ ) {
			Integer min = getMinInt(col);
			if (min != null) {
				result = (result == null)? min: Math.min(result, min);
			}
		}
		return result;
	}


}
