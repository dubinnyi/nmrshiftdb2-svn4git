
# -----------------------------------------------------------------------
# GUESTBOOK
# -----------------------------------------------------------------------
drop table if exists GUESTBOOK;

CREATE TABLE GUESTBOOK
(
		            GUESTBOOK_ID INTEGER NOT NULL,
		            USER_ID INTEGER,
		            DATE DATETIME NOT NULL,
		            TEXT MEDIUMTEXT NOT NULL,
		            VALID VARCHAR (8) default 'false' NOT NULL,
    PRIMARY KEY(GUESTBOOK_ID),
    FOREIGN KEY (USER_ID) REFERENCES TURBINE_USER (USER_ID)
    
);

# -----------------------------------------------------------------------
# SESSIONS
# -----------------------------------------------------------------------
drop table if exists SESSIONS;

CREATE TABLE SESSIONS
(
		            SESSIONS INTEGER NOT NULL,
		            LOWEST_LOAD_SERVER VARCHAR (255),
		            SESSIONS_ALL INTEGER default 0 NOT NULL,
		            SESSIONS_ALL_DATE DATETIME NOT NULL,
    PRIMARY KEY(SESSIONS)
);

# -----------------------------------------------------------------------
# CONDITION_TYPE
# -----------------------------------------------------------------------
drop table if exists CONDITION_TYPE;

CREATE TABLE CONDITION_TYPE
(
		            CONDITION_TYPE_ID INTEGER NOT NULL,
		            CONDITION_NAME VARCHAR (255) NOT NULL,
		            CONDITION_NAME_INPUT VARCHAR (255),
		            CONDITION_TYPE CHAR NOT NULL,
		            DATA_TYPE VARCHAR (255) NOT NULL,
		            DICT_REF VARCHAR (255) NOT NULL,
		            UNITS VARCHAR (255) NOT NULL,
		            CML_ENTRY_TYPE VARCHAR (255) NOT NULL,
		            REQUIRED VARCHAR (255) NOT NULL,
                    FOR_1D VARCHAR (255) NOT NULL,
                    FOR_2D VARCHAR (255) NOT NULL,
    PRIMARY KEY(CONDITION_TYPE_ID)
);

# -----------------------------------------------------------------------
# NMR_CONDITION
# -----------------------------------------------------------------------
drop table if exists NMR_CONDITION;

CREATE TABLE NMR_CONDITION
(
		            CONDITION_ID INTEGER NOT NULL,
		            VALUE VARCHAR (255) NOT NULL,
		            CONDITION_TYPE_ID INTEGER NOT NULL,
		            USER_ID INTEGER NOT NULL,
    PRIMARY KEY(CONDITION_ID),
    FOREIGN KEY (CONDITION_TYPE_ID) REFERENCES CONDITION_TYPE (CONDITION_TYPE_ID)
    ,
    FOREIGN KEY (USER_ID) REFERENCES TURBINE_USER (USER_ID)
    ,
    INDEX CONDITION_TYPE_ID_INDEX (CONDITION_TYPE_ID),
    INDEX USER_ID_INDEX (USER_ID)
);

# -----------------------------------------------------------------------
# SPECTRUM_TYPE
# -----------------------------------------------------------------------
drop table if exists SPECTRUM_TYPE;

CREATE TABLE SPECTRUM_TYPE
(
		            SPECTRUM_TYPE_ID INTEGER NOT NULL,
		            DIMENSIONALITY INTEGER NOT NULL,
		            NAME VARCHAR (255) NOT NULL,
		            USER_ID INTEGER NOT NULL,
		            FREQUENCY_RATIO FLOAT NOT NULL,
		            PEAKS_MANDATORY VARCHAR (255) NOT NULL,
    NMREDATA_NAME VARCHAR(255),
    RED_LIMIT FLOAT,
    ORANGE_LIMIT FLOAT,
    PRIMARY KEY(SPECTRUM_TYPE_ID),
    FOREIGN KEY (USER_ID) REFERENCES TURBINE_USER (USER_ID)
    ,
    INDEX USER_ID_INDEX (USER_ID),
    INDEX DIMENSIONLATA_INDEX (DIMENSIONALITY)
);


# -----------------------------------------------------------------------
# SPECTRUM_TYPE_CONDITION
# -----------------------------------------------------------------------
drop table if exists SPECTRUM_TYPE_CONDITION;

CREATE TABLE SPECTRUM_TYPE_CONDITION
(
    SPECTRUM_TYPE_ID INTEGER NOT NULL,
    CONDITION_TYPE_ID INTEGER NOT NULL,
    PRIMARY KEY(SPECTRUM_TYPE_ID,CONDITION_TYPE_ID));
    
# -----------------------------------------------------------------------
# ISOTOPE
# -----------------------------------------------------------------------
drop table if exists ISOTOPE;

CREATE TABLE ISOTOPE
(
		            SPECTRUM_TYPE_ID INTEGER NOT NULL,
		            AXIS INTEGER NOT NULL,
		            ELEMENT_SYMBOL VARCHAR (255) NOT NULL,
		            ATOMIC_MASS INTEGER NOT NULL,
    PRIMARY KEY(SPECTRUM_TYPE_ID,AXIS),
    FOREIGN KEY (SPECTRUM_TYPE_ID) REFERENCES SPECTRUM_TYPE (SPECTRUM_TYPE_ID)
    ,
    INDEX ELEMENT_SYMBOL_INDEX (ELEMENT_SYMBOL),
    INDEX SPECTRUM_TYPE_ID_INDEX (SPECTRUM_TYPE_ID)
);

# -----------------------------------------------------------------------
# MOLECULE
# -----------------------------------------------------------------------
drop table if exists MOLECULE;

CREATE TABLE MOLECULE
(
		            MOLECULE_ID INTEGER NOT NULL,
		            DATE DATETIME NOT NULL,
		            CAS_NUMBER MEDIUMTEXT NOT NULL,
		            MOLECULAR_WEIGHT FLOAT NOT NULL,
		            SMILES_STRING MEDIUMBLOB NOT NULL,
		            SMILES_STRING_CHIRAL MEDIUMBLOB,
		            USER_ID INTEGER NOT NULL,
		            fp0 BIGINT NOT NULL,
		            fp1 BIGINT NOT NULL,
		            fp2 BIGINT NOT NULL,
		            fp3 BIGINT NOT NULL,
		            fp4 BIGINT NOT NULL,
		            fp5 BIGINT NOT NULL,
		            fp6 BIGINT NOT NULL,
		            fp7 BIGINT NOT NULL,
		            fp8 BIGINT NOT NULL,
		            fp9 BIGINT NOT NULL,
		            fp10 BIGINT NOT NULL,
		            fp11 BIGINT NOT NULL,
		            fp12 BIGINT NOT NULL,
		            fp13 BIGINT NOT NULL,
		            fp14 BIGINT NOT NULL,
		            fp15 BIGINT NOT NULL,
		            DBE FLOAT NOT NULL,
		            SSSR INTEGER NOT NULL,
		            SAR INTEGER NOT NULL,
		            COMMENT LONGBLOB,
		            FORMULA MEDIUMTEXT,
		            MULTIPLICITY_0 INTEGER default 0,
		            MULTIPLICITY_1 INTEGER default 0,
		            MULTIPLICITY_2 INTEGER default 0,
		            MULTIPLICITY_3 INTEGER default 0,
                    VIEWS INTEGER default 0,
				    DOI MEDIUMTEXT,
    PRIMARY KEY(MOLECULE_ID),
    FOREIGN KEY (USER_ID) REFERENCES TURBINE_USER (USER_ID)
    ,
    INDEX SMILES_STRING_INDEX (SMILES_STRING(250)),
    INDEX SMILES_STRING_CHIRAL_INDEX (SMILES_STRING_CHIRAL(250)),
    INDEX CAS_NUMBER_INDEX (CAS_NUMBER(250)),
    INDEX MOLECULAR_WEIGHT_INDEX (MOLECULAR_WEIGHT),
    INDEX USER_ID_INDEX (USER_ID),
    INDEX FORMULA_INDEX (FORMULA(250)),
    INDEX MULTIPLICITY_0_INDEX (MULTIPLICITY_0),
    INDEX MULTIPLICITY_1_INDEX (MULTIPLICITY_1),
    INDEX MULTIPLICITY_2_INDEX (MULTIPLICITY_2),
    INDEX MULTIPLICITY_3_INDEX (MULTIPLICITY_3)
);

# -----------------------------------------------------------------------
# MIXTURE
# -----------------------------------------------------------------------
drop table if exists MIXTURE;

