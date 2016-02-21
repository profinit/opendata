DELETE FROM partner_list_entry;
DELETE FROM unresolved_relationship;
DELETE FROM retrieval;
DELETE FROM entity WHERE is_public = FALSE;
UPDATE data_source SET last_processed_date = NULL;
UPDATE data_instance SET last_processed_date = NULL;