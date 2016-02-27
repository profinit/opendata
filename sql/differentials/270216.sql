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