package gov.usgswim.sparrow.util;

import gov.usgswim.sparrow.Double2D;

import gov.usgswim.sparrow.Int2D;

import gov.usgswim.sparrow.PredictionDataSet;

import gov.usgswim.sparrow.domain.Model;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Connection;

import java.sql.SQLException;

import java.sql.Statement;

import java.util.ArrayList;

public class JDBCUtil {
	public JDBCUtil() {
	}
	
	/**
	 * Loads only the data required to run a prediction.
	 * 
	 * @param conn
	 * @param modelId
	 * @return
	 * @throws SQLException
	 */
	public static PredictionDataSet loadMinimalPredictDataSet(Connection conn, int modelId)
			throws SQLException {
		
		PredictionDataSet dataSet = new PredictionDataSet();
		
		Int2D sources = loadSource(conn, modelId);	//Need this list
		
		dataSet.setSys( loadSystemInfo(conn, modelId) );
		dataSet.setTopo( loadTopo(conn, modelId) );
		dataSet.setCoef( loadSourceReachCoef(conn, modelId, 0, sources) );
		dataSet.setDecay( loadDecay(conn, modelId, 0) );
		dataSet.setSrc( loadSourceValues(conn, modelId, sources) );
		
		return dataSet;
	}
	
	/**
	 * Loads all the model data.
	 * 
	 * @param conn
	 * @param modelId
	 * @return
	 * @throws SQLException
	 */
	public static PredictionDataSet loadFullPredictDataSet(Connection conn, int modelId)
			throws SQLException {
		
		return null;
	}
	
	public static int writePredictDataSet(PredictionDataSet data, Connection conn)
			throws SQLException {
		
		return null;
	}
	
	
	/**
	 * Returns a Int2D table of all System info
	 * <h4>Data Columns, sorted by HYDSEQ.  One row per reach (i = reach index)</h4>
	 * <ol>
	 * <li>[i][0] REACH_ID - The system id for the reach (db unique id)
	 * <li>[i][1] HYDSEQ - The model specific hydrological sequence number
	 * </ol>
	 * 
	 * @param conn	A JDBC Connection to run the query on
	 * @param modelId	The ID of the Sparrow model
	 * @return Fetched data - see Data Columns above.
	 * @throws SQLException
	 */
	public static Int2D loadSystemInfo(Connection conn, int modelId) throws SQLException {
		String query =
			"SELECT MODEL_REACH_ID as MODEL_REACH, HYDSEQ FROM MODEL_REACH WHERE SPARROW_MODEL_ID = " +  modelId + " ORDER BY HYDSEQ";
	
		return readAsInteger(conn, query, 2000);
		
	}
	
	/**
	 * Returns a Int2D table of all topo data for for a single model.
	 * <h4>Data Columns (sorted by HYDSEQ)</h4>
	 * <ol>
	 * <li>FNODE - The from node
	 * <li>TNODE - The to node
	 * <li>IFTRAN - 1 if this reach transmits to its end node, 0 otherwise
	 * </ol>
	 * 
	 * @param conn	A JDBC Connection to run the query on
	 * @param modelId	The ID of the Sparrow model
	 * @return Fetched data - see Data Columns above.
	 * @throws SQLException
	 */
	public static Int2D loadTopo(Connection conn, int modelId) throws SQLException {
		String query =
			"SELECT FNODE, TNODE, IFTRAN FROM ALL_TOPO_VW WHERE SPARROW_MODEL_ID = " +  modelId + " ORDER BY HYDSEQ";
	
		return readAsInteger(conn, query, 1000);
		
	}
	
