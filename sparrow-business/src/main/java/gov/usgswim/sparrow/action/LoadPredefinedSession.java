package gov.usgswim.sparrow.action;

import gov.usgswim.sparrow.domain.IPredefinedSession;
import gov.usgswim.sparrow.domain.PredefinedSessionBuilder;
import gov.usgswim.sparrow.domain.PredefinedSessionType;
import gov.usgswim.sparrow.request.PredefinedSessionUniqueRequest;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;
import gov.usgswim.sparrow.domain.PredefinedSessionTopic;

/**
 * Note that the RW connection is used b/c it is likely pointed to a transactional
 * db separate from the RO database.
 * 
 * @author eeverman
 *
 */
public class LoadPredefinedSession extends Action<IPredefinedSession> {

	/**
	 * A request for a uniquely defined session
	 */
	PredefinedSessionUniqueRequest request;
	


	public LoadPredefinedSession(PredefinedSessionUniqueRequest request) {
		this.request = request;
	}
	

	/**
	 * @return ImmutableList of immutable SparrowModels
	 */
	@Override
	public IPredefinedSession doAction() throws Exception {


		Map<String, Object> paramMap = new HashMap<String, Object>();
		String queryName = null;	//The name of the query to run
		
		if (request.getId() != null) {
			//Select all records for a single model
			paramMap.put("ID", request.getId());
			queryName = "LoadById";
		} else if (request.getUniqueCode() != null) {
			//Select a single record based on the code
			paramMap.put("UNIQUE_CODE", request.getUniqueCode());
			queryName = "LoadByUniqueCode";
		} else{
			throw new Exception("Either the id or the uniqueCode must not be null");
		}

		PreparedStatement selectSessions = null;
		ResultSet rset = null;
		List<IPredefinedSession> sessions = new ArrayList<IPredefinedSession>();

		selectSessions = getRWPSFromPropertiesFile(queryName, null, paramMap);


		rset = selectSessions.executeQuery();
		addResultSetForAutoClose(rset);
		sessions = hydrate(rset);
		
		if (sessions.size() == 0) {
			throw new Exception("Could not find the PredefinedSession.");
		}



		//Returns an ImmutableList of immutable SparrowModels.
		return sessions.get(0);
	}
	
	public List<IPredefinedSession> hydrate(ResultSet rset) throws Exception {

		List<IPredefinedSession> sessions = new ArrayList<IPredefinedSession>();
		
		while (rset.next()) {
			PredefinedSessionBuilder s = new PredefinedSessionBuilder();
			s.setId(rset.getLong("PREDEFINED_SESSION_ID"));
			s.setUniqueCode(rset.getString("UNIQUE_CODE"));
			s.setModelId(rset.getLong("SPARROW_MODEL_ID"));
			
			String strType = rset.getString("PREDEFINED_TYPE");
			PredefinedSessionType sType = PredefinedSessionType.valueOf(strType);
			s.setPredefinedSessionType(sType);
			
			String strApproved = rset.getString("APPROVED");
			boolean approved = "T".equals(strApproved);
			s.setApproved(approved);
			
			s.setName(rset.getString("NAME"));
			s.setDescription(rset.getString("DESCRIPTION"));
			s.setSortOrder(rset.getInt("SORT_ORDER"));
			s.setContextString(rset.getString("CONTEXT_STRING"));
			s.setAddDate(rset.getDate("ADD_DATE"));
			s.setAddBy(rset.getString("ADD_BY"));
			s.setAddNote(rset.getString("ADD_NOTE"));
			s.setAddContactInfo(rset.getString("ADD_CONTACT_INFO"));
			s.setGroupName(rset.getString("GROUP_NAME"));
			s.setTopic(PredefinedSessionTopic.valueOf(rset.getString("TOPIC")));
			
			
			sessions.add(s.toImmutable());
		}


		//Returns an ImmutableList of immutable SparrowModels.
		return ImmutableList.copyOf(sessions);
	}
	
		}
