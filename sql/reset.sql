TRUNCATE TABLE partner_list_entry;
TRUNCATE TABLE unresolved_relationship;
TRUNCATE TABLE retrieval CASCADE ;
DELETE FROM entity WHERE is_public = FALSE;
UPDATE data_source SET last_processed_date = NULL;
UPDATE data_instance SET last_processed_date = NULL;