CREATE TABLE MIXTURE
(
		            MIXTURE_ID INTEGER NOT NULL,
		            USER_ID INTEGER NOT NULL,
		            MIXTURE_COMMENT MEDIUMTEXT,
    PRIMARY KEY(MIXTURE_ID),
    FOREIGN KEY (USER_ID) REFERENCES TURBINE_USER (USER_ID)
    
);

# -----------------------------------------------------------------------
# MIXTURE_MOLECULE
# -----------------------------------------------------------------------
drop table if exists MIXTURE_MOLECULE;

CREATE TABLE MIXTURE_MOLECULE
(
		            MIXTURE_ID INTEGER NOT NULL,
		            MOLECULE_ID INTEGER NOT NULL,
		            RATIO INTEGER NOT NULL,
    PRIMARY KEY(MIXTURE_ID,MOLECULE_ID),
    FOREIGN KEY (MIXTURE_ID) REFERENCES MIXTURE (MIXTURE_ID)
    ,
    FOREIGN KEY (MOLECULE_ID) REFERENCES MOLECULE (MOLECULE_ID)
    
);

# -----------------------------------------------------------------------
# SUBSTANCE
# -----------------------------------------------------------------------
drop table if exists SUBSTANCE;

CREATE TABLE SUBSTANCE
(
		            SUBSTANCE_ID INTEGER NOT NULL,
		            USER_ID INTEGER NOT NULL,
		            SUBSTANCE_COMMENT MEDIUMTEXT,
    PRIMARY KEY (SUBSTANCE_ID),
    FOREIGN KEY (USER_ID) REFERENCES TURBINE_USER (USER_ID)
    
);

# -----------------------------------------------------------------------
# BOOKMARK
# -----------------------------------------------------------------------
drop table if exists BOOKMARK;

CREATE TABLE BOOKMARK
(
		            MOLECULE_ID INTEGER NOT NULL,
		            USER_ID INTEGER NOT NULL,
    PRIMARY KEY(MOLECULE_ID,USER_ID),
    FOREIGN KEY (MOLECULE_ID) REFERENCES MOLECULE (MOLECULE_ID)
    ,
    FOREIGN KEY (USER_ID) REFERENCES TURBINE_USER (USER_ID)
    
);

# -----------------------------------------------------------------------
# CANONICAL_NAME_TYPE
# -----------------------------------------------------------------------
drop table if exists CANONICAL_NAME_TYPE;

CREATE TABLE CANONICAL_NAME_TYPE
(
		            CANONICAL_NAME_TYPE_ID INTEGER NOT NULL,
		            CANONICAL_NAME_TYPE VARCHAR (255) NOT NULL,
    PRIMARY KEY(CANONICAL_NAME_TYPE_ID)
);

# -----------------------------------------------------------------------
# CANONICAL_NAME
# -----------------------------------------------------------------------
drop table if exists CANONICAL_NAME;

CREATE TABLE CANONICAL_NAME
(
		            MOLECULE_ID INTEGER NOT NULL,
		            CANONICAL_NAME_TYPE_ID INTEGER NOT NULL,
		            NAME VARCHAR (255) NOT NULL,
		            NAME_SOUNDEX MEDIUMTEXT NOT NULL,
    PRIMARY KEY(MOLECULE_ID,CANONICAL_NAME_TYPE_ID),
    FOREIGN KEY (MOLECULE_ID) REFERENCES MOLECULE (MOLECULE_ID)
    ,
    FOREIGN KEY (CANONICAL_NAME_TYPE_ID) REFERENCES CANONICAL_NAME_TYPE (CANONICAL_NAME_TYPE_ID)
    
);

# -----------------------------------------------------------------------
# WISHLIST
# -----------------------------------------------------------------------
drop table if exists WISHLIST;

CREATE TABLE WISHLIST
(
		            USER_ID INTEGER,
		            DATE DATETIME NOT NULL,
		            SPECTRUM_TYPE_ID INTEGER NOT NULL,
		            MOLECULE_ID INTEGER NOT NULL,
    PRIMARY KEY(USER_ID,SPECTRUM_TYPE_ID,MOLECULE_ID),
    FOREIGN KEY (SPECTRUM_TYPE_ID) REFERENCES SPECTRUM_TYPE (SPECTRUM_TYPE_ID)
    ,
    FOREIGN KEY (USER_ID) REFERENCES TURBINE_USER (USER_ID)
    ,
    FOREIGN KEY (MOLECULE_ID) REFERENCES MOLECULE (MOLECULE_ID)
    
);

# -----------------------------------------------------------------------
# SPECTRUM
# -----------------------------------------------------------------------
drop table if exists SPECTRUM;

CREATE TABLE SPECTRUM
(
		            SPECTRUM_ID INTEGER NOT NULL,
		            DATE DATETIME NOT NULL,
		            SPECFILE MEDIUMTEXT NOT NULL,
		            SIMPLE_SPECFILE MEDIUMTEXT NOT NULL,
		            REVIEW_FLAG VARCHAR (8) NOT NULL,
		            REVIEW_KEY VARCHAR (6) NOT NULL,
		            FINGERPRINT BIGINT NOT NULL,
		            USER_ID INTEGER NOT NULL,
		            SPECTRUM_TYPE_ID INTEGER NOT NULL,
		            COMMENT LONGBLOB,
		            COMMENT_SOUNDEX MEDIUMTEXT NOT NULL,
		            MOLECULE_ID INTEGER,
		            MIXTURE_ID INTEGER,
		            SUBSTANCE_ID INTEGER,
		            PUBLISHED VARCHAR (8) NOT NULL,
                    RAW_DATA_VIEWS INTEGER default 0,
		            CORRELATION_SPECFILE MEDIUMTEXT NOT NULL,
    PRIMARY KEY(SPECTRUM_ID),
    FOREIGN KEY (USER_ID) REFERENCES TURBINE_USER (USER_ID)
    ,
    FOREIGN KEY (SPECTRUM_TYPE_ID) REFERENCES SPECTRUM_TYPE (SPECTRUM_TYPE_ID)
    ,
    FOREIGN KEY (MOLECULE_ID) REFERENCES MOLECULE (MOLECULE_ID)
    ,
    FOREIGN KEY (MIXTURE_ID) REFERENCES MIXTURE (MIXTURE_ID)
    ,
    FOREIGN KEY (SUBSTANCE_ID) REFERENCES SUBSTANCE (SUBSTANCE_ID)
    ,
    INDEX REVIEW_FLAG_INDEX (REVIEW_FLAG),
    INDEX MOLECULE_ID_INDEX (MOLECULE_ID),
    INDEX MIXTURE_ID_INDEX (MIXTURE_ID),
    INDEX SUBSTANCE_ID_INDEX (SUBSTANCE_ID),
    INDEX SPECTRUM_TYPE_ID_INDEX (SPECTRUM_TYPE_ID),
    INDEX SPECFILE_INDEX (SPECFILE(255)),
    INDEX DATE_INDEX (DATE),
    INDEX USER_ID_INDEX (USER_ID)
);

# -----------------------------------------------------------------------
# SPECTRUM_FILE
# -----------------------------------------------------------------------
drop table if exists SPECTRUM_FILE;

CREATE TABLE SPECTRUM_FILE
(
                    SPECTRUM_ID INTEGER NOT NULL,
                    TYPE VARCHAR (25) NOT NULL,
                    FORMAT VARCHAR (15) NOT NULL,
                    DATA LONGBLOB NOT NULL,
    PRIMARY KEY(SPECTRUM_ID,TYPE),
    FOREIGN KEY (SPECTRUM_ID) REFERENCES SPECTRUM (SPECTRUM_ID)
    
);

# -----------------------------------------------------------------------
# SPECTRUM_CONDITION
# -----------------------------------------------------------------------
drop table if exists SPECTRUM_CONDITION;

CREATE TABLE SPECTRUM_CONDITION
(
		            SPECTRUM_ID INTEGER NOT NULL,
		            CONDITION_ID INTEGER NOT NULL,
    PRIMARY KEY(SPECTRUM_ID,CONDITION_ID),
    FOREIGN KEY (SPECTRUM_ID) REFERENCES SPECTRUM (SPECTRUM_ID)
    ,
    FOREIGN KEY (CONDITION_ID) REFERENCES NMR_CONDITION (CONDITION_ID)
    
);

# -----------------------------------------------------------------------
# ATOM
# -----------------------------------------------------------------------
drop table if exists ATOM;

