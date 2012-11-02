package gov.usgs.cida.sparrow.validation.tests;

import gov.usgs.cida.sparrow.validation.framework.SparrowModelValidationBase;
import gov.usgs.cida.sparrow.validation.framework.TestResult;
import gov.usgs.cida.datatable.DataTable;
import gov.usgs.cida.sparrow.validation.framework.Comparator;
import gov.usgswim.sparrow.PredictData;
import gov.usgswim.sparrow.TopoData;
import gov.usgswim.sparrow.service.SharedApplication;

/**
 * Checks that the database model's FRAC values total to ONE at each fnode.
 * 
 * @author eeverman
 */
public class FracValuesShouldTotalToOne extends SparrowModelValidationBase {
	
	public boolean requiresDb() { return true; }
	public boolean requiresTextFile() { return false; }
	
	
	public FracValuesShouldTotalToOne(Comparator comparator, boolean failedTestIsOnlyAWarning) {
		super(comparator, failedTestIsOnlyAWarning);
	}
	
	public TestResult testModel(Long modelId) throws Exception {
		
		PredictData predictData = SharedApplication.getInstance().getPredictData(modelId);
		TopoData topo = predictData.getTopo();
		
		for (int row = 0; row < topo.getRowCount(); row++) {
			
			Long reachId = predictData.getIdForRow(row);
			Integer fnode = topo.getFromNode(row);
			Boolean isShoreReach = topo.isShoreReach(row);
			Boolean ifTran = topo.isIfTran(row);
			
			if (!isShoreReach) {
				//This is a regular reach
				
				//int[] allReachesAtFromFnode = topo.findAll(PredictData.TOPO_FNODE_COL, fnode);
				int[] allReachesAtFromFnode = topo.findFlowReachesLeavingSameNode(row);
				int[] upstreamRowsToNode = topo.findAllowedUpstreamReaches(row);
				
				double fracTotal = 0d;

				for (int sameNodeRow : allReachesAtFromFnode) {
					double thisFrac = topo.getFrac(sameNodeRow);
					fracTotal+= thisFrac;
				}
				
				if (upstreamRowsToNode.length == 0) {
					//Headwater reach - FRAC doesn't really matter
					
					
					double thisFrac = topo.getFrac(row);
					
					if (! comp(1d, thisFrac)) {
						this.recordRowWarn(modelId, reachId, row, "1", thisFrac, "1", "db frac", isShoreReach, ifTran, 
								"FRAC != 1 for HEADWATER REACH.  FNODE: " + fnode + " (This is unusual, but not really an error)");
					}
					
					if (allReachesAtFromFnode.length > 1) {
						this.recordRowWarn(modelId, reachId, row, "1", thisFrac, "1", "db frac", isShoreReach, ifTran, 
								"FRAC != 1 for HEADWATER REACH.  FNODE: " + fnode + " (This is unusual, but not really an error)");
					}
					
											//No reaches deliver load to the FNODE of this reach, so it is a headwater reach.
						//If there are multiple reaches leaving this from node, 
						//this could be the unusual case of a parted river, where there is
						//no upstream reach and two reaches each flow AWAY from a single
						//FNODE.
						//The FRAC should be 1 for any headwater reach (even if a parting),
						//though it doesn't really matter.
						
						
				} else {
					//Non-headwater reach
					
					if (! comp(1d, fracTotal)) {
					
						//The fractions leaving this node do not total to one...  Seems like a problem
						if (allReachesAtFromFnode.length == 1) {
							this.recordRowError(modelId, reachId, row, "1", fracTotal, "1", "db frac total", isShoreReach, ifTran, 
									"FRAC total != 1 for SINGLE REACH.  FNODE: " + fnode);
						} else {
							this.recordRowError(modelId, reachId, row, "1", fracTotal, "1", "db frac total", isShoreReach, ifTran, 
									"FRAC total != 1 for " + allReachesAtFromFnode.length + " reach diversion.  FNODE: " + fnode);
						}
					}


				}
			
			} else {
				//This is a shore reach - it doesn't really matter what the FRAC is,
				//but it probably should be one.
				double thisFrac = topo.getFrac(row);
				
				if (! comp(1d, thisFrac)) {
					this.recordRowWarn(modelId, reachId, row, "1", thisFrac, "1", "db frac total", isShoreReach, ifTran, 
							"FRAC total != 1 for SINGLE SHORE REACH.  FNODE: " + fnode + " (This is unusual, but not really an error)");
				}
			}
		}
		
		
		return result;
	}
	
	
}

