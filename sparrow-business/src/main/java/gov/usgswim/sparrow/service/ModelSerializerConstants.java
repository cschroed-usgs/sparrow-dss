package gov.usgswim.sparrow.service;

import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang.time.FastDateFormat;


public class ModelSerializerConstants {
	public static final String TARGET_NAMESPACE = "http://www.usgs.gov/sparrow/meta_response/v0_1";
	public static final String TARGET_NAMESPACE_LOCATION = "http://www.usgs.gov/sparrow/meta_response.xsd";
	public static final String MODELS = "models";
	public static final String MODEL = "model";
	public static final String MODEL_ID_ATTRIBUTE = "id";
	public static final String STATUS = "status";
	public static final String MODEL_APPROVED = "approved";
	public static final String MODEL_PUBLIC = "public";
	public static final String MODEL_ARCHIVED = "archived";
	public static final String SOURCE_UNITS = "units";
	public static final String SOURCE_CONSTITUENT = "constituent";
	public static final String SOURCE_DESCRIPTION = "description";
	public static final String SOURCE_DISPLAY_NAME = "displayName";
	public static final String SOURCE_NAME = "name";
	public static final String SOURCE_SORT_ORDER = "sortOrder";
	public static final String SOURCE_IDENTIFIER = "identifier";
	public static final String SOURCE_ID = "id";
	public static final String SOURCE = "source";
	public static final String SOURCES = "sources";
	public static final String SESSION_TOPIC = "topic";
	public static final String SESSION_ADD_NOTE = "add_note";
	public static final String SESSION_ADD_DATE = "add_date";
	public static final String SESSION_ADD_BY = "add_by";
	public static final String SESSION_SORT_ORDER = "sort_order";
	public static final String SESSION_APPROVED = "approved";
	public static final String SESSION_TYPE = "type";
	public static final String SESSION_GROUP_NAME = "group_name";
	public static final String SESSION_DESCRIPTION = "description";
	public static final String SESSION_NAME = "name";
	public static final String SESSION_KEY = "key";
	public static final String SESSION = "session";
	public static final String SESSIONS = "sessions";
	public static final String EAST_BOUNDS_ATTRIBUTE = "east";
	public static final String SOUTH_BOUNDS_ATTRIBUTE = "south";
	public static final String WEST_BOUNDS_ATTRIBUTE = "west";
	public static final String NORTH_BOUNDS_ATTRIBUTE = "north";
	public static final String BOUNDS = "bounds";
	public static final String REGION = "region";
	public static final String REGIONS = "regions";
	public static final String STATE = "state";
	public static final String STATES = "states";
	public static final String NATIONAL = "national";
	public static final String SPATIAL_MEMBERSHIP = "spatialMembership";
	public static final String BASE_YEAR = "baseYear";
	public static final String MODEL_UNITS = "units";
	public static final String MODEL_CONSTITUENT = "constituent";
	public static final String THEME_NAME = "themeName";
	public static final String ENH_NETWORK_URL = "enhNetworkUrl";
	public static final String ENH_NETWORK_NAME = "enhNetworkName";
	public static final String ENH_NETWORK_ID = "enhNetworkId";
	public static final String CONTACT_ID = "contactId";
	public static final String DATE_ADDED = "dateAdded";
	public static final String MODEL_URL = "url";
	public static final String MODEL_DESCRIPTION = "description";
	public static final String ALIAS = "alias";
	public static final String MODEL_NAME = "name";
	public static final FastDateFormat DATE_FORMAT = DateFormatUtils.ISO_DATE_FORMAT;
}
