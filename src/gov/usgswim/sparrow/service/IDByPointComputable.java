package gov.usgswim.sparrow.service;

import gov.usgswim.sparrow.Data2D;
import gov.usgswim.sparrow.Data2DView;
import gov.usgswim.task.Computable;
import gov.usgswim.sparrow.Int2DImm;
import gov.usgswim.sparrow.util.JDBCUtil;

import java.sql.Connection;
import java.sql.SQLException;

import org.apache.log4j.Logger;


/**
 * Identifies reaches within a model nearest a point, sorted
 * by distance.
 *
 * The reaches are returned in a Int2D object:
 * The first column is the model id for the reach, the second column is the
 * distance (in meters) from the original point.  The number of rows is determined
 * by the number of results requested, however, in the case where reaches cannot
 * be found 'nearby'*, the number of rows can be less and can be zero rows.
 *
 * *'nearby' is defined arbitrarily to improve database responsiveness.
 * 
 * By implementing Computable, this task can be put in a ComputableCache, which
 * executes the task if the result does not already exist.
 */
public class IDByPointComputable implements Computable<IDByPointRequest, Int2DImm> {
	protected static Logger log =
		Logger.getLogger(PredictDatasetComputable.class); //logging for this class
		
	public IDByPointComputable() {
	}

	public Int2DImm compute(IDByPointRequest req) throws Exception {
		Connection conn = null;
		
		Int2DImm data = null;
		
		try {
			conn = SharedApplication.getInstance().getConnection();
			
			long startTime = System.currentTimeMillis();
			
			String query = buildQuery(req);
			
			if (log.isDebugEnabled()) {
				log.debug("Begin ID by point query for model #" + req.getModelId() + " long: " + req.getPoint().x + " lat: " + req.getPoint().y + " query: " + query);
			}
			
			//Load the data
			data = JDBCUtil.readAsInteger(conn, query, 100);
			
			//data has reach IDs in the first column and distances in the 2nd.
			//Convert to reachIDs as actual IDs and distance in the first column
			int[] ids = data.getIntColumn(0);	//IDs are in column zero
			Data2D view = new Data2DView(data, 1, 1);	//Create view of only 2nd column (column 1)
			int[][] intData = view.getIntData();	//Grab the data array of the view's data
			data = new Int2DImm(intData, view.getHeadings(), -1, ids);
			
			log.debug("End ID by point query for model #" + req.getModelId() + "  Time: " + (System.currentTimeMillis() - startTime) + "ms");
			
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					//ignore
				}
			}
		}
		
		return data;
		
	}
	
	public String buildQuery(IDByPointRequest req) {
		double x = req.getPoint().x;
		double y = req.getPoint().y;
		
		String query =
			"SELECT * FROM (\n" +
			"	SELECT \n" +
			" model_reach_id, \n" +
			"		round(SDO_GEOM.SDO_DISTANCE(REACH_GEOM, sdo_geometry(2001, 8307, " +
			"			sdo_point_type(" + x + ", " + y + ", NULL)," +
			"			NULL, NULL), 0.00005, 'unit=M'),4) Dist_In_Meters \n" +
			"	FROM all_geom_vw \n" +
			"	WHERE \n" +
			"	sparrow_model_id = " + req.getModelId() + " and \n" +
			"	SDO_FILTER(reach_geom, SDO_GEOMETRY(2003, 8307, NULL, SDO_ELEM_INFO_ARRAY(1,1003,3), " +
			"	SDO_ORDINATE_ARRAY(" + (x - 2) + "," + (y - 2) + "," + (x + 2) +"," + (y + 2) + "))) = 'TRUE' \n" +
			" ORDER BY Dist_In_Meters \n" +
			") INNER WHERE rownum < " + (req.getNumberOfResults() + 1);
			
		return query;
	}
}