CREATE TABLE ATOM
(
		            ATOM_ID INTEGER NOT NULL,
		            SYMBOL VARCHAR (3) NOT NULL,
		            MOLECULE_ID INTEGER NOT NULL,
		            HOSE_CODE MEDIUMBLOB NOT NULL,
		            HOSE_CODE_WITH_RINGS MEDIUMBLOB NOT NULL,
		            ATOMIC_MASS INTEGER NOT NULL,
		            FORMAL_CHARGE INTEGER NOT NULL,
		            IS_AROMATIC VARCHAR (5) default 'false' NOT NULL,
		            IS_VISIBLE VARCHAR (5) default 'false' NOT NULL,
		            HETERO VARCHAR (5) default 'false' NOT NULL,
		            ATOM_NUMBER INTEGER NOT NULL,
		            EQUIVALENCE_NUMBER INTEGER NOT NULL,
    PRIMARY KEY(ATOM_ID),
    FOREIGN KEY (MOLECULE_ID) REFERENCES MOLECULE (MOLECULE_ID)
    ,
    INDEX MOLECULE_ID_INDEX (MOLECULE_ID),
    INDEX HOSE_CODE_INDEX (HOSE_CODE(256)),
    INDEX HOSE_CODE_WITH_RINGS_INDEX (HOSE_CODE_WITH_RINGS(256))
);

# -----------------------------------------------------------------------
# COORDINATE_SET_TYPE
# -----------------------------------------------------------------------
drop table if exists COORDINATE_SET_TYPE;

CREATE TABLE COORDINATE_SET_TYPE
(
		            COORDINATE_SET_TYPE_ID INTEGER NOT NULL,
		            DESCRIPTION LONGBLOB,
    PRIMARY KEY(COORDINATE_SET_TYPE_ID)
);

# -----------------------------------------------------------------------
# COORDINATE_SET_2D
# -----------------------------------------------------------------------
drop table if exists COORDINATE_SET_2D;

CREATE TABLE COORDINATE_SET_2D
(
		            COORDINATE_SET_TYPE_ID INTEGER NOT NULL,
		            ATOM_ID INTEGER NOT NULL,
		            X_COORDINATE DOUBLE NOT NULL,
		            Y_COORDINATE DOUBLE NOT NULL,
    PRIMARY KEY(COORDINATE_SET_TYPE_ID,ATOM_ID),
    FOREIGN KEY (ATOM_ID) REFERENCES ATOM (ATOM_ID)
    ,
    FOREIGN KEY (COORDINATE_SET_TYPE_ID) REFERENCES COORDINATE_SET_TYPE (COORDINATE_SET_TYPE_ID)
    
);

# -----------------------------------------------------------------------
# COORDINATE_SET_3D
# -----------------------------------------------------------------------
drop table if exists COORDINATE_SET_3D;

CREATE TABLE COORDINATE_SET_3D
(
		            COORDINATE_SET_TYPE_ID INTEGER NOT NULL,
		            ATOM_ID INTEGER NOT NULL,
		            X_COORDINATE DOUBLE NOT NULL,
		            Y_COORDINATE DOUBLE NOT NULL,
		            Z_COORDINATE DOUBLE NOT NULL,
    PRIMARY KEY(COORDINATE_SET_TYPE_ID,ATOM_ID),
    FOREIGN KEY (ATOM_ID) REFERENCES ATOM (ATOM_ID)
    ,
    FOREIGN KEY (COORDINATE_SET_TYPE_ID) REFERENCES COORDINATE_SET_TYPE (COORDINATE_SET_TYPE_ID)
    
);

# -----------------------------------------------------------------------
# BOND
# -----------------------------------------------------------------------
drop table if exists BOND;

CREATE TABLE BOND
(
		            BOND_ID INTEGER NOT NULL,
		            DEGREE INTEGER NOT NULL,
		            STEREO INTEGER NOT NULL,
		            IS_CONFIGURATION_SPECIFIED VARCHAR (5) default 'false' NOT NULL,
		            IS_AROMATIC VARCHAR (5) default 'false' NOT NULL,
    PRIMARY KEY(BOND_ID)
);

# -----------------------------------------------------------------------
# BOND_ATOM
# -----------------------------------------------------------------------
drop table if exists BOND_ATOM;

CREATE TABLE BOND_ATOM
(
		            BOND_ID INTEGER NOT NULL,
		            ATOM_ID INTEGER NOT NULL,
		            ORDER_NUMBER INTEGER NOT NULL,
    PRIMARY KEY(BOND_ID,ATOM_ID),
    FOREIGN KEY (BOND_ID) REFERENCES BOND (BOND_ID)
    ,
    FOREIGN KEY (ATOM_ID) REFERENCES ATOM (ATOM_ID)
    
);

# -----------------------------------------------------------------------
# NMR_SIGNAL
# -----------------------------------------------------------------------
drop table if exists NMR_SIGNAL;

CREATE TABLE NMR_SIGNAL
(
		            SIGNAL_ID INTEGER NOT NULL,
		            INTENSITY REAL NOT NULL,
		            MULTIPLICITY VARCHAR (255),
		            SPECTRUM_ID INTEGER NOT NULL,
    PRIMARY KEY(SIGNAL_ID),
    FOREIGN KEY (SPECTRUM_ID) REFERENCES SPECTRUM (SPECTRUM_ID)
    ,
    INDEX SPECTRUM_ID_INDEX (SPECTRUM_ID),
    INDEX SPECTRUM_MULTIPLICITY_INDEX (SPECTRUM_ID, MULTIPLICITY)
);

# -----------------------------------------------------------------------
# COUPLING
# -----------------------------------------------------------------------
drop table if exists COUPLING;

CREATE TABLE COUPLING
(
                    SPECTRUM_ID INTEGER NOT NULL,
                    ATOM_ID_1 INTEGER,
                    ATOM_ID_2 INTEGER,
                    SIGNAL_ID INTEGER,
                    CONSTANT DOUBLE NOT NULL,
    FOREIGN KEY (ATOM_ID_1) REFERENCES ATOM (ATOM_ID)
    ,
    FOREIGN KEY (ATOM_ID_2) REFERENCES ATOM (ATOM_ID)
    ,
    FOREIGN KEY (SIGNAL_ID) REFERENCES NMR_SIGNAL (SIGNAL_ID)
    ,
    FOREIGN KEY (SPECTRUM_ID) REFERENCES SPECTRUM (SPECTRUM_ID)
    
);

# -----------------------------------------------------------------------
# SHIFT
# -----------------------------------------------------------------------
drop table if exists SHIFT;

CREATE TABLE SHIFT
(
		            SIGNAL_ID INTEGER NOT NULL,
		            AXIS INTEGER NOT NULL,
		            VALUE REAL NOT NULL,
    PRIMARY KEY(SIGNAL_ID,AXIS),
    INDEX AXIS_INDEX (AXIS),
    INDEX SIGNAL_ID_INDEX (SIGNAL_ID),
    FOREIGN KEY (SIGNAL_ID) REFERENCES NMR_SIGNAL (SIGNAL_ID)
    
);

# -----------------------------------------------------------------------
# SIGNAL_ATOM
# -----------------------------------------------------------------------
drop table if exists SIGNAL_ATOM;

CREATE TABLE SIGNAL_ATOM
(
		            SIGNAL_ID INTEGER NOT NULL,
		            AXIS INTEGER NOT NULL,
		            ATOM_ID INTEGER NOT NULL,
    PRIMARY KEY(SIGNAL_ID,AXIS,ATOM_ID),
    FOREIGN KEY (SIGNAL_ID) REFERENCES SHIFT (SIGNAL_ID)
    ,
    FOREIGN KEY (AXIS) REFERENCES SHIFT (AXIS)
    ,
    FOREIGN KEY (ATOM_ID) REFERENCES ATOM (ATOM_ID)
    
);

# -----------------------------------------------------------------------
# CHEMICAL_NAME
# -----------------------------------------------------------------------
drop table if exists CHEMICAL_NAME;

CREATE TABLE CHEMICAL_NAME
(
		            MOLECULE_ID INTEGER NOT NULL,
		            NAME VARCHAR (255) NOT NULL,
		            NAME_SOUNDEX VARCHAR (255) NOT NULL,
    PRIMARY KEY(MOLECULE_ID,NAME),
    FOREIGN KEY (MOLECULE_ID) REFERENCES MOLECULE (MOLECULE_ID)
    
);

# -----------------------------------------------------------------------
# USER_SPECTRUM
# -----------------------------------------------------------------------
drop table if exists USER_SPECTRUM;

