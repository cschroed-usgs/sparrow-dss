--liquibase formatted sql

--This is for the sparrow_dss schema

--logicalFilePath: predefinedSessionTrigger17.sql

--changeset cschroed:addAutoIncrementPKtoPredefinedSession
CREATE OR REPLACE EDITIONABLE TRIGGER "PREDEFINED_SESSION_AUTO_ID_TRG" BEFORE
INSERT ON "PREDEFINED_SESSION" REFERENCING NEW AS newRow
FOR EACH ROW
	WHEN (
newRow.PREDEFINED_SESSION_ID IS NULL
	) BEGIN
SELECT PREDEFINED_SESSION_SEQ.nextval INTO :newRow.PREDEFINED_SESSION_ID FROM dual;
END;