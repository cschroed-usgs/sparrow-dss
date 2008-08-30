package gov.usgswim.sparrow.loader;

import gov.usgswim.sparrow.util.DataLoader;
import gov.usgswim.sparrow.util.JDBCUtil;
import static gov.usgswim.sparrow.loader.ModelDataAssumptions.*;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import oracle.jdbc.driver.OracleDriver;

import org.apache.commons.lang.StringUtils;

import sun.util.calendar.Era;

public class ModelDataLoader {
	private static Connection PROD_CONN = getProductionConnection();
	private static Connection DEV_CONN = getDevelopmentConnection();
	
	private static final String REACH_IDENTIFIER = "local_id";
	private static final String FULL_IDENTIFIER = "std_id";
	public static final String[] BASIC_COEF_COLUMNS = new String[] {"ITER", "INC_DELIVF", "TOT_DELIVF", "BOOT_ERROR"};
	
	
	
	{

	}
	
	public static void deleteModel(Long modelID, File modelMetadata) throws IOException {
		
		BufferedWriter writer = makeOutWriter(modelMetadata, "delete_all.sql");		
		// ----------------
		// GENERATE THE SQL
		// ----------------
		try {
			String removeModel = "DELETE FROM " + JDBCUtil.SPARROW_SCHEMA + ".SPARROW_MODEL WHERE SPARROW_MODEL_ID = " + modelID + ";";
			String removeReaches = "DELETE FROM " + JDBCUtil.SPARROW_SCHEMA + ".MODEL_REACH WHERE SPARROW_MODEL_ID = " + modelID + ";";
			String removeSource = "DELETE FROM " + JDBCUtil.SPARROW_SCHEMA + ".SOURCE WHERE SPARROW_MODEL_ID = " + modelID + ";";

			writer.write(removeModel);
			writer.write("\n");
			writer.write(removeReaches);
			writer.write("\n");
			writer.write(removeSource);
			writer.write("\n");
		} finally {
			writer.write("commit;\n");
			writer.flush();
			writer.close();
		}
	}
	
	public static void insertModelMetadata(Connection conn, Long modelID, File modelMetadata) throws IOException {
		DataFileDescriptor fileMetaData = validateModelMetadata(modelMetadata);
		assert(fileMetaData.lines == 1):"there should only be one line for the model metadata (excluding headers)";
		
		BufferedReader reader = new BufferedReader(new FileReader(modelMetadata));
		BufferedWriter writer = makeOutWriter(modelMetadata, "model_metadata_insert.sql");
		advancePastHeader(fileMetaData, reader);
		
		// ----------------
		// GENERATE THE SQL
		// ----------------
		try {
			String line = reader.readLine();			
			String[] inputValues = line.split(fileMetaData.delimiter);
			List<String> values = new ArrayList<String>();
			String[] allFields = {"SPARROW_MODEL_ID", "NAME","DESCRIPTION","DATE_ADDED","CONTACT_ID","ENH_NETWORK_ID"};
			{
				values.add((modelID == null)? null: Long.toString(modelID));
				// if no modelID submitted, SPARROW_MODEL_ID has an insert trigger using sequence SPARROW_MODEL_SEQ
				values.add(quoteForSQL(inputValues[1]));
				values.add(quoteForSQL(inputValues[2]));
				values.add("SYSDATE");
				values.add(Integer.toString(CONTACT_ID));
				values.add(Integer.toString(ENH_NETWORK_ID));
			}
			
			String sql = "INSERT into " + JDBCUtil.SPARROW_SCHEMA + ".SPARROW_MODEL ("
			+ joinInParallel(allFields, ",", values) + ")"
			+" VALUES (" + (join(values, ",") + ");\n");
			
			writer.write(sql.toString());
		} finally {
			writer.write("commit;\n");
			writer.flush();
			writer.close();
			reader.close();
		}
	}

