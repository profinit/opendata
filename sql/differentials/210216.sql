INSERT INTO periodicity (periodicity) VALUES ('weekly');

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

ALTER TABLE record ADD periodicity VARCHAR(50) NULL;

ALTER TABLE record ADD CONSTRAINT "FK_record_periodicity"
FOREIGN KEY ("periodicity") REFERENCES "periodicity" ("periodicity") ON DELETE No Action ON UPDATE No Action
;

ALTER TABLE entity ALTER COLUMN name TYPE VARCHAR(2000);