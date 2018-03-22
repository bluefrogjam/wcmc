CREATE TABLE STANDARD_HIST (
	"bin_id" float,
	"qualifier" float,
	"min_ratio" double precision,
	"min_apex_sn" double precision,
	"max_ratio" double precision,
	"min_distance_ratio" double precision,
	"max_distance_ratio" double precision,
	"min_similarity" double precision,
	"required" varchar(5),
	"changed_at" timestamp DEFAULT NULL,
	"changed_by" varchar(255),
	"id" float
);

CREATE TABLE LOCKING (
	"id" float,
	"description" varchar(255),
	"locked_by" varchar(255)
);

CREATE TABLE META_KEY (
	"key_id" float,
	"key" varchar(255)
);
CREATE INDEX META_KEY_INDEX ON META_KEY ("key");

CREATE TABLE BIN_COMPARE (
	"id" float NOT NULL,
	"bin_id" float NOT NULL,
	"compare" float
);
ALTER TABLE BIN_COMPARE ADD PRIMARY KEY ("id");

CREATE TABLE QUANTIFICATION (
	"sample_id" integer NOT NULL,
	"version" integer,
	"result" bytea
);
ALTER TABLE QUANTIFICATION ADD PRIMARY KEY ("sample_id");
CREATE UNIQUE INDEX QUALIFICATION_INDEX_SAMPLE_ID ON QUANTIFICATION ("sample_id");

CREATE TABLE RESULT_LINK (
	"result_id" float,
	"sample_id" float,
	"id" float NOT NULL,
	"class" varchar(256)
);
ALTER TABLE RESULT_LINK ADD PRIMARY KEY ("id");
CREATE INDEX RESULT_LINK_INDEX ON RESULT_LINK ("result_id");

CREATE TABLE SYNONYME (
	"id" float NOT NULL,
	"name" varchar(255),
	"bin_id" float
);
ALTER TABLE SYNONYME ADD PRIMARY KEY ("id");

CREATE TABLE STRUCTURE (
	"id" float NOT NULL,
	"molare_mass" double precision,
	"smiles" varchar(2000),
	"formula" varchar(2000),
	"bin_id" float
);
ALTER TABLE STRUCTURE ADD PRIMARY KEY ("id");

CREATE TABLE BIN_GROUP (
	"group_id" float,
	"name" varchar(255),
	"description" varchar(1000)
);

CREATE TABLE RESULT (
	"result_id" float NOT NULL,
	"name" varchar(255),
	"setupx" varchar(255),
	"description" varchar(2000),
	"pattern" varchar(2000)
);
ALTER TABLE RESULT ADD PRIMARY KEY ("result_id");

CREATE TABLE COMMENTS (
	"id" bigint NOT NULL,
	"text" text,
	"discriminator" bigint,
	"type" float
);
ALTER TABLE COMMENTS ADD PRIMARY KEY ("id");
CREATE INDEX COMMENTS_TYPE_INDEX ON COMMENTS ("type");

CREATE TABLE RAWDATA (
	"name" varchar(255) NOT NULL,
	"data" bytea,
	"version" timestamp,
	"visible" varchar(5) DEFAULT 'TRUE',
	"hash" float NOT NULL
);
ALTER TABLE RAWDATA ADD PRIMARY KEY ("name","hash");

CREATE TABLE CONFIGURATION (
	"configuration_id" integer NOT NULL,
	"data" text,
	"version" integer
);
ALTER TABLE CONFIGURATION ADD PRIMARY KEY ("configuration_id");

CREATE TABLE BIN_REFERENCES (
	"id" integer NOT NULL,
	"bin_id" integer,
	"refrence_class" integer,
	"refrence_id" varchar(255)
);
ALTER TABLE BIN_REFERENCES ADD PRIMARY KEY ("id");

CREATE TABLE REFERENCE_CLASS (
	"id" integer NOT NULL,
	"name" varchar(255),
	"description" varchar(3000),
	"pattern" varchar(2000)
);
ALTER TABLE REFERENCE_CLASS ADD PRIMARY KEY ("id");