	public static void insertSources(Connection conn, Long modelID, File sourceMetadata) throws IOException {
		DataFileDescriptor md = validateSourceMetadata(sourceMetadata);

		BufferedReader reader = new BufferedReader(new FileReader(sourceMetadata));
		BufferedWriter writer = makeOutWriter(sourceMetadata, "src_metadata_insert.sql");
		advancePastHeader(md, reader);
		
		// ----------------
		// GENERATE THE SQL
		// ----------------
		try {
			String line = null;
			int identifier = 0; // FACT: identifier is 1-based and autogenerated
			String[] allFields = {"SOURCE_ID", "NAME", "DESCRIPTION", "SORT_ORDER", "SPARROW_MODEL_ID", 
					"IDENTIFIER", "DISPLAY_NAME", "CONSTITUENT", "UNITS", "PRECISION", 
					"IS_POINT_SOURCE"};
			
			while ( (line=reader.readLine()) != null) {
				if (line.trim().length() == 0) continue;
				identifier++;
				// HACK: trailing "_" added because split() will drop trailing empty spaces
				String[] inputValues = (line + md.delimiter + "_").split(md.delimiter);
				assert(inputValues[md.indexOf("id")].equals(inputValues[md.indexOf("sort_order")]));
				
				List<String> values = new ArrayList<String>();
				{
					values.add(inputValues[md.indexOf("id")]); //SOURCE_ID has insert trigger using sequence SOURCE_SEQ
					values.add(quoteForSQL(inputValues[md.indexOf("name")]));
					values.add(quoteForSQL(inputValues[md.indexOf("description")])); 
					values.add(inputValues[md.indexOf("sort_order")]);
					values.add(Long.toString(modelID)); 
					values.add(Integer.toString(identifier));
					String displayName = useNameForDisplayNameIfUnavailable(md, inputValues);
					values.add(displayName);
					values.add(quoteForSQL(inputValues[md.indexOf("constituent")])); 
					values.add(quoteForSQL(inputValues[md.indexOf("units")]));
					String precision = useDefaultPrecisionIfUnavailable(md, inputValues);
					values.add(precision);
					String isPointSource = ModelDataAssumptions.translatePointSource(inputValues[md.indexOf("is_point_source")]);
					values.add(quoteForSQL(isPointSource));
				}
				
				
				String sql = "INSERT into " + JDBCUtil.SPARROW_SCHEMA 
				+ ".SOURCE (" + joinInParallel(allFields, ",", values) + ")" // 10
				+ " VALUES (" + join(values, ",") + ");\n";
				
				writer.write(sql);
			}
		} finally {
			writer.write("commit;\n");
			writer.flush();
			writer.close();
			reader.close();
		}
	}

	/**
	 * Following JDBCUtil.writeModelReaches()
	 * 
	 * @param conn
	 * @param modelID
	 * @param topoData
	 * @param ancillaryData
	 * @throws IOException
	 * @throws SQLException 
	 */
	public static void insertReaches(Connection conn, long modelID, File topoData, File ancillaryData) throws IOException, SQLException {
		DataFileDescriptor md = validateTopologicalData(topoData);		
		DataFileDescriptor amd = validateAncillaryData(ancillaryData);
		assert(md.lines == amd.lines);
		
		BufferedReader tReader = new BufferedReader(new FileReader(topoData));
		BufferedReader aReader = new BufferedReader(new FileReader(ancillaryData));
		BufferedWriter writer = makeOutWriter(topoData, "model_reaches_insert.sql");
		advancePastHeader(md, tReader);
		
		int stdIdMatchCount = 0;	//Number of reaches where the STD_ID matched a enh reach
		int stdIdNullCount = 0;		//Number of reaches where the STD_ID is null (actually, counting zero as null)
		int stdIdNotMatched = 0;	//Number of reaches where the STD_ID is assigned, but not matched.
		
		// ----------------
		// GENERATE THE SQL
		// ----------------
		try {
			String line = null;
			String aline = null;
			int identifier = 0; // FACT: identifier is 1-based and autogenerated
			advancePastHeader(amd, aReader);
			String[] allFields = {"IDENTIFIER","FULL_IDENTIFIER","HYDSEQ","IFTRAN","ENH_REACH_ID",
					"SPARROW_MODEL_ID","FNODE","TNODE"};
			Map<Integer, Integer> enhancedReachIDMap = readNetworkReaches();
			
			while ( (line=tReader.readLine()) != null) {
				aline = aReader.readLine();
				if (line.trim().length() == 0) {continue;}
				
				//identifier++;
				// HACK: trailing "_" added because split() will drop trailing empty spaces
				String[] topoInputValues = (line + md.delimiter + "_").split(md.delimiter);
				String[] ancilInputValues = (aline + amd.delimiter + "_").split(amd.delimiter);
				
				List<String> values = new ArrayList<String>();
				{
					values.add(ancilInputValues[amd.indexOf(REACH_IDENTIFIER)]);
					values.add(quoteForSQL(ancilInputValues[amd.indexOf(FULL_IDENTIFIER)]));
					values.add(topoInputValues[md.indexOf("hydseq")]);
					values.add(topoInputValues[md.indexOf("iftran")]);
					{
						String std_id = ancilInputValues[amd.indexOf(FULL_IDENTIFIER)];
						if (std_id == null || std_id.length() == 0 || std_id.equals("0")) {
							values.add(null);
							stdIdNullCount++; // no std_id or 0 std_id provided
						} else {
							Integer match = enhancedReachIDMap.get(Integer.valueOf(std_id));
							if (match != null) { 
								values.add(match.toString());
								stdIdMatchCount++; // successful match
							} else {
								values.add(null);
								stdIdNotMatched++; // no corresponding enh_id
							}
						}
					}
					values.add(Long.toString(modelID));
					values.add(topoInputValues[md.indexOf("fnode")]);
					values.add(topoInputValues[md.indexOf("tnode")]);
				}
				
				String sql = "INSERT INTO " + JDBCUtil.SPARROW_SCHEMA + ".MODEL_REACH "
				+ "(" + joinInParallel(allFields, ",", values) + ")"
				+ " VALUES (" + join(values, ",") + ");\n";
				
				writer.write(sql);
			}
		} finally {
			writer.write("commit;\n");
			writer.flush();
			writer.close();
			tReader.close();
			aReader.close();
		}
	}
	
