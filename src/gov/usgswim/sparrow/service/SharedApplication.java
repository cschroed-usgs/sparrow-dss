package gov.usgswim.sparrow.service;

import gov.usgswim.datatable.DataTable;
import gov.usgswim.sparrow.PredictComputable;
import gov.usgswim.sparrow.PredictData;
import gov.usgswim.sparrow.PredictRequest;
import gov.usgswim.sparrow.PredictResult;
import gov.usgswim.sparrow.domain.ModelImm;
import gov.usgswim.sparrow.service.idbypoint.IDByPointComputable;
import gov.usgswim.sparrow.service.idbypoint.IDByPointRequest;
import gov.usgswim.sparrow.service.model.ModelComputable;
import gov.usgswim.sparrow.service.model.ModelRequest;
import gov.usgswim.sparrow.service.predict.PredictDatasetComputable;
import gov.usgswim.sparrow.util.DataSourceProxy;
import gov.usgswim.sparrow.util.JDBCConnectable;
import gov.usgswim.task.ComputableCache;
import gov.usgswim.sparrow.parser.*;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.naming.InitialContext;
import javax.sql.DataSource;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.constructs.blocking.SelfPopulatingCache;

import oracle.jdbc.driver.OracleDriver;

public class SharedApplication extends DataSourceProxy implements JDBCConnectable {
	private static SharedApplication instance;
	private String dsName = "jdbc/sparrowDSDS";
	private DataSource datasource;
	private boolean lookupFailed = false;
	private ComputableCache<PredictRequest, PredictResult> predictResultCache;
	private ComputableCache<Long, PredictData> predictDatasetCache;	//Long is the Model ID
	private ComputableCache<IDByPointRequest, DataTable> idByPointCache;
	private ComputableCache<ModelRequest, ModelImm> modelCache;

	//an ehcache test cache
	public static final String SERIALIZABLE_CACHE = "PredictContext";
	
	//ehcache bean cache names
	public static final String PREDICT_CONTEXT_CACHE = "PredictContext";
	public static final String ADJUSTMENT_GROUPS_CACHE = "AdjustmentGroups";
	public static final String ANALYSES_CACHE = "Analyses";
	public static final String TERMINAL_REACHES_CACHE = "TerminalReaches";
	public static final String AREA_OF_INTEREST_CACHE = "AreaOfInterest";
	
	
	//ehcache self-populated cache names
	public static final String PREDICT_DATA_CACHE = "PredictData";
	public static final String PREDICT_RESULT_CACHE = "PredictResult";

	
	private SharedApplication() {
		super(null);

		predictResultCache = new ComputableCache<PredictRequest, PredictResult>(new PredictComputable(), "Predict Result Cache");
		predictDatasetCache = new ComputableCache<Long, PredictData>(new PredictDatasetComputable(), "Predict Dataset Cache");
		idByPointCache = new ComputableCache<IDByPointRequest, DataTable>(new IDByPointComputable(), "ID by Point Cache");
		modelCache = new ComputableCache<ModelRequest, ModelImm>(new ModelComputable(), "Model Cache");
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

	public Integer putSerializable(Serializable context) {
		Ehcache c = CacheManager.getInstance().getEhcache(SERIALIZABLE_CACHE);
		int hash = context.hashCode();
		c.put( new Element(hash, context) );
		return hash;
	}
	
	public Serializable getSerializable(Integer id) {
		return getSerializable(id, false);
	}
	
	public Serializable getSerializable(Integer id, boolean quiet) {
		Ehcache c = CacheManager.getInstance().getEhcache(SERIALIZABLE_CACHE);
		Element e  = (quiet)?c.getQuiet(id):c.get(id);
		return (e != null)?((Serializable) e.getObjectValue()):null;
	}
	
	//PredictContext Cache
	public Integer putPredictionContext(PredictionContext context) {
		Ehcache c = CacheManager.getInstance().getEhcache(PREDICT_CONTEXT_CACHE);
		int hash = context.hashCode();
		c.put( new Element(hash, context) );
		return hash;
	}
	
	public PredictionContext getPredictionContext(Integer id) {
		return getPredictionContext(id, false);
	}
	
	public PredictionContext getPredictionContext(Integer id, boolean quiet) {
		Ehcache c = CacheManager.getInstance().getEhcache(PREDICT_CONTEXT_CACHE);
		Element e  = (quiet)?c.getQuiet(id):c.get(id);
		return (e != null)?((PredictionContext) e.getObjectValue()):null;
	}
	
	//AdjustmentGroup Cache
	//TODO:  need this yet
	
	//Analysis Cache
	public Integer putAnalysis(Analysis context) {
		Ehcache c = CacheManager.getInstance().getEhcache(ANALYSES_CACHE);
		int hash = context.hashCode();
		c.put( new Element(hash, context) );
		return hash;
	}
	
	public Analysis getAnalysisContext(Integer id) {
		return getAnalysis(id, false);
	}
	
	public Analysis getAnalysis(Integer id, boolean quiet) {
		Ehcache c = CacheManager.getInstance().getEhcache(ANALYSES_CACHE);
		Element e  = (quiet)?c.getQuiet(id):c.get(id);
		return (e != null)?((Analysis) e.getObjectValue()):null;
	}
	
	//TerminalReach Cache
	public Integer putTerminalReaches(Analysis context) {
		Ehcache c = CacheManager.getInstance().getEhcache(TERMINAL_REACHES_CACHE);
		int hash = context.hashCode();
		c.put( new Element(hash, context) );
		return hash;
	}
	
	public TerminalReaches getTerminalReaches(Integer id) {
		return getTerminalReaches(id, false);
	}
	
	public TerminalReaches getTerminalReaches(Integer id, boolean quiet) {
		Ehcache c = CacheManager.getInstance().getEhcache(TERMINAL_REACHES_CACHE);
		Element e  = (quiet)?c.getQuiet(id):c.get(id);
		return (e != null)?((TerminalReaches) e.getObjectValue()):null;
	}
	
	//AreaOfInterest Cache
	//TODO:  need this yet
	//
	
	//PredictData Cache
	public PredictData getPredictData(Long id) {
		return getPredictData(id, false);
	}
	
	public PredictData getPredictData(Long id, boolean quiet) {
		Ehcache c = CacheManager.getInstance().getEhcache(PREDICT_DATA_CACHE);
		Element e  = (quiet)?c.getQuiet(id):c.get(id);
		return (e != null)?((PredictData) e.getObjectValue()):null;
	}
	
	//PredictResult Cache
	public DataTable getPredictResult(PredictRequest req) {
		return getPredictResult(req, false);
	}
	
	public DataTable getPredictResult(PredictRequest req, boolean quiet) {
		Ehcache c = CacheManager.getInstance().getEhcache(PREDICT_RESULT_CACHE);
		Element e  = (quiet)?c.getQuiet(req):c.get(req);
		return (e != null)?((DataTable) e.getObjectValue()):null;
	}
	
	
	
	public ComputableCache<PredictRequest, PredictResult> getPredictResultCache() {
		return predictResultCache;
	}

	public ComputableCache<Long, PredictData> getPredictDatasetCache() {
		return predictDatasetCache;
	}

	public ComputableCache<IDByPointRequest, DataTable> getIdByPointCache() {
		return idByPointCache;
	}

	public ComputableCache<ModelRequest, ModelImm> getModelCache() {
		return modelCache;
	}
	

}

