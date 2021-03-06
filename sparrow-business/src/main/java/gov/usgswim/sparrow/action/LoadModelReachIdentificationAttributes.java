package gov.usgswim.sparrow.action;

import gov.usgs.cida.datatable.DataTable;
import gov.usgs.cida.datatable.DataTableWritable;
import gov.usgs.cida.datatable.utils.DataTableConverter;
import gov.usgswim.sparrow.datatable.TableProperties;
import gov.usgswim.sparrow.domain.BaseDataSeriesType;
import gov.usgswim.sparrow.domain.DataSeriesType;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;


/**
 * Loads a table containing reach identification attributes for an entire model.
 * 
 * Returns a table with these columns:
 * Reach Name
 * EDACODE
 * EDANAME
 * FULL_IDENTIFIER
 * 
 * 
 * @author eeverman
 *
 */
public class LoadModelReachIdentificationAttributes extends Action<DataTable> {
	
	/** Name of the query in the classname matched properties file */
	public static final String QUERY_NAME = "attributesSQL";
	
	protected long modelId;
	
	
	
	public LoadModelReachIdentificationAttributes(long modelId) {
		super();
		this.modelId = modelId;
	}



	public LoadModelReachIdentificationAttributes() {
		super();
	}



	@Override
	public DataTable doAction() throws Exception {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("ModelId", this.modelId);

		PreparedStatement st = getROPSFromPropertiesFile(QUERY_NAME, this.getClass(), params);
		
		ResultSet rset = null;
		DataTableWritable attribs = null;

		rset = st.executeQuery();
		addResultSetForAutoClose(rset);
		attribs = DataTableConverter.toDataTable(rset);
		attribs.getColumns()[3].setProperty(TableProperties.DATA_SERIES.toString(), DataSeriesType.client_id.toString());
		attribs.getColumns()[3].setProperty(TableProperties.DATA_TYPE.toString(), BaseDataSeriesType.client_id.toString());
		
		return attribs.toImmutable();
	}



	@Override
	public Long getModelId() {
		return modelId;
	}



	public void setModelId(long modelId) {
		this.modelId = modelId;
	}


}