	public static void insertReachDecayCoefs(Connection conn, Long modelID, File reachCoefdata, File ancilData) throws IOException, SQLException {
		assert(conn != null) : "Connection required to do id lookups";
		DataFileDescriptor md = validateCoefData(reachCoefdata);
		DataFileDescriptor amd = validateAncillaryData(ancilData);
		
		// -----
		// SETUP
		// -----
		BufferedReader dcReader = new BufferedReader(new FileReader(reachCoefdata));
		BufferedReader aReader = new BufferedReader(new FileReader(ancilData));
		BufferedWriter writer = makeOutWriter(reachCoefdata, "reach_decay_coef_insert.sql");
		Map<Integer, Integer> reachIDLookup = retrieveModelReachIDLookup(conn, modelID);
		advancePastHeader(md, dcReader);
		advancePastHeader(amd, aReader);
		
		// ----------------
		// GENERATE THE SQL
		// ----------------
		try {
			String line = null;
			String aLine = null;
			while ( (line=dcReader.readLine()) != null) {
				aLine = aReader.readLine();
				if (line.trim().length() == 0) continue;
				// HACK: trailing "_" added because split() will drop trailing empty spaces
				String[] inputValues = (line + md.delimiter + "_").split(md.delimiter);
				String[] ancilValues = (aLine + amd.delimiter + "_").split(amd.delimiter);

				String[] values = new String[5];
				{
					values[0] = inputValues[md.indexOf("ITER")];
					values[1] = inputValues[md.indexOf("INC_DELIVF")]; 
					values[2] = inputValues[md.indexOf("TOT_DELIVF")]; 
					values[3] = inputValues[md.indexOf("BOOT_ERROR")];

					Integer identifier = Integer.valueOf(ancilValues[amd.indexOf(REACH_IDENTIFIER)]);
					values[4] = Integer.toString(reachIDLookup.get(identifier));
				}
				
				String sql = "INSERT INTO REACH_COEF (ITERATION, INC_DELIVERY, TOTAL_DELIVERY, BOOT_ERROR, MODEL_REACH_ID) "
				+ " VALUES (" + StringUtils.join(values, ",") + ");\n";
				
				writer.write(sql);
			}
		} finally {
			writer.write("commit;\n");
			writer.flush();
			writer.close();
			dcReader.close();
			aReader.close();
		}
	}
	
