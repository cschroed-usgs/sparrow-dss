OPTIONS (SKIP=1)
LOAD DATA
INTO TABLE TEMP_ENH_REACH
TRUNCATE
FIELDS TERMINATED BY X'9'
TRAILING NULLCOLS
(fnode :FNODE_
,tnode :TNODE_
,LPOLY_ filler
,RPOLY_ filler
,LENGTH filler
,NHDWFLOW_M filler
,NHDWFLOW_1 filler
,identifier :COMID
,full_identifier :comid
,FDATE filler
,RESOLUTION filler
,GNIS_ID filler
,GNIS_NAME filler
,LENGTHKM filler
,REACHCODE filler
,FLOWDIR filler
,WBAREACOMI filler
,FTYPEfiller
,FCODE filler
,SHAPE_LENG filler
,ENABLED filler
,CHK_FLOW filler
,CASE_ filler
,FREQUENCY filler
,GRID_CODE filler
,CUMDRAINAG filler
,VALUE filler
,COUNT filler
,PROD_UNIT filler
,NEWID filler
,FROMNODE filler
,TONODE filler
,hydseq "nvl(:HYDROSEQ, 0)"
,DIVERGENCE filler
,AREAWTMAP filler
,AREAWTMAT filler
,H2 filler
,H4 filler
,TOTUP_DA filler
,COUNT100M2 filler
,MEAN_ANN_P
,MEAN_ANN_T
,H8
,H6	
)