CREATE TABLE BIN_RATIO (
	"id" float,
	"bin_id" float,
	"main_ion" float,
	"secondaery_ion" float,
	"ratio" double precision,
	"min_ratio" double precision,
	"max_ratio" double precision
);

CREATE TABLE BIN (
	"bin_id" integer NOT NULL,
	"apex_sn" double precision,
	"apex" text,
	"export" varchar(255) DEFAULT 'FALSE',
	"generatequantmass" varchar(255) DEFAULT 'TRUE',
	"spectra" text,
	"minus" integer DEFAULT 2000,
	"name" varchar(255),
	"new" varchar(255) DEFAULT 'TRUE',
	"plus" integer DEFAULT 2000,
	"purity" double precision,
	"quantmass" integer,
	"retention_index" integer,
	"sample_id" integer,
	"spectra_id" integer,
	"uniquemass" integer,
	"signal_noise" double precision,
	"molare_mass" double precision,
	"group_id" float,
	"quality" varchar(6) DEFAULT 'FALSE'
);
ALTER TABLE BIN ADD PRIMARY KEY ("bin_id");
CREATE INDEX BIN_EXPORT_INDEX ON BIN ("export");

CREATE TABLE STANDARD (
	"bin_id" float NOT NULL,
	"qualifier" float NOT NULL,
	"min_ratio" double precision NOT NULL,
	"min_apex_sn" double precision NOT NULL,
	"max_ratio" double precision NOT NULL,
	"min_distance_ratio" double precision DEFAULT 0.9,
	"max_distance_ratio" double precision DEFAULT 1.1,
	"min_similarity" double precision DEFAULT 800,
	"required" varchar(5) DEFAULT 'TRUE'
);
ALTER TABLE STANDARD ADD PRIMARY KEY ("bin_id");
CREATE UNIQUE INDEX STANDARD_INDEX_BINID ON STANDARD ("bin_id");

CREATE TABLE USER_VALIDATION (
	"user_id" float,
	"sample_id" float
);
CREATE UNIQUE INDEX USER01 ON USER_VALIDATION ("user_id","sample_id");

CREATE TABLE METAINFORMATION (
	"setupxid" varchar(25) NOT NULL,
	"data" text
);

CREATE TABLE QUALITYCONTROL (
	"id" float NOT NULL,
	"amount" double precision NOT NULL
);

CREATE TABLE SAMPLE_INFO (
	"sample_id" float,
	"key_id" float,
	"value" varchar(2048),
	"id" float
);

CREATE TABLE VIRTUAL_BIN (
	"bin_id" integer NOT NULL,
	"parent_id" integer,
	"ion_a" integer,
	"ion_b" integer,
	"name" varchar(255),
	"ratio" double precision,
	"export" varchar(5) DEFAULT 'TRUE'
);
ALTER TABLE VIRTUAL_BIN ADD PRIMARY KEY ("bin_id");

CREATE TABLE CLASSIFICATION (
	"id" integer NOT NULL,
	"structure" integer,
	"class" integer
);
ALTER TABLE CLASSIFICATION ADD PRIMARY KEY ("id");

CREATE TABLE LIBRARY_SPEC (
	"specId" integer NOT NULL,
	"id" integer,
	"spectra" text,
	"name" varchar(255)
);
ALTER TABLE LIBRARY_SPEC ADD PRIMARY KEY ("specId");

CREATE TABLE LIBRARY (
	"id" integer NOT NULL,
	"description" varchar(255),
	"name" varchar(255)
);
ALTER TABLE LIBRARY ADD PRIMARY KEY ("id");

CREATE TABLE TYPE (
	"id" float NOT NULL,
	"name" varchar(255),
	"pattern" varchar(255) NOT NULL,
	"create_bin" varchar(5) DEFAULT 'FALSE',
	"description" text
);
ALTER TABLE TYPE ADD PRIMARY KEY ("id");

