package gov.usgswim.sparrow.cachefactory;

import gov.usgswim.datatable.DataTable;
import gov.usgswim.sparrow.parser.PredictionContext;
import gov.usgswim.sparrow.service.SharedApplication;

import java.util.Arrays;

import net.sf.ehcache.constructs.blocking.CacheEntryFactory;

import org.apache.log4j.Logger;

/**
 * This factory class creates a binning array based on a request from EHCache.
 * 
 * Binning is the process of creating bins for a set of data.  For instance,
 * this data:<br>
 * <code>1, 2, 2, 9, 20, 29</code><br>
 * could be broken into two bins containing three values each based on Equal Count as:
 * <li>bin 1:  1 to 2 (inclusive)
 * <li>bin 2:  2 to 29
 * Equal Range binning for two bins would result in:
 * <li>bin 1:  1 to 15
 * <li>bin 2:  15 to 29
 * 
 * This class implements CacheEntryFactory, which plugs into the caching system
 * so that the createEntry() method is only called when a entry needs to be
 * created/loaded.
 * 
 * Caching, blocking, and de-caching are all handled by the caching system, so
 * that this factory class only needs to worry about building a new entity in
 * (what it can consider) a single thread environment.
 * 
 * @author eeverman
 */
public class BinningFactory implements CacheEntryFactory {
	protected static Logger log =
		Logger.getLogger(BinningFactory.class); //logging for this class
	
	public Object createEntry(Object binningRequest) throws Exception {
		BinningRequest request = (BinningRequest)binningRequest;
		PredictionContext context = SharedApplication.getInstance().getPredictionContext(request.getContextID());
		
		if (context == null) {
			throw new Exception("No context found for context-id '" + request.getContextID() + "'");
		}

		PredictionContext.DataColumn dc = context.getDataColumn();
		
		double[] bins = null;
		
		// Determine type of binning to perform, calling the appropriate method
		if (request.getBinType() == BinningRequest.BIN_TYPE.EQUAL_COUNT) {
		    bins = getEqualCountBins(dc.getTable(), dc.getColumn(), request.getBinCount());
		} else if (request.getBinType() == BinningRequest.BIN_TYPE.EQUAL_RANGE) {
		    bins = getEqualRangeBins(dc.getTable(), dc.getColumn(), request.getBinCount());
		}
		
		return bins;
	}
	
	
	/**
	 * Returns an equal count set of bins so that the bins define break-point
	 * boundaries with approximately an equal number of values in each bin.
	 * 
     * @param data Table of data containing the column to divide into bins.
     * @param columnIndex Index of the column to divide into bins.
     * @param binCount Number of bins to divide the column into.
     * @return Set of bins such that the bins define break-point boundaries
     *         with an approximately equal number of values contained within.
	 */
	protected double[] getEqualCountBins(DataTable data, int columnIndex, int binCount) {

		int totalRows = data.getRowCount();	//Total rows of data
		
		//Number of rows 'contained' in each bin.  This likely will not come out even,
		//so use a double to preserve the fractional rows.
		double binSize = (double)(totalRows) / (double)(binCount);	
		
		float[] values = new float[totalRows];	//Array holding all values
		//The bins, where each value is a fence post w/ values between, thus, there is one more 'post' than bins.
		//The first value is the lowest value in values[], the last value is the largest value.
		double[] bins = new double[binCount + 1];	
		
		//Export all values in the specified column to values[] so they can be sorted
		for (int r=0; r<totalRows; r++) {
			values[r] = data.getFloat(r, columnIndex);
		}
		
		Arrays.sort(values);
		
		//Assign first and last values for the bins (min and max)
		bins[0] = values[0];
		bins[binCount] = values[totalRows - 1];
		
		//Assign the middle breaks so that equal numbers of values fall into each bin
		for (int i=1; i<(binCount); i++) {
			
			//Get the row containing the nearest integer split
			int split = (int) ((double)i * binSize);
			
			//The bin boundary is the value contained at that row.
			bins[i] = (double) values[split];
		}
		
		return bins;
	}
	
    /**
     * Returns an equal range set of bins such that the bins define break-point
     * boundaries whose values are approximately equally spaced apart.
     *
     * @param data Table of data containing the column to divide into bins.
     * @param columnIndex Index of the column to divide into bins.
     * @param binCount Number of bins to divide the column into.
     * @return Set of bins such that the bins define break-point boundaries
     *         whose values are approximately equally spaced apart.
     */
    protected double[] getEqualRangeBins(DataTable data, int columnIndex, int binCount) {
        int totalRows = data.getRowCount(); // Total rows of data

        // Grab the min and max values from the datatable
        double minValue = Double.MAX_VALUE;
        double maxValue = Double.MIN_VALUE;
        for (int r = 0; r < totalRows; r++) {
            float value = data.getFloat(r, columnIndex);
            minValue = Math.min(value, minValue);
            maxValue = Math.max(value, maxValue);
        }

        // Size of the range of values that will be defined by each bin
        double binRangeSize = (maxValue - minValue) / (double)(binCount);

        // The bins, where each value is a fence post with values between, thus
        // there is one more post than bins.  The first value is the minimum,
        // the last value is the maximum.
        double[] bins = new double[binCount + 1];
        bins[0] = minValue;
        bins[binCount] = maxValue;

        // Assign the breakpoints so that an equal range of values fall into each bin
        for (int i = 1; i < binCount; i++) {
            bins[i] = minValue + ((double)i * binRangeSize);
        }

        return bins;
    }
}
