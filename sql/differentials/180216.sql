ALTER TABLE data_instance ADD incremental BOOLEAN NOT NULL DEFAULT TRUE;
UPDATE data_instance SET incremental = FALSE WHERE url = 'http://data.justice.cz/Personln%20informace/Smlouvy%20MSp%20ke%20sta%C5%BEen%C3%AD.xlsx';