CREATE TABLE RUNTIME (
	"sample_id" float NOT NULL,
	"operation" varchar(255) NOT NULL,
	"start" double precision NOT NULL,
	"end" double precision NOT NULL,
	"group" varchar(255),
	"type" float
);

CREATE TABLE SAMPLES (
	"sample_id" integer NOT NULL,
	"configuration_id" integer,
	"correction_failed" varchar(255) DEFAULT 'FALSE' ,
	"created_bin" varchar(255) DEFAULT 'FALSE' ,
	"class" varchar(255),
	"sample_name" varchar(255),
	"allowNewBin" varchar(255) DEFAULT 'FALSE',
	"priority" integer DEFAULT 3,
	"saturated" varchar(255) DEFAULT 'FALSE',
	"setupx_id" varchar(255) DEFAULT 'not assigned',
	"status" integer DEFAULT 0,
	"version" integer NOT NULL,
	"finished" varchar(5) DEFAULT 'FALSE',
	"visible" varchar(5) DEFAULT 'TRUE',
	"date" timestamp,
	"sod" float DEFAULT 1 ,
	"type" float,
	"operator" varchar(25),
	"machine" varchar(25),
	"run_id" float,
	"date_of_import" timestamp,
	"msmethod" varchar(255),
	"dpmethod" varchar(255),
	"tray" float,
	"qcmethod" varchar(255),
	"asmethod" varchar(255),
	"gcmethod" varchar(255),
	"correctedWith" integer NULL
	
);
ALTER TABLE SAMPLES ADD PRIMARY KEY ("sample_id");

CREATE TABLE REFERENCE (
	"id" integer NOT NULL,
	"bin_id" integer,
	"value" varchar(3000),
	"class_id" float
);
ALTER TABLE REFERENCE ADD PRIMARY KEY ("id");
CREATE INDEX REFRENCE_BINID_INDEX ON REFERENCE ("bin_id");

CREATE TABLE SPECTRA (
	"uniquemass" double precision,
	"signal_noise" double precision,
	"purity" double precision,
	"retention_index" float,
	"retention_time" double precision,
	"sample_id" float,
	"spectra_id" float NOT NULL,
	"apex" text,
	"spectra" text,
	"apex_sn" double precision,
	"bin_id" float DEFAULT NULL,
	"match" double precision DEFAULT -1,
	"new_bin" varchar(6) DEFAULT 'FALSE',
	"found_at_correction" varchar(6) DEFAULT 'FALSE',
	"problematic" varchar(6) DEFAULT 'FALSE',
	"leco" varchar(25)
);
ALTER TABLE SPECTRA ADD PRIMARY KEY ("spectra_id");
CREATE UNIQUE INDEX SPECTRA_PRIMAERY ON SPECTRA ("spectra_id");
CREATE INDEX SPECTRA_INDEX_SAMPLE_BIN ON SPECTRA ("bin_id","sample_id");
CREATE INDEX SPECTRA_SAMPLE_ID ON SPECTRA ("sample_id");
CREATE INDEX SPECTRA_BINID ON SPECTRA ("bin_id");

CREATE TABLE SUBSTANCE_CLASSES (
	"id" integer NOT NULL,
	"name" varchar(255),
	"smile" varchar(1000),
	"describtion" varchar(3000)
);

CREATE TABLE correction_data ( 
    sample_id	int4 NULL,
    bin_id   	int4 NULL,
    x        	float8 NULL,
    y        	float8 NULL,
    position 	int4 NULL 
    )
 ;
ALTER TABLE SUBSTANCE_CLASSES ADD PRIMARY KEY ("id");

ALTER TABLE VIRTUAL_BIN ADD CONSTRAINT "FK454B6D93BDFDA0EB" FOREIGN KEY ("parent_id") REFERENCES BIN ("bin_id") ON DELETE NO ACTION NOT DEFERRABLE INITIALLY IMMEDIATE;



CREATE SEQUENCE BIN_ID INCREMENT 1 MINVALUE 1 MAXVALUE 2147483646 START 1 CACHE 20;


