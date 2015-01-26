package gov.usgswim.sparrow.action;

import java.util.ArrayList;

import gov.usgswim.sparrow.datatable.SparrowColumnSpecifier;

/**
 * Action will load a context, check all of its results to see if they are in the 
 * given bins.
 * 
 * Will return a true or false string value
 */
public class VerifyInclusiveBinning extends Action<Boolean> {
	String[] binHighList;
	String[] binLowList;
	ArrayList<Double[]> bins;
	SparrowColumnSpecifier data;
	
    public VerifyInclusiveBinning(SparrowColumnSpecifier data, String[] high, String[] low) throws Exception {
    	this.data = data;
    	this.binHighList = high;
    	this.binLowList = low;
    	
    	if(this.binHighList.length != this.binLowList.length) throw new Exception("Invalid bin specification");
    	
    	bins = new ArrayList<Double[]>();
    	
    	for(int i =0; i < binHighList.length; i ++) {
    		bins.add(new Double[]{Double.parseDouble(binLowList[i]), Double.parseDouble(binHighList[i])});
    	}
    }
    
	public Boolean doAction() throws Exception {
		
		//iterate through all values in the data to see if they are within range
		for (int r=0; r<data.getTable().getRowCount(); r++) {
			boolean thisResultInBounds = false;
			Double value = data.getTable().getDouble(r, data.getColumn());
			
			if (value != null && !Double.isNaN(value)) {
				for(Double[] bin : bins) {
					thisResultInBounds = thisResultInBounds || (value.compareTo(bin[0])>=0 && value.compareTo(bin[1])<=0);
				}
			}
			if(!thisResultInBounds) return Boolean.FALSE; //return false as soon as we find a single value that does not exist in a bin
		}
		
		return Boolean.TRUE;
	}
	
	@Override
	public Long getModelId() {
		if (data != null) {
			return data.getModelId();
		} else {
			return null;
		}
	}
}