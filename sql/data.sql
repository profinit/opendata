--MFCR-------------------------------------------------------------------------
INSERT INTO entity(entity_type, name, ico, dic, is_public) VALUES ('ministry', 'Ministerstvo financí ČR', '00006947', 'CZ00006947', TRUE);

-- MFCR: DataInstances must be checked every time extraction runs
INSERT INTO data_source(entity_id, record_type, periodicity, handling_class, active, description) VALUES (
  (SELECT entity_id FROM entity WHERE name = 'Ministerstvo financí ČR'),
  'order', 'daily', 'eu.profinit.opendata.institution.mfcr.MFCRHandler', TRUE, 'Objednávky MFČR');

INSERT INTO data_source(entity_id, record_type, periodicity, handling_class, active, description) VALUES (
  (SELECT entity_id FROM entity WHERE name = 'Ministerstvo financí ČR'),
  'invoice', 'daily', 'eu.profinit.opendata.institution.mfcr.MFCRHandler', TRUE, 'Faktury MFČR');

INSERT INTO data_source(entity_id, record_type, periodicity, handling_class, active, description) VALUES (
  (SELECT entity_id FROM entity WHERE name = 'Ministerstvo financí ČR'),
  'contract', 'daily', 'eu.profinit.opendata.institution.mfcr.MFCRHandler', TRUE, 'Smlouvy MFČR');

-- Justice: Manual data instance for contracts, others are automatic----------------------------
WITH msp AS (INSERT INTO entity(entity_type, name, ico, is_public) VALUES
  ('ministry', 'Ministerstvo spravedlnosti ČR', '00025429', TRUE) RETURNING entity_id),

dsid AS (
  INSERT INTO data_source (entity_id, record_type, periodicity, handling_class, active, description) VALUES (
  (SELECT entity_id FROM msp),
  'contract', 'aperiodic', 'eu.profinit.opendata.institution.justice.JusticeHandler', TRUE, 'Smlouvy MSp')
  RETURNING data_source_id
),

dsid2 AS (
    INSERT INTO data_source (entity_id, record_type, periodicity, handling_class, active, description) VALUES (
    (SELECT entity_id FROM msp),
    'invoice', 'quarterly', 'eu.profinit.opendata.institution.justice.JusticeHandler', TRUE, 'Faktury MSp')
    RETURNING data_source_id
)

INSERT INTO data_instance(data_source_id, url, format, periodicity, description, mapping_file, incremental) VALUES (
  (SELECT  data_source_id FROM dsid), 'http://data.justice.cz/Personln%20informace/Smlouvy%20MSp%20ke%20sta%C5%BEen%C3%AD.xlsx',
  'xlsx', 'quarterly', 'Smlouvy MSp 2011 - 2015', 'mappings/justice/mapping-contracts.xml', false
);

-- MZP: All data instances are manual----------------------------

WITH mzp AS (INSERT INTO entity(entity_type, name, ico, is_public) VALUES
  ('ministry', 'Ministerstvo životního prostředí', '00164801', TRUE) RETURNING entity_id),

    orders_ds AS (
    INSERT INTO data_source (entity_id, record_type, periodicity, handling_class, active, description) VALUES (
      (SELECT entity_id FROM mzp),
      'order', 'aperiodic', 'eu.profinit.opendata.control.BlankHandler', TRUE, 'Objednávky MŽP')
    RETURNING data_source_id
  ),

    contracts_ds AS (
    INSERT INTO data_source (entity_id, record_type, periodicity, handling_class, active, description) VALUES (
      (SELECT entity_id FROM mzp),
      'contract', 'aperiodic', 'eu.profinit.opendata.control.BlankHandler', TRUE, 'Smlouvy MŽP')
    RETURNING data_source_id
  ),

    invoices_ds AS (
    INSERT INTO data_source (entity_id, record_type, periodicity, handling_class, active, description) VALUES (
      (SELECT entity_id FROM mzp),
      'invoice', 'aperiodic', 'eu.profinit.opendata.control.BlankHandler', TRUE, 'Faktury MŽP')
    RETURNING data_source_id
  )

