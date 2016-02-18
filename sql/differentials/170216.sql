WITH msp AS (INSERT INTO entity(entity_type, name, ico, is_public) VALUES
  ('ministry', 'Ministerstvo spravedlnosti ČR', '00025429', TRUE) RETURNING entity_id),

dsid AS (
  INSERT INTO data_source (entity_id, record_type, periodicity, handling_class, active, description) VALUES (
  (SELECT entity_id FROM msp),
  'contract', 'aperiodic', 'eu.profinit.opendata.institution.justice.JusticeHandler', TRUE, 'Smlouvy MSp')
  RETURNING data_source_id
)

INSERT INTO data_instance(data_source_id, url, format, periodicity, description, mapping_file) VALUES (
  (SELECT  data_source_id FROM dsid), 'http://data.justice.cz/Personln%20informace/Smlouvy%20MSp%20ke%20sta%C5%BEen%C3%AD.xlsx',
  'xlsx', 'quarterly', 'Smlouvy MSp 2011 - 2015', 'mappings/justice/mapping-contracts.xml'
);