	/**
	 * Returns a Double2D table of all source/reach coef's for for a single model.
	 * <h4>Data Columns with one row per reach (sorted by HYDSEQ)</h4>
	 * <ol>
	 * <li>[Source Name 1] - The coef's for the first source in one column
	 * <li>[Source Name 2...] - The coef's for the 2nd...
	 * <li>...
	 * </ol>
	 * 
	 * @param conn	A JDBC Connection to run the query on
	 * @param modelId	The ID of the Sparrow model
	 * @param iteration The iteration for which coef's should be returned.  Zero is the nominal value - all others are for bootstrapping.
	 * @param sources	An Int2D list of the sources for the model, in one column (see loadSource)
	 * @return Fetched data - see Data Columns above.
	 * @throws SQLException
	 */
	public static Double2D loadSourceReachCoef(Connection conn, int modelId, int iteration, Int2D sources) throws SQLException {
	
		if (iteration < 0) {
			throw new IllegalArgumentException("The iteration cannot be less then zero");
		}
		if (sources.getRowCount() == 0) {
			throw new IllegalArgumentException("There must be at least one source");
		}
	
		String reachCountQuery =
			"SELECT COUNT(*) FROM MODEL_REACH WHERE SPARROW_MODEL_ID = " + modelId;
		Int2D reachCountData = readAsInteger(conn, reachCountQuery, 1);
		int reachCount = reachCountData.getInt(0, 0);
		int sourceCount = sources.getRowCount();
		
		Double2D sourceReachCoef = new Double2D(new double[reachCount][sourceCount]);
		
	
		for (int srcIndex=0; srcIndex<sourceCount; srcIndex++) {
		
			String query =
				"SELECT coef.VALUE AS Value " +
				"FROM SOURCE_REACH_COEF coef INNER JOIN MODEL_REACH rch ON coef.MODEL_REACH_ID = rch.MODEL_REACH_ID " +
				"WHERE " +
				"rch.SPARROW_MODEL_ID = " +  modelId + " AND " +
				"coef.Iteration = " +  iteration + " AND " +
				"coef.SOURCE_ID = " +  sources.getInt(srcIndex, 0) + " " +
				"ORDER BY rch.HYDSEQ";
			
			
			Statement st = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			st.setFetchSize(2000);
			ResultSet rs = null;
			
			try {
			
				rs = st.executeQuery(query);
				loadColumn(rs, sourceReachCoef, 0, srcIndex);
				
			} finally {
				if (rs != null) {
					rs.close();
					rs = null;
				}
			}
			
		}
		
		return sourceReachCoef;

	}

	
	/**
	 * Returns a Double2D table of all decay data for for a single model.
	 * 
	 * <h4>Data Columns, sorted by HYDSEQ</h4>
	 * <p>One row per reach (i = reach index)</p>
	 * <ol>
	 * <li>[i][0] == the instream decay at reach i.<br>
	 *   This decay is assumed to be at mid-reach and already computed as such.
	 *   That is, it would normally be the sqr root of the instream decay, and
	 *   it is assumed that this value already has the square root taken.
	 * <li>src[i][1] == the upstream decay at reach i.<br>
	 *   This decay is applied to the load coming from the upstream node.
	 * <li>Additional columns ignored
	 * </ol>
	 * 
	 * @param conn	A JDBC Connection to run the query on
	 * @param modelId	The ID of the Sparrow model
	 * @param iteration The iteration for which coef's should be returned.  Zero is the nominal value - all others are for bootstrapping.
	 * @return Fetched data - see Data Columns above.
	 * @throws SQLException
	 */
	public static Double2D loadDecay(Connection conn, int modelId, int iteration) throws SQLException {
		String query =
			"SELECT coef.INC_DELIVERY, coef.TOTAL_DELIVERY " +
			"FROM REACH_COEF coef INNER JOIN MODEL_REACH rch ON coef.MODEL_REACH_ID = rch.MODEL_REACH_ID " +
			"WHERE rch.SPARROW_MODEL_ID = " +  modelId + " AND coef.ITERATION = " + iteration + " " +
			"ORDER BY rch.HYDSEQ";
	
		return readAsDouble(conn, query, 2000);
		
	}
	
	
	/**
	 * Returns a Double2D table of all source values for for a single model.
	 * <h4>Data Columns with one row per reach (sorted by HYDSEQ)</h4>
	 * <ol>
	 * <li>[Source Name 1] - The values for the first source in one column
	 * <li>[Source Name 2...] - The values for the 2nd...
	 * <li>...
	 * </ol>
	 * 
	 * @param conn	A JDBC Connection to run the query on
	 * @param modelId	The ID of the Sparrow model
	 * @param sources	An Int2D list of the sources for the model, in one column (see loadSource)
	 * @return Fetched data - see Data Columns above.
	 * @throws SQLException
	 */
	public static Double2D loadSourceValues(Connection conn, int modelId, Int2D sources) throws SQLException {
	
		if (sources.getRowCount() == 0) {
			throw new IllegalArgumentException("There must be at least one source");
		}
	
		String reachCountQuery =
			"SELECT COUNT(*) FROM MODEL_REACH WHERE SPARROW_MODEL_ID = " + modelId;
		Int2D reachCountData = readAsInteger(conn, reachCountQuery, 1);
		int reachCount = reachCountData.getInt(0, 0);
		int sourceCount = sources.getRowCount();
		
		Double2D sourceValue = new Double2D(new double[reachCount][sourceCount]);
		
	
		for (int srcIndex=0; srcIndex<sourceCount; srcIndex++) {
		
			String query =
				"SELECT src.VALUE AS Value " +
				"FROM SOURCE_VALUE src INNER JOIN MODEL_REACH rch ON src.MODEL_REACH_ID = rch.MODEL_REACH_ID " +
				"WHERE " +
				"rch.SPARROW_MODEL_ID = " +  modelId + " AND " +
				"src.SOURCE_ID = " +  sources.getInt(srcIndex, 0) + " " +
				"ORDER BY rch.HYDSEQ";
			
			
			Statement st = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			st.setFetchSize(2000);
			ResultSet rs = null;
			
			try {
			
				rs = st.executeQuery(query);
				loadColumn(rs, sourceValue, 0, srcIndex);
				
			} finally {
				if (rs != null) {
					rs.close();
					rs = null;
				}
			}
			
		}
		
		return sourceValue;

	}
	
