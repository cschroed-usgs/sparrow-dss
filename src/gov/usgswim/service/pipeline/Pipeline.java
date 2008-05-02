package gov.usgswim.service.pipeline;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface Pipeline {

	void dispatch(PipelineRequest o, HttpServletResponse response) throws Exception;

	PipelineRequest parse(HttpServletRequest request) throws Exception;

	void setXMLParamName(String xmlParamName);

}
