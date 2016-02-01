/* Fills tables representing enumerated types */

INSERT INTO authority_role (authority_role) VALUES ('supplier');
INSERT INTO authority_role (authority_role) VALUES ('customer');

INSERT INTO record_type (record_type) VALUES ('contract');
INSERT INTO record_type (record_type) VALUES ('invoice');
INSERT INTO record_type (record_type) VALUES ('order');

INSERT INTO periodicity (periodicity) VALUES ('daily');
INSERT INTO periodicity (periodicity) VALUES ('yearly');
INSERT INTO periodicity (periodicity) VALUES ('monthly');
INSERT INTO periodicity (periodicity) VALUES ('quarterly');
INSERT INTO periodicity (periodicity) VALUES ('aperiodic');

INSERT INTO entity_type (entity_type) VALUES ('ministry');
INSERT INTO entity_type (entity_type) VALUES ('company');
INSERT INTO entity_type (entity_type) VALUES ('individual');
INSERT INTO entity_type (entity_type) VALUES ('other');
