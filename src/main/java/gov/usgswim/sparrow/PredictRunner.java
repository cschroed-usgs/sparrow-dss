package gov.usgswim.sparrow;

import static gov.usgswim.sparrow.PredictData.FNODE_COL;
import static gov.usgswim.sparrow.PredictData.IFTRAN_COL;
import static gov.usgswim.sparrow.PredictData.INSTREAM_DECAY_COL;
import static gov.usgswim.sparrow.PredictData.TNODE_COL;
import static gov.usgswim.sparrow.PredictData.UPSTREAM_DECAY_COL;
import gov.usgswim.datatable.DataTable;
import gov.usgswim.sparrow.datatable.PredictResultImm;

/**
 * A simple SPARROW prediction implementation.
 *
 * Note: It is assumed that the reach order in the topo, coef, and src arrays
 * all match, and that the reach order is such that reach(n) never flows to
 * reach(<n).
 */
public class PredictRunner implements Runner {
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
	 * The stream and reservoir decay. The values in the array are *actually*
	 * delivery, which is (1 - decay). I.E. the delivery calculation is already
	 * done.
	 *
	 * src[i][0] == the instream decay at reach i. This decay is assumed to be
	 * at mid-reach and already computed as such. That is, it would normally be
	 * the sqrt root of the instream decay, and it is assumed that this value
	 * already has the square root taken. src[i][1] == the upstream decay at
	 * reach i. This decay is applied to the load coming from the upstream node.
	 */
	protected DataTable decayCoefficient;

	/**
	 * The number of nodes
	 */
	protected int nodeCount;

	 /**
	 * Construct a new instance using a PredictionDataSet.
	 *
	 * @param data An all-in-one data object
	 *
	 */
	public PredictRunner(PredictData data) {
		{// assign the passed values to the class variables
			this.topo = data.getTopo();
			this.deliveryCoefficient = data.getCoef();
			this.sourceValues = data.getSrc();
			this.decayCoefficient = data.getDecay();
		}


		int maxNode = Math.max(topo.getMaxInt(FNODE_COL), topo
				.getMaxInt(TNODE_COL));

		{ // IK: Efficiency checks disabled for now as they cause failing tests,
			// and I'm not sure if I want to do this optimization as it doesn't
			// have sufficient benefit. Basically, we are allocating an array of
			// node values based on the maximum index of the nodes. In an ideal
			// world, that number should be slightly more than the number of
			// reaches.
			// TODO Add this check to data loading process
			boolean isCheckEfficiency = false;
			if (isCheckEfficiency) {
				checkEfficiency(maxNode);
			}
		}
		this.predictData = data;
		nodeCount = maxNode + 1;
	}


	public PredictResultImm doPredict() throws Exception {
		int reachCount = topo.getRowCount(); // # of reaches is equal to the number of 'rows' in topo
		int sourceCount = sourceValues.getColumnCount(); // # of sources is equal to the number of 'columns' in an
		// arbitrary row (row zero)
		int totalIncrementalColOffset = 2*sourceCount;
		int grandTotalColOffset = totalIncrementalColOffset + 1;
		/*
		 * The number of predicted values per reach (k = number of sources, i =
		 * reach #) [i, 0 ... (k-1)] incremental added at reach, per source k
		 * (NOT decayed, just showing what comes in) [i, k ... (2k-1)] total at
		 * reach (w/ up stream contrib), per source k (decayed) [i, (2k)] total
		 * incremental contribution at reach (NOT decayed) [i, (2k + 1)] grand
		 * total at reach (incremental + from node). Comparable to measured.
		 * (decayed)
		 */

		int rchValColCount = (sourceCount * 2) + 2;
		double incReachContribution[][] = new double[reachCount][rchValColCount];

		/*
		 * Array of accumulated values at nodes
		 */
		double upstreamNodeContribution[][] = new double[nodeCount][sourceCount];


		// Iterate over all reaches
		for (int reach = 0; reach < reachCount; reach++) {

			double reachIncrementalContributionAllSourcesTotal = 0d; // incremental for all sources/ (NOT decayed)
			double rchGrandTotal = 0d; // all sources + all from upstream node (decayed)

			// Iterate over all sources
			for (int sourceType = 0; sourceType < sourceCount; sourceType++) {
				int source = sourceType + sourceCount;

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

			incReachContribution[reach][totalIncrementalColOffset] = reachIncrementalContributionAllSourcesTotal; // incremental for all sources (NOT decayed)
			incReachContribution[reach][grandTotalColOffset] = rchGrandTotal; // all sources + all from upstream node (Decayed)

		}

		return PredictResultImm.buildPredictResult(incReachContribution,
				predictData);

	}

	/**
	 * Method to check the efficiency/density of the node ids. Ideally, they would all be consecutive
	 *
	 * @param maxNode
	 */
	private void checkEfficiency(int maxNode) {
		int minNode = Math.min(topo.getMinInt(FNODE_COL), topo
				.getMinInt(TNODE_COL));
		assert (minNode < 2) : "too high a starting point for the node indices leads to large memory consumption";

		double ratio = (double) maxNode / (double) topo.getRowCount();
		assert (ratio < 1.2) : "large gaps in the indices" + maxNode
		+ " - " + +topo.getRowCount()
		+ "results in inefficient memory consumption";
	}
}