CREATE TABLE USER_SPECTRUM
(
		            USER_ID INTEGER NOT NULL,
		            SPECTRUM_ID INTEGER NOT NULL,
		            USER_SPECTRUM_ID INTEGER NOT NULL,
		            ASSIGNMENT_DATE DATETIME,
		            REVIEW_DATE DATETIME,
    PRIMARY KEY(USER_ID,SPECTRUM_ID,USER_SPECTRUM_ID),
    FOREIGN KEY (USER_ID) REFERENCES TURBINE_USER (USER_ID)
    ,
    FOREIGN KEY (SPECTRUM_ID) REFERENCES SPECTRUM (SPECTRUM_ID)
    
);

# -----------------------------------------------------------------------
# USER_SPECTRUM_EDIT
# -----------------------------------------------------------------------
drop table if exists USER_SPECTRUM_EDIT;

CREATE TABLE USER_SPECTRUM_EDIT
(
		            USER_ID INTEGER NOT NULL,
		            SPECTRUM_ID_OLD INTEGER NOT NULL,
		            SPECTRUM_ID_NEW INTEGER NOT NULL,
		            EDIT DATETIME NOT NULL,
    PRIMARY KEY(USER_ID,SPECTRUM_ID_OLD,SPECTRUM_ID_NEW,EDIT),
    FOREIGN KEY (USER_ID) REFERENCES TURBINE_USER (USER_ID)
    ,
    FOREIGN KEY (SPECTRUM_ID_OLD) REFERENCES SPECTRUM (SPECTRUM_ID)
    ,
    FOREIGN KEY (SPECTRUM_ID_NEW) REFERENCES SPECTRUM (SPECTRUM_ID)
    
);

# -----------------------------------------------------------------------
# USER_SPECTRUM_MARK
# -----------------------------------------------------------------------
drop table if exists USER_SPECTRUM_MARK;

CREATE TABLE USER_SPECTRUM_MARK
(
		            USER_ID INTEGER NOT NULL,
		            SPECTRUM_ID INTEGER NOT NULL,
		            MARK INTEGER NOT NULL,
		            MARK_DATE DATETIME NOT NULL,
		            COMMENT MEDIUMTEXT NOT NULL,
    PRIMARY KEY(USER_ID,SPECTRUM_ID),
    FOREIGN KEY (USER_ID) REFERENCES TURBINE_USER (USER_ID)
    ,
    FOREIGN KEY (SPECTRUM_ID) REFERENCES SPECTRUM (SPECTRUM_ID)
    
);

# -----------------------------------------------------------------------
# MOLECULE_HYPERLINK
# -----------------------------------------------------------------------
drop table if exists MOLECULE_HYPERLINK;

CREATE TABLE MOLECULE_HYPERLINK
(
		            MOLECULE_ID INTEGER NOT NULL,
		            HYPERLINK MEDIUMTEXT NOT NULL,
		            DESCRIPTION MEDIUMTEXT NOT NULL,
		            DESCRIPTION_SOUNDEX MEDIUMTEXT NOT NULL,
    PRIMARY KEY(MOLECULE_ID,HYPERLINK(250)),
    FOREIGN KEY (MOLECULE_ID) REFERENCES MOLECULE (MOLECULE_ID)
    ,
    INDEX HYPERLINK_INDEX (HYPERLINK(250))
);

# -----------------------------------------------------------------------
# SPECTRUM_HYPERLINK
# -----------------------------------------------------------------------
drop table if exists SPECTRUM_HYPERLINK;

CREATE TABLE SPECTRUM_HYPERLINK
(
		            SPECTRUM_ID INTEGER NOT NULL,
		            HYPERLINK MEDIUMTEXT NOT NULL,
		            DESCRIPTION MEDIUMTEXT NOT NULL,
		            DESCRIPTION_SOUNDEX MEDIUMTEXT NOT NULL,
    PRIMARY KEY(SPECTRUM_ID,HYPERLINK(250)),
    FOREIGN KEY (SPECTRUM_ID) REFERENCES SPECTRUM (SPECTRUM_ID)
    ,
    INDEX HYPERLINK_INDEX (HYPERLINK(250))
);

# -----------------------------------------------------------------------
# KEYWORD
# -----------------------------------------------------------------------
drop table if exists KEYWORD;

CREATE TABLE KEYWORD
(
		            KEYWORD_ID INTEGER NOT NULL,
		            KEYWORD VARCHAR (255) NOT NULL,
		            KEYWORD_SOUNDEX MEDIUMTEXT NOT NULL,
    PRIMARY KEY(KEYWORD_ID)
);

# -----------------------------------------------------------------------
# MOLECULE_KEYWORD
# -----------------------------------------------------------------------
drop table if exists MOLECULE_KEYWORD;

CREATE TABLE MOLECULE_KEYWORD
(
		            MOLECULE_ID INTEGER NOT NULL,
		            KEYWORD_ID INTEGER NOT NULL,
    PRIMARY KEY(MOLECULE_ID,KEYWORD_ID),
    FOREIGN KEY (MOLECULE_ID) REFERENCES MOLECULE (MOLECULE_ID)
    ,
    FOREIGN KEY (KEYWORD_ID) REFERENCES KEYWORD (KEYWORD_ID)
    
);

# -----------------------------------------------------------------------
# SPECTRUM_KEYWORD
# -----------------------------------------------------------------------
drop table if exists SPECTRUM_KEYWORD;

CREATE TABLE SPECTRUM_KEYWORD
(
		            SPECTRUM_ID INTEGER NOT NULL,
		            KEYWORD_ID INTEGER NOT NULL,
    PRIMARY KEY(SPECTRUM_ID,KEYWORD_ID),
    FOREIGN KEY (SPECTRUM_ID) REFERENCES SPECTRUM (SPECTRUM_ID)
    ,
    FOREIGN KEY (KEYWORD_ID) REFERENCES KEYWORD (KEYWORD_ID)
    
);

# -----------------------------------------------------------------------
# SUBSTANCE_KEYWORD
# -----------------------------------------------------------------------
drop table if exists SUBSTANCE_KEYWORD;

create table SUBSTANCE_KEYWORD 
(
    SUBSTANCE_ID int(11) NOT NULL DEFAULT '0',
    KEYWORD_ID int(11) NOT NULL DEFAULT '0',
    PRIMARY KEY (`SUBSTANCE_ID`,`KEYWORD_ID`),
    FOREIGN KEY (`SUBSTANCE_ID`) REFERENCES `SUBSTANCE` (`SUBSTANCE_ID`)
    ,
    FOREIGN KEY (`KEYWORD_ID`) REFERENCES `KEYWORD` (`KEYWORD_ID`)

);

# -----------------------------------------------------------------------
# DATASET_REVIEW_KEY
# -----------------------------------------------------------------------
drop table if exists DATASET_REVIEW_KEY;

CREATE TABLE DATASET_REVIEW_KEY
(
    DATASET_REVIEW_KEY_ID INTEGER NOT NULL,
    MOLECULE_ID INTEGER NOT NULL,
    CONDITION_ID INTEGER NOT NULL,
    REVIEW_KEY VARCHAR(6) NOT NULL,
    PRIMARY KEY(DATASET_REVIEW_KEY_ID));


# -----------------------------------------------------------------------
# DATASET_REVIEW_KEY_KEYWORD
# -----------------------------------------------------------------------
drop table if exists DATASET_REVIEW_KEY_KEYWORD;

CREATE TABLE DATASET_REVIEW_KEY_KEYWORD
(
    DATASET_REVIEW_KEY_ID INTEGER NOT NULL,
    KEYWORD_ID INTEGER NOT NULL,
    PRIMARY KEY(DATASET_REVIEW_KEY_ID,KEYWORD_ID));


# -----------------------------------------------------------------------
# DATASET_DOI
# -----------------------------------------------------------------------
drop table if exists DATASET_DOI;

CREATE TABLE DATASET_DOI
(
    DATASET_DOI_ID INTEGER NOT NULL,
    MOLECULE_ID INTEGER NOT NULL,
    CONDITION_ID INTEGER NOT NULL,
    DOI MEDIUMTEXT NOT NULL,
    PRIMARY KEY(DATASET_DOI_ID));
    
    

# -----------------------------------------------------------------------
# DATASET_DOI_KEYWORD
# -----------------------------------------------------------------------
drop table if exists DATASET_DOI_KEYWORD;

CREATE TABLE DATASET_DOI_KEYWORD
(
    DATASET_DOI_ID INTEGER NOT NULL,
    KEYWORD_ID INTEGER NOT NULL,
    PRIMARY KEY(DATASET_DOI_ID,KEYWORD_ID));
    
