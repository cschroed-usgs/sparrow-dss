package gov.usgswim.sparrow.validation;

import gov.usgswim.sparrow.validation.tests.ModelValidationResult;
import gov.usgswim.sparrow.validation.tests.ModelValidator;
import gov.usgswim.sparrow.LifecycleListener;
import gov.usgswim.sparrow.action.LoadModelMetadata;
import gov.usgswim.sparrow.service.SharedApplication;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.extras.DOMConfigurator;

/**
 * This test was created to recreate a calc error where delivery based calcs
 * (like total delivered flux) were resulting in mapped values identical
 * to the base data (i.e. total flux).
 * 
 * @author eeverman
 */
public class SparrowModelValidationRunner {
	
	static {
		// Set up a simple configuration that logs on the console.
		
		URL log4jUrl = SparrowModelValidationRunner.class.getResource("/log4j_test.xml");
		LogManager.resetConfiguration();
		DOMConfigurator.configure(log4jUrl);
	}
	
	/**
	 * The required comparison accuracy (expected - actual)/(max(expected, actual))
	 * This value is slightly relaxed for values less than 1.
	 */
	final double REQUIRED_COMPARISON_FRACTION = .001d;	//comp fraction
	
	protected static Logger log = null;
	
	
	public final static String ID_COL_KEY = "id_col";	//Table property of the key column
	
	//final static int NUMBER_OF_BAD_INCREMENTALS_TO_PRINT = 5;
	//final static int NUMBER_OF_BAD_TOTALS_TO_PRINT = 6;
	//The 'print all non-matching values' option:
	final static int NUMBER_OF_BAD_INCREMENTALS_TO_PRINT = Integer.MAX_VALUE;
	final static int NUMBER_OF_BAD_TOTALS_TO_PRINT = Integer.MAX_VALUE;
	
	final static String QUIT = "quit";
	
	String singleModelPath;
	String activeModelDirectory;
	List<Long> modelIds;
	String dbPwd;
	
	int firstModelId = -1;
	int lastModelId = -1;
	
	
	/**
	 * Error: Print single line messages for each row error.  No - additional (multi-line) details.
	 * Warn:	In addition to errors, print single line warnings for rows that are suspicious.
	 * Debug:	If available, print multi-line detail for each error.
	 * Trace:	Used to debug the tests themselves, this option prints additional info about successful values as well.
	 */
	protected Level logLevel;
	
	private List<ModelValidator> validators = new ArrayList<ModelValidator>();
	
	//Application lifecycle listener handles startup / shutdown
	static LifecycleListener lifecycle = new LifecycleListener();
	
 /**
	* The SparrowModelValidationRunner subclass name must be passed as the first
	* arg.  That subclass must override the loadMOdelValidators method to add
	* its suite of validators.
	* 
	* @param args
	* @throws Exception 
	*/
	public static void main(String[] args) throws Exception {

		log = Logger.getLogger(SparrowModelValidationRunner.class); //logging for this class
	
		String runnerToRun = args[0];
		SparrowModelValidationRunner runner = (SparrowModelValidationRunner) SparrowModelValidationRunner.class.forName(runnerToRun).newInstance();
		runner.loadModelValidators();
		
		if (runner.getValidators().isEmpty()) {
			log.fatal("No validators were found.  Subclass SparrowModelValidationRunner to override the loadModelValidators method to add some.");
			return;
		} else {
			log.info("Found " + runner.getValidators().size() + " validation tests to run against the models.");
		}
		
		
		
		
		runner.oneTimeUserInput();
		runner.initSystemConfig();
		if (runner.initModelValidators()) {
			runner.run();
		} else {
			log.fatal("Unable to run any models or tests.");
		}
	}
	
	
	public ModelValidationResult run() {
		
		ModelValidationResult result;

		log.info("*****************************************");
		if (singleModelPath != null) {
			log.info(" ************ Running one model from a text file ************");
			log.info("*************************************************************");
			
			Long id = getModelIdFromPath(singleModelPath);
			
			if (id != null) {
				result = runOneModel(id);
			} else {
				result = new ModelValidationResult();
				result.configError = true;
			}
		} else if (modelIds != null && modelIds.size() > 0) {
			log.info(" ****** Running models from a list of ids (no text file) ********");
			log.info("*****************************************************************");
			
			result = runListOfModels(modelIds);

		} else {
			
			log.info("************ Running a directory of models ************");
			log.info("*****************************************");
			
			result = runOneModelDirectory(this.activeModelDirectory, this.firstModelId, this.lastModelId);
			
		}
		

		if (result.modelsRun > 0 && result.modelsFailed == 0) {
			log.debug("+ + + + + EVERYTHING LOOKS GREAT! + + + + +");
			log.debug("+ + + + + " + result.modelsRun + " models run. + + + + +");
		} else if (result.modelsFailed > 0) {
			log.error("- - - - - AT LEAST ONE MODEL FAILED VALIDATION.  PLEASE REVIEW THE LOG MESSAGES TO FIND THE VALIDATION ERRORS. + + + + +");
			log.debug("- - - - - " + result.modelsRun + " models run. - - - - - ");
			log.debug("- - - - - " + result.modelsFailed + " models FAILED. - - - - - ");
		} else {
			log.error("- - - - - NO MODELS WERE FOUND.  PLEASE CHECK PATH INFO. - - - - - ");
		}
		
		log.info("*****************************************");
		log.info("************ Run Complete ***************");
		log.info("*****************************************");
		
		return result;
	}
	
