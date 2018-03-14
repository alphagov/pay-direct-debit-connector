--liquibase formatted sql

--changeset uk.gov.pay:alter_table-payers-address-line-1
ALTER TABLE payers ALTER COLUMN address_line1 DROP NOT NULL;
--rollback ALTER TABLE payers ALTER COLUMN address_line1 SET NOT NULL;

--changeset uk.gov.pay:alter_table-payers-address_line2
ALTER TABLE payers ALTER COLUMN address_line2 DROP NOT NULL;
--rollback ALTER TABLE payers ALTER COLUMN address_line2 SET NOT NULL;

--changeset uk.gov.pay:alter_table-payers-address_postcode
ALTER TABLE payers ALTER COLUMN address_postcode DROP NOT NULL;
--rollback ALTER TABLE payers ALTER COLUMN address_postcode SET NOT NULL;

--changeset uk.gov.pay:alter_table-payers-address_city
ALTER TABLE payers ALTER COLUMN address_city DROP NOT NULL;
--rollback ALTER TABLE payers ALTER COLUMN address_city SET NOT NULL;

--changeset uk.gov.pay:alter_table-payers-address_country
ALTER TABLE payers ALTER COLUMN address_country DROP NOT NULL;
--rollback ALTER TABLE payers ALTER COLUMN address_country SET NOT NULL;
