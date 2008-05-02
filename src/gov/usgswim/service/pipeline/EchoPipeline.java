package gov.usgswim.service.pipeline;

import static gov.usgs.webservices.framework.formatter.IFormatter.OutputType.XML;

import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class EchoPipeline implements Pipeline {

	private String fileName = "request";

	public void dispatch(PipelineRequest o, HttpServletResponse response)
			throws Exception {
		response.setContentType(XML.getMimeType());
		response.addHeader( "Content-Disposition","attachment; filename=" + fileName + "." + XML.getFileSuffix() );
		PrintWriter out = response.getWriter();
		out.write(o.getXMLRequest());
		out.flush();
		out.close();
	}

	public PipelineRequest parse(HttpServletRequest request) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public void setXMLParamName(String xmlParamName) {
		// TODO Auto-generated method stub
		
	}

}
