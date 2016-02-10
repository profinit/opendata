/* ---------------------------------------------------- */
/*  Generated by Enterprise Architect Version 12.0 		*/
/*  Created On : 19-XI-2015 9:54:06 				*/
/*  DBMS       : PostgreSQL 						*/
/* ---------------------------------------------------- */

/* Drop Sequences for Autonumber Columns */

DROP SEQUENCE IF EXISTS "retrieval_retrieval_id_seq"
;

DROP SEQUENCE IF EXISTS "record_record_id_seq"
;

DROP SEQUENCE IF EXISTS "entity_entity_id_seq"
;

DROP SEQUENCE IF EXISTS "data_source_data_source_id_seq"
;

DROP SEQUENCE IF EXISTS "data_instance_data_instance_id_seq"
;

DROP SEQUENCE IF EXISTS "data_instance_data_source_id_seq"
;

/* Drop Tables */

DROP TABLE IF EXISTS "retrieval" CASCADE
;

DROP TABLE IF EXISTS "record_type" CASCADE
;

DROP TABLE IF EXISTS "record" CASCADE
;

DROP TABLE IF EXISTS "periodicity" CASCADE
;

DROP TABLE IF EXISTS "entity_type" CASCADE
;

DROP TABLE IF EXISTS "entity" CASCADE
;

DROP TABLE IF EXISTS "data_source" CASCADE
;

DROP TABLE IF EXISTS "data_instance" CASCADE
;

DROP TABLE IF EXISTS "authority_role" CASCADE
;

/* Create Tables */

CREATE TABLE "retrieval"
(
	"date" timestamp without time zone NOT NULL,
	"failure_reason" varchar(1000)	 NULL,
	"num_bad_records" int NOT NULL,
	"num_records_inserted" int NOT NULL,
	"success" boolean NOT NULL,
	"retrieval_id" integer NOT NULL DEFAULT nextval(('"retrieval_retrieval_id_seq"'::text)::regclass),
	"data_instance_id" integer NULL
)
;

CREATE TABLE "record_type"
(
	"record_type" varchar(50)	 NOT NULL
)
;

CREATE TABLE "record"
(
	"amount_czk_with_vat" double precision NULL,
	"amount_czk_without_vat" double precision NULL,
	"authority_identifier" varchar(50)	 NULL,
	"currency" varchar(3)	 NOT NULL,
	"date_created" date NOT NULL,
	"date_of_expiry" date NULL,
	"date_of_payment" date NULL,
	"due_date" date NULL,
	"in_effect" boolean NULL,
	"master_id" varchar(50)	 NOT NULL,
	"original_currency_amount" double precision NULL,
	"partner_code" varchar(50) NULL,
	"subject" varchar(5000)	 NULL,
	"variable_symbol" varchar(50)	 NULL,
	"record_id" integer NOT NULL DEFAULT nextval(('"record_record_id_seq"'::text)::regclass),
	"partner" integer NULL,
	"authority" integer NULL,
	"parent_id" integer NULL,
	"retrieval_id" integer NULL,
	"record_type" varchar(50)	 NOT NULL,
	"authority_role" varchar(50)	 NULL,
	"budget_category" varchar(500) NULL
)
;

CREATE TABLE "periodicity"
(
	"periodicity" varchar(50)	 NOT NULL
)
;

CREATE TABLE "entity_type"
(
	"entity_type" varchar(50)	 NOT NULL
)
;

CREATE TABLE "entity"
(
	"dic" varchar(50)	 NULL,
	"ico" varchar(50)	 NULL,
	"is_public" boolean NOT NULL,
	"name" varchar(255)	 NOT NULL,
	"entity_id" integer NOT NULL DEFAULT nextval(('"entity_entity_id_seq"'::text)::regclass),
	"entity_type" varchar(50)	 NOT NULL
)
;

CREATE TABLE "data_source"
(
	"last_processed_date" timestamp without time zone NULL,
	"data_source_id" integer NOT NULL DEFAULT nextval(('"data_source_data_source_id_seq"'::text)::regclass),
	"entity_id" integer NOT NULL,
	"record_type" varchar(50)	 NOT NULL,
	"periodicity" varchar(50)	 NOT NULL,
	"handling_class" varchar(100)	 NULL,
	"active" boolean NOT NULL,
	"description" varchar(255)	 NULL
)
;

CREATE TABLE "data_instance"
(
	"format" varchar(6)	 NOT NULL,
	"url" varchar(255)	 NOT NULL,
	"data_instance_id" integer NOT NULL DEFAULT nextval(('"data_instance_data_instance_id_seq"'::text)::regclass),
	"data_source_id" integer NOT NULL DEFAULT nextval(('"data_instance_data_source_id_seq"'::text)::regclass),
	"periodicity" varchar(50)	 NOT NULL,
	"last_processed_date" timestamp without time zone NULL,
	"expires" date NULL,
	"last_processed_row" integer NULL,
	"authority_id" varchar(255) NULL,
	"description" varchar(255) NULL
)
;

CREATE TABLE "authority_role"
(
	"authority_role" varchar(50)	 NOT NULL
)
;