	/**
	 * Returns the number of failures
	 * @param file
	 * @return 
	 */
	protected ModelValidationResult runOneModel(Long id) {
			
		ModelValidationResult result = new ModelValidationResult();
		
		for (ModelValidator v : validators) {
			try {
				
				v.beforeEachTest(id);
				result.addTestToSingleModelTotal(v.testModel(id));
				v.afterEachTest(id);
				
			} catch (Exception ex) {
				log.error("Failed while running the test " + v.getClass().getCanonicalName(), ex);
			}
		}

		return result;	
	}
	
	protected Long getModelIdFromPath(String path) {
		String idString = path.substring(0, path.lastIndexOf('.'));
		idString = idString.substring(idString.lastIndexOf(File.separatorChar) + 1);
		Long id = null;
		try {
			id = Long.parseLong(idString);
			return id;
		} catch (Exception e) {
			log.fatal("Couldn't figure out the model number from the model path: " + path);
			return null;
		}
	}
	
	protected ModelValidationResult runOneModelDirectory(String modelDirectory, long firstModelIdNumber, long lastModelIdNumber) {
		ModelValidationResult result = new ModelValidationResult();

		for (long id = firstModelIdNumber; id <= lastModelIdNumber; id++) {
			ModelValidationResult oneResult = runOneModel(id);
			result.addModelToTotal(oneResult);
			result.addTestToTotal(oneResult);
		}

		return result;
	}
	
	protected ModelValidationResult runListOfModels(List<Long> ids) {
		ModelValidationResult result = new ModelValidationResult();

		for (long id : ids) {
			ModelValidationResult oneResult = runOneModel(id);
			result.addModelToTotal(oneResult);
			result.addTestToTotal(oneResult);
		}

		return result;
	}
	

	public void addValidator(ModelValidator validator) {
		validators.add(validator);
	}
	
	public boolean oneTimeUserInput() throws Exception {
		
		
		promptIntro();
		
		
		
		boolean requiresDb = false;
		boolean requiresText = false;
		
		for (ModelValidator v : validators) {
			requiresDb = requiresDb || v.requiresDb();
			requiresText = requiresText || v.requiresTextFile();
		}
		
		if (requiresText) {
			promptPathOrDir();
			
			if (singleModelPath != null) {
				File f = new File(singleModelPath);
				if (! f.exists()) {
				log.fatal("Oops, the model path '" + singleModelPath + "' does not seem to exist.");
				return false;
				}
			} else if (activeModelDirectory != null) {
				File f = new File(activeModelDirectory);
				if (! f.exists()) {
				log.fatal("Oops, the directory '" + activeModelDirectory + "' does not seem to exist.");
				return false;
				}
			} else {
				log.fatal("A model path or a directory must be specified.");
				return false;
			}
		} else {
			//If text files are not involved, we need a list of model ids
			prompModelIds();
		}
		
		if (requiresDb) {
			promptPwd();
			intiDbConfig();
			
			try {
				Connection conn = SharedApplication.getInstance().getROConnection();
				conn.close();
			} catch (Exception e) {
				log.fatal("Oops, a bad pwd, or lack of network access to the db?", e);
				return false;
			}
		}
		
		promptLogDetail();
		return true;

	}
		
	
	public void loadModelValidators() {
		//override to add validators
	}
	
	public boolean initModelValidators() {
		
		boolean ok = true;
		
		for (ModelValidator mv : getValidators()) {
			
			try {
				
				ok = mv.initTest(this, false);
				if (! ok) return false;
				
			} catch (Exception e) {
				log.error("Unable to initiate the test: " + mv.getClass(), e);
				return false;
			}

		}
		
		return true;
	}
	
	protected List<ModelValidator> getValidators() {
		return validators;
	}
	
	
	

	public void intiDbConfig() {

		
		//Production Properties
		System.setProperty("dburl", "jdbc:oracle:thin:@130.11.165.152:1521:widw");
		System.setProperty("dbuser", "sparrow_dss");
		System.setProperty("dbpass", dbPwd);
		
	}
	
	public void initLoggingConfig() {
		//Turn off logging for the lifecycle
		Logger.getLogger(LifecycleListener.class).setLevel(Level.ERROR);
	}
	
