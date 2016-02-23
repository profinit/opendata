UPDATE data_source SET handling_class = 'eu.profinit.opendata.control.GenericDataSourceHandler'
  WHERE handling_class = 'eu.profinit.opendata.institution.mzp.MZPHandler';


WITH mk AS (INSERT INTO entity(entity_type, name, ico, dic, is_public) VALUES
  ('ministry', 'Ministerstvo kultury ČR', '00023671', 'CZ00023671', TRUE) RETURNING entity_id),

    contracts_ds AS (
    INSERT INTO data_source (entity_id, record_type, periodicity, handling_class, active, description) VALUES (
      (SELECT entity_id FROM mk),
      'contract', 'aperiodic', 'eu.profinit.opendata.control.GenericDataSourceHandler', TRUE, 'Smlouvy MK')
    RETURNING data_source_id
  ),

    invoices_ds AS (
    INSERT INTO data_source (entity_id, record_type, periodicity, handling_class, active, description) VALUES (
      (SELECT entity_id FROM mk),
      'invoice', 'aperiodic', 'eu.profinit.opendata.control.GenericDataSourceHandler', TRUE, 'Faktury MK')
    RETURNING data_source_id
  )

INSERT INTO data_instance(data_source_id, url, format, periodicity, description, mapping_file, incremental) VALUES

  (
    (SELECT  data_source_id FROM contracts_ds), 'http://www.mkcr.cz/assets/povinne-zverejnovane-informace/MK-smlouvy-2015-03-16.xlsx',
    'xlsx', 'aperiodic', 'Platné smlouvy MK k 31. 1. 2015', 'mappings/mk/mapping-contracts.xml', FALSE
  ),

  (
    (SELECT  data_source_id FROM invoices_ds), 'http://www.mkcr.cz/assets/povinne-zverejnovane-informace/Uhrazene-faktury-dobropisy-a-platebni-poukazy_leden_2015.xlsx',
    'xlsx', 'aperiodic', 'Faktury MK leden 2015', 'mappings/mk/mapping-invoices.xml', TRUE
  );