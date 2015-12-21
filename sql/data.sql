INSERT INTO entity(entity_type, name, ico, dic, is_public) VALUES ('ministry', 'Ministerstvo financí ČR', '00006947', 'CZ00006947', TRUE);

INSERT INTO data_source(entity_id, record_type, periodicity, handling_class, active, description) VALUES (
  (SELECT entity_id FROM entity WHERE name = 'Ministerstvo financí ČR'),
  'order', 'aperiodic', 'eu.profinit.opendata.institution.mfcr.MFCRHandler', TRUE, 'Objednávky MFČR');