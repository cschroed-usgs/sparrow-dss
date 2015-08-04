--liquibase formatted sql

--This is for the stream_network schema

--logicalFilePath: changeLog14_StreamNetworkUpdIDwFullID.sql

--changeset kmschoep:ERupdateIDENTIFIERwithFULL_IDENTIFIER2
UPDATE ENH_REACH
SET IDENTIFIER = TO_NUMBER(FULL_IDENTIFIER)
WHERE
      full_identifier is not null
  AND decode(REGEXP_INSTR (full_identifier, '[^[:digit:]]'),0,'NUMBER','NOT_NUMBER') = 'NUMBER';
--rollback select null from dual;