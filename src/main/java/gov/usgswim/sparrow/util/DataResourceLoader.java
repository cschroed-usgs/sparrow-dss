package gov.usgswim.sparrow.util;

import java.io.IOException;
import java.sql.SQLException;

import gov.usgswim.datatable.DataTable;
import gov.usgswim.datatable.DataTableWritable;
import gov.usgswim.datatable.impl.SimpleDataTableWritable;
import gov.usgswim.datatable.impl.StandardNumberColumnDataWritable;
import gov.usgswim.datatable.utils.DataTableUtils;

public class DataResourceLoader {

	public static DataTableWritable makeSourceMetaStructure() {
		// Note that the topo.txt file from the modelers does not have the reach id
		String[] headings = {"SOURCE_ID", "NAME", "DISPLAY_NAME", "DESCRIPTION", "CONSTITUENT", "UNITS", "PRECISION", "IS_POINT_SOURCE"};
	
		Class<?>[] types= {Long.class, String.class, String.class, String.class, String.class, String.class, Integer.class, Integer.class};
		return new SimpleDataTableWritable(headings, null, types);
	}

	public static DataTableWritable loadSourceMetadata(long modelId){
		String sourceMetaFolder = DataResourceUtils.getModelResourceFilePath(modelId, "src_metadata.txt");
		DataTableWritable sourceMeta = makeSourceMetaStructure();
		return DataTableUtils.fill(sourceMeta, sourceMetaFolder, false, "\t", true);
	}

	public static DataTableWritable loadTopo(long modelId) throws SQLException,
	IOException {
		String topoFile = DataResourceUtils.getModelResourceFilePath(modelId, "topo.txt");
		DataTableWritable topo = makeTopoStructure();
		DataTableUtils.fill(topo, topoFile, false, "\t", true);
		return topo;
	}

	public static DataTableWritable loadDecay(long modelId) {
		// This is really a part of coef
		// Only two columns, instream and total decay
		return null;
	
	}

	public static DataTableWritable loadSourceReachCoef(long modelId) {
	
		return null;
	}

	public static DataTableWritable makeTopoStructure() {
		// Note that the topo.txt file from the modelers does not have the reach id
		String[] headings = {"reachID", "fnode", "tnode", "iftran", "hydseq"}; // TODO check name of reach id
		Class<?>[] types= {Integer.class, Integer.class, Integer.class, Integer.class, Integer.class};
		DataTableWritable result = new SimpleDataTableWritable(headings, null, types);
		return result;
	}

	public static DataTableWritable loadSourceReachCoef(long modelId, DataTable sources) {
		SimpleDataTableWritable result = new SimpleDataTableWritable();
		for (int srcIndex = 0; srcIndex < sources.getRowCount(); srcIndex++) {
			result.addColumn(new StandardNumberColumnDataWritable<Double>());
			// may need to add names
		}
		// may need to add id
		String coefFile = DataResourceUtils.getModelResourceFilePath(modelId, "coef.txt");
		return DataTableUtils.fill(result, coefFile, false, "\t", true);
	}

}