INSERT INTO data_instance(data_source_id, url, format, periodicity, description, mapping_file, incremental) VALUES
  (
    (SELECT  data_source_id FROM orders_ds), 'http://www.mzp.cz/AIS/smlouvy-web.nsf/exportOrdersValidAsXLSX.xsp',
    'xlsx', 'weekly', 'Průběžné objednávky MŽP', 'mappings/mzp/mapping-orders.xml', FALSE
  ),

  (
    (SELECT  data_source_id FROM contracts_ds), 'http://www.mzp.cz/AIS/smlouvy-web.nsf/exportContractsValidAsXLSX.xsp',
    'xlsx', 'weekly', 'Platné smlouvy MŽP', 'mappings/mzp/mapping-contracts.xml', FALSE
  ),

  (
    (SELECT  data_source_id FROM invoices_ds), 'http://www.mzp.cz/AIS/smlouvy-web.nsf/exportInvoicesAsXLSX.xsp',
    'xlsx', 'weekly', 'Průběžné faktury MŽP', 'mappings/mzp/mapping-invoices.xml', FALSE
  );

-- MK: Data instances are manual and must be periodically updated ----------------------------

WITH mk AS (INSERT INTO entity(entity_type, name, ico, dic, is_public) VALUES
  ('ministry', 'Ministerstvo kultury ČR', '00023671', 'CZ00023671', TRUE) RETURNING entity_id),

    contracts_ds AS (
    INSERT INTO data_source (entity_id, record_type, periodicity, handling_class, active, description) VALUES (
      (SELECT entity_id FROM mk),
      'contract', 'aperiodic', 'eu.profinit.opendata.control.BlankHandler', TRUE, 'Smlouvy MK')
    RETURNING data_source_id
  ),

    invoices_ds AS (
    INSERT INTO data_source (entity_id, record_type, periodicity, handling_class, active, description) VALUES (
      (SELECT entity_id FROM mk),
      'invoice', 'aperiodic', 'eu.profinit.opendata.control.BlankHandler', TRUE, 'Faktury MK')
    RETURNING data_source_id
  )

INSERT INTO data_instance(data_source_id, url, format, periodicity, description, mapping_file, incremental) VALUES

  (
    (SELECT  data_source_id FROM contracts_ds), 'http://www.mkcr.cz/assets/povinne-zverejnovane-informace/MK-smlouvy-2015-03-16.xlsx',
    'xlsx', 'aperiodic', 'Platné smlouvy MK k 31. 1. 2015', 'mappings/mk/mapping-contracts.xml', FALSE
  ),

  (
    (SELECT  data_source_id FROM invoices_ds), 'http://www.mkcr.cz/assets/povinne-zverejnovane-informace/Uhrazene-faktury-dobropisy-a-platebni-poukazy_leden_2015.xlsx',
    'xlsx', 'aperiodic', 'Faktury MK leden 2015', 'mappings/mk/mapping-invoices.xml', FALSE
  );

-- MMR: Data instances are manual and experimentally periodic, but we don't know how updates are published. ----------------------------

WITH mmr AS (INSERT INTO entity(entity_type, name, ico, is_public) VALUES
  ('ministry', 'Ministerstvo pro místní rozvoj', '66002222', TRUE) RETURNING entity_id),

    contracts_ds AS (
    INSERT INTO data_source (entity_id, record_type, periodicity, handling_class, active, description) VALUES (
      (SELECT entity_id FROM mmr),
      'contract', 'aperiodic', 'eu.profinit.opendata.control.BlankHandler', TRUE, 'Smlouvy MMR')
    RETURNING data_source_id
  ),

    invoices_ds AS (
    INSERT INTO data_source (entity_id, record_type, periodicity, handling_class, active, description) VALUES (
      (SELECT entity_id FROM mmr),
      'invoice', 'aperiodic', 'eu.profinit.opendata.control.BlankHandler', TRUE, 'Faktury MMR')
    RETURNING data_source_id
  ),

    orders_ds AS (
    INSERT INTO data_source (entity_id, record_type, periodicity, handling_class, active, description) VALUES (
      (SELECT entity_id FROM mmr),
      'order', 'aperiodic', 'eu.profinit.opendata.control.BlankHandler', TRUE, 'Objednávky MMR')
    RETURNING data_source_id
  )