	public static void insertSourceReachCoefs(Connection conn, Long modelID,
			File coefData, File sourceMetadata, File ancilData)
			throws IOException, SQLException {
		// ----------
		// VALIDATION
		// ----------
		assert(conn != null): "Connection required to do id lookups";
		DataFileDescriptor md = validateCoefData(coefData);
		validateSourceMetadata(sourceMetadata);
		DataFileDescriptor amd = validateAncillaryData(ancilData);
		
		String[] sources = readSourceNames(sourceMetadata);
		String[] cappedSources = ModelDataAssumptions.addPrefixAndCapitalize(sources);
		assert(md.hasColumns(cappedSources));

		// -----
		// SETUP
		// -----
		BufferedReader coefReader = new BufferedReader(new FileReader(coefData));
		BufferedReader ancilReader = new BufferedReader(new FileReader(ancilData));
		BufferedWriter writer = makeOutWriter(coefData, "src_reach_coef_insert.sql");
		Map<Integer, Integer> reachIDLookup = retrieveModelReachIDLookup(conn, modelID);
		Map<Integer, Integer> sourceIDLookup = retrieveSourceIDLookup(conn, modelID);
		advancePastHeader(md, coefReader);
		advancePastHeader(amd, ancilReader);
		
		// ----------------
		// GENERATE THE SQL
		// ----------------
		try {
			String line = null;
			String aLine = null;

			int lineCount = 0;
			
			while ( (line=coefReader.readLine()) != null) {
				// complications to handle the possibility that ancil must be
				// looped once for each iteration
				aLine = ancilReader.readLine();
				if (aLine != null && aLine.trim().length() == 0) {
					aLine = ancilReader.readLine();
				}
				if (aLine == null) {
					ancilReader.close();
					// reopen the file
					ancilReader = new BufferedReader(new FileReader(ancilData));
					advancePastHeader(amd, ancilReader);
					aLine = ancilReader.readLine();
				}
				if (line.trim().length() == 0 && aLine.trim().length() == 0) continue;
				
				// HACK: trailing "_" added because split() will drop trailing empty spaces
				String[] inputValues = (line + md.delimiter + "_").split(md.delimiter);
				String[] ancilValues = (aLine + amd.delimiter + "_").split(amd.delimiter);
				
				Integer identifier = Integer.valueOf(ancilValues[amd.indexOf(REACH_IDENTIFIER)]);
				Integer modelReachID = reachIDLookup.get(identifier);
				String modelReachIDString = Integer.toString(modelReachID);
				
				for (Integer i=1; i <=sourceIDLookup.size(); i++) {
					// source ids are 1-based.
					String[] values = new String[4];
					values[0] = inputValues[md.indexOf("ITER")];
					values[1] = inputValues[BASIC_COEF_COLUMNS.length + i];
					values[2] = Integer.toString(sourceIDLookup.get(i));
					values[3] = modelReachIDString;
					
					// NOTE: SOURCE_REACH_COEF_SEQ autoinserts value of SOURCE_REACH_COEF_ID
					String sql = "INSERT into " + JDBCUtil.SPARROW_SCHEMA 
					+ ".SOURCE_REACH_COEF (ITERATION, VALUE, SOURCE_ID, MODEL_REACH_ID)"
					+ " VALUES (" + StringUtils.join(values, ",") + ");\n";
					
					writer.write(sql);
				}
			}
		} finally {
			writer.write("commit;\n");
			writer.flush();
			writer.close();
			coefReader.close();
			ancilReader.close();
		}
	}
	