/* Create Table Comments, Sequences for Autonumber Columns */

CREATE SEQUENCE "retrieval_retrieval_id_seq" INCREMENT 1 START 1
;

CREATE SEQUENCE "record_record_id_seq" INCREMENT 1 START 1
;

CREATE SEQUENCE "entity_entity_id_seq" INCREMENT 1 START 1
;

CREATE SEQUENCE "data_source_data_source_id_seq" INCREMENT 1 START 1
;

CREATE SEQUENCE "data_instance_data_instance_id_seq" INCREMENT 1 START 1
;

CREATE SEQUENCE "data_instance_data_source_id_seq" INCREMENT 1 START 1
;

/* Create Primary Keys, Indexes, Uniques, Checks */

ALTER TABLE "retrieval" ADD CONSTRAINT "PK_retrieval"
	PRIMARY KEY ("retrieval_id")
;

ALTER TABLE "record_type" ADD CONSTRAINT "PK_record_type"
	PRIMARY KEY ("record_type")
;

ALTER TABLE "record" ADD CONSTRAINT "PK_record"
	PRIMARY KEY ("record_id")
;

ALTER TABLE "periodicity" ADD CONSTRAINT "PK_Periodicity"
	PRIMARY KEY ("periodicity")
;

ALTER TABLE "entity_type" ADD CONSTRAINT "PK_entity_type"
	PRIMARY KEY ("entity_type")
;

ALTER TABLE "entity" ADD CONSTRAINT "PK_entity"
	PRIMARY KEY ("entity_id")
;

ALTER TABLE "data_source" ADD CONSTRAINT "PK_data_source"
	PRIMARY KEY ("data_source_id")
;

ALTER TABLE "data_instance" ADD CONSTRAINT "PK_data_instance"
	PRIMARY KEY ("data_instance_id")
;

ALTER TABLE "authority_role" ADD CONSTRAINT "PK_authority_role"
	PRIMARY KEY ("authority_role")
;

/* Create Foreign Key Constraints */

ALTER TABLE "retrieval" ADD CONSTRAINT "FK_retrieval_data_instance"
	FOREIGN KEY ("data_instance_id") REFERENCES "data_instance" ("data_instance_id") ON DELETE Cascade ON UPDATE Cascade
;

ALTER TABLE "record" ADD CONSTRAINT "FK_record_authority_role"
	FOREIGN KEY ("authority_role") REFERENCES "authority_role" ("authority_role") ON DELETE No Action ON UPDATE No Action
;

ALTER TABLE "record" ADD CONSTRAINT "FK_record_record_type"
	FOREIGN KEY ("record_type") REFERENCES "record_type" ("record_type") ON DELETE No Action ON UPDATE No Action
;

ALTER TABLE "record" ADD CONSTRAINT "FK_partner"
	FOREIGN KEY ("partner") REFERENCES "entity" ("entity_id") ON DELETE Set Null ON UPDATE Cascade
;

ALTER TABLE "record" ADD CONSTRAINT "FK_authority"
	FOREIGN KEY ("authority") REFERENCES "entity" ("entity_id") ON DELETE Set Null ON UPDATE Cascade
;

ALTER TABLE "record" ADD CONSTRAINT "FK_record_parent"
	FOREIGN KEY ("parent_id") REFERENCES "record" ("record_id") ON DELETE Set Null ON UPDATE Cascade
;

ALTER TABLE "record" ADD CONSTRAINT "FK_record_retrieval"
	FOREIGN KEY ("retrieval_id") REFERENCES "retrieval" ("retrieval_id") ON DELETE Cascade ON UPDATE Cascade
;

ALTER TABLE "entity" ADD CONSTRAINT "FK_entity_entity_type"
	FOREIGN KEY ("entity_type") REFERENCES "entity_type" ("entity_type") ON DELETE No Action ON UPDATE No Action
;

ALTER TABLE "data_source" ADD CONSTRAINT "FK_data_source_periodicity"
	FOREIGN KEY ("periodicity") REFERENCES "periodicity" ("periodicity") ON DELETE No Action ON UPDATE No Action
;

ALTER TABLE "data_source" ADD CONSTRAINT "FK_data_source_record_type"
	FOREIGN KEY ("record_type") REFERENCES "record_type" ("record_type") ON DELETE No Action ON UPDATE No Action
;

ALTER TABLE "data_source" ADD CONSTRAINT "FK_data_source_entity"
	FOREIGN KEY ("entity_id") REFERENCES "entity" ("entity_id") ON DELETE Cascade ON UPDATE Cascade
;

ALTER TABLE "data_instance" ADD CONSTRAINT "FK_data_instance_periodicity"
	FOREIGN KEY ("periodicity") REFERENCES "periodicity" ("periodicity") ON DELETE No Action ON UPDATE No Action
;

ALTER TABLE "data_instance" ADD CONSTRAINT "FK_data_instance_data_source"
	FOREIGN KEY ("data_source_id") REFERENCES "data_source" ("data_source_id") ON DELETE Cascade ON UPDATE Cascade
;
