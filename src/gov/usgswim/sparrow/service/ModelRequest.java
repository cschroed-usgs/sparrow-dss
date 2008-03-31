package gov.usgswim.sparrow.service;

import gov.usgswim.service.pipeline.PipelineRequest;

import org.apache.commons.lang.BooleanUtils;

public class ModelRequest implements PipelineRequest{
	boolean _public = true;
	boolean _approved = true;
	boolean _archived = false;
	boolean _sources = false;
	private String mimetype = "xml";
	private boolean isEcho;
	private String xmlRequest;
	private boolean isUnzipped;

	public ModelRequest() {
	}

	public void setPublic(boolean p) {
		this._public = p;
	}

	public void setPublic(String p) {
		_public = BooleanUtils.toBoolean(p);
	}

	public boolean isPublic() {
		return _public;
	}

	public void setApproved(boolean approved) {
		this._approved = approved;
	}

	public void setApproved(String approved) {
		this._approved = BooleanUtils.toBoolean(approved);
	}

	public boolean isApproved() {
		return _approved;
	}

	public void setArchived(boolean archived) {
		this._archived = archived;
	}

	public void setArchived(String archived) {
		this._archived = BooleanUtils.toBoolean(archived);
	}

	public boolean isArchived() {
		return _archived;
	}

	public void setSources(boolean sources) {
		this._sources = sources;
	}

	public void setSources(String sources) {
		this._sources = BooleanUtils.toBoolean(sources);
	}

	public boolean isSources() {
		return _sources;
	}

	public String getMimeType() {
		return mimetype;
	}

	public void setMimeType(String mimetype) {
		if (mimetype != null) {
			this.mimetype = mimetype;	
		}	
	}
	public String getFileName() {
		return "model";
	}

	public void setEcho(String echo) {
		isEcho = ("yes".equalsIgnoreCase(echo) || "true".equalsIgnoreCase(echo));
	}

	public void setEcho(boolean echo) {
		isEcho = echo;
	}

	public boolean isEcho() {
		return isEcho;
	}

	public String getXMLRequest() {
		return xmlRequest;
	}

	public void setXMLRequest(String request) {
		xmlRequest = request;		
	}
	
	public boolean isZipped() {
		return !isUnzipped;
	}
	
	public void setZip(boolean zip) {
		isUnzipped = !zip;
	}
}
