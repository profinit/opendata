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

-- Justice: Manual data instance for contracts, others are automatic
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

-- MZP: All data instances are manual

WITH mzp AS (INSERT INTO entity(entity_type, name, ico, is_public) VALUES
  ('ministry', 'Ministerstvo životního prostředí', '00164801', TRUE) RETURNING entity_id),

    orders_ds AS (
    INSERT INTO data_source (entity_id, record_type, periodicity, handling_class, active, description) VALUES (
      (SELECT entity_id FROM mzp),
      'order', 'aperiodic', 'eu.profinit.opendata.institution.mzp.MZPHandler', TRUE, 'Objednávky MŽP')
    RETURNING data_source_id
  ),

    contracts_ds AS (
    INSERT INTO data_source (entity_id, record_type, periodicity, handling_class, active, description) VALUES (
      (SELECT entity_id FROM mzp),
      'contract', 'aperiodic', 'eu.profinit.opendata.institution.mzp.MZPHandler', TRUE, 'Smlouvy MŽP')
    RETURNING data_source_id
  ),

    invoices_ds AS (
    INSERT INTO data_source (entity_id, record_type, periodicity, handling_class, active, description) VALUES (
      (SELECT entity_id FROM mzp),
      'invoice', 'aperiodic', 'eu.profinit.opendata.institution.mzp.MZPHandler', TRUE, 'Faktury MŽP')
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