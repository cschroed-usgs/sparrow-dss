package gov.usgs.cida.sparrow.service.util;

/**
 * Naming conventions that must be shared between the service application
 * and GeoServer.
 * 
 * @author eeverman
 */
public class NamingConventions {
	
	
	
	private static final String REUSABLE_SUFFIX = "reusable";
	
	private static final String DEFAULT_STYLE_SUFFIX = "default";
	
	public static final String FLOWLINE_DEFAULT_STYLE_SUFFIX = "flowline-" + DEFAULT_STYLE_SUFFIX;
	
	public static final String CATCHMENT_DEFAULT_STYLE_SUFFIX = "catchment-" + DEFAULT_STYLE_SUFFIX;
	
	/** Base namespace for the entire app, as used on GeoServer */
	public static final String APP_SPATIAL_NAMESPACE = "http://water.usgs.gov/nawqa/sparrow/dss/spatial";
	
	/** Short Geoserver workspace name for flowline (reach) layers */
	public static final String FLOWLINE_WORKSPACE_NAME = "sparrow-flowline";
	
	/** Full Geoserver namespace for flowline (reach) layers */
	public static final String FLOWLINE_NAMESPACE = APP_SPATIAL_NAMESPACE + "/" + FLOWLINE_WORKSPACE_NAME;
	
	/** Short Geoserver workspace name for flowline (reach) layers that are deemed to be reusable */
	public static final String FLOWLINE_REUSABLE_WORKSPACE_NAME = "sparrow-flowline" + "-" + REUSABLE_SUFFIX;
	
	/** Full Geoserver namespace for flowline (reach) layers that are deemed to be reusable */
	public static final String FLOWLINE_REUSABLE_NAMESPACE = APP_SPATIAL_NAMESPACE + "/" + FLOWLINE_REUSABLE_WORKSPACE_NAME;
	
	/** short Geoserver workspace name for catchment layers */
	public static final String CATCHMENT_WORKSPACE_NAME = "sparrow-catchment";
	
	/** full Geoserver namespace catchment layers */
	public static final String CATCHMENT_NAMESPACE = APP_SPATIAL_NAMESPACE + "/" + CATCHMENT_WORKSPACE_NAME;
	
	/** short Geoserver workspace name for catchment layers */
	public static final String CATCHMENT_REUSABLE_WORKSPACE_NAME = "sparrow-catchment" + "-" + REUSABLE_SUFFIX;
	
	/** full Geoserver namespace catchment layers */
	public static final String CATCHMENT_REUSABLE_NAMESPACE = APP_SPATIAL_NAMESPACE + "/" + CATCHMENT_REUSABLE_WORKSPACE_NAME;
	
	/**
	 * Creates a name for the layer that does not start with a number, so that
	 * is it safe to use an an XML element name.
	 * 
	 * A separate workspace is used for contexts that are deemed to be reusable,
	 * that is, they contain little or no customization and have a high likelyhood
	 * of being reused.
	 * 
	 * @param modelId
	 * @param contextId
	 * @param reusable If true, the reusable workspace name is used.
	 * @return The layer name, prefixed with the appropriate workspace name.
	 */
	public static String getFullFlowlineLayerName(int modelId, int contextId, boolean reusable) {
		if (reusable) {
			return FLOWLINE_REUSABLE_WORKSPACE_NAME + ":" + convertContextIdToXMLSafeName(modelId, contextId);
		} else {
			return FLOWLINE_WORKSPACE_NAME + ":" + convertContextIdToXMLSafeName(modelId, contextId);
		}
		
	}
       
        public static String getFullFlowlineLayerPostgresName(int modelId, int contextId, boolean reusable) {
		if (reusable) {
			return "postgres-ws" + ":" + "reusable_flow_" + contextId;
		} else {
			return "postgres-ws" + ":" + "flow_" + contextId;
		}
		
	}
        
	
	/**
	 * Creates a name for the layer that does not start with a number, so that
	 * is it safe to use an an XML element name.
	 * 
	 * A separate workspace is used for contexts that are deemed to be reusable,
	 * that is, they contain little or no customization and have a high likelyhood
	 * of being reused.
	 * 
	 * @param contextId
	 * @param reusable If true, the reusable workspace name is used.
	 * @return The layer name, prefixed with the appropriate workspace name.
	 */
	public static String getFullCatchmentLayerName(int modelId, int contextId, boolean reusable) {
		if (reusable) {
			return CATCHMENT_REUSABLE_WORKSPACE_NAME + ":" + convertContextIdToXMLSafeName(modelId, contextId);
		} else {
			return CATCHMENT_WORKSPACE_NAME + ":" + convertContextIdToXMLSafeName(modelId, contextId);
		}
	}
	
        public static String getFullCatchmentLayerPostgresName(int modelId, int contextId, boolean reusable) {
		if (reusable) {
			return "postgres-ws" + ":" + "reuseable_catch_" + contextId;
		} else {
			return "postgres-ws" + ":" + "catch_" + contextId;
		}
	}
	/**
	 * Returns the complete namespace (with the http://water.usgs.gov... prefix)
	 * for flowline layers.  A different namespace is used if the layer is deemed
	 * to be likely to be reused.
	 * 
	 * @param reusable
	 * @return 
	 */
	public static String getFlowlineNamespace(boolean reusable) {
		if (reusable) {
			return FLOWLINE_REUSABLE_NAMESPACE;
		} else {
			return FLOWLINE_NAMESPACE;
		}
	}
	
