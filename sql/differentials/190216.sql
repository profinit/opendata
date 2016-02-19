INSERT INTO data_source (entity_id, record_type, periodicity, handling_class, active, description) VALUES (
  (SELECT entity_id FROM entity WHERE ico = '00025429'),
  'invoice', 'quarterly', 'eu.profinit.opendata.institution.justice.JusticeHandler', TRUE, 'Faktury MSp')
;