package gov.usgswim.sparrow.action;

import static gov.usgswim.sparrow.PredictData.IFTRAN_COL;
import static gov.usgswim.sparrow.PredictData.UPSTREAM_DECAY_COL;
import gov.usgswim.datatable.ColumnData;
import gov.usgswim.datatable.DataTable;
import gov.usgswim.datatable.impl.SparseDoubleColumnData;
import gov.usgswim.datatable.impl.StandardDoubleColumnData;
import gov.usgswim.sparrow.PredictData;
import gov.usgswim.sparrow.SparrowUnits;
import gov.usgswim.sparrow.datatable.TableProperties;
import gov.usgswim.sparrow.parser.BaseDataSeriesType;
import gov.usgswim.sparrow.parser.DataSeriesType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

/**
 * This action creates a ColumnData containing the delivery
 * fractions to the set of Target reaches.
 * 
 * @author eeverman
 *
 */
public class CalcDeliveryFractionDataColumn extends Action<ColumnData> {

	protected PredictData predictData;
	protected Set<Long> targetReachIds;
	protected String msg = null;
	
	/**
	 * Sets the predictData used to calc the delivery fraction.
	 * @param predictData
	 */
	public void setPredictData(PredictData predictData) {
		this.predictData = predictData;
	}

	/**
	 * The targets to calc the delivery fraction w/ respect to.
	 * @param targetReachIds
	 */
	public void setTargetReachIds(Set<Long> targetReachIds) {
		this.targetReachIds = targetReachIds;
	}
	
	@Override
	protected String getPostMessage() {
		return msg;
	}
	
	@Override
	public ColumnData doAction() throws Exception {
		//Hash containing rows as keys and DeliveryReaches as values.
		HashMap<Integer, DeliveryReach> deliveries = calcDeliveryHash(predictData, targetReachIds);
		int baseRows = predictData.getTopo().getRowCount();
		
		//Props for the returned column
		Map<String, String> props = new HashMap<String, String>();
		props.put(TableProperties.CONSTITUENT.getPublicName(), predictData.getModel().getConstituent());
		props.put(TableProperties.DATA_TYPE.getPublicName(), BaseDataSeriesType.delivered_fraction.name());
		props.put(TableProperties.DATA_SERIES.getPublicName(), DataSeriesType.delivered_fraction.name());
		
		if (deliveries.size() > (baseRows / 9)) {
			double[] vals2d = new double[baseRows];
			
			for (DeliveryReach dr : deliveries.values()) {
				vals2d[dr.getRow()] = dr.getDelivery();
			}
			

			
			//Todo:  It would be nice to have a standard property name for the
			//model that this relates to and what the rows are related to.
			StandardDoubleColumnData column = new StandardDoubleColumnData(
					vals2d,
					getDataSeriesProperty(DataSeriesType.delivered_fraction, false),
					SparrowUnits.FRACTION.getUserName(),
					getDataSeriesProperty(DataSeriesType.delivered_fraction, true),
					props, false);
			
			return column;
		} else {
			int hashSize = baseRows * 3 / 2;
			if ((hashSize / 2) == ((double)hashSize / 2d)) hashSize++;
			
			HashMap<Integer, Double> delFracs = new HashMap<Integer, Double>(hashSize, 1);
			
			for (DeliveryReach dr : deliveries.values()) {
				delFracs.put(dr.getRow(), dr.getDelivery());
			}
			
			SparseDoubleColumnData column = new SparseDoubleColumnData(
					delFracs,
					getDataSeriesProperty(DataSeriesType.delivered_fraction, false),
					SparrowUnits.FRACTION.getUserName(),
					getDataSeriesProperty(DataSeriesType.delivered_fraction, true),
					props, null, predictData.getTopo().getRowCount(), 0d);
			
			return column;
		}
	}
	
	
	/**
	 * Calculates the delivery fractions for reaches upstream of a specified targets.
	 * A list of all upstream reaches are return, some of which may contain
	 * a zero value for delivery.
	 * 
	 * @param predictData The predict data to search within for upstream reaches
	 * @param targetReachIds A Set of reach IDs that define the targets.
	 * @return
	 * @throws Exception
	 */
	protected HashMap<Integer, DeliveryReach> calcDeliveryHash(
			PredictData predictData, Set<Long> targetReachIds) throws Exception {
		
		DataTable topo = predictData.getTopo();
		
		Iterator<Long> targetIdIterator = targetReachIds.iterator();
		
		//que that pops the most downstream target first
		PriorityQueue<DeliveryReach> targetProcessingQue = new PriorityQueue<DeliveryReach>();
		
		//uses a row number as the key
		HashMap<Integer, DeliveryReach> mergedDeliveryFractions = new HashMap<Integer, DeliveryReach>();
		
		//Build processing que to ensure that downstream targets are processed first
		while (targetIdIterator.hasNext()) {
			Long id = targetIdIterator.next();
			int row = predictData.getRowForReachID(id);
			DeliveryReach current = new DeliveryReach(
					row, 1d, topo.getInt(row, PredictData.HYDSEQ_COL)
			);
			targetProcessingQue.add(current);
		}
		
		while (! targetProcessingQue.isEmpty()) {
			DeliveryReach currentTarget = targetProcessingQue.poll();
			
			if (mergedDeliveryFractions.get(currentTarget.getRow()) == null) {
				List<DeliveryReach> currentCalcs =
					calcDeliveryForSingleTarget(predictData, currentTarget);
				
				//merge results of the reaches upstream of this target w/ previous
				for (DeliveryReach dr : currentCalcs) {
					DeliveryReach existing = mergedDeliveryFractions.get(dr.getRow());
					if (existing == null) {
						mergedDeliveryFractions.put(dr.getRow(), dr);
					} else {
						existing.addDelivery(dr.getDelivery());
					}
				}
			} else {
				log.info("A target reach (row=" + currentTarget.getRow()
					+ ") was excluded b/c it is upstream of another target reach.");
			}
		}
		
		msg = "Delivery Details:  Model size:  " + topo.getRowCount() +
				" rows, " + targetReachIds.size() + " targets.  " +
				"Found " + mergedDeliveryFractions.size() + " upstream reaches.";
		
		return mergedDeliveryFractions;
	}
	
