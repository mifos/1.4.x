CREATE TABLE PPI_SURVEY_INSTANCE (
    INSTANCE_ID INTEGER NOT NULL,
    BOTTOM_HALF_BELOW DECIMAL(10, 3),
    TOP_HALF_BELOW DECIMAL(10, 3),
    PRIMARY KEY(INSTANCE_ID)
); 
  
UPDATE DATABASE_VERSION SET DATABASE_VERSION = 190 WHERE DATABASE_VERSION = 189;
