package gov.usgswim.sparrow;

import static gov.usgswim.sparrow.PredictData.FNODE_COL;
import static gov.usgswim.sparrow.PredictData.IFTRAN_COL;
import static gov.usgswim.sparrow.PredictData.INSTREAM_DECAY_COL;
import static gov.usgswim.sparrow.PredictData.TNODE_COL;
import static gov.usgswim.sparrow.PredictData.UPSTREAM_DECAY_COL;

import java.util.Set;

import gov.usgswim.datatable.DataTable;
import gov.usgswim.sparrow.datatable.PredictResultImm;
import gov.usgswim.sparrow.navigation.NavigationUtils;

public class DeliveryRunner implements Runner {
	/**
	 * The parent of all child values. If not passed in, it is created.
	 */
	protected PredictData predictData;

	/**
	 * Invariant topographic info about each reach.
	 * i = reach index [i][0] from node index [i][1] to node index [i][2] 'if
	 * transmit' is 1 if the reach transmits to its to-node
	 * 
	 * NOTE: We assume that the node indexes start at zero and have no skips.
	 * Thus, nodeCount must equal the largest node index + 1
	 * @see gov.usgswim.ImmutableBuilder.PredictData#getTopo()
	 * 
	 */
	protected DataTable topo;

	/**
	 * The coef's for each reach-source. coef[i][k] == the coefficient for
	 * source k at reach i
	 */
	protected DataTable deliveryCoefficient;

	/**
	 * The source amount for each reach-source. src[i][k] == the amount added
	 * via source k at reach i
	 */
	protected DataTable sourceValues;

	/**
	 * The stream and resevor decay. The values in the array are *actually*
	 * delivery, which is (1 - decay). I.E. the delivery calculation is already
	 * done.
	 * 
	 * src[i][0] == the instream decay at reach i. This decay is assumed to be
	 * at mid-reach and already computed as such. That is, it would normally be
	 * the sqr root of the instream decay, and it is assumed that this value
	 * already has the square root taken. src[i][1] == the upstream decay at
	 * reach i. This decay is applied to the load coming from the upstream node.
	 */
	protected DataTable decayCoefficient;
	/**
	 * The number of nodes
	 */
	protected int nodeCount;

	protected Set<Long> targetReaches;
	
	
	// ============
	// CONSTRUCTORS
	// ============
	public DeliveryRunner(DataTable topo, DataTable coef, DataTable src,
			DataTable decay, Set<Long> targetReaches) {
		this(new PredictDataImm(topo, coef, src, null, decay,
				null, null), targetReaches);
	}
	
	public DeliveryRunner(PredictData data, Set<Long> targetReaches) {
		this.topo = data.getTopo(); // assign the passed values to the class
									// variables
		this.deliveryCoefficient = data.getCoef();
		this.sourceValues = data.getSrc();
		this.decayCoefficient = data.getDecay();
		this.targetReaches = targetReaches;

		int maxNode = Math.max(topo.getMaxInt(FNODE_COL), topo
				.getMaxInt(TNODE_COL));

		this.predictData = data;
		nodeCount = maxNode + 1;
	}
	
	public void calcualteDeliveryCoefficients() {
		int maxReachRow = NavigationUtils.findMaxReachRow(targetReaches, topo);
		PredictResultStructure prs = PredictResultStructure.analyzePredictResultStructure(maxReachRow, sourceValues);
		
		// Reaches to store coefficients, one for each source type
		double incReachContribution[][] = new double[prs.reachCount][prs.rchValColCount];

		// Nodes to store coefficients, one for each source type
		double upstreamNodeContribution[][] = new double[nodeCount][prs.sourceCount];
	}
	
	public PredictResultImm doPredict() throws Exception {
		int maxReachRow = NavigationUtils.findMaxReachRow(targetReaches, topo);
		
		PredictResultStructure prs = PredictResultStructure.analyzePredictResultStructure(maxReachRow, sourceValues);

		double incReachContribution[][] = new double[prs.reachCount][prs.rchValColCount];

		/*
		 * Array of accumulated values at nodes
		 */
		double upstreamNodeContribution[][] = new double[nodeCount][prs.sourceCount];

		// Iterate over all reaches
		for (int reach = 0; reach < prs.reachCount; reach++) {

			double reachIncrementalContributionAllSourcesTotal = 0d; // incremental for all sources/ (NOT decayed)
			double rchGrandTotal = 0d; // all sources + all from upstream node (decayed)

			// Iterate over all sources
			for (int sourceType = 0; sourceType < prs.sourceCount; sourceType++) {
				int source = sourceType + prs.sourceCount;

				// temp var to store the incremental per source k.
				// Land delivery and coeff both included in coef value. (NOT
				// decayed)
				double incrementalReachContribution = deliveryCoefficient
						.getDouble(reach, sourceType)
						* sourceValues.getDouble(reach, sourceType);

				incReachContribution[reach][sourceType] = incrementalReachContribution;

				// total at reach (w/ up stream contrib) per source k (Decayed)
				incReachContribution[reach][source] = 
					(incrementalReachContribution * decayCoefficient.getDouble(reach, INSTREAM_DECAY_COL)) /* Just the decayed source */
						+ (upstreamNodeContribution[topo.getInt(reach, FNODE_COL)][sourceType] * decayCoefficient.getDouble(reach, UPSTREAM_DECAY_COL)); /* Just the decayed upstream portion */

				// Accumulate at downstream node if this reach transmits
				if (topo.getInt(reach, IFTRAN_COL) != 0) {
					upstreamNodeContribution[topo.getInt(reach, TNODE_COL)][sourceType] += incReachContribution[reach][source];
				}

				reachIncrementalContributionAllSourcesTotal += incrementalReachContribution; // add to incremental total for all sources at reach
				rchGrandTotal += incReachContribution[reach][source]; // add to grand total for all sources (w/upsteam) at reach
			}

			incReachContribution[reach][prs.totalIncrementalColOffset] = reachIncrementalContributionAllSourcesTotal; // incremental for all sources (NOT decayed)
			incReachContribution[reach][prs.grandTotalColOffset] = rchGrandTotal; // all sources + all from upstream node (Decayed)

		}

		return PredictResultImm.buildPredictResult(incReachContribution,
				predictData);
	}
}