INSERT INTO data_instance(data_source_id, url, format, periodicity, description, mapping_file, incremental) VALUES

  (
    (SELECT  data_source_id FROM contracts_ds), 'http://www.mmr.cz/getmedia/3418880e-894f-4cff-9e9e-62a172394c85/Smlouvy_1.xlsx',
    'xlsx', 'monthly', 'Smlouvy MMR od 1. 1. 2015', 'mappings/mmr/mapping-contracts.xml', FALSE
  ),

  (
    (SELECT  data_source_id FROM invoices_ds), 'http://www.mmr.cz/getmedia/30c36454-8062-4649-9b55-fbb89d1c86e5/Faktury-2015_2.xlsx',
    'xlsx', 'monthly', 'Faktury MMR od 1. 1. 2015', 'mappings/mmr/mapping-invoices.xml', FALSE
  ),

  (
    (SELECT  data_source_id FROM orders_ds), 'http://www.mmr.cz/getmedia/2a2ea9c8-e6d5-46f5-8d7b-f961b8fabbce/Objednavky_2.xlsx',
    'xlsx', 'monthly', 'Objednávky MMR od 1. 1. 2015', 'mappings/mmr/mapping-orders.xml', FALSE
  );
  
--MOCR-------------------------------------------------------------------------
INSERT INTO entity(entity_type, name, ico, dic, is_public) VALUES ('ministry', 'Ministerstvo obrany ČR', '60162694', 'CZ60162694', TRUE);

-- MOCR: DataInstances must be checked every time extraction runs

INSERT INTO data_source(entity_id, record_type, periodicity, handling_class, active, description) VALUES (
  (SELECT entity_id FROM entity WHERE name = 'Ministerstvo obrany ČR'),
  'invoice', 'daily', 'eu.profinit.opendata.institution.mocr.MOCRHandler', TRUE, 'Faktury MOČR');

INSERT INTO data_source(entity_id, record_type, periodicity, handling_class, active, description) VALUES (
  (SELECT entity_id FROM entity WHERE name = 'Ministerstvo obrany ČR'),
  'contract', 'daily', 'eu.profinit.opendata.institution.mocr.MOCRHandler', TRUE, 'Smlouvy MOČR');

-- MDCR: Data instances are manual and experimentally periodic, but we don't know how updates are published. ----------------------------

WITH mdcr AS (INSERT INTO entity(entity_type, name, ico, dic, is_public) VALUES
  ('ministry', 'Ministerstvo dopravy', '66003008', 'CZ66003008', TRUE) RETURNING entity_id),

    contracts_ds AS (
    INSERT INTO data_source (entity_id, record_type, periodicity, handling_class, active, description) VALUES (
      (SELECT entity_id FROM mdcr),
      'contract', 'aperiodic', 'eu.profinit.opendata.control.BlankHandler', TRUE, 'Smlouvy MDČR')
    RETURNING data_source_id
  ),

    invoices_ds AS (
    INSERT INTO data_source (entity_id, record_type, periodicity, handling_class, active, description) VALUES (
      (SELECT entity_id FROM mdcr),
      'invoice', 'aperiodic', 'eu.profinit.opendata.control.BlankHandler', TRUE, 'Faktury MDČR')
    RETURNING data_source_id
  )

INSERT INTO data_instance(data_source_id, url, format, periodicity, description, mapping_file, incremental) VALUES

  (
    (SELECT  data_source_id FROM contracts_ds), 'http://www.mdcr.cz/NR/rdonlyres/8B067F15-9D7F-4307-9133-9A8EC47AD86F/0/smlouvy_md_2015.xls',
    'xlsx', 'yearly', 'Smlouvy MDČR 2015', 'mappings/mdcr/mapping-contracts.xml', FALSE
  ),
  
  (
    (SELECT  data_source_id FROM contracts_ds), 'http://www.mdcr.cz/NR/rdonlyres/F6485532-145E-4073-A399-9D633462265E/0/smlouvy_md_2016.xls',
    'xlsx', 'yearly', 'Smlouvy MDČR 2015', 'mappings/mdcr/mapping-contracts.xml', FALSE
  ),
  
  (
    (SELECT  data_source_id FROM invoices_ds), 'http://www.mdcr.cz/NR/rdonlyres/F8B84F41-9237-4049-9A53-CD373AC43F17/0/faktury_md_2015.xls',
    'xlsx', 'yearly', 'Faktury MDČR 2015', 'mappings/mdcr/mapping-invoices.xml', FALSE
  ),
  
  (
    (SELECT  data_source_id FROM invoices_ds), 'http://www.mdcr.cz/NR/rdonlyres/F6452068-EBB7-4338-BA49-43E563DD919A/0/faktury_md_2016.xls',
    'xlsx', 'yearly', 'Faktury MDČR 2016', 'mappings/mdcr/mapping-invoices.xml', FALSE
  );