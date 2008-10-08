package gov.usgswim.sparrow.datatable;

import static gov.usgswim.sparrow.service.predict.AggregateType.sum;
import static gov.usgswim.sparrow.service.predict.ValueType.incremental;
import static gov.usgswim.sparrow.service.predict.ValueType.total;
import gov.usgs.webservices.framework.utils.TemporaryHelper;
import gov.usgswim.Immutable;
import gov.usgswim.datatable.ColumnData;
import gov.usgswim.datatable.DataTable;
import gov.usgswim.datatable.impl.SimpleDataTable;
import gov.usgswim.sparrow.PredictData;

import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

/**
 * An immutable implementation of PredictResult. This is simply an Immutable
 * DataTable with convenience getXXX methods appropriate to the content
 * 
 * This will likely be the only implementation unless the PredictRunner code is
 * modified to use a builder instead of arrays.
 * 
 * @author eeverman
 */
@Immutable
public class PredictResultImm extends SimpleDataTable implements PredictResult {
	
	/**
	 * A mapping from a source Identifier to the column number of the column
	 * containing the Incremental value for that source.
	 */
	private final Map<Long, Integer> srcIdIncMap;
	
	/**
	 * A mapping from a source Identifier to the column number of the column
	 * containing the Total value for that source.
	 */
	private final Map<Long, Integer> srcIdTotalMap;

	/**
	 * Index of the total Incremental column.
	 */
	private final int totalIncCol;
	
	/**
	 * Index of the total Total column.
	 */
	private final int totalTotalCol;
	
	/**
	 * The number of sources.
	 */
	private final int sourceCount;
	
	
	public PredictResultImm(ColumnData[] columns, long[] rowIds, 
				Map<Long, Integer> srcIdIncMap, Map<Long, Integer> srcIdTotalMap,
				int totalIncCol, int totalTotalCol) {
		
		super(columns, "Prediction Data", "Prediction Result Data", Collections.<String, String>emptyMap(), rowIds);
		

		{
			Hashtable<Long, Integer> map = new Hashtable<Long, Integer>(srcIdIncMap.size() * 2 + 1);
			map.putAll(srcIdIncMap);
			this.srcIdIncMap = map;
		}

		{
			Hashtable<Long, Integer> map = new Hashtable<Long, Integer>(srcIdTotalMap.size() * 2 + 1);
			map.putAll(srcIdTotalMap);
			this.srcIdTotalMap = map;
		}
		
		this.totalIncCol = totalIncCol;
		this.totalTotalCol = totalTotalCol;
		this.sourceCount = srcIdIncMap.size();
	}

    /**
     * Adds appropriate metadata to the raw 2x2 array and returns the result as a DataTable
     * 
     * @param data
     * @param predictData
     * @return
     * @throws Exception
     */
    public static PredictResultImm buildPredictResult(double[][] data, PredictData predictData) throws Exception {
        return buildPredictResult(data, predictData, null);
    }
    
