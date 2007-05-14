package gov.usgswim.sparrow.util;

import gov.usgswim.sparrow.Data2D;
import gov.usgswim.sparrow.PredictionDataSet;

import gov.usgswim.sparrow.domain.ModelImp;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import java.sql.Connection;
import java.sql.DriverManager;

import java.sql.SQLException;

import java.util.HashSet;

import oracle.jdbc.OracleDriver;

public class LoadTestRunner {
	public static String DATA_ROOT_DIR = "/data/ch2007_04_24/";

	public LoadTestRunner() {
	}

	public static void main(String[] args) throws Exception {
		LoadTestRunner loadTestRunner = new LoadTestRunner();
		
		loadTestRunner.run(args);
		
		

	}
	
	public void run(String[] args) throws Exception {
		String rootDir = DATA_ROOT_DIR;
		
		if (args != null && args.length > 0) {
			rootDir = args[0];
		}
		
		if (! rootDir.endsWith("/")) rootDir = rootDir + "/";
		
		PredictionDataSet pd = new PredictionDataSet();
		
		pd.setAncil( TabDelimFileUtil.readAsDouble(getClass().getResourceAsStream(rootDir + "ancil.txt"), true) );
		pd.setCoef( TabDelimFileUtil.readAsDouble(getClass().getResourceAsStream(rootDir + "coef.txt"), true) );
		pd.setSrc( TabDelimFileUtil.readAsDouble(getClass().getResourceAsStream(rootDir + "src.txt"), true) );
		pd.setTopo( TabDelimFileUtil.readAsDouble(getClass().getResourceAsStream(rootDir + "topo.txt"), true) );
		
		ModelImp model = new ModelImp(21);
		model.setEnhNetworkId(2);
		
		pd.setModel( model );
		
		Connection conn = getConnection();
		
		try {
			conn.setAutoCommit(false);
			JDBCUtil.writePredictDataSet(pd, conn);
		} finally {
			try {
				conn.rollback();
			} catch (Exception ee) {
				//ignore
			}
			conn.close();
		}

	}
	
	/*
	protected PredictionDataSet removeDuplicates(PredictionDataSet pdSrc) {
		HashSet dups = new HashSet();
		Data2D topo = pdSrc.getTopo();
		
		for (int i = 1; i <topo.getRowCount(); i++)  {
			if (topo.getDouble(i, 3) == topo.getDouble(i - 1, 3)) {
				dups.add(i);
			}
		}
		
		////// copy over skippping dups //////
		
		
		
		
		
	}
	*/
	
	protected Connection getConnection() throws SQLException {
		String username = "SPARROW_DSS";
		String password = "***REMOVED***";
		String thinConn = "jdbc:oracle:thin:@130.11.165.152:1521:widw";
		DriverManager.registerDriver(new OracleDriver());
		return DriverManager.getConnection(thinConn,username,password);
	}

}
