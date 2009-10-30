package gov.usgswim.sparrow.parser;

/**
 *
 */
public enum DataSeriesType {

	//Total (i.e. includes upstream) predicted load.  
    total						(true, false, false, false, 1, false, false, false, false, true), // active
    total_std_error_estimate	(true, false, false, true, 1, false, false, true, false, false),
    
    //Incremental (i.e. does not include upstream) predicted load.  
    incremental					(true, false, false, false, 1, false, false, false, false, true), //active
    incremental_std_error_estimate	(true, false, false, true, 1, false, false, true, false, false), //active
    
    incremental_yield			(true, false, false, false, 1, false, true, false, false, true), // active
    total_concentration			(true, false, false, false, 1, false, true, false, false, true), // active
    source_value				(false, true, false, false, 2, false, false, false, false, true), // active
    incremental_delivered_yield	(true, false, false, false, 1, true, true, false, true, true), // inaccurate?
    total_delivered_flux		(true, false, false, false, 1, true, false, false, true, true), // inaccurate?
    incremental_delivered_flux	(true, false, false, false, 1, true, false, false, true, true), // inaccurate?
    delivered_fraction			(true, false, false, false, 0, true, false, false, true, true), // inaccurate?
    total_decay					(true, false, false, false, 1, false, false, true, false, true),
    total_no_decay				(true, false, false, false, 1, false, false, true, false, true),
    land_to_water_coef			(false, false, true, false, 2, false, false, false, false, true),
    instream_decay_coef			(false, false, true, false, 0, false, false, false, false, true)
    ;

    // TODO cut down the list of attributes
    private final boolean resultBased;
    private final boolean sourceBased;
    private final boolean coefBased;
    private final boolean errEstBased;
    private final int srcRequirement;	//0 Not allowed, 1 allowed, 2 required
    private static final int NO_SOURCES_ALLOWED = 0,SOURCES_ALLOWED = 1,SOURCES_REQUIRED = 2;
    private final boolean targetRequired;
    private final boolean weighted;
    private final boolean extraColumn;
    private final boolean deliveryBased;
    private final boolean analysisAllowed;



    DataSeriesType(boolean resultBased, boolean sourceBased, boolean coefBased,
            boolean errEstBased, int srcRequirement, boolean targetRequired,
            boolean weighted, boolean extraColumn, boolean deliveryBased,
            boolean analysisAllowed) {

        this.resultBased = resultBased;
        this.sourceBased = sourceBased;
        this.coefBased = coefBased;
        this.errEstBased = errEstBased;
        this.srcRequirement = srcRequirement;
        this.targetRequired = targetRequired;
        this.weighted = weighted;
        this.extraColumn = extraColumn;
        this.deliveryBased = deliveryBased;
        this.analysisAllowed = analysisAllowed;
    }


    /**
     * Returns true if this is a delivery calculation, which requires a target.
     * @return
     */
    public boolean isDeliveryBased() {
    	return deliveryBased;
    }
    
    /**
     * Returns true if analysis (like aggregation) is allowed on this series.
     * @return
     */
    public boolean isAnalysisAllowed() {
    	return analysisAllowed;
    }
    
    /**
     * Returns true if analysis (like aggregation) is NOT allowed on this series.
     * @return
     */
    public boolean isAnalysisDisallowed() {
    	return ! analysisAllowed;
    }

    /**
     * Returns true if this dataSeries uses PredictResult data (possibly in
     * combination with other data) to generate the returned data.
     * @return
     */
    public boolean isResultBased() {
        return resultBased;
    }

    /**
     * Returns true if this dataseries uses only the PredictResult values.
     * @return
     */
    public boolean isResultBasedOnly() {
        return resultBased && (!sourceBased) && (!coefBased) && (!errEstBased);
    }

    /**
     * Returns true if this dataseries uses source data from PredictData (possibly in
     * combination with other data including the PredictResults) to generate the
     * returned data.
     * @return
     */
    public boolean isSourceBased() {
        return sourceBased;
    }

    /**
     * Returns true if this dataseries is based only on source values from PredictData.
     * @return
     */
    public boolean isSourceBasedOnly() {
        return sourceBased && (!resultBased) && (!coefBased);
    }

    /**
     * Returns true if this dataseries uses coef data from PredictData (possibly in
     * combination with other data including the PredictResults) to generate the
     * returned data.
     *
     * An example would be instream_decay_coef, which only uses the coef's from
     * the PredictData and doesn't even use the prediction results.
     * @return
     */
    public boolean isCoefBased() {
        return coefBased;
    }

    /**
     * Returns true if this dataseries is based only on coef's found in the PredictData.
     * @return
     */
    public boolean isCoefBasedOnly() {
        return coefBased && (!resultBased) && (!sourceBased);
    }
    
    /**
     * Return true if this estimate is an estimate of of standard error.
     * @return
     */
    public boolean isStandardErrorEstimateBased() {
    	return errEstBased;
    }

    /**
     * Returns true if a source is either required or allowed for this dataseries.
     * @return
     */
    public boolean isSourceAllowed() {
        return srcRequirement == SOURCES_ALLOWED || srcRequirement == SOURCES_REQUIRED;
    }

    /**
     * Returns true if this dataseries does not allow a source to be specified.
     * instream_decay_coef is an example:  There are no source specific values
     * for this coef.
     *
     * @return
     */
    public boolean isSourceDisallowed() {
        return srcRequirement == NO_SOURCES_ALLOWED;
    }

    /**
     * Returns true if this dataseries requires a source to specified.
     * source_value is an example:  To report source values, you must specify
     * which source value.
     * @return
     */
    public boolean isSourceRequired() {
        return srcRequirement == SOURCES_REQUIRED;
    }

    /**
     * This dataseries requires Target reaches to be defined.
     * @return
     */
    public boolean isTargetRequired() {
        return targetRequired;
    }

    /**
     * Returns {@code true} if this data series requires a weighting be applied
     * to the returned data, {@code false} otherwise.
     *
     * @return {@code true} if this data series requires a weighting be applied
     *         to the returned data, {@code false} otherwise.
     */
    public boolean isWeighted() {
        return weighted;
    }

    /**
     * This dataseries requires an extra non-standard at the time the prediction
     * is run.  This will perhaps be an alternate implementation of PredictionRunner.
     * @return
     */
    public boolean isExtraColumn() {
        return extraColumn;
    }
}