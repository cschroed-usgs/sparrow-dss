package gov.usgswim.sparrow.datatable;

import gov.usgs.cida.datatable.ColumnData;
import gov.usgs.cida.datatable.DataTable;

/**
 * An inner class to bundle a DataTable and a column index together so that
 * it is possible to return these two together for methods returning the
 * data column.
 *
 * @author eeverman
 *
 */
public class SparrowColumnSpecifier {
	private final DataTable table;
	private final int column;
	private Integer contextId;
	private Long modelId;

	
	public SparrowColumnSpecifier(DataTable table, int column, Integer contextId, Long modelId) {
		this.table = table;
		this.column = column;
		this.contextId = contextId;
		this.modelId = modelId;
	}

	public DataTable getTable() {
		return table;
	}

	public int getColumn() {
		return column;
	}
	
	public ColumnData getColumnData() {
		return table.getColumn(column);
	}

	/**
	 * The PredictionContext ID this datacolumn is for.
	 * 
	 * It may be null in the case we are holding data that is model specific but
	 * not context specific, or for derived display data, such as aggregating by
	 * HUC for use outside of a prediction context.
	 * @return
	 */
	public Integer getContextId() {
		return contextId;
	}
	
	/**
	 * The SparrowModel ID this datacolumn is for.
	 * 
	 * @return
	 */
	public Long getModelId() {
		return modelId;
	}
	
	/**
	 * Shortcut to get a double value w/o digging to the table.
	 * @param row
	 * @return
	 */
	public Double getDouble(int row) {
		return table.getDouble(row, column);
	}
	
	/**
	 * Shortcut to determine if there are row IDs w/o digging to the table.
	 * @return
	 */
	public boolean hasRowIds() {
		return table.hasRowIds();
	}
	
	/**
	 * Shortcut to get a row ID w/o digging to the table.
	 * @param row
	 * @return
	 */
	public Long getIdForRow(int row) {
		return table.getIdForRow(row);
	}
	
	/**
	 * Shortcut to get the units of data column w/o digging to the table.
	 * @return
	 */
	public String getUnits() {
		return table.getUnits(column);
	}
	
	/**
	 * Returns the thing being measured.
	 * @return
	 */
	public String getConstituent() {
		return table.getProperty(column, TableProperties.CONSTITUENT.toString());
	}
	
	/**
	 * Shortcut to get the description of data column w/o digging to the table.
	 * @return
	 */
	public String getDescription() {
		return table.getDescription(column);
	}
	
	/**
	 * Shortcut to get the name of data column w/o digging to the table.
	 * @return
	 */
	public String getColumnName() {
		return table.getName(column);
	}
	
	/**
	 * Shortcut to get a property of data table w/o digging to the table.
	 * @param name
	 * @return
	 */
	public String getTableProperty(String name) {
		return table.getProperty(name);
	}
	
	/**
	 * Shortcut to the table rowCount.
	 * @return
	 */
	public int getRowCount() {
		return table.getRowCount();
	}
	
	/**
	 * Shortcut to the table columnCount.
	 * @return
	 */
	public int getColumnCount() {
		return table.getColumnCount();
	}

}