ALTER TABLE DATASET_DOI
    ADD CONSTRAINT DATASET_DOI_FK_1
    FOREIGN KEY (MOLECULE_ID)
    REFERENCES MOLECULE (MOLECULE_ID)
;

ALTER TABLE DATASET_DOI
    ADD CONSTRAINT DATASET_DOI_FK_2
    FOREIGN KEY (CONDITION_ID)
    REFERENCES NMR_CONDITION (CONDITION_ID)
;

ALTER TABLE DATASET_DOI_KEYWORD
    ADD CONSTRAINT DATASET_DOI_KEYWORD_FK_1
    FOREIGN KEY (DATASET_DOI_ID)
    REFERENCES DATASET_DOI (DATASET_DOI_ID)
;

ALTER TABLE DATASET_DOI_KEYWORD
    ADD CONSTRAINT DATASET_DOI_KEYWORD_FK_2
    FOREIGN KEY (KEYWORD_ID)
    REFERENCES KEYWORD (KEYWORD_ID)

# -----------------------------------------------------------------------
# REVIEWGROUP
# -----------------------------------------------------------------------
drop table if exists REVIEWGROUP;

CREATE TABLE REVIEWGROUP
(
		            REVIEWGROUP_ID INTEGER NOT NULL,
		            REVIEWGROUP_NAME VARCHAR (20) NOT NULL,
		            USER_ID INTEGER,
    PRIMARY KEY(REVIEWGROUP_ID),
    UNIQUE (REVIEWGROUP_NAME(20)),
    FOREIGN KEY (USER_ID) REFERENCES TURBINE_USER (USER_ID)
);

# -----------------------------------------------------------------------
# REVIEWGROUP_USER
# -----------------------------------------------------------------------
drop table if exists REVIEWGROUP_USER;

CREATE TABLE REVIEWGROUP_USER
(
		            REVIEWGROUP_ID INTEGER NOT NULL,
		            USER_ID INTEGER NOT NULL,
    PRIMARY KEY(REVIEWGROUP_ID,USER_ID),
    FOREIGN KEY (USER_ID) REFERENCES TURBINE_USER (USER_ID)
    ,
    FOREIGN KEY (REVIEWGROUP_ID) REFERENCES REVIEWGROUP (REVIEWGROUP_ID)
    
);

# -----------------------------------------------------------------------
# REVIEWTYPE
# -----------------------------------------------------------------------
drop table if exists REVIEWTYPE;

CREATE TABLE REVIEWTYPE
(
                    REVIEWTYPE_ID INTEGER NOT NULL,
                    REVIEWTYPE_NAME VARCHAR (20) NOT NULL,
                    USER_ID INTEGER,
    PRIMARY KEY(REVIEWTYPE_ID),
    FOREIGN KEY (USER_ID) REFERENCES TURBINE_USER (USER_ID)
    ,
    UNIQUE (REVIEWTYPE_NAME(20))
);

# -----------------------------------------------------------------------
# REVIEWTYPE_USER
# -----------------------------------------------------------------------
drop table if exists REVIEWTYPE_USER;

CREATE TABLE REVIEWTYPE_USER
(
                    REVIEWTYPE_ID INTEGER NOT NULL,
                    USER_ID INTEGER NOT NULL,
    PRIMARY KEY(REVIEWTYPE_ID,USER_ID),
    FOREIGN KEY (USER_ID) REFERENCES TURBINE_USER (USER_ID)
    ,
    FOREIGN KEY (REVIEWTYPE_ID) REFERENCES REVIEWTYPE (REVIEWTYPE_ID)
    
);

# -----------------------------------------------------------------------
# LITERATURE
# -----------------------------------------------------------------------
drop table if exists LITERATURE;

CREATE TABLE LITERATURE
(
		            LITERATURE_ID INTEGER NOT NULL,
		            TITLE VARCHAR (255) NOT NULL,
		            SUBTITLE VARCHAR (255),
		            TITLE_TOTAL_SOUNDEX VARCHAR (255) NOT NULL,
		            TITLE_TOTAL VARCHAR (255) NOT NULL,
		            ET_AL VARCHAR (5) default 'false' NOT NULL,
		            URL VARCHAR (255),
		            DOI VARCHAR (255),
    PRIMARY KEY(LITERATURE_ID)
);

# -----------------------------------------------------------------------
# SPECTRUM_LITERATURE
# -----------------------------------------------------------------------
drop table if exists SPECTRUM_LITERATURE;

CREATE TABLE SPECTRUM_LITERATURE
(
		            SPECTRUM_ID INTEGER NOT NULL,
		            LITERATURE_ID INTEGER NOT NULL,
    PRIMARY KEY(SPECTRUM_ID,LITERATURE_ID),
    FOREIGN KEY (SPECTRUM_ID) REFERENCES SPECTRUM (SPECTRUM_ID)
    ,
    FOREIGN KEY (LITERATURE_ID) REFERENCES LITERATURE (LITERATURE_ID)
    
);

# -----------------------------------------------------------------------
# IDENTIFIER
# -----------------------------------------------------------------------
drop table if exists IDENTIFIER;

CREATE TABLE IDENTIFIER
(
		            ATOM_ID INTEGER NOT NULL,
		            SPECTRUM_ID INTEGER NOT NULL,
		            IDENTIFIER VARCHAR (255) NOT NULL,
    PRIMARY KEY(ATOM_ID,SPECTRUM_ID),
    FOREIGN KEY (ATOM_ID) REFERENCES ATOM (ATOM_ID)
    ,
    FOREIGN KEY (SPECTRUM_ID) REFERENCES SPECTRUM (SPECTRUM_ID)
);

# -----------------------------------------------------------------------
# JOURNAL
# -----------------------------------------------------------------------
drop table if exists JOURNAL;

CREATE TABLE JOURNAL
(
		            JOURNAL_ID INTEGER NOT NULL,
		            TITLE VARCHAR (255) NOT NULL,
		            SUBTITLE VARCHAR (255),
		            ITA VARCHAR (255),
    PRIMARY KEY(JOURNAL_ID)
);

# -----------------------------------------------------------------------
# JOURNAL_VOLUME
# -----------------------------------------------------------------------
drop table if exists JOURNAL_VOLUME;

CREATE TABLE JOURNAL_VOLUME
(
		            JOURNAL_VOLUME_ID INTEGER NOT NULL,
		            VOLUME INTEGER NOT NULL,
		            YEAR INTEGER NOT NULL,
		            JOURNAL_ID INTEGER NOT NULL,
    PRIMARY KEY(JOURNAL_VOLUME_ID),
    FOREIGN KEY (JOURNAL_ID) REFERENCES JOURNAL (JOURNAL_ID)
    ,
    INDEX JOURNAL_ID_INDEX (JOURNAL_ID)
);

# -----------------------------------------------------------------------
# ARTICLE
# -----------------------------------------------------------------------
drop table if exists ARTICLE;

CREATE TABLE ARTICLE
(
		            LITERATURE_ID INTEGER NOT NULL,
		            PAGES_FROM INTEGER NOT NULL,
		            PAGES_TO INTEGER NOT NULL,
		            ISSUE_NUMBER INTEGER NOT NULL,
		            PUBLISHED_IN_BOOK INTEGER,
		            PUBLISHED_IN_JOURNAL INTEGER,
    PRIMARY KEY(LITERATURE_ID),
    FOREIGN KEY (LITERATURE_ID) REFERENCES LITERATURE (LITERATURE_ID)
    ,
    FOREIGN KEY (PUBLISHED_IN_BOOK) REFERENCES LITERATURE (LITERATURE_ID)
    ,
    FOREIGN KEY (PUBLISHED_IN_JOURNAL) REFERENCES JOURNAL_VOLUME (JOURNAL_VOLUME_ID)
    ,
    INDEX PUBLISHED_IN_BOOK_INDEX (PUBLISHED_IN_BOOK),
    INDEX PUBLISHED_IN_JOURNAL_INDEX (PUBLISHED_IN_JOURNAL)
);

# -----------------------------------------------------------------------
# BOOK
# -----------------------------------------------------------------------
drop table if exists BOOK;

CREATE TABLE BOOK
(
		            LITERATURE_ID INTEGER NOT NULL,
    PRIMARY KEY(LITERATURE_ID),
    FOREIGN KEY (LITERATURE_ID) REFERENCES LITERATURE (LITERATURE_ID)
    
);

# -----------------------------------------------------------------------
# AUTHOR
# -----------------------------------------------------------------------
drop table if exists AUTHOR;