	public static void insertSourceValues(Connection conn, Long modelID, 
			File sourceValuesData, File sourceMetadata, File ancilData)
			throws IOException, SQLException {
		// ----------
		// VALIDATION
		// ----------
		assert(conn != null) : "Connection required to do id lookups";
		DataFileDescriptor md = validateSourceData(sourceValuesData);
		DataFileDescriptor smd = validateSourceMetadata(sourceMetadata);
		DataFileDescriptor amd = validateAncillaryData(ancilData);
		
		String[] sources = readSourceNames(sourceMetadata);
		assert(Arrays.equals(md.getHeaders(), sources)) : "the order should be the same";

		// -----
		// SETUP
		// -----
		BufferedReader sourceReader = new BufferedReader(new FileReader(sourceValuesData));
		BufferedReader ancilReader = new BufferedReader(new FileReader(ancilData));
		BufferedWriter writer = makeOutWriter(sourceValuesData, "src_value_insert.sql");
		
		Map<Integer, Integer> reachIDLookup = retrieveModelReachIDLookup(conn, modelID);
		Map<Integer, Integer> sourceIDLookup = retrieveSourceIDLookup(conn, modelID);
		advancePastHeader(md, sourceReader);
		advancePastHeader(amd, ancilReader);
		
		// ----------------
		// GENERATE THE SQL
		// ----------------
		try {
			String line = null;
			String aLine = null;

			int lineCount = 0;
			
			
			while ( (line=sourceReader.readLine()) != null) {
				aLine = ancilReader.readLine();
				if (line.trim().length() == 0 && aLine.trim().length() == 0) continue;
				
				// HACK: trailing "_" added because split() will drop trailing empty spaces
				String[] inputValues = (line + md.delimiter + "_").split(md.delimiter);
				String[] ancilValues = (aLine + amd.delimiter + "_").split(amd.delimiter);
				
				Integer identifier = Integer.valueOf(ancilValues[amd.indexOf(REACH_IDENTIFIER)]);
				Integer modelReachID = reachIDLookup.get(identifier);
				String modelReachIDString = Integer.toString(modelReachID);
				
				for (Integer i=1; i <=sourceIDLookup.size(); i++) {
					// source ids are 1-based.
					String[] values = new String[3];
					values[0] = inputValues[i-1];
					values[2] = Integer.toString(sourceIDLookup.get(i));
					values[2] = modelReachIDString;
					
					// NOTE: SOURCE_REACH_COEF_SEQ autoinserts value of SOURCE_REACH_COEF_ID
					String sql = "INSERT INTO source_value (VALUE, SOURCE_ID, MODEL_REACH_ID) "
					+ " VALUES (" + StringUtils.join(values, ",") + ");\n";
					
					writer.write(sql);
				}
			}
		} finally {
			writer.write("commit;\n");
			writer.flush();
			writer.close();
			sourceReader.close();
			ancilReader.close();
		}
	}
	
	public static void checkSequenceConsistency() {
		String sql = "    select 'MODEL_REACH.MODEL_REACH_ID' as sequence_name, 'MAX' as type, max(model_reach_id) as val from model_reach "
		+ "union all "
		+ "    select sequence_name, 'SEQUENCE' as type, last_number as val from user_sequences "
		+ "        where sequence_name = 'MODEL_REACH_SEQ' "
		+ "union all "
		+ "    select 'REACH_COEF.REACH_COEF_ID' as sequence_name, 'MAX' as type, max(REACH_COEF_ID) as val from REACH_COEF "
		+ "union all "
		+ "    select sequence_name, 'SEQUENCE' as type, last_number as val from user_sequences "
		+ "        where sequence_name = 'REACH_COEF_SEQ' "
		+ "union all "
		+ "    select 'SOURCE_REACH_COEF.SOURCE_REACH_COEF_ID' as sequence_name, 'MAX' as type, max(SOURCE_REACH_COEF_ID) as val from SOURCE_REACH_COEF "
		+ "union all "
		+ "    select sequence_name, 'SEQUENCE' as type, last_number as val from user_sequences "
		+ "        where sequence_name = 'SOURCE_REACH_COEF_SEQ' "
		+ "union all "
		+ "    select 'SOURCE.SOURCE_ID' as sequence_name, 'MAX' as type, max(SOURCE_ID) as val from SOURCE "
		+ "union all "
		+ "    select sequence_name, 'SEQUENCE' as type, last_number as val from user_sequences "
		+ "        where sequence_name = 'SOURCE_SEQ' "
		+ "union all "
		+ "    select 'SOURCE_VALUE.SOURCE_VALUE_ID' as sequence_name, 'MAX' as type, max(SOURCE_VALUE_ID) as val from SOURCE_VALUE "
		+ "union all "
		+ "    select sequence_name, 'SEQUENCE' as type, last_number as val from user_sequences "
		+ "        where sequence_name = 'SOURCE_VALUE_SEQ' "
		+ "union all "
		+ "    select 'SPARROW_MODEL.SPARROW_MODEL_ID' as sequence_name, 'MAX' as type, max(SPARROW_MODEL_ID) as val from SPARROW_MODEL "
		+ "union all "
		+ "    select sequence_name, 'SEQUENCE' as type, last_number as val from user_sequences "
		+ "        where sequence_name = 'SPARROW_MODEL_SEQ' ";
	}
	
	// ==========
	// VALIDATION
	// ==========
	
	public static DataFileDescriptor validateSourceData(File srcValData)
	throws IOException {
		assert(srcValData !=  null && srcValData.exists());
		assert(srcValData.getName().equals("src.txt"));
		DataFileDescriptor md = Analyzer.analyzeFile(srcValData);
		assert(md.hasColumnHeaders());
		return md;
	}
	