	/**
	 * Returns a single column Int2D table of all source IDs for a single model.
	 * <h4>Data Columns (sorted by SORT_ORDER)</h4>
	 * <ol>
	 * <li>SOURCE_ID - The DB ID for the source
	 * </ol>
	 * 
	 * @param conn	A JDBC Connection to run the query on
	 * @param modelId	The ID of the Sparrow model
	 * @return	An Int2D object contains the list of source_id's in a single column
	 * @throws SQLException
	 */
	public static Int2D loadSource(Connection conn, int modelId) throws SQLException {
		String query =
			"SELECT SOURCE_ID FROM SOURCE WHERE SPARROW_MODEL_ID = " +  modelId + " ORDER BY SORT_ORDER";
	
		Statement st = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
		st.setFetchSize(1000);

		ResultSet rs = null;
		
		try {
		
			rs = st.executeQuery(query);
			return readAsInteger(rs);
			
		} finally {
			if (rs != null) {
				rs.close();
				rs = null;
			}
		}
	}
	
	
	/**
	 * Loads a single column from the resultSet source to the Double2D destination table.
	 * For consistency, the from and to columns are ZERO INDEXED in both cases.
	 * 
	 * @param source Resultset to load the data from.  The resultset is assumed to be before the first row.
	 * @param dest The destination Double2D table
	 * @param fromCol The column (zero indexed) in the resultset to load from
	 * @param toCol The column (zero indexed) in the Double2D table to load to
	 * @throws SQLException
	 */
	public static void loadColumn(ResultSet source, Double2D dest, int fromCol, int toCol) throws SQLException {
		
		fromCol++;		//covert to ONE base index
		int currentRow = 0;
		
		while (source.next()){
		
			double d = source.getDouble(fromCol);
			dest.setValueAt(new Double(d), currentRow,  toCol);
			currentRow++;
			
		}

	}
	
	
	 