CREATE TABLE AUTHOR
(
		            AUTHOR_ID INTEGER NOT NULL,
		            SURNAME VARCHAR (255) NOT NULL,
		            NAME VARCHAR (255) NOT NULL,
		            NAME_TOTAL_SOUNDEX VARCHAR (255) NOT NULL,
		            NAME_TOTAL VARCHAR (255) NOT NULL,
    PRIMARY KEY(AUTHOR_ID)
);

# -----------------------------------------------------------------------
# IS_EDITOR
# -----------------------------------------------------------------------
drop table if exists IS_EDITOR;

CREATE TABLE IS_EDITOR
(
		            AUTHOR_ID INTEGER NOT NULL,
		            LITERATURE_ID INTEGER NOT NULL,
		            EDITOR_ORDER INTEGER default 0 NOT NULL,
    PRIMARY KEY(AUTHOR_ID,LITERATURE_ID),
    FOREIGN KEY (AUTHOR_ID) REFERENCES AUTHOR (AUTHOR_ID)
    ,
    FOREIGN KEY (LITERATURE_ID) REFERENCES LITERATURE (LITERATURE_ID)
    
);

# -----------------------------------------------------------------------
# IS_AUTHOR
# -----------------------------------------------------------------------
drop table if exists IS_AUTHOR;

CREATE TABLE IS_AUTHOR
(
		            AUTHOR_ID INTEGER NOT NULL,
		            LITERATURE_ID INTEGER NOT NULL,
		            AUTHOR_ORDER INTEGER default 0 NOT NULL,
    PRIMARY KEY(AUTHOR_ID,LITERATURE_ID),
    FOREIGN KEY (AUTHOR_ID) REFERENCES AUTHOR (AUTHOR_ID)
    ,
    FOREIGN KEY (LITERATURE_ID) REFERENCES LITERATURE (LITERATURE_ID)
    
);

# -----------------------------------------------------------------------
# PUBLISHER
# -----------------------------------------------------------------------
drop table if exists PUBLISHER;

CREATE TABLE PUBLISHER
(
		            PUBLISHER_ID INTEGER NOT NULL,
		            NAME VARCHAR (255) NOT NULL,
		            PLACE VARCHAR (255) NOT NULL,
    PRIMARY KEY(PUBLISHER_ID)
);

# -----------------------------------------------------------------------
# BOOK_PUBLISHER
# -----------------------------------------------------------------------
drop table if exists BOOK_PUBLISHER;

CREATE TABLE BOOK_PUBLISHER
(
		            PUBLISHER_ID INTEGER NOT NULL,
		            LITERATURE_ID INTEGER NOT NULL,
		            YEAR INTEGER NOT NULL,
    PRIMARY KEY(PUBLISHER_ID,LITERATURE_ID),
    FOREIGN KEY (PUBLISHER_ID) REFERENCES PUBLISHER (PUBLISHER_ID)
    ,
    FOREIGN KEY (LITERATURE_ID) REFERENCES LITERATURE (LITERATURE_ID)
    
);

# -----------------------------------------------------------------------
# LABGROUP
# -----------------------------------------------------------------------
drop table if exists LABGROUP;

CREATE TABLE LABGROUP
(
		            LABGROUP_ID INTEGER NOT NULL,
		            LABGROUP_NAME VARCHAR (20) NOT NULL,
		            LEADER INTEGER NOT NULL,
    PRIMARY KEY(LABGROUP_ID),
    FOREIGN KEY (LEADER) REFERENCES TURBINE_USER (USER_ID)
    ,
    UNIQUE (LABGROUP_NAME(20))
);

# -----------------------------------------------------------------------
# USER_HISTORY
# -----------------------------------------------------------------------
drop table if exists USER_HISTORY;

CREATE TABLE USER_HISTORY
(
		            USER_HISTORY_ID INTEGER NOT NULL,
		            USER_ID INTEGER NOT NULL,
		            JOIN_DATE DATETIME NOT NULL,
		            LEAVE_DATE DATETIME,
		            LABGROUP_ID INTEGER,
		            AFFILIATION_1 VARCHAR (99),
		            AFFILIATION_2 VARCHAR (99),
    PRIMARY KEY(USER_HISTORY_ID),
    FOREIGN KEY (USER_ID) REFERENCES TURBINE_USER (USER_ID)
    ,
    FOREIGN KEY (LABGROUP_ID) REFERENCES LABGROUP (LABGROUP_ID)
    
);

# -----------------------------------------------------------------------
# MACHINE
# -----------------------------------------------------------------------
drop table if exists MACHINE;

CREATE TABLE MACHINE
(
		            MACHINE_ID INTEGER NOT NULL,
		            NAME VARCHAR (20) NOT NULL,
		            FIELD_STRENGTH INTEGER NOT NULL,
		            VALID VARCHAR (10) NOT NULL,
		            LABGROUP_ID INTEGER NOT NULL,
		            DEFAULT_MACHINE INTEGER NOT NULL,
		            MEASURE_SELF VARCHAR (10) NOT NULL,
		            MEASURE_OPERATOR VARCHAR (10) NOT NULL,
		            MEASURE_CHANGER VARCHAR (10) NOT NULL,
                    MEASURE_BARCODE VARCHAR (10) NOT NULL,
			    MEASURE_SELF_NIGHT VARCHAR(10) NOT NULL,
			    MEASURE_OPERATOR_NIGHT VARCHAR(10) NOT NULL,
			    MEASURE_CHANGER_NIGHT VARCHAR(10) NOT NULL,
                MEASURE_BARCODE_NIGHT VARCHAR(10) NOT NULL,
			    MEASURE_SELF_ENFORCE_STRUCTURE VARCHAR(10) NOT NULL,
			    MEASURE_OPERATOR_ENFORCE_STRUCTURE VARCHAR(10) NOT NULL,
			    MEASURE_CHANGER_ENFORCE_STRUCTURE VARCHAR(10) NOT NULL,
                MEASURE_BARCODE_ENFORCE_STRUCTURE VARCHAR(10) NOT NULL,
			    DEFAULT_MODE VARCHAR(10) NOT NULL,
			    DEFAULT_MODE_NIGHT VARCHAR(10) NOT NULL,
		            ICONNMR_INTEGRATION VARCHAR (10) NOT NULL,
		            SCP_MACHINE_NAME VARCHAR (50),
		            SCP_PATH VARCHAR (50),
		            SHIFT_OPERATION_MON VARCHAR (8) NOT NULL,
		            SHIFT_OPERATION_TUE VARCHAR (8) NOT NULL,
		            SHIFT_OPERATION_WED VARCHAR (8) NOT NULL,
		            SHIFT_OPERATION_THU VARCHAR (8) NOT NULL,
		            SHIFT_OPERATION_FRI VARCHAR (8) NOT NULL,
		            SHIFT_OPERATION_SAT VARCHAR (8) NOT NULL,
		            SHIFT_OPERATION_SUN VARCHAR (8) NOT NULL,
    HOUR_DAY_MONDAY INTEGER NOT NULL,
    MINUTE_DAY_MONDAY INTEGER NOT NULL,
    HOUR_NIGHT_MONDAY INTEGER NOT NULL,
    MINUTE_NIGHT_MONDAY INTEGER NOT NULL,
    HOUR_DAY_TUESDAY INTEGER NOT NULL,
    MINUTE_DAY_TUESDAY INTEGER NOT NULL,
    HOUR_NIGHT_TUESDAY INTEGER NOT NULL,
    MINUTE_NIGHT_TUESDAY INTEGER NOT NULL,
    HOUR_DAY_WEDNESDAY INTEGER NOT NULL,
    MINUTE_DAY_WEDNESDAY INTEGER NOT NULL,
    HOUR_NIGHT_WEDNESDAY INTEGER NOT NULL,
    MINUTE_NIGHT_WEDNESDAY INTEGER NOT NULL,
    HOUR_DAY_THURSDAY INTEGER NOT NULL,
    MINUTE_DAY_THURSDAY INTEGER NOT NULL,
    HOUR_NIGHT_THURSDAY INTEGER NOT NULL,
    MINUTE_NIGHT_THURSDAY INTEGER NOT NULL,
    HOUR_DAY_FRIDAY INTEGER NOT NULL,
    MINUTE_DAY_FRIDAY INTEGER NOT NULL,
    HOUR_NIGHT_FRIDAY INTEGER NOT NULL,
    MINUTE_NIGHT_FRIDAY INTEGER NOT NULL,
    HOUR_DAY_SATURDAY INTEGER NOT NULL,
    MINUTE_DAY_SATURDAY INTEGER NOT NULL,
    HOUR_NIGHT_SATURDAY INTEGER NOT NULL,
    MINUTE_NIGHT_SATURDAY INTEGER NOT NULL,
    HOUR_DAY_SUNDAY INTEGER NOT NULL,
    MINUTE_DAY_SUNDAY INTEGER NOT NULL,
    HOUR_NIGHT_SUNDAY INTEGER NOT NULL,
    MINUTE_NIGHT_SUNDAY INTEGER NOT NULL,
		            CHANGE_TIME INTEGER NOT NULL,
		            SFTP_HOST VARCHAR (50),
		            SFTP_USER VARCHAR (50),
		            SFTP_PASSWORD VARCHAR (50),
		            SCP_PASSWORD VARCHAR (50),
		            SCP_USERNAME VARCHAR (50),
                FORCE_WEEKEND VARCHAR(10) NOT NULL,
                NIGHT_LIMIT INTEGER,
                DEFAULT_ON_CLIENT VARCHAR(20) NOT NULL default "",
                HAS_LOW_PRIO VARCHAR(20) NOT NULL,
                NUMBER_OF_HOLDERS INTEGER,
    PRIMARY KEY(MACHINE_ID),
    FOREIGN KEY (LABGROUP_ID) REFERENCES LABGROUP (LABGROUP_ID)
    ,
    FOREIGN KEY (FIELD_STRENGTH) REFERENCES NMR_CONDITION (CONDITION_ID)
    
);

