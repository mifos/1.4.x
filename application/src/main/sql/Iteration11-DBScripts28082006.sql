ALTER TABLE CUSTOMER_FLAG_DETAIL MODIFY VERSION_NO INTEGER NULL;

UPDATE custom_field_definition SET MANDATORY_FLAG  = 0 WHERE FIELD_ID IN (11,12);
