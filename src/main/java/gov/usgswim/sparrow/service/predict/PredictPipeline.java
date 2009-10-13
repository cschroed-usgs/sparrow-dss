package gov.usgswim.sparrow.service.predict;

import gov.usgs.webservices.framework.formatter.IFormatter;
import gov.usgs.webservices.framework.formatter.JSONFormatter;
import gov.usgs.webservices.framework.formatter.SparrowFlatteningFormatter;
import gov.usgs.webservices.framework.formatter.IFormatter.OutputType;
import gov.usgswim.sparrow.service.AbstractPipeline;

public class PredictPipeline extends AbstractPipeline<PredictExportRequest>{

	// TODO eliminate duplicate code from IDByPointPipeline.configure(new JSONFormatter());
	public static JSONFormatter configure(JSONFormatter jFormatter) {
		jFormatter.identifyRepeatedTagElement(JSONFormatter.ANY_PARENT, "reachGroup");
		jFormatter.identifyRepeatedTagElement(JSONFormatter.ANY_PARENT, "adjustment");
		jFormatter.identifyRepeatedTagElement(JSONFormatter.ANY_PARENT, "logicalSet");
		jFormatter.identifyRepeatedTagElement(JSONFormatter.ANY_PARENT, "reach");
		jFormatter.identifyRepeatedTagElement("columns", "group");
		jFormatter.identifyRepeatedTagElement(JSONFormatter.ANY_PARENT, "col");
		jFormatter.identifyRepeatedTagElement(JSONFormatter.ANY_PARENT, "r");
		jFormatter.identifyRepeatedTagElement("data", "section");
		jFormatter.identifyRepeatedTagElement(JSONFormatter.ANY_PARENT, "c");

		return jFormatter;
	}

	public PredictPipeline(){
		super(new PredictExportService(), new PredictExportParser());
//		super(null, null);
	}

	@Override
	protected IFormatter getCustomFlatteningFormatter(OutputType outputType) {
		return new SparrowFlatteningFormatter(outputType);
	}

	@Override
	public IFormatter getConfiguredJSONFormatter() {
		return configure(new JSONFormatter());
	}



}
