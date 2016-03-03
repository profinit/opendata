ALTER TABLE record ADD COLUMN amount_czk DOUBLE PRECISION NULL;
UPDATE record SET amount_czk = amount_czk_with_vat where amount_czk_with_vat is not null;
UPDATE record SET amount_czk = amount_czk_without_vat where amount_czk is null;
ALTER TABLE record DROP COLUMN amount_czk_with_vat;
ALTER TABLE record DROP COLUMN amount_czk_without_vat;