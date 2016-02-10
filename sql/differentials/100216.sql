INSERT INTO record_type (record_type) VALUES ('payment');
ALTER TABLE record ADD budget_category VARCHAR(500);
ALTER TABLE data_instance ADD mapping_file VARCHAR(255);