CREATE SEQUENCE COMMENT_ID INCREMENT 1 MINVALUE 1 MAXVALUE 2147483646 START 1 CACHE 20;


CREATE SEQUENCE RESULT_ID INCREMENT 1 MINVALUE 1 MAXVALUE 2147483646 START 1 CACHE 20;

CREATE SEQUENCE LINK_ID INCREMENT 1 MINVALUE 1 MAXVALUE 2147483646 START 1 CACHE 20;

CREATE SEQUENCE SPECTRA_ID INCREMENT 1 MINVALUE 1 MAXVALUE 2147483646 START 1 CACHE 20;

CREATE SEQUENCE HIBERNATE_SEQUENCE INCREMENT 1 MINVALUE 1 MAXVALUE 2147483646 START 1 CACHE 20;

CREATE SEQUENCE JOB_ID INCREMENT 1 MINVALUE 0 MAXVALUE 2147483646 START 1 CACHE 20;

CREATE SEQUENCE QUANTIFICATION_ID INCREMENT 1 MINVALUE 0 MAXVALUE 2147483646 START 1 CACHE 100;

CREATE SEQUENCE TYPE_ID INCREMENT 1 MINVALUE 1 MAXVALUE 2147483646 START 1 CACHE 20;

CREATE SEQUENCE SAMPLE_ID INCREMENT 1 MINVALUE 0 MAXVALUE 2147483646 START 1 CACHE 100;

CREATE VIEW BIN_SAMPLES (sample_id, configuration_id, correction_failed, created_bin, class, sample_name, allowNewBin, priority, saturated, setupx_id, status, version, finished, visible, date, sod, type) AS SELECT "sample_id","configuration_id","correction_failed","created_bin","class","sample_name","allowNewBin","priority","saturated","setupx_id","status","version","finished","visible","date","sod","type" FROM samples where "sample_id" in (select   "sample_id" from bin)
 ;
CREATE VIEW EXPERIMENT_CLASS (class) AS select "class" from SAMPLES group by "class"
 ;
CREATE VIEW SPECTRA_FOUND (uniquemass, signal_noise, purity, retention_index, retention_time, sample_id, spectra_id, apex, spectra, apex_sn, bin_id, match, new_bin, found_at_correction, leco) AS SELECT "uniquemass","signal_noise","purity","retention_index","retention_time","sample_id","spectra_id","apex","spectra","apex_sn","bin_id","match","new_bin","found_at_correction","leco" FROM spectra where "bin_id" is not null
 ;
CREATE VIEW SPECTRA_NOT_FOUND (uniquemass, signal_noise, purity, retention_index, retention_time, sample_id, spectra_id, apex, spectra, apex_sn) AS select "uniquemass","signal_noise","purity","retention_index","retention_time","sample_id","spectra_id","apex","spectra","apex_sn" from spectra where "bin_id" is null
 
 ;
CREATE VIEW view_virtualbin (ratio, ion_b, ion_a, bin_id, signal_noise, apex_sn, apex, spectra, retention_index, purity, spectra_id, quantmass, export, name) AS SELECT VIRTUAL_BIN."ratio", VIRTUAL_BIN."ion_b", 
VIRTUAL_BIN."ion_a", VIRTUAL_BIN."bin_id", BIN."signal_noise", BIN."apex_sn", BIN."apex", 
BIN."spectra", BIN."retention_index", BIN."purity", BIN."spectra_id", BIN."quantmass", VIRTUAL_BIN."export", VIRTUAL_BIN."name" FROM 
BIN,VIRTUAL_BIN WHERE BIN."bin_id"= VIRTUAL_BIN."parent_id"
 
   
 
 
 
 
 
 
 ;


CREATE INDEX sample_info_id
    ON sample_info(key_id)
;
CREATE INDEX sample_info_id_sample
    ON sample_info(sample_id)
;
CREATE INDEX sample_info_value
    ON sample_info(value)
;