	public static DataFileDescriptor validateCoefData(File reachCoefdata)
			throws IOException {
		assert(reachCoefdata !=  null && reachCoefdata.exists());
		assert(reachCoefdata.getName().equals("coef.txt"));
		DataFileDescriptor md = Analyzer.analyzeFile(reachCoefdata);
		assert(md.hasColumnHeaders());
		assert(md.hasColumns(BASIC_COEF_COLUMNS));
		return md;
	}
	public static DataFileDescriptor validateModelMetadata(File modelMetadata)
			throws IOException {
		assert (modelMetadata != null && modelMetadata.exists());
		assert (modelMetadata.getName().equals("model_metadata.txt"));
		DataFileDescriptor fileMetaData = Analyzer.analyzeFile(modelMetadata);
		assert (fileMetaData.hasColumnHeaders());
		assert (fileMetaData.hasColumns("network", "name", "description",
				"constituent", "units", "precision"));
		return fileMetaData;
	}
	
	public static DataFileDescriptor validateSourceMetadata(File sourceMetadata)
			throws IOException {
		assert (sourceMetadata !=  null && sourceMetadata.exists());
		assert(sourceMetadata.getName().equals("src_metadata.txt"));
		DataFileDescriptor md = Analyzer.analyzeFile(sourceMetadata);
		assert(md.hasColumnHeaders());
		assert(md.hasColumns("id", "sort_order", "name", "display_name", "description", 
				"constituent", "units", "precision", "is_point_source"));
		return md;
	}
	
	public static DataFileDescriptor validateTopologicalData(File topoData)
			throws IOException {
		assert(topoData != null && topoData.exists());
		assert(topoData.getName().equals("topo.txt"));
		DataFileDescriptor tmd = Analyzer.analyzeFile(topoData);
		assert(tmd.hasColumnHeaders());
		assert(tmd.hasColumns("fnode", "tnode", "iftran", "hydseq"));
		return tmd;
	}
	public static DataFileDescriptor validateAncillaryData(File ancillaryData)
			throws IOException {
		assert(ancillaryData != null && ancillaryData.exists());
		assert(ancillaryData.getName().equals("ancil.txt"));
		DataFileDescriptor amd = Analyzer.analyzeFile(ancillaryData);
		assert(amd.hasColumnHeaders());
		assert(amd.hasColumns(REACH_IDENTIFIER, FULL_IDENTIFIER, "new_or_modified", "waterid", "hydseq", 
				"demiarea", "demtarea", "meanq", "delivery_target", "RR", 
				"CONTFLAG", "PNAME", "HEADFLAG", "TERMFLAG", "RESCODE", 
				"RCHTYPE", "station_id", "statid", "del_frac"));
		return amd;
	}
	
	public static Map<Integer, Integer> retrieveModelReachIDLookup(
			Connection conn, long modelID) throws SQLException {
		// NOTE: unlike the other functions, this one MUST hit the database, as
		// the model_reach_ids are generally generated via a sequence in an
		// insert trigger
		String selectAllReachesQuery = "SELECT IDENTIFIER, MODEL_REACH_ID FROM " + JDBCUtil.SPARROW_SCHEMA + ".MODEL_REACH WHERE SPARROW_MODEL_ID = " 
		+ modelID;	
		Map<Integer, Integer> modelIdMap = JDBCUtil.buildIntegerMap(conn, selectAllReachesQuery);
		return modelIdMap;
	}
	
	public static Map<Integer, Integer> retrieveSourceIDLookup(
			Connection conn, long modelID) throws SQLException {
		// NOTE: unlike the other functions, this one MUST hit the database, as
		// the source_ids are generally generated via a sequence in an
		// insert trigger
		String sourceMapQuery = "SELECT IDENTIFIER, SOURCE_ID FROM " + JDBCUtil.SPARROW_SCHEMA + ".SOURCE WHERE SPARROW_MODEL_ID = " + modelID + " ORDER BY SORT_ORDER";
		Map<Integer, Integer> sourceIdMap = JDBCUtil.buildIntegerMap(conn, sourceMapQuery);
		// ordered map necessary here (but probably can be avoided.
		TreeMap<Integer, Integer> result = new TreeMap<Integer, Integer>();
		result.putAll(sourceIdMap);
		return result;
	}
	