# -----------------------------------------------------------------------
# MACHINE_USER_EXCLUDED
# -----------------------------------------------------------------------
drop table if exists MACHINE_USER_EXCLUDED;

CREATE TABLE MACHINE_USER_EXCLUDED
(
    MACHINE_ID INTEGER NOT NULL,
    USER_ID INTEGER NOT NULL,
    PRIMARY KEY(MACHINE_ID,USER_ID),
    FOREIGN KEY (MACHINE_ID) REFERENCES MACHINE (MACHINE_ID)
    ,
    FOREIGN KEY (USER_ID) REFERENCES TURBINE_USER (USER_ID)
    
);


# -----------------------------------------------------------------------
# MACHINE_CONDITION
# -----------------------------------------------------------------------
drop table if exists MACHINE_CONDITION;

CREATE TABLE MACHINE_CONDITION
(
		            MACHINE_ID INTEGER,
		            CONDITION_ID INTEGER NOT NULL,
		            NAME_ICONNMR VARCHAR (50),
		            ORDER_POSITION INTEGER NOT NULL,
    PRIMARY KEY(MACHINE_ID,CONDITION_ID),
    FOREIGN KEY (MACHINE_ID) REFERENCES MACHINE (MACHINE_ID)
    ,
    FOREIGN KEY (CONDITION_ID) REFERENCES NMR_CONDITION (CONDITION_ID)
    
);

# -----------------------------------------------------------------------
# EXPERIMENT_CATEGORY
# -----------------------------------------------------------------------
drop table if exists EXPERIMENT_CATEGORY;

CREATE TABLE EXPERIMENT_CATEGORY
(
		            EXPERIMENT_CATEGORY_ID INTEGER NOT NULL,
		            NAME VARCHAR (20) NOT NULL,
		            ORDER_POSITION INTEGER NOT NULL,
    PRIMARY KEY(EXPERIMENT_CATEGORY_ID)
);

# -----------------------------------------------------------------------
# EXPERIMENT_REPORT_CATEGORY
# -----------------------------------------------------------------------
drop table if exists EXPERIMENT_REPORT_CATEGORY;

CREATE TABLE EXPERIMENT_REPORT_CATEGORY
(
		            EXPERIMENT_REPORT_CATEGORY_ID INTEGER NOT NULL,
		            NAME VARCHAR (20) NOT NULL,
		            ORDER_POSITION INTEGER NOT NULL,
    PRIMARY KEY(EXPERIMENT_REPORT_CATEGORY_ID)
);

# -----------------------------------------------------------------------
# EXPERIMENT
# -----------------------------------------------------------------------
drop table if exists EXPERIMENT;

CREATE TABLE EXPERIMENT
(
		            EXPERIMENT_ID INTEGER NOT NULL,
		            NAME VARCHAR (50) NOT NULL,
		            VALID VARCHAR (10) NOT NULL,
		            LABGROUP_ID INTEGER NOT NULL,
		            EXPERIMENT_CATEGORY_ID INTEGER NOT NULL,
		            EXPERIMENT_REPORT_CATEGORY_ID INTEGER NOT NULL,
		            MAX_NUMBER INTEGER NOT NULL,
		            LINE_BEFORE VARCHAR (50) NOT NULL,
		            AUTOMATION_ORDER INTEGER NOT NULL,
    SUB_EXPERIMENT_1_ID INTEGER,
    SUB_EXPERIMENT_2_ID INTEGER,
    SUB_EXPERIMENT_3_ID INTEGER,
    ORDER_POSITION INTEGER NOT NULL,
    IDENTICAL_WITH INTEGER,
     PRIMARY KEY(EXPERIMENT_ID),
    FOREIGN KEY (EXPERIMENT_CATEGORY_ID) REFERENCES EXPERIMENT_CATEGORY (EXPERIMENT_CATEGORY_ID)
    ,
    FOREIGN KEY (EXPERIMENT_REPORT_CATEGORY_ID) REFERENCES EXPERIMENT_REPORT_CATEGORY (EXPERIMENT_REPORT_CATEGORY_ID)
    ,
    FOREIGN KEY (LABGROUP_ID) REFERENCES LABGROUP (LABGROUP_ID)
    ,
    FOREIGN KEY (SUB_EXPERIMENT_1_ID) REFERENCES EXPERIMENT (EXPERIMENT_ID)
    ,
    FOREIGN KEY (SUB_EXPERIMENT_2_ID) REFERENCES EXPERIMENT (EXPERIMENT_ID)    
    ,
    FOREIGN KEY (SUB_EXPERIMENT_3_ID) REFERENCES EXPERIMENT (EXPERIMENT_ID)    
    ,
    FOREIGN KEY (IDENTICAL_WITH) REFERENCES EXPERIMENT (EXPERIMENT_ID)    
);

# -----------------------------------------------------------------------
# EXPERIMENT_PARAMETER
# -----------------------------------------------------------------------
drop table if exists EXPERIMENT_PARAMETER;

CREATE TABLE EXPERIMENT_PARAMETER
(
    PARAMETER_ID INTEGER NOT NULL,
    NAME_DISPLAY VARCHAR(50) NOT NULL,
    NAME_ICONNMR VARCHAR(50) NOT NULL,
    VISIBLE VARCHAR(8) NOT NULL,
    PRIMARY KEY(PARAMETER_ID));    
    
# -----------------------------------------------------------------------
# EXPERIMENT_MACHINE
# -----------------------------------------------------------------------
drop table if exists EXPERIMENT_MACHINE;

CREATE TABLE EXPERIMENT_MACHINE
(
                    EXPERIMENT_MACHINE_ID INTEGER NOT NULL,
		            EXPERIMENT_ID INTEGER NOT NULL,
		            MACHINE_ID INTEGER NOT NULL,
		            NAME_ICONNMR VARCHAR (50) NOT NULL,
		            VALID_DAY VARCHAR (8) NOT NULL,
		            VALID_NIGHT VARCHAR (8) NOT NULL,
		            DURATION INTEGER NOT NULL,
                    COMPULSORY VARCHAR(8) NOT NULL,
					HIDE_HOUR_BEGIN INTEGER,
				    HIDE_MINUTE_BEGIN INTEGER,
				    HIDE_HOUR_END INTEGER,
				    HIDE_MINUTE_END INTEGER,
				    PRIMARY KEY(EXPERIMENT_MACHINE_ID),
    FOREIGN KEY (MACHINE_ID) REFERENCES MACHINE (MACHINE_ID)
    ,
    FOREIGN KEY (EXPERIMENT_ID) REFERENCES EXPERIMENT (EXPERIMENT_ID)
    
);

# -----------------------------------------------------------------------
# EXPERIMENT_MACHINE_PARAMETER
# -----------------------------------------------------------------------
drop table if exists EXPERIMENT_MACHINE_PARAMETER;

CREATE TABLE EXPERIMENT_MACHINE_PARAMETER
(
    EXPERIMENT_MACHINE_PARAMETER_ID INTEGER NOT NULL,
    EXPERIMENT_MACHINE_ID INTEGER NOT NULL,
    PARAMETER_ID INTEGER NOT NULL,
    DEFAULT_VALUE FLOAT NOT NULL,
    PRIMARY KEY(EXPERIMENT_MACHINE_PARAMETER_ID),
    FOREIGN KEY (EXPERIMENT_MACHINE_ID) REFERENCES EXPERIMENT_MACHINE (EXPERIMENT_MACHINE_ID)
    ,
    FOREIGN KEY (PARAMETER_ID) REFERENCES EXPERIMENT_PARAMETER (PARAMETER_ID)
    
);