	/**
	 * Creates a Int2D table from the passed query.
	 * 
	 * All values in the source must be convertable to an integer.
	 * 
	 * @param conn	A connection to use for the query
	 * @param query The query to run
	 * @param fetchSize Number of rows returned per fetch - use large values (1000+ for queries w/ few columns)
	 * @return
	 * @throws SQLException
	 */
	public static Int2D readAsInteger(Connection conn, String query, int fetchSize) throws SQLException {
		Statement st = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
		st.setFetchSize(fetchSize);

		ResultSet rs = null;
		
		try {
		
			rs = st.executeQuery(query);
			return readAsInteger(rs);
			
		} finally {
			if (rs != null) {
				rs.close();
				rs = null;
			}
		}
	}
	
	/**
	 * Creates a Int2D table from the passed resultset.
	 * 
	 * All values in the source must be convertable to an integer.
	 * 
	 * @param source
	 * @return
	 * @throws SQLException
	 */
	public static Int2D readAsInteger(ResultSet source) throws SQLException {
			
		ArrayList list = new ArrayList(500);
		String[] headings = null;		
		
		headings = readHeadings(source.getMetaData());
		int colCount = headings.length; //Number of columns
		
		while (source.next()){
		
		
			int[] row = new int[colCount];
			
			for (int i=1; i<=colCount; i++) {
				row[i - 1] = source.getInt(i);
			}
			
			list.add(row);
			
		}
				
		
		//copy the array list to a double[][] array
		int[][] data = new int[list.size()][];
		for (int i = 0; i < data.length; i++)  {
			data[i] = (int[]) list.get(i);
		}
		
		Int2D data2D = new Int2D(data, headings);
		
		return data2D;
	}
	
	
	/**
	 * Creates a Double2D table from the passed query.
	 * 
	 * All values in the source must be convertable to a double.
	 * 
	 * @param conn	A connection to use for the query
	 * @param query The query to run
	 * @param fetchSize Number of rows returned per fetch - use large values (1000+ for queries w/ few columns)
	 * @return
	 * @throws SQLException
	 */
	public static Double2D readAsDouble(Connection conn, String query, int fetchSize) throws SQLException {
		Statement st = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
		st.setFetchSize(fetchSize);

		ResultSet rs = null;
		
		try {
		
			rs = st.executeQuery(query);
			return readAsDouble(rs);
			
		} finally {
			if (rs != null) {
				rs.close();
				rs = null;
			}
		}
	}
	
	/**
	 * Creates a Double2D table from the passed resultset.
	 * 
	 * All values in the source must be convertable to a double.
	 * 
	 * @param source
	 * @return
	 * @throws SQLException
	 */
	public static Double2D readAsDouble(ResultSet source) throws SQLException {
			
		ArrayList list = new ArrayList(500);
		String[] headings = null;
		
		headings = readHeadings(source.getMetaData());
		int colCount = headings.length; //Number of columns
		
		while (source.next()){
		
		
			double[] row = new double[colCount];
			
			for (int i=1; i<=colCount; i++) {
				row[i - 1] = source.getDouble(i);
			}
			
			list.add(row);
			
		}
				
		
		//copy the array list to a double[][] array
		double[][] data = new double[list.size()][];
		for (int i = 0; i < data.length; i++)  {
			data[i] = (double[]) list.get(i);
		}
		
		Double2D data2D = new Double2D(data, headings);
		
		return data2D;
	}
	
	private static String[] readHeadings(ResultSetMetaData meta) throws SQLException {
		int count = meta.getColumnCount();
		String[] headings = new String[count];
		
		for (int i=1; i<=count; i++) {
			headings[i - 1] = meta.getColumnName(i);
		}
		
		return headings;
			
	}
}