    public static PredictResultImm buildPredictResult(double[][] data, PredictData predictData, long[] ids)
    throws Exception {
        ColumnData[] columns = new ColumnData[data[0].length];
        int sourceCount = predictData.getSrc().getColumnCount();

        //Same definition as the instance vars
        // Lookup table for key=source_id, value=array index of source contribution data
        Map<Long, Integer> srcIdIncMap = new Hashtable<Long, Integer>(13, 2);
        Map<Long, Integer> srcIdTotalMap = new Hashtable<Long, Integer>(13, 2);

        // Get the metadata to be attached to the column definitions
        DataTable sourceMetadata = predictData.getSrcMetadata();
        Integer displayNameCol = sourceMetadata.getColumnByName("DISPLAY_NAME");
        Integer constituentCol = sourceMetadata.getColumnByName("CONSTITUENT");
        Integer unitsCol = sourceMetadata.getColumnByName("UNITS");
        Integer precisionCol = sourceMetadata.getColumnByName("PRECISION");
		
        // ------------------------------------------
        // Define the source columns of the DataTable
        // ------------------------------------------
        for (int srcIndex = 0; srcIndex < sourceCount; srcIndex++) {

            // Pull out the metadata for the source
            String displayName = sourceMetadata.getString(srcIndex, displayNameCol);
            String constituent = sourceMetadata.getString(srcIndex, constituentCol);
            String units = sourceMetadata.getString(srcIndex, unitsCol);
            Long precision = sourceMetadata.getLong(srcIndex, precisionCol);

            //
            int srcIncAddIndex = srcIndex; // index for iterating through the incremental source contributions
            int srcTotalIndex = srcIndex + sourceCount; // index for iterating through the total source contributions

            srcIdIncMap.put(predictData.getSourceIdForSourceIndex(srcIndex), srcIncAddIndex); 
            srcIdTotalMap.put(predictData.getSourceIdForSourceIndex(srcIndex), srcTotalIndex); 

            // Map of metadata values for inc-add column
            Map<String, String> incProps = new HashMap<String, String>();
            incProps.put(VALUE_TYPE_PROP, incremental.name());
            incProps.put(CONSTITUENT_PROP, constituent);
            incProps.put(PRECISION_PROP, Long.toString(precision));

            // Map of metadata values for total column
            Map<String, String> totProps = new HashMap<String, String>();
            totProps.put(VALUE_TYPE_PROP, total.name());
            totProps.put(CONSTITUENT_PROP, constituent);
            totProps.put(PRECISION_PROP, Long.toString(precision));

            columns[srcIncAddIndex] = new ImmutableDoubleColumn(data, srcIncAddIndex, displayName + " Inc. Addition", units, "description", incProps);
            columns[srcTotalIndex] = new ImmutableDoubleColumn(data, srcTotalIndex, displayName + " Total (w/ upstream, decayed)", units, "description", totProps);
        }

        // ------------------------------------------
        // Define the total columns of the DataTable
        // ------------------------------------------
        int totalIncCol = 2 * sourceCount;	//The total inc col comes right after the two sets of source columns
        Map<String, String> totalIncProps = new HashMap<String, String>();
        totalIncProps.put(VALUE_TYPE_PROP, incremental.name());
        totalIncProps.put(AGGREGATE_TYPE_PROP, sum.name());

        int totalTotalCol = totalIncCol + 1; //The grand total col comes right after the total incremental col
        Map<String, String> grandTotalProps = new HashMap<String, String>();
        grandTotalProps.put(VALUE_TYPE_PROP, total.name());
        grandTotalProps.put(AGGREGATE_TYPE_PROP, sum.name());

        columns[totalIncCol] = new ImmutableDoubleColumn(data, totalIncCol, "Total Inc. (not decayed)", "units", "description", totalIncProps);
        columns[totalTotalCol] = new ImmutableDoubleColumn(data, totalTotalCol, "Grand Total (measurable)", "units", "description", grandTotalProps);

        // only get the ids if available
        if (ids == null) {
            ids = (predictData.getSys() != null) ? TemporaryHelper.getRowIds(predictData.getSys()) : null;
        }

        return new PredictResultImm(columns, ids, srcIdIncMap, srcIdTotalMap, totalIncCol, totalTotalCol);
    }

    public int getSourceCount() {
        return sourceCount;
    }

    public Double getIncremental(int row) {
        return getDouble(row, totalIncCol);
    }

    public int getIncrementalCol() {
        return totalIncCol;
    }

    public Double getTotal(int row) {
        return getDouble(row, totalTotalCol);
    }

    public int getTotalCol() {
        return totalTotalCol;
    }

    public int getIncrementalColForSrc(Long srcId) {
        return srcIdIncMap.get(srcId);
    }

    public Double getIncrementalForSrc(int row, Long srcId) {
        return getDouble(row, srcIdIncMap.get(srcId));
    }

    public int getTotalColForSrc(Long srcId) {
        return srcIdTotalMap.get(srcId);
    }

    public Double getTotalForSrc(int row, Long srcId) {
        return getDouble(row, srcIdTotalMap.get(srcId));
    }

}