	// ==============
	// LOOKUP METHODS
	// ==============
	private static String[] readSourceNames(File sourceMetadata) throws IOException {
		if (sourceMetadata ==  null || !sourceMetadata.exists()) return null;
		assert(sourceMetadata.getName().equals("src_metadata.txt"));
		DataFileDescriptor md = Analyzer.analyzeFile(sourceMetadata);
		assert(md.hasColumnHeaders());
		assert(md.hasColumns("id", "sort_order", "name", "display_name", "description", 
				"constituent", "units", "precision", "is_point_source"));
		
		BufferedReader reader = new BufferedReader(new FileReader(sourceMetadata));
		List<String> headers = new ArrayList<String>();
		try {
			String line = null;
			int identifier = 0; // FACT: identifier is 1-based and autogenerated
								advancePastHeader(md, reader);
			while ( (line=reader.readLine()) != null) {
				if (line.trim().length() == 0) continue;
				String[] inputValues = (line + md.delimiter + "_").split(md.delimiter);

				headers.add(inputValues[md.indexOf("name")]);
			}

		} finally {
			reader.close();
		}
		return headers.toArray(new String[headers.size()]);
	}
	
	public static Map<Integer, Integer> readNetworkReaches() throws SQLException {
		String enhReachQuery = "SELECT IDENTIFIER, ENH_REACH_ID FROM " + JDBCUtil.NETWORK_SCHEMA + ".ENH_REACH WHERE ENH_NETWORK_ID = " 
		+ ModelDataAssumptions.ENH_NETWORK_ID;
		return JDBCUtil.buildIntegerMap(PROD_CONN, enhReachQuery);
	}
	
	// ===============
	// UTILITY METHODS
	// ===============
	private static BufferedWriter makeOutWriter(File sourceValuesData, String fileName)
	throws IOException {
		String sqlOutputFilePath = sourceValuesData.getParent() + "/" + fileName;
		File outputFile = new File(sqlOutputFilePath);
		return new BufferedWriter(new FileWriter(outputFile), 8192);
	}
	
	private static void advancePastHeader(DataFileDescriptor md,
			BufferedReader coefReader) throws IOException {
		String line;
		if (md.hasColumnHeaders()) {
			line = coefReader.readLine();
		}
	}
	
	public static String quoteForSQL(String value) {
		if (value == null || value.length() == 0) return null;
		StringBuffer quotedString = new StringBuffer("'");
		// escape all singlequotes for SQL
		quotedString.append(value.replaceAll("'", "''")).append("'");
		return quotedString.toString();
	}
	
	public static StringBuffer join(List<String> values, String delimiter) {
		delimiter = (delimiter == null)? ",": delimiter;
		StringBuffer result = new StringBuffer();
		for (int i=0; i<values.size(); i++) {
			String value = values.get(i);
			if (value != null && value.length() > 0) {
				if (result.length() > 0) {
					result.append(delimiter);
				}
				result.append(value);
			}
		}
		return result;
	}
	
	public static StringBuffer joinInParallel(String[] values, String delimiter, List<String> governors) {
		delimiter = (delimiter == null)? ",": delimiter;
		StringBuffer result = new StringBuffer();
		for (int i=0; i<governors.size(); i++) {
			String governor = governors.get(i);
			if (governor != null && governor.length() > 0) {
				if (result.length() > 0) {
					result.append(delimiter);
				}
				result.append(values[i]);
			}
		}
		return result;
	}
	
	/**
	 * TODO: BAD BAD BAD EVIL EVIL EVIL 
	 * @return
	 * @throws SQLException
	 */
	public static Connection getProductionConnection(){
		try {
			DriverManager.registerDriver(new OracleDriver());		

			String username = "SPARROW_DSS";
			String password = "***REMOVED***";
			String thinConn = "jdbc:oracle:thin:@130.11.165.152:1521:widw";

			return DriverManager.getConnection(thinConn,username,password);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace(System.err);
		}
		return null;
	}
	
	public static Connection getDevelopmentConnection() {
		try {
			DriverManager.registerDriver(new OracleDriver());		

			String username = "SPARROW_DSS";
			String password = "admin";
			String thinConn = "jdbc:oracle:thin:@localhost:1521:xe";

			return DriverManager.getConnection(thinConn,username,password);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace(System.err);
		}
		return null;
	}
}