<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:output method="xml" indent="yes" />
    <xsl:template match="/MapperConfig">
        <MapperConfig>
            <xsl:apply-templates select="logging"/>
            <xsl:apply-templates select="save_images_at"/>
            <xsl:apply-templates select="ip_monitor"/>
            <xsl:apply-templates select="web_proxy"/>
            <xsl:apply-templates select="security_config"/>
            <xsl:apply-templates select="global_map_config"/>
            <spatial_data_cache max_cache_size="128" report_stats="false" />
            <xsl:apply-templates select="custom_image_renderer"/>
            <xsl:apply-templates select="srs_mapping"/>
            <xsl:apply-templates select="wms_config"/>
            <xsl:apply-templates select="ns_data_provider[@id!='sparrowPredict']"/>
            	<ns_data_provider id="sparrowPredict"
            					  class="gov.usgswim.sparrow.MapViewerSparrowDataProvider" />
            <xsl:apply-templates select="s_data_provider"/>
            <xsl:apply-templates select="map_cache_server"/>
            <xsl:apply-templates select="map_data_source[@name!='sparrow']"/>
            	<map_data_source name="sparrow"
            	    jdbc_host="130.11.165.152"
                	jdbc_sid="widw"
                	jdbc_port="1521"
                	jdbc_user="SPARROW_DSS"
                	jdbc_password="!***REMOVED***"
                	jdbc_mode="thin"
                	max_connections="40"
                	number_of_mappers="30"
            	/>
        </MapperConfig>
    </xsl:template>
    <xsl:template match="*">
        <xsl:copy-of select="."/>
    </xsl:template>
</xsl:stylesheet>
