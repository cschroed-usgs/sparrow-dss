package gov.usgswim.sparrow.action;

import gov.usgswim.sparrow.domain.ReachGeometry;
import gov.usgswim.sparrow.request.ReachClientId;
import gov.usgswim.sparrow.request.ReachID;
import gov.usgswim.sparrow.service.idbypoint.ReachInfo;

import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

public class LoadReachByID extends Action<ReachInfo> {



	protected ReachClientId reachClientId;
	
	public LoadReachByID() {
		//default
	}
	
	public LoadReachByID(ReachClientId reachId) {
		this.reachClientId = reachId;
	}
	
	public ReachClientId getReachId() {
		return reachClientId;
	}

	public void setReachId(ReachClientId reachId) {
		this.reachClientId = reachId;
	}
	
	@Override
	public ReachInfo doAction() throws Exception {
		
		ReachInfo result = null;
		ResultSet rs = null;
		
		
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("ModelId", this.reachClientId.getModelID());
		params.put("Identifier", this.reachClientId.getReachClientId());
		
		rs = getROPSFromPropertiesFile("LoadScalerAttribsByFullId", getClass(), params).executeQuery();

		
		addResultSetForAutoClose(rs);
		
		//Scaler attribs we need to temp store
		String reachName = null;
		String identifier = null;
		String huc2 = null;
		String huc2name = null;
		String huc4 = null;
		String huc4name = null;
		String huc6 = null;
		String huc6name = null;
		String huc8 = null;
		String huc8name = null;
		
		if (rs.next()) {
			
			reachName = rs.getString("reach_name");
			identifier = rs.getString("identifier");
			huc2 = rs.getString("HUC2");
			huc2name = rs.getString("HUC2_NAME");
			huc4 = rs.getString("HUC4");
			huc4name = rs.getString("HUC4_NAME");
			huc6 = rs.getString("HUC6");
			huc6name = rs.getString("HUC6_NAME");
			huc8 = rs.getString("HUC8");
			huc8name = rs.getString("HUC8_NAME");
		}
		
		rs.close();
		
		LoadReachCatchmentDetail catchAction = new LoadReachCatchmentDetail();
		
		Long reachIdLong = Long.parseLong(identifier);
		ReachID reachId = new ReachID(reachClientId.getModelID(), reachIdLong);
		catchAction.setReach(reachId);
		ReachGeometry reachGeom = catchAction.run();
		
		result = new ReachInfo(
				reachClientId.getModelID(), reachIdLong, reachClientId.getReachClientId(), reachName, null,
				reachGeom,
				huc2, huc2name, huc4, huc4name,
				huc6, huc6name, huc8, huc8name);
		
		
		return result;
	}


}