	/**
	 * Calculates the delivery fractions for the reaches upstream of a specified target.
	 * A list of all upstream reaches are return, some of which may contain
	 * a zero value for delivery.
	 * 
	 * @param predictData The predict data to search within for upstream reaches
	 * @param targetReach The target reach to calculate delivery to
	 * @return A list of DeliveryReaches which are upstream of the targetReach
	 * @throws Exception
	 */
	protected List<DeliveryReach> calcDeliveryForSingleTarget(
			PredictData predictData, DeliveryReach targetReach) throws Exception {
		
		DataTable topo = predictData.getTopo();
		
		//Queue of upstream reaches as they are found, in rev hydseq order
		Queue<DeliveryReach> upstreamReaches = new PriorityQueue<DeliveryReach>(4);
		
		//All reaches for which we have thus far calculated a delivery fraction
		List<DeliveryReach> deliveryReaches = new ArrayList<DeliveryReach>();
		
		//Add the current reaches' upstream reaches to the queue
		addUpstreamReachesToQueue(upstreamReaches, predictData, targetReach);
		deliveryReaches.add(targetReach);	//add to output - should be delivery of 1
		
		//Continually loop through upstream reaches
		while (! upstreamReaches.isEmpty()) {
			DeliveryReach current = upstreamReaches.poll();
			
			//Multiple downstream reaches are unlikely, but possible.
			//They would have to come from the same target, split, and rejoin.
			for (DeliveryReach downstream : current.getDownstreamReaches()) {
				int downstreamRow = downstream.getRow();
				
				//complete calculated delivery frac of one of the reaches
				//downstream of the current reach.
				//In the case of a split there would be multiple.
				double downstrmCalcedDeliveryFrac = downstream.getDelivery();
				
				//Total delivery (that is, the inverse of total decay) for  the
				//downstream reach.  NOTE that in our data, total delivery
				//is already multiplied by the fraction in the cases of a split.
				double downstrmTotalDelivery = predictData.getDelivery().
					getDouble(downstreamRow, UPSTREAM_DECAY_COL);
				
				//Zero if the reach has no transport to the downstream reach.
				double ifTran = topo.getDouble(current.getRow(), IFTRAN_COL);
				
				double addedDelivery =
					downstrmCalcedDeliveryFrac * downstrmTotalDelivery * ifTran;
				
				current.addDelivery(addedDelivery);
			}
			
			//Add the current reaches' upstream reaches to the queue
			addUpstreamReachesToQueue(upstreamReaches, predictData, current);
			
			deliveryReaches.add(current);	//Add current to list of completed
		}
		
		return deliveryReaches;
	}
	
	/**
	 * Adds the reaches immediately upstream to the queue.
	 * Reaches are added with a delivery fraction of zero.  If a duplicate reach
	 * is added (multiple pathways to the same reach), the current reach is
	 * added to its list of downstream reaches.
	 * @param upstreamReaches The queue to add to
	 * @param predictData The predict data to seach within
	 * @param current The current reach for which to find immediate upstream reaches
	 */
	protected void addUpstreamReachesToQueue(Queue<DeliveryReach> upstreamReaches,
			PredictData predictData, DeliveryReach current) {
		
		DataTable topo = predictData.getTopo();
		long fnode = topo.getLong(current.getRow(), PredictData.FNODE_COL);
		
		//The index requires that an Integer be used.
		int[] upstream = topo.findAll(PredictData.TNODE_COL, new Integer((int)fnode));
		
		for (int rowNum : upstream) {
			
			DeliveryReach toBeAdded = new DeliveryReach(
				rowNum, 0d, topo.getInt(rowNum, PredictData.HYDSEQ_COL), current
			);
			
			if (! upstreamReaches.contains(toBeAdded)) {
				upstreamReaches.add(toBeAdded);
			} else {
				//This reach already exists in our upstream list.
				//Find it & add the current reach as an added downstream reach.
				Iterator<DeliveryReach> upstreamIt = upstreamReaches.iterator();
				
				while (upstreamIt.hasNext()) {
					DeliveryReach u = upstreamIt.next();
					
					if (u.equals(toBeAdded)) {	//.equals based only on row
						u.addDownstreamReach(current);
						break;
					}
				}
				
			}
		}
	}
}
