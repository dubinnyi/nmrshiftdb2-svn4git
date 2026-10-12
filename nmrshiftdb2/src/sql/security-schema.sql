
# -----------------------------------------------------------------------
# TURBINE_PERMISSION
# -----------------------------------------------------------------------
drop table if exists TURBINE_PERMISSION;

CREATE TABLE TURBINE_PERMISSION
(
		            PERMISSION_ID INTEGER NOT NULL,
		            PERMISSION_NAME VARCHAR (99) NOT NULL,
		            OBJECTDATA MEDIUMBLOB,
    PRIMARY KEY(PERMISSION_ID),
    UNIQUE (PERMISSION_NAME)
);

# -----------------------------------------------------------------------
# TURBINE_ROLE
# -----------------------------------------------------------------------
drop table if exists TURBINE_ROLE;

CREATE TABLE TURBINE_ROLE
(
		            ROLE_ID INTEGER NOT NULL,
		            ROLE_NAME VARCHAR (99) NOT NULL,
		            OBJECTDATA MEDIUMBLOB,
    PRIMARY KEY(ROLE_ID),
    UNIQUE (ROLE_NAME)
);

# -----------------------------------------------------------------------
# TURBINE_GROUP
# -----------------------------------------------------------------------
drop table if exists TURBINE_GROUP;

CREATE TABLE TURBINE_GROUP
(
		            GROUP_ID INTEGER NOT NULL,
		            GROUP_NAME VARCHAR (99) NOT NULL,
		            OBJECTDATA MEDIUMBLOB,
    PRIMARY KEY(GROUP_ID),
    UNIQUE (GROUP_NAME)
);

# -----------------------------------------------------------------------
# TURBINE_ROLE_PERMISSION
# -----------------------------------------------------------------------
drop table if exists TURBINE_ROLE_PERMISSION;

CREATE TABLE TURBINE_ROLE_PERMISSION
(
		            ROLE_ID INTEGER NOT NULL,
		            PERMISSION_ID INTEGER NOT NULL,
    PRIMARY KEY(ROLE_ID,PERMISSION_ID),
    FOREIGN KEY (ROLE_ID) REFERENCES TURBINE_ROLE (ROLE_ID)
    ,
    FOREIGN KEY (PERMISSION_ID) REFERENCES TURBINE_PERMISSION (PERMISSION_ID)
    
);

# -----------------------------------------------------------------------
# TURBINE_USER
# -----------------------------------------------------------------------
drop table if exists TURBINE_USER;

CREATE TABLE TURBINE_USER
(
		            USER_ID INTEGER NOT NULL,
		            LOGIN_NAME VARCHAR (32) NOT NULL,
		            PASSWORD_VALUE VARCHAR (32) NOT NULL,
		            FIRST_NAME VARCHAR (99) NOT NULL,
		            LAST_NAME VARCHAR (99) NOT NULL,
		            TITLE VARCHAR (99),
		            ADDRESS VARCHAR (99) NOT NULL,
		            CITY VARCHAR (99) NOT NULL,
		            STATE VARCHAR (99),
		            ZIP_CODE VARCHAR (99) NOT NULL,
		            COUNTRY VARCHAR (99) NOT NULL,
		            WEB_PAGE VARCHAR (99),
		            EMAIL VARCHAR (99) NOT NULL,
		            CONFIRM_VALUE VARCHAR (99),
		            MODIFIED TIMESTAMP,
		            CREATED TIMESTAMP NOT NULL default '2004-01-01 00:00:00',
		            LAST_LOGIN TIMESTAMP NOT NULL default '2004-01-01 00:00:00',
		            OBJECTDATA MEDIUMBLOB,
		            BROKENSUBMIT MEDIUMBLOB,
		            searchhistory MEDIUMBLOB,
		            structureshistory MEDIUMBLOB,
		            LAST_DOWNLOAD_DATE TIMESTAMP NOT NULL default '2004-01-01 00:00:00',
		            HALL_OF_FAME VARCHAR (5) default 'true' NOT NULL,
		            LABGROUP_KEY INTEGER,
		            BROKEN_EDIT MEDIUMBLOB,
		            OLD_LITERATURE MEDIUMBLOB,
		            PASSWORD_CHANGED TIMESTAMP NOT NULL default '2004-01-01 00:00:00',
		            DISABLED VARCHAR (1),
		            SALT VARCHAR (32),
					HOST_ID VARCHAR (256),
					HOST_ID_TIME TIMESTAMP,
    PRIMARY KEY(USER_ID),
    UNIQUE (LOGIN_NAME)
);

# -----------------------------------------------------------------------
# TURBINE_USER_GROUP_ROLE
# -----------------------------------------------------------------------
drop table if exists TURBINE_USER_GROUP_ROLE;

CREATE TABLE TURBINE_USER_GROUP_ROLE
(
		            USER_ID INTEGER NOT NULL,
		            GROUP_ID INTEGER NOT NULL,
		            ROLE_ID INTEGER NOT NULL,
    PRIMARY KEY(USER_ID,GROUP_ID,ROLE_ID),
    FOREIGN KEY (USER_ID) REFERENCES TURBINE_USER (USER_ID)
    ,
    FOREIGN KEY (GROUP_ID) REFERENCES TURBINE_GROUP (GROUP_ID)
    ,
    FOREIGN KEY (ROLE_ID) REFERENCES TURBINE_ROLE (ROLE_ID)
    
);

# -----------------------------------------------------------------------
# TURBINE_SCHEDULED_JOB
# -----------------------------------------------------------------------
drop table if exists TURBINE_SCHEDULED_JOB;

CREATE TABLE TURBINE_SCHEDULED_JOB
(
		            JOB_ID INTEGER NOT NULL,
		            SECOND INTEGER default -1 NOT NULL,
		            MINUTE INTEGER default -1 NOT NULL,
		            HOUR INTEGER default -1 NOT NULL,
		            WEEK_DAY INTEGER default -1 NOT NULL,
		            DAY_OF_MONTH INTEGER default -1 NOT NULL,
		            TASK VARCHAR (99) NOT NULL,
		            EMAIL VARCHAR (99),
		            PROPERTY MEDIUMBLOB,
    PRIMARY KEY(JOB_ID)
);

  
  
  
  
  
  
  
  
