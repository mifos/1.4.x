CREATE UNIQUE INDEX PERSONNEL_LOGIN_IDX ON PERSONNEL (LOGIN_NAME);

UPDATE DATABASE_VERSION SET DATABASE_VERSION = 181 WHERE DATABASE_VERSION = 180;
