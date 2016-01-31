INSERT INTO entity(entity_type, name, ico, dic, is_public) VALUES ('ministry', 'Ministerstvo financí ČR', '00006947', 'CZ00006947', TRUE);

-- MFCR: DataInstances must be checked every time extraction runs
INSERT INTO data_source(entity_id, record_type, periodicity, handling_class, active, description) VALUES (
  (SELECT entity_id FROM entity WHERE name = 'Ministerstvo financí ČR'),
  'order', 'daily', 'eu.profinit.opendata.institution.mfcr.MFCRHandler', TRUE, 'Objednávky MFČR');

INSERT INTO data_source(entity_id, record_type, periodicity, handling_class, active, description) VALUES (
  (SELECT entity_id FROM entity WHERE name = 'Ministerstvo financí ČR'),
  'invoice', 'daily', 'eu.profinit.opendata.institution.mfcr.MFCRHandler', TRUE, 'Faktury MFČR');