# -----------------------------------------------------------------------
# EXCLUDING_EXPERIMENTS
# -----------------------------------------------------------------------
drop table if exists EXCLUDING_EXPERIMENTS;

CREATE TABLE EXCLUDING_EXPERIMENTS
(
		            MACHINE_ID INTEGER NOT NULL,
		            EXPERIMENT_ID_1 INTEGER NOT NULL,
		            EXPERIMENT_ID_2 INTEGER NOT NULL,
    PRIMARY KEY(MACHINE_ID,EXPERIMENT_ID_1,EXPERIMENT_ID_2),
    FOREIGN KEY (MACHINE_ID) REFERENCES MACHINE (MACHINE_ID)
    ,
    FOREIGN KEY (EXPERIMENT_ID_1) REFERENCES EXPERIMENT (EXPERIMENT_ID)
    ,
    FOREIGN KEY (EXPERIMENT_ID_2) REFERENCES EXPERIMENT (EXPERIMENT_ID)
    
);

# -----------------------------------------------------------------------
# SAMPLE
# -----------------------------------------------------------------------
drop table if exists SAMPLE;

CREATE TABLE SAMPLE
(
		            SAMPLE_ID INTEGER NOT NULL,
		            USERS_ID VARCHAR (20) NOT NULL,
		            USERS_ID_COMMENT VARCHAR (40) NOT NULL,
		            USER_ID INTEGER NOT NULL,
		            SOLVENT INTEGER NOT NULL,
		            MACHINE INTEGER NOT NULL,
		            PROBABLE_STRUCTURE INTEGER,
		            DATE DATETIME NOT NULL,
		            SPECIAL_CARE VARCHAR (1000) NOT NULL,
		            OTHER_WISHED_SPECTRUM VARCHAR (50) NOT NULL,
		            FINISHED VARCHAR (8) NOT NULL,
		            PROCESS VARCHAR (8) NOT NULL,
		            ATTACHMENT LONGBLOB NOT NULL,
		            ATTACHMENT_NAME VARCHAR (20) NOT NULL,
		            HOLDER_NO INTEGER NOT NULL,
                    FINISHED_EXPERIMENTS INTEGER,
				    print INTEGER,
				    scan INTEGER,
				    complete INTEGER,
                    SHORT_ID INTEGER,
                    LOW_PRIO VARCHAR(20) NOT NULL,
                    ADDITIONAL_DURATION INTEGER,
    PRIMARY KEY(SAMPLE_ID),
    FOREIGN KEY (USER_ID) REFERENCES TURBINE_USER (USER_ID)
    ,
    FOREIGN KEY (SOLVENT) REFERENCES NMR_CONDITION (CONDITION_ID)
    ,
    FOREIGN KEY (MACHINE) REFERENCES MACHINE (MACHINE_ID)
    ,
    FOREIGN KEY (PROBABLE_STRUCTURE) REFERENCES MOLECULE (MOLECULE_ID)
    ,
    INDEX USER_ID_INDEX (USER_ID)
);

# -----------------------------------------------------------------------
# EXPERIMENT_SAMPLE
# -----------------------------------------------------------------------
drop table if exists EXPERIMENT_SAMPLE;

CREATE TABLE EXPERIMENT_SAMPLE
(
		            EXPERIMENT_ID INTEGER NOT NULL,
		            SAMPLE_ID INTEGER NOT NULL,
                    `COUNT` int NOT NULL,
                    `SEQUENCE` int NOT NULL,
                    `DURATION` int DEFAULT NULL,
    PRIMARY KEY(EXPERIMENT_ID,SAMPLE_ID,SEQUENCE),
    FOREIGN KEY (SAMPLE_ID) REFERENCES SAMPLE (SAMPLE_ID)
    ,
    FOREIGN KEY (EXPERIMENT_ID) REFERENCES EXPERIMENT (EXPERIMENT_ID)
    
);

# -----------------------------------------------------------------------
# EXPERIMENT_SAMPLE_PARAMETER
# -----------------------------------------------------------------------
drop table if exists EXPERIMENT_SAMPLE_PARAMETER;

CREATE TABLE EXPERIMENT_SAMPLE_PARAMETER
(
    EXPERIMENT_ID INTEGER NOT NULL,
    SAMPLE_ID INTEGER NOT NULL,
    PARAMETER_ID INTEGER NOT NULL,
    SEQUENCE INTEGER NOT NULL,
    VALUE FLOAT NOT NULL,
    PRIMARY KEY(EXPERIMENT_ID,SAMPLE_ID,PARAMETER_ID,SEQUENCE),
    FOREIGN KEY (SAMPLE_ID) REFERENCES SAMPLE (SAMPLE_ID)
    ,
    FOREIGN KEY (EXPERIMENT_ID) REFERENCES EXPERIMENT (EXPERIMENT_ID)
    ,
    FOREIGN KEY (PARAMETER_ID) REFERENCES EXPERIMENT_PARAMETER (PARAMETER_ID)
);

# -----------------------------------------------------------------------
# RAW_FILE
# -----------------------------------------------------------------------
drop table if exists RAW_FILE;

CREATE TABLE RAW_FILE
(
		            RAW_FILE_ID INTEGER NOT NULL,
		            SAMPLE_ID INTEGER NOT NULL,
		            URL VARCHAR (255) NOT NULL,
		            CONTAINED_FILES MEDIUMTEXT NOT NULL,
		            ASSIGNED VARCHAR (255) NOT NULL,
		            MD5 VARCHAR (32) NOT NULL,
                    DATE DATETIME,
    PRIMARY KEY(RAW_FILE_ID),
    FOREIGN KEY (SAMPLE_ID) REFERENCES SAMPLE (SAMPLE_ID)
    
);

# -----------------------------------------------------------------------
# RAW_FILE_SPECTRUM
# -----------------------------------------------------------------------
drop table if exists RAW_FILE_SPECTRUM;

CREATE TABLE RAW_FILE_SPECTRUM
(
		            RAW_FILE_ID INTEGER NOT NULL,
		            SPECTRUM_ID INTEGER NOT NULL,
    PRIMARY KEY(RAW_FILE_ID,SPECTRUM_ID),
    FOREIGN KEY (RAW_FILE_ID) REFERENCES RAW_FILE (RAW_FILE_ID)
    ,
    FOREIGN KEY (SPECTRUM_ID) REFERENCES SPECTRUM (SPECTRUM_ID)
    
);

# -----------------------------------------------------------------------
# QUICK_CHECK
# -----------------------------------------------------------------------
drop table if exists QUICK_CHECK;

CREATE TABLE QUICK_CHECK
(
                    QUICK_CHECK_ID INTEGER NOT NULL,
                    USER_ID INTEGER,
                    DATE DATETIME NOT NULL,
                    NAME VARCHAR (255) NOT NULL,
                    CML MEDIUMTEXT NOT NULL,
    PRIMARY KEY(QUICK_CHECK_ID),
    FOREIGN KEY (USER_ID) REFERENCES TURBINE_USER (USER_ID)
    
);  
  

# -----------------------------------------------------------------------
# AFFILIATION_1_LOV
# -----------------------------------------------------------------------
drop table if exists AFFILIATION_1_LOV;

CREATE TABLE AFFILIATION_1_LOV
(
		            AFFILIATION_1_LOV_ID INTEGER NOT NULL,
		            AFFILIATION_1 VARCHAR (255) NOT NULL,
    PRIMARY KEY(AFFILIATION_1_LOV_ID)
);

# -----------------------------------------------------------------------
# AFFILIATION_2_LOV
# -----------------------------------------------------------------------
drop table if exists AFFILIATION_2_LOV;

CREATE TABLE AFFILIATION_2_LOV
(
		            AFFILIATION_2_LOV_ID INTEGER NOT NULL,
		            AFFILIATION_1_LOV_ID INTEGER NOT NULL,
		            AFFILIATION_2 VARCHAR (255) NOT NULL,
                            DESCRIPTION VARCHAR (255) NOT NULL,
    PRIMARY KEY(AFFILIATION_2_LOV_ID),
    FOREIGN KEY (AFFILIATION_1_LOV_ID) REFERENCES AFFILIATION_1_LOV (AFFILIATION_1_LOV_ID)
);  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