	/**
	 * Returns the complete namespace (with the http://water.usgs.gov... prefix)
	 * for catchment layers.  A different namespace is used if the layer is deemed
	 * to be likely to be reused.
	 * 
	 * @param reusable
	 * @return 
	 */
	public static String getCatchmentNamespace(boolean reusable) {
		if (reusable) {
			return CATCHMENT_REUSABLE_NAMESPACE;
		} else {
			return CATCHMENT_NAMESPACE;
		}
	}
	
	/**
	 * Returns the name of the GeoServer workspace for flowline layers.
	 * A different namespace is used if the layer is deemed
	 * to be likely to be reused.
	 * 
	 * @param reusable
	 * @return 
	 */
	public static String getFlowlineWorkspaceName(boolean reusable) {
		if (reusable) {
			return FLOWLINE_REUSABLE_WORKSPACE_NAME;
		} else {
			return FLOWLINE_WORKSPACE_NAME;
		}
	}
	
	/**
	 * Returns the name of the GeoServer workspace for catchment layers.
	 * A different namespace is used if the layer is deemed
	 * to be likely to be reused.
	 * 
	 * @param reusable
	 * @return 
	 */
	public static String getCatchmentWorkspaceName(boolean reusable) {
		if (reusable) {
			return CATCHMENT_REUSABLE_WORKSPACE_NAME;
		} else {
			return CATCHMENT_WORKSPACE_NAME;
		}
	}
	
	
	/**
	 * Creates a name for the layer that does not start with a number, so that
	 * is it safe to use an an XML element name.
	 * 
	 * @param modelId
	 * @param contextId
	 * @return 
	 */
	public static String convertContextIdToXMLSafeName(int modelId, int contextId) {
		if (contextId >= 0) {
			return Integer.toString(modelId) + "P" + Integer.toString(contextId);
		} else {
			return Integer.toString(modelId) + "N" + Integer.toString(Math.abs(contextId));
		}
	}
	
	/**
	 * Builds a regex string that can be used to match resources associated with
	 * a particular model.
	 * 
	 * @param modelId
	 * @return 
	 */
	public static String buildModelRegex(int modelId) {
		return "" + modelId + "[NP].*";
	}
	
	/**
	 * Returns the default style name for the flowline layer of a given context.
	 * 
	 * Note that only 'reusable' contexts should be given named default styles -
	 * These are context that have no adjustments and will be tile cached, which
	 * requires a named style.
	 * 
	 * @param modelId
	 * @param contextId
	 * @return 
	 */
	public static String buildDefaultFlowlineStyleName(int modelId, int contextId) {
		return convertContextIdToXMLSafeName(modelId, contextId) + "-" + FLOWLINE_DEFAULT_STYLE_SUFFIX;
	}
	
	/**
	 * Returns the default style name for the catchment layer of a given context.
	 * 
	 * Note that only 'reusable' contexts should be given named default styles -
	 * These are context that have no adjustments and will be tile cached, which
	 * requires a named style.
	 * 
	 * @param modelId
	 * @param contextId
	 * @return 
	 */
	public static String buildDefaultCatchmentStyleName(int modelId, int contextId) {
		return convertContextIdToXMLSafeName(modelId, contextId) + "-" + CATCHMENT_DEFAULT_STYLE_SUFFIX;
	}
	
	/**
	 * Provides a quick filter for styles to determine if they follow the naming
	 * conventions of a default style for a reusable layer.
	 * 
	 * @param styleName
	 * @return 
	 */
	public static boolean isLikelyReusableStyleName(String styleName) {
		return 
			styleName.matches("\\d+[NP]\\d+-" + FLOWLINE_DEFAULT_STYLE_SUFFIX) ||
			styleName.matches("\\d+[NP]\\d+-" + CATCHMENT_DEFAULT_STYLE_SUFFIX);
	}
	
	/**
	 * Provides a more definitive filter to determine if a given style is (very)
	 * likely to be the default style for the named reusable layer.
	 * 
	 * This is used to determine if the style is specific to the layer so that
	 * it can be deleted when the layer is deleted.
	 * 
	 * @param styleName
	 * @param layerName
	 * @return 
	 */
	public static boolean isLikelyReusableStyleNameForLayer(String styleName, String layerName) {
		return 
			styleName.matches(layerName + "-" + FLOWLINE_DEFAULT_STYLE_SUFFIX) ||
			styleName.matches(layerName + "-" + CATCHMENT_DEFAULT_STYLE_SUFFIX);
	}
	
	/**
	 * Converts an XML safe layer name back to its context ID.
	 * 
	 * @param encodedContextId encoded via convertContextIdToXMLSafeName
	 * @return 
	 */
	public static int convertXMLSafeNameToContextId(String encodedContextId) throws NumberFormatException {
		encodedContextId = encodedContextId.toUpperCase();
		
		if (encodedContextId.contains("P")) {
			return Integer.parseInt(encodedContextId.substring(encodedContextId.indexOf("P") + 1));
		} else if (encodedContextId.contains("N")) {
			return (-1) * (Integer.parseInt(encodedContextId.substring(encodedContextId.indexOf("N") + 1)));
		} else {
			throw new NumberFormatException("Unable to read context ID encoded as '" + encodedContextId + "'");
		}
	}
}
