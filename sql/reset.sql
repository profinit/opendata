DELETE FROM retrieval;
DELETE FROM data_instance;
DELETE FROM partner_list_entry;
DELETE FROM entity WHERE is_public = FALSE;
UPDATE data_source SET last_processed_date = NULL;