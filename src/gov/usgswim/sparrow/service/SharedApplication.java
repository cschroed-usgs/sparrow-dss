package gov.usgswim.sparrow.service;

import gov.usgswim.sparrow.Computable;
import gov.usgswim.sparrow.ComputableCache;
import gov.usgswim.sparrow.Double2D;
import gov.usgswim.sparrow.PredictDatasetComputable;
import gov.usgswim.sparrow.PredictionComputable;
import gov.usgswim.sparrow.PredictionDataSet;
import gov.usgswim.sparrow.PredictionRequest;
import gov.usgswim.sparrow.util.DataSourceProxy;
import gov.usgswim.sparrow.util.JDBCConnectable;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.naming.InitialContext;

import javax.sql.DataSource;

import oracle.jdbc.driver.OracleDriver;


public class SharedApplication extends DataSourceProxy implements JDBCConnectable {
	private static SharedApplication instance;
	private String dsName = "jdbc/sparrowDS";
	private DataSource datasource;
	private boolean lookupFailed = false;
	private ComputableCache<PredictionRequest, Double2D> predictResultCache;
	private ComputableCache<Long, PredictionDataSet> predictDatasetCache;
	private ComputableCache metadataCache;
	
	
	private SharedApplication() {
		super(null);
		
		predictResultCache = new ComputableCache<PredictionRequest, Double2D>(new PredictionComputable());
		predictDatasetCache = new ComputableCache<Long, PredictionDataSet>(new PredictDatasetComputable());
	}
	
	public static synchronized SharedApplication getInstance() {
		if (instance == null) {
			instance = new SharedApplication();
		}
		
		return instance;
	}
	
	public Connection getConnection() throws SQLException {
		return findConnection();
	}
	
	public Connection getConnection(String username, String password)
			throws SQLException {
			
		return findConnection();
	}
	
	private Connection findConnection() throws SQLException {
		synchronized (this) {
			if (datasource == null && ! lookupFailed) {
				try {
					InitialContext context = new InitialContext();
					datasource = (DataSource)context.lookup(dsName);
				} catch (Exception e) {
					lookupFailed = true;
				}
			}
		}
		
		
		if (datasource != null) {
			return datasource.getConnection();
		} else {
			return getDirectConnection();
		}
		
	}
	
	private Connection getDirectConnection() throws SQLException {
		synchronized (this) {
			DriverManager.registerDriver(new OracleDriver());
		}
		
		String username = "SPARROW_DSS";
		String password = "***REMOVED***";
		String thinConn = "jdbc:oracle:thin:@130.11.165.152:1521:widw";
		
		return DriverManager.getConnection(thinConn,username,password);
	}

	public ComputableCache<PredictionRequest, Double2D> getPredictResultCache() {
		return predictResultCache;
	}

	public ComputableCache<Long, PredictionDataSet> getPredictDatasetCache() {
		return predictDatasetCache;
	}
}