	protected void initSystemConfig() {
		
		if (logLevel != null) {
			Logger baseLogger = Logger.getLogger("gov.usgswim.sparrow.validation");
			baseLogger.setLevel(logLevel);
		}
				
				
		//Tell JNDI config to not expect JNDI props
		System.setProperty(
				"gov.usgs.cida.config.DynamicReadOnlyProperties.EXPECT_NON_JNDI_ENVIRONMENT",
				"true");
		
		System.setProperty(LoadModelMetadata.SKIP_LOADING_PREDEFINED_THEMES, "true");
		
		
		lifecycle.contextInitialized(null, true);
	}
	
	
	public void promptIntro() {
		System.out.println("- - Welcome to the new and improved model validator - -");
		System.out.println("The validator works in two modes:");
		System.out.println("1) Test a single model by entering the complete path to a single model (ending with .txt), or");
		System.out.println("2) Test several models from a directory by entering a directoy (ending with '/' or '\\')");
		System.out.println("If you enter a directory, you will be prompted for a start and end model number.");
		System.out.println("Enter 'quit' for any response to stop.");
		System.out.println("");
	}
	
	public void promptPathOrDir() {
		String pathOrDir  = prompt("Enter a direcotry containing models or the complete path to a single model:");
		
		pathOrDir = pathOrDir.trim();
		if (QUIT.equalsIgnoreCase(pathOrDir)) return;

		if (pathOrDir.endsWith(".txt")) {
			//we have a single path
			singleModelPath = pathOrDir;
		} else if (pathOrDir.endsWith(File.separator)) {
			activeModelDirectory = pathOrDir;
			promptFirstLastModel();
		} else {
			System.out.println("What?? - please try again.");
			promptPathOrDir();
		}
	}
		 
	public void promptLogDetail() {
		
		System.out.println("");
		System.out.println("Select Logging Level using the first letter of these options:");
		System.out.println("* Error: (Default) Print single line messages for each row error.  No - additional (multi-line) details.");
		System.out.println("* Warn:	In addition to errors, print single line warnings for rows that are suspicious.");
		System.out.println("* Debug:	If available, print multi-line detail for each error.");
		System.out.println("* Trace:	Used to debug the tests themselves, this option prints additional info about successful values as well.");
		
		String level  = prompt("Logging Level (E/W/D/T) or [Enter] to use the default 'Error' Level:");
		
		level = level.trim();
		if (QUIT.equalsIgnoreCase(level)) return;

		
		if ("".equals(level) || "e".equalsIgnoreCase(level)) {
			logLevel = Level.ERROR;
		} else if ("w".equalsIgnoreCase(level)) {
			logLevel = Level.WARN;
		} else if ("d".equalsIgnoreCase(level)) {
			logLevel = Level.DEBUG;
		} else if ("t".equalsIgnoreCase(level)) {
			logLevel = Level.TRACE;
		} else {
			System.out.println("Hmm, that was exactly understand that, but I'll take it to be the ERROR log level.");
			logLevel = Level.ERROR;
		}
		
	}
	
	public void promptFirstLastModel() {
		try {
			String firstIdStr  = prompt("Enter the ID of the first model to test:");
			if (QUIT.equalsIgnoreCase(firstIdStr)) return;
			firstModelId = Integer.parseInt(firstIdStr);
			
			String lastIdStr  = prompt("Enter the ID of the last model to test:");
			if (QUIT.equalsIgnoreCase(lastIdStr)) return;
			lastModelId = Integer.parseInt(lastIdStr);
		} catch (Exception e) {
			System.out.println("I really need numbers for this part.  Lets try that again.");
			promptFirstLastModel();
		}
	}
	
	public void prompModelIds() {
		try {
			String idStrs  = prompt("Enter a list of model IDs, separated by a comma and/or space:");
			if (QUIT.equalsIgnoreCase(idStrs)) return;
			
			String[] idStrArray = StringUtils.split(idStrs, ", \t");
			modelIds = new ArrayList<Long>();
			for (String s : idStrArray) {
				modelIds.add(Long.parseLong(s));
			}
			
			
		} catch (Exception e) {
			System.out.println("I really need a list of IDs.  Lets try that again.");
			prompModelIds();
		}
	}
	
	public void promptPwd() {
		String pwd = prompt("Enter the db password:");
		if (QUIT.equalsIgnoreCase(pwd)) return;
		dbPwd = pwd;
	}
	
	public Level getTestLogLevel() {
		return logLevel;
	}
	
	
	public static String prompt(String prompt) {
	
			      //  prompt the user to enter their name
		  System.out.print(prompt);
		
		  //  open up standard input
		  BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		
		  String val = null;
		
		  //  read the username from the command-line; need to use try/catch with the
		  //  readLine() method
		  try {
		     val = br.readLine();
		  } catch (IOException ioe) {
		     System.out.println("IO error trying to read input!");
		     System.exit(1);
		  } finally {
			  //br.close();
		  }
		  
		  return val;
	  }